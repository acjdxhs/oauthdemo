package com.example.auth.controller;

import com.example.auth.entity.OauthInfo;
import com.example.auth.service.ServerService;
import com.example.auth.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

@Controller
@CrossOrigin
@RequestMapping("/oauth")
public class AuthController {

    @Autowired
    ServerService service;

    @RequestMapping("/login")
    public ModelAndView login(@RequestParam("client_id") String clientId, @RequestParam("redirect_uri") String redirectUrl,
                        @RequestParam("response_type")String responseType, @RequestParam("scope")String scope) {
        if(!responseType.equals("code")) {
            return new ModelAndView("error", "errorMessage", "不支持的response_type类型");
        }
        if(!service.verifyClientId(clientId)) {
                return new ModelAndView("error", "errorMessage", "clientId不存在");
        }
        ModelAndView view = new ModelAndView("login");
        view.addObject("redirectUrl", redirectUrl);
        view.addObject("scope", scope);
        view.addObject("clientId", clientId);
        return view;
    }

    @RequestMapping("/authorize")
    public Object authorize (String redirectUrl, String clientId, String username, String password) {
        // 验证账号密码
        if (!service.verifyAccount(username, password)) {
            return new ModelAndView("error", "errorMessage", "账号密码错误");
        }
        System.out.println("授权成功");
        String authorizationCode = "";
        //如果已经获取过授权码
        OauthInfo info = service.getInfo(clientId, redirectUrl, username);
        if (info != null && !"".equals(info.getCode())) {
            authorizationCode = info.getCode();
        } else {
            // 生成授权码, 生成16位的随机字符串
            authorizationCode = Utils.getRandomString(16);
            service.inserInfo(clientId,redirectUrl,username,authorizationCode);
        }
        String url = redirectUrl + "?code="  + authorizationCode;
        System.out.println("重定向客户端地址：" + url);
        return "redirect:" + url;
    }

}
