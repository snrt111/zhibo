<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed, nextTick } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { message } from 'ant-design-vue';
import Hls from 'hls.js';
import { liveApi } from '../api/live';
import { danmakuApi } from '../api/danmaku';
import { giftApi } from '../api/gift';
import { followApi } from '../api/follow';
import { commentApi } from '../api/comment';
import { reportApi } from '../api/report';
import { watchHistoryApi } from '../api/watchHistory';
import { websocketService } from '../utils/websocket';
import { handleResponseAsync, errorHandler } from '../utils/errorHandler';
import GiftIcon from './GiftIcon.vue';
import DanmakuLayer from './DanmakuLayer.vue';
import ShareDialog from './ShareDialog.vue';

const route = useRoute();
const router = useRouter();
const liveId = computed(() => parseInt(route.params.id as string) || 0);
const loading = ref(false);
const liveDetail = ref<any>(null);
const danmakuList = ref<any[]>([]);
const giftList = ref<any[]>([]);
const hotGiftList = ref<any[]>([]);
const danmakuContent = ref('');
const selectedGift = ref<any>(null);
const giftCount = ref(1);
const videoRef = ref<HTMLVideoElement | null>(null);
const hlsInstance = ref<Hls | null>(null);
const subscriptionId = ref('');
const showGiftEffect = ref(false);
const currentGiftEffect = ref<any>(null);
const danmakuColor = ref('#FFFFFF');
const showColorPicker = ref(false);
const danmakuFontSize = ref(1);
const showFontPicker = ref(false);
const showGiftPanel = ref(false);
const isMobile = ref(window.innerWidth <= 768);
const isLiked = ref(false);
const likeCount = ref(0);
const showDanmaku = ref(true);
const danmakuListRef = ref<HTMLElement | null>(null);
const isStreamLoading = ref(false);
const streamRetryCount = ref(0);
const maxRetryCount = 5;
const isFollowing = ref(false);
const showCommentPanel = ref(false);
const commentList = ref<any[]>([]);
const commentContent = ref('');
const commentPage = ref(0);
const commentTotal = ref(0);
const commentLoading = ref(false);
const showReportPanel = ref(false);
const reportReason = ref('');
const reportDescription = ref('');
const showShareDialog = ref(false);
const shareData = ref({
  liveId: 0,
  title: '',
  cover: '',
  anchorName: ''
});
const reportReasons = [
  { value: '1', label: '色情低俗' },
  { value: '2', label: '违法违规' },
  { value: '3', label: '诈骗欺诈' },
  { value: '4', label: '侵权盗版' },
  { value: '5', label: '辱骂攻击' },
  { value: '6', label: '其他原因' }
];

const colorOptions = [
  { color: '#FFFFFF', name: '白色' },
  { color: '#FF0000', name: '红色' },
  { color: '#00FF00', name: '绿色' },
  { color: '#0000FF', name: '蓝色' },
  { color: '#FFFF00', name: '黄色' },
  { color: '#FF00FF', name: '紫色' },
  { color: '#FFA500', name: '橙色' },
  { color: '#00FFFF', name: '青色' },
];

const fontSizes = [
  { value: 0, label: '小', preview: 12 },
  { value: 1, label: '中', preview: 14 },
  { value: 2, label: '大', preview: 18 },
];

const watchStartTime = ref<number>(0);
const watchDuration = ref<number>(0);
const watchHistoryTimer = ref<NodeJS.Timeout | null>(null);
const currentLiveId = ref<number>(0);

const startWatchHistory = () => {
  const token = localStorage.getItem('token');
  if (!token) return;
  
  currentLiveId.value = liveId.value;
  console.log('开始观看直播 - liveId:', currentLiveId.value);
  
  watchStartTime.value = Date.now();
  watchDuration.value = 0;
  
  watchHistoryTimer.value = setInterval(() => {
    watchDuration.value = Math.floor((Date.now() - watchStartTime.value) / 1000);
  }, 1000);
};

const stopWatchHistory = async () => {
  if (watchHistoryTimer.value) {
    clearInterval(watchHistoryTimer.value);
    watchHistoryTimer.value = null;
  }
  
  const token = localStorage.getItem('token');
  if (!token || watchDuration.value < 10) {
    console.log('未记录观看历史: token=', !!token, ', duration=', watchDuration.value, ', liveId=', currentLiveId.value);
    return;
  }
  
  console.log('准备记录观看历史 - liveId:', currentLiveId.value, ', duration:', watchDuration.value);
  
  try {
    const response = await watchHistoryApi.recordWatchHistory(currentLiveId.value, watchDuration.value);
    console.log('观看历史记录成功:', response);
  } catch (error) {
    console.error('记录观看历史失败:', error);
  }
};

const getLiveDetail = async () => {
  if (!liveId.value) return;

  loading.value = true;
  try {
    const response = await liveApi.getLiveDetail(liveId.value);
    await handleResponseAsync(response, async (data) => {
      liveDetail.value = data;
      likeCount.value = (data as any)?.likeCount ?? 0;
      if (liveDetail.value.status === 1) {
        await getPlayUrl();
      }
      await increaseViewCount();
      await checkFollowStatus();
      startWatchHistory();
    });
  } catch (error) {
    errorHandler.handle(error);
  } finally {
    loading.value = false;
  }
};

const checkFollowStatus = async () => {
  const token = localStorage.getItem('token');
  if (!token || !liveDetail.value?.userId) return;
  try {
    const response = await followApi.isFollowing(liveDetail.value.userId);
    await handleResponseAsync(response, (data) => {
      isFollowing.value = data || false;
    });
  } catch (error) {
    errorHandler.handle(error, false);
  }
};

const handleFollow = async () => {
  const token = localStorage.getItem('token');
  if (!token) {
    message.error('请先登录后再关注');
    return;
  }
  if (!liveDetail.value?.userId) {
    message.error('无法获取主播信息');
    return;
  }

  try {
    if (isFollowing.value) {
      const response = await followApi.unfollow(liveDetail.value.userId);
      await handleResponseAsync(response, () => {
        isFollowing.value = false;
        message.success('取消关注成功');
      });
    } else {
      const response = await followApi.follow(liveDetail.value.userId);
      await handleResponseAsync(response, () => {
        isFollowing.value = true;
        message.success('关注成功');
      });
    }
  } catch (error) {
    errorHandler.handle(error);
  }
};

const handleShare = () => {
  if (!liveDetail.value) return;

  shareData.value = {
    liveId: liveId.value,
    title: liveDetail.value.title,
    cover: liveDetail.value.cover,
    anchorName: liveDetail.value.username || '主播'
  };
  showShareDialog.value = true;
};

const handleShareSuccess = (platform: string) => {
  console.log('分享到:', platform);
};

const increaseViewCount = async () => {
  if (!liveId.value) return;
  try {
    await liveApi.increaseViewCount(liveId.value);
  } catch (error) {
    errorHandler.handle(error, false);
  }
};

const getPlayUrl = async () => {
  try {
    const response = await liveApi.getPlayUrl(liveId.value);
    await handleResponseAsync(response, (data) => {
      if (data) {
        let url = data as string;
        if (url.startsWith('/')) {
          url = `${window.location.protocol}//${window.location.host}${url}`;
        }
        liveDetail.value.playUrl = url;
        streamRetryCount.value = 0;
        nextTick();
        setTimeout(() => {
          initPlayer(url);
        }, 100);
      }
    });
  } catch (error) {
    errorHandler.handle(error, false);
  }
};

const retryPlayStream = () => {
  if (streamRetryCount.value < maxRetryCount) {
    streamRetryCount.value++;
    console.log(`重试加载流 (${streamRetryCount.value}/${maxRetryCount})...`);
    setTimeout(() => {
      if (liveDetail.value.playUrl) {
        initPlayer(liveDetail.value.playUrl);
      } else {
        getPlayUrl();
      }
    }, 2000 * streamRetryCount.value);
  } else {
    message.error('直播流加载失败，请稍后重试');
    isStreamLoading.value = false;
  }
};

const initPlayer = (playUrl: string) => {
  console.log('开始初始化播放器，videoRef:', videoRef.value, '播放地址:', playUrl);
  isStreamLoading.value = true;
  
  if (!videoRef.value) {
    console.error('videoRef 为空，无法初始化播放器');
    retryPlayStream();
    return;
  }

  if (Hls.isSupported()) {
    if (hlsInstance.value) {
      hlsInstance.value.destroy();
      hlsInstance.value = null;
    }
    hlsInstance.value = new Hls({
      enableWorker: true,
      lowLatencyMode: true,
      debug: true,
    });
    hlsInstance.value.loadSource(playUrl);
    hlsInstance.value.attachMedia(videoRef.value);
    hlsInstance.value.on(Hls.Events.MANIFEST_PARSED, () => {
      console.log('HLS manifest 解析完成，开始播放');
      isStreamLoading.value = false;
      streamRetryCount.value = 0;
      videoRef.value?.play().catch((e) => {
        console.warn('自动播放失败，需要用户交互:', e);
      });
    });
    hlsInstance.value.on(Hls.Events.ERROR, (_event, data) => {
      console.error('HLS 错误:', data);
      if (data.fatal) {
        switch (data.type) {
          case Hls.ErrorTypes.NETWORK_ERROR:
            console.log('网络错误，尝试重新加载...');
            hlsInstance.value?.startLoad();
            break;
          case Hls.ErrorTypes.MEDIA_ERROR:
            console.log('媒体错误，尝试恢复...');
            hlsInstance.value?.recoverMediaError();
            break;
          default:
            console.log('致命错误，销毁实例并重试');
            hlsInstance.value?.destroy();
            hlsInstance.value = null;
            retryPlayStream();
            break;
        }
      }
    });
    hlsInstance.value.on(Hls.Events.LEVEL_SWITCHED, () => {
      console.log('HLS 级别切换完成');
      isStreamLoading.value = false;
    });
  } else if (videoRef.value.canPlayType('application/vnd.apple.mpegurl')) {
    videoRef.value.src = playUrl;
    videoRef.value.addEventListener('loadedmetadata', () => {
      console.log('原生播放器元数据加载完成');
      isStreamLoading.value = false;
      streamRetryCount.value = 0;
      videoRef.value?.play().catch((e) => {
        console.warn('自动播放失败，需要用户交互:', e);
      });
    });
    videoRef.value.addEventListener('error', (e) => {
      console.error('原生播放器错误:', e);
      retryPlayStream();
    });
  } else {
    console.error('浏览器不支持 HLS 播放');
    message.error('您的浏览器不支持直播播放');
    isStreamLoading.value = false;
  }
};

const initWebSocket = async () => {
  try {
    const wsUrl = window.location.protocol === 'https:'
      ? `https://${window.location.host}/ws`
      : `http://${window.location.host}/ws`;
    console.log('开始连接 WebSocket:', wsUrl);
    await websocketService.connect(wsUrl);
    console.log('WebSocket 连接成功，开始订阅:', `/topic/live/${liveId.value}`);
    
    subscriptionId.value = websocketService.subscribe(
      `/topic/live/${liveId.value}`,
      handleMessage
    );
    console.log('订阅成功，subscriptionId:', subscriptionId.value);
  } catch (error) {
    console.error('WebSocket连接错误:', error);
  }
};

const scrollToBottom = () => {
  nextTick(() => {
    if (danmakuListRef.value) {
      danmakuListRef.value.scrollTop = danmakuListRef.value.scrollHeight;
    }
  });
};

const handleMessage = (message: any) => {
  console.log('收到 WebSocket 消息:', message);
  if (message.type === 'danmaku') {
    danmakuList.value.push(message);
    if (danmakuList.value.length > 100) {
      danmakuList.value.shift();
    }
    scrollToBottom();
  } else if (message.type === 'gift') {
    showGiftAnimation(message);
  } else if (message.type === 'comment') {
    commentList.value.unshift(message);
    commentTotal.value++;
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
    await handleResponseAsync(response, (data) => {
      danmakuList.value = (data || []).reverse();
      scrollToBottom();
    });
  } catch (error) {
    errorHandler.handle(error, false);
  }
};

const getGiftList = async () => {
  try {
    const [allGiftsResponse, hotGiftsResponse] = await Promise.all([
      giftApi.getGiftList(),
      giftApi.getHotGiftList()
    ]);
    await handleResponseAsync(allGiftsResponse, (data) => {
      giftList.value = data || [];
    });
    await handleResponseAsync(hotGiftsResponse, (data) => {
      hotGiftList.value = data || [];
    });
  } catch (error) {
    errorHandler.handle(error, false);
  }
};

const sendDanmaku = async () => {
  const token = localStorage.getItem('token');
  if (!token) {
    message.error('请先登录后再发送弹幕');
    return;
  }

  if (!danmakuContent.value) {
    message.error('请输入弹幕内容');
    return;
  }

  if (!liveId.value) return;

  try {
    const response = await danmakuApi.sendDanmaku({
      liveId: liveId.value,
      content: danmakuContent.value,
      color: danmakuColor.value,
      fontSize: danmakuFontSize.value
    });
    await handleResponseAsync(response, () => {
      danmakuContent.value = '';
      getDanmakuList();
    });
  } catch (error) {
    errorHandler.handle(error);
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
    await handleResponseAsync(response, () => {
      message.success('发送礼物成功');
      selectedGift.value = null;
      giftCount.value = 1;
    });
  } catch (error) {
    errorHandler.handle(error);
  }
};

const handleLike = () => {
  isLiked.value = !isLiked.value;
  likeCount.value += isLiked.value ? 1 : -1;
};

const handleBack = () => {
  router.push('/');
};

const formatCount = (count: number) => {
  if (count >= 10000) {
    return (count / 10000).toFixed(1) + 'w';
  }
  return count.toString();
};

const getCommentList = async (reset: boolean = false) => {
  if (!liveId.value) return;
  
  if (reset) {
    commentPage.value = 0;
    commentList.value = [];
  }
  
  commentLoading.value = true;
  try {
    const response = await commentApi.getCommentList(liveId.value, commentPage.value, 20);
    await handleResponseAsync(response, (data: any) => {
      if (data?.list) {
        if (commentPage.value === 0) {
          commentList.value = data.list;
        } else {
          commentList.value.push(...data.list);
        }
        commentTotal.value = data.total || 0;
      }
    });
  } catch (error) {
    errorHandler.handle(error, false);
  } finally {
    commentLoading.value = false;
  }
};

const sendComment = async () => {
  const token = localStorage.getItem('token');
  if (!token) {
    message.error('请先登录后再发表评论');
    return;
  }

  if (!commentContent.value || !commentContent.value.trim()) {
    message.error('请输入评论内容');
    return;
  }

  if (!liveId.value) return;

  try {
    const response = await commentApi.createComment({
      liveId: liveId.value,
      content: commentContent.value.trim()
    });
    await handleResponseAsync(response, () => {
      message.success('评论成功');
      commentContent.value = '';
      getCommentList(true);
    });
  } catch (error) {
    errorHandler.handle(error);
  }
};

const likeComment = async (commentId: number) => {
  try {
    const response = await commentApi.likeComment(commentId);
    await handleResponseAsync(response, () => {
      const comment = commentList.value.find((c: any) => c.id === commentId);
      if (comment) {
        comment.likeCount = (comment.likeCount || 0) + 1;
        comment.isLiked = true;
      }
    });
  } catch (error) {
    errorHandler.handle(error, false);
  }
};

const loadMoreComments = () => {
  if (commentLoading.value || commentList.value.length >= commentTotal.value) return;
  commentPage.value++;
  getCommentList();
};

const formatTime = (dateStr: string) => {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  const now = new Date();
  const diff = now.getTime() - date.getTime();
  
  if (diff < 60000) return '刚刚';
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前';
  if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前';
  return date.toLocaleDateString();
};

const submitReport = async () => {
  if (!reportReason.value) {
    message.error('请选择举报原因');
    return;
  }
  
  try {
    const response = await reportApi.createReport({
      targetType: 1,
      targetId: liveId.value,
      reason: reportReasons.find(r => r.value === reportReason.value)?.label || '',
      description: reportDescription.value
    });
    await handleResponseAsync(response, () => {
      message.success('举报已提交，我们会尽快处理');
      showReportPanel.value = false;
      reportReason.value = '';
      reportDescription.value = '';
    });
  } catch (error) {
    errorHandler.handle(error);
  }
};

onMounted(() => {
  getLiveDetail();
  getDanmakuList();
  getGiftList();
  getCommentList(true);
  initWebSocket();
  
  window.addEventListener('resize', () => {
    isMobile.value = window.innerWidth <= 768;
  });
});

onUnmounted(() => {
  stopWatchHistory();
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
  <div class="live-room-container" :class="{ 'mobile': isMobile }">
    <template v-if="!isMobile">
      <div class="live-room-header">
        <div class="header-left">
          <button class="back-button" @click="handleBack">
            <span class="back-icon">‹</span>
          </button>
          <div class="host-avatar">
            <img v-if="liveDetail?.userAvatar" :src="liveDetail.userAvatar" alt="主播头像" />
            <span v-else class="avatar-placeholder">{{ liveDetail?.userNickname?.charAt(0) || 'U' }}</span>
          </div>
          <div class="live-info">
            <div class="host-name">
              <span class="nickname">{{ liveDetail?.userNickname || '用户' + liveDetail?.userId }}</span>
              <span v-if="liveDetail?.status === 1" class="live-badge-small">
                <span class="live-dot-small"></span>
                直播中
              </span>
            </div>
            <div class="view-count-small">👁 {{ formatCount(liveDetail?.viewCount || 0) }} 观看</div>
          </div>
        </div>
        <div class="header-right">
          <button class="follow-button" :class="{ followed: isFollowing }" @click="handleFollow">
            {{ isFollowing ? '已关注' : '+ 关注' }}
          </button>
          <button class="share-button" @click="handleShare">分享</button>
          <button class="report-button" @click="showReportPanel = true">举报</button>
        </div>
      </div>
    </template>
    
    <div class="live-room-content">
      <div v-if="loading" class="loading">
        <div class="loading-spinner"></div>
        <span>加载中...</span>
      </div>
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
            <DanmakuLayer 
              v-if="!isMobile" 
              :danmaku-list="danmakuList" 
              :visible="showDanmaku" 
            />
            <div v-if="isStreamLoading" class="stream-loading">
              <div class="loading-spinner"></div>
              <span>直播流加载中...</span>
            </div>
            <div v-if="liveDetail.status !== 1" class="live-offline">
              <div class="offline-icon">📺</div>
              <p>直播未开始或已结束</p>
            </div>
          </div>
          
          <div v-if="!isMobile" class="desktop-danmaku-bar">
              <button 
                class="danmaku-toggle-btn" 
                :class="{ active: showDanmaku }"
                @click="showDanmaku = !showDanmaku"
              >
                <span class="btn-icon">{{ showDanmaku ? '✓' : '' }}</span>
                弹幕
              </button>
              
              <div class="danmaku-color-wrapper">
                <button class="danmaku-setting-btn" @click="showColorPicker = !showColorPicker">
                  <span class="color-dot" :style="{ backgroundColor: danmakuColor }"></span>
                  颜色
                </button>
                <div class="color-picker-panel" v-if="showColorPicker">
                  <div 
                    v-for="option in colorOptions" 
                    :key="option.color"
                    class="color-option"
                    :class="{ active: danmakuColor === option.color }"
                    :style="{ backgroundColor: option.color }"
                    @click="danmakuColor = option.color; showColorPicker = false"
                    :title="option.name"
                  ></div>
                </div>
              </div>
              
              <div class="danmaku-font-wrapper">
                <button class="danmaku-setting-btn" @click="showFontPicker = !showFontPicker">
                  <span class="font-icon">T</span>
                  大小
                </button>
                <div class="font-picker-panel" v-if="showFontPicker">
                  <button 
                    v-for="size in fontSizes" 
                    :key="size.value"
                    class="font-option"
                    :class="{ active: danmakuFontSize === size.value }"
                    :style="{ fontSize: size.preview + 'px' }"
                    @click="danmakuFontSize = size.value; showFontPicker = false"
                  >
                    {{ size.label }}
                  </button>
                </div>
              </div>
              
              <div class="danmaku-input-area">
                <input 
                  type="text" 
                  v-model="danmakuContent" 
                  placeholder="发送弹幕..."
                  @keyup.enter="sendDanmaku"
                />
              </div>
              <button class="send-danmaku-btn" @click="sendDanmaku">发送</button>
            </div>
        </div>
        
        <div v-if="isMobile" class="live-sidebar">
          <div class="mobile-header">
            <button class="back-button" @click="handleBack">
              <span class="back-icon">‹</span>
            </button>
            <div class="mobile-host-info">
              <div class="host-avatar-small">
                <img v-if="liveDetail?.userAvatar" :src="liveDetail.userAvatar" alt="主播头像" />
                <span v-else class="avatar-placeholder">{{ liveDetail?.userNickname?.charAt(0) || 'U' }}</span>
              </div>
              <span class="host-name-small">{{ liveDetail?.userNickname || '用户' + liveDetail?.userId }}</span>
              <span v-if="liveDetail?.status === 1" class="live-tag">直播中</span>
            </div>
          </div>
          
          <div class="mobile-sidebar">
            <button class="sidebar-btn like-btn" :class="{ active: isLiked }" @click="handleLike">
              <span class="btn-icon">{{ isLiked ? '❤️' : '🤍' }}</span>
              <span class="btn-text">{{ formatCount(likeCount) }}</span>
            </button>
            <button class="sidebar-btn gift-btn" @click="showGiftPanel = true">
              <span class="btn-icon">🎁</span>
              <span class="btn-text">礼物</span>
            </button>
            <button class="sidebar-btn comment-btn" @click="showCommentPanel = true">
              <span class="btn-icon">💬</span>
              <span class="btn-text">{{ commentTotal }}</span>
            </button>
            <button class="sidebar-btn share-btn">
              <span class="btn-icon">↗️</span>
              <span class="btn-text">分享</span>
            </button>
            <button class="sidebar-btn report-btn" @click="showReportPanel = true">
              <span class="btn-icon">⚠️</span>
              <span class="btn-text">举报</span>
            </button>
          </div>
          
          <DanmakuLayer 
            v-if="isMobile" 
            :danmaku-list="danmakuList" 
            :visible="showDanmaku" 
          />
          
          <div class="mobile-bottom">
            <div class="mobile-danmaku-controls">
              <button 
                class="mobile-danmaku-toggle" 
                :class="{ active: showDanmaku }"
                @click="showDanmaku = !showDanmaku"
              >
                {{ showDanmaku ? '弹幕开' : '弹幕关' }}
              </button>
              
              <div class="mobile-color-wrapper">
                <button class="mobile-setting-btn" @click="showColorPicker = !showColorPicker">
                  <span class="color-dot" :style="{ backgroundColor: danmakuColor }"></span>
                </button>
                <div class="mobile-color-panel" v-if="showColorPicker">
                  <div 
                    v-for="option in colorOptions" 
                    :key="option.color"
                    class="mobile-color-option"
                    :class="{ active: danmakuColor === option.color }"
                    :style="{ backgroundColor: option.color }"
                    @click="danmakuColor = option.color; showColorPicker = false"
                  ></div>
                </div>
              </div>
              
              <div class="mobile-font-wrapper">
                <button class="mobile-setting-btn" @click="showFontPicker = !showFontPicker">
                  T
                </button>
                <div class="mobile-font-panel" v-if="showFontPicker">
                  <button 
                    v-for="size in fontSizes" 
                    :key="size.value"
                    class="mobile-font-option"
                    :class="{ active: danmakuFontSize === size.value }"
                    @click="danmakuFontSize = size.value; showFontPicker = false"
                  >
                    {{ size.label }}
                  </button>
                </div>
              </div>
            </div>
            
            <div class="danmaku-input-wrapper">
              <input 
                type="text" 
                v-model="danmakuContent" 
                placeholder="发送弹幕..."
                @keyup.enter="sendDanmaku"
              />
              <button class="send-btn" @click="sendDanmaku">发送</button>
            </div>
          </div>
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
        
        <template v-if="!isMobile">
          <div class="live-interaction">
            <div class="gift-section">
              <div class="gift-header">
                <span class="gift-title">🎁 礼物</span>
              </div>
              <!-- 热门礼物 -->
              <div v-if="hotGiftList.length > 0" class="hot-gifts-section">
                <div class="hot-gifts-header">
                  <span class="hot-tag">🔥 热门</span>
                </div>
                <div class="gift-list hot-gift-list">
                  <div 
                    v-for="gift in hotGiftList" 
                    :key="gift.id" 
                    class="gift-item hot" 
                    :class="{ active: selectedGift?.id === gift.id }"
                    @click="selectedGift = gift"
                  >
                    <div class="hot-badge">HOT</div>
                    <GiftIcon :name="gift.name" :icon="gift.icon" size="medium" />
                    <p class="gift-name">{{ gift.name }}</p>
                    <p class="gift-price">￥{{ gift.price }}</p>
                  </div>
                </div>
              </div>
              <!-- 全部礼物 -->
              <div class="all-gifts-section">
                <div class="all-gifts-header">
                  <span class="all-tag">全部礼物</span>
                  <span class="sort-hint">（按价格从低到高）</span>
                </div>
                <div class="gift-list">
                  <div 
                    v-for="gift in giftList" 
                    :key="gift.id" 
                    class="gift-item" 
                    :class="{ active: selectedGift?.id === gift.id, hot: gift.isHot }"
                    @click="selectedGift = gift"
                  >
                    <div v-if="gift.isHot" class="hot-badge">HOT</div>
                    <GiftIcon :name="gift.name" :icon="gift.icon" size="medium" />
                    <p class="gift-name">{{ gift.name }}</p>
                    <p class="gift-price">￥{{ gift.price }}</p>
                  </div>
                </div>
              </div>
              <div class="gift-send" v-if="selectedGift">
                <div class="gift-count">
                  <button class="count-btn" @click="giftCount > 1 && giftCount--">-</button>
                  <input 
                    type="number" 
                    v-model.number="giftCount" 
                    min="1" 
                    max="999"
                    class="count-input"
                    @blur="giftCount = Math.max(1, Math.min(999, giftCount || 1))"
                  />
                  <button class="count-btn" @click="giftCount < 999 && giftCount++">+</button>
                </div>
                <button class="send-gift-button" @click="sendGift">
                  发送 (￥{{ (selectedGift.price * giftCount).toFixed(2) }})
                </button>
              </div>
            </div>
            
            <div class="comment-section">
              <div class="comment-section-header">
                <span class="comment-title">💬 评论区</span>
                <span class="comment-count">{{ commentTotal }}条评论</span>
              </div>
              <div class="comment-section-list">
                <div 
                  v-for="comment in commentList.slice(0, 5)" 
                  :key="comment.id"
                  class="comment-section-item"
                >
                  <div class="comment-section-avatar">
                    <img v-if="comment.userAvatar" :src="comment.userAvatar" alt="头像" />
                    <span v-else>{{ comment.userNickname?.charAt(0) || 'U' }}</span>
                  </div>
                  <div class="comment-section-content">
                    <div class="comment-section-header-row">
                      <span class="comment-section-nickname">{{ comment.userNickname || '用户' + comment.userId }}</span>
                      <span class="comment-section-time">{{ formatTime(comment.createdAt) }}</span>
                    </div>
                    <p class="comment-section-text">{{ comment.content }}</p>
                  </div>
                </div>
                <div v-if="commentList.length === 0" class="comment-section-empty">
                  暂无评论
                </div>
                <div v-if="commentTotal > 5" class="view-all-comments">
                  <button @click="showCommentPanel = true">查看全部 {{ commentTotal }} 条评论</button>
                </div>
              </div>
              <div class="comment-section-input">
                <div class="comment-input-avatar">
                  <img v-if="userInfo?.avatar" :src="userInfo.avatar" alt="avatar" />
                  <span v-else>{{ userInfo?.nickname?.charAt(0) || 'U' }}</span>
                </div>
                <input 
                  type="text" 
                  v-model="commentContent" 
                  placeholder="有爱评论，说点儿好听的~"
                  @keyup.enter="sendComment"
                />
                <button class="comment-send-btn" @click="sendComment">发送</button>
              </div>
            </div>
          </div>
        </template>
      </div>
    </div>
    
    <div class="gift-panel" :class="{ active: showGiftPanel }">
      <div class="gift-panel-overlay" @click="showGiftPanel = false"></div>
      <div class="gift-panel-content">
        <div class="gift-panel-header">
          <span class="gift-panel-title">选择礼物</span>
          <button class="gift-panel-close" @click="showGiftPanel = false">×</button>
        </div>
        <!-- 热门礼物 -->
        <div v-if="hotGiftList.length > 0" class="gift-panel-hot-section">
          <div class="gift-panel-section-title">
            <span class="hot-tag">🔥 热门礼物</span>
          </div>
          <div class="gift-panel-list">
            <div 
              v-for="gift in hotGiftList" 
              :key="gift.id"
              class="gift-panel-item hot" 
              :class="{ active: selectedGift?.id === gift.id }"
              @click="selectedGift = gift"
            >
              <div class="hot-badge">HOT</div>
              <GiftIcon :name="gift.name" :icon="gift.icon" size="medium" />
              <p class="gift-name">{{ gift.name }}</p>
              <p class="gift-price">￥{{ gift.price }}</p>
            </div>
          </div>
        </div>
        <!-- 全部礼物 -->
        <div class="gift-panel-all-section">
          <div class="gift-panel-section-title">
            <span>全部礼物</span>
            <span class="sort-hint">（按价格从低到高）</span>
          </div>
          <div class="gift-panel-list">
            <div 
              v-for="gift in giftList" 
              :key="gift.id"
              class="gift-panel-item" 
              :class="{ active: selectedGift?.id === gift.id, hot: gift.isHot }"
              @click="selectedGift = gift"
            >
              <div v-if="gift.isHot" class="hot-badge">HOT</div>
              <GiftIcon :name="gift.name" :icon="gift.icon" size="medium" />
              <p class="gift-name">{{ gift.name }}</p>
              <p class="gift-price">￥{{ gift.price }}</p>
            </div>
          </div>
        </div>
        <div class="gift-panel-footer" v-if="selectedGift">
          <div class="gift-count-selector">
            <button class="count-btn" @click="giftCount > 1 && giftCount--">-</button>
            <input 
              type="number" 
              v-model.number="giftCount" 
              min="1" 
              max="999"
              class="count-input"
              @blur="giftCount = Math.max(1, Math.min(999, giftCount || 1))"
            />
            <button class="count-btn" @click="giftCount < 999 && giftCount++">+</button>
          </div>
          <button class="gift-send-button" @click="sendGift(); showGiftPanel = false">
            发送 (￥{{ (selectedGift.price * giftCount).toFixed(2) }})
          </button>
        </div>
      </div>
    </div>
    
    <div class="comment-panel" :class="{ active: showCommentPanel }">
      <div class="comment-panel-overlay" @click="showCommentPanel = false"></div>
      <div class="comment-panel-content">
        <div class="comment-panel-header">
          <span class="comment-panel-title">💬 评论区</span>
          <button class="comment-panel-close" @click="showCommentPanel = false">×</button>
        </div>
        <div class="comment-list" v-if="commentList.length > 0">
          <div 
            v-for="comment in commentList" 
            :key="comment.id"
            class="comment-item"
          >
            <div class="comment-avatar">
              <img v-if="comment.userAvatar" :src="comment.userAvatar" alt="头像" />
              <span v-else class="avatar-placeholder">{{ comment.userNickname?.charAt(0) || 'U' }}</span>
            </div>
            <div class="comment-content">
              <div class="comment-header">
                <span class="comment-nickname">{{ comment.userNickname || '用户' + comment.userId }}</span>
                <span class="comment-time">{{ formatTime(comment.createdAt) }}</span>
              </div>
              <p class="comment-text">{{ comment.content }}</p>
              <div class="comment-actions">
                <button 
                  class="like-comment-btn" 
                  :class="{ active: comment.isLiked }"
                  @click="likeComment(comment.id)"
                >
                  👍 {{ comment.likeCount || 0 }}
                </button>
              </div>
            </div>
          </div>
        </div>
        <div v-else class="comment-empty">
          暂无评论，快来发表第一条评论吧~
        </div>
        <div v-if="commentList.length < commentTotal && !commentLoading" class="load-more">
          <button @click="loadMoreComments">加载更多</button>
        </div>
        <div v-if="commentLoading" class="comment-loading">加载中...</div>
        <div class="comment-input-section">
          <input 
            type="text" 
            v-model="commentContent" 
            placeholder="发表评论..."
            @keyup.enter="sendComment"
          />
          <button class="send-comment-button" @click="sendComment">发送</button>
        </div>
      </div>
    </div>
    
    <div class="report-panel" :class="{ active: showReportPanel }">
      <div class="report-panel-overlay" @click="showReportPanel = false"></div>
      <div class="report-panel-content">
        <div class="report-panel-header">
          <span class="report-panel-title">举报直播间</span>
          <button class="report-panel-close" @click="showReportPanel = false">×</button>
        </div>
        <div class="report-panel-body">
          <div class="report-reason-title">请选择举报原因</div>
          <div class="report-reason-list">
            <label 
              v-for="reason in reportReasons" 
              :key="reason.value"
              class="report-reason-item"
            >
              <input 
                type="radio" 
                :value="reason.value" 
                v-model="reportReason"
              />
              <span class="reason-label">{{ reason.label }}</span>
            </label>
          </div>
          <div class="report-description-wrapper">
            <label class="report-description-label">补充说明（选填）</label>
            <textarea 
              v-model="reportDescription" 
              placeholder="请补充举报的具体内容..."
              rows="4"
              maxlength="500"
            ></textarea>
            <span class="char-count">{{ reportDescription.length }}/500</span>
          </div>
        </div>
        <div class="report-panel-footer">
          <button class="cancel-report-btn" @click="showReportPanel = false">取消</button>
          <button class="submit-report-btn" @click="submitReport">提交举报</button>
        </div>
      </div>
    </div>

    <!-- 分享对话框 -->
    <ShareDialog
      v-model:visible="showShareDialog"
      :share-data="shareData"
      @share="handleShareSuccess"
    />
  </div>
</template>

<style scoped>
* {
  box-sizing: border-box;
}

.live-room-container {
  width: 100%;
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #000;
  color: #fff;
  border-radius: 20px;
  overflow: hidden;
}

.live-room-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 24px;
  background: linear-gradient(180deg, rgba(0,0,0,0.8) 0%, transparent 100%);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-right {
  display: flex;
  gap: 12px;
}

.back-button {
  width: 40px;
  height: 40px;
  background: rgba(255, 255, 255, 0.1);
  border: none;
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.back-button:hover {
  background: rgba(255, 255, 255, 0.2);
  transform: scale(1.1);
}

.back-icon {
  font-size: 24px;
  font-weight: bold;
  color: #fff;
}

.host-avatar {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  overflow: hidden;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  border: 2px solid #fff;
}

.host-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-placeholder {
  color: #fff;
  font-size: 18px;
  font-weight: 600;
}

.live-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.host-name {
  display: flex;
  align-items: center;
  gap: 8px;
}

.nickname {
  font-size: 16px;
  font-weight: 600;
  color: #fff;
}

.live-badge-small {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 3px 10px;
  background: linear-gradient(135deg, #ff4757 0%, #ff6b81 100%);
  color: white;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.live-dot-small {
  width: 6px;
  height: 6px;
  background: #fff;
  border-radius: 50%;
  animation: pulse 1.5s infinite;
}

.view-count-small {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.7);
}

.follow-button {
  padding: 8px 20px;
  background: linear-gradient(135deg, #ff4757 0%, #ff6b81 100%);
  border: none;
  border-radius: 20px;
  color: #fff;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.follow-button:hover {
  transform: scale(1.05);
  box-shadow: 0 4px 15px rgba(255, 71, 87, 0.4);
}

.follow-button.followed {
  background: linear-gradient(135deg, #52c41a 0%, #73d13d 100%);
}

.follow-button.followed:hover {
  box-shadow: 0 4px 15px rgba(82, 196, 26, 0.4);
}

.share-button {
  padding: 8px 20px;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 20px;
  color: #fff;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.share-button:hover {
  background: rgba(255, 255, 255, 0.2);
}

.report-button {
  padding: 8px 20px;
  background: rgba(255, 71, 87, 0.2);
  border: 1px solid rgba(255, 71, 87, 0.4);
  border-radius: 20px;
  color: #ff4757;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.report-button:hover {
  background: rgba(255, 71, 87, 0.3);
}

@keyframes pulse {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.5; transform: scale(1.2); }
}

.live-room-content {
  flex: 1;
  display: flex;
  overflow: hidden;
}

.loading, .empty {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  width: 100%;
  gap: 16px;
  font-size: 16px;
  color: rgba(255, 255, 255, 0.6);
}

.loading-spinner {
  width: 50px;
  height: 50px;
  border: 4px solid rgba(255, 255, 255, 0.1);
  border-top-color: #ff4757;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.live-room {
  display: flex;
  width: 100%;
  height: 100%;
}

.live-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  position: relative;
  background: #000;
}

.live-stream {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
  background: #000;
  overflow: hidden;
  position: relative;
}

.live-video {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.stream-loading {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  gap: 16px;
  background-color: rgba(0, 0, 0, 0.7);
  color: #fff;
  z-index: 10;
}

.stream-loading .loading-spinner {
  width: 48px;
  height: 48px;
  border: 4px solid rgba(255, 255, 255, 0.3);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.live-offline {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  gap: 16px;
  background-color: rgba(0, 0, 0, 0.85);
  color: #fff;
  backdrop-filter: blur(10px);
}

.offline-icon {
  font-size: 64px;
  opacity: 0.8;
}

.live-offline p {
  margin: 0;
  font-size: 18px;
  font-weight: 500;
}

.desktop-danmaku-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(10px);
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.desktop-danmaku-bar .danmaku-toggle-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 8px 16px;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 20px;
  color: rgba(255, 255, 255, 0.8);
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.desktop-danmaku-bar .danmaku-toggle-btn:hover {
  background: rgba(255, 255, 255, 0.15);
}

.desktop-danmaku-bar .danmaku-toggle-btn.active {
  background: linear-gradient(135deg, #FE2C55 0%, #FF4373 100%);
  border-color: #FE2C55;
  color: #fff;
}

.desktop-danmaku-bar .btn-icon {
  font-size: 12px;
}

.desktop-danmaku-bar .danmaku-color-wrapper,
.desktop-danmaku-bar .danmaku-font-wrapper {
  position: relative;
}

.desktop-danmaku-bar .danmaku-setting-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 8px 12px;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 20px;
  color: rgba(255, 255, 255, 0.8);
  font-size: 13px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.desktop-danmaku-bar .danmaku-setting-btn:hover {
  background: rgba(255, 255, 255, 0.15);
}

.desktop-danmaku-bar .color-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.desktop-danmaku-bar .font-icon {
  font-weight: 600;
  font-size: 12px;
}

.desktop-danmaku-bar .color-picker-panel,
.desktop-danmaku-bar .font-picker-panel {
  position: absolute;
  bottom: 100%;
  left: 0;
  margin-bottom: 8px;
  padding: 12px;
  background: rgba(0, 0, 0, 0.9);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 12px;
  backdrop-filter: blur(10px);
  z-index: 100;
}

.desktop-danmaku-bar .color-picker-panel {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 8px;
}

.desktop-danmaku-bar .color-option {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  cursor: pointer;
  border: 2px solid transparent;
  transition: all 0.2s ease;
}

.desktop-danmaku-bar .color-option:hover {
  transform: scale(1.1);
}

.desktop-danmaku-bar .color-option.active {
  border-color: #fff;
  box-shadow: 0 0 8px rgba(255, 255, 255, 0.5);
}

.desktop-danmaku-bar .font-picker-panel {
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 60px;
}

.desktop-danmaku-bar .font-option {
  padding: 6px 12px;
  background: transparent;
  border: none;
  border-radius: 8px;
  color: rgba(255, 255, 255, 0.8);
  cursor: pointer;
  transition: all 0.2s ease;
  text-align: center;
}

.desktop-danmaku-bar .font-option:hover {
  background: rgba(255, 255, 255, 0.1);
}

.desktop-danmaku-bar .font-option.active {
  background: linear-gradient(135deg, #FE2C55 0%, #FF4373 100%);
  color: #fff;
}

.desktop-danmaku-bar .danmaku-input-area {
  flex: 1;
}

.desktop-danmaku-bar .danmaku-input-area input {
  width: 100%;
  padding: 8px 16px;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 20px;
  color: #fff;
  font-size: 14px;
  outline: none;
  transition: all 0.3s ease;
}

.desktop-danmaku-bar .danmaku-input-area input::placeholder {
  color: rgba(255, 255, 255, 0.4);
}

.desktop-danmaku-bar .danmaku-input-area input:focus {
  background: rgba(255, 255, 255, 0.15);
  border-color: rgba(255, 255, 255, 0.4);
}

.desktop-danmaku-bar .send-danmaku-btn {
  padding: 8px 20px;
  background: linear-gradient(135deg, #FE2C55 0%, #FF4373 100%);
  border: none;
  border-radius: 20px;
  color: #fff;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.desktop-danmaku-bar .send-danmaku-btn:hover {
  transform: scale(1.05);
  box-shadow: 0 4px 12px rgba(254, 44, 85, 0.4);
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

.gift-effect-content .gift-icon {
  width: 60px;
  height: 60px;
  flex-shrink: 0;
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
  width: 380px;
  display: flex;
  flex-direction: column;
  background: linear-gradient(180deg, #1a1a2e 0%, #16213e 100%);
  border-left: 1px solid rgba(255, 255, 255, 0.1);
  height: 100%;
  overflow: hidden;
}

.danmaku-control-section {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background: rgba(0, 0, 0, 0.2);
  border-radius: 8px;
  margin-bottom: 16px;
}

.danmaku-label {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.8);
}

.danmaku-toggle-btn {
  padding: 6px 16px;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 16px;
  color: rgba(255, 255, 255, 0.6);
  font-size: 13px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.danmaku-toggle-btn:hover {
  background: rgba(255, 255, 255, 0.15);
}

.danmaku-toggle-btn.active {
  background: linear-gradient(135deg, #FE2C55 0%, #FF4373 100%);
  border-color: #FE2C55;
  color: #fff;
}

.gift-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding-bottom: 12px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.gift-title {
  font-size: 15px;
  font-weight: 600;
  color: #fff;
}

.gift-section {
  flex-shrink: 0;
  padding: 16px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.gift-list {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  overflow-y: auto;
  padding: 4px;
  max-height: 180px;
}

.gift-list::-webkit-scrollbar {
  width: 4px;
}

.gift-list::-webkit-scrollbar-track {
  background: rgba(255, 255, 255, 0.05);
}

.gift-list::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.2);
  border-radius: 2px;
}

.gift-item {
  border: 2px solid rgba(255, 255, 255, 0.1);
  border-radius: 8px;
  padding: 5px 4px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  background: rgba(255, 255, 255, 0.05);
  display: flex;
  flex-direction: column;
  align-items: center;
}

.gift-item:hover {
  border-color: #FE2C55;
  background: rgba(254, 44, 85, 0.1);
  transform: translateY(-2px);
}

.gift-item.active {
  border-color: #FE2C55;
  background: rgba(254, 44, 85, 0.2);
  box-shadow: 0 0 0 2px rgba(254, 44, 85, 0.3);
}

.gift-item.hot {
  border-color: rgba(255, 107, 107, 0.5);
  background: linear-gradient(135deg, rgba(255, 107, 107, 0.15) 0%, rgba(255, 142, 83, 0.1) 100%);
  position: relative;
  overflow: hidden;
}

.gift-item.hot:hover {
  border-color: #FF6B6B;
  background: linear-gradient(135deg, rgba(255, 107, 107, 0.25) 0%, rgba(255, 142, 83, 0.2) 100%);
}

.hot-badge {
  position: absolute;
  top: 2px;
  right: 2px;
  background: linear-gradient(135deg, #FF6B6B 0%, #FF8E53 100%);
  color: white;
  font-size: 8px;
  font-weight: bold;
  padding: 1px 4px;
  border-radius: 4px;
  z-index: 1;
}

.hot-gifts-section {
  margin-bottom: 12px;
  padding-bottom: 12px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.hot-gifts-header {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}

.hot-tag {
  font-size: 12px;
  font-weight: 600;
  color: #FF6B6B;
}

.all-gifts-section {
  margin-top: 8px;
}

.all-gifts-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.all-tag {
  font-size: 12px;
  font-weight: 500;
  color: rgba(255, 255, 255, 0.8);
}

.sort-hint {
  font-size: 10px;
  color: rgba(255, 255, 255, 0.5);
}

.hot-gift-list {
  max-height: 100px;
}

.gift-item .gift-icon {
  width: 20px !important;
  height: 20px !important;
  margin-bottom: 3px;
}

.gift-item img {
  width: 20px;
  height: 20px;
  object-fit: contain;
  margin: 0 auto 3px;
  display: block;
}

.gift-name {
  margin: 0;
  font-size: 9px;
  font-weight: 500;
  color: rgba(255, 255, 255, 0.9);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.gift-price {
  margin: 2px 0 0 0;
  font-size: 9px;
  color: #FFD700;
  font-weight: 600;
}

.gift-send {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.gift-count {
  display: flex;
  align-items: center;
  gap: 8px;
}

.count-btn {
  width: 28px;
  height: 28px;
  border: none;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  color: #fff;
  font-size: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.count-btn:hover {
  background: rgba(255, 255, 255, 0.2);
}

.count-value {
  min-width: 24px;
  text-align: center;
  font-size: 14px;
  font-weight: 600;
  color: #fff;
}

.count-input {
  width: 50px;
  height: 28px;
  text-align: center;
  font-size: 14px;
  font-weight: 600;
  color: #fff;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 6px;
  outline: none;
  transition: all 0.3s ease;
  -moz-appearance: textfield;
}

.count-input::-webkit-outer-spin-button,
.count-input::-webkit-inner-spin-button {
  -webkit-appearance: none;
  margin: 0;
}

.count-input:focus {
  border-color: #FE2C55;
  background: rgba(255, 255, 255, 0.15);
}

.send-gift-button {
  flex: 1;
  padding: 10px 16px;
  background: linear-gradient(135deg, #FE2C55 0%, #FF4373 100%);
  color: white;
  border: none;
  border-radius: 20px;
  cursor: pointer;
  font-size: 13px;
  font-weight: 600;
  transition: all 0.3s ease;
}

.send-gift-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(254, 44, 85, 0.4);
}

.danmaku-input-section {
  flex-shrink: 0;
  padding: 16px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  background: rgba(0, 0, 0, 0.3);
}

.color-picker {
  display: flex;
  gap: 8px;
  padding: 12px;
  background: rgba(0, 0, 0, 0.3);
  border-radius: 8px;
  margin-bottom: 12px;
}

.color-option {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  cursor: pointer;
  border: 2px solid transparent;
  transition: all 0.3s ease;
}

.color-option.active {
  border-color: #fff;
  transform: scale(1.2);
}

.color-option:hover {
  transform: scale(1.15);
}

.input-wrapper {
  display: flex;
  align-items: center;
  gap: 10px;
}

.color-toggle {
  width: 36px;
  height: 36px;
  border: none;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.color-toggle:hover {
  background: rgba(255, 255, 255, 0.2);
}

.color-preview {
  width: 18px;
  height: 18px;
  border-radius: 50%;
  border: 2px solid rgba(255, 255, 255, 0.3);
}

.input-wrapper input {
  flex: 1;
  padding: 10px 16px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 20px;
  font-size: 14px;
  background: rgba(255, 255, 255, 0.1);
  color: #fff;
  transition: all 0.3s ease;
}

.input-wrapper input::placeholder {
  color: rgba(255, 255, 255, 0.4);
}

.input-wrapper input:focus {
  outline: none;
  border-color: #FE2C55;
  background: rgba(255, 255, 255, 0.15);
}

.send-button {
  padding: 10px 20px;
  background: linear-gradient(135deg, #FE2C55 0%, #FF4373 100%);
  color: white;
  border: none;
  border-radius: 20px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
  transition: all 0.3s ease;
  white-space: nowrap;
}

.comment-section {
  flex: 1;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  background: rgba(0, 0, 0, 0.2);
  display: flex;
  flex-direction: column;
  min-height: 0;
  position: relative;
}

.comment-section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
}

.comment-title {
  font-size: 14px;
  font-weight: 600;
  color: #fff;
}

.comment-count {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.5);
}

.comment-section-list {
  flex: 1;
  overflow-y: auto;
  padding: 12px 16px;
  padding-bottom: 70px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.comment-section-item {
  display: flex;
  gap: 10px;
}

.comment-section-avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  overflow: hidden;
  flex-shrink: 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 600;
  color: #fff;
}

.comment-section-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.comment-section-content {
  flex: 1;
  min-width: 0;
}

.comment-section-header-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 2px;
}

.comment-section-nickname {
  font-size: 12px;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.9);
}

.comment-section-time {
  font-size: 10px;
  color: rgba(255, 255, 255, 0.4);
}

.comment-section-text {
  margin: 0;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.7);
  line-height: 1.4;
  word-break: break-word;
}

.comment-section-empty {
  text-align: center;
  color: rgba(255, 255, 255, 0.4);
  font-size: 12px;
  padding: 16px;
}

.view-all-comments {
  text-align: center;
  padding: 8px 0;
}

.view-all-comments button {
  padding: 6px 16px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 12px;
  background: transparent;
  color: rgba(255, 255, 255, 0.6);
  font-size: 11px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.view-all-comments button:hover {
  border-color: #FE2C55;
  color: #FE2C55;
}

.comment-section-input {
  position: absolute;
  bottom: 16px;
  left: 16px;
  right: 16px;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 16px;
  border-radius: 24px;
  background: rgba(0, 0, 0, 0.8);
  z-index: 10;
}

.comment-input-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  overflow: hidden;
  flex-shrink: 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 600;
  color: #fff;
}

.comment-input-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.comment-section-input input {
  flex: 1;
  padding: 8px 14px;
  border: 1px solid rgba(255, 255, 255, 0.15);
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.05);
  color: #fff;
  font-size: 12px;
  outline: none;
  transition: all 0.3s ease;
}

.comment-section-input input::placeholder {
  color: rgba(255, 255, 255, 0.3);
}

.comment-section-input input:focus {
  border-color: #FE2C55;
  background: rgba(255, 255, 255, 0.1);
}

.comment-send-btn {
  padding: 8px 18px;
  border: none;
  border-radius: 16px;
  background: linear-gradient(135deg, #FE2C55 0%, #FF4373 100%);
  color: #fff;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.comment-send-btn:hover {
  transform: scale(1.02);
}

.send-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(254, 44, 85, 0.4);
}

/* 移动端适配 - 抖音风格 */
@media (max-width: 768px) {
  .live-room-container.mobile {
    background: #000;
  }

  .live-room-header {
    display: none;
  }

  .live-room-content {
    position: relative;
  }

  .live-room {
    flex-direction: column;
  }

  .live-main {
    width: 100%;
    height: 100vh;
  }

  .live-stream {
    width: 100%;
    height: 100%;
  }

  .live-video {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  .live-offline {
    font-size: 16px;
  }

  .mobile-header {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    z-index: 10;
    display: flex;
    align-items: center;
    padding: 12px 16px;
    background: linear-gradient(180deg, rgba(0,0,0,0.6) 0%, transparent 100%);
  }

  .mobile-header .back-button {
    width: 32px;
    height: 32px;
    background: rgba(0, 0, 0, 0.4);
  }

  .mobile-header .back-icon {
    font-size: 18px;
  }

  .mobile-host-info {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-left: 12px;
  }

  .host-avatar-small {
    width: 28px;
    height: 28px;
    border-radius: 50%;
    overflow: hidden;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border: 2px solid #fff;
  }

  .host-avatar-small img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  .host-avatar-small .avatar-placeholder {
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    font-size: 12px;
    font-weight: 600;
  }

  .host-name-small {
    font-size: 14px;
    font-weight: 600;
    color: #fff;
    text-shadow: 0 1px 2px rgba(0,0,0,0.5);
  }

  .live-tag {
    padding: 2px 8px;
    background: linear-gradient(135deg, #ff4757 0%, #ff6b81 100%);
    border-radius: 10px;
    font-size: 11px;
    font-weight: 500;
    color: #fff;
  }

  .mobile-sidebar {
    position: absolute;
    right: 12px;
    bottom: 120px;
    display: flex;
    flex-direction: column;
    gap: 20px;
    z-index: 20;
  }

  .sidebar-btn {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 4px;
    background: transparent;
    border: none;
    cursor: pointer;
    padding: 8px 4px;
    transition: transform 0.2s ease;
  }

  .sidebar-btn:active {
    transform: scale(0.95);
  }

  .sidebar-btn.like-btn.active {
    animation: likePop 0.3s ease;
  }

  @keyframes likePop {
    0% { transform: scale(1); }
    50% { transform: scale(1.3); }
    100% { transform: scale(1); }
  }

  .btn-icon {
    font-size: 28px;
    filter: drop-shadow(0 2px 4px rgba(0,0,0,0.5));
  }

  .btn-text {
    font-size: 11px;
    color: #fff;
    text-shadow: 0 1px 2px rgba(0,0,0,0.5);
  }

  .mobile-bottom {
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    z-index: 10;
    padding: 16px;
    background: linear-gradient(0deg, rgba(0,0,0,0.8) 0%, transparent 100%);
  }

  .mobile-danmaku-controls {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 10px;
  }

  .mobile-danmaku-toggle {
    padding: 6px 12px;
    background: rgba(255, 255, 255, 0.15);
    border: 1px solid rgba(255, 255, 255, 0.3);
    border-radius: 16px;
    color: rgba(255, 255, 255, 0.8);
    font-size: 12px;
    cursor: pointer;
    transition: all 0.3s ease;
  }

  .mobile-danmaku-toggle.active {
    background: linear-gradient(135deg, #FE2C55 0%, #FF4373 100%);
    border-color: #FE2C55;
    color: #fff;
  }

  .mobile-color-wrapper,
  .mobile-font-wrapper {
    position: relative;
  }

  .mobile-setting-btn {
    width: 32px;
    height: 32px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: rgba(255, 255, 255, 0.15);
    border: 1px solid rgba(255, 255, 255, 0.3);
    border-radius: 50%;
    color: #fff;
    font-size: 14px;
    cursor: pointer;
  }

  .mobile-setting-btn .color-dot {
    width: 16px;
    height: 16px;
    border-radius: 50%;
    border: 2px solid rgba(255, 255, 255, 0.5);
  }

  .mobile-color-panel {
    position: absolute;
    bottom: 40px;
    left: 50%;
    transform: translateX(-50%);
    display: flex;
    gap: 6px;
    padding: 8px;
    background: rgba(0, 0, 0, 0.9);
    border-radius: 8px;
    z-index: 100;
  }

  .mobile-color-option {
    width: 24px;
    height: 24px;
    border-radius: 50%;
    cursor: pointer;
    border: 2px solid transparent;
    transition: all 0.2s ease;
  }

  .mobile-color-option.active {
    border-color: #fff;
    transform: scale(1.1);
  }

  .mobile-font-panel {
    position: absolute;
    bottom: 40px;
    left: 50%;
    transform: translateX(-50%);
    display: flex;
    flex-direction: column;
    gap: 4px;
    padding: 6px;
    background: rgba(0, 0, 0, 0.9);
    border-radius: 8px;
    z-index: 100;
    min-width: 60px;
  }

  .mobile-font-option {
    padding: 6px 10px;
    background: transparent;
    border: none;
    color: rgba(255, 255, 255, 0.8);
    font-size: 12px;
    cursor: pointer;
    border-radius: 4px;
    transition: all 0.2s ease;
  }

  .mobile-font-option:hover,
  .mobile-font-option.active {
    background: rgba(255, 255, 255, 0.2);
    color: #fff;
  }

  .danmaku-overlay {
    margin-bottom: 12px;
    max-height: 100px;
    overflow: hidden;
  }

  .danmaku-overlay-item {
    padding: 4px 10px;
    background: rgba(0, 0, 0, 0.4);
    border-radius: 12px;
    margin-bottom: 6px;
    font-size: 12px;
    animation: danmakuIn 0.3s ease;
  }

  @keyframes danmakuIn {
    from {
      opacity: 0;
      transform: translateY(10px);
    }
    to {
      opacity: 1;
      transform: translateY(0);
    }
  }

  .danmaku-overlay-item .danmaku-user {
    color: #FFD700;
    font-weight: 600;
    margin-right: 6px;
  }

  .danmaku-overlay-item .danmaku-text {
    color: #fff;
  }

  .danmaku-input-wrapper {
    display: flex;
    gap: 10px;
    align-items: center;
  }

  .danmaku-input-wrapper input {
    flex: 1;
    padding: 10px 16px;
    border: none;
    border-radius: 20px;
    background: rgba(255, 255, 255, 0.15);
    color: #fff;
    font-size: 14px;
    backdrop-filter: blur(10px);
  }

  .danmaku-input-wrapper input::placeholder {
    color: rgba(255, 255, 255, 0.5);
  }

  .danmaku-input-wrapper input:focus {
    outline: none;
    background: rgba(255, 255, 255, 0.2);
  }

  .send-btn {
    padding: 10px 20px;
    background: linear-gradient(135deg, #FE2C55 0%, #FF4373 100%);
    border: none;
    border-radius: 20px;
    color: #fff;
    font-size: 14px;
    font-weight: 600;
    cursor: pointer;
  }

  .gift-effect {
    bottom: 120px;
    left: 16px;
    max-width: 200px;
  }

  .gift-effect-content {
    padding: 8px 12px;
    flex-direction: column;
    align-items: flex-start;
    gap: 6px;
  }

  .gift-effect-content img {
    width: 36px;
    height: 36px;
  }

  .gift-user {
    font-size: 11px;
  }

  .gift-effect-text .gift-name {
    font-size: 12px;
  }
}

@media (max-width: 480px) {
  .mobile-header {
    padding: 10px 12px;
  }

  .mobile-sidebar {
    right: 8px;
    bottom: 100px;
    gap: 16px;
  }

  .btn-icon {
    font-size: 24px;
  }

  .btn-text {
    font-size: 10px;
  }

  .mobile-bottom {
    padding: 12px;
  }

  .danmaku-overlay {
    max-height: 150px;
  }

  .danmaku-overlay-item {
    font-size: 11px;
    padding: 2px 6px;
    margin-bottom: 4px;
  }

  .danmaku-overlay-item:last-child {
    margin-bottom: 0;
  }

  .danmaku-input-wrapper input {
    padding: 8px 14px;
    font-size: 13px;
  }

  .send-btn {
    padding: 8px 16px;
    font-size: 13px;
  }
}

.gift-panel {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 1000;
  visibility: hidden;
  transition: visibility 0.3s;
}

.gift-panel.active {
  visibility: visible;
}

.gift-panel-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  opacity: 0;
  transition: opacity 0.3s;
}

.gift-panel.active .gift-panel-overlay {
  opacity: 1;
}

.gift-panel-content {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: linear-gradient(180deg, #1a1a2e 0%, #16213e 100%);
  border-radius: 16px 16px 0 0;
  transform: translateY(100%);
  transition: transform 0.3s ease;
}

.gift-panel.active .gift-panel-content {
  transform: translateY(0);
}

.gift-panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.gift-panel-title {
  font-size: 16px;
  font-weight: 600;
  color: #fff;
}

.gift-panel-close {
  width: 32px;
  height: 32px;
  border: none;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  color: #fff;
  font-size: 20px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

.gift-panel-list {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  padding: 16px;
  max-height: 200px;
  overflow-y: auto;
}

.gift-panel-item {
  border: 2px solid rgba(255, 255, 255, 0.1);
  border-radius: 8px;
  padding: 12px 8px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  background: rgba(255, 255, 255, 0.05);
}

.gift-panel-item:hover,
.gift-panel-item.active {
  border-color: #FE2C55;
  background: rgba(254, 44, 85, 0.2);
}

.gift-panel-item.hot {
  border-color: rgba(255, 107, 107, 0.5);
  background: linear-gradient(135deg, rgba(255, 107, 107, 0.15) 0%, rgba(255, 142, 83, 0.1) 100%);
  position: relative;
  overflow: hidden;
}

.gift-panel-item.hot:hover,
.gift-panel-item.hot.active {
  border-color: #FF6B6B;
  background: linear-gradient(135deg, rgba(255, 107, 107, 0.25) 0%, rgba(255, 142, 83, 0.2) 100%);
}

.gift-panel-hot-section {
  padding: 12px 16px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.gift-panel-hot-section .gift-panel-list {
  padding: 8px 0;
  max-height: 120px;
}

.gift-panel-all-section {
  padding: 12px 16px;
}

.gift-panel-all-section .gift-panel-list {
  padding: 8px 0;
}

.gift-panel-section-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: 13px;
  font-weight: 500;
  color: rgba(255, 255, 255, 0.8);
}

.gift-panel-section-title .hot-tag {
  color: #FF6B6B;
  font-weight: 600;
}

.gift-panel-section-title .sort-hint {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.5);
}

.gift-panel-item img {
  width: 40px;
  height: 40px;
  object-fit: contain;
  margin-bottom: 6px;
}

.gift-panel-item .gift-name {
  margin: 0;
  font-size: 11px;
  font-weight: 500;
  color: rgba(255, 255, 255, 0.9);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.gift-panel-item .gift-price {
  margin: 4px 0 0 0;
  font-size: 11px;
  color: #FFD700;
  font-weight: 600;
}

.gift-panel-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 16px 20px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  background: rgba(0, 0, 0, 0.3);
}

.gift-count-selector {
  display: flex;
  align-items: center;
  gap: 12px;
}

.gift-count-selector .count-btn {
  width: 32px;
  height: 32px;
  border: none;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  color: #fff;
  font-size: 18px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.gift-count-selector .count-value {
  min-width: 30px;
  text-align: center;
  font-size: 16px;
  font-weight: 600;
  color: #fff;
}

.gift-count-selector .count-input {
  width: 60px;
  height: 32px;
  text-align: center;
  font-size: 16px;
  font-weight: 600;
  color: #fff;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 8px;
  outline: none;
  transition: all 0.3s ease;
  -moz-appearance: textfield;
}

.gift-count-selector .count-input::-webkit-outer-spin-button,
.gift-count-selector .count-input::-webkit-inner-spin-button {
  -webkit-appearance: none;
  margin: 0;
}

.gift-count-selector .count-input:focus {
  border-color: #FE2C55;
  background: rgba(255, 255, 255, 0.15);
}

.gift-send-button {
  flex: 1;
  padding: 12px 24px;
  background: linear-gradient(135deg, #FE2C55 0%, #FF4373 100%);
  color: white;
  border: none;
  border-radius: 24px;
  cursor: pointer;
  font-size: 15px;
  font-weight: 600;
}

.comment-panel {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 1000;
  visibility: hidden;
  transition: visibility 0.3s ease;
}

.comment-panel.active {
  visibility: visible;
}

.comment-panel-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  opacity: 0;
  transition: opacity 0.3s ease;
}

.comment-panel.active .comment-panel-overlay {
  opacity: 1;
}

.comment-panel-content {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: linear-gradient(180deg, #1a1a2e 0%, #16213e 100%);
  border-radius: 16px 16px 0 0;
  transform: translateY(100%);
  transition: transform 0.3s ease;
  display: flex;
  flex-direction: column;
  max-height: 80vh;
}

.comment-panel.active .comment-panel-content {
  transform: translateY(0);
}

.comment-panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  flex-shrink: 0;
}

.comment-panel-title {
  font-size: 16px;
  font-weight: 600;
  color: #fff;
}

.comment-panel-close {
  width: 32px;
  height: 32px;
  border: none;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  color: #fff;
  font-size: 20px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

.comment-panel-close:hover {
  background: rgba(255, 255, 255, 0.2);
}

.comment-list {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.comment-item {
  display: flex;
  gap: 12px;
}

.comment-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  overflow: hidden;
  flex-shrink: 0;
  background: rgba(255, 255, 255, 0.1);
}

.comment-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.comment-avatar .avatar-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  font-weight: 600;
  color: #fff;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.comment-content {
  flex: 1;
  min-width: 0;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 6px;
}

.comment-nickname {
  font-size: 14px;
  font-weight: 600;
  color: #fff;
}

.comment-time {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.5);
}

.comment-text {
  margin: 0;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.9);
  line-height: 1.5;
  word-break: break-word;
}

.comment-actions {
  margin-top: 8px;
}

.like-comment-btn {
  padding: 4px 12px;
  border: none;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.1);
  color: rgba(255, 255, 255, 0.7);
  font-size: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.like-comment-btn:hover,
.like-comment-btn.active {
  background: rgba(254, 44, 85, 0.3);
  color: #FE2C55;
}

.comment-empty {
  padding: 40px 20px;
  text-align: center;
  color: rgba(255, 255, 255, 0.5);
  font-size: 14px;
}

.load-more {
  padding: 12px 16px;
  text-align: center;
}

.load-more button {
  padding: 8px 24px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 16px;
  background: transparent;
  color: rgba(255, 255, 255, 0.7);
  font-size: 13px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.load-more button:hover {
  border-color: #FE2C55;
  color: #FE2C55;
}

.comment-loading {
  padding: 12px 16px;
  text-align: center;
  color: rgba(255, 255, 255, 0.5);
  font-size: 13px;
}

.comment-input-section {
  display: flex;
  gap: 12px;
  padding: 16px 20px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  background: rgba(0, 0, 0, 0.3);
  flex-shrink: 0;
}

.comment-input-section input {
  flex: 1;
  padding: 10px 16px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.05);
  color: #fff;
  font-size: 14px;
  outline: none;
  transition: all 0.3s ease;
}

.comment-input-section input::placeholder {
  color: rgba(255, 255, 255, 0.4);
}

.comment-input-section input:focus {
  border-color: #FE2C55;
  background: rgba(255, 255, 255, 0.1);
}

.send-comment-button {
  padding: 10px 24px;
  border: none;
  border-radius: 20px;
  background: linear-gradient(135deg, #FE2C55 0%, #FF4373 100%);
  color: #fff;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.send-comment-button:hover {
  transform: scale(1.02);
}

.report-panel {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 1000;
  visibility: hidden;
  transition: visibility 0.3s;
}

.report-panel.active {
  visibility: visible;
}

.report-panel-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  opacity: 0;
  transition: opacity 0.3s;
}

.report-panel.active .report-panel-overlay {
  opacity: 1;
}

.report-panel-content {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: linear-gradient(180deg, #1a1a2e 0%, #16213e 100%);
  border-radius: 16px 16px 0 0;
  transform: translateY(100%);
  transition: transform 0.3s ease;
  max-height: 80vh;
  display: flex;
  flex-direction: column;
}

.report-panel.active .report-panel-content {
  transform: translateY(0);
}

.report-panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.report-panel-title {
  font-size: 16px;
  font-weight: 600;
  color: #fff;
}

.report-panel-close {
  width: 32px;
  height: 32px;
  border: none;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  color: #fff;
  font-size: 20px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

.report-panel-body {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
}

.report-reason-title {
  font-size: 14px;
  font-weight: 600;
  color: #fff;
  margin-bottom: 16px;
}

.report-reason-list {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  margin-bottom: 20px;
}

.report-reason-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 16px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.report-reason-item:hover {
  background: rgba(255, 255, 255, 0.1);
  border-color: rgba(255, 255, 255, 0.2);
}

.report-reason-item:has(input:checked) {
  background: rgba(254, 44, 85, 0.2);
  border-color: #FE2C55;
}

.report-reason-item input {
  accent-color: #FE2C55;
}

.reason-label {
  font-size: 14px;
  color: #fff;
}

.report-description-wrapper {
  position: relative;
}

.report-description-label {
  display: block;
  font-size: 14px;
  font-weight: 600;
  color: #fff;
  margin-bottom: 12px;
}

.report-description-wrapper textarea {
  width: 100%;
  padding: 12px 16px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.05);
  color: #fff;
  font-size: 14px;
  resize: none;
  outline: none;
  transition: all 0.3s ease;
  box-sizing: border-box;
}

.report-description-wrapper textarea::placeholder {
  color: rgba(255, 255, 255, 0.4);
}

.report-description-wrapper textarea:focus {
  border-color: #FE2C55;
  background: rgba(255, 255, 255, 0.1);
}

.char-count {
  position: absolute;
  right: 12px;
  bottom: 12px;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.4);
}

.report-panel-footer {
  display: flex;
  gap: 12px;
  padding: 16px 20px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  background: rgba(0, 0, 0, 0.3);
}

.cancel-report-btn {
  flex: 1;
  padding: 12px 24px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 8px;
  background: transparent;
  color: rgba(255, 255, 255, 0.7);
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.cancel-report-btn:hover {
  background: rgba(255, 255, 255, 0.1);
}

.submit-report-btn {
  flex: 1;
  padding: 12px 24px;
  border: none;
  border-radius: 8px;
  background: linear-gradient(135deg, #FE2C55 0%, #FF4373 100%);
  color: #fff;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.submit-report-btn:hover {
  transform: scale(1.02);
}

@media (min-width: 769px) {
  .report-panel-content {
    left: 50%;
    right: auto;
    bottom: 50%;
    transform: translate(-50%, 100%);
    width: 480px;
    border-radius: 16px;
  }
  
  .report-panel.active .report-panel-content {
    transform: translate(-50%, 50%);
  }
}
</style>
