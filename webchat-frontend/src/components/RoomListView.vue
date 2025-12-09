<template>
  <div class="room-list-container">
    <div
      v-for="item in roomList"
      :key="item.roomName"
      class="room-item"
      :class="{ active: item.roomName === currentRoom?.roomName }"
      @click="handleSelect(item.roomName)"
    >
      <el-avatar
        :size="45"
        :src="item.avatar || defaultRoom"
        fit="cover"
      />
      <span class="alias">{{ item.roomAlias || item.roomName }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import {useRoomStore} from "@/stores/roomStore.ts";
import {computed} from "vue";
import {getRoomDetail} from "@/api/room.ts";
import {ElMessage} from "element-plus";
import defaultRoom from "@/assets/defaultRoom.png"
import type {RoomDetailResponse} from "@/types/room.ts";

const roomStore = useRoomStore()

const roomList = computed(() => roomStore.roomList)
const currentRoom = computed(() => roomStore.currentRoom)

const handleSelect = async (roomName: string) => {
  if (currentRoom.value?.roomName === roomName) {
    return
  }

  try {
    console.log("aaa", roomName)
    const res = await getRoomDetail(roomName)
    console.log("aaa", res)

    const roomDetail: RoomDetailResponse = res.data
    const roomItem = roomStore.roomList.find(
      (item) => item.roomName === roomName
    )
    roomDetail.avatar = roomItem?.avatar || defaultRoom

    roomStore.setCurrentRoom(res.data)
  } catch (err: any) {
    ElMessage.error(err.message || '加载房间详情失败')
  }
}

</script>

<style scoped>
.room-list-container {
  height: 100%;
  overflow-y: auto;
  padding: 8px 0;
}

.room-item {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 10px 16px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.room-item:hover,
.room-item.active {
  background-color: var(--el-color-primary-light-9);
}

.room-item.active {
  border-left: 3px solid var(--el-color-primary);
  padding-left: 13px;
}

.alias {
  font-size: 18px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
