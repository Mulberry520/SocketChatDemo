<template>
  <div class="container">
    <!-- 连接信息 -->
    <div class="connection-info">
      <span>连接状态: </span>
      <span :class="{ connected: connected, disconnected: !connected }">
        {{ connected ? '🟢 已连接' : '🔴 未连接' }}
      </span>
      <button v-if="!connected" @click="connect">连接</button>
      <button v-else @click="disconnect">断开</button>
    </div>

    <!-- 房间信息 -->
    <div class="room-info">
      <label for="roomInput">房间名:</label>
      <input id="roomInput" v-model="currentRoom" placeholder="输入房间名" />
      <button @click="joinRoom" :disabled="!connected || !currentRoom.trim()">加入房间</button>
    </div>

    <!-- 消息发送区 -->
    <div class="send-area">
      <textarea v-model="messageToSend" placeholder="输入消息..." rows="3"></textarea>
      <br />
      <button @click="sendMessage" :disabled="!connected || !currentRoom.trim() || !messageToSend.trim()">
        发送消息
      </button>
    </div>

    <!-- 消息显示区 -->
    <div class="messages-container">
      <h3>消息记录:</h3>
      <div ref="messagesContainerRef" class="messages">
        <div v-for="(msg, index) in messages" :key="index" class="message-item">
          <strong>[{{ msg.timestamp }}]</strong>
          <span v-if="msg.type === 'info'" class="info">{{ msg.content }}</span>
          <span v-else-if="msg.type === 'join'" class="join">👤 {{ msg.sender }} 加入了房间 [{{ msg.room }}]</span>
          <span v-else-if="msg.type === 'quit'" class="quit">🚪 {{ msg.sender }} 离开了房间 [{{ msg.room }}]</span>
          <span v-else-if="msg.type === 'message'" class="message">
            <strong>{{ msg.sender }}:</strong> {{ msg.content }}
          </span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { Ref } from 'vue';
import { ref, onMounted, onUnmounted, nextTick } from 'vue';
import type { IFrame, IMessage } from '@stomp/stompjs';
import { Client } from '@stomp/stompjs';

interface MessageItem {
  type: 'info' | 'join' | 'quit' | 'message';
  content: string;
  sender?: string;
  room?: string;
  timestamp: string;
}

// --- Reactive State ---
const connected = ref(false);
const currentRoom = ref('');
const messageToSend = ref('');
const messages: Ref<MessageItem[]> = ref([]);
const messagesContainerRef: Ref<HTMLElement | null> = ref(null);

// --- STOMP Client Instance ---
let clientInstance: Client | null = null;
let currentSubscription: { unsubscribe: () => void } | null = null; // 存储订阅对象，方便取消

// --- Utility Functions ---
const addMessage = (item: Omit<MessageItem, 'timestamp'>) => {
  const now = new Date();
  const timestamp = `${now.getHours().toString().padStart(2, '0')}:${now.getMinutes().toString().padStart(2, '0')}:${now.getSeconds().toString().padStart(2, '0')}`;
  console.log("📬 [UI] Adding message to list:", { ...item, timestamp }); // Debug log
  messages.value.push({ ...item, timestamp });
  nextTick(() => {
    if (messagesContainerRef.value) {
      messagesContainerRef.value.scrollTop = messagesContainerRef.value.scrollHeight;
    }
  });
};

const logInfo = (msg: string) => {
  console.log(`[INFO] ${msg}`);
  addMessage({ type: 'info', content: msg });
};

const logError = (msg: string) => {
  console.error(`[ERROR] ${msg}`);
  addMessage({ type: 'info', content: `❌ ${msg}` });
};

// --- STOMP Callbacks ---
const onConnect = (frame: IFrame) => {
  console.log("🔵 [STOMP] onConnect callback triggered!", frame);
  connected.value = true;
  logInfo(`✅ STOMP 连接成功！会话ID: ${frame.headers['session'] || 'N/A'}`);

  // 关键：确保 clientInstance 有效后再进行订阅
  if (!clientInstance) {
    const errorMsg = "❌ [STOMP] clientInstance is unexpectedly null in onConnect!";
    console.error(errorMsg);
    logError(errorMsg.substring(6)); // Remove emoji for message list
    return;
  }

  // 如果已经订阅了某个房间，则重新订阅
  if (currentRoom.value) {
    performJoinRoom(currentRoom.value);
  }
};

const onError = (frame: IFrame) => {
  console.error("🔴 [STOMP] Connection error:", frame);
  connected.value = false;
  logError(`STOMP 连接错误: ${frame.headers.message || frame.body || 'Unknown error'}`);
};

const onDisconnect = (frame: IFrame) => {
  console.log("🟡 [STOMP] Disconnected", frame);
  connected.value = false;
  if (currentSubscription) {
    try {
      currentSubscription.unsubscribe();
      console.log("📭 [STOMP] Previous subscription unsubscribed on disconnect.");
    } catch (e) {
      console.warn("[STOMP] Warning during unsubscribe on disconnect:", e);
    }
    currentSubscription = null;
  }
  logInfo("🟡 STOMP 连接已断开");
};

const onWebSocketClose = (event: CloseEvent) => {
  console.log("🔌 [WS] WebSocket closed", event);
  connected.value = false;
  if (currentSubscription) {
    try {
      currentSubscription.unsubscribe();
      console.log("📭 [STOMP] Previous subscription unsubscribed on WS close.");
    } catch (e) {
      console.warn("[STOMP] Warning during unsubscribe on WS close:", e);
    }
    currentSubscription = null;
  }
  // 可以根据 close code 决定是否重连
  logInfo(`🔌 WebSocket 已关闭 (Code: ${event.code})`);
};

// --- Connection Logic ---
const connect = () => {
  if (connected.value) {
    logInfo("⚠️ 已经处于连接状态");
    return;
  }

  try {
    // 清理旧实例（如果有的话）
    if (clientInstance) {
      disconnect(); // 先断开旧连接
    }

    clientInstance = new Client({
      brokerURL: 'ws://localhost:8080/ws',
      connectHeaders: {
        Authorization: `Bearer ${localStorage.getItem('accessToken')}`
      },
      onConnect: onConnect,
      onStompError: onError,
      onDisconnect: onDisconnect,

    });


    clientInstance.activate();
    console.log("🚀 [STOMP] Attempting to activate client...");
    logInfo("🚀 正在尝试连接到服务器...");
  } catch (error) {
    const errorMsg = `连接失败: ${(error as Error).message}`;
    console.error("💥 [STOMP] Failed to create or activate client:", error);
    logError(errorMsg);
  }
};

const disconnect = () => {
  if (!connected.value) {
    logInfo("⚠️ 当前未连接");
    return;
  }

  try {
    if (currentSubscription) {
      currentSubscription.unsubscribe();
      console.log("📭 [STOMP] Unsubscribed before disconnecting.");
      currentSubscription = null;
    }
    if (clientInstance) {
      clientInstance.deactivate(); // deactivate 通常比 forceDisconnect 更优雅
      console.log("🛑 [STOMP] Client deactivated.");
      logInfo("🛑 正在断开连接...");
    }
  } catch (error) {
    console.error("💥 [STOMP] Error during disconnect:", error);
    logError(`断开连接时出错: ${(error as Error).message}`);
  }
};

// --- Room Logic ---
const joinRoom = () => {
  if (!connected.value) {
    logError("请先连接服务器");
    return;
  }
  if (!currentRoom.value.trim()) {
    logError("请输入房间名");
    return;
  }
  performJoinRoom(currentRoom.value.trim());
};

const performJoinRoom = (roomName: string) => {
  if (!clientInstance || !connected.value) {
    logError("无法加入房间：客户端未连接");
    return;
  }

  // 如果已有订阅，先取消
  if (currentSubscription) {
    try {
      currentSubscription.unsubscribe();
      console.log("📭 [STOMP] Previous subscription unsubscribed before joining new room.");
    } catch (e) {
      console.warn("[STOMP] Warning during unsubscribe before joining:", e);
    }
    currentSubscription = null;
  }

  const destination = `/app/room/${roomName}/connect`;
  try {
    clientInstance.publish({ destination, body: '{}' }); // 发送空JSON或根据后端需求调整
    console.log(`📤 [STOMP] Sent JOIN request to ${destination}`);
    logInfo(`📤 正在加入房间 [${roomName}]...`);

    // 🔥 使用内联回调函数确保绑定，并捕获返回值
    const sub = clientInstance.subscribe('/user/queue/room-message', (message: IMessage) => {
      console.log("📬 [STOMP] Message received from subscription:", message);
      console.log("📬 [STOMP] Raw message body:", message.body);

      // ✅ 强制性输出，确保这个函数被调用
      console.assert(true, "💥 [DEBUG] onMessageReceived WAS CALLED! This line should appear in console.");

      try {
        const parsedBody = JSON.parse(message.body);
        console.log("📦 [STOMP] Parsed message body:", parsedBody);

        const room = parsedBody.recipient || 'unknown_room';
        const sender = parsedBody.sender || 'System';
        const timeStr = parsedBody.time ? new Date(parsedBody.time).toLocaleTimeString() : new Date().toLocaleTimeString();

        switch (parsedBody.type) {
          case 'JOIN':
            addMessage({ type: 'join', sender, room, content: '' });
            break;
          case 'QUIT':
            addMessage({ type: 'quit', sender, room, content: '' });
            break;
          case 'MESSAGE':
            addMessage({ type: 'message', sender, content: parsedBody.content, timestamp: timeStr });
            break;
          default:
            logInfo(`📩 收到未知类型消息: ${parsedBody.type}`);
            console.warn("❓ [STOMP] Received unknown message type:", parsedBody);
        }
      } catch (e) {
        const errorMsg = `消息解析失败: ${(e as Error).message}`;
        console.error("❌ [STOMP] Error parsing received message:", e, message.body);
        logError(errorMsg);
      }
    });

    // ✅ 检查订阅对象是否有效
    if (!sub) {
      console.error("❌ [STOMP] subscribe returned null or undefined!");
      logError("❌ 订阅失败，请检查网络连接或服务器配置。");
      return;
    }

    // ✅ 将订阅对象存储起来
    currentSubscription = sub;

    // ✅ 打印成功订阅的信息
    console.log("📬 [STOMP] Successfully subscribed to /user/queue/room-message. Subscription object:", sub);

  } catch (error) {
    const errorMsg = `加入房间 [${roomName}] 失败: ${(error as Error).message}`;
    console.error("💥 [STOMP] Failed to join room or subscribe:", error);
    logError(errorMsg);
  }
};

const sendMessage = () => {
  if (!connected.value) {
    logError("请先连接服务器");
    return;
  }
  if (!currentRoom.value.trim()) {
    logError("请先加入一个房间");
    return;
  }
  if (!messageToSend.value.trim()) {
    logError("消息不能为空");
    return;
  }

  const destination = `/app/room/${currentRoom.value.trim()}/chat`;
  const payload = JSON.stringify({ content: messageToSend.value.trim() });

  try {
    clientInstance?.publish({ destination, body: payload });
    console.log(`📤 [STOMP] Sent chat message to ${destination}: ${payload}`);
    // 注意：这里不再本地添加消息，而是等待后端广播回来
    messageToSend.value = ''; // 清空输入框
  } catch (error) {
    const errorMsg = `发送消息失败: ${(error as Error).message}`;
    console.error("💥 [STOMP] Failed to send message:", error);
    logError(errorMsg);
  }
};

// --- Lifecycle Hooks ---
onMounted(() => {
  console.log("🏠 [Component] WebsocketView mounted.");
  connect();
});

onUnmounted(() => {
  console.log("🧨 [Component] WebsocketView unmounted. Cleaning up...");
  disconnect(); // 确保组件卸载时清理资源
});
</script>

<style scoped>
.container {
  max-width: 800px;
  margin: 20px auto;
  padding: 20px;
  border: 1px solid #ccc;
  border-radius: 5px;
  font-family: Arial, sans-serif;
}

.connection-info, .room-info, .send-area {
  margin-bottom: 15px;
}

.connected {
  color: green;
  font-weight: bold;
}

.disconnected {
  color: red;
  font-weight: bold;
}

.messages-container {
  border-top: 1px solid #eee;
  padding-top: 15px;
}

.messages {
  height: 300px;
  overflow-y: auto;
  border: 1px solid #ddd;
  padding: 10px;
  background-color: #f9f9f9;
  white-space: pre-wrap; /* Preserve line breaks */
}

.message-item {
  margin-bottom: 5px;
  padding: 2px 0;
}

.info { color: blue; }
.join { color: green; }
.quit { color: orange; }
.message { color: black; }

button {
  margin-left: 10px;
  padding: 5px 10px;
  cursor: pointer;
}

button:disabled {
  cursor: not-allowed;
  opacity: 0.6;
}

textarea {
  width: 100%;
  box-sizing: border-box;
}
</style>
