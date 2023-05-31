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
