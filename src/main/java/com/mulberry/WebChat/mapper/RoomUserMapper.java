package com.mulberry.WebChat.mapper;

import com.mulberry.WebChat.dto.RoomListResp;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface RoomUserMapper {
    @Select("select room_name, room_alias, user_alias from room_users where username = #{username}")
    List<RoomListResp> selectJoinedRooms(@Param("username") String username);

    @Select("select room_name, room_alias, user_alias from room_users where room_name = #{room_name} and username = #{username}")
    RoomListResp selectAlias(
            @Param("username") String username,
            @Param("room_name") String roomName
    );

    @Select("select username from room_users where room_name = #{room_name}")
    List<String> selectUsersByRoom(@Param("room_name") String roomName);

    @Select("select id from room_users where room_name = #{room_name} and username = #{username}")
    Long selectIfUserInRoom(
            @Param("username") String username,
            @Param("room_name") String roomName
    );

    @Insert("insert into room_users (room_name, username) values (#{room_name}, #{username})")
    int insertRoomUser(
            @Param("username") String username,
            @Param("room_name") String roomName
    );

    @Update("update room_users set user_alias = #{user_alias}, room_alias = #{room_alias} where username = #{username} and room_name = #{room_name}")
    int updateAlias(
            @Param("username") String username,
            @Param("room_name") String roomName,
            @Param("user_alias") String userAlias,
            @Param("room_alias") String roomAlias
    );

    @Delete("delete from room_users where username = #{username} and room_name = #{room_name}")
    int deleteUserInRoom(
            @Param("username") String username,
            @Param("room_name") String roomName
    );
}
