import { defineStore } from 'pinia'
import defaultAvatar from '@/assets/defaultAvatar.png'
import type {UserinfoResponse} from "@/types/userinfo.ts";

export const useUserStore = defineStore('user', {
  state: () => ({
    accessToken: localStorage.getItem('accessToken') || null,
    username: localStorage.getItem('username') || null,
    avatar: localStorage.getItem('avatar') || defaultAvatar,
    userinfo: null as UserinfoResponse | null
  }),

  actions: {
    setAccessToken(token: string) {
      this.accessToken = token
      localStorage.setItem('accessToken', token)
    },
    setUsername(username: string) {
      this.username = username
      localStorage.setItem('username', username)
    },
    setAvatar(avatar: string) {
      const validAvatar = avatar && avatar.trim() !== '' ? avatar : defaultAvatar
      this.avatar = validAvatar
      localStorage.setItem('avatar', avatar)
    },
    setUserinfo(userinfo: UserinfoResponse) {
      this.userinfo = userinfo
    },

    clearToken() {
      this.accessToken = null
      localStorage.removeItem('accessToken')
    },
    clearUserinfo() {
      this.accessToken = null
      this.username = null
      this.avatar = defaultAvatar
      this.userinfo = null
    }
  }
})
