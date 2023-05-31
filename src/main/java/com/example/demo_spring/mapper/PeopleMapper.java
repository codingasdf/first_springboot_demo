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
