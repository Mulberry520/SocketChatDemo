package com.mulberry.WebChat.mapper;

import com.mulberry.WebChat.dto.UserDetailDTO;
import org.apache.ibatis.annotations.*;

import java.time.LocalDate;

@Mapper
public interface ChatUserMapper {
    @Insert("insert into user (username, password, phone) values (#{username}, #{password}, #{phone})")
    int insertBasicInfo(
            @Param("username") String name,
            @Param("password") String passwd,
            @Param("phone") String phone
    );


    @Select("select * from user where username = #{username}")
    UserDetailDTO selectAllByName(@Param("username") String name);

    @Select("select id from user where username = #{username}")
    Long selectIdByName(@Param("username") String name);

    @Select("select id from user where phone = #{phone}")
    Long selectIdByPhone(@Param("phone") String phone);

    @Select("select password from user where id = #{id}")
    String selectPasswdById(@Param("id") Long userId);

    @Select("select status from user where username = #{username}")
    String selectStatusByName(@Param("username") String name);


    @Update("update user set status = #{status} where username = #{username}")
    int updateStatusByName(
            @Param("status") String status,
            @Param("username") String username
    );

    @Update("update user set nickname = #{nickname}, gender = #{gender}, birth = #{birth}, region = #{region}, biography = #{biography}, email = #{email} where id = #{id}")
    int updateDetailById(
            @Param("id") Long userId,
            @Param("nickname") String nickname,
            @Param("gender") String gender,
            @Param("birth") LocalDate birth,
            @Param("region") String region,
            @Param("biography") String bio,
            @Param("email") String email
    );

    @Update("update user set password = #{password} where id = #{id}")
    int updatePasswordById(
            @Param("id") Long userId,
            @Param("password") String newPassword
    );
}
