package com.example.auth.mapper;

import com.example.auth.entity.OauthClient;
import com.example.auth.entity.OauthInfo;
import com.example.auth.entity.Token;
import com.example.auth.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Component;

@Mapper
@Component
public interface OAuthMapper {

    @Select("select * from oauthclient where clientId = #{clientId}")
    OauthClient getClientById(@Param("clientId") String clientId);

    @Select("select * from user where username=#{username}")
    User getUserByName(@Param("username") String username);

    @Select("select * from oauthinfo where clientId=#{clientId}" +
            "and redirectUrl=#{redirectUrl}" +
            "and username=#{username}")
    OauthInfo getInfo(@Param("clientId")String clientId, @Param("redirectUrl")String redirectUrl,
                      @Param("username")String username);

    @Select("select * from oauthinfo where code=#{code}")
    OauthInfo getInfoByCode(@Param("code")String code);

    @Insert("insert into oauthinfo(clientId, redirectUrl, username, code) " +
            "values(#{clientId}, #{redirectUrl}, #{username}, #{code})")
    void insertInfo(OauthInfo info);

    @Insert("insert into user(username, password, message) " +
            "values(#{user.username}, #{user.password}, #{user.message})")
    void insertUser(@Param("user")User user);

    @Select("select * from user where username=#{username}")
    User getUser(@Param("username")String username);

    @Insert("insert into token(username, token)" +
            "values(#{username}, #{token})")
    void savaToken(Token token);

    @Select("select * from token where token=#{token}")
    Token getToken(@Param("token")String token);


}
