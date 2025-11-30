import { Client } from '@stomp/stompjs';
import type { Message, IFrame } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

// 注意：SockJS 使用 HTTP URL，不是 ws://
const WEBSOCKET_URL = 'http://localhost:8080/ws';

// 全局 STOMP 客户端实例
let stompClient: Client | null = null;

// 连接状态
let isConnected = false;
let reconnectAttempts = 0;
const MAX_RECONNECT_ATTEMPTS = 5;
const RECONNECT_DELAY = 5000;

// 回调函数类型
type MessageCallback = (payload: any) => void;
type ErrorCallback = (error: string) => void;

// 存储订阅回调（用于取消订阅）
const subscriptions = new Map<string, () => void>();

/**
 * 创建并激活 STOMP 客户端（使用 SockJS）
 */
function createStompClient(onConnect: () => void, onError: ErrorCallback): Client {
  const token = localStorage.getItem('accessToken');
  if (!token) {
    throw new Error('No access token found in localStorage');
  }

  const client = new Client({
    webSocketFactory: () => {
      return new SockJS(`${WEBSOCKET_URL}?token=${encodeURIComponent(token)}`);
    },
    debug: (str) => {
      console.log('[WebSocket Debug]', str);
    },
    onConnect: (frame: IFrame) => {
      console.log('WebSocket connected successfully', frame);
      isConnected = true;
      reconnectAttempts = 0;
      onConnect();
    },
    onStompError: (frame: IFrame) => {
      console.error('STOMP protocol error:', frame.headers.message || 'Unknown STOMP error');
      onError(frame.headers.message || 'STOMP error occurred');
    },
    onWebSocketClose: () => {
      console.warn('WebSocket connection closed');
      handleDisconnect();
      attemptReconnect(onConnect, onError);
    },
    onWebSocketError: (error: Event) => {
      console.error('WebSocket transport error:', error);
      onError('WebSocket transport error');
    },
    reconnectDelay: RECONNECT_DELAY,
  });

  return client;
}

/**
 * 处理断开连接
 */
function handleDisconnect() {
  isConnected = false;
  subscriptions.forEach((unsubscribe) => unsubscribe());
  subscriptions.clear();
}

/**
 * 尝试重连
 */
function attemptReconnect(onConnect: () => void, onError: ErrorCallback) {
  if (reconnectAttempts >= MAX_RECONNECT_ATTEMPTS) {
    console.error('Max reconnection attempts reached. Giving up.');
    return;
  }

  reconnectAttempts++;
  console.log(`Attempting to reconnect... (${reconnectAttempts}/${MAX_RECONNECT_ATTEMPTS})`);

  setTimeout(() => {
    if (!isConnected) {
      connect(onConnect, onError);
    }
  }, RECONNECT_DELAY);
}

/**
 * 初始化连接
 */
export function connect(
  onConnect: () => void,
  onError: ErrorCallback = (err) => console.error('WebSocket error:', err)
): void {
  if (isConnected || stompClient) {
    console.log("Reconnected")
    disconnect()
  }

  try {
    stompClient = createStompClient(onConnect, onError);
    stompClient.activate();
  } catch (error) {
    console.error('Failed to create WebSocket client:', error);
    onError('Failed to initialize WebSocket connection');
  }
}

/**
 * 断开连接
 */
export function disconnect(): void {
  if (stompClient) {
    stompClient.deactivate().then(() => {
      console.log('WebSocket disconnected gracefully');
    });
    stompClient = null;
    handleDisconnect();
  }
}

/**
 * 订阅消息
 * @param destination - 如 '/topic/message' 或 '/user/queue/private'
 * @param callback - 收到消息时的回调
 * @returns 取消订阅函数
 */
export function subscribe(destination: string, callback: MessageCallback): () => void {
  if (!stompClient || !isConnected) {
    console.warn(`Cannot subscribe to ${destination}: not connected`);
    return () => {};
  }

  const subscription = stompClient.subscribe(destination, (message: Message) => {
    try {
      const payload = JSON.parse(message.body);
      callback(payload);
    } catch (e) {
      console.error('Failed to parse message body:', message.body, e);
    }
  });

  const unsubscribe = () => {
    subscription.unsubscribe();
    subscriptions.delete(destination);
  };

  subscriptions.set(destination, unsubscribe);
  return unsubscribe;
}

/**
 * 发送消息
 * @param destination - 如 '/app/chat' 或 '/app/private'
 * @param body - 消息体（会自动 JSON.stringify）
 */
export function publish(destination: string, body: Record<string, any>): void {
  if (!stompClient || !isConnected) {
    console.error(`Cannot publish to ${destination}: not connected`);
    return;
  }

  stompClient.publish({
    destination,
    body: JSON.stringify(body),
  });
}

/**
 * 检查是否已连接
 */
export function isConnectedToWebSocket(): boolean {
  return isConnected;
}
