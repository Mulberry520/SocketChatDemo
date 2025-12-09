<template>
  <div class="login-container">
    <el-card class="login-card" shadow="never">
      <h2 class="title">{{ isRegister ? '注册' : '登录' }}</h2>

      <!-- 登录表单 -->
      <el-form
        v-if="!isRegister"
        @submit.prevent="handleLogin"
        label-position="top"
        :disabled="loading"
        size="large"
      >
        <el-form-item label="用户名" required>
          <el-input
            v-model="loginForm.username"
            placeholder="请输入用户名"
            clearable
          />
        </el-form-item>

        <el-form-item label="密码" required>
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            show-password
            clearable
          />
        </el-form-item>

        <el-button
          type="primary"
          native-type="submit"
          :loading="loading"
          style="width: 100%; margin-top: 24px"
        >
          {{ loading ? '登录中...' : '立即登录' }}
        </el-button>

        <p v-if="error" class="error">{{ error }}</p>
        <p class="toggle-link" @click="isRegister = true">还没有账号？去注册</p>
      </el-form>

      <!-- 注册表单 -->
      <el-form
        v-else
        @submit.prevent="handleRegister"
        label-position="top"
        :disabled="loading"
        size="large"
      >
        <el-form-item label="用户名" required>
          <el-input
            v-model="registerForm.username"
            placeholder="请输入用户名"
            clearable
          />
        </el-form-item>

        <el-form-item label="密码" required>
          <el-input
            v-model="registerForm.password"
            type="password"
            placeholder="请输入密码"
            show-password
            clearable
          />
        </el-form-item>

        <el-form-item label="手机号" required>
          <el-input
            v-model.number="registerForm.phone"
            type="tel"
            placeholder="请输入11位手机号"
            clearable
          />
        </el-form-item>

        <el-button
          type="primary"
          native-type="submit"
          :loading="loading"
          style="width: 100%; margin-top: 24px"
        >
          {{ loading ? '注册中...' : '立即注册' }}
        </el-button>

        <p v-if="error" class="error">{{ error }}</p>
        <p class="toggle-link" @click="isRegister = false">已有账号？去登录</p>
      </el-form>
    </el-card>
  </div>
</template>

<!-- script setup 部分保持原样，无需修改 -->
<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { login, register } from '@/api/auth'
import type { LoginRequest, RegisterRequest } from '@/types/auth'
import {useUserStore} from "@/stores/userStore.ts";
import {getUserAvatar} from "@/api/user.ts";

const loginForm = ref<LoginRequest>({
  username: '',
  password: ''
})

const registerForm = ref<RegisterRequest>({
  username: '',
  password: '',
  phone: ''
})

const isRegister = ref(false)
const loading = ref(false)
const error = ref('')
const router = useRouter()

const userStore = useUserStore()

const handleLogin = async () => {
  if (loading.value) {
    return
  }

  loading.value = true
  error.value = ''

  try {
    const response = await login(loginForm.value)
    const accessToken = String(response.data)

    userStore.setAccessToken(accessToken)
    userStore.setUsername(loginForm.value.username)
    loginForm.value = {username: '', password: ''}
    const avatarRes = await getUserAvatar()
    userStore.setAvatar(String(avatarRes.data))

    await router.push('/main')
  } catch (err: any) {
    error.value = err.message || '登录失败，请检查用户名或密码'
  } finally {
    loading.value = false
  }
}

const handleRegister = async () => {
  if (loading.value) {
    return
  }

  if (!/^\d{11}$/.test(String(registerForm.value.phone))) {
    error.value = '请输入有效的11位手机号'
    return
  }

  loading.value = true
  error.value = ''

  try {
    await register(registerForm.value)
    loginForm.value = {
      username: registerForm.value.username,
      password: registerForm.value.password
    }
    registerForm.value = { username: '', password: '', phone: 0 }
    isRegister.value = false

    error.value = '注册成功！请登录'
  } catch (err: any) {
    error.value = err.message || '注册失败，请稍后重试'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #f5f7fa;
  padding: 20px;
}

.login-card {
  width: 100%;
  max-width: 400px;
  border-radius: 12px;
  padding: 32px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.title {
  text-align: center;
  font-size: 24px;
  font-weight: 600;
  margin-bottom: 32px;
  color: var(--el-text-color-primary);
}

.error {
  color: var(--el-color-danger);
  font-size: 14px;
  text-align: center;
  margin: 12px 0;
}

.toggle-link {
  text-align: center;
  font-size: 14px;
  color: var(--el-color-primary);
  cursor: pointer;
  margin-top: 16px;
  user-select: none;
}

.toggle-link:hover {
  text-decoration: underline;
}
</style>
