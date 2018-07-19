package com.example.auth.service;

import com.example.auth.entity.OauthClient;
import com.example.auth.entity.OauthInfo;
import com.example.auth.entity.Token;
import com.example.auth.entity.User;
import com.example.auth.mapper.OAuthMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ServerService {

    @Autowired
    OAuthMapper mapper;

    // 验证 clientId
    public boolean verifyClientId (String clientId) {
        OauthClient client = mapper.getClientById(clientId);
        return client != null;
    }

    // 验证 clientSecret
    public boolean verifySecret(String clientId, String clientSecret) {
        OauthClient client = mapper.getClientById(clientId);
        return null != client && clientSecret.equals(client.getClientSecret());
    }

    // 验证用户账号密码是否匹配
    public boolean verifyAccount (String username, String password) {
        User user = mapper.getUserByName(username);
        return null != user && password.equals(user.getPassword());
    }

    // 根据clientId, redirectUrl, username获取 OauthInfo对象
    public OauthInfo getInfo(String clientId, String redirectUrl, String username) {
        return mapper.getInfo(clientId, redirectUrl, username);
    }

    public OauthInfo getInfoByCode(String code) {
        return mapper.getInfoByCode(code);
    }

    // 插入 OauthInfo 信息
    public void inserInfo(String clientId, String redirectUrl, String username, String code) {
        OauthInfo info = new OauthInfo();
        info.setClientId(clientId);
        info.setRedirectUrl(redirectUrl);
        info.setCode(code);
        info.setUsername(username);
        mapper.insertInfo(info);
    }

    public void saveToken(String username, String token) {
        Token t = new Token();
        t.setUsername(username);
        t.setToken(token);
        mapper.savaToken(t);
    }

    public Token getToken(String token) {
        return mapper.getToken(token);
    }

    public int insertUser(String username, String password, String message) {
        User user = mapper.getUser(username);
        if (user != null) {
            return 1; //账号存在
        }
        user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setMessage(message);
        mapper.insertUser(user);
        return 0;
    }

    public User getUser(String username) {
        return mapper.getUser(username);
    }

    // 验证 clientId, clientSecret, redirect_url是否与获取 code 时一致， code
//    public boolean verify (String clientId, String clientSecret, String code, String redirectUrl) {
//        OauthEntity oauthEntity = mapper.getByClientId(clientId);
//        if (null != oauthEntity &&
//                clientSecret.equals(oauthEntity.getClientSecret()) &&
//                code.equals(oauthEntity.getCode()) &&
//                redirectUrl.equals(oauthEntity.getRedirectUrl())) {
//            return true;
//        } else {
//            return false;
//        }
//    }

//    // 插入 clientId, clientSecret, redirect_url. code
//    public void insert (String clientId, String clientSecret, String code, String redirectUrl) {
//        OauthEntity entity = new OauthEntity();
//        entity.setClientId(clientId);
//        entity.setClientSecret(clientSecret);
//        entity.setCode(code);
//        entity.setRedirectUrl(redirectUrl);
//        mapper.insert(entity);
//    }
}
