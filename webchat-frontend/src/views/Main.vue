<template>
  <div class="chat-layout">
    <!-- 左侧导航栏 -->
    <div class="left-sidebar">
      <!-- 头像 -->
      <el-avatar
        :size="48"
        :src="userAvatar"
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
              <el-dropdown-item command="lock">Lock</el-dropdown-item>
              <el-dropdown-item command="feedback">Feedback</el-dropdown-item>
              <el-dropdown-item command="settings">Settings</el-dropdown-item>
              <el-dropdown-item divided command="logout">Logout</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>

    <!-- 中间区域 -->
    <div class="middle-panel">
      <template v-if="activeTab === 'chats'">
        <div class="panel-header">
          <el-input
            v-model="searchQuery"
            placeholder="Search"
            prefix-icon="el-icon-search"
            clearable
            size="small"
          />
        </div>
        <el-scrollbar class="scrollable-content">
          <div class="chat-item" v-for="i in 5" :key="i">
            <el-avatar>U</el-avatar>
            <div class="chat-info">
              <div class="name">User {{ i }}</div>
              <div class="last-msg">Hello, how are you?</div>
            </div>
            <div class="time">10:{{ i * 5 }}</div>
          </div>
        </el-scrollbar>
      </template>

      <template v-else-if="activeTab === 'contacts'">
        <FriendListView />
      </template>

      <template v-else-if="activeTab == 'rooms'">
        <ChatRoomListView
          :rooms="chatRooms"
          :selected-room-id="selectedRoomId"
          @select="selectedRoomId = $event"
        />
      </template>
    </div>

    <!-- 右侧主内容区 -->
    <div class="main-content">
      <template v-if="activeTab === 'rooms'">
        <ChatRoomDetailView
          ref="roomDetailRef"
          :room="selectedRoom"
          @join="handleJoinRoom"
          @send-message="handleSendMessage"
        />
      </template>

      <template v-else>
        <div class="welcome-text">
          <h2>Welcome to Chat!</h2>
          <p>Select a chat or contact to start messaging.</p>
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
import {computed, onMounted, ref} from 'vue'
import { Message, User, Setting, ChatDotRound } from '@element-plus/icons-vue'
import defaultAvatar from '@/assets/defaultAvatar.png'
import FriendListView from '@/components/FriendListView.vue'
import UserinfoCardView from '@/components/UserinfoCardView.vue'
import ChatRoomDetailView from "@/components/ChatRoomDetailView.vue"
import ChatRoomListView from "@/components/ChatRoomListView.vue"
import {getUserAvatar, getUserinfo} from "@/api/user.ts";
import {logout} from "@/api/auth.ts";
import router from "@/router";
import {ElMessage} from "element-plus";
import {connect, isConnectedToWebSocket, publish, subscribe} from "@/utils/websocket.ts";

type ActiveTab = 'chats' | 'contacts' | 'rooms'

const activeTab = ref<ActiveTab>('chats')
const showUserInfo = ref(false)
const userAvatar = ref<string>(defaultAvatar)
const searchQuery = ref('')

const chatRooms = ref([
  { id: 'public', name: 'Public Room', description: 'All users can join', isJoined: false }
])
const selectedRoomId = ref<string | null>(null)
const joining = ref(false)
const selectedRoom = computed(() => {
  return chatRooms.value.find(r => r.id === selectedRoomId.value) || null
})


const loadUserData = async () => {
  try {
    const avatarRes = await getUserAvatar()
    console.log(avatarRes.data)
    if (avatarRes.data) {
      userAvatar.value = String(avatarRes.data)
    }
  } catch (err) {
    console.error('Load user data failed')
  }
}

const handleLogout = async () => {
  const confirmed = window.confirm('Are you sure you want to logout?')
  if (!confirmed) {
    return
  }

  try {
    await logout()
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

// 拿到子组件实例
const roomDetailRef = ref<InstanceType<typeof ChatRoomDetailView> | null>(null)

// 加入房间
const handleJoinRoom = async (room: any) => {
  joining.value = true
  try {
    // 连接 WebSocket
    if (!isConnectedToWebSocket()) {
      await new Promise<void>((resolve, reject) => {
        connect(
          () => resolve(),
          (err) => reject(err)
        )
      })
    }

    // 订阅房间消息
    const topic = `/topic/room/${room.id}`
    subscribe(topic, (msg: any) => {
      roomDetailRef.value?.addMessage({
        sender: msg.sender || 'Anonymous',
        content: msg.content || ''
      })
    })

    // 更新状态
    room.isJoined = true
    roomDetailRef.value?.setConnected(true)
    ElMessage.success(`Joined ${room.name}`)
  } catch (err) {
    ElMessage.error('Failed to join room')
    console.error(err)
  } finally {
    joining.value = false
  }
}

// 发送消息
const handleSendMessage = (content: string) => {
  if (!selectedRoom.value) return
  publish(`/app/room/${selectedRoom.value.id}/message`, { content })
}

onMounted(() => {
  loadUserData()
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
  height: 100%;
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

/* 确保 SVG 居中且大小一致 */
.icon-btn svg {
  width: 20px;
  height: 20px;
  display: block; /* 防止基线对齐问题 */
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

.time {
  font-size: 10px;
  color: #c0c4cc;
}

/* 主内容区 */
.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  background: #f0f0f0;
  color: #666;
}

.welcome-text h2 {
  margin-bottom: 8px;
}
</style>
