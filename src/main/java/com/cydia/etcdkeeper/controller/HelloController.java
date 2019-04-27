package com.cydia.etcdkeeper.controller;

import com.cydia.etcdkeeper.vo.StudentVo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@Controller
public class HelloController {

    @RequestMapping("/hello")
    public String hello(Model model) {

        List<StudentVo> list = new ArrayList<>();

        list.add(new StudentVo("张三", 20, "北京"));
        list.add(new StudentVo("李四", 30, "上海"));
        list.add(new StudentVo("王五", 40, "河北"));
        list.add(new StudentVo("赵六", 50, "山西"));
        model.addAttribute("list", list);

        return "index";
    }
}
