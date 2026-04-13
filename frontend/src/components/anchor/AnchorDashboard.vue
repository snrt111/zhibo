<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { message } from 'ant-design-vue';
import { liveApi } from '../../api/live';
import { handleResponseAsync, errorHandler } from '../../utils/errorHandler';
import ImageUploader from '../common/ImageUploader.vue';

const router = useRouter();
const loading = ref(false);
const myLiveList = ref<any[]>([]);
const showCreateModal = ref(false);
const categoryList = ref<any[]>([]);
const newLive = reactive({
  title: '',
  description: '',
  cover: '',
  categoryId: null as number | null
});

const getCategoryList = async () => {
  try {
    const response = await liveApi.getCategoryList();
    console.log('Category response:', response);
    if (response && response.code === 200) {
      categoryList.value = response.data || [];
      if (categoryList.value.length > 0) {
        newLive.categoryId = categoryList.value[0].id;
      }
    } else {
      categoryList.value = [];
    }
  } catch (error) {
    console.error('Get category error:', error);
    categoryList.value = [];
  }
};

const getMyLives = async () => {
  loading.value = true;
  try {
    const response = await liveApi.getMyLives();
    await handleResponseAsync(response, (data) => {
      myLiveList.value = data || [];
    }, () => {
      myLiveList.value = [];
    });
  } catch (error) {
    errorHandler.handle(error, false);
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
  if (!newLive.categoryId) {
    message.error('请选择直播分类');
    return;
  }
  try {
    const response = await liveApi.create(newLive);
    await handleResponseAsync(response, () => {
      message.success('创建直播成功');
      showCreateModal.value = false;
      newLive.title = '';
      newLive.description = '';
      newLive.cover = '';
      newLive.categoryId = categoryList.value.length > 0 ? categoryList.value[0].id : null;
      getMyLives();
    });
  } catch (error) {
    errorHandler.handle(error);
  }
};

const handleStartLive = async (id: number) => {
  try {
    console.log('开始直播，直播ID:', id);
    const response = await liveApi.start(id);
    console.log('开始直播响应:', response);
    await handleResponseAsync(response, () => {
      message.success('直播已开始');
      router.push(`/anchor/live/${id}`);
    });
  } catch (error) {
    console.error('开始直播错误:', error);
    errorHandler.handle(error);
  }
};

const handleEndLive = async (id: number) => {
  try {
    const response = await liveApi.end(id);
    await handleResponseAsync(response, () => {
      message.success('直播已结束');
      getMyLives();
    });
  } catch (error) {
    errorHandler.handle(error);
  }
};

const handleDeleteLive = async (id: number) => {
  try {
    const response = await liveApi.delete(id);
    await handleResponseAsync(response, () => {
      message.success('删除直播成功');
      getMyLives();
    });
  } catch (error) {
    errorHandler.handle(error);
  }
};

onMounted(() => {
  console.log('AnchorDashboard mounted, calling getCategoryList...');
  getCategoryList();
  getMyLives();
});
</script>

<template>
  <div class="anchor-dashboard-container">
    <div class="content-inner">
      <div class="content-header">
        <h2>我的直播</h2>
        <div class="header-actions">
          <button class="create-button" @click="showCreateModal = true">创建直播</button>
        </div>
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
              <button v-if="live.status !== 1" class="delete-button" @click="handleDeleteLive(live.id)">删除</button>
            </div>
          </div>
        </div>
      </div>

      <!-- 创建直播弹窗 -->
      <div v-if="showCreateModal" class="modal-overlay" @click="showCreateModal = false">
        <div class="modal-content" @click.stop>
          <h3>创建直播</h3>
          <div style="margin-bottom: 10px; font-size: 12px; color: #999;">
            分类数量: {{ categoryList.length }}
          </div>
          <div class="form-item">
            <label>直播标题</label>
            <input type="text" v-model="newLive.title" placeholder="请输入直播标题" />
          </div>
          <div class="form-item">
            <label>直播描述</label>
            <textarea v-model="newLive.description" placeholder="请输入直播描述"></textarea>
          </div>
          <div class="form-item">
            <label>封面图片</label>
            <ImageUploader v-model="newLive.cover" />
          </div>
          <div class="form-item">
            <label>直播分类</label>
            <select v-model="newLive.categoryId">
              <option v-for="category in categoryList" :key="category.id" :value="category.id">
                {{ category.name }}
              </option>
            </select>
          </div>
          <div class="modal-actions">
            <button class="cancel-button" @click="showCreateModal = false">取消</button>
            <button class="confirm-button" @click="handleCreateLive">创建</button>
          </div>
        </div>
      </div>

      </div>
  </div>
</template>

<style scoped>
.anchor-dashboard-container {
  min-height: calc(100vh - 72px);
  background: linear-gradient(180deg, #0f0f0f 0%, #1a1a1a 100%);
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
  color: #fff;
  font-weight: 600;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.create-button {
  padding: 10px 20px;
  background: linear-gradient(135deg, #ff4757 0%, #ff6b81 100%);
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.create-button:hover {
  background: linear-gradient(135deg, #ff6b81 0%, #ff4757 100%);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(255, 71, 87, 0.3);
}

.loading, .empty {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  height: 500px;
  font-size: 18px;
  color: rgba(255, 255, 255, 0.6);
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
  background: rgba(255, 255, 255, 0.05);
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid rgba(255, 255, 255, 0.08);
  transition: all 0.3s ease;
}

.live-item:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.4);
  border-color: rgba(255, 71, 87, 0.3);
}

.live-cover {
  position: relative;
  height: 200px;
  overflow: hidden;
  background: #1a1a1a;
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
  color: #fff;
  line-height: 1.4;
}

.live-description {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.8);
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
  color: rgba(255, 255, 255, 0.7);
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
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
  flex: 1;
  min-width: 100px;
}

.start-button {
  background: linear-gradient(135deg, #00b894 0%, #00cec9 100%);
  color: white;
}

.start-button:hover {
  background: linear-gradient(135deg, #00cec9 0%, #00b894 100%);
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(0, 206, 201, 0.3);
}

.end-button {
  background: linear-gradient(135deg, #ff4757 0%, #ff6b81 100%);
  color: white;
}

.end-button:hover {
  background: linear-gradient(135deg, #ff6b81 0%, #ff4757 100%);
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(255, 71, 87, 0.3);
}

.delete-button {
  background: rgba(255, 255, 255, 0.1);
  color: rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.delete-button:hover {
  background: rgba(255, 255, 255, 0.15);
  transform: translateY(-1px);
}

.push-url-button {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.push-url-button:hover {
  background: linear-gradient(135deg, #764ba2 0%, #667eea 100%);
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.3);
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.7);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
  border-radius: 12px;
  padding: 32px;
  width: 500px;
  max-width: 90%;
  border: 1px solid rgba(255, 255, 255, 0.1);
  box-shadow: 0 12px 48px rgba(0, 0, 0, 0.4);
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
  color: #fff;
  font-weight: 600;
}

.form-item {
  margin-bottom: 20px;
}

.form-item label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
  color: rgba(255, 255, 255, 0.9);
  font-size: 14px;
}

.form-item input, .form-item textarea {
  width: 100%;
  padding: 12px 16px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 6px;
  font-size: 14px;
  box-sizing: border-box;
  transition: all 0.3s ease;
  background: rgba(255, 255, 255, 0.05);
  color: #fff;
}

.form-item input:focus, .form-item textarea:focus, .form-item select:focus {
  outline: none;
  border-color: #ff4757;
  box-shadow: 0 0 0 3px rgba(255, 71, 87, 0.2);
}

.form-item textarea {
  height: 120px;
  resize: vertical;
}

.form-item select {
  width: 100%;
  padding: 12px 16px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 6px;
  font-size: 14px;
  box-sizing: border-box;
  transition: all 0.3s ease;
  background: rgba(255, 255, 255, 0.05);
  color: #fff;
  cursor: pointer;
}

.form-item select option {
  background: #1a1a2e;
  color: #fff;
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
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.cancel-button {
  background: rgba(255, 255, 255, 0.1);
  color: rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.cancel-button:hover {
  background: rgba(255, 255, 255, 0.15);
  transform: translateY(-1px);
}

.confirm-button {
  background: linear-gradient(135deg, #ff4757 0%, #ff6b81 100%);
  color: white;
}

.confirm-button:hover {
  background: linear-gradient(135deg, #ff6b81 0%, #ff4757 100%);
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(255, 71, 87, 0.3);
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

.browser-push-button {
  background-color: #722ed1;
  color: white;
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

.browser-push-button:hover {
  background-color: #9254de;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(114, 46, 209, 0.3);
}

.modal-large {
  width: 800px;
  max-width: 95%;
}

.browser-push-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
  margin-bottom: 24px;
}

.preview-section {
  position: relative;
}

.video-container {
  width: 100%;
  aspect-ratio: 16 / 9;
  background-color: #000;
  border-radius: 8px;
  overflow: hidden;
}

.preview-video {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.preview-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #666;
  font-size: 16px;
}

.pushing-indicator {
  position: absolute;
  top: 12px;
  left: 12px;
  display: flex;
  align-items: center;
  gap: 8px;
  background-color: rgba(0, 0, 0, 0.7);
  padding: 6px 12px;
  border-radius: 20px;
  color: white;
  font-size: 14px;
}

.pulse {
  width: 10px;
  height: 10px;
  background-color: #ff4d4f;
  border-radius: 50%;
  animation: pulse 1.5s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.3;
  }
}

.control-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.device-tabs {
  display: flex;
  gap: 8px;
}

.tab-btn {
  flex: 1;
  padding: 10px 16px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  background-color: white;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s ease;
}

.tab-btn.active {
  background-color: #1890ff;
  color: white;
  border-color: #1890ff;
}

.tab-btn:hover:not(.active) {
  border-color: #1890ff;
  color: #1890ff;
}

.control-section select {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  font-size: 14px;
  background-color: white;
  cursor: pointer;
}

.control-section select:focus {
  outline: none;
  border-color: #1890ff;
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);
}

.action-buttons {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: auto;
}

.preview-button, .start-push-button, .stop-push-button {
  padding: 12px 24px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.preview-button {
  background-color: #1890ff;
  color: white;
}

.preview-button:hover {
  background-color: #40a9ff;
}

.start-push-button {
  background-color: #52c41a;
  color: white;
}

.start-push-button:hover {
  background-color: #73d13d;
}

.stop-push-button {
  background-color: #ff4d4f;
  color: white;
}

.stop-push-button:hover {
  background-color: #ff7875;
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
  
  .start-button, .end-button, .delete-button, .push-url-button, .browser-push-button {
    width: 100%;
  }
  
  .browser-push-content {
    grid-template-columns: 1fr;
  }
  
  .modal-large {
    width: 95%;
  }
}
</style>
