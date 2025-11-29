import type {UserinfoResponse, UserUpdateRequest} from "@/types/Userinfo.ts";
import request from "@/utils/request.ts";
import type {ApiResponse} from "@/types/api.ts";

export const getUserinfo = () => {
  return request.get<ApiResponse<UserinfoResponse>>('/user/userinfo')
}

export const updateUserinfo = (data: UserUpdateRequest) => {
  return request.post('/user/userinfo', data)
}

export const getUserAvatar = () => {
  return request.get<ApiResponse<string | null>>('/user/avatar')
}

export const updateUserAvatar = (file: File) => {
  const formData = new FormData()
  formData.append('file', file)

  return request.post(
    '/user/avatar',
    formData,
    {
      headers: {'Content-Type': 'multipart/form-data'}
    }
  )
}
