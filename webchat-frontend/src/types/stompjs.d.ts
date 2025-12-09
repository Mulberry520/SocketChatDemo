declare module 'stompjs' {
  export interface Frame {
    command: string;
    headers: Record<string, string>;
    body: string;
  }

  export interface Client {
    connect(headers: Record<string, any>, connectCallback: () => void, errorCallback?: (error: any) => void): void;
    disconnect(disconnectCallback?: () => void): void;
    subscribe(destination: string, callback: (frame: Frame) => void, headers?: Record<string, any>): { unsubscribe(): void };
    send(destination: string, headers?: Record<string, any>, body?: string): void;
  }

  export function over(ws: WebSocket | SockJS): Client;
}
