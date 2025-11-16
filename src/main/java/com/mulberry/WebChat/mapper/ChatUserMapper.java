package com.mulberry.WebChat.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ChatUserMapper {
    @Insert("insert into user (username, password, nickname) values (#{username}, #{password}, #{nickname})")
    int insertBasicInfo(
            @Param("username") String name,
            @Param("password") String passwd,
            @Param("nickname") String nickname
    );

    @Select("select id from user where username = #{username}")
    Long selectIdByName(@Param("username") String name);

    @Select("select password from user where id = #{id}")
    String selectPasswdById(@Param("id") Long userId);

}
