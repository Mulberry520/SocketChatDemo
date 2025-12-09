<template>
  <el-card class="user-info-card" shadow="never">
    <!-- 查看模式 -->
    <div v-if="!isEditing">
      <!-- 头像 -->
      <div class="avatar-container">
        <el-avatar :size="80" :src="avatarUrl" />
      </div>

      <!-- 昵称 & 用户名 -->
      <h3 class="nickname">{{ userInfo.nickname || userStore.username }}</h3>
      <p class="username">@{{ userStore.username }}</p>

      <!-- 性别 + 地区 -->
      <div class="row">
        <span v-if="userInfo.gender" class="field">
          性别：{{ displayGender }}
        </span>
        <span v-if="userInfo.region" class="field">
          地区：{{ userInfo.region }}
        </span>
      </div>

      <!-- 生日 -->
      <div v-if="userInfo.birth" class="row">
        <span class="field">生日：{{ formatDate(userInfo.birth) }}</span>
      </div>

      <!-- 手机 -->
      <div class="row">
        <span class="field">手机：{{ userInfo.phone }}</span>
      </div>

      <!-- 邮箱 -->
      <div v-if="userInfo.email" class="row">
        <span class="field">邮箱：{{ userInfo.email }}</span>
      </div>

      <!-- 简介 -->
      <div v-if="userInfo.biography" class="row bio">
        <span class="field">简介：</span>
        <span class="bio-content">{{ userInfo.biography }}</span>
      </div>

      <!-- 注册时间 -->
      <div class="row">
        <span class="field">注册：{{ formatDate(String(userInfo.createTime)) }}</span>
      </div>

      <!-- 编辑按钮 -->
      <div class="action-area">
        <el-button @click="startEdit" type="primary" plain>编辑个人信息</el-button>
      </div>
    </div>

    <!-- 编辑模式 -->
    <div v-else>
      <!-- 头像区域 -->
      <div class="avatar-section">
        <div class="avatar-editor" @click="triggerFileInput">
          <el-avatar :size="80" :src="previewAvatar" />
          <div class="change-overlay">点击更换</div>
          <input
            ref="fileInput"
            type="file"
            accept="image/*"
            @change="handleFileChange"
            style="display: none"
          />
        </div>
      </div>

      <!-- 昵称 -->
      <el-form-item label="昵称">
        <el-input v-model="editForm.nickname" placeholder="请输入昵称" />
      </el-form-item>

      <!-- 性别 + 地区（同行） -->
      <el-row :gutter="16" class="dual-row">
        <el-col :span="12">
          <el-form-item label="性别">
            <el-select v-model="editForm.gender" placeholder="请选择性别" style="width: 100%">
              <el-option value="" label="保密" />
              <el-option value="male" label="男" />
              <el-option value="female" label="女" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="地区">
            <el-input v-model="editForm.region" placeholder="例如：北京" />
          </el-form-item>
        </el-col>
      </el-row>

      <!-- 生日 -->
      <el-form-item label="生日">
        <el-date-picker
          v-model="editForm.birth"
          type="date"
          placeholder="选择生日"
          format="YYYY年MM月DD日"
          value-format="YYYY-MM-DD"
          style="width: 100%"
        />
      </el-form-item>

      <!-- 邮箱 -->
      <el-form-item label="邮箱">
        <el-input v-model="editForm.email" type="email" placeholder="your@email.com" />
      </el-form-item>

      <!-- 简介 -->
      <el-form-item label="简介">
        <el-input
          v-model="editForm.biography"
          type="textarea"
          :rows="4"
          placeholder="介绍一下自己..."
          maxlength="200"
          show-word-limit
        />
      </el-form-item>

      <!-- 操作按钮 -->
      <div class="form-actions">
        <el-button @click="cancelEdit">取消</el-button>
        <el-button
          @click="saveEdit"
          :loading="saving"
          type="primary"
          :disabled="saving"
        >
          {{ saving ? '保存中...' : '保存' }}
        </el-button>
      </div>
    </div>
  </el-card>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import {
  updateUserinfo,
  updateUserAvatar, getUserinfo, getUserAvatar
} from '@/api/user'
import type { UserUpdateRequest } from '@/types/userinfo'
import { useUserStore } from '@/stores/userStore'

const props = defineProps<{
  visible: boolean
}>()

const isEditing = ref(false)
const saving = ref(false)
const fileInput = ref<HTMLInputElement | null>(null)
const selectedFile = ref<File | null>(null)

const userStore = useUserStore()

const avatarUrl = computed(() => {
  return userStore.avatar
})
const userInfo = computed(() => {
  return userStore.userinfo || {
    username: '',
    nickname: null,
    gender: null,
    birth: null,
    region: null,
    biography: null,
    email: null,
    phone: '',
    createTime: ''
  }
})
const displayGender = computed(() => {
  const gender = userInfo.value.gender
  if (gender === 'male') {
    return '男'
  }
  if (gender === 'female') {
    return '女'
  }
  return '保密'
})

const previewAvatar = computed(() => {
  if (selectedFile.value) {
    return URL.createObjectURL(selectedFile.value)
  }
  return avatarUrl.value
})

const editForm = ref<UserUpdateRequest>({
  nickname: null,
  gender: null,
  birth: null,
  region: null,
  biography: null,
  email: null
})


const refreshUserinfo = async () => {
  try {
    const userinfoRes = await getUserinfo()
    userStore.setUserinfo(userinfoRes.data)

    const avatarRes = await getUserAvatar()
    userStore.setAvatar(avatarRes.data)
  } catch (err) {
    console.error('刷新用户信息失败', err)
    ElMessage.error('刷新用户信息失败')
  }
}

const syncToEditForm = () => {
  editForm.value = {
    nickname: userStore.userinfo?.nickname ?? null,
    gender: userStore.userinfo?.gender ?? null,
    birth: userStore.userinfo?.birth ?? null,
    region: userStore.userinfo?.region ?? null,
    biography: userStore.userinfo?.biography ?? null,
    email: userStore.userinfo?.email ?? null
  }
}

const startEdit = () => {
  syncToEditForm()
  isEditing.value = true
  selectedFile.value = null
}

const cancelEdit = () => {
  isEditing.value = false
  selectedFile.value = null
}

const triggerFileInput = () => {
  fileInput.value?.click()
}

const handleFileChange = (e: Event) => {
  const target = e.target as HTMLInputElement
  const file = target.files?.[0]
  if (file && file.type.startsWith('image/')) {
    selectedFile.value = file
  } else if (file) {
    ElMessage.warning('请选择图片文件')
  }
}

const saveEdit = async () => {
  saving.value = true
  try {
    const clean = (str: string | null) => (str === '' ? null : str)
    const payload: UserUpdateRequest = {
      nickname: clean(editForm.value.nickname),
      gender: clean(editForm.value.gender),
      birth: clean(editForm.value.birth),
      region: clean(editForm.value.region),
      biography: clean(editForm.value.biography),
      email: clean(editForm.value.email)
    }

    await updateUserinfo(payload)
    if (selectedFile.value) {
      await updateUserAvatar(selectedFile.value)
    }

    await refreshUserinfo()
    isEditing.value = false
    ElMessage.success('保存成功！')
  } catch (err) {
    console.error('保存失败', err)
    ElMessage.error('保存失败，请重试')
  } finally {
    saving.value = false
  }
}

const formatDate = (dateStr: string) => {
  return dateStr.replace(/(\d{4})-(\d{2})-(\d{2})/, '$1年$2月$3日')
}
</script>

<style scoped>
.user-info-card {
  max-width: 500px;
  margin: 0 auto;
  border-radius: 12px;
  background-color: #fff;
}

/* 查看模式头像 */
.avatar-container {
  display: flex;
  justify-content: center;
  margin-bottom: 20px;
}

/* 编辑模式头像 —— 关键：居中 */
.avatar-section {
  display: flex;
  justify-content: center;
  margin-bottom: 24px;
}

.avatar-editor {
  position: relative;
  cursor: pointer;
  width: fit-content; /* 防止被拉宽 */
}

.change-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.4);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  opacity: 0;
  transition: opacity 0.2s;
}

.avatar-editor:hover .change-overlay {
  opacity: 1;
}

.nickname {
  text-align: center;
  font-size: 18px;
  font-weight: 600;
  margin: 16px 0 8px;
  color: var(--el-text-color-primary);
}

.username {
  text-align: center;
  font-size: 14px;
  color: var(--el-text-color-secondary);
  margin-bottom: 20px;
}

.row {
  display: flex;
  gap: 12px;
  margin-bottom: 12px;
  padding: 0 16px;
  font-size: 14px;
  color: var(--el-text-color-regular);
}

.bio {
  flex-direction: column;
  gap: 4px;
}

.bio .field {
  font-weight: 600;
  color: var(--el-text-color-secondary);
}

.bio-content {
  line-height: 1.5;
  white-space: pre-wrap;
}

.action-area {
  text-align: center;
  margin-top: 24px;
}

.dual-row {
  margin-bottom: 16px;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 24px;
}
</style>
