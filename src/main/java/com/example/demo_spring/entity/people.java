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
