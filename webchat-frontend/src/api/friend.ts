import request from "@/utils/request.ts";
import type {ApiResponse} from "@/types/api.ts";
import type {
  FriendDetailResponse,
  FriendListResponse,
  FriendUpdateRequest
} from "@/types/friend.ts";

export const getFriendList = () => {
  return request.get<ApiResponse<FriendListResponse>>('/friends/all')
}

export const getFriendDetail = (friend: string) => {
  return request.get<ApiResponse<FriendDetailResponse>>('/friends', {
    params: { friend }
  })
}

export const changeFriendInfo = (changes: FriendUpdateRequest) => {
  return request.post('/friends', changes)
}

export const deleteFriend = (friend: string) => {
  return request.delete('/friends', {
    params: { friend }
  })
}
