class StreamWebSocket {
  private ws: WebSocket | null = null;
  private url: string = '';
  private connected: boolean = false;
  private onMessageCallback: ((message: string) => void) | null = null;

  connect(url: string): Promise<void> {
    return new Promise((resolve, reject) => {
      if (this.connected && this.url === url) {
        resolve();
        return;
      }

      this.url = url;
      this.ws = new WebSocket(url);
      this.ws.binaryType = 'arraybuffer';

      this.ws.onopen = () => {
        this.connected = true;
        console.log('Stream WebSocket connected');
        resolve();
      };

      this.ws.onerror = (error) => {
        console.error('Stream WebSocket error:', error);
        if (!this.connected) {
          reject(error);
        }
      };

      this.ws.onclose = () => {
        this.connected = false;
        console.log('Stream WebSocket disconnected');
      };

      this.ws.onmessage = (event) => {
        if (this.onMessageCallback) {
          this.onMessageCallback(event.data);
        }
      };
    });
  }

  sendBinary(data: ArrayBuffer | Blob): void {
    if (!this.connected || !this.ws) {
      console.error('Stream WebSocket not connected');
      return;
    }
    if (this.ws.readyState === WebSocket.OPEN) {
      this.ws.send(data);
    }
  }

  sendText(message: string): void {
    if (!this.connected || !this.ws) {
      console.error('Stream WebSocket not connected');
      return;
    }
    if (this.ws.readyState === WebSocket.OPEN) {
      this.ws.send(message);
    }
  }

  onMessage(callback: (message: string) => void): void {
    this.onMessageCallback = callback;
  }

  disconnect(): void {
    if (this.ws) {
      this.ws.close();
      this.ws = null;
      this.connected = false;
    }
  }

  isConnected(): boolean {
    return this.connected;
  }
}

export const streamWebSocket = new StreamWebSocket();