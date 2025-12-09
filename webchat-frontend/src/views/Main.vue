<template>
  <div class="chat-layout">
    <!-- 左侧导航栏 -->
    <div class="left-sidebar">
      <!-- 头像 -->
      <el-avatar
        :size="48"
        :src="userStore.avatar"
        style="cursor: pointer"
        @click="showUserInfo = true"
      />

      <!-- 导航组：聊天、联系人 -->
      <div class="nav-group">
        <button
          class="icon-btn"
          :class="{ active: activeTab === 'chats' }"
          @click="activeTab = 'chats'"
        >
          <Message />
        </button>

        <button
          class="icon-btn"
          :class="{ active: activeTab === 'contacts' }"
          @click="activeTab = 'contacts'"
        >
          <User />
        </button>

        <button
          class="icon-btn"
          :class="{ active: activeTab === 'rooms'}"
          @click="activeTab = 'rooms'" >
          <ChatDotRound />
        </button>
      </div>

      <!-- 底部操作组：设置、退出 -->
      <div class="bottom-group">
        <el-dropdown trigger="click" @command="handleCommand">
          <el-button circle>
            <Setting />
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="lock">锁定账号</el-dropdown-item>
              <el-dropdown-item command="feedback">提交反馈</el-dropdown-item>
              <el-dropdown-item command="settings">更改设置</el-dropdown-item>
              <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>

    <!-- 中间区域 -->
    <div class="middle-panel">
      <template v-if="activeTab === 'chats'">
        <h1>TODO</h1>
      </template>

      <template v-else-if="activeTab === 'contacts'">
        <FriendListView />
      </template>

      <template v-else-if="activeTab == 'rooms'">
        <RoomListView />
      </template>

      <template v-else>
        <div class="welcome-text">
          <h1>Welcome</h1>
        </div>
      </template>
    </div>

    <!-- 右侧主内容区 -->
    <div class="main-content">
      <template v-if="activeTab === 'chats'">
        <websocket-view />
        <h1>TODO</h1>
      </template>

      <template v-else-if="activeTab === 'contacts'">
        <FriendDetailView />
      </template>

      <template v-else-if="activeTab === 'rooms'">
        <RoomDetailView />
      </template>

      <template v-else>
        <div class="welcome-text">
          <h1>Welcome to {{userStore.username}}!</h1>
        </div>
      </template>
    </div>

    <!-- 用户信息弹窗 -->
    <el-dialog
      v-model="showUserInfo"
      title="个人信息"
      width="500px"
      top="8vh"
      :close-on-click-modal="false"
      append-to-body
    >
      <UserinfoCardView
        v-if="showUserInfo"
        :visible="showUserInfo"
        @close="showUserInfo = false"
      />
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import router from "@/router";
import {ElMessage} from "element-plus";
import {onMounted, ref} from 'vue'
import { Message, User, Setting, ChatDotRound } from '@element-plus/icons-vue'

import UserinfoCardView from '@/components/UserinfoCardView.vue'
import FriendListView from '@/components/FriendListView.vue'
import FriendDetailView from "@/components/FriendDetailView.vue";
import RoomListView from "@/components/RoomListView.vue";
import RoomDetailView from "@/components/RoomDetailView.vue";
import WebsocketView from "@/views/WebsocketView.vue";

import {logout} from "@/api/auth.ts";
import {useUserStore} from "@/stores/userStore.ts";
import {getFriendList} from "@/api/friend.ts";
import {useFriendStore} from "@/stores/friendStore.ts";
import {getUserAvatar, getUserinfo} from "@/api/user.ts";
import {getJoinedRoomList} from "@/api/room.ts";
import {useRoomStore} from "@/stores/roomStore.ts";
import type {IFrame} from "@stomp/stompjs";
import type {ChatMessage} from "@/types/room.ts";
import {
  connectWebSocket,
  initWebSocket,
  setOnConnectedCallback, setOnDisconnectedCallback, setOnErrorCallback,
  setOnMessageReceivedCallback
} from "@/utils/websocket.ts";

type ActiveTab = 'chats' | 'contacts' | 'rooms' | null
const activeTab = ref<ActiveTab>(null)

const showUserInfo = ref(false)

const userStore = useUserStore()
const friendStore = useFriendStore()
const roomStore = useRoomStore()


const handleWsConnected = (frame: IFrame) => {
  console.log("🟢 [Main] WebSocket connected successfully!", frame);
  ElMessage.success('WebSocket 连接已建立');
  // TODO
};

const handleWsDisconnected = () => {
  console.log("🔴 [Main] WebSocket disconnected.");
  ElMessage.info('WebSocket 连接已断开');
  // TODO
};

const handleWsError = (frame: IFrame) => {
  console.error("💥 [Main] WebSocket error occurred:", frame);
  ElMessage.error(`WebSocket 错误: ${frame.headers['message'] || '未知错误'}`);
};

const handleWsMessageReceived = (message: ChatMessage) => {
  console.log("📬 [Main] Message received via websocketUtil:", message);
  const roomName = message.recipient || 'world';
  roomStore.addMessageToRoom(roomName, message);
};


const handleLogout = async () => {
  const confirmed = window.confirm('Are you sure you want to logout?')
  if (!confirmed) {
    return
  }

  try {
    await logout()
    userStore.clearUserinfo()
  } catch (err) {
    console.warn('Logout api wrong', err)
  } finally {
    localStorage.removeItem('accessToken')
    await router.push('/login')
  }
}

const handleCommand = (command: string) => {
  switch (command) {
    case 'logout':
      handleLogout()
      break
    case 'lock':
      ElMessage.info('Lock clicked')
      break
    case 'feedback':
      ElMessage.info('Feedback clicked')
      break
    case 'settings':
      ElMessage.info('Settings clicked')
      break
    default:
      console.log('Unknown command:', command)
  }
}

onMounted(async () => {
  try {
    const userinfoRes = await getUserinfo()
    userStore.setUserinfo(userinfoRes.data)

    const avatarRes = await getUserAvatar()
    userStore.setAvatar(avatarRes.data)

    const friendListRes = await getFriendList()
    friendStore.setFriendList(friendListRes.data)

    const roomListRes = await getJoinedRoomList()
    roomStore.setRoomList(roomListRes.data)

    initWebSocket({
      brokerURL: 'ws://localhost:8080/ws',
      debug: import.meta.env.DEV, // 开发模式下开启调试
      reconnectDelay: 3000,
      getToken: () => {
        return userStore.accessToken
      },
    });

    setOnMessageReceivedCallback(handleWsMessageReceived); // <-- Use the setter
    setOnConnectedCallback(handleWsConnected);
    setOnDisconnectedCallback(handleWsDisconnected);
    setOnErrorCallback(handleWsError);

    try {
      await connectWebSocket();
      console.log("[Main] onMounted - WebSocket connection attempt finished.");
    } catch (error) {
      console.error("[Main] onMounted - Failed to connect WebSocket:", error);
      ElMessage.error('WebSocket 连接失败: ' + (error as Error).message);
    }
  } catch (err) {
    console.error('[ERROR] Failed to load friend list:', err);
    ElMessage.error('加载用户信息失败')
  }
})
</script>

<style scoped>
.chat-layout {
  display: flex;
  height: 100vh;
  background-color: #f0f0f0;
}

/* 左侧导航栏 */
.left-sidebar {
  width: 60px;
  background: white;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 16px 0;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  z-index: 10;
  justify-content: space-between;
  height: 98%;
}

.nav-group,
.bottom-group {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.nav-group {
  flex: 1;
  margin-top: 20px;
}

.bottom-group {
  margin-bottom: 16px;
}

:deep(.el-dropdown-menu) {
  min-width: 160px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.15);
  background-color: white;
}

:deep(.el-dropdown-menu .el-dropdown-item) {
  padding: 12px 16px;
  font-size: 14px;
  color: #333;
  transition: all 0.2s;
}

:deep(.el-dropdown-menu .el-dropdown-item:hover) {
  background-color: #f5f5f5;
  color: #409eff;
}

:deep(.el-dropdown-menu .el-dropdown-item.divided) {
  margin-top: 8px;
  border-top: 1px solid #ebeef5;
}

:deep(.bottom-group .el-button) {
  width: 48px;
  height: 48px;
  padding: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

:deep(.bottom-group .el-button svg) {
  width: 18px;
  height: 18px;
}

/* 自定义图标按钮 */
.icon-btn {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  background-color: transparent;
  color: #666;
  cursor: pointer;
  transition: all 0.2s;
  outline: none;
}

.icon-btn:hover {
  background-color: #f0f4ff;
  color: #409eff;
}

.icon-btn.active {
  background-color: #e0e8ff;
  color: #409eff;
}

.icon-btn svg {
  width: 20px;
  height: 20px;
  display: block;
}

/* 中间面板 */
.middle-panel {
  width: 280px;
  background: white;
  border-right: 1px solid #ebeef5;
  display: flex;
  flex-direction: column;
  height: 100%;
}

/* 主内容区 */
.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  overflow: auto;
  background: #f0f0f0;
  color: #666;
}

.welcome-text h1 {
  margin-bottom: 8px;
}
</style>
