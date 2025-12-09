import type {RoomDetailResponse, RoomListResponse} from "@/types/room.ts";
import request from "@/utils/request.ts";
import type {ApiResponse} from "@/types/api.ts";

export const getJoinedRoomList = () => {
  return request.get<ApiResponse<RoomListResponse>>('/rooms/joined')
}

export const getRoomDetail = (roomName: string) => {
  return request.post<ApiResponse<RoomDetailResponse>>('/rooms', {
    param: { roomName }
  })
}
