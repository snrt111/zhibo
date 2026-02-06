<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { message } from 'ant-design-vue';
import { liveApi } from '../../api/live';

const router = useRouter();
const loading = ref(false);
const myLiveList = ref<any[]>([]);
const showCreateModal = ref(false);
const showPushUrlModal = ref(false);
const currentPushUrl = ref('');
const newLive = reactive({
  title: '',
  description: '',
  cover: ''
});

const getMyLives = async () => {
  loading.value = true;
  try {
    const response = await liveApi.getMyLives();
    if (response.code === 200) {
      myLiveList.value = response.data || [];
    } else {
      myLiveList.value = [];
    }
  } catch (error) {
    console.error('获取我的直播错误:', error);
    myLiveList.value = [];
  } finally {
    loading.value = false;
  }
};

const handleCreateLive = async () => {
  if (!newLive.title) {
    message.error('请输入直播标题');
    return;
  }
  try {
    const response = await liveApi.create(newLive);
    if (response.code === 200) {
      message.success('创建直播成功');
      showCreateModal.value = false;
      newLive.title = '';
      newLive.description = '';
      newLive.cover = '';
      getMyLives();
    } else {
      message.error(response.message || '创建直播失败');
    }
  } catch (error) {
    console.error('创建直播错误:', error);
    message.error('创建直播失败');
  }
};

const handleStartLive = async (id: number) => {
  try {
    const response = await liveApi.start(id);
    if (response.code === 200) {
      message.success('直播已开始');
      getMyLives();
      router.push(`/live/${id}`);
    } else {
      message.error(response.message || '开始直播失败');
    }
  } catch (error) {
    console.error('开始直播错误:', error);
    message.error('开始直播失败');
  }
};

const handleEndLive = async (id: number) => {
  try {
    const response = await liveApi.end(id);
    if (response.code === 200) {
      message.success('直播已结束');
      getMyLives();
    } else {
      message.error(response.message || '结束直播失败');
    }
  } catch (error) {
    console.error('结束直播错误:', error);
    message.error('结束直播失败');
  }
};

const handleDeleteLive = async (id: number) => {
  try {
    const response = await liveApi.delete(id);
    if (response.code === 200) {
      message.success('删除直播成功');
      getMyLives();
    } else {
      message.error(response.message || '删除直播失败');
    }
  } catch (error) {
    console.error('删除直播错误:', error);
    message.error('删除直播失败');
  }
};

const handleShowPushUrl = async (id: number) => {
  try {
    const response = await liveApi.getPushUrl(id);
    if (response.code === 200 && response.data) {
      currentPushUrl.value = response.data;
      showPushUrlModal.value = true;
    } else {
      message.error(response.message || '获取推流地址失败');
    }
  } catch (error) {
    console.error('获取推流地址错误:', error);
    message.error('获取推流地址失败');
  }
};

const copyToClipboard = () => {
  navigator.clipboard.writeText(currentPushUrl.value)
    .then(() => {
      message.success('推流地址已复制到剪贴板');
    })
    .catch(err => {
      console.error('复制失败:', err);
      message.error('复制失败');
    });
};

onMounted(() => {
  getMyLives();
});
</script>

<template>
  <div class="anchor-dashboard-container">
    <div class="content-inner">
      <div class="content-header">
        <h2>我的直播</h2>
        <button class="create-button" @click="showCreateModal = true">创建直播</button>
      </div>
      <div v-if="loading" class="loading">加载中...</div>
      <div v-else-if="myLiveList.length === 0" class="empty">
        <p>暂无直播</p>
        <p>点击上方"创建直播"按钮开始您的第一场直播吧！</p>
      </div>
      <div v-else class="live-list">
        <div v-for="live in myLiveList" :key="live.id" class="live-item">
          <div class="live-cover">
  <img :src="live.cover || 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=live%20streaming%20cover%20abstract%20colorful%20modern&image_size=square'" :alt="live.title" />
  <div v-if="live.status === 1" class="live-status">直播中</div>
  <div v-else-if="live.status === 2" class="live-status ended">已结束</div>
</div>
          <div class="live-info">
            <h3 class="live-title">{{ live.title }}</h3>
            <p class="live-description">{{ live.description }}</p>
            <div class="live-stats">
              <span class="view-count">👁 {{ live.viewCount || 0 }}</span>
              <span class="like-count">❤️ {{ live.likeCount || 0 }}</span>
            </div>
            <div class="live-actions">
              <button v-if="live.status === 0" class="start-button" @click="handleStartLive(live.id)">开始直播</button>
              <button v-if="live.status === 1" class="end-button" @click="handleEndLive(live.id)">结束直播</button>
              <button v-if="live.status !== 1" class="push-url-button" @click="handleShowPushUrl(live.id)">获取推流地址</button>
              <button v-if="live.status !== 1" class="delete-button" @click="handleDeleteLive(live.id)">删除</button>
            </div>
          </div>
        </div>
      </div>

      <!-- 创建直播弹窗 -->
      <div v-if="showCreateModal" class="modal-overlay" @click="showCreateModal = false">
        <div class="modal-content" @click.stop>
          <h3>创建直播</h3>
          <div class="form-item">
            <label>直播标题</label>
            <input type="text" v-model="newLive.title" placeholder="请输入直播标题" />
          </div>
          <div class="form-item">
            <label>直播描述</label>
            <textarea v-model="newLive.description" placeholder="请输入直播描述"></textarea>
          </div>
          <div class="form-item">
            <label>封面图片URL</label>
            <input type="text" v-model="newLive.cover" placeholder="请输入封面图片URL（可选）" />
          </div>
          <div class="modal-actions">
            <button class="cancel-button" @click="showCreateModal = false">取消</button>
            <button class="confirm-button" @click="handleCreateLive">创建</button>
          </div>
        </div>
      </div>

      <!-- 推流地址弹窗 -->
      <div v-if="showPushUrlModal" class="modal-overlay" @click="showPushUrlModal = false">
        <div class="modal-content modal-small" @click.stop>
          <h3>推流地址</h3>
          <div class="form-item">
            <label>推流地址</label>
            <div class="url-container">
              <input type="text" :value="currentPushUrl" readonly />
              <button class="copy-button" @click="copyToClipboard">复制</button>
            </div>
            <p class="tip">请使用OBS等推流软件，将此地址填入推流设置中的"服务器URL"或"Stream URL"字段</p>
          </div>
          <div class="modal-actions">
            <button class="confirm-button" @click="showPushUrlModal = false">确定</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.anchor-dashboard-container {
  min-height: calc(100vh - 72px);
  background-color: #f0f2f5;
  padding: 20px;
  margin: 0;
  border-radius: 0;
  box-shadow: none;
}

.content-inner {
  max-width: 1200px;
  margin: 0 auto;
  width: 100%;
}

.content-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  padding: 0 20px;
}

.content-header h2 {
  margin: 0;
  font-size: 28px;
  color: #333;
  font-weight: 600;
}

.create-button {
  padding: 10px 20px;
  background-color: #52c41a;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.create-button:hover {
  background-color: #73d13d;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(82, 196, 26, 0.3);
}

.loading, .empty {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  height: 500px;
  font-size: 18px;
  color: #666;
  padding: 0 20px;
}

.empty p {
  margin: 10px 0;
  text-align: center;
}

.live-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 24px;
  padding: 0 20px;
}

.live-item {
  background-color: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
}

.live-item:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.live-cover {
  position: relative;
  height: 200px;
  overflow: hidden;
}

.live-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
}

.live-item:hover .live-cover img {
  transform: scale(1.05);
}

.live-status {
  position: absolute;
  top: 12px;
  right: 12px;
  background-color: #ff4d4f;
  color: white;
  padding: 6px 16px;
  border-radius: 16px;
  font-size: 12px;
  font-weight: 600;
  z-index: 10;
}

.live-status.ended {
  background-color: #8c8c8c;
}

.live-info {
  padding: 20px;
}

.live-title {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 12px;
  color: #333;
  line-height: 1.4;
}

.live-description {
  font-size: 14px;
  color: #666;
  margin-bottom: 16px;
  height: 48px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  line-height: 1.5;
}

.live-stats {
  display: flex;
  gap: 24px;
  font-size: 14px;
  color: #999;
  margin-bottom: 16px;
}

.live-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.start-button, .end-button, .delete-button, .push-url-button {
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
  flex: 1;
  min-width: 100px;
}

.start-button {
  background-color: #52c41a;
  color: white;
}

.start-button:hover {
  background-color: #73d13d;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(82, 196, 26, 0.3);
}

.end-button {
  background-color: #ff4d4f;
  color: white;
}

.end-button:hover {
  background-color: #ff7875;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(255, 77, 79, 0.3);
}

.delete-button {
  background-color: #d9d9d9;
  color: #333;
}

.delete-button:hover {
  background-color: #bfbfbf;
  transform: translateY(-1px);
}

.push-url-button {
  background-color: #1890ff;
  color: white;
}

.push-url-button:hover {
  background-color: #40a9ff;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(24, 144, 255, 0.3);
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background-color: white;
  border-radius: 12px;
  padding: 32px;
  width: 500px;
  max-width: 90%;
  box-shadow: 0 12px 48px rgba(0, 0, 0, 0.15);
  animation: modalFadeIn 0.3s ease;
}

@keyframes modalFadeIn {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.modal-content h3 {
  margin: 0 0 24px 0;
  font-size: 20px;
  color: #333;
  font-weight: 600;
}

.form-item {
  margin-bottom: 20px;
}

.form-item label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
  color: #333;
  font-size: 14px;
}

.form-item input, .form-item textarea {
  width: 100%;
  padding: 12px 16px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  font-size: 14px;
  box-sizing: border-box;
  transition: all 0.3s ease;
}

.form-item input:focus, .form-item textarea:focus {
  outline: none;
  border-color: #1890ff;
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);
}

.form-item textarea {
  height: 120px;
  resize: vertical;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 24px;
}

.cancel-button, .confirm-button {
  padding: 10px 20px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.cancel-button {
  background-color: #d9d9d9;
  color: #333;
}

.cancel-button:hover {
  background-color: #bfbfbf;
  transform: translateY(-1px);
}

.confirm-button {
  background-color: #1890ff;
  color: white;
}

.confirm-button:hover {
  background-color: #40a9ff;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(24, 144, 255, 0.3);
}

.modal-small {
  width: 450px;
}

.url-container {
  display: flex;
  gap: 12px;
}

.url-container input {
  flex: 1;
}

.copy-button {
  padding: 12px 20px;
  background-color: #52c41a;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  white-space: nowrap;
  transition: all 0.3s ease;
}

.copy-button:hover {
  background-color: #73d13d;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(82, 196, 26, 0.3);
}

.tip {
  margin: 12px 0 0 0;
  font-size: 12px;
  color: #999;
  line-height: 1.4;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .content-inner {
    max-width: 100%;
  }
  
  .live-list {
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  }
}

@media (max-width: 768px) {
  .content-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
    padding: 0 16px;
  }
  
  .content-header h2 {
    font-size: 24px;
  }
  
  .live-list {
    grid-template-columns: 1fr;
    padding: 0 16px;
  }
  
  .live-actions {
    flex-direction: column;
  }
  
  .start-button, .end-button, .delete-button, .push-url-button {
    width: 100%;
  }
}
</style>
