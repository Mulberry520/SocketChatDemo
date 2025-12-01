<template>
  <div class="room-detail" v-if="room">
    <!-- 房间标题 + Leave 按钮 -->
    <h3 style="margin: 0 0 12px 0; font-size: 16px; display: flex; justify-content: space-between; align-items: center;">
      {{ room.name }}
      <el-button
        v-if="room.isJoined"
        size="small"
        type="danger"
        plain
        @click="handleLeave"
      >
        Leave Room
      </el-button>
    </h3>
    <p style="margin-top: 0; margin-bottom: 12px;">{{ room.description }}</p>

    <!-- 未加入：显示加入按钮 -->
    <el-button
      v-if="!room.isJoined"
      type="primary"
      :loading="joining"
      @click="handleJoin"
    >
      Join Room
    </el-button>

    <!-- 已加入：聊天界面 -->
    <template v-else>
      <div :style="{ color: isConnected ? 'green' : 'red', fontWeight: 'bold', margin: '10px 0' }">
        Status: {{ isConnected ? '✅ Connected' : '❌ Disconnected' }}
      </div>

      <div ref="messageContainer" class="message-list">
        <div
          v-for="msg in messages"
          :key="msg.id"
          class="message-item"
          :class="{ 'self': msg.sender === currentUsername }"
        >
          <div class="message-bubble">
            <div class="sender" v-if="msg.sender !== currentUsername">{{ msg.sender }}</div>
            <div class="content">{{ msg.content }}</div>
          </div>
        </div>
        <div v-if="messages.length === 0" class="no-messages">
          No messages yet...
        </div>
      </div>

      <div class="input-area">
        <el-input
          v-model="inputText"
          @keydown="handleKeydown"
          placeholder="Type a message..."
          clearable
          type="textarea"
          :autosize="{ minRows: 1, maxRows: 3 }"
          resize="none"
        />
        <el-button type="primary" @click="handleSend" :disabled="!inputText.trim()">Send</el-button>
      </div>
    </template>
  </div>

  <div v-else class="empty-hint">
    <el-empty description="No room selected" />
  </div>
</template>

<script setup lang="ts">
import {ref, watch, nextTick, onBeforeUnmount} from 'vue'
import { ElButton, ElInput, ElEmpty, ElMessage } from 'element-plus'
import {
  connect,
  disconnect,
  isConnectedToWebSocket,
  publish,
  subscribe
} from '@/utils/websocket.ts'

interface ChatRoom {
  id: string
  name: string
  description: string
  isJoined: boolean
}

const props = defineProps<{
  room: ChatRoom | null
}>()

const currentUsername = localStorage.getItem('username') || 'Anonymous'

const messages = ref<Array<{ id: string; sender: string; content: string }>>([])
const inputText = ref('')
const joining = ref(false)
const isConnected = ref(false)

// 当前房间的订阅取消函数
let currentUnsubscribe: (() => void) | null = null

// 监听 room 变化
watch(() => props.room, (newRoom, oldRoom) => {
  // 如果切换房间，先清理旧状态
  if (oldRoom && oldRoom.isJoined) {
    handleLeave()
  }
  messages.value = []
}, { immediate: true })

// 加入房间
const handleJoin = async () => {
  if (!props.room) return
  joining.value = true
  try {
    // 确保连接
    if (!isConnectedToWebSocket()) {
      await new Promise<void>((resolve, reject) => {
        connect(
          () => resolve(),
          (err) => reject(err)
        )
      })
    }

    // 订阅当前房间
    const topic = `/topic/room/${props.room.id}`
    currentUnsubscribe = subscribe(topic, (payload: any) => {
      // payload 应包含 id, sender, content
      messages.value.push({
        id: payload.id?.toString() || Date.now().toString(),
        sender: payload.sender || 'Anonymous',
        content: payload.content || ''
      })

      // 可选：自动滚动到底部
      nextTick(() => {
        const container = document.querySelector('.message-list')
        if (container) {
          container.scrollTop = container.scrollHeight
        }
      })
    })

    // 更新状态
    props.room.isJoined = true
    isConnected.value = true
    ElMessage.success(`Joined ${props.room.name}`)
  } catch (err) {
    ElMessage.error('Failed to join room')
    console.error(err)
  } finally {
    joining.value = false
  }
}

// 离开房间
const handleLeave = () => {
  if (!props.room) return

  // 取消订阅
  if (currentUnsubscribe) {
    currentUnsubscribe()
    currentUnsubscribe = null
  }

  // 重置状态
  props.room.isJoined = false
  isConnected.value = false
  messages.value = []

  ElMessage.success(`Left ${props.room.name}`)
}

// 发送消息（仅发给后端，不本地添加）
const handleSend = () => {
  if (!inputText.value.trim() || !props.room) return

  publish(`/app/room/${props.room.id}/message`, {
    content: inputText.value
  })

  inputText.value = ''
}

const handleKeydown = (e: KeyboardEvent) => {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    handleSend()
  }
}

// 组件卸载时清理
defineExpose({
  // 如需外部调用，可暴露方法
})

// 清理订阅（组件销毁时）
onBeforeUnmount(() => {
  if (currentUnsubscribe) {
    currentUnsubscribe()
  }
})
</script>

<style scoped>
.room-detail {
  flex: 1;
  width: 100%;
  padding: 16px;
  height: 100%;
  display: flex;
  flex-direction: column;
  box-sizing: border-box;
}

.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 10px;
  background: #f9f9f9;
  border-radius: 4px;
  margin: 10px 0;
  display: flex;
  flex-direction: column;
}

.no-messages {
  color: gray;
  font-style: italic;
  text-align: center;
  padding: 20px;
}

.message-item {
  display: flex;
  margin-bottom: 10px;
}

.message-item.self {
  justify-content: flex-end;
}

.message-bubble {
  max-width: 70%;
  word-wrap: break-word;
}

.message-item:not(.self) .message-bubble {
  background-color: #e0e0e0;
  border-radius: 12px 12px 12px 0;
  padding: 8px 12px;
}

.message-item.self .message-bubble {
  background-color: #1890ff;
  color: white;
  border-radius: 12px 12px 0 12px;
  padding: 8px 12px;
  text-align: right;
}

.sender {
  font-size: 12px;
  color: #666;
  margin-bottom: 2px;
  font-weight: bold;
}

.input-area {
  display: flex;
  gap: 8px;
  margin-top: 10px;
  align-items: flex-end;
}

:deep(.el-textarea__inner) {
  min-height: 36px !important;
  max-height: 80px !important;
  font-size: 16px;
  padding: 8px 12px;
}
</style>
