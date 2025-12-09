<template>
  <div class="room-detail-container">
    <!-- 如果没有选择房间 -->
    <div v-if="!currentRoom" class="no-room-selected">
      <p>请选择一个房间查看详情</p>
    </div>

    <!-- 如果选择了房间 -->
    <div v-else class="room-content">
      <!-- 未加入房间时的预览界面 -->
      <div v-if="!isJoined" class="room-preview">
        <div class="preview-header">
          <el-avatar :size="80" :src="currentRoom.avatar || defaultRoomAvatar" shape="square" />
          <div class="preview-info">
            <h2 class="preview-room-name">{{ currentRoom.roomAlias || currentRoom.roomName }}</h2>
            <p class="preview-room-id">ID: {{ currentRoom.roomName }}</p>
          </div>
        </div>

        <div class="preview-details">
          <el-descriptions :column="1" size="small" border>
            <el-descriptions-item label="创建者">{{ currentRoom.createUser }}</el-descriptions-item>
            <el-descriptions-item label="类型">{{ currentRoom.isPublic ? '公开' : '私有' }}</el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ formatDate(currentRoom.createTime) }}</el-descriptions-item>
            <el-descriptions-item label="成员数">{{ currentRoom.members.length }}</el-descriptions-item>
            <el-descriptions-item label="我的昵称">{{ currentRoom.userAlias || '-' }}</el-descriptions-item>
            <el-descriptions-item label="简介">{{ currentRoom.information || '-' }}</el-descriptions-item>
          </el-descriptions>

          <div class="preview-members-section">
            <h3>成员列表</h3>
            <ul class="member-list">
              <li v-for="member in currentRoom.members" :key="member" class="member-item">
                {{ member }}
                <el-tag v-if="member === currentRoom.createUser" size="small" type="success">创建者</el-tag>
              </li>
            </ul>
          </div>
        </div>

        <div class="preview-actions">
          <el-button type="primary" @click="handleJoinClick" :loading="isActionLoading" size="large">
            加入聊天
          </el-button>
        </div>
      </div>

      <!-- 已加入房间时的聊天界面 -->
      <div v-else class="chat-area">
        <!-- 聊天室头部信息 (包含详情/退出菜单) -->
        <div class="chat-header">
          <el-avatar :size="40" :src="currentRoom.avatar || defaultRoomAvatar" shape="square" />
          <div class="room-info">
            <span class="room-name">{{ currentRoom.roomAlias || currentRoom.roomName }}</span>
          </div>
          <div class="header-actions">
            <!-- 三点菜单 -->
            <el-dropdown trigger="click" @command="handleMenuCommand">
              <span class="el-dropdown-link">
                <el-icon :size="20"><MoreFilled /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="detail" :icon="InfoFilled">详情</el-dropdown-item>
                  <el-dropdown-item command="leave" :icon="SwitchButton" divided>退出</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>

        <!-- 消息历史区域 -->
        <!-- 消息历史区域 -->
        <div ref="messagesContainerRef" class="messages-container">
          <template v-for="msg in messages" :key="index">
            <!-- JOIN/QUIT 消息 - 居中显示 -->
            <div v-if="msg.type !== 'MESSAGE'" class="system-message">
              <el-tag :type="msg.type === 'JOIN' ? 'success' : 'warning'" size="small" effect="plain">
                <i :class="msg.type === 'JOIN' ? 'el-icon-user' : 'el-icon-user-minus'"></i>
                {{ msg.sender }} {{ msg.type === 'JOIN' ? '加入了房间' : '退出了房间' }}
                <span class="system-message-time">{{ formatTime(msg.time) }}</span>
              </el-tag>
            </div>

            <!-- MESSAGE 消息 - 气泡显示 -->
            <!-- 修改开始 -->
            <div
              v-else
              class="message-wrapper"
              :class="{ 'own-message': msg.sender === userStore.username, 'other-message': msg.sender !== userStore.username }"
            >
              <!-- 接收到的消息：发送者名字在上 -->
              <div v-if="msg.sender !== userStore.username" class="message-sender-above">{{ msg.sender }}</div>

              <!-- 消息气泡 -->
              <div class="message-bubble" :class="{ 'sent': msg.sender === userStore.username, 'received': msg.sender !== userStore.username }">
                <!-- 自己发送的消息：名字不显示在气泡内 -->
                <!-- <div v-if="msg.sender === userStore.username" class="message-sender-inside">{{ msg.sender }}</div> -->
                <div class="message-content">{{ msg.content }}</div>
                <div class="message-time">{{ formatTime(msg.time) }}</div>
              </div>
            </div>
            <!-- 修改结束 -->
          </template>
        </div>

        <!-- 发送消息区域 -->
        <div class="input-area-wrapper">
          <div class="input-area">
            <el-input
              v-model="newMessage"
              placeholder="输入消息..."
              @keyup.enter="sendMessage"
              :disabled="!isJoined"
              type="textarea"
              autosize
              :rows="1"
              :maxRows="3"
              style="padding: 8px; margin-bottom: 4px;"
            />
            <el-button
              type="primary"
              @click="sendMessage"
              :disabled="!isJoined || !newMessage.trim()"
              :loading="isSending"
              style="margin-top: 8px;"
            >
              发送
            </el-button>
          </div>
          <div class="bottom-placeholder"></div>
        </div>
      </div>
    </div>
  </div>

  <!-- 抽屉形式的房间详情 -->
  <el-drawer v-model="drawerVisible" title="房间详情" direction="rtl" size="40%">
    <div class="drawer-content">
      <el-descriptions :column="1" size="small" border>
        <el-descriptions-item label="名称">{{ currentRoom?.roomName }}</el-descriptions-item>
        <el-descriptions-item label="别名">{{ currentRoom?.roomAlias || '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建者">{{ currentRoom?.createUser }}</el-descriptions-item>
        <el-descriptions-item label="类型">{{ currentRoom?.isPublic ? '公开' : '私有' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ currentRoom ? formatDate(currentRoom.createTime) : '' }}</el-descriptions-item>
        <el-descriptions-item label="成员数">{{ currentRoom?.members.length }}</el-descriptions-item>
        <el-descriptions-item label="我的昵称">{{ currentRoom?.userAlias || '-' }}</el-descriptions-item>
        <el-descriptions-item label="简介">{{ currentRoom?.information || '-' }}</el-descriptions-item>
      </el-descriptions>

      <div class="preview-members-section" v-if="currentRoom">
        <h3>成员列表</h3>
        <el-table :data="currentRoom.members.map(m => ({ name: m }))" style="width: 100%" size="small" max-height="300">
          <el-table-column prop="name" label="用户名">
            <template #default="scope">
              {{ scope.row.name }}
              <el-tag v-if="scope.row.name === currentRoom.createUser" size="small" type="success">创建者</el-tag>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
  </el-drawer>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, onUnmounted, ref, watch } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { MoreFilled, InfoFilled, SwitchButton } from '@element-plus/icons-vue';
import { useRoomStore } from '@/stores/roomStore.ts';
import { useUserStore } from '@/stores/userStore';
import { joinRoom, leaveRoom, sendChatMessage } from '@/utils/websocket.ts';
import defaultRoomAvatar from '@/assets/defaultRoom.png';
import type { ChatMessage } from '@/types/room.ts';

const roomStore = useRoomStore();
const userStore = useUserStore();

const isActionLoading = ref(false);
const newMessage = ref('');
const isSending = ref(false);
const messagesContainerRef = ref<HTMLDivElement | null>(null);
const drawerVisible = ref(false);

const currentRoom = computed(() => roomStore.currentRoom);

const messages = computed<ChatMessage[]>(() => {
  if (currentRoom.value) {
    return roomStore.getRoomMessage(currentRoom.value.roomName);
  }
  return [];
});

const isJoined = computed(() => {
  return currentRoom.value ? roomStore.isListeningRoom(currentRoom.value.roomName) : false;
});

/** 格式化时间戳 (用于消息) */
const formatTime = (timestamp: string | undefined): string => {
  if (!timestamp) return '';
  const date = new Date(timestamp);
  return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
};

/** 格式化日期时间 (用于房间创建时间) */
const formatDate = (timestamp: string): string => {
  const date = new Date(timestamp);
  return date.toLocaleString();
};

/** 滚动消息容器到底部 */
const scrollToBottom = async () => {
  await nextTick();
  if (messagesContainerRef.value) {
    messagesContainerRef.value.scrollTop = messagesContainerRef.value.scrollHeight;
  }
};

/** 发送消息 */
const sendMessage = async () => {
  if (!currentRoom.value || !newMessage.value.trim() || !isJoined.value) {
    return;
  }
  const content = newMessage.value.trim();
  isSending.value = true;
  try {
    await sendChatMessage(currentRoom.value.roomName, content);
    console.log(`[RoomDetailView] Sent message to ${currentRoom.value.roomName}: ${content}`);
    newMessage.value = '';
  } catch (error) {
    console.error('[RoomDetailView] Failed to send message:', error);
    ElMessage.error('消息发送失败');
  } finally {
    isSending.value = false;
  }
};

/** 处理“加入聊天”按钮点击 */
const handleJoinClick = async () => {
  if (!currentRoom.value) return;
  const roomName = currentRoom.value.roomName;

  if (isActionLoading.value) return;
  isActionLoading.value = true;

  try {
    await joinRoom(roomName);
    roomStore.addListeningRoom(roomName);
    console.log(`[RoomDetailView] Joined room: ${roomName}`);
    await scrollToBottom();
  } catch (error) {
    console.error(`[RoomDetailView] Failed to join room ${roomName}:`, error);
    ElMessage.error(`加入房间 ${roomName} 失败`);
  } finally {
    isActionLoading.value = false;
  }
};

/** 处理菜单命令 */
const handleMenuCommand = (command: string) => {
  if (command === 'detail') {
    drawerVisible.value = true;
  } else if (command === 'leave') {
    handleLeaveClick(); // 调用现有的退出处理函数
  }
};

/** 处理“退出聊天”按钮点击 */
const handleLeaveClick = async () => {
  if (!currentRoom.value) return;
  const roomName = currentRoom.value.roomName;

  try {
    await ElMessageBox.confirm(
      `确定要退出聊天室 "${currentRoom.value.roomAlias || roomName}" 吗？`,
      '确认退出',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    );
  } catch {
    return;
  }

  if (isActionLoading.value) return;
  isActionLoading.value = true;

  try {
    await leaveRoom(roomName);
    roomStore.removeListeningRoom(roomName);
    console.log(`[RoomDetailView] Left room: ${roomName}`);
    ElMessage.success(`已退出聊天室 ${currentRoom.value.roomAlias || roomName}`);
    drawerVisible.value = false; // 关闭抽屉
  } catch (error) {
    console.error(`[RoomDetailView] Failed to leave room ${roomName}:`, error);
    ElMessage.error(`离开房间 ${roomName} 失败`);
    roomStore.removeListeningRoom(roomName);
  } finally {
    isActionLoading.value = false;
  }
};

watch(messages, async () => {
  await scrollToBottom();
}, { deep: true });

onMounted(async () => {
  console.log('[RoomDetailView] Mounted');
});

onUnmounted(async () => {
  console.log('[RoomDetailView] Unmounted');
  if (currentRoom.value) {
    // ...
  }
});

</script>

<style scoped>
.room-detail-container {
  display: flex;
  flex-direction: column;
  height: 100%;
  width: 100%;
  background-color: #ffffff;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.no-room-selected {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  color: #999;
  font-size: 16px;
}

.room-content {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.room-preview {
  display: flex;
  flex-direction: column;
  height: 100%;
  padding: 20px;
  box-sizing: border-box;
  overflow-y: auto;
}

.preview-header {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #eee;
}

.preview-info {
  margin-left: 20px;
}

.preview-room-name {
  margin: 0 0 5px 0;
  font-size: 24px;
  font-weight: bold;
  color: #333;
}

.preview-room-id {
  margin: 0;
  color: #888;
  font-size: 14px;
}

.preview-details {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.preview-members-section h3 {
  margin-top: 0;
  margin-bottom: 10px;
  color: #333;
}

.member-list {
  list-style-type: none;
  margin: 0;
  max-height: 200px;
  overflow-y: auto;
  border: 1px solid #eee;
  border-radius: 4px;
  padding: 5px;
}

.member-item {
  padding: 5px 10px;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.member-item:last-child {
  border-bottom: none;
}

.preview-actions {
  display: flex;
  justify-content: center;
  padding-top: 20px;
  border-top: 1px solid #eee;
  margin-top: auto;
}

.chat-area {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.chat-header {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid #eee;
  background-color: #f8f9fa;
}

.room-info {
  flex-grow: 1;
  margin-left: 12px;
}

.room-name {
  font-size: 18px;
  font-weight: bold;
  color: #333;
}

.el-dropdown-link {
  cursor: pointer;
  color: var(--el-color-primary);
  outline: none;
}

.el-dropdown-link:focus {
  outline: none;
}

.header-actions {
  margin-right: 8px;
}

.messages-container {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  background-color: #fafafa;
}

/* --- 消息样式 --- */

.system-message {
  display: flex;
  justify-content: center;
  width: 100%;
  margin: 8px 0;
}

.system-message-time {
  margin-left: 8px;
  font-size: 0.8em;
  color: #999;
}

/* 消息气泡 */
.message-bubble {
  padding: 10px 15px;
  border-radius: 18px;
  position: relative;
  word-wrap: break-word;
  display: flex;
  flex-direction: column;
  box-shadow: 0 1px 2px rgba(0,0,0,0.1);
  min-width: 50px;
}

.message-bubble.sent {
  align-self: flex-end;
  background-color: #FFB6C1;
  color: white;
  border-bottom-right-radius: 4px;
}

.message-bubble.received {
  align-self: flex-start;
  background-color: #D8BFD8;
  border: 1px solid #e0e0e0;
  color: white;
  border-bottom-left-radius: 4px;
}

.message-sender-above {
  font-size: 12px;
  font-weight: bold;
  margin-bottom: 4px; /* 名字和气泡之间的间距 */
  color: #666; /* 名字颜色 */
  padding: 0 10px;
}

.message-content {
  font-size: 14px;
  line-height: 1.5;
}

.message-time {
  font-size: 10px;
  text-align: right;
  margin-top: 6px;
  opacity: 0.7;
}

.message-wrapper {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  margin-bottom: 12px; /* 消息块之间的间距 */
  max-width: 100%; /* 防止溢出容器 */
}

.message-wrapper.own-message {
  align-items: flex-end;
}

.message-wrapper.other-message {
  align-items: flex-start;
}

/* 输入区域 */
.input-area {
  display: flex;
  align-items: flex-end;
  gap: 8px;
  padding: 8px 12px;
  border-top: 1px solid #eee;
  background-color: #fff;
  height: auto;
  max-height: 100px;
}

/* 底部空白占位符 */
.bottom-placeholder {
  flex-grow: 1;
  height: 0;
}

.input-area-wrapper {
  display: flex;
  flex-direction: column;
  padding-bottom: 60px;
}


/* 覆盖 Element Plus 默认 textarea 样式，使其更紧凑 */
.input-area :deep(.el-textarea__inner) {
  resize: none;
  min-height: 32px !important;
  padding: 6px 10px;
  font-family: inherit;
}

/* 抽屉内容样式 */
.drawer-content {
  padding: 20px;
  box-sizing: border-box;
  height: 100%;
  display: flex;
  flex-direction: column;
  gap: 20px;
  overflow-y: auto;
}

.drawer-content .preview-members-section {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.drawer-content .preview-members-section h3 {
  margin-top: 0;
}

</style>
