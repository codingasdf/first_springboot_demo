
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


