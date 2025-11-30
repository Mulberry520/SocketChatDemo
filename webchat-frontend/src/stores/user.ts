import { defineStore } from "pinia"

export const userStore = defineStore(
  "userStore",
  {
    actions: {
      changeUsername(role: string) {
        this.username += role
      }
    },
    getters: {
      getUsername():string {
        return this.username;
      }
    },
    state() {
      return {
        username: "test22"
      }
    },
  }
)

