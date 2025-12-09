export interface RoomListResponse {
  roomName: string,
  roomAlias: string | null
  userAlias: string | null
  avatar: string | null
}

export interface RoomDetailResponse {
  roomName: string,
  roomAlias: string | null,
  userAlias: string | null,
  createUser: string,
  isPublic: boolean,
  information: string | null,
  avatar: string | null,
  members: string[],
  createTime: string
}

export interface ChatMessage {
  type: 'JOIN' | 'QUIT' | 'MESSAGE';
  content: string | null
  sender: string
  recipient: string
  time: string;
}
