import { Client, Stomp } from '@stomp/stompjs'
import type { IFrame, IMessage } from '@stomp/stompjs'
import type { ChatMessage } from '@/types/room.ts'

// --- 类型定义 ---
export interface WebSocketConfig {
  brokerURL: string; // 例如 '/ws' 或 'http://localhost:8080/ws'
  reconnectDelay?: number; // 重连延迟，默认 5000ms
  heartbeatIncoming?: number; // 心跳检测 (ms)
  heartbeatOutgoing?: number;
  debug?: boolean;
  getToken: () => string | null;
}

// --- 状态管理 ---
let stompClient: Client | null = null;
let isConnected = false;
let isConnecting = false;
let config: WebSocketConfig | null = null;

// --- 回调函数 (供外部注册) ---
let onConnectedCallback: ((frame: IFrame) => void) | null = null;
let onDisconnectedCallback: (() => void) | null = null;
let onErrorCallback: ((frame: IFrame) => void) | null = null;
let onMessageReceivedCallback: ((message: ChatMessage) => void) | null = null; // 接收单个消息

// --- 内部辅助函数 ---
const logDebug = (...args: any[]) => {
  if (config?.debug) {
    console.log('[WebSocket Util]', ...args);
  }
};

const logError = (...args: any[]) => {
  console.error('[WebSocket Util]', ...args);
};

// --- 核心功能函数 ---

/**
 * 初始化配置
 * @param cfg 配置对象
 */
export const initWebSocket = (cfg: WebSocketConfig) => {
  config = { ...cfg }; // 浅拷贝配置
  logDebug('Initialized with config:', config);
};

/**
 * 连接到 WebSocket 服务器
 * @returns Promise<void>
 */
export const connectWebSocket = (): Promise<void> => {
  return new Promise((resolve, reject) => {
    if (!config) {
      const errorMsg: string = 'WebSocket 未初始化，请先调用 initWebSocket(config)';
      logError(errorMsg);
      reject(new Error(errorMsg));
      return;
    }

    if (isConnecting) {
      logDebug('Connection attempt already in progress.');
      resolve();
      return;
    }

    if (isConnected) {
      logDebug('Already connected.');
      resolve();
      return;
    }

    isConnecting = true;
    logDebug('Attempting to connect...');

    try {
      stompClient = new Client({
        brokerURL: config.brokerURL,
        connectHeaders: {
          Authorization: `Bearer ${config.getToken()}`,
        },
        reconnectDelay: config.reconnectDelay ?? 5000,
        heartbeatIncoming: config.heartbeatIncoming ?? 0,
        heartbeatOutgoing: config.heartbeatOutgoing ?? 0,
        onConnect: (frame: IFrame) => {
          logDebug('Connected:', frame);
          isConnected = true;
          isConnecting = false;

          // 注册全局消息监听器
          if (stompClient) {
            try {
              const subscription = stompClient.subscribe('/user/queue/room-message', (message: IMessage) => {
                logDebug('Message received on /user/queue/room-message:', message);
                try {
                  const parsedBody: ChatMessage = JSON.parse(message.body);
                  onMessageReceivedCallback?.(parsedBody);
                } catch (e) {
                  logError('Failed to parse incoming message:', e, message.body);
                }
              });
              logDebug('Subscribed to /user/queue/room-message');
              // 可以选择存储 subscription 以便 later unsubscribe
            } catch (subscribeError: any) {
              logError('Failed to subscribe after connect:', subscribeError);
              // 可能需要断开连接或触发错误回调
              onErrorCallback?.({ headers: {}, body: `Subscription failed: ${subscribeError.message}`, command: 'ERROR' } as IFrame);
              return; // 阻止 resolve
            }
          }
          onConnectedCallback?.(frame);
          resolve();
        },
        onDisconnect: (frame: IFrame) => {
          logDebug('Disconnected:', frame);
          isConnected = false;
          isConnecting = false;
          stompClient = null;
          onDisconnectedCallback?.();
        },
        onStompError: (frame: IFrame) => {
          logError('STOMP Error/Broker reported error:', frame.headers['message'], frame.body);
          isConnecting = false; // 确保连接中状态被清除
          onErrorCallback?.(frame);
          // reject(new Error(frame.headers['message'] || 'STOMP Error'));
        },
        onWebSocketError: (event: Event) => {
          logError('WebSocket Error:', event);
          if (isConnecting) {
            isConnecting = false;
          }
        },
        onWebSocketClose: (event: CloseEvent) => {
          logDebug('WebSocket closed:', event.code, event.reason);
        }
      })

      if (config.debug) {
        stompClient.debug = (str) => console.log('[STOMP Debug]', str);
      }

      stompClient.activate();
    } catch (error) {
      isConnecting = false;
      logError('Connection failed:', error);
      reject(error);
    }
  });
};

/**
 * 断开 WebSocket 连接
 * @returns Promise<void>
 */
export const disconnectWebSocket = (): Promise<void> => {
  return new Promise((resolve) => {
    if (stompClient && isConnected) {
      logDebug('Attempting to disconnect...');
      try {
        stompClient.deactivate(); // 优雅地断开
      } catch (deactivateError) {
        logError('Error during deactivation:', deactivateError);
      }
      resolve();
    } else {
      logDebug('Not connected or client is null, nothing to disconnect.');
      resolve(); // 即使没连接，也算成功断开
    }
  });
};

/**
 * 加入指定房间
 * @param roomName 房间名称
 * @returns Promise<void>
 */
export const joinRoom = (roomName: string): Promise<void> => {
  return new Promise((resolve, reject) => {
    if (!isConnected || !stompClient) {
      const errorMsg = 'Cannot join room: Not connected to WebSocket.';
      logError(errorMsg);
      reject(new Error(errorMsg));
      return;
    }

    const destination = `/app/room/${roomName}/connect`;
    try {
      stompClient.publish({ destination, body: '{}' });
      logDebug(`Sent JOIN request to ${destination}`);
      resolve();
    } catch (error) {
      logError(`Failed to send JOIN request to ${destination}:`, error);
      reject(error);
    }
  });
};

/**
 * 离开指定房间
 * @param roomName 房间名称
 * @returns Promise<void>
 */
export const leaveRoom = (roomName: string): Promise<void> => {
  return new Promise((resolve, reject) => {
    if (!isConnected || !stompClient) {
      const errorMsg = 'Cannot leave room: Not connected to WebSocket.';
      logError(errorMsg);
      reject(new Error(errorMsg));
      return;
    }

    const destination = `/app/room/${roomName}/disconnect`;
    try {
      stompClient.publish({ destination, body: '{}' });
      logDebug(`Sent QUIT request to ${destination}`);
      resolve();
    } catch (error) {
      logError(`Failed to send QUIT request to ${destination}:`, error);
      reject(error);
    }
  });
};

/**
 * 向指定房间发送聊天消息
 * @param roomName 房间名称
 * @param content 消息内容
 * @returns Promise<void>
 */
export const sendChatMessage = (roomName: string, content: string): Promise<void> => {
  return new Promise((resolve, reject) => {
    if (!isConnected || !stompClient) {
      const errorMsg = 'Cannot send message: Not connected to WebSocket.';
      logError(errorMsg);
      reject(new Error(errorMsg));
      return;
    }

    const destination = `/app/room/${roomName}/chat`;
    const payload = JSON.stringify({ content });

    try {
      stompClient.publish({ destination, body: payload });
      logDebug(`Sent chat message to ${destination}: ${content}`);
      resolve();
    } catch (error) {
      logError(`Failed to send message to ${destination}:`, error);
      reject(error);
    }
  });
};

// --- 状态查询函数 ---
export const isWebSocketConnected = () => isConnected;

// --- 回调注册函数 ---
export const setOnConnectedCallback = (callback: (frame: IFrame) => void) => {
  onConnectedCallback = callback;
};

export const setOnDisconnectedCallback = (callback: () => void) => {
  onDisconnectedCallback = callback;
};

export const setOnErrorCallback = (callback: (frame: IFrame) => void) => {
  onErrorCallback = callback;
};

export const setOnMessageReceivedCallback = (callback: (message: ChatMessage) => void) => {
  onMessageReceivedCallback = callback;
};

// --- 清理函数 (可选，用于应用关闭时) ---
export const cleanup = () => {
  if (isConnected && stompClient) {
    stompClient.deactivate();
  }
  stompClient = null;
  isConnected = false;
  isConnecting = false;
  config = null;
  onConnectedCallback = null;
  onDisconnectedCallback = null;
  onErrorCallback = null;
  onMessageReceivedCallback = null;
  logDebug('WebSocket util cleaned up.');
};
