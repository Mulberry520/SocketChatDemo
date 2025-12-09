<template>
  <div v-if="current" class="friend-detail-container">
    <!-- 头像 + 基础信息（横向布局） -->
    <div class="header-row">
      <el-avatar :size="100" :src="current.avatar || defaultAvatar" fit="cover" />

      <div class="info-section">
        <div class="nickname">{{ current.nickname || '—' }}</div>
        <div class="username-row">
          <span>@{{ current.username }}</span>
          <el-icon
            class="favor-icon"
            :class="{ favorite: current.isFavor }"
            @click="toggleFavor"
          >
            <Star />
          </el-icon>
        </div>
        <div class="status">{{ current.status === 'ONLINE' ? '在线' : '离线' }}</div>
      </div>
    </div>

    <!-- 分割线 -->
    <el-divider />

    <!-- 备注（alias） -->
    <div class="alias-section">
      <div class="label">备注</div>
      <div class="alias-input-wrapper">
        <el-input
          v-model="tempAlias"
          ref="aliasInputRef"
          placeholder="点击设置备注"
          clearable
          size="large"
          style="width: 100%;"
          @focus="onAliasFocus"
        />
        <div v-if="showAliasActions" class="alias-actions">
          <el-button
            type="success"
            size="small"
            circle
            @click="confirmAlias"
          >
            <el-icon><Check /></el-icon>
          </el-button>
          <el-button
            type="info"
            size="small"
            circle
            @click="cancelAlias"
          >
            <el-icon><Close /></el-icon>
          </el-button>
        </div>
      </div>
    </div>

    <!-- 其他信息（两列布局） -->
    <div class="detail-grid">
      <div class="field-row">
        <span class="label">性别：</span>
        <span class="value">
          {{
            current.gender === 'male'
              ? '男'
              : current.gender === 'female'
                ? '女'
                : '保密'
          }}
        </span>
      </div>
      <div class="field-row">
        <span class="label">地区：</span>
        <span class="value">{{ current.region || '—' }}</span>
      </div>
      <div class="field-row">
        <span class="label">生日：</span>
        <span class="value">{{ formatDate(current.birth) }}</span>
      </div>
      <div class="field-row">
        <span class="label">邮箱：</span>
        <span class="value">{{ current.email || '—' }}</span>
      </div>
      <div class="field-row">
        <span class="label">简介：</span>
        <span class="value bio">{{ current.biography || '—' }}</span>
      </div>
    </div>

    <!-- 按钮并排 -->
    <div class="action-buttons">
      <el-button type="primary" size="large" style="flex: 1; margin-right: 8px;">发起聊天</el-button>
      <el-button type="danger" size="large" style="flex: 1;" @click="handleDeleteFriend">删除好友</el-button>
    </div>
  </div>

  <div v-else class="empty-placeholder">
    <el-empty description="好友详情页" />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, nextTick } from 'vue'
import { Star, Check, Close } from '@element-plus/icons-vue'
import { useFriendStore } from '@/stores/friendStore'
import { changeFriendInfo, deleteFriend } from '@/api/friend'
import defaultAvatar from '@/assets/defaultAvatar.png'
import { ElMessage, ElMessageBox } from 'element-plus'

const friendStore = useFriendStore()
const current = computed(() => friendStore.currentFriend)

// --- Alias 编辑状态 ---
const tempAlias = ref('')
const originalAlias = ref('')
const showAliasActions = ref(false)
const aliasInputRef = ref()

const onAliasFocus = () => {
  if (!current.value) {
    return
  }

  originalAlias.value = current.value.alias || ''
  tempAlias.value = current.value.alias || ''
  showAliasActions.value = true
  nextTick(() => {
    aliasInputRef.value?.focus()
  })
}

const confirmAlias = async () => {
  if (!current.value) return
  const newAlias = tempAlias.value.trim()
  const req = {
    friendUsername: current.value.username,
    alias: newAlias || null,
    isFavor: current.value.isFavor
  }
  tempAlias.value = ''

  try {
    await changeFriendInfo(req)
    current.value.alias = newAlias || null
    ElMessage.success('备注已更新')
    showAliasActions.value = false
  } catch (err) {
    ElMessage.error('更新备注失败')
  }
}

const cancelAlias = () => {
  tempAlias.value = originalAlias.value
  showAliasActions.value = false
}

// --- 收藏切换 ---
const toggleFavor = async () => {
  if (!current.value) return

  const newFavor = !current.value.isFavor
  const req = {
    friendUsername: current.value.username,
    alias: current.value.alias || null,
    isFavor: newFavor
  }

  try {
    await changeFriendInfo(req)
    current.value.isFavor = newFavor
    ElMessage.success(newFavor ? '已特别关注' : '取消特别关注')
  } catch (err) {
    ElMessage.error('操作失败')
  }
}

// --- 删除好友 ---
const handleDeleteFriend = () => {
  if (!current.value) return

  ElMessageBox.confirm(
    `确定要删除好友 "${current.value.nickname || current.value.username}" 吗？`,
    '删除好友',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
      draggable: true
    }
  )
    .then(async () => {
      try {
        await deleteFriend(current.value!.username)
        ElMessage.success('好友已删除')
        friendStore.removeFriend(current.value!.username)
        friendStore.setCurrentFriend(null)
      } catch (err) {
        ElMessage.error('删除失败')
      }
    })
    .catch(() => {
      // 用户取消
    })
}

const formatDate = (dateStr: string) => {
  if (!dateStr) return '—'
  return dateStr.replace(/^(\d{4})-(\d{2})-(\d{2}).*$/, '$1年$2月$3日')
}
</script>

<style scoped>
.friend-detail-container {
  min-height: 100%;
  overflow-y: auto;
  padding: 24px;
  box-sizing: border-box;
  border-radius: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.header-row {
  display: flex;
  gap: 20px;
  align-items: flex-start;
  flex: none;
}

.info-section {
  flex: 1;
}

.nickname {
  font-size: 20px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  margin-bottom: 4px;
}

.username-row {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 500;
  color: var(--el-text-color-regular);
}

.favor-icon {
  font-size: 18px;
  color: var(--el-color-info);
  cursor: pointer;
  transition: color 0.2s;
}

.favor-icon.favorite {
  color: #f7ba2a;
}

.status {
  font-size: 14px;
  color: var(--el-color-success);
  margin-top: 4px;
}

.el-divider {
  margin: 24px 0;
}

.alias-section {
  margin-bottom: 24px;
}

.label {
  font-size: 15px;
  color: var(--el-text-color-secondary);
  margin-bottom: 8px;
  font-weight: 500;
}

/* 备注输入容器 */
.alias-input-wrapper {
  position: relative;
  width: 100%;
}

.alias-input-wrapper .el-input {
  padding-right: 90px; /* 为按钮留空间 */
}

.alias-actions {
  position: absolute;
  right: 8px;
  top: 50%;
  transform: translateY(-50%);
  display: flex;
  gap: 6px;
}

.detail-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-bottom: 24px;
}

.field-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.label {
  width: 50px;
  font-size: 15px;
  color: var(--el-text-color-secondary);
  font-weight: 500;
}

.value {
  flex: 1;
  font-size: 15px;
  color: var(--el-text-color-primary);
  word-break: break-word;
}

.bio {
  line-height: 1.5;
}

.action-buttons {
  display: flex;
  gap: 8px;
  margin-top: 32px;
  flex: none;
}

@media (max-width: 500px) {
  .detail-grid {
    grid-template-columns: 1fr;
  }
  .action-buttons {
    flex-direction: column;
  }
  .alias-input-wrapper .el-input {
    padding-right: 10px; /* 小屏隐藏按钮或调整 */
  }
  .alias-actions {
    display: none; /* 可选：小屏不显示按钮，用回车/失焦提交 */
  }
}
</style>
