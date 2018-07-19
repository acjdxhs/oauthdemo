package com.example.auth.controller;

import com.example.auth.entity.OauthInfo;
import com.example.auth.entity.Token;
import com.example.auth.entity.User;
import com.example.auth.service.ServerService;
import com.example.auth.util.Utils;
import org.apache.oltu.oauth2.as.response.*;
import org.apache.oltu.oauth2.as.request.*;
import org.apache.oltu.oauth2.as.issuer.*;

import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.OAuthResponse;
import org.apache.oltu.oauth2.common.message.types.ParameterStyle;
import org.apache.oltu.oauth2.common.token.OAuthToken;
import org.apache.oltu.oauth2.rs.request.OAuthAccessResourceRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/server")
public class ServerController {

    @Autowired
    ServerService service;

    @RequestMapping ("/responseCode")
    public ResponseEntity responseCode(HttpServletRequest request) {
        System.out.println("返回 Code 开始");
        try {
            // 构建授权请求
            OAuthAuthzRequest oAuthAuthzRequest = new OAuthAuthzRequest(request);
            String clientId = oAuthAuthzRequest.getClientId();
            String redirectUrl = oAuthAuthzRequest.getRedirectURI();
            // 验证 clientId
            if (service.verifyClientId(clientId)) {
                // 生成授权码, 生成16位的随机字符串
                String authorizationCode = Utils.getRandomString(16);
                // 构建 OAuth 响应
                OAuthASResponse.OAuthAuthorizationResponseBuilder builder =
                        OAuthASResponse.authorizationResponse(request, HttpServletResponse.SC_FOUND);
                // 设置授权码
                builder.setCode(authorizationCode);
                final OAuthResponse response = builder.location(redirectUrl).buildQueryMessage();
                System.out.println("客户端重定向地址为：" + response.getLocationUri());
                //service.insert(clientId, clientSecret, authorizationCode, redirectUrl);
                System.out.println("返回 Code 结束");
                return new ResponseEntity(response.getBody(), HttpStatus.valueOf(response.getResponseStatus()));
            }
        } catch (OAuthSystemException e) {
            e.printStackTrace();
        } catch (OAuthProblemException e) {
            e.printStackTrace();
        }
        return null;
    }

    @RequestMapping (value = "/responseAccessToken", method = RequestMethod.POST)
    public HttpEntity responseAccessToken (HttpServletRequest request) {
        OAuthIssuer oAuthIssuer = null;
        OAuthResponse oAuthResponse = null;
        try {
            // 构建 OAuth 请求
            OAuthTokenRequest oAuthTokenRequest = new OAuthTokenRequest(request);
            String code = oAuthTokenRequest.getCode();
            String clientSecret = oAuthTokenRequest.getClientSecret();
            String redirectUrl = oAuthTokenRequest.getRedirectURI();
            String clientId = oAuthTokenRequest.getClientId();

            // 验证 clientSecret
            if (!service.verifySecret(clientId, clientSecret)) {
                oAuthResponse = OAuthASResponse.errorResponse(403)
                        .setError("应用密钥错误")
                        .buildQueryMessage();
                System.out.println("应用密钥错误");
                return new ResponseEntity(oAuthResponse.getBody(), HttpStatus.valueOf(oAuthResponse.getResponseStatus()));
            }

            // 验证 clientId, redirectUrl, code
            OauthInfo info = service.getInfoByCode(code);
            if (info == null || !redirectUrl.equals(info.getRedirectUrl()) ||
                    !clientId.equals(info.getClientId())){
                oAuthResponse = OAuthASResponse.errorResponse(403)
                        .setError("授权码错误或未知应用")
                        .buildQueryMessage();
                System.out.println("授权码错误或未知应用");
                return new ResponseEntity(oAuthResponse.getBody(), HttpStatus.valueOf(oAuthResponse.getResponseStatus()));
            }
            // 生成 AccessToken
            oAuthIssuer = new OAuthIssuerImpl(new MD5Generator());
            final String accessToken = oAuthIssuer.accessToken();
            System.out.println("服务器产生的 token 为：" + accessToken);
            // 将 token 跟能获取的资源联系起来
            service.saveToken(info.getUsername(), accessToken);
            // 生成 OAuth 响应
            oAuthResponse = OAuthASResponse
                    .tokenResponse(HttpServletResponse.SC_OK)
                    .setAccessToken(accessToken)
                    .buildJSONMessage();
            return new ResponseEntity(oAuthResponse.getBody(), HttpStatus.valueOf(oAuthResponse.getResponseStatus()));
        } catch (OAuthSystemException e) {
            e.printStackTrace();
        } catch (OAuthProblemException e) {
            e.printStackTrace();
        }
        return null;
    }

    @RequestMapping(value = "/responseRes", method = RequestMethod.GET)
    public HttpEntity responseRes(HttpServletRequest request) {
        try {
            // 获取资源请求
            OAuthAccessResourceRequest oauthRequest = new OAuthAccessResourceRequest(request, ParameterStyle.QUERY);
            // 获取 token
            String accessToken = oauthRequest.getAccessToken();
            //验证token
            Token token = service.getToken(accessToken);
            if (token == null) {
                return new ResponseEntity(HttpStatus.FORBIDDEN);
            }
            String username = token.getUsername();
            User user = service.getUser(username);
            return new ResponseEntity(user.getMessage(),HttpStatus.OK);
        } catch (OAuthSystemException e) {
            e.printStackTrace();
        } catch (OAuthProblemException e) {
            e.printStackTrace();
        }
        return null;
    }

}
