package com.mulberry.WebChat.mapper;

import com.mulberry.WebChat.dto.RoomRequestReceivedResp;
import com.mulberry.WebChat.dto.RoomRequestSentResp;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface RoomRequestMapper {
    @Select("select req.* from room r inner join room_request req on r.room_name = req.target_room where r.create_user = #{create_user} and req.status = 'unverified'")
    List<RoomRequestReceivedResp> selectReceivedRoomRequest(@Param("create_user") String username);

    @Select("select * from room_request where request_user = #{request_user}")
    List<RoomRequestSentResp> selectSentRoomRequest(@Param("request_user") String username);

    @Insert("insert into room_request (requesst_user, target_room, information, status) values (#{request_user}, #{target_room}, #{information}, #{status})")
    int insertRequest(
            @Param("request_user") String username,
            @Param("target_room") String roomName,
            @Param("information") String information,
            @Param("status") String status
    );

    @Update("update room_request set status = #{status} where request_user = #{request_user} and target_room = #{target_room}")
    int updateRequestStatus(
            @Param("request_user") String username,
            @Param("target_room") String roomName,
            @Param("status") String newStatus
    );

    @Delete("delete from room_request where id = #{id} and request_user = #{request_user}")
    int deleteRequest(
            @Param("request_user") String username,
            @Param("id") Long requestId
    );
}
