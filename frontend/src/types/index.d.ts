declare module 'sockjs-client' {
  class SockJS {
    constructor(url: string, _reserved?: any, options?: any);
    protocol: string;
    readyState: number;
    send(data: string): void;
    close(): void;
    onopen: (() => void) | null;
    onmessage: ((e: { data: string }) => void) | null;
    onerror: ((e: any) => void) | null;
    onclose: (() => void) | null;
  }
  export = SockJS;
}

declare module 'stompjs' {
  interface Client {
    connect(headers: any, connectCallback: () => void, errorCallback?: (error: any) => void): void;
    disconnect(disconnectCallback: () => void): void;
    send(destination: string, headers?: any, body?: string): void;
    subscribe(destination: string, callback: (message: any) => void, headers?: any): any;
    unsubscribe(id: string): void;
    begin(transaction: string): void;
    commit(transaction: string): void;
    abort(transaction: string): void;
    ack(messageID: string, subscription: string, headers?: any): void;
  }

  export function over(ws: any): Client;
  export function client(url: string, protocols?: string | string[]): Client;
}
