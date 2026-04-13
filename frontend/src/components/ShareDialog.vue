<template>
  <a-modal
    :open="visible"
    title="分享直播间"
    @cancel="handleClose"
    :footer="null"
    width="480px"
  >
    <div class="share-dialog-content">
      <!-- 分享预览 -->
      <div class="share-preview" v-if="shareData">
        <img :src="shareData.cover" class="share-cover" />
        <div class="share-info">
          <div class="share-title">{{ shareData.title }}</div>
          <div class="share-anchor">主播：{{ shareData.anchorName }}</div>
        </div>
      </div>

      <a-divider />

      <!-- 分享方式 -->
      <div class="share-methods">
        <div class="share-section-title">分享到</div>
        <div class="share-buttons">
          <div class="share-btn" @click="shareToWeixin">
            <div class="share-icon weixin">
              <WechatOutlined />
            </div>
            <span>微信</span>
          </div>
          <div class="share-btn" @click="shareToWeibo">
            <div class="share-icon weibo">
              <WeiboCircleOutlined />
            </div>
            <span>微博</span>
          </div>
          <div class="share-btn" @click="shareToQQ">
            <div class="share-icon qq">
              <QqOutlined />
            </div>
            <span>QQ</span>
          </div>
          <div class="share-btn" @click="shareToLink">
            <div class="share-icon link">
              <LinkOutlined />
            </div>
            <span>复制链接</span>
          </div>
        </div>
      </div>

      <a-divider />

      <!-- 分享链接 -->
      <div class="share-link-section">
        <div class="share-section-title">分享链接</div>
        <div class="share-link-input">
          <a-input
            :value="shareUrl"
            readonly
            class="share-url-input"
          >
            <template #addonAfter>
              <a-button type="primary" @click="copyLink">
                复制
              </a-button>
            </template>
          </a-input>
        </div>
      </div>

      <!-- 二维码 -->
      <div class="share-qrcode-section" v-if="showQRCode">
        <div class="share-section-title">扫码观看</div>
        <div class="qrcode-wrapper">
          <canvas ref="qrcodeCanvas"></canvas>
        </div>
        <div class="qrcode-tip">使用微信扫一扫，分享给好友</div>
      </div>
    </div>
  </a-modal>
</template>

<script setup lang="ts">
import { ref, watch, nextTick } from 'vue';
import { message } from 'ant-design-vue';
import {
  WechatOutlined,
  WeiboCircleOutlined,
  QqOutlined,
  LinkOutlined
} from '@ant-design/icons-vue';
import QRCode from 'qrcode';

interface ShareData {
  liveId: number;
  title: string;
  cover: string;
  anchorName: string;
}

const props = defineProps<{
  visible: boolean;
  shareData?: ShareData;
}>();

const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void;
  (e: 'share', platform: string): void;
}>();

const shareUrl = ref('');
const showQRCode = ref(false);
const qrcodeCanvas = ref<HTMLCanvasElement | null>(null);

// 生成分享链接
const generateShareUrl = () => {
  if (!props.shareData) return '';
  const baseUrl = window.location.origin;
  return `${baseUrl}/live/${props.shareData.liveId}`;
};

// 监听visible变化
watch(() => props.visible, (newVal) => {
  if (newVal && props.shareData) {
    shareUrl.value = generateShareUrl();
    showQRCode.value = false;
  }
});

// 分享到微信
const shareToWeixin = async () => {
  showQRCode.value = true;
  await nextTick();
  generateQRCode();
  emit('share', 'weixin');
};

// 生成二维码
const generateQRCode = async () => {
  if (!qrcodeCanvas.value || !shareUrl.value) return;
  try {
    await QRCode.toCanvas(qrcodeCanvas.value, shareUrl.value, {
      width: 200,
      margin: 2,
      color: {
        dark: '#000000',
        light: '#ffffff'
      }
    });
  } catch (error) {
    console.error('生成二维码失败:', error);
  }
};

// 分享到微博
const shareToWeibo = () => {
  const text = encodeURIComponent(`快来看看这个直播间：${props.shareData?.title}`);
  const url = encodeURIComponent(shareUrl.value);
  const pic = encodeURIComponent(props.shareData?.cover || '');
  const weiboUrl = `https://service.weibo.com/share/share.php?title=${text}&url=${url}&pic=${pic}`;
  window.open(weiboUrl, '_blank', 'width=600,height=500');
  emit('share', 'weibo');
};

// 分享到QQ
const shareToQQ = () => {
  const title = encodeURIComponent(props.shareData?.title || '');
  const url = encodeURIComponent(shareUrl.value);
  const pics = encodeURIComponent(props.shareData?.cover || '');
  const summary = encodeURIComponent(`主播${props.shareData?.anchorName}正在直播，快来围观！`);
  const qqUrl = `https://connect.qq.com/widget/shareqq/index.html?title=${title}&url=${url}&pics=${pics}&summary=${summary}`;
  window.open(qqUrl, '_blank', 'width=600,height=500');
  emit('share', 'qq');
};

// 复制链接分享
const shareToLink = () => {
  copyLink();
  emit('share', 'link');
};

// 复制链接
const copyLink = async () => {
  try {
    await navigator.clipboard.writeText(shareUrl.value);
    message.success('链接已复制到剪贴板');
  } catch (err) {
    // 降级方案
    const input = document.createElement('input');
    input.value = shareUrl.value;
    document.body.appendChild(input);
    input.select();
    document.execCommand('copy');
    document.body.removeChild(input);
    message.success('链接已复制到剪贴板');
  }
};

// 关闭弹窗
const handleClose = () => {
  emit('update:visible', false);
  showQRCode.value = false;
};
</script>

<style scoped>
.share-dialog-content {
  padding: 0 8px;
}

.share-preview {
  display: flex;
  gap: 16px;
  padding: 8px 0;
}

.share-cover {
  width: 120px;
  height: 80px;
  object-fit: cover;
  border-radius: 8px;
}

.share-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.share-title {
  font-size: 16px;
  font-weight: 500;
  color: #262626;
  margin-bottom: 8px;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.share-anchor {
  font-size: 14px;
  color: #8c8c8c;
}

.share-section-title {
  font-size: 14px;
  color: #595959;
  margin-bottom: 16px;
}

.share-buttons {
  display: flex;
  justify-content: space-around;
  padding: 8px 0;
}

.share-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  transition: transform 0.2s;
}

.share-btn:hover {
  transform: translateY(-2px);
}

.share-btn span {
  font-size: 12px;
  color: #595959;
}

.share-icon {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: white;
  transition: box-shadow 0.2s;
}

.share-icon:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.share-icon.weixin {
  background: linear-gradient(135deg, #07c160 0%, #05a350 100%);
}

.share-icon.weibo {
  background: linear-gradient(135deg, #ff8200 0%, #e67400 100%);
}

.share-icon.qq {
  background: linear-gradient(135deg, #12b7f5 0%, #0a9bd1 100%);
}

.share-icon.link {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.share-link-section {
  padding: 8px 0;
}

.share-url-input :deep(.ant-input) {
  background-color: #f5f5f5;
}

.share-qrcode-section {
  padding: 16px 0;
  text-align: center;
}

.qrcode-wrapper {
  display: inline-block;
  padding: 16px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.qrcode-tip {
  margin-top: 12px;
  font-size: 12px;
  color: #8c8c8c;
}
</style>
