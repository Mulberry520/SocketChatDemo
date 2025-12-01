<template>
  <div class="panel-header">
    <h3 style="margin: 0; font-size: 16px;">Chat Rooms</h3>
  </div>
  <el-scrollbar class="scrollable-content">
    <div
      v-for="room in rooms"
      :key="room.id"
      class="chat-item"
      :class="{ selected: room.id === selectedRoomId }"
      @click="selectRoom(room.id)"
    >
      <div class="chat-info">
        <div class="name">{{ room.name }}</div>
        <div class="last-msg">{{ room.description }}</div>
      </div>
      <el-tag size="small" type="info" v-if="room.isJoined">Joined</el-tag>
    </div>
  </el-scrollbar>
</template>

<script setup lang="ts">
import { ElTag } from 'element-plus'

interface ChatRoom {
  id: string
  name: string
  description: string
  isJoined: boolean
}

const props = defineProps<{
  rooms: ChatRoom[]
  selectedRoomId: string | null
}>()

const emit = defineEmits<{
  (e: 'select', roomId: string): void
}>()

const selectRoom = (roomId: string) => {
  emit('select', roomId)
}
</script>

<style scoped>
.panel-header {
  padding: 12px;
  border-bottom: 1px solid #eee;
}

.scrollable-content {
  flex: 1;
}

.chat-item {
  display: flex;
  align-items: center;
  padding: 12px;
  gap: 12px;
  cursor: pointer;
}

.chat-item:hover {
  background-color: #f5f5f5;
}

.chat-item.selected {
  background-color: #e0e8ff;
}

.chat-info {
  flex: 1;
  min-width: 0;
}

.name {
  font-weight: 600;
  font-size: 14px;
}

.last-msg {
  font-size: 12px;
  color: #909399;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>
