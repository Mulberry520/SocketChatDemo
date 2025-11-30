<template>
  <div class="room-detail" v-if="room">
    <h2>{{ room.name }}</h2>
    <p>{{ room.description }}</p>

    <!-- 未加入：显示加入按钮 -->
    <el-button
      v-if="!room.isJoined"
      type="primary"
      :loading="joining"
      @click="handleJoin"
    >
      Join Room
    </el-button>

    <!-- 已加入：显示聊天界面 -->
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
import { ref, watch } from 'vue'
import { ElButton, ElInput, ElEmpty } from 'element-plus'

interface ChatRoom {
  id: string
  name: string
  description: string
  isJoined: boolean
}

const props = defineProps<{
  room: ChatRoom | null
}>()

const emit = defineEmits<{
  (e: 'join', room: ChatRoom): void
  (e: 'send-message', content: string): void
}>()

const currentUsername = localStorage.getItem('username')

const messages = ref<Array<{ id: string; sender: string; content: string }>>([])
const inputText = ref('')
const joining = ref(false)
const isConnected = ref(false)

// 监听 room 变化，重置消息
watch(() => props.room, () => {
  messages.value = []
})

const handleJoin = () => {
  if (!props.room) return
  emit('join', props.room)
}

const handleSend = () => {
  if (!inputText.value.trim()) return
  emit('send-message', inputText.value.trim())
  inputText.value = ''
}

const handleKeydown = (e: KeyboardEvent) => {
  if (e.key === 'Enter') {
    if (e.shiftKey) {
      return;
    }

    // 普通 Enter：发送消息
    e.preventDefault(); // 阻止 textarea 默认换行
    handleSend();
  }
}

// 暴露方法给父组件
defineExpose({
  addMessage(msg: { sender: string; content: string }) {
    messages.value.push({ ...msg, id: Date.now().toString() })
  },
  setConnected(status: boolean) {
    isConnected.value = status
  }
})
</script>

<style scoped>
.room-detail {
  padding: 20px;
  height: 100%;
  display: flex;
  flex-direction: column;
  max-width: 800px;
  width: 100%;
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
  max-height: 80px !important; /* 约3行 */
  font-size: 16px;
  padding: 8px 12px;
}

:deep(.message-item .message-bubble .content) {
  white-space: pre-wrap !important;
  word-break: break-word;
  line-height: 1.4;
  margin-top: 2px;
}
</style>
