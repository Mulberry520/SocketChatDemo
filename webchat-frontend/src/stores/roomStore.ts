import {defineStore} from "pinia";
import type {ChatMessage, RoomDetailResponse, RoomListResponse} from "@/types/room.ts";
import {reactive} from "vue";

export const useRoomStore = defineStore('room', {
  state: () => ({
    currentRoom: null as RoomDetailResponse | null,
    roomList: [] as RoomListResponse[],
    listeningRooms: new Set<string>(),
    roomMessages: reactive(new Map<string, ChatMessage[]>()) as Map<string, ChatMessage[]>
  }),
  actions: {
    setCurrentRoom(room: RoomDetailResponse | null) {
      this.currentRoom = room
    },
    setRoomList(list: RoomListResponse[]) {
      this.roomList = list
    },
    addListeningRoom(roomName: string) {
      this.listeningRooms.add(roomName)
    },
    removeListeningRoom(roomName: string) {
      this.listeningRooms.delete(roomName)
    },
    isListeningRoom(roomName: string): boolean {
      return this.listeningRooms.has(roomName)
    },
    addMessageToRoom(roomName: string, message: ChatMessage) {
      if (!this.roomMessages.has(roomName)) {
        this.roomMessages.set(roomName, []);
      }
      const messages = this.roomMessages.get(roomName)!;
      messages.push(message)
    },
    getRoomMessage(roomName: string): ChatMessage[] {
      return this.roomMessages.get(roomName) || [];
    }
  }
})
