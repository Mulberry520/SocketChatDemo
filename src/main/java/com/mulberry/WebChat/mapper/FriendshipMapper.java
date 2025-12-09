package com.mulberry.WebChat.mapper;

import com.mulberry.WebChat.dto.*;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface FriendshipMapper {
    @Select("select friend_username, alias from friendship where username = #{username} order by favor desc, alias")
    List<FriendListResp> selectFriendNames(@Param("username") String username);

    @Select("select id from friendship where username = #{username} and friend_username = #{friend_username}")
    Long selectIfFriendExists(
            @Param("username") String username,
            @Param("friend_username") String friendUsername
    );

    @Select("select alias from friendship where username = #{username} and friend_username = #{friend_username}")
    String selectFriendAlias(
            @Param("username") String username,
            @Param("friend_username") String friendUsername
    );

    @Select("select favor from friendship where username = #{username} and friend_username = #{friend_username}")
    Integer selectFriendIsFavor(
            @Param("username") String username,
            @Param("friend_username") String friendUsername
    );


    @Insert("insert into friendship (username, friend_username) values (#{username}, #{friend_username})")
    int insertFriendship(
            @Param("username") String username,
            @Param("friend_username") String friendUsername
    );

    @Update("update friendship set alias = #{alias} where username = #{username} and friend_username = #{friend_username}")
    int updateFriendAlias(
            @Param("username") String username,
            @Param("friend_username") String friendUsername,
            @Param("alias") String newAlias
    );

    @Update("update friendship set favor = #{favor} where username = #{username} and friend_username = #{friend_username}")
    int updateFriendFavor(
            @Param("username") String username,
            @Param("friend_username") String friendUsername,
            @Param("favor") Integer isFavor
    );

    @Delete("delete from friendship where username = #{username} and friend_username = #{friendUsername}")
    int deleteFriendshipByName(
            @Param("username") String username,
            @Param("friend_username") String friendUsername
    );
}
