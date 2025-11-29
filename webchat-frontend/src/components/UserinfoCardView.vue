<template>
  <div class="user-info-card">
    <!-- 查看模式 -->
    <div v-if="!isEditing" class="view-mode">
      <!-- 头像 -->
      <div class="avatar-container">
        <img :src="avatarUrl" alt="Avatar" class="avatar" />
      </div>

      <!-- 昵称 & 用户名 -->
      <h3 class="nickname">{{ userInfo.nickname || userInfo.username }}</h3>
      <p class="username">@{{ userInfo.username }}</p>

      <!-- 性别 + 地区（同行） -->
      <div class="row">
        <span v-if="userInfo.gender" class="field">
          性别：{{ userInfo.gender === 'male' ? '男' : '女' }}
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
        <span class="field">注册：{{ formatDateTime(userInfo.createTime) }}</span>
      </div>

      <!-- 编辑按钮 -->
      <div class="action-area">
        <button @click="startEdit" class="btn-edit">编辑个人信息</button>
      </div>
    </div>

    <!-- 编辑模式 -->
    <div v-else class="edit-mode">
      <!-- 头像上传 -->
      <div class="avatar-editor" @click="triggerFileInput">
        <img :src="previewAvatar" alt="Preview" class="avatar" />
        <div class="change-overlay">点击更换</div>
        <input
          ref="fileInput"
          type="file"
          accept="image/*"
          @change="handleFileChange"
          style="display: none"
        />
      </div>

      <!-- 昵称 -->
      <div class="form-row">
        <label>昵称</label>
        <input v-model="editForm.nickname" type="text" placeholder="昵称" />
      </div>

      <!-- 性别 + 地区（同行） -->
      <div class="form-row dual">
        <div class="half">
          <label>性别</label>
          <select v-model="editForm.gender">
            <option value="">保密</option>
            <option value="male">男</option>
            <option value="female">女</option>
          </select>
        </div>
        <div class="half">
          <label>地区</label>
          <input v-model="editForm.region" type="text" placeholder="例如：北京" />
        </div>
      </div>

      <!-- 生日 -->
      <div class="form-row">
        <label>生日</label>
        <input v-model="editForm.birth" type="date" />
      </div>

      <!-- 邮箱 -->
      <div class="form-row">
        <label>邮箱</label>
        <input v-model="editForm.email" type="email" placeholder="your@email.com" />
      </div>

      <!-- 简介 -->
      <div class="form-row full">
        <label>个人简介</label>
        <textarea v-model="editForm.biography" placeholder="介绍一下自己..."></textarea>
      </div>

      <!-- 操作按钮 -->
      <div class="form-actions">
        <button @click="cancelEdit" class="btn-cancel">取消</button>
        <button @click="saveEdit" :disabled="saving" class="btn-save">
          {{ saving ? '保存中...' : '保存' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { getUserinfo, getUserAvatar, updateUserinfo, updateUserAvatar } from '@/api/user'
import type { UserinfoResponse, UserUpdateRequest } from '@/types/Userinfo'
import defaultAvatar from '@/assets/defaultAvatar.png'

const props = defineProps<{
  visible: boolean
}>()

// 用户数据
const userInfo = ref<UserinfoResponse>({
  username: '',
  nickname: null,
  gender: null,
  birth: null,
  region: null,
  biography: null,
  email: null,
  phone: '',
  createTime: ''
})

// 编辑表单
const editForm = ref<UserUpdateRequest>({
  nickname: null,
  gender: null,
  birth: null,
  region: null,
  biography: null,
  email: null
})

const avatarUrl = ref(defaultAvatar)
const isEditing = ref(false)
const saving = ref(false)
const fileInput = ref<HTMLInputElement | null>(null)
const selectedFile = ref<File | null>(null)

// 预览头像
const previewAvatar = computed(() => {
  if (selectedFile.value) {
    return URL.createObjectURL(selectedFile.value)
  }
  return avatarUrl.value
})

// 加载用户数据
const loadUserData = async () => {
  try {
    const infoRes = await getUserinfo()
    userInfo.value = infoRes.data

    const avatarRes = await getUserAvatar()
    avatarUrl.value = String(avatarRes.data || defaultAvatar)

    syncToEditForm()
  } catch (err) {
    console.error('Failed to load user info', err)
  }
}

// 将 userInfo 同步到 editForm
const syncToEditForm = () => {
  editForm.value = {
    nickname: userInfo.value.nickname,
    gender: userInfo.value.gender,
    birth: userInfo.value.birth,
    region: userInfo.value.region,
    biography: userInfo.value.biography,
    email: userInfo.value.email
  }
}

// 进入编辑模式
const startEdit = () => {
  syncToEditForm()
  isEditing.value = true
  selectedFile.value = null
}

// 取消编辑
const cancelEdit = () => {
  isEditing.value = false
  selectedFile.value = null
}

// 触发文件选择
const triggerFileInput = () => {
  fileInput.value?.click()
}

// 处理文件选择
const handleFileChange = (e: Event) => {
  const target = e.target as HTMLInputElement
  const file = target.files?.[0]
  if (file && file.type.startsWith('image/')) {
    selectedFile.value = file
  } else if (file) {
    alert('请选择图片文件')
  }
}

// 保存编辑
const saveEdit = async () => {
  saving.value = true
  try {
    // 清理空字符串为 null
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

    // 重新加载最新数据
    await loadUserData()
    isEditing.value = false
    alert('保存成功！')
  } catch (err) {
    console.error('保存失败', err)
    alert('保存失败，请重试')
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  if (props.visible) {
    loadUserData()
  }
})

defineExpose({ loadUserData })

// 格式化函数
const formatDate = (dateStr: string) => {
  return dateStr.replace(/(\d{4})-(\d{2})-(\d{2})/, '$1年$2月$3日')
}

const formatDateTime = (dateTimeStr: string) => {
  return dateTimeStr.replace(/(\d{4})-(\d{2})-(\d{2}) (\d{2}:\d{2})/, '$1年$2月$3日 $4')
}
</script>

<style scoped>
.user-info-card {
  width: 100%;
  max-width: 400px;
  padding: 24px;
  background-color: #fff;
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  margin: 0 auto;
  overflow: visible;
}

/* —————— 公共 —————— */
.avatar-container {
  display: flex;
  justify-content: center;
  margin-bottom: 16px;
}

.avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid #f0f0f0;
}

.nickname {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  text-align: center;
  margin: 16px 0 6px;
}

.username {
  font-size: 14px;
  color: #999;
  text-align: center;
  margin: 0 0 20px;
}

.row {
  display: flex;
  align-items: flex-start;
  margin-bottom: 12px;
  padding: 0 16px;
}

.field {
  font-size: 14px;
  color: #333;
}

.bio {
  flex-direction: column;
  gap: 4px;
}

.bio .field {
  font-weight: 600;
  color: #666;
}

.bio-content {
  font-size: 14px;
  color: #333;
  line-height: 1.5;
  white-space: pre-wrap;
}

.action-area {
  margin-top: 24px;
  text-align: center;
}

.btn-edit {
  padding: 8px 24px;
  background: #f5f5f5;
  color: #333;
  border: 1px solid #ddd;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
}

.btn-edit:hover {
  background: #e9e9e9;
}

/* —————— 编辑模式 —————— */
.edit-mode {
  padding: 24px;
}

.avatar-editor {
  position: relative;
  width: 80px;
  height: 80px;
  border-radius: 50%;
  overflow: hidden;
  margin: 0 auto 20px;
  cursor: pointer;
}

.avatar-editor img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.change-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.4);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.2s;
  font-size: 12px;
}

.avatar-editor:hover .change-overlay {
  opacity: 1;
}

.form-row {
  margin-bottom: 16px;
}

.form-row label {
  display: block;
  font-size: 14px;
  color: #666;
  margin-bottom: 6px;
}

.form-row input,
.form-row select,
.form-row textarea {
  width: 100%;
  padding: 8px 10px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  background-color: #fafafa;
}

.form-row textarea {
  min-height: 60px;
  resize: vertical;
}

/* 双列布局：性别 + 地区 */
.dual {
  display: flex;
  gap: 12px;
}

.half {
  flex: 1;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 20px;
}

.btn-cancel, .btn-save {
  padding: 8px 20px;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
}

.btn-cancel {
  background: #f0f0f0;
  color: #333;
}

.btn-save {
  background: #d4b8e7;
  color: white;
}

.btn-save:disabled {
  background: #ccc;
  cursor: not-allowed;
}
</style>
