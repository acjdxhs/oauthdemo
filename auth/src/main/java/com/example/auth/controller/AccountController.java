package com.example.auth.controller;

import com.example.auth.service.ServerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/account")
public class AccountController {

    @Autowired
    ServerService service;

    @RequestMapping("")
    public String register() {
        System.out.println("h");
        return "register";
    }

    @RequestMapping("/reg")
    public ModelAndView register(String username, String password, String message) {
        int status = service.insertUser(username, password, message);
        if (status == 1) {
            return new ModelAndView("error", "errorMessage", "账号已存在");
        }
        return new ModelAndView("error", "errorMessage", "注册成功");
    }
}
