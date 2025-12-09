<template>
  <div class="room-detail-container">
    <!-- 如果没有选择房间 -->
    <div v-if="!currentRoom" class="no-room-selected">
      <p>请选择一个房间开始聊天</p>
    </div>

    <!-- 如果选择了房间 -->
    <div v-else class="chat-area">
      <!-- 房间头部信息 -->
      <div class="chat-header">
        <el-avatar
          :size="40"
          :src="currentRoom.avatar || defaultRoomAvatar"
          shape="square"
        />
        <div class="room-info">
          <span class="room-name">{{ currentRoom.roomAlias || currentRoom.roomName }}</span>
          <!-- 可以在这里添加更多房间信息，如在线人数等 -->
        </div>
      </div>

      <!-- 消息历史区域 -->
      <div ref="messagesContainerRef" class="messages-container">
        <div
          v-for="(msg, index) in messages"
          :key="index"
          class="message"
          :class="{ 'sent': msg.sender === userStore.username, 'received': msg.sender !== userStore.username }"
        >
          <div class="message-sender">{{ msg.sender }}</div>
          <div class="message-content">{{ msg.content }}</div>
          <div class="message-time">{{ formatTime(msg.time)}}</div>
        </div>
        <!-- 加载指示器或空状态可以放在这里 -->
      </div>

      <!-- 发送消息区域 -->
      <div class="input-area">
        <el-input
          v-model="newMessage"
          placeholder="输入消息..."
          @keyup.enter="sendMessage"
          :disabled="!isJoined"
        />
        <el-button
          type="primary"
          @click="sendMessage"
          :disabled="!isJoined || !newMessage.trim()"
          :loading="isSending"
        >
          发送
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, onUnmounted, ref, watch } from 'vue';
import { ElMessage } from 'element-plus';
import { useRoomStore } from '@/stores/roomStore.ts';
import { useUserStore } from '@/stores/userStore';
import { joinRoom, leaveRoom, sendChatMessage } from '@/utils/websocket.ts'; // 引入 websocketUtil 的操作函数
import defaultRoomAvatar from '@/assets/defaultRoom.png'; // 默认房间头像

const roomStore = useRoomStore();
const userStore = useUserStore();

const newMessage = ref('');
const isSending = ref(false);
const messagesContainerRef = ref<HTMLDivElement | null>(null);

// 计算属性：当前房间
const currentRoom = computed(() => roomStore.currentRoom);

// 计算属性：当前房间的消息列表
const messages = computed(() => {
  if (currentRoom.value) {
    return roomStore.getRoomMessage(currentRoom.value.roomName);
  }
  return [];
});

// 计算属性：是否已加入当前房间
const isJoined = computed(() => {
  return currentRoom.value ? roomStore.isListeningRoom(currentRoom.value.roomName) : false;
});

/**
 * 格式化时间戳
 * @param timestamp ISO 8601 时间字符串
 */
const formatTime = (timestamp: string | undefined): string => {
  if (!timestamp) return '';
  const date = new Date(timestamp);
  // 简单格式化为 HH:mm:ss，你可以根据需要调整
  return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
};

/**
 * 滚动消息容器到底部
 */
const scrollToBottom = async () => {
  await nextTick(); // 确保 DOM 更新完毕
  if (messagesContainerRef.value) {
    messagesContainerRef.value.scrollTop = messagesContainerRef.value.scrollHeight;
  }
};

/**
 * 发送消息
 */
const sendMessage = async () => {
  if (!currentRoom.value || !newMessage.value.trim() || !isJoined.value) {
    return;
  }

  const content = newMessage.value.trim();
  isSending.value = true;

  try {
    await sendChatMessage(currentRoom.value.roomName, content);
    console.log(`[RoomDetailView] Sent message to ${currentRoom.value.roomName}: ${content}`);
    newMessage.value = ''; // 清空输入框
    // 注意：发送的消息会在 handleWsMessageReceived 回调中被添加到 store 并触发视图更新
  } catch (error) {
    console.error('[RoomDetailView] Failed to send message:', error);
    ElMessage.error('消息发送失败');
  } finally {
    isSending.value = false;
  }
};

/**
 * 加入房间
 */
const doJoinRoom = async (roomName: string) => {
  if (roomStore.isListeningRoom(roomName)) {
    console.log(`[RoomDetailView] Already joined room: ${roomName}`);
    return; // 已经加入了
  }
  try {
    await joinRoom(roomName);
    roomStore.addListeningRoom(roomName); // 更新 Store 状态
    console.log(`[RoomDetailView] Joined room: ${roomName}`);
    // 加入后滚动到底部
    await scrollToBottom();
  } catch (error) {
    console.error(`[RoomDetailView] Failed to join room ${roomName}:`, error);
    ElMessage.error(`加入房间 ${roomName} 失败`);
  }
};

/**
 * 离开房间
 */
const doLeaveRoom = async (roomName: string) => {
  if (!roomStore.isListeningRoom(roomName)) {
    console.log(`[RoomDetailView] Not joined room: ${roomName}, skipping leave.`);
    return; // 没有加入过
  }
  try {
    await leaveRoom(roomName);
    roomStore.removeListeningRoom(roomName); // 更新 Store 状态
    console.log(`[RoomDetailView] Left room: ${roomName}`);
  } catch (error) {
    console.error(`[RoomDetailView] Failed to leave room ${roomName}:`, error);
    ElMessage.error(`离开房间 ${roomName} 失败`);
    // 即使失败也从本地状态移除，避免卡死
    roomStore.removeListeningRoom(roomName);
  }
};


// 监听消息变化，自动滚动到底部
watch(messages, async () => {
  await scrollToBottom();
}, { deep: true }); // deep watch might be needed depending on how messages are updated in the store

// 监听当前房间变化
watch(currentRoom, async (newVal, oldVal) => {
  const oldRoomName = oldVal?.roomName;
  const newRoomName = newVal?.roomName;

  // 如果离开了旧房间
  if (oldRoomName) {
    await doLeaveRoom(oldRoomName);
  }

  // 如果进入了新房间
  if (newRoomName) {
    await doJoinRoom(newRoomName);
  }
}, { immediate: true }); // immediate: true 确保在组件挂载时也执行一次


// 组件挂载时（如果已经有 currentRoom）
onMounted(async () => {
  console.log('[RoomDetailView] Mounted');
  // 加入逻辑已经在 watch(currentRoom, ...) 的 immediate: true 中处理了
});

// 组件卸载时离开当前房间
onUnmounted(async () => {
  console.log('[RoomDetailView] Unmounted');
  if (currentRoom.value) {
    await doLeaveRoom(currentRoom.value.roomName);
  }
});

</script>

<style scoped>
.room-detail-container {
  display: flex;
  flex-direction: column;
  height: 100%; /* 确保占满父容器高度 */
  width: 100%;
  background-color: #ffffff;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  overflow: hidden; /* 防止内容溢出 */
}

.no-room-selected {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  color: #999;
  font-size: 16px;
}

.chat-area {
  display: flex;
  flex-direction: column;
  height: 100%;
}

/* 聊天头部 */
.chat-header {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid #eee;
  background-color: #f8f9fa;
}
.room-info {
  margin-left: 12px;
}
.room-name {
  font-size: 18px;
  font-weight: bold;
  color: #333;
}

/* 消息容器 */
.messages-container {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px; /* 消息之间的间距 */
  background-color: #fafafa; /* 可选背景色 */
}
.message {
  max-width: 70%;
  padding: 8px 12px;
  border-radius: 12px;
  position: relative;
  word-wrap: break-word; /* 长单词换行 */
}
.message.sent {
  align-self: flex-end;
  background-color: #409eff; /* Element Plus primary color */
  color: white;
}
.message.received {
  align-self: flex-start;
  background-color: #e4e7ed; /* Element Plus info light color */
  color: #606266;
}
.message-sender {
  font-size: 12px;
  font-weight: bold;
  margin-bottom: 4px;
}
.message-content {
  font-size: 14px;
}
.message-time {
  font-size: 10px;
  text-align: right;
  margin-top: 4px;
  opacity: 0.8;
}

/* 输入区域 */
.input-area {
  display: flex;
  padding: 12px 16px;
  border-top: 1px solid #eee;
  background-color: #fff;
  gap: 8px; /* 输入框和按钮之间的间距 */
}
.input-area .el-input {
  flex: 1; /* 输入框占据剩余空间 */
}
</style>
