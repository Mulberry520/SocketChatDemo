<template>
  <div class="login-container">
    <div class="login-card">
      <h2>{{ isRegister ? 'Register' : 'Login' }}</h2>

      <!-- Login form -->
      <form v-if="!isRegister" @submit.prevent="handleLogin">
        <div class="form-group">
          <label>Username</label>
          <input v-model="loginForm.username" type="text" placeholder="Input your username" required />
        </div>
        <div class="form-group">
          <label>Password</label>
          <input v-model="loginForm.password" type="password" placeholder="Input your password" required />
        </div>
        <button type="submit" :disabled="loading">
          {{ loading ? 'Logged in ...' : 'Login' }}
        </button>
        <p v-if="error" class="error">{{ error }}</p>
        <p class="toggle-link" @click="isRegister = true">Don't have an account? Go to register</p>
      </form>

      <!-- register form -->
      <form v-else @submit.prevent="handleRegister">
        <div class="form-group">
          <label>Username</label>
          <input v-model="registerForm.username" type="text" placeholder="Input your username" required />
        </div>
        <div class="form-group">
          <label>Password</label>
          <input v-model="registerForm.password" type="password" placeholder="Input your password" required />
        </div>
        <div class="form-group">
          <label>Phone number</label>
          <input
            v-model.number="registerForm.phone"
            type="tel"
            required
          />
        </div>
        <button type="submit" :disabled="loading">
          {{ loading ? 'Registering ...' : 'Register' }}
        </button>
        <p v-if="error" class="error">{{ error }}</p>
        <p class="toggle-link" @click="isRegister = false">Already have an account? Go to login</p>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { login, register } from '@/api/auth'
import type { LoginRequest, RegisterRequest } from '@/types/auth'
import {connect} from "@/utils/websocket.ts";

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
  if (loading.value) {
    return
  }

  loading.value = true
  error.value = ''

  try {
    const response = await login(loginForm.value)
    const accessToken = String(response.data)
    localStorage.setItem('accessToken', accessToken)

    await router.push('/dashboard')
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
  padding: 2rem;
  background: white;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

h2 {
  text-align: center;
  margin-bottom: 1.5rem;
  color: #333;
}

.form-group {
  margin-bottom: 1rem;
}

label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: bold;
  color: #555;
}

input {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 1rem;
}

input:focus {
  outline: none;
  border-color: #409eff;
}

button {
  width: 100%;
  padding: 0.75rem;
  background-color: #409eff;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 1rem;
  cursor: pointer;
}

button:disabled {
  background-color: #a0cfff;
  cursor: not-allowed;
}

.error {
  color: #f56565;
  margin-top: 0.5rem;
  text-align: center;
}

.toggle-link {
  text-align: center;
  margin-top: 1rem;
  color: #409eff;
  cursor: pointer;
  text-decoration: underline;
}
</style>
