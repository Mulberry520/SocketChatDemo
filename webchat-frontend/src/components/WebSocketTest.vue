<template>
  <div style="padding: 20px; max-width: 800px; margin: 0 auto;">
    <h2>WebSocket Chat Test</h2>

    <!-- 连接状态 -->
    <div :style="{ color: isConnected ? 'green' : 'red', fontWeight: 'bold' }">
      Status: {{ isConnected ? '✅ Connected' : '❌ Disconnected' }}
    </div>

    <!-- 群聊区域 -->
    <div style="margin-top: 20px;">
      <h3>Group Chat</h3>
      <div
        id="group-messages"
        style="height: 200px; overflow-y: auto; border: 1px solid #ccc; padding: 10px; margin-bottom: 10px; background: #f9f9f9;"
      >
        <div v-for="msg in groupMessages" :key="msg.id">
          <strong>{{ msg.sender }}:</strong> {{ msg.content }}
        </div>
        <div v-if="groupMessages.length === 0" style="color: gray; font-style: italic;">
          No messages yet...
        </div>
      </div>
      <input
        v-model="groupInput"
        @keyup.enter="sendGroupMessage"
        placeholder="Type a group message..."
        style="width: 70%; padding: 8px; margin-right: 5px;"
      />
      <button @click="sendGroupMessage" style="padding: 8px 12px;">Send</button>
    </div>

    <!-- 私聊区域 -->
    <div style="margin-top: 30px;">
      <h3>Private Message</h3>
      <div style="display: flex; gap: 10px; margin-bottom: 10px;">
        <input
          v-model="privateRecipient"
          placeholder="Recipient username"
          style="flex: 1; padding: 8px;"
        />
        <input
          v-model="privateInput"
          @keyup.enter="sendPrivateMessage"
          placeholder="Your private message"
          style="flex: 2; padding: 8px;"
        />
        <button @click="sendPrivateMessage" style="padding: 8px 12px;">Send</button>
      </div>

      <div
        id="private-messages"
        style="height: 150px; overflow-y: auto; border: 1px solid #999; padding: 10px; background: #f0f8ff; color: #0066cc;"
      >
        <div v-for="msg in privateMessages" :key="msg.id">
          <strong>[Private] {{ msg.sender }}:</strong> {{ msg.content }}
        </div>
        <div v-if="privateMessages.length === 0" style="color: gray; font-style: italic;">
          No private messages...
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue';
import {
  connect,
  disconnect,
  subscribe,
  publish,
  isConnectedToWebSocket
} from '@/utils/websocket'; // 👈 引入你的工具模块

// 响应式状态（只管 UI）
const isConnected = ref(false);
const groupMessages = ref<{ id: string; sender: string; content: string }[]>([]);
const privateMessages = ref<{ id: string; sender: string; content: string }[]>([]);
const groupInput = ref('');
const privateInput = ref('');
const privateRecipient = ref('');

// 同步连接状态（可选：用 setInterval 或响应式 store 更佳）
const updateConnectionStatus = () => {
  isConnected.value = isConnectedToWebSocket();
};

// 连接回调
const onWsConnect = () => {
  console.log('✅ Connected via shared websocket module');
  isConnected.value = true;

  // 使用工具模块的 subscribe
  subscribe('/topic/message', (msg) => {
    groupMessages.value.push({ ...msg, id: Date.now().toString() });
  });

  subscribe('/user/queue/private', (msg) => {
    privateMessages.value.push({ ...msg, id: Date.now().toString() });
  });
};

const onWsError = (error: string) => {
  console.error('WebSocket error:', error);
  isConnected.value = false;
  alert('Connection failed: ' + error);
};

// 发送消息（使用工具模块的 publish）
const sendGroupMessage = () => {
  if (!groupInput.value.trim()) return;
  publish('/app/chat', { content: groupInput.value.trim() });
  groupInput.value = '';
};

const sendPrivateMessage = () => {
  if (!privateRecipient.value.trim() || !privateInput.value.trim()) {
    alert('Please enter both recipient and message');
    return;
  }
  publish('/app/private', {
    recipient: privateRecipient.value.trim(),
    content: privateInput.value.trim(),
  });
  privateInput.value = '';
};

// 生命周期
onMounted(() => {
  connect(onWsConnect, onWsError);
  // 可选：定期同步状态（或改用 Pinia 管理状态）
  const interval = setInterval(updateConnectionStatus, 1000);
  onUnmounted(() => clearInterval(interval));
});

onUnmounted(() => {
  disconnect();
});
</script>

<style scoped>
</style>
