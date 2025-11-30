<template>
  <Teleport to="body" v-if="visible">
    <div class="modal-overlay" @click="close">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h2>My Friends</h2>
          <button @click="close" class="close-btn">×</button>
        </div>
        <div class="friends-list">
          <div v-if="loading">Loading friends...</div>
          <div v-else-if="friends.length === 0">No friends yet.</div>
          <div v-else class="friend-item" v-for="f in friends" :key="f.friendUsername">
            <strong>{{ f.alias || f.friendUsername }}</strong>
            <span class="status" :class="f.friendStatus">{{ f.friendStatus }}</span>
            <span class="favor">❤️ {{ f.favor }}</span>
          </div>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import {getFriendList} from "@/api/friendship.ts";
import type {FriendsResponse} from "@/types/friendship.ts";

const props = defineProps<{
  visible: boolean
}>()

const emit = defineEmits<{
  (e: 'close'): void
}>()

const friends = ref<FriendsResponse[]>([])
const loading = ref(false)

const close = () => {
  emit('close')
}

watch(() => props.visible, async (newVal) => {
  if (newVal && friends.value.length === 0) {
    await loadFriends()
  }
})

const loadFriends = async () => {
  loading.value = true
  try {
    const res = await getFriendList()
    friends.value = res.data
  } catch (err) {
    console.error('Failed to load friends', err)
    alert('Failed to load friend list')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 12px;
  padding: 20px;
  width: 90%;
  max-width: 500px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.close-btn {
  font-size: 24px;
  background: none;
  border: none;
  cursor: pointer;
  color: #999;
}

.friends-list {
  max-height: 400px;
  overflow-y: auto;
}

.friend-item {
  display: flex;
  justify-content: space-between;
  padding: 10px 0;
  border-bottom: 1px solid #eee;
}

.friend-item:last-child {
  border-bottom: none;
}

.status {
  font-size: 0.85rem;
  padding: 2px 6px;
  border-radius: 4px;
}

.status.online {
  background: #d4edda;
  color: #155724;
}

.status.offline {
  background: #f8d7da;
  color: #721c24;
}

.favor {
  font-size: 0.9rem;
  color: #e74c3c;
}
</style>
