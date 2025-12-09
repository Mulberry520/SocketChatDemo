package com.mulberry.WebChat.mapper;

import com.mulberry.WebChat.dto.RoomCreatedDTO;
import com.mulberry.WebChat.dto.RoomDetailResp;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ChatRoomMapper {
    @Insert("insert into room (room_name, create_uesr, is_public, information) values (#{room_name}, #{create_user}, #{is_public}, #{information})")
    int insertRoom(
            @Param("room_name") String roomName,
            @Param("create_user") String username,
            @Param("is_public") Integer isPublic,
            @Param("information") String information
    );

    @Select("select * from room where room_name = #{room_name}")
    RoomDetailResp selectRoomDetail(@Param("room_name") String roomName);

    @Select("select id from room where room_name = #{room_name} and create_user = #{create_user}")
    Long selectIsRoomOwner(
            @Param("room_name") String roomName,
            @Param("create_user") String username
    );

    @Select("select id from room where room_name = #{room_name}")
    Long selectIsRoomExists(@Param("room_name") String roomName);

    @Select("select is_public from room where room_name = #{room_name}")
    Boolean selectIsRoomPublic(@Param("room_name") String roomName);

    @Select("select room_name, is_public, information from room where create_user = #{create_user}")
    List<RoomCreatedDTO> selectRoomsByCreator(@Param("create_user") String username);

    @Select("select avatar from room where room_name = #{room_name}")
    String selectAvatar(@Param("room_name") String roomName);
}
