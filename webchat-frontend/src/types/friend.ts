export interface FriendListResponse {
  friendUsername: string,
  alias: string | null,
  avatar: string | null
}

export interface FriendDetailResponse {
  username: string,
  nickname: string,
  status: string,
  gender: string,
  region: string,
  biography: string,
  email: string,
  birth: string,
  avatar: string,
  alias: string,
  isFavor: boolean
}

export interface FriendUpdateRequest {
  friendUsername: string,
  alias: string | null,
  isFavor: boolean
}
