import SockJS from 'sockjs-client';
import Stomp from 'stompjs';

class WebSocketService {
  private stompClient: any = null;
  private connected: boolean = false;
  private subscriptions: Map<string, any> = new Map();
  private url: string = '';

  connect(url: string): Promise<void> {
    return new Promise((resolve, reject) => {
      if (this.connected && this.url === url) {
        resolve();
        return;
      }

      this.url = url;
      const socket = new SockJS(url);
      this.stompClient = Stomp.over(socket);
      
      this.stompClient.connect(
        {},
        () => {
          this.connected = true;
          console.log('WebSocket connected');
          resolve();
        },
        (error: any) => {
          console.error('WebSocket connection error:', error);
          this.connected = false;
          reject(error);
        }
      );
    });
  }

  subscribe(destination: string, callback: (message: any) => void): string {
    if (!this.connected || !this.stompClient) {
      console.error('WebSocket not connected');
      return '';
    }

    console.log('订阅目的地:', destination);
    const subscription = this.stompClient.subscribe(destination, (message: any) => {
      console.log('收到 WebSocket 原始消息:', message);
      console.log('消息 body:', message.body);
      try {
        const body = JSON.parse(message.body);
        console.log('解析后的消息:', body);
        callback(body);
      } catch (e) {
        console.error('解析消息失败:', e, '原始 body:', message.body);
        callback(message.body);
      }
    });

    const subscriptionId = destination + '_' + Date.now();
    this.subscriptions.set(subscriptionId, subscription);
    console.log('订阅成功，subscriptionId:', subscriptionId);
    return subscriptionId;
  }

  unsubscribe(subscriptionId: string): void {
    const subscription = this.subscriptions.get(subscriptionId);
    if (subscription) {
      subscription.unsubscribe();
      this.subscriptions.delete(subscriptionId);
    }
  }

  send(destination: string, body: any): void {
    if (!this.connected || !this.stompClient) {
      console.error('WebSocket not connected');
      return;
    }
    this.stompClient.send(destination, {}, JSON.stringify(body));
  }

  disconnect(): void {
    if (this.stompClient && this.connected) {
      this.subscriptions.forEach((subscription) => subscription.unsubscribe());
      this.subscriptions.clear();
      this.stompClient.disconnect(() => {
        console.log('WebSocket disconnected');
      });
      this.connected = false;
      this.stompClient = null;
    }
  }

  isConnected(): boolean {
    return this.connected;
  }
}

export const websocketService = new WebSocketService();
