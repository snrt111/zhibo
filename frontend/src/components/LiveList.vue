<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { message } from 'ant-design-vue';
import { liveApi } from '../api/live';

const router = useRouter();
const loading = ref(false);
const liveList = ref<any[]>([]);

const getLiveList = async () => {
  loading.value = true;
  try {
    const res = await liveApi.getLiveList();
    if (res.code === 200) {
      liveList.value = res.data || [];
    } else {
      message.error(res.message || '获取直播列表失败');
    }
  } catch (error) {
    console.error('获取直播列表错误:', error);
    message.error('获取直播列表失败，请检查网络连接');
  } finally {
    loading.value = false;
  }
};

const handleLiveClick = (id: number) => {
  router.push(`/live/${id}`);
};

onMounted(() => {
  getLiveList();
});
</script>

<template>
  <div class="live-list-container">
    <div class="live-content-inner">
      <div class="live-list-header">
        <h1>直播列表</h1>
        <p class="header-subtitle">发现精彩直播内容</p>
      </div>
      <div class="live-list-content">
        <div v-if="loading" class="loading">加载中...</div>
        <div v-else-if="liveList.length === 0" class="empty">暂无直播</div>
        <div v-else class="live-list">
          <div 
            v-for="live in liveList" 
            :key="live.id" 
            class="live-item" 
            @click="handleLiveClick(live.id)"
          >
            <div class="live-cover">
  <img :src="live.cover || 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=live%20streaming%20cover%20abstract%20colorful%20modern&image_size=square'" :alt="live.title" />
  <div v-if="live.status === 1" class="live-status">直播中</div>
</div>
            <div class="live-info">
              <h3 class="live-title">{{ live.title }}</h3>
              <p class="live-description">{{ live.description }}</p>
              <div class="live-stats">
                <span class="view-count">👁 {{ live.viewCount || 0 }}</span>
                <span class="like-count">❤️ {{ live.likeCount || 0 }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.live-list-container {
  width: 100%;
  min-height: calc(100vh - 72px);
  background-color: #f0f2f5;
  padding: 30px;
  margin: 0;
  border-radius: 0;
  box-shadow: none;
}

.live-content-inner {
  max-width: 1400px;
  margin: 0 auto;
  width: 100%;
}

.live-list-header {
  margin-bottom: 32px;
  padding-bottom: 16px;
  border-bottom: 1px solid #e8e8e8;
}

.live-list-header h1 {
  font-size: 36px;
  color: #333;
  font-weight: 600;
  margin-bottom: 8px;
}

.header-subtitle {
  font-size: 16px;
  color: #666;
  margin: 0;
}

.live-list-content {
  min-height: 600px;
}

.loading, .empty {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 600px;
  font-size: 18px;
  color: #999;
  background-color: #fafafa;
  border-radius: 8px;
  box-shadow: inset 0 2px 4px rgba(0, 0, 0, 0.05);
}

.live-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 24px;
}

.live-item {
  border: 1px solid #e8e8e8;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
  min-height: 340px;
  background-color: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.live-item:hover {
  box-shadow: 0 8px 24px 0 rgba(0, 0, 0, 0.15);
  transform: translateY(-4px);
  border-color: #d9d9d9;
}

.live-cover {
  position: relative;
  height: 200px;
  overflow: hidden;
  border-radius: 12px 12px 0 0;
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
  background-color: rgba(255, 77, 79, 0.9);
  color: white;
  padding: 6px 16px;
  border-radius: 16px;
  font-size: 14px;
  font-weight: 600;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.15);
  transition: all 0.3s ease;
}

.live-item:hover .live-status {
  background-color: rgba(255, 77, 79, 1);
  transform: scale(1.05);
}

.live-info {
  padding: 20px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.live-title {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 12px;
  color: #333;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  transition: color 0.3s ease;
}

.live-item:hover .live-title {
  color: #667eea;
}

.live-description {
  font-size: 14px;
  color: #666;
  margin-bottom: 16px;
  flex: 1;
  min-height: 48px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  line-height: 1.5;
}

.live-stats {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
  color: #999;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
  margin-top: auto;
}

.view-count, .like-count {
  display: flex;
  align-items: center;
  gap: 6px;
  transition: color 0.3s ease;
}

.live-item:hover .view-count,
.live-item:hover .like-count {
  color: #667eea;
}

/* 移除重复的图标伪元素 */

/* 响应式布局调整 */
@media (max-width: 1400px) {
  .live-list {
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
    gap: 20px;
  }
  
  .live-item {
    min-height: 320px;
  }
  
  .live-cover {
    height: 180px;
  }
}

@media (max-width: 1200px) {
  .live-list {
    grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
    gap: 20px;
  }
  
  .live-item {
    min-height: 300px;
  }
  
  .live-cover {
    height: 160px;
  }
  
  .live-info {
    padding: 16px;
  }
  
  .live-title {
    font-size: 18px;
  }
}

@media (max-width: 992px) {
  .live-list-header h1 {
    font-size: 32px;
  }
  
  .live-list {
    grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
    gap: 16px;
  }
  
  .live-item {
    min-height: 280px;
  }
  
  .live-cover {
    height: 150px;
  }
}

@media (max-width: 768px) {
  .live-list-header h1 {
    font-size: 28px;
  }
  
  .live-list {
    grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
    gap: 16px;
  }
  
  .live-item {
    min-height: 260px;
    border-radius: 8px;
  }
  
  .live-cover {
    height: 140px;
    border-radius: 8px 8px 0 0;
  }
  
  .live-info {
    padding: 12px;
  }
  
  .live-title {
    font-size: 16px;
  }
  
  .live-description {
    font-size: 12px;
    min-height: 40px;
  }
}

@media (max-width: 480px) {
  .live-list {
    grid-template-columns: 1fr;
  }
  
  .live-item {
    min-height: 300px;
  }
  
  .live-cover {
    height: 180px;
  }
}
</style>