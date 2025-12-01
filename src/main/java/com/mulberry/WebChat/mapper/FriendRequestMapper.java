package com.mulberry.WebChat.mapper;

import com.mulberry.WebChat.dto.RequestReceivedResp;
import com.mulberry.WebChat.dto.RequestSentResp;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface FriendRequestMapper {
    @Select("select * from friend_request where target_user = #{target_user} and status = 'unverified'")
    List<RequestReceivedResp> selectRequestsReceived(@Param("target_user") String username);

    @Select("select * from friend_request where request_user = #{request_user}")
    List<RequestSentResp> selectRequestsSent(@Param("request_user") String username);


    @Delete("delete from friend_request where id = #{id} and request_user = #{request_user}")
    int deleteRequestSentById(
            @Param("id") Long requestId,
            @Param("request_user") String username
    );

    @Update("update friend_request set status = #{status} where request_user = #{request_user} and target_user = #{target_user}")
    int updateRequestStatus(
            @Param("target_user") String username,
            @Param("request_user") String friendUsername,
            @Param("status") String status
    );

    @Insert("insert into friend_request (request_user, target_user, information, status) values (#{request_user}, #{target_user}, #{information}, #{status})")
    int insertRequest(
            @Param("request_user") String username,
            @Param("target_user") String targetUsername,
            @Param("information") String information,
            @Param("status") String status
    );
}
