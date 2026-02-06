<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { message } from 'ant-design-vue';
import Hls from 'hls.js';
import { liveApi } from '../api/live';
import { danmakuApi } from '../api/danmaku';
import { giftApi } from '../api/gift';
import { websocketService } from '../utils/websocket';

const route = useRoute();
const router = useRouter();
const liveId = computed(() => parseInt(route.params.id as string) || 0);
const loading = ref(false);
const liveDetail = ref<any>(null);
const danmakuList = ref<any[]>([]);
const giftList = ref<any[]>([]);
const danmakuContent = ref('');
const selectedGift = ref<any>(null);
const giftCount = ref(1);
const videoRef = ref<HTMLVideoElement | null>(null);
const hlsInstance = ref<Hls | null>(null);
const subscriptionId = ref('');
const showGiftEffect = ref(false);
const currentGiftEffect = ref<any>(null);
const danmakuColor = ref('#FFFFFF');
const colorOptions = [
  { color: '#FFFFFF', name: '白色' },
  { color: '#FF0000', name: '红色' },
  { color: '#00FF00', name: '绿色' },
  { color: '#0000FF', name: '蓝色' },
  { color: '#FFFF00', name: '黄色' },
  { color: '#FF00FF', name: '紫色' },
];

const getLiveDetail = async () => {
  if (!liveId.value) return;

  loading.value = true;
  try {
    const response = await liveApi.getLiveDetail(liveId.value);
    if (response.code === 200) {
      liveDetail.value = response.data;
      if (liveDetail.value.status === 1) {
        await getPlayUrl();
      }
    } else {
      message.error(response.message || '获取直播详情失败');
    }
  } catch (error) {
    console.error('获取直播详情错误:', error);
    message.error('获取直播详情失败，请检查网络连接');
  } finally {
    loading.value = false;
  }
};

const getPlayUrl = async () => {
  try {
    const response = await liveApi.getPlayUrl(liveId.value);
    if (response.code === 200 && response.data) {
      liveDetail.value.playUrl = response.data;
      initPlayer(response.data);
    }
  } catch (error) {
    console.error('获取播放地址错误:', error);
  }
};

const initPlayer = (playUrl: string) => {
  if (!videoRef.value) return;

  if (Hls.isSupported()) {
    if (hlsInstance.value) {
      hlsInstance.value.destroy();
    }
    hlsInstance.value = new Hls({
      enableWorker: true,
      lowLatencyMode: true,
    });
    hlsInstance.value.loadSource(playUrl);
    hlsInstance.value.attachMedia(videoRef.value);
    hlsInstance.value.on(Hls.Events.MANIFEST_PARSED, () => {
      videoRef.value?.play().catch((e) => console.error('自动播放失败:', e));
    });
  } else if (videoRef.value.canPlayType('application/vnd.apple.mpegurl')) {
    videoRef.value.src = playUrl;
    videoRef.value.addEventListener('loadedmetadata', () => {
      videoRef.value?.play().catch((e) => console.error('自动播放失败:', e));
    });
  }
};

const initWebSocket = async () => {
  try {
    const wsUrl = `${window.location.protocol}//${window.location.host}/ws`;
    await websocketService.connect(wsUrl);
    
    subscriptionId.value = websocketService.subscribe(
      `/topic/live/${liveId.value}`,
      handleMessage
    );
  } catch (error) {
    console.error('WebSocket连接错误:', error);
  }
};

const handleMessage = (message: any) => {
  if (message.type === 'danmaku') {
    danmakuList.value.unshift(message);
    if (danmakuList.value.length > 100) {
      danmakuList.value.pop();
    }
  } else if (message.type === 'gift') {
    showGiftAnimation(message);
  }
};

const showGiftAnimation = (giftData: any) => {
  currentGiftEffect.value = giftData;
  showGiftEffect.value = true;
  setTimeout(() => {
    showGiftEffect.value = false;
  }, 3000);
};

const getDanmakuList = async () => {
  if (!liveId.value) return;

  try {
    const response = await danmakuApi.getDanmakuList(liveId.value);
    if (response.code === 200) {
      danmakuList.value = response.data || [];
    }
  } catch (error) {
    console.error('获取弹幕列表错误:', error);
  }
};

const getGiftList = async () => {
  try {
    const response = await giftApi.getGiftList();
    if (response.code === 200) {
      giftList.value = response.data || [];
    }
  } catch (error) {
    console.error('获取礼物列表错误:', error);
  }
};

const sendDanmaku = async () => {
  if (!danmakuContent.value) {
    message.error('请输入弹幕内容');
    return;
  }

  if (!liveId.value) return;

  try {
    const response = await danmakuApi.sendDanmaku({
      liveId: liveId.value,
      content: danmakuContent.value,
      color: danmakuColor.value
    });

    if (response.code === 200) {
      danmakuContent.value = '';
    } else {
      message.error(response.message || '发送弹幕失败');
    }
  } catch (error) {
    console.error('发送弹幕错误:', error);
    message.error('发送弹幕失败，请检查网络连接');
  }
};

const sendGift = async () => {
  if (!selectedGift.value) {
    message.error('请选择礼物');
    return;
  }

  if (!liveId.value) return;

  try {
    const response = await giftApi.sendGift({
      liveId: liveId.value,
      giftId: selectedGift.value.id,
      count: giftCount.value,
      toUserId: liveDetail.value.userId
    });

    if (response.code === 200) {
      message.success('发送礼物成功');
      selectedGift.value = null;
      giftCount.value = 1;
    } else {
      message.error(response.message || '发送礼物失败');
    }
  } catch (error) {
    console.error('发送礼物错误:', error);
    message.error('发送礼物失败，请检查网络连接');
  }
};

const handleBack = () => {
  router.push('/');
};

onMounted(() => {
  getLiveDetail();
  getDanmakuList();
  getGiftList();
  initWebSocket();
});

onUnmounted(() => {
  if (subscriptionId.value) {
    websocketService.unsubscribe(subscriptionId.value);
  }
  websocketService.disconnect();
  if (hlsInstance.value) {
    hlsInstance.value.destroy();
  }
});
</script>

<template>
  <div class="live-room-container">
    <div class="live-room-header">
      <button class="back-button" @click="handleBack">← 返回</button>
      <h1 v-if="liveDetail">{{ liveDetail.title }}</h1>
      <h1 v-else>直播间</h1>
      <div v-if="liveDetail?.status === 1" class="live-badge">直播中</div>
    </div>
    <div class="live-room-content">
      <div v-if="loading" class="loading">加载中...</div>
      <div v-else-if="!liveDetail" class="empty">直播间不存在</div>
      <div v-else class="live-room">
        <div class="live-main">
          <div class="live-stream">
            <video 
              ref="videoRef"
              class="live-video"
              autoplay
              muted
              playsinline
            ></video>
            <div v-if="liveDetail.status !== 1" class="live-offline">
              <p>直播未开始或已结束</p>
            </div>
          </div>
          
          <div v-if="showGiftEffect" class="gift-effect">
            <div class="gift-effect-content">
              <img :src="currentGiftEffect?.giftImage" alt="礼物" />
              <div class="gift-effect-text">
                <p class="gift-user">{{ currentGiftEffect?.fromUsername }} 送出</p>
                <p class="gift-name">{{ currentGiftEffect?.giftName }} x{{ currentGiftEffect?.giftCount }}</p>
              </div>
            </div>
          </div>
        </div>
        
        <div class="live-interaction">
          <div class="danmaku-section">
            <div class="section-header">
              <h3>弹幕</h3>
              <span class="danmaku-count">{{ danmakuList.length }}条</span>
            </div>
            <div class="danmaku-list" ref="danmakuListRef">
              <div 
                v-for="(danmaku, index) in danmakuList" 
                :key="index" 
                class="danmaku-item"
              >
                <span class="danmaku-user">{{ danmaku.username || '用户' + danmaku.userId }}:</span>
                <span class="danmaku-content" :style="{ color: danmaku.color || '#FFFFFF' }">
                  {{ danmaku.content }}
                </span>
              </div>
            </div>
            <div class="danmaku-input">
              <div class="color-picker">
                <div 
                  v-for="option in colorOptions" 
                  :key="option.color"
                  class="color-option"
                  :class="{ active: danmakuColor === option.color }"
                  :style="{ backgroundColor: option.color }"
                  @click="danmakuColor = option.color"
                  :title="option.name"
                ></div>
              </div>
              <input 
                type="text" 
                v-model="danmakuContent" 
                placeholder="输入弹幕内容..."
                @keyup.enter="sendDanmaku"
              />
              <button class="send-button" @click="sendDanmaku">发送</button>
            </div>
          </div>
          
          <div class="gift-section">
            <h3>礼物</h3>
            <div class="gift-list">
              <div 
                v-for="gift in giftList" 
                :key="gift.id" 
                class="gift-item" 
                :class="{ active: selectedGift?.id === gift.id }"
                @click="selectedGift = gift"
              >
                <img :src="gift.image || 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=gift%20icon&image_size=square'" :alt="gift.name" />
                <p class="gift-name">{{ gift.name }}</p>
                <p class="gift-price">￥{{ gift.price }}</p>
              </div>
            </div>
            <div class="gift-send" v-if="selectedGift">
              <div class="gift-count">
                <span>数量:</span>
                <button @click="giftCount > 1 && giftCount--">-</button>
                <span class="count-value">{{ giftCount }}</span>
                <button @click="giftCount++">+</button>
              </div>
              <button class="send-gift-button" @click="sendGift">
                发送礼物 (￥{{ selectedGift.price * giftCount }})
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.live-room-container {
  width: 100%;
  height: 100%;
}

.live-room-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
  padding-bottom: 20px;
  border-bottom: 1px solid #e8e8e8;
}

.live-room-title {
  flex: 1;
}

.live-room-title h1 {
  font-size: 32px;
  font-weight: 600;
  color: #333;
  margin-bottom: 12px;
  line-height: 1.2;
}

.live-room-meta {
  display: flex;
  align-items: center;
  gap: 24px;
  font-size: 14px;
  color: #666;
}

/* 移除可能导致图标重复的伪元素 */
/* .view-count::before {
  content: '👁';
  margin-right: 6px;
  font-size: 16px;
} */

.live-badge {
  padding: 8px 20px;
  background-color: #ff4d4f;
  color: white;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 600;
  animation: pulse 2s infinite;
  box-shadow: 0 2px 8px rgba(255, 77, 79, 0.3);
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.7; }
}

.back-button {
  padding: 10px 20px;
  background-color: #f0f0f0;
  color: #333;
  border: 1px solid #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 6px;
  margin-right: 20px;
}

.back-button:hover {
  background-color: #e6e6e6;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.live-room-content {
  min-height: 600px;
  height: calc(100vh - 320px);
}

.loading, .empty {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  font-size: 18px;
  color: #999;
  background-color: #fafafa;
  border-radius: 8px;
  box-shadow: inset 0 2px 4px rgba(0, 0, 0, 0.05);
}

.live-room {
  display: flex;
  gap: 24px;
  height: 100%;
}

.live-main {
  flex: 3;
  display: flex;
  flex-direction: column;
  min-width: 600px;
}

.live-stream {
  flex: 1;
  background-color: #000;
  border-radius: 12px;
  overflow: hidden;
  position: relative;
  min-height: 480px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.2);
}

.live-video {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.live-offline {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: rgba(0, 0, 0, 0.8);
  color: #fff;
  font-size: 20px;
  font-weight: 500;
  backdrop-filter: blur(5px);
}

.gift-effect {
  position: absolute;
  bottom: 30px;
  left: 30px;
  z-index: 100;
  animation: slideIn 0.5s ease-out, fadeOut 0.5s ease-in 3s forwards;
  max-width: 400px;
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
    transform: translateX(-30px);
  }
}

.gift-effect-content {
  display: flex;
  align-items: center;
  gap: 16px;
  background: linear-gradient(135deg, rgba(255, 215, 0, 0.95), rgba(255, 140, 0, 0.95));
  padding: 16px 24px;
  border-radius: 50px;
  box-shadow: 0 8px 32px rgba(255, 140, 0, 0.4);
  backdrop-filter: blur(10px);
}

.gift-effect-content img {
  width: 60px;
  height: 60px;
  object-fit: contain;
  border: 2px solid #fff;
  border-radius: 50%;
  background-color: rgba(255, 255, 255, 0.9);
}

.gift-effect-text {
  color: #fff;
}

.gift-user {
  margin: 0;
  font-size: 14px;
  font-weight: 600;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
}

.gift-name {
  margin: 4px 0 0 0;
  font-size: 18px;
  font-weight: 700;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
}

.live-interaction {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 24px;
  min-width: 360px;
  max-width: 400px;
}

.danmaku-section, .gift-section {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  flex: 1;
  display: flex;
  flex-direction: column;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.section-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #333;
  display: flex;
  align-items: center;
  gap: 8px;
}

.section-header h3::before {
  content: '💬';
  font-size: 20px;
}

.gift-section .section-header h3::before {
  content: '🎁';
}

.danmaku-count {
  font-size: 14px;
  color: #999;
  background-color: #f5f5f5;
  padding: 4px 12px;
  border-radius: 12px;
}

.danmaku-list {
  flex: 1;
  overflow-y: auto;
  margin-bottom: 20px;
  padding: 12px;
  background: #fafafa;
  border-radius: 8px;
  border: 1px solid #f0f0f0;
}

.danmaku-list::-webkit-scrollbar {
  width: 6px;
}

.danmaku-list::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.danmaku-list::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.danmaku-list::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

.danmaku-item {
  padding: 10px 0;
  border-bottom: 1px solid #f0f0f0;
  font-size: 14px;
  animation: fadeIn 0.3s ease;
}

.danmaku-item:last-child {
  border-bottom: none;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.danmaku-user {
  font-weight: 600;
  margin-right: 12px;
  color: #667eea;
  font-size: 14px;
}

.danmaku-content {
  word-break: break-all;
  color: #333;
  line-height: 1.4;
}

.danmaku-input {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.color-picker {
  display: flex;
  gap: 10px;
}

.color-option {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  cursor: pointer;
  border: 2px solid transparent;
  transition: all 0.3s ease;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.color-option.active {
  border-color: #667eea;
  transform: scale(1.2);
  box-shadow: 0 4px 8px rgba(102, 126, 234, 0.3);
}

.color-option:hover {
  transform: scale(1.1);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
}

.danmaku-input input {
  flex: 1;
  padding: 12px 16px;
  border: 1px solid #d9d9d9;
  border-radius: 8px;
  font-size: 14px;
  transition: all 0.3s ease;
  background-color: #fafafa;
}

.danmaku-input input:focus {
  outline: none;
  border-color: #667eea;
  background-color: white;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.send-button {
  padding: 12px 24px;
  background-color: #667eea;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
  transition: all 0.3s ease;
  white-space: nowrap;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.3);
}

.send-button:hover {
  background-color: #5a6fe0;
  transform: translateY(-1px);
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.4);
}

.send-button:active {
  transform: translateY(0);
}

.gift-section h3 {
  margin: 0 0 16px 0;
  font-size: 18px;
  font-weight: 600;
  color: #333;
  display: flex;
  align-items: center;
  gap: 8px;
}

.gift-list {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 20px;
  max-height: 240px;
  overflow-y: auto;
  padding: 8px;
}

.gift-list::-webkit-scrollbar {
  width: 6px;
}

.gift-list::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.gift-list::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.gift-item {
  border: 2px solid #f0f0f0;
  border-radius: 8px;
  padding: 16px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  background-color: #fafafa;
  position: relative;
  overflow: hidden;
}

.gift-item::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.gift-item:hover {
  border-color: #667eea;
  background-color: #f0f4ff;
  transform: translateY(-3px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.25);
}

.gift-item:hover::before {
  opacity: 1;
}

.gift-item.active {
  border-color: #667eea;
  background-color: #f0f4ff;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.2);
}

.gift-item img {
  width: 72px;
  height: 72px;
  object-fit: contain;
  margin-bottom: 12px;
  transition: transform 0.3s ease;
  z-index: 1;
  position: relative;
}

.gift-item:hover img {
  transform: scale(1.1);
}

.gift-name {
  margin: 0;
  font-size: 14px;
  font-weight: 600;
  color: #333;
  z-index: 1;
  position: relative;
  text-align: center;
}

.gift-price {
  margin: 6px 0 0 0;
  font-size: 14px;
  color: #ff4d4f;
  font-weight: 600;
  z-index: 1;
  position: relative;
}

.gift-send {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
  margin-top: auto;
}

.gift-count {
  display: flex;
  align-items: center;
  gap: 12px;
}

.gift-count span:first-child {
  font-size: 14px;
  font-weight: 500;
  color: #666;
  min-width: 50px;
}

.gift-count button {
  padding: 6px 16px;
  background-color: #f0f0f0;
  border: 1px solid #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.08);
}

.gift-count button:hover {
  background-color: #e8e8e8;
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.12);
}

.count-value {
  min-width: 60px;
  text-align: center;
  font-size: 16px;
  font-weight: 600;
  color: #333;
  background-color: #fafafa;
  padding: 6px 12px;
  border-radius: 6px;
  border: 1px solid #f0f0f0;
}

.send-gift-button {
  padding: 14px;
  background: linear-gradient(135deg, #ff4d4f 0%, #ff7875 100%);
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 16px;
  font-weight: 600;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(255, 77, 79, 0.3);
}

.send-gift-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(255, 77, 79, 0.4);
}

.send-gift-button:active {
  transform: translateY(0);
}

/* 响应式布局调整 */
@media (max-width: 1400px) {
  .live-room {
    height: calc(100vh - 300px);
  }
  
  .live-main {
    min-width: 500px;
  }
  
  .live-interaction {
    min-width: 320px;
    max-width: 360px;
  }
  
  .live-stream {
    min-height: 400px;
  }
}

@media (max-width: 1200px) {
  .live-room {
    flex-direction: column;
    height: auto;
    min-height: 800px;
  }
  
  .live-main {
    min-width: 100%;
  }
  
  .live-interaction {
    min-width: 100%;
    max-width: 100%;
    flex-direction: row;
  }
  
  .danmaku-section, .gift-section {
    flex: 1;
  }
}

@media (max-width: 768px) {
  .live-room-container {
    padding: 16px;
  }
  
  .live-room-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .live-room-title h1 {
    font-size: 24px;
  }
  
  .live-room {
    gap: 16px;
  }
  
  .live-interaction {
    flex-direction: column;
  }
  
  .live-stream {
    min-height: 320px;
    border-radius: 8px;
  }
  
  .gift-effect {
    bottom: 20px;
    left: 20px;
    max-width: 300px;
  }
  
  .gift-effect-content {
    padding: 12px 20px;
  }
  
  .gift-effect-content img {
    width: 48px;
    height: 48px;
  }
}
</style>
