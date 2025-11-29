export interface UserinfoResponse {
  username: string,
  nickname: string | null,
  gender: string | null,
  birth: string | null,
  region: string | null,
  biography: string | null,
  email: string | null,
  phone: string,
  createTime: string
}

export interface UserUpdateRequest {
  nickname: string | null,
  gender: string | null,    // 'male' or 'female'
  birth: string | null,     // yyyy-MM-dd
  region: string | null,
  biography: string | null,
  email: string | null
}
