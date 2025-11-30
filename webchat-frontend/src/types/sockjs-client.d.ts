declare module 'sockjs-client' {
  class SockJS extends EventTarget {
    constructor(url: string, _reserved?: any, options?: any);
    close(): void;
    send(data: string): void;
    onopen: ((ev: Event) => any) | null;
    onmessage: ((ev: MessageEvent) => any) | null;
    onclose: ((ev: CloseEvent) => any) | null;
    onerror: ((ev: Event) => any) | null;
    readyState: number;
    static readonly CONNECTING: number;
    static readonly OPEN: number;
    static readonly CLOSING: number;
    static readonly CLOSED: number;
  }

  export = SockJS;
}
