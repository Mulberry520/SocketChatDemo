import request from "@/utils/request.ts";
import type {ApiResponse} from "@/types/api.ts";
import type {FriendsResponse} from "@/types/friendship.ts";

export const getFriendList = () => {
  return request.get<ApiResponse<FriendsResponse[]>>('/friends')
}
