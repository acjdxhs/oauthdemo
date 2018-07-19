package com.example.infosec.controller;

import org.apache.oltu.oauth2.client.OAuthClient;
import org.apache.oltu.oauth2.client.URLConnectionClient;
import org.apache.oltu.oauth2.client.request.OAuthBearerClientRequest;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.client.response.OAuthAccessTokenResponse;
import org.apache.oltu.oauth2.client.response.OAuthResourceResponse;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.OAuthResponse;
import org.apache.oltu.oauth2.common.message.types.GrantType;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLConnection;

@Controller
@RequestMapping("/client")
public class clientController {
    String remote = "http://10.8.0.1:8082";
    String local = "http://10.8.0.6:8081";

    String clientId = "infosec client";
    String clientSecret = "infosec secret";
    String response_type = "code";
    String scope = "获取 message 信息";
    String accessTokenUrl = "";
    String redirectUrl =  local + "/client/requestAccessToken";
    String code = "";

    // 申请 code 的请求
    @RequestMapping("/requestCode")
    public String requestCode (HttpServletRequest request, HttpServletResponse response) {
        String requestUrl = null;
        try {
            accessTokenUrl = remote + "/oauth/login";
            System.out.println("申请 code 开始");
            OAuthClientRequest accessTokenRequest = OAuthClientRequest
                    .authorizationLocation(accessTokenUrl)
                    .setResponseType(response_type)
                    .setClientId(clientId)
                    .setRedirectURI(redirectUrl)
                    .setScope(scope)
                    .buildQueryMessage();
            requestUrl = accessTokenRequest.getLocationUri();

            System.out.println("申请code的认证服务器地址；" + requestUrl);
        } catch (OAuthSystemException e) {
            e.printStackTrace();
        }
        return "redirect:" + requestUrl;
    }

    // 接收服务器返回的 code，提交申请 access token 的请求
    @RequestMapping ("/requestAccessToken")
    public String requestAccessToken (HttpServletRequest request, HttpServletResponse response) {
        accessTokenUrl = remote + "/server/responseAccessToken";
        code = request.getParameter("code");
        System.out.println("code : " + code);
        System.out.println("申请 access token 开始");
        String accessToken = null;
        try {
            OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());
            OAuthClientRequest oAuthClientRequest = OAuthClientRequest
                    .tokenLocation(accessTokenUrl)
                    .setGrantType(GrantType.AUTHORIZATION_CODE)
                    .setClientId(clientId)
                    .setClientSecret(clientSecret)
                    .setCode(code)
                    .setRedirectURI(redirectUrl)
                    .buildQueryMessage();
            // 去服务器获取token，并返回响应
            OAuthAccessTokenResponse oAuthResponse = oAuthClient.accessToken(oAuthClientRequest, OAuth.HttpMethod.POST);
            accessToken = oAuthResponse.getAccessToken();
            System.out.println("获取的 token 是：" + accessToken);
        } catch (OAuthSystemException e) {
            e.printStackTrace();
        } catch (OAuthProblemException e) {
            e.printStackTrace();
        }
        return "redirect:" + local +"/client/requestRes?accessToken=" + accessToken;
        //return "forward:/server/requestRes?accessToken=" + accessToken;
    }

    // 使用 accessToken 请求资源
    @RequestMapping("/requestRes")
    public ModelAndView requestRes (HttpServletRequest request) {
        System.out.println("开始获取资源");
        String accessToken = request.getParameter("accessToken");
        String resUrl = remote + "/server/responseRes";
        OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());
        try {
            OAuthClientRequest resRequest = new OAuthBearerClientRequest(resUrl)
                    .setAccessToken(accessToken)
                    .buildQueryMessage();
            OAuthResourceResponse resourceResponse = oAuthClient.resource(resRequest, OAuth.HttpMethod.GET, OAuthResourceResponse.class);
            String message = resourceResponse.getBody();
            System.out.println("获取的资源是：" +  message);
            return new ModelAndView("home", "user", message);
        } catch (OAuthSystemException e) {
            e.printStackTrace();
        } catch (OAuthProblemException e) {
            e.printStackTrace();
        }
        return new ModelAndView("error", "errorMessage", "出错了");
    }

}
