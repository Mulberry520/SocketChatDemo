<template>
  <div class="dashboard">
    <h1>Welcome，{{ username }}！</h1>

    <button @click="showUserInfo = true" class="btn-view-info">Information</button>
    <br><br>
    <button @click="handleLogout" class="btn-view-info">Logout</button>

    <!-- 模态框 -->
    <Teleport to="body" v-if="showUserInfo">
      <div class="modal-overlay" @click="showUserInfo = false">
        <div class="modal-content" @click.stop>
          <div class="modal-header">
            <button @click="showUserInfo = false" class="close-btn">×</button>
          </div>
          <UserInfoCard :visible="showUserInfo" @close="showUserInfo = false" />
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup lang="ts">
import {ref} from 'vue'
import UserInfoCard from '@/components/UserinfoCardView.vue'
import {useRouter} from "vue-router";
import {logout} from "@/api/auth.ts";

const router = useRouter()
const showUserInfo = ref(false)
const username = ref('User')

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
</script>

<style scoped>
.dashboard {
  padding: 2rem;
}

.btn-view-info {
  padding: 8px 16px;
  background: #409eff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

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
  border-radius: 16px;
  overflow: visible;
  max-width: 440px;
  width: 100%;
  padding-top: 16px;
}

.modal-header {
  text-align: right;
  padding: 16px 16px 0;
}

.close-btn {
  font-size: 24px;
  background: none;
  border: none;
  cursor: pointer;
  color: #999;
}
</style>
