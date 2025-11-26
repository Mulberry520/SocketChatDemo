package com.mulberry.WebChat.mapper;

import com.mulberry.WebChat.dto.FriendRequestDTO;
import com.mulberry.WebChat.dto.FriendResponseResp;
import com.mulberry.WebChat.dto.FriendsResp;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface FriendshipMapper {
    @Select("select friend_id, alias, favor from friendship where user_id = #{user_id} and status = 'approved'")
    List<FriendsResp> selectFriendById(@Param("user_id") Long userId);

    @Select("select id from friendship where user_id = #{user_id} and friend_id = #{friend_id}")
    Long selectFriendshipIfExists(
            @Param("user_id") Long userId,
            @Param("friend_id") Long friendId
    );

    @Select("select friend_id from friendship where id = #{id} and user_id = #{user_id}")
    Long selectFriendIdByUser(
            @Param("id") Long friendshipId,
            @Param("user_id") Long UserId
    );

    @Select("select * from friendship where user_id = #{user_id} and status != 'blocked'")
    List<FriendRequestDTO> selectFriendRequestByUserId(@Param("user_id") Long userId);

    @Select("select * from friendship where id = #{id}")
    FriendRequestDTO selectFriendshipById(@Param("id") Long friendshipId);

    @Select("select * from friendship where friend_id = #{friend_id} and status = 'unverified'")
    List<FriendResponseResp> selectFriendshipAboutUser(@Param("friend_id") Long userId);

    @Insert("insert into friendship (user_id, friend_id, alias, status, information) value (#{user_id}, #{friend_id}, #{alias}, #{status}, #{information})")
    int insertFriendship(
            @Param("user_id") Long userId,
            @Param("friend_id") Long friendId,
            @Param("alias") String alias,
            @Param("status") String status,
            @Param("information") String information
    );

    @Update("update friendship set status = #{status} where user_id = #{user_id} and friend_id = #{friend_id}")
    int updateStatusById(
            @Param("user_id") Long userId,
            @Param("friend_id") Long friendId,
            @Param("status") String status
    );

    @Delete("delete from friendship where id = #{id}")
    int deleteFriendshipById(@Param("id") Long friendshipId);
}
