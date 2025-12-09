import { defineStore } from 'pinia'
import type { FriendDetailResponse, FriendListResponse } from '@/types/friend'

export const useFriendStore = defineStore('friend', {
  state: () => ({
    currentFriend: null as FriendDetailResponse | null,
    friendList: [] as FriendListResponse[]
  }),
  actions: {
    setCurrentFriend(friend: FriendDetailResponse | null) {
      this.currentFriend = friend
    },
    setFriendList(list: FriendListResponse[]) {
      this.friendList = list
    }
  }
})
