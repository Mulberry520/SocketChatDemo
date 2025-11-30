<template>
  <div class="login-container">
    <el-card class="login-card">
      <h2>{{ isRegister ? 'Register' : 'Login' }}</h2>

      <!-- Login form -->
      <el-form
        v-if="!isRegister"
        @submit.prevent="handleLogin"
        label-position="top"
        :disabled="loading"
      >
        <el-form-item label="Username" required>
          <el-input
            v-model="loginForm.username"
            placeholder="Input your username"
            clearable
          />
        </el-form-item>

        <el-form-item label="Password" required>
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="Input your password"
            show-password
            clearable
          />
        </el-form-item>

        <el-button
          type="primary"
          native-type="submit"
          :loading="loading"
          style="width: 100%"
        >
          {{ loading ? 'Logging in...' : 'Login' }}
        </el-button>

        <p v-if="error" class="error">{{ error }}</p>
        <p class="toggle-link" @click="isRegister = true">Don't have an account? Go to register</p>
      </el-form>

      <!-- Register form -->
      <el-form
        v-else
        @submit.prevent="handleRegister"
        label-position="top"
        :disabled="loading"
      >
        <el-form-item label="Username" required>
          <el-input
            v-model="registerForm.username"
            placeholder="Input your username"
            clearable
          />
        </el-form-item>

        <el-form-item label="Password" required>
          <el-input
            v-model="registerForm.password"
            type="password"
            placeholder="Input your password"
            show-password
            clearable
          />
        </el-form-item>

        <el-form-item label="Phone number" required>
          <el-input
            v-model.number="registerForm.phone"
            type="tel"
            placeholder="Enter 11-digit phone number"
            clearable
          />
        </el-form-item>

        <el-button
          type="primary"
          native-type="submit"
          :loading="loading"
          style="width: 100%"
        >
          {{ loading ? 'Registering...' : 'Register' }}
        </el-button>

        <p v-if="error" class="error">{{ error }}</p>
        <p class="toggle-link" @click="isRegister = false">Already have an account? Go to login</p>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { login, register } from '@/api/auth'
import type { LoginRequest, RegisterRequest } from '@/types/auth'

const loginForm = ref<LoginRequest>({
  username: '',
  password: ''
})

const registerForm = ref<RegisterRequest>({
  username: '',
  password: '',
  phone: 0
})

const isRegister = ref(false)
const loading = ref(false)
const error = ref('')
const router = useRouter()

const handleLogin = async () => {
  if (loading.value) return

  loading.value = true
  error.value = ''

  try {
    const response = await login(loginForm.value)
    const accessToken = String(response.data)
    localStorage.setItem('accessToken', accessToken)
    localStorage.setItem('username', loginForm.value.username)

    await router.push('/main')
  } catch (err: any) {
    error.value = err.message || 'Login failed, check username or password'
  } finally {
    loading.value = false
  }
}

const handleRegister = async () => {
  if (loading.value) return

  if (!/^\d{11}$/.test(String(registerForm.value.phone))) {
    error.value = 'Input a valid 11-digit phone number'
    return
  }

  loading.value = true
  error.value = ''

  try {
    await register(registerForm.value)
    error.value = 'Register success! Please login'
    isRegister.value = false
    registerForm.value = { username: '', password: '', phone: 0 }
  } catch (err: any) {
    error.value = err.message || 'Register failed, try again later'
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
}

.login-card {
  width: 100%;
  max-width: 400px;
  padding: 30px;
  border-radius: 8px;
}

.login-card h2 {
  text-align: center;
  margin-bottom: 24px;
  color: #1f2d3d;
}

.error {
  color: #f56c6c;
  font-size: 14px;
  margin-top: 8px;
  text-align: center;
}

.toggle-link {
  text-align: center;
  margin-top: 16px;
  color: #409eff;
  cursor: pointer;
  font-size: 14px;
}

.toggle-link:hover {
  text-decoration: underline;
}
</style>
