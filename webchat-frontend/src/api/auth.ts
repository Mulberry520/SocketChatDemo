import type {LoginRequest, RegisterRequest} from "@/types/auth.ts"
import request from "@/utils/request.ts";
import type {ApiResponse} from "@/types/api.ts";

export const login = (data: LoginRequest) => {
  return request.post<ApiResponse<string>>('/auth/login', data)
}

export const register = (data: RegisterRequest) => {
  return request.post<ApiResponse<string>>('/auth/register', data)
}

export const logout = () => {
  return request.post<ApiResponse<string>>('/auth/logout')
}
