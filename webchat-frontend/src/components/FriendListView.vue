<template>
  <div class="friend-list-container">
    <div
      v-for="item in friendList"
      :key="item.friendUsername"
      class="friend-item"
      :class="{ active: item.friendUsername === currentFriend?.username }"
      @click="handleSelect(item.friendUsername)"
    >
      <el-avatar
        :size="45"
        :src="item.avatar || defaultAvatar"
        fit="cover"
      />
      <span class="alias">{{ item.alias || item.friendUsername }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useFriendStore } from '@/stores/friendStore'
import { getFriendDetail } from '@/api/friend'
import defaultAvatar from '@/assets/defaultAvatar.png'
import { ElMessage } from 'element-plus'
import type {FriendDetailResponse} from "@/types/friend.ts";

const friendStore = useFriendStore()

const friendList = computed(() => friendStore.friendList)
const currentFriend = computed(() => friendStore.currentFriend)

const handleSelect = async (username: string) => {
  if (currentFriend.value?.username === username) {
    return
  }

  try {
    const res = await getFriendDetail(username)
    const friendListItem = friendStore.friendList.find(
      (item) => item.friendUsername === username
    )

    const friendDetail:FriendDetailResponse = res.data
    friendDetail.avatar = friendListItem?.avatar || defaultAvatar

    friendStore.setCurrentFriend(friendDetail)
  } catch (err: any) {
    ElMessage.error(err.message || '加载好友详情失败')
  }
}
</script>

<style scoped>
.friend-list-container {
  height: 100%;
  overflow-y: auto;
  padding: 8px 0;
}

.friend-item {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 10px 16px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.friend-item:hover,
.friend-item.active {
  background-color: var(--el-color-primary-light-9);
}

.friend-item.active {
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
