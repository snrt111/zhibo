<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed, nextTick } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { message } from 'ant-design-vue';
import { liveApi } from '../../api/live';
import { danmakuApi } from '../../api/danmaku';
import { streamWebSocket } from '../../utils/streamWebSocket';
import { websocketService } from '../../utils/websocket';
import { handleResponseAsync, errorHandler } from '../../utils/errorHandler';
import GiftIcon from '../GiftIcon.vue';

const route = useRoute();
const router = useRouter();
const liveId = computed(() => parseInt(route.params.id as string) || 0);
const loading = ref(false);
const liveDetail = ref<any>(null);
const danmakuList = ref<any[]>([]);
const danmakuListRef = ref<HTMLElement | null>(null);
const isPushing = ref(false);
const localVideoRef = ref<HTMLVideoElement | null>(null);
const mediaStream = ref<MediaStream | null>(null);
const mediaRecorder = ref<MediaRecorder | null>(null);
const selectedDevice = ref<'camera' | 'screen'>('camera');
const videoDevices = ref<MediaDeviceInfo[]>([]);
const audioDevices = ref<MediaDeviceInfo[]>([]);
const selectedVideoDevice = ref('');
const selectedAudioDevice = ref('');
const isPreviewing = ref(false);
const showDanmaku = ref(true);
const danmakuSubscriptionId = ref('');
const showGiftEffect = ref(false);
const currentGiftEffect = ref<any>(null);

const getLiveDetail = async () => {
  if (!liveId.value) {
    message.error('直播间ID无效');
    return;
  }
  loading.value = true;
  try {
    const response = await liveApi.getLiveDetail(liveId.value);
    await handleResponseAsync(response, async (data) => {
      liveDetail.value = data;
      if (liveDetail.value.status === 1) {
        await getMediaDevices();
      }
    });
  } catch (error) {
    errorHandler.handle(error);
  } finally {
    loading.value = false;
  }
};

const getDanmakuList = async () => {
  if (!liveId.value) return;
  try {
    const response = await danmakuApi.getDanmakuList(liveId.value);
    await handleResponseAsync(response, (data) => {
      danmakuList.value = (data || []).reverse();
      scrollToBottom();
    });
  } catch (error) {
    errorHandler.handle(error, false);
  }
};

const scrollToBottom = () => {
  nextTick(() => {
    if (danmakuListRef.value) {
      danmakuListRef.value.scrollTop = danmakuListRef.value.scrollHeight;
    }
  });
};

const initDanmakuWebSocket = async () => {
  try {
    const wsUrl = window.location.protocol === 'https:'
      ? `https://${window.location.host}/ws`
      : `http://${window.location.host}/ws`;
    console.log('主播端开始连接 WebSocket:', wsUrl);
    await websocketService.connect(wsUrl);
    console.log('主播端 WebSocket 连接成功，开始订阅:', `/topic/live/${liveId.value}`);
    
    danmakuSubscriptionId.value = websocketService.subscribe(
      `/topic/live/${liveId.value}`,
      handleDanmakuMessage
    );
    console.log('主播端订阅成功，subscriptionId:', danmakuSubscriptionId.value);
  } catch (error) {
    console.error('主播端 WebSocket 连接错误:', error);
  }
};

const handleDanmakuMessage = (message: any) => {
  console.log('主播端收到 WebSocket 消息:', message);
  if (message.type === 'danmaku') {
    danmakuList.value.push(message);
    if (danmakuList.value.length > 100) {
      danmakuList.value.shift();
    }
    scrollToBottom();
  } else if (message.type === 'gift') {
    showGiftAnimation(message);
  }
};

const showGiftAnimation = (giftData: any) => {
  currentGiftEffect.value = giftData;
  showGiftEffect.value = true;
  setTimeout(() => {
    showGiftEffect.value = false;
  }, 3500);
};

const getMediaDevices = async () => {
  try {
    // 先请求权限，这样设备才会有 label
    const tempStream = await navigator.mediaDevices.getUserMedia({ video: true, audio: true });
    tempStream.getTracks().forEach(track => track.stop());
    
    const devices = await navigator.mediaDevices.enumerateDevices();
    videoDevices.value = devices.filter(d => d.kind === 'videoinput');
    audioDevices.value = devices.filter(d => d.kind === 'audioinput');
    if (videoDevices.value.length > 0) {
      selectedVideoDevice.value = videoDevices.value[0]?.deviceId || '';
    }
    if (audioDevices.value.length > 0) {
      selectedAudioDevice.value = audioDevices.value[0]?.deviceId || '';
    }
  } catch (error: any) {
    console.error('获取设备列表失败:', error);
    let errorMsg = '获取设备列表失败';
    if (error.name === 'NotAllowedError') {
      errorMsg = '请允许浏览器访问摄像头和麦克风权限';
    } else if (error.name === 'NotFoundError') {
      errorMsg = '未找到摄像头或麦克风设备';
    }
    message.error(errorMsg);
  }
};

const startPreview = async () => {
  try {
    stopPreview();
    let stream: MediaStream;
    if (selectedDevice.value === 'camera') {
      const constraints: MediaStreamConstraints = {
        audio: selectedAudioDevice.value ? { deviceId: { exact: selectedAudioDevice.value } } : true,
        video: selectedVideoDevice.value ? { deviceId: { exact: selectedVideoDevice.value } } : true
      };
      console.log('获取摄像头设备，约束:', constraints);
      stream = await navigator.mediaDevices.getUserMedia(constraints);
    } else {
      console.log('获取屏幕共享');
      stream = await navigator.mediaDevices.getDisplayMedia({
        video: true,
        audio: true
      });
    }
    mediaStream.value = stream;
    if (localVideoRef.value) {
      localVideoRef.value.srcObject = stream;
    }
    isPreviewing.value = true;
    message.success('预览已开始');
  } catch (error: any) {
    console.error('获取媒体设备失败:', error);
    let errorMsg = '获取媒体设备失败';
    if (error.name === 'NotAllowedError') {
      errorMsg = '请允许浏览器访问摄像头和麦克风权限';
    } else if (error.name === 'NotFoundError') {
      errorMsg = '未找到摄像头或麦克风设备';
    } else if (error.name === 'NotReadableError') {
      errorMsg = '设备被其他程序占用';
    } else if (error.name === 'OverconstrainedError') {
      errorMsg = '所选设备不满足要求，请更换设备';
    }
    message.error(errorMsg);
  }
};

const stopPreview = () => {
  if (mediaStream.value) {
    mediaStream.value.getTracks().forEach(track => track.stop());
    mediaStream.value = null;
  }
  if (localVideoRef.value) {
    localVideoRef.value.srcObject = null;
  }
  isPreviewing.value = false;
};

const startPush = async () => {
  if (!mediaStream.value) {
    await startPreview();
  }
  if (!mediaStream.value) {
    message.error('获取媒体设备失败，请检查权限设置');
    return;
  }
  if (!liveDetail.value?.streamKey) {
    message.error('获取推流密钥失败');
    return;
  }
  try {
    const wsUrl = window.location.protocol === 'https:'
      ? `wss://${window.location.host}/ws/stream`
      : `ws://${window.location.host}/ws/stream`;
    
    await streamWebSocket.connect(wsUrl);
    
    streamWebSocket.onMessage((msg) => {
      if (msg.startsWith('STARTED:')) {
        isPushing.value = true;
        message.success('推流已开始');
      } else if (msg.startsWith('ERROR:')) {
        message.error('推流启动失败');
        stopPush();
      }
    });
    
    streamWebSocket.sendText(`START:${liveDetail.value.streamKey}`);
    
    const recorder = new MediaRecorder(mediaStream.value, {
      mimeType: 'video/webm;codecs=vp8,opus',
      videoBitsPerSecond: 2500000
    });
    recorder.ondataavailable = (event) => {
      if (event.data.size > 0 && isPushing.value) {
        event.data.arrayBuffer().then(buffer => {
          streamWebSocket.sendBinary(buffer);
        });
      }
    };
    recorder.start(1000);
    mediaRecorder.value = recorder;
  } catch (error) {
    console.error('开始推流失败:', error);
    message.error('开始推流失败');
  }
};

const stopPush = () => {
  if (mediaRecorder.value) {
    mediaRecorder.value.stop();
    mediaRecorder.value = null;
  }
  streamWebSocket.sendText('STOP');
  streamWebSocket.disconnect();
  isPushing.value = false;
  message.success('推流已停止');
};

const handleBack = async () => {
  if (isPushing.value) {
    stopPush();
  }
  stopPreview();
  try {
    await liveApi.end(liveId.value);
  } catch (error) {
    console.error('结束直播错误:', error);
  }
  router.push('/anchor');
};

const switchDevice = async (device: 'camera' | 'screen') => {
  if (selectedDevice.value === device) return;
  const wasPushing = isPushing.value;
  const currentRecorder = mediaRecorder.value;
  
  if (currentRecorder) {
    currentRecorder.stop();
    mediaRecorder.value = null;
  }
  isPushing.value = false;
  
  stopPreview();
  selectedDevice.value = device;
  
  await startPreview();
  
  if (wasPushing && mediaStream.value) {
    await startPush();
  }
};

const formatCount = (count: number) => {
  if (count >= 10000) {
    return (count / 10000).toFixed(1) + 'w';
  }
  return count.toString();
};

onMounted(async () => {
  if (!liveId.value) {
    message.error('直播间ID无效，无法进入直播间');
    return;
  }
  await getLiveDetail();
  getDanmakuList();
  initDanmakuWebSocket();
});

onUnmounted(() => {
  if (danmakuSubscriptionId.value) {
    websocketService.unsubscribe(danmakuSubscriptionId.value);
  }
  websocketService.disconnect();
  if (isPushing.value) {
    stopPush();
  }
  stopPreview();
});
</script>

<template>
  <div class="anchor-live-room">
    <div class="live-room-header">
      <button class="back-button" @click="handleBack">
        <span class="back-icon">‹</span>
        返回
      </button>
      <div class="header-title">
        <h2>{{ liveDetail?.title || '直播间' }}</h2>
        <span v-if="liveDetail?.status === 1" class="live-badge">
          <span class="live-dot"></span>
          直播中
        </span>
      </div>
      <div class="header-stats">
        <span class="stat-item">👁 {{ formatCount(liveDetail?.viewCount || 0) }}</span>
        <span class="stat-item">💬 {{ danmakuList.length }}</span>
      </div>
    </div>

    <div class="live-room-content">
      <div class="video-section">
        <div class="video-container">
          <video
            ref="localVideoRef"
            autoplay
            muted
            playsinline
            class="preview-video"
          ></video>
          <div v-if="!mediaStream" class="preview-placeholder">
            <span class="placeholder-icon">📹</span>
            <span>正在加载摄像头...</span>
          </div>
          <div v-if="isPushing" class="pushing-indicator">
            <span class="pulse"></span>
            <span>推流中</span>
          </div>
          <div v-if="showGiftEffect" class="gift-effect">
            <div class="gift-effect-content">
              <GiftIcon :name="currentGiftEffect?.giftName || ''" :icon="currentGiftEffect?.giftImage" size="large" :glow="true" />
              <div class="gift-effect-text">
                <p class="gift-user">{{ currentGiftEffect?.fromUsername }} 送出</p>
                <p class="gift-name">{{ currentGiftEffect?.giftName }} x{{ currentGiftEffect?.giftCount }}</p>
              </div>
            </div>
          </div>
          <div class="video-controls-overlay">
            <div class="control-panel">
              <div class="device-selector">
                <span class="selector-label">视频源：</span>
                <div class="device-tabs">
                  <button
                    :class="['tab-btn', { active: selectedDevice === 'camera' }]"
                    @click="switchDevice('camera')"
                  >
                    📷 摄像头
                  </button>
                  <button
                    :class="['tab-btn', { active: selectedDevice === 'screen' }]"
                    @click="switchDevice('screen')"
                  >
                    🖥️ 屏幕共享
                  </button>
                </div>
              </div>

              <div v-if="selectedDevice === 'camera'" class="device-options">
                <div v-if="videoDevices.length === 0" class="device-notice">
                  <button class="btn btn-get-devices" @click="getMediaDevices">
                    🔍 获取设备列表
                  </button>
                  <span class="notice-text">首次使用需要授权摄像头和麦克风权限</span>
                </div>
                <template v-else>
                  <div class="option-item">
                    <label>摄像头：</label>
                    <select v-model="selectedVideoDevice" @change="isPreviewing && startPreview()">
                      <option v-for="device in videoDevices" :key="device.deviceId" :value="device.deviceId">
                        {{ device.label || '未知设备' }}
                      </option>
                    </select>
                  </div>
                  <div class="option-item">
                    <label>麦克风：</label>
                    <select v-model="selectedAudioDevice" @change="isPreviewing && startPreview()">
                      <option v-for="device in audioDevices" :key="device.deviceId" :value="device.deviceId">
                        {{ device.label || '未知设备' }}
                      </option>
                    </select>
                  </div>
                </template>
              </div>

              <div class="action-buttons">
                <button
                  v-if="!isPushing"
                  class="btn btn-start-push"
                  @click="startPush"
                  :disabled="selectedDevice === 'camera' && videoDevices.length === 0"
                >
                  ▶️ 开始推流
                </button>
                <button
                  v-if="isPushing"
                  class="btn btn-stop-push"
                  @click="stopPush"
                >
                  ⏹️ 停止推流
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="danmaku-section">
        <div class="danmaku-header">
          <span class="danmaku-title">💬 实时弹幕</span>
          <button class="toggle-btn" @click="showDanmaku = !showDanmaku">
            {{ showDanmaku ? '隐藏' : '显示' }}
          </button>
        </div>
        <div v-if="showDanmaku" class="danmaku-list" ref="danmakuListRef">
          <div
            v-for="(danmaku, index) in danmakuList"
            :key="index"
            class="danmaku-item"
          >
            <span class="danmaku-user">{{ danmaku.username || '用户' + danmaku.userId }}:</span>
            <span class="danmaku-content" :style="{ color: danmaku.color || '#fff' }">
              {{ danmaku.content }}
            </span>
          </div>
          <div v-if="danmakuList.length === 0" class="danmaku-empty">
            暂无弹幕
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
* {
  box-sizing: border-box;
}

.anchor-live-room {
  width: 100%;
  min-height: 100vh;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
  color: #fff;
  display: flex;
  flex-direction: column;
}

.live-room-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 24px;
  background: rgba(0, 0, 0, 0.3);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.back-button {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: rgba(255, 255, 255, 0.1);
  border: none;
  border-radius: 8px;
  color: #fff;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s ease;
}

.back-button:hover {
  background: rgba(255, 255, 255, 0.2);
}

.back-icon {
  font-size: 20px;
}

.header-title {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-title h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
}

.live-badge {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 4px 12px;
  background: linear-gradient(135deg, #ff4757 0%, #ff6b81 100%);
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
}

.live-dot {
  width: 8px;
  height: 8px;
  background: #fff;
  border-radius: 50%;
  animation: pulse 1.5s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.header-stats {
  display: flex;
  gap: 16px;
}

.stat-item {
  padding: 6px 12px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 20px;
  font-size: 14px;
}

.live-room-content {
  flex: 1;
  display: flex;
  padding: 24px;
  gap: 24px;
  overflow: hidden;
}

.video-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-width: 0;
}

.video-container {
  position: relative;
  flex: 1;
  background: #000;
  border-radius: 12px;
  overflow: hidden;
  min-height: 400px;
}

.preview-video {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.preview-placeholder {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: rgba(255, 255, 255, 0.5);
  font-size: 16px;
}

.placeholder-icon {
  font-size: 64px;
  opacity: 0.5;
}

.pushing-indicator {
  position: absolute;
  top: 16px;
  right: 16px;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: rgba(255, 71, 87, 0.9);
  border-radius: 20px;
  font-size: 14px;
  font-weight: 600;
}

.pushing-indicator .pulse {
  width: 10px;
  height: 10px;
  background: #fff;
  border-radius: 50%;
  animation: pulse 1s infinite;
}

.gift-effect {
  position: absolute;
  bottom: 100px;
  left: 20px;
  z-index: 100;
  animation: slideIn 0.5s ease-out, fadeOut 0.5s ease-in 3s forwards;
}

.gift-effect-content {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 20px;
  background: linear-gradient(135deg, rgba(255, 215, 0, 0.9), rgba(255, 140, 0, 0.9));
  border-radius: 50px;
  box-shadow: 0 4px 20px rgba(255, 140, 0, 0.4);
}

.gift-effect-content .gift-icon {
  width: 48px;
  height: 48px;
  flex-shrink: 0;
}

.gift-effect-text {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.gift-user {
  margin: 0;
  font-size: 14px;
  font-weight: 600;
  color: #fff;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
}

.gift-effect-text .gift-name {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
  color: #fff;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
}

@keyframes slideIn {
  from {
    transform: translateX(-100%);
    opacity: 0;
  }
  to {
    transform: translateX(0);
    opacity: 1;
  }
}

@keyframes fadeOut {
  from {
    opacity: 1;
  }
  to {
    opacity: 0;
  }
}

.video-controls-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: linear-gradient(transparent, rgba(0, 0, 0, 0.8));
  padding: 20px;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.video-container:hover .video-controls-overlay {
  opacity: 1;
}

.control-panel {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
}

.device-selector {
  display: flex;
  align-items: center;
  gap: 12px;
}

.selector-label {
  font-size: 13px;
  font-weight: 500;
  color: rgba(255, 255, 255, 0.8);
}

.device-tabs {
  display: flex;
  gap: 8px;
}

.tab-btn {
  padding: 6px 14px;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 6px;
  color: #fff;
  cursor: pointer;
  font-size: 13px;
  transition: all 0.3s ease;
}

.tab-btn:hover:not(:disabled) {
  background: rgba(255, 255, 255, 0.2);
}

.tab-btn.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-color: transparent;
}

.tab-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.device-options {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.option-item {
  display: flex;
  align-items: center;
  gap: 6px;
}

.option-item label {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.7);
}

.option-item select {
  padding: 6px 10px;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 4px;
  color: #fff;
  font-size: 12px;
  cursor: pointer;
  min-width: 150px;
}

.option-item select:focus {
  outline: none;
  border-color: #667eea;
}

.action-buttons {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.btn {
  padding: 10px 20px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.btn:hover {
  transform: translateY(-2px);
}

.btn-preview {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
}

.btn-stop-preview {
  background: rgba(255, 255, 255, 0.2);
  color: #fff;
}

.btn-start-push {
  background: linear-gradient(135deg, #52c41a 0%, #73d13d 100%);
  color: #fff;
}

.btn-start-push:disabled {
  background: rgba(255, 255, 255, 0.2);
  color: rgba(255, 255, 255, 0.4);
  cursor: not-allowed;
}

.btn-stop-push {
  background: linear-gradient(135deg, #ff4757 0%, #ff6b81 100%);
  color: #fff;
}

.btn-get-devices {
  background: linear-gradient(135deg, #1890ff 0%, #40a9ff 100%);
  color: #fff;
}

.device-notice {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 16px;
}

.notice-text {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.6);
}

.btn-end-live {
  background: linear-gradient(135deg, #fa8c16 0%, #ffa940 100%);
  color: #fff;
}

.danmaku-section {
  width: 320px;
  display: flex;
  flex-direction: column;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 12px;
  overflow: hidden;
}

.danmaku-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.danmaku-title {
  font-size: 16px;
  font-weight: 600;
}

.toggle-btn {
  padding: 4px 12px;
  background: rgba(255, 255, 255, 0.1);
  border: none;
  border-radius: 6px;
  color: #fff;
  cursor: pointer;
  font-size: 12px;
  transition: all 0.3s ease;
}

.toggle-btn:hover {
  background: rgba(255, 255, 255, 0.2);
}

.danmaku-list {
  flex: 1;
  overflow-y: auto;
  padding: 12px;
}

.danmaku-item {
  padding: 8px 12px;
  margin-bottom: 8px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 8px;
  font-size: 14px;
  line-height: 1.5;
}

.danmaku-user {
  color: #667eea;
  font-weight: 500;
  margin-right: 8px;
}

.danmaku-content {
  word-break: break-word;
}

.danmaku-empty {
  text-align: center;
  color: rgba(255, 255, 255, 0.5);
  padding: 40px 20px;
}

@media (max-width: 1024px) {
  .live-room-content {
    flex-direction: column;
  }

  .danmaku-section {
    width: 100%;
    max-height: 300px;
  }
}

@media (max-width: 768px) {
  .live-room-header {
    flex-wrap: wrap;
    gap: 12px;
    padding: 12px 16px;
  }

  .header-title {
    order: -1;
    width: 100%;
  }

  .header-title h2 {
    font-size: 16px;
  }

  .live-room-content {
    padding: 16px;
  }

  .video-container {
    min-height: 250px;
  }

  .device-options {
    flex-direction: column;
  }

  .option-item {
    width: 100%;
  }

  .option-item select {
    flex: 1;
    min-width: 0;
  }

  .action-buttons {
    justify-content: center;
  }

  .btn {
    flex: 1;
    min-width: 120px;
  }
}
</style>
