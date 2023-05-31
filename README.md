

初学者完成SpringBoot+MySQL+MyBatisPlus+Liquibase，从0开始实现CRUD，前后端分离，部署到linux

用于记录与分享自己的学习过程

因为我是初次接触，而且是后续完善整理的文档，所以
- 可能出现垃圾代码，小白问题
- 流程顺序方面可能会有一些问题
- 在账密配置，服务器地址等信息可能不对应
- 整理时把报错和备注放到步骤后面了，这部分仅个人记录，不在流程内


# 环境


开发环境
- Windows 10 22H2
- java，jdk-19，以前装的
- IntelliJ IDEA 2023.1.1，主要开发工具，有新ui
- Visual Studio Code，部分文件编辑，写了个简单的测试前端
- Navicat，查看数据库情况，生成测试数据，idea也能看，这个方便
- ssh工具，远程连接
- VMware Workstation，本地测试（不想在win端安装mysql），安装了ubuntu-23.04-live-server-amd64
- mysql，在虚拟机ubuntu直接使用apt安装，Ver 8.0.33
- 可以正常访问外网的代理，用于idea依赖下载，查阅资料等


服务器环境
- 轻量应用服务器
- Ubuntu 22.04.2 LTS
- mysql，在ubuntu直接使用apt安装，Ver 8.0.33




# 本地虚拟机内linux


在VMware Workstation Pro，安装ubuntu，详细安装过程略
- 镜像为阿里源
- https://mirrors.aliyun.com/ubuntu-releases/23.04
- ubuntu-23.04-live-server-amd64.iso
- 4h8g，40g硬盘
- 网络为桥接，方便后续在局域网ssh连接
- 安装过程中可选修改更新源为国内源

安装完成后使用ssh连接


## 安装mysql


```bash

# 参考资料
# https://www.sjkjc.com/mysql/install-on-ubuntu/

# 更新软件仓库包索引
sudo apt update

# 升级本地软件
sudo apt upgrade

# 安装 MySQL
sudo apt install mysql-server

# 启动 MySQL 服务器
sudo systemctl start mysql

```



## 初始化mysql，安全配置，mysql_secure_installation


先修改默认账号root的登陆方式，防止mysql_secure_installation出错

```bash

# 接入mysql
sudo mysql

# --------------------sql--------------------

# 查看数据库
show databases;
# 列出账号
SELECT user, host FROM mysql.user;

# 修改root登陆方式
ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password by 'my-secret-password';

SELECT user, host FROM mysql.user;

quit
# --------------------sql--------------------

```

之后执行mysql_secure_installation

```bash

sudo mysql_secure_installation

# root输密码
# 都选y
# 密码复杂度选最简单，然后设置密码

```


## 新建mysql管理账号


一些模板，无需执行，可参考

```sql

-- 创建账号
-- username，账户名
-- password，密码
-- %，任意主机
-- localhost，本地
-- mysql_native_password是明确指定身份验证插件
CREATE USER 'username'@'localhost' IDENTIFIED BY 'password';
CREATE USER 'username'@'%' IDENTIFIED BY 'password';
CREATE USER 'username'@'%' IDENTIFIED WITH mysql_native_password BY 'password';

-- 修改权限
-- WITH GRANT OPTION，表示该用户可以将他们拥有的权限授权给其他用户
GRANT ALL PRIVILEGES ON *.* TO 'username'@'localhost' WITH GRANT OPTION;
GRANT ALL PRIVILEGES ON *.* TO 'username'@'%';

```

实际操作

```bash

sudo mysql -u root -p
# 输密码

# --------------------sql--------------------

SELECT user, host FROM mysql.user;

# 创建可远程登陆的admin
CREATE USER 'admin'@'%' IDENTIFIED BY 'password';
# 授予admin全部权限
GRANT ALL PRIVILEGES ON *.* TO 'admin'@'%';

# 创建本地账号springboot
CREATE USER 'springboot'@'localhost' IDENTIFIED BY '00000000';
# 授予只允许更改指定数据库test_a
GRANT ALL PRIVILEGES ON test_a.* TO 'springboot'@'localhost';

SELECT user, host FROM mysql.user;

# 刷新权限管理缓存
FLUSH PRIVILEGES;

# --------------------sql--------------------

```


## 允许mysql被远程连接

```bash

# 查看端口占用
netstat -lntp

# 不是这个文件，但里面指明了去哪找
cat /etc/mysql/my.cnf

# 修改配置，注释掉 bind-address = 127.0.0.1
sudo vim /etc/mysql/mysql.conf.d/mysqld.cnf

# 重启服务
sudo systemctl restart mysql

netstat -lntp

```

至此虚拟机内环境配置完成


## 备注，控制mysql运行


```bash

查看 MySQL 服务器状态
sudo systemctl status mysql

启动 MySQL 服务器
sudo systemctl start mysql

停止 MySQL 服务器
sudo systemctl stop mysql

重启 MySQL 服务器
sudo systemctl restart mysql

配置 MySQL 服务器自启动
sudo systemctl enable mysql

```



## 备注，mysql配置文件

```bash

# 编辑MySQL配置文件my.cnf
cat /etc/mysql/my.cnf

#
# The MySQL database server configuration file.
#
# You can copy this to one of:
# - "/etc/mysql/my.cnf" to set global options,
# - "~/.my.cnf" to set user-specific options.
#
# One can use all long options that the program supports.
# Run program with --help to get a list of available options and with
# --print-defaults to see which it would actually understand and use.
#
# For explanations see
# http://dev.mysql.com/doc/mysql/en/server-system-variables.html

#
# * IMPORTANT: Additional settings that can override those from this file!
#   The files must end with '.cnf', otherwise they'll be ignored.
#

!includedir /etc/mysql/conf.d/
!includedir /etc/mysql/mysql.conf.d/

第一个是mysql客户端的配置文件目录
第二个是mysql服务端配置文件目录

# 实际位置
# vim /etc/mysql/mysql.conf.d/mysqld.cnf，报错
sudo vim /etc/mysql/mysql.conf.d/mysqld.cnf
# 选择了注释#bind-address = 127.0.0.1


# 找到“bind-address”行，并将其注释掉或设置为服务器的IP地址
# 这个配置会限制MySQL仅可以通过本地地址127.0.0.1（即localhost）进行访问，而不允许来自其他机器的请求
# 0.0.0.0接受来自任何IP地址的连接

# 下面是3个方法
#bind-address = 127.0.0.1
bind-address = your_server_ip_address
bind-address = 0.0.0.0

# 重新启动MySQL服务，未执行
sudo service mysql restart

# 实际执行了
sudo systemctl restart mysql
netstat -lntp

```



## 备注，ubuntu防火墙，配置

未执行，默认是关闭的

```bash

# 默认情况下防火墙未开启
# 不需要执行
sudo ufw status
# 需要打开MySQL服务端口3306，以允许外部访问
sudo ufw allow mysql
sudo ufw allow 3306/tcp

```




## 报错，mysql安全配置，mysql_secure_installation无法重设密码


```bash

# MySQL 安全配置
sudo mysql_secure_installation

# 报错
# ... Failed! Error: SET PASSWORD has no significance for user 'root'@'localhost' as the authentication method used doesn't store authentication data in the MySQL server. Please consider using ALTER USER instead if you want to change authentication parameters.

# 解决
# https://askubuntu.com/questions/1406395/mysql-root-password-setup-error/

# I killed the mysql_secure_installation process
sudo pkill -f mysql_secure_installation

# I logged into mysql using:
sudo mysql
# of course, sudo asks my system root password. Once I provided the right root password, I'm connected on mysql as root mysql user.

# I use my mysql session to run ALTER USER:
mysql> ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password by 'my-secret-password';

mysql> exit

#Run sudo mysql_secure_installation command, and complete steps of securing Mysql.

# 重新运行 MySQL 安全配置
sudo mysql_secure_installation


# --------------------完整配置过程--------------------

Securing the MySQL server deployment.

Enter password for user root:
The 'validate_password' component is installed on the server.
The subsequent steps will run with the existing configuration
of the component.
Using existing password for root.

Estimated strength of the password: 50
Change the password for root ? ((Press y|Y for Yes, any other key for No) : y

New password:

Re-enter new password:

Estimated strength of the password: 50
Do you wish to continue with the password provided?(Press y|Y for Yes, any other key for No) : y
By default, a MySQL installation has an anonymous user,
allowing anyone to log into MySQL without having to have
a user account created for them. This is intended only for
testing, and to make the installation go a bit smoother.
You should remove them before moving into a production
environment.

Remove anonymous users? (Press y|Y for Yes, any other key for No) : y
Success.


Normally, root should only be allowed to connect from
'localhost'. This ensures that someone cannot guess at
the root password from the network.

Disallow root login remotely? (Press y|Y for Yes, any other key for No) : y
Success.

By default, MySQL comes with a database named 'test' that
anyone can access. This is also intended only for testing,
and should be removed before moving into a production
environment.


Remove test database and access to it? (Press y|Y for Yes, any other key for No) : y
 - Dropping test database...
Success.

 - Removing privileges on test database...
Success.

Reloading the privilege tables will ensure that all changes
made so far will take effect immediately.

Reload privilege tables now? (Press y|Y for Yes, any other key for No) : y
Success.

All done!
# --------------------完整配置过程--------------------


MySQL 8.0或更高版本，"auth_socket"插件用于默认本地连接，必须更改为"mysql_native_password"才能使用密码进行身份验证

ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password by 'my-secret-password';

将MySQL中的root用户在本地主机localhost的身份验证插件从"auth_socket"更改为"mysql_native_password"，并设置密码为"my-secret-password"，执行此命令后，能够使用新密码访问root帐户

这意味着可以使用密码进行身份验证，而不是通过套接字文件，连接密码可能暴露在 SQL 历史记录中


```





# IDEA部分

这部分记录在IDEA里的全部操作

从装完软件开始


## 配置idea内部代理

此处需要代理正常运行并且代理软件提供了接口，这里提供另两个方案
- 代理已经接管全部流量，就不用设置了
- 在项目初始化时，配置server url，换成国内源

这里以clash为例

- 启动idea
- 右侧Customize-All settings，或者其他位置打开设置
- 右侧appearance & behavior-system settings-http proxy
- 单选选择manual proxy configuration

```bash

# 我自己这边http和socks都支持

# host name
127.0.0.1

# port number
7890

# no proxy for
# 添加到排除，来源为，win中clash自动配置
localhost,127.*,10.*,172.16.*,172.17.*,172.18.*,172.19.*,172.20.*,172.21.*,172.22.*,172.23.*,172.24.*,172.25.*,172.26.*,172.27.*,172.28.*,172.29.*,172.30.*,172.31.*,192.168.*

# proxy authentication
登陆，不需要设置

# check connection
https://www.google.com

```


## 初创项目


选择new project，弹窗

左侧generators中选择，spring initializr


```bash

# server url
默认不需要改，没配置代理的话可以考虑换成国内源

# name
默认demo，改成了demo_spring

# location
自选，会根据name自动建立文件夹
下面git不需要选中

# language
默认 java

# type
默认gradle-groovy，改成maven

# group
默认com.example

# artifact
与name相同

# package name
com.example.{与name相同}
不需要改

# jdk
默认19，19.0.1

# java
默认17，改成8

# packaging
默认jar

```

next，开始选择dependecies

```bash

# 这里选择3个

# developer tools
lombok
# web
spting web
# sql
mysql driver

# 其他都默认

```

选完后create，等待下载组件，国内没配置代理会很慢


## 连接数据库，新建数据表


理论上可以不用Navicat，自带idea数据库功能够用

右侧，数据库，添加，mysql，输入之前设置的账密，登陆

首次添加需要下载，missing file

新建scheam，test_a

在test_a，新建table，表名为people
- id，varchar(20)，非空，主键
- name，varchar(50)，非空
- phone，varchar(20)，非空

这个新建的数据表仅供初次功能实现测试使用，一开始还没引入liquibase，后续引入时这张people表可以删除




## crud功能实现


### pom.xml引入MyBatisPlus

pom.xml在项目根目录

参考

https://www.baomidou.com/pages/226c21/

https://github.com/baomidou/mybatis-plus/blob/3.0/CHANGELOG.md


```xml

<!-- 在</dependencies>上面加 -->

    <dependency>
        <groupId>com.baomidou</groupId>
        <artifactId>mybatis-plus-boot-starter</artifactId>
        <version>最新版本</version>
    </dependency>

```


最新版本号在github可见

添加后，右侧maven刷新


### application.yml 改名，添加配置


位置在

`demo_spring\src\main\resources\application.properties`

把 application.properties 改成 application.yml

然后添加配置


```yaml

# 完整内容

server:
  port: 8088
  address: 0.0.0.0
spring:
  application:
    name: demo
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url : jdbc:mysql://localhost:3306/test_a?serverTimezone=UTC
    username : 'springboot'
    password : '00000000'

```


### 新建 class，entity.people，添加内容


在`demo_spring\src\main\java\com\example\demo_spring`，即project内com.example.demo_spring文件夹内

新建java class，class，名称为entity.people

完成效果是

`demo_spring\src\main\java\com\example\demo_spring\entity\people.java`

添加内容

```java

//完整内容

package com.example.demo_spring.entity;


import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@TableName("people")
//mybatis plus插件，本来类名需要与数据库表名一致，否则需要加上@TableName("表名")
@Data
//lombok插件，自动生成get、set、toString方法
@AllArgsConstructor
//lombok插件，自动生成全参构造方法
@NoArgsConstructor
//lombok插件，自动生成无参构造方法
public class people {
    //对应表中的id、name、phone字段，有几个字段就需要几个属性
    private String id;
    private String name;
    private String phone;
}


```


### 新建 interface，mapper.PeopleMapper，添加内容


在`demo_spring\src\main\java\com\example\demo_spring`，即project内com.example.demo_spring文件夹内

新建java class，interface，名称为mapper.PeopleMapper（新建接口）

完成效果是

`demo_spring\src\main\java\com\example\demo_spring\mapper\PeopleMapper.java`

添加内容


```java

//完整内容

package com.example.demo_spring.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo_spring.entity.people;

//通过mybatis plus插件，继承BaseMapper接口，即可使用mybatis plus的增删改查方法
//需要到启动类中添加@MapperScan("com.example.demo_spring")
//到DemoSpringApplication.java中添加@MapperScan("com.example.demo_spring.mapper")
public interface PeopleMapper extends BaseMapper<people> {

    //这里不需要写方法，mybatis plus会自动实现
    //如果需要自定义方法，可以在这里写

}

```



### 在 DemoSpringApplication.java 中添加 MapperScan

DemoSpringApplication.java为初建项目自动生成

位置在`demo_spring\src\main\java\com\example\demo_spring`，即project内com.example.demo_spring文件夹内


```java

//完整内容

package com.example.demo_spring;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.demo_spring.mapper")
//这里的MapperScan注解，是为了让mybatis plus能够扫描到mapper接口
public class DemoSpringApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoSpringApplication.class, args);
    }

}

```



### 新建 class，controller.PeopleController



在`demo_spring\src\main\java\com\example\demo_spring`，即project内com.example.demo_spring文件夹内

新建java class，class，名称为controller.PeopleController

完成效果是

`demo_spring\src\main\java\com\example\demo_spring\controller\PeopleController.java`

添加内容

```java

//完整内容

package com.example.demo_spring.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.example.demo_spring.entity.people;
import com.example.demo_spring.mapper.PeopleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
//@RestController负责把后端的数据以json的形式返回给前端
@CrossOrigin(methods = {RequestMethod.GET,RequestMethod.POST,RequestMethod.PUT,RequestMethod.DELETE})
//解决跨域问题
public class PeopleController {

    //自动装配
    //自动装配的意思是，当你需要用到一个类的时候，Spring会自动帮你把这个类创建出来，然后注入到你需要用到的地方。
    //这里peopleMapper报红不影响运行，因为是自动装配
    @Autowired
    PeopleMapper peopleMapper;

    //插入数据，id留空时，会自动生成
    @RequestMapping("/insert")
    public int insert(String id, String name, String phone){
        return peopleMapper.insert(new people(id,name,phone));
        //return peopleMapper.insert(new people(id,name,phone))>0?"success":"fail";
    }

    //查询全部数据
    @RequestMapping("/selectAll")
    public List<people> selectList(){
        return peopleMapper.selectList(null);
    }

    //查询包含关键字的数据
    @RequestMapping("/select")
    public List<people> selectList(String id, String name, String phone){
        QueryWrapper<people> wrapper = new QueryWrapper<>();
        //为空时，不加入查询条件
        if(id!=null && !id.equals("")){
            wrapper.like("id",id);
        }
        if(name!=null && !name.equals("")){
            wrapper.like("name",name);
        }
        if(phone!=null && !phone.equals("")){
            wrapper.like("phone",phone);
        }
        return peopleMapper.selectList(wrapper);
    }

    //根据确定的id，更新其他对应信息
    @RequestMapping("/updateById")
    public int updateById(String id, String name, String phone){
        UpdateWrapper<people> wrapper = new UpdateWrapper<>();
        wrapper.eq("id",id);
        return peopleMapper.update(new people(id,name,phone),wrapper);
    }

    //根据确定的phone，更新其他对应信息
    @RequestMapping("/updateByPhone")
    public int updateByPhone(String id, String name, String phone){
        UpdateWrapper<people> wrapper = new UpdateWrapper<>();
        wrapper.eq("phone",phone);
        return peopleMapper.update(new people(id,name,phone),wrapper);
    }

    //删除包含关键字的数据，关键字判断为like，留空时，删除全部数据
    @RequestMapping("/delete")
    public int delete(String id, String name, String phone){
        QueryWrapper<people> wrapper = new QueryWrapper<>();
        wrapper.like("id",id);
        wrapper.like("name",name);
        wrapper.like("phone",phone);
        return peopleMapper.delete(wrapper);
    }

    //删除包含关键字的数据，只有全部字段内内容与关键字完全匹配才删除
    @RequestMapping("/deleteMatchAll")
    public int deleteMatchAll(String id, String name, String phone){
        QueryWrapper<people> wrapper = new QueryWrapper<>();
        wrapper.eq("id",id);
        wrapper.eq("name",name);
        wrapper.eq("phone",phone);
        return peopleMapper.delete(wrapper);
    }

}

```





### 功能测试


至此，代码部分告一段落，可以运行并测试功能

crud功能均实现，通过地址栏增删改查


插入，全留空会生成唯一id

http://localhost:8088/insert?id=&name=&phone=

查询，通过like匹配

http://localhost:8088/select?id=&name=&phone=

更新，通过eq匹配id更新

http://localhost:8088/updateById?id=&name=&phone=

更新，通过eq匹配phone更新

http://localhost:8088/updateByPhone?id=&name=&phone=

删除，通过like匹配

http://localhost:8088/delete?id=&name=&phone=

删除，通过eq匹配

http://localhost:8088/deleteMatchAll?id=&name=&phone=


同时也编写了一个html文件用于测试，详见项目文件test_crud.html

用浏览器打开test_crud.html，可以更方便的测试




## 引入liquibase


一些参考

https://www.pdai.tech/md/spring/springboot/springboot-x-mysql-liquibase.html

https://zhuanlan.zhihu.com/p/552175791


官方文档

https://docs.liquibase.com/tools-integrations/springboot/springboot.html

https://docs.liquibase.com/tools-integrations/springboot/using-springboot-with-maven.html

https://docs.liquibase.com/tools-integrations/maven/workflows/using-liquibase-maven-plugin-and-springboot.html



### 新建文件夹


在`demo_spring\src\main\resources`文件夹里面新建`db\changelog`

用于后续存放

`demo_spring\src\main\resources\db\changelog\db.changelog-master.xml`

当时好像是在哪看到说，不会自动生成这个文件夹，一定要新建，没测试



### pom.xml引入liquibase与插件


pom.xml在项目根目录

然后在pom.xml添加内容


```xml

<!-- 在</dependencies>上面加 -->

<dependency>  
<groupId>org.liquibase</groupId>  
<artifactId>liquibase-core</artifactId>  
</dependency>

<!-- 在</plugins>上面加 -->

<plugin>  
<groupId>org.liquibase</groupId>  
<artifactId>liquibase-maven-plugin</artifactId>  
<configuration>
<driver>com.mysql.cj.jdbc.Driver</driver>  
<url>jdbc:mysql://localhost:3306/test_a?serverTimezone=UTC</url>  
<username>springboot</username>  
<password>00000000</password>
<outputChangeLogFile>src/main/resources/db/changelog/db.changelog-master.xml</outputChangeLogFile>
<changeLogFile>src/main/resources/db/changelog/db.changelog-master.xml</changeLogFile>  
</configuration>  
</plugin>

```

完成后右侧maven刷新



### application.yml，添加配置


位置在

`demo_spring\src\main\resources\application.yml`

添加liquibase配置


```yaml

# 完整内容

server:
  port: 8088
  address: 0.0.0.0
spring:
  application:
    name: demo
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url : jdbc:mysql://localhost:3306/test_a?serverTimezone=UTC
    username : 'springboot'
    password : '00000000'
  liquibase:
    enabled: true
    change-log: classpath:/db/changelog/db.changelog-master.xml


```



### 使用 generateChangeLog 生成 db.changelog-master.yaml 并增设判断


保证目前数据库是正常的，就是经过之前crud测试的那个数据库，要可以连接，并且内容正常


打开在右侧maven菜单栏内的demo_spring-plugins-liquibase

运行`liquibase:generateChangeLog` 功能

这时如果配置都正确，将会生成`demo_spring\src\main\resources\db\changelog\db.changelog-master.xml`

打开db.changelog-master.xml，增设判断

```xml

# 完整内容

<?xml version="1.1" encoding="UTF-8" standalone="no"?>
<databaseChangeLog xmlns="http://www.liquibase.org/xml/ns/dbchangelog" xmlns:ext="http://www.liquibase.org/xml/ns/dbchangelog-ext" xmlns:pro="http://www.liquibase.org/xml/ns/pro" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog-ext http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-ext.xsd http://www.liquibase.org/xml/ns/pro http://www.liquibase.org/xml/ns/pro/liquibase-pro-latest.xsd http://www.liquibase.org/xml/ns/dbchangelog http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-latest.xsd">
    <changeSet author="auto_generated" id="1684124327266-1">

        <preConditions onFail="MARK_RAN">
            <not>
                <tableExists tableName="people"/>
            </not>
        </preConditions>

        <createTable tableName="people">
            <column name="id" type="VARCHAR(20)">
                <constraints nullable="false" primaryKey="true"/>
            </column>
            <column name="name" type="VARCHAR(50)">
                <constraints nullable="false"/>
            </column>
            <column name="phone" type="VARCHAR(20)">
                <constraints nullable="false"/>
            </column>
        </createTable>

    </changeSet>
</databaseChangeLog>

```



### 功能测试

到这里关于引入liquibase所需全部配置都完成了

正常情况也就是初次部署项目的时候，people是不存在的，在这时候运行项目，会在数据库中生成“DATABASECHANGELOG”，“DATABASECHANGELOGLOCK”以及“people”共3张表，后续测试crud都正常


这时候把test_a数据库内全部内容3张表删除，再次运行

可见成功生成3张表，其中“DATABASECHANGELOG”内记录下的“EXECTYPE”字段为“EXECUTED”，可见正常执行

然后把test_a数据库内“DATABASECHANGELOG”，“DATABASECHANGELOGLOCK”两张表删除，即删除数据库test_a内全部表

这时候再次运行项目，会发现项目也可以正常运行，且“DATABASECHANGELOG”内记录下的“EXECTYPE”字段为“MARK_RAN”，表示在创建时已经跳过并记录了这个表


至此，全部功能都实现了，接着尝试部署到服务器


## 备注，MyBatis-Plus使用参考


```java

//MyBatis-Plus参考
//https://www.baomidou.com/pages/49cc81/
//https://www.baomidou.com/pages/10c804/
//Mapper CRUD 接口 和 条件构造器

// 插入一条记录
int insert(T entity);


// 根据 entity 条件，删除记录
int delete(@Param(Constants.WRAPPER) Wrapper<T> wrapper);
// 删除（根据ID 批量删除）
int deleteBatchIds(@Param(Constants.COLLECTION) Collection<? extends Serializable> idList);
// 根据 ID 删除
int deleteById(Serializable id);


// 根据 whereWrapper 条件，更新记录
int update(@Param(Constants.ENTITY) T updateEntity, @Param(Constants.WRAPPER) Wrapper<T> whereWrapper);
// 根据 ID 修改
int updateById(@Param(Constants.ENTITY) T entity);


// 根据 ID 查询
T selectById(Serializable id);
// 根据 entity 条件，查询一条记录
T selectOne(@Param(Constants.WRAPPER) Wrapper<T> queryWrapper);
// 查询（根据ID 批量查询）
List<T> selectBatchIds(@Param(Constants.COLLECTION) Collection<? extends Serializable> idList);
// 根据 entity 条件，查询全部记录
List<T> selectList(@Param(Constants.WRAPPER) Wrapper<T> queryWrapper);


```






## 报错，跨域问题


```
报错

Access to XMLHttpRequest at 'http://localhost:8088/updateById?id=NaN&name=aaa&phone=aaa'  
from origin 'null' has been blocked by CORS policy:  
Response to preflight request doesn't pass access control check:  
No 'Access-Control-Allow-Origin' header is present on the requested resource.

```

需要在public class PeopleController上面增加@CrossOrigin

```java

//@CrossOrigin(methods = {org.springframework.web.bind.annotation.RequestMethod.GET,
//        org.springframework.web.bind.annotation.RequestMethod.POST,
//        org.springframework.web.bind.annotation.RequestMethod.PUT,
//        org.springframework.web.bind.annotation.RequestMethod.DELETE},
//        origins = "*",
//        allowCredentials = "true",
//        allowedHeaders = "*",
//        maxAge = 3600)

@CrossOrigin(methods = {RequestMethod.GET,RequestMethod.POST,RequestMethod.PUT,RequestMethod.DELETE})

public class PeopleController {

}

```





## 报错，数据库相关零碎问题


```bash

# 一开始设置了id为主键，自增，但遇到了问题，取消了
# 是少一个插入值的问题，解决了，但为了可以重复，没改回来

# 修改了id为varchar
# 同时修改了代码中int到string
# 原因是控制台报了错，提示输入的数据经过了转换，int不能为空
# Resolved [org.springframework.web.method.annotation.MethodArgumentTypeMismatchException: Failed to convert value of type 'java.lang.String' to required type 'int'; For input string: ""]

# 因为要updata，改回id为主键，但不能自增，因为是string

# 插入时主键为""，会有随机数

```






## 报错，仅导入liquibase后，不配置直接运行


会提示找不到db.changelog-master.yaml

```

2023-05-15T01:23:01.603+08:00 ERROR 16412 --- [           main] o.s.b.d.LoggingFailureAnalysisReporter   : 

***************************
APPLICATION FAILED TO START
***************************

Description:

Liquibase failed to start because no changelog could be found at 'classpath:/db/changelog/db.changelog-master.yaml'.

Action:

Make sure a Liquibase changelog is present at the configured path.


Process finished with exit code 1

```

需要改配置文件，在application.yml添加liquibase配置

```yaml

  liquibase:
    enabled: true
    change-log: classpath:/db/changelog/db.changelog-master.xml

```






## 报错，使用 liquibase-maven-plugin（generateChangeLog）需要的额外配置


在使用generateChangeLog功能时，需要额外配置插件的configuration，否则会报错

```xml

<plugin>
<groupId>org.liquibase</groupId>
<artifactId>liquibase-maven-plugin</artifactId>
<configuration>
{在这新增配置}
</configuration>
</plugin>


数据库配置与application.yml内的相同


未配置
<driver>com.mysql.cj.jdbc.Driver</driver>  
<url>jdbc:mysql://localhost:3306/test_a?serverTimezone=UTC</url>  
<username>springboot</username>  
<password>00000000</password>
的时候，会报错
The database URL has not been specified either as a parameter or in a properties file.


未配置
<outputChangeLogFile>src/main/resources/db/changelog/db.changelog-master.xml</outputChangeLogFile>
的时候，会报错
The outputChangeLogFile property must be specified.


未配置
<changeLogFile>src/main/resources/db/changelog/db.changelog-master.xml</changeLogFile>
的时候，会报错
The changeLogFile must be specified.


```




## 报错，旧项目引入liquibase，配置db.changelog-master.xml，因为已存在表，导致项目无法运行


启动报错，仅截取部分代码


```java

 _ _   |_  _ _|_. ___ _ |    _ 
| | |\/|_)(_| | |_\  |_)||_|_\ 
     /               |         
                        3.5.3 
2023-05-15T12:07:24.699+08:00  INFO 1672 --- [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Starting...
2023-05-15T12:07:24.838+08:00  INFO 1672 --- [           main] com.zaxxer.hikari.pool.HikariPool        : HikariPool-1 - Added connection com.mysql.cj.jdbc.ConnectionImpl@2674d4f6
2023-05-15T12:07:24.839+08:00  INFO 1672 --- [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Start completed.
2023-05-15T12:07:24.968+08:00  INFO 1672 --- [           main] liquibase.lockservice                    : Successfully acquired change log lock
2023-05-15T12:07:25.087+08:00  INFO 1672 --- [           main] liquibase.changelog                      : Creating database history table with name: test_a.DATABASECHANGELOG
2023-05-15T12:07:25.096+08:00  INFO 1672 --- [           main] liquibase.changelog                      : Reading from test_a.DATABASECHANGELOG
Running Changeset: db/changelog/db.changelog-master.xml::1684083205604-1::hjkkk (generated)
2023-05-15T12:07:25.139+08:00  INFO 1672 --- [           main] liquibase.lockservice                    : Successfully released change log lock
2023-05-15T12:07:25.140+08:00  WARN 1672 --- [           main] ConfigServletWebServerApplicationContext : Exception encountered during context initialization - cancelling refresh attempt: org.springframework.beans.factory.BeanCreationException: Error creating bean with name 'liquibase' defined in class path resource [org/springframework/boot/autoconfigure/liquibase/LiquibaseAutoConfiguration$LiquibaseConfiguration.class]: liquibase.exception.MigrationFailedException: Migration failed for changeset db/changelog/db.changelog-master.xml::1684083205604-1::hjkkk (generated):
     Reason: liquibase.exception.DatabaseException: Table 'people' already exists [Failed SQL: (1050) CREATE TABLE test_a.people (id VARCHAR(20) NOT NULL, name VARCHAR(50) NOT NULL, phone VARCHAR(20) NOT NULL, CONSTRAINT PK_PEOPLE PRIMARY KEY (id))]
2023-05-15T12:07:25.141+08:00  INFO 1672 --- [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Shutdown initiated...
2023-05-15T12:07:25.165+08:00  INFO 1672 --- [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Shutdown completed.
2023-05-15T12:07:25.166+08:00  INFO 1672 --- [           main] o.apache.catalina.core.StandardService   : Stopping service [Tomcat]
2023-05-15T12:07:25.175+08:00  INFO 1672 --- [           main] .s.b.a.l.ConditionEvaluationReportLogger : 

Error starting ApplicationContext. To display the condition evaluation report re-run your application with 'debug' enabled.
2023-05-15T12:07:25.183+08:00 ERROR 1672 --- [           main] o.s.boot.SpringApplication               : Application run failed


```


```java

org.springframework.beans.factory.BeanCreationException: Error creating bean with name 'liquibase' defined in class path resource [org/springframework/boot/autoconfigure/liquibase/LiquibaseAutoConfiguration$LiquibaseConfiguration.class]: liquibase.exception.MigrationFailedException: Migration failed for changeset db/changelog/db.changelog-master.xml::1684083205604-1::hjkkk (generated):
     Reason: liquibase.exception.DatabaseException: Table 'people' already exists [Failed SQL: (1050) CREATE TABLE test_a.people (id VARCHAR(20) NOT NULL, name VARCHAR(50) NOT NULL, phone VARCHAR(20) NOT NULL, CONSTRAINT PK_PEOPLE PRIMARY KEY (id))]
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.initializeBean(AbstractAutowireCapableBeanFactory.java:1770) ~[spring-beans-6.0.8.jar:6.0.8]
	at


Caused by: liquibase.exception.LiquibaseException: liquibase.exception.MigrationFailedException: Migration failed for changeset db/changelog/db.changelog-master.xml::1684083205604-1::hjkkk (generated):
     Reason: liquibase.exception.DatabaseException: Table 'people' already exists [Failed SQL: (1050) CREATE TABLE test_a.people (id VARCHAR(20) NOT NULL, name VARCHAR(50) NOT NULL, phone VARCHAR(20) NOT NULL, CONSTRAINT PK_PEOPLE PRIMARY KEY (id))]
	at 


Caused by: liquibase.exception.MigrationFailedException: Migration failed for changeset db/changelog/db.changelog-master.xml::1684083205604-1::hjkkk (generated):
     Reason: liquibase.exception.DatabaseException: Table 'people' already exists [Failed SQL: (1050) CREATE TABLE test_a.people (id VARCHAR(20) NOT NULL, name VARCHAR(50) NOT NULL, phone VARCHAR(20) NOT NULL, CONSTRAINT PK_PEOPLE PRIMARY KEY (id))]
	at

```



尝试禁用liquibase，enabled赋值false，可以运行，改回后不行

```yaml

  liquibase:
    enabled: false
    change-log: classpath:/db/changelog/db.changelog-master.xml

```



初次解决

因为日志提示表存在，然后才报的错，于是把表删了

可以成功运行了，会自动创建表

但是再次把表删除的时候，就不会自动创建了

只有把数据库内liquibase的记录表一起删除的时候，才会全部重新自动创建



最终解决

stackoverflow答案收录，类似问题

https://stackoverflow.com/questions/29667760/liquibase-mysqlsyntaxerrorexception-table-already-exists

https://stackoverflow.com/questions/66377049/table-already-exists-error-when-replacing-yaml-by-xml

官方文档，tableExists

https://docs.liquibase.com/concepts/changelogs/home.html

https://docs.liquibase.com/concepts/changelogs/preconditions.html



总结下来是3个方法

第一，用过，临时解决

直接删除已添加的表，可以和记录变更的表一起删



第二，目前使用

可以使用 preConditions 元素配置数据库中表格的存在条件，就是加判断

修改/db/changelog/db.changelog-master.xml

```xml

preConditions，先决条件是您添加到更改日志或单个更改集的标签，用于根据数据库状态控制更新的执行。

CONTINUE，跳过变更集。将在下一次更新时再次尝试执行变更集。
HALT，停止执行整个变更日志（默认）。
MARK_RAN，跳过变更集但将其标记为已执行。
WARN，发送警告并继续正常执行变更集/变更日志。

tableExists，定义指定的表是否存在于数据库中

```

最后改完实际效果，db.changelog-master.xml 文件内容为

```xml

部分省略

<?xml version="1.1" encoding="UTF-8" standalone="no"?>
<databaseChangeLog>
    <changeSet author="hjkkk (generated)" id="1684124327266-1">
                
        <preConditions onFail="MARK_RAN">
			<not>
				<tableExists tableName="people"/>
			</not>
		</preConditions>
        
        <createTable tableName="people">
        </createTable>
    </changeSet>
</databaseChangeLog>

```


第三，未测试，没用这个方法

修改
demo_spring/pom.xml
尝试加配置

说是`drop-first`配置默认为false，表示Liquibase执行时不会删除现有表格。

想强制覆盖现有表格，可以将`drop-first`设置为true。




## 尝试，把pom.xml-plugin-configuration移动到application.yml，不行


尝试把pom.xml中的plugins的配置去掉，想着只在application.yml内的liquibase部分配置数据库信息，不行


估计是liquibase-maven-plugin内的配置只是在plugin插件内，或者说在测试过程中专用的配置，也可能是plugin内的配置是用于从指定数据库提取信息的专用测试配置，猜的，总之不行




## 尝试，把pom.xml-plugin移动到dependency，不行

突发奇想，实际不行，会提示

```xml

Could not find artifact org.liquibase:liquibase-maven-plugin:pom:unknown in central (https://repo.maven.apache.org/maven2)

```



## 备注，通过updateSQL生成sql脚本


运行 `liquibase:updateSQL` 命令 ，以查看将要应用的变更集。此命令将生成一个 SQL 脚本，显示在应用变更集时执行的所有操作

`liquibase:updateSQL` 不会修改数据库




## 报错，是 generateChangeLog 不是 deactivateChangeLog


```

排错的时候，尝试删除了
src/main/resources/db/changelog/db.changelog-master.xml
但再次生成时，因为点错了plugin，导致一直报错
是generateChangeLog不是deactivateChangeLog
用错命令了，要用的是generateChangeLog
deactivateChangeLog会报错
报错内容为

The file src/main/resources/db/changelog/db.changelog-master.xml  
was not found in the configured search path:  
More locations can be added with the 'searchPath' parameter.

```

无需解决，无影响





# 部署到远程linux


## idea内文件更改


```bash

# pom.xml
mysql地址
账号
密码

# application.yml
mysql地址
账号
密码
服务器ip地址

```


## 打包项目


打开右侧maven-demo_spring-lifecycle

运行package功能


```xml

报错

There are test failures.

Please refer to D:\output_idea\demo_spring\target\surefire-reports for the individual test results.
Please refer to dump files (if any exist) [date].dump, [date]-jvmRun[N].dump and [date].dumpstream.

需要在，pom.xml，引入插件

<plugin>
<groupId>org.apache.maven.plugins</groupId>
<artifactId>maven-surefire-plugin</artifactId>
<configuration>
<testFailureIgnore>true</testFailureIgnore>
</configuration>
</plugin>

```


## 服务器配置


```bash


# 安装mysql，启动

sudo apt update

sudo apt upgrade

sudo apt install mysql-server

sudo systemctl start mysql



# mysql安全配置

sudo mysql

# ----------sql----------
show databases;

SELECT user, host FROM mysql.user;

ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password by 'my-secret-password';

SELECT user, host FROM mysql.user;

quit
# ----------sql----------


sudo mysql_secure_installation
root输密码
yyyy


# mysql新建账号

sudo mysql -u root -p
root输密码

# ----------sql----------
SELECT user, host FROM mysql.user;

CREATE USER 'admin'@'%' IDENTIFIED BY '因为在服务器，所以这里要设置强密码';

GRANT ALL PRIVILEGES ON *.* TO 'admin'@'%';

CREATE USER 'springboot'@'localhost' IDENTIFIED BY '00000000';

GRANT ALL PRIVILEGES ON test_a.* TO 'springboot'@'localhost';

SELECT user, host FROM mysql.user;

FLUSH PRIVILEGES;
# ----------sql----------



# 开放远程连接

netstat -lntp

cat /etc/mysql/my.cnf

sudo vim /etc/mysql/mysql.conf.d/mysqld.cnf
注释#bind-address = 127.0.0.1

sudo systemctl restart mysql

netstat -lntp



# 安装java

java -version

sudo apt install openjdk-19-jdk

java -version

```


## 云服务商管理面板开放端口


项目端口，8088

mysql端口，3306




## 上传，运行，测试


```bash

chmod +x demo_spring-0.0.1-SNAPSHOT.jar

java -jar demo_spring-0.0.1-SNAPSHOT.jar

```


