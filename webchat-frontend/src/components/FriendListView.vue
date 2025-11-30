<template>
  <div class="friends-panel">
    <div class="panel-header">
      <h3>My Friends</h3>
    </div>
    <el-scrollbar class="friends-list">
      <div v-if="loading" class="loading">
        <el-skeleton :rows="5" animated />
      </div>
      <div v-else-if="friends.length === 0" class="empty">
        No friends yet.
      </div>
      <div v-else class="friend-item" v-for="f in friends" :key="f.friendUsername">
        <el-avatar>{{ f.alias?.charAt(0).toUpperCase() || f.friendUsername.charAt(0).toUpperCase() }}</el-avatar>
        <div class="friend-info">
          <div class="name">{{ f.alias || f.friendUsername }}</div>
          <div class="status-tag" :class="`status-${f.friendStatus.toLowerCase()}`">
            {{ f.friendStatus }}
          </div>
        </div>
        <div class="favor">❤️ {{ f.favor }}</div>
      </div>
    </el-scrollbar>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getFriendList } from '@/api/friendship'
import type { FriendsResponse } from '@/types/friendship'
import {ElMessage} from "element-plus";

const friends = ref<FriendsResponse[]>([])
const loading = ref(false)

onMounted(async () => {
  await loadFriends()
})

const loadFriends = async () => {
  loading.value = true
  try {
    const res = await getFriendList()
    friends.value = res.data
  } catch (err) {
    console.error('Failed to load friends', err)
    ElMessage.error('Failed to load friend list')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.friends-panel {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.panel-header {
  padding: 16px;
  border-bottom: 1px solid #eee;
  font-weight: 600;
}

.friends-list {
  flex: 1;
}

.friend-item {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  gap: 12px;
  border-bottom: 1px solid #f5f5f5;
}

.friend-info {
  flex: 1;
  min-width: 0;
}

.name {
  font-weight: 600;
  font-size: 14px;
  margin-bottom: 4px;
}

.status-tag {
  font-size: 10px;
  padding: 2px 6px;
  border-radius: 4px;
  display: inline-block;
}

.status-online {
  background: #eaf8f0;
  color: #07c160;
}

.status-offline {
  background: #f5f5f5;
  color: #999;
}

.favor {
  font-size: 12px;
  color: #ff6b6b;
  white-space: nowrap;
}

.loading,
.empty {
  padding: 20px;
  text-align: center;
  color: #999;
}
</style>
