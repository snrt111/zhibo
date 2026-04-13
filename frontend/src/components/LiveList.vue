<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { message } from 'ant-design-vue';
import { liveApi } from '../api/live';
import { handleResponseAsync, errorHandler } from '../utils/errorHandler';

const router = useRouter();
const loading = ref(false);
const liveList = ref<any[]>([]);
const categoryList = ref<any[]>([]);
const activeCategory = ref<number | null>(null);
const isMobile = ref(window.innerWidth <= 768);
const searchKeyword = ref('');

const getCategoryList = async () => {
  try {
    const res = await liveApi.getCategoryList();
    await handleResponseAsync(res, (data) => {
      categoryList.value = data || [];
    });
  } catch (error) {
    categoryList.value = [];
  }
};

const getLiveList = async () => {
  loading.value = true;
  try {
    const res = await liveApi.getLiveList();
    await handleResponseAsync(res, (data) => {
      liveList.value = data || [];
    });
  } catch (error) {
    errorHandler.handle(error);
    liveList.value = [];
  } finally {
    loading.value = false;
  }
};

const handleCategoryClick = async (categoryId: number | null) => {
  activeCategory.value = categoryId;
  searchKeyword.value = '';
  loading.value = true;
  try {
    if (categoryId === null) {
      await getLiveList();
    } else {
      const res = await liveApi.getLiveListByCategory(categoryId);
      await handleResponseAsync(res, (data) => {
        liveList.value = data || [];
      });
    }
  } catch (error) {
    errorHandler.handle(error);
    liveList.value = [];
  } finally {
    loading.value = false;
  }
};

const handleSearch = async () => {
  if (!searchKeyword.value.trim()) {
    await getLiveList();
    return;
  }
  loading.value = true;
  try {
    const res = await liveApi.search({ keyword: searchKeyword.value });
    await handleResponseAsync(res, (data) => {
      liveList.value = data?.list || [];
      if (liveList.value.length === 0) {
        message.info('未找到相关直播');
      }
    });
  } catch (error) {
    errorHandler.handle(error);
  } finally {
    loading.value = false;
  }
};

const clearSearch = async () => {
  searchKeyword.value = '';
  activeCategory.value = null;
  await getLiveList();
};

const handleLiveClick = (id: number) => {
  router.push(`/live/${id}`);
};

const formatCount = (count: number) => {
  if (count >= 10000) {
    return (count / 10000).toFixed(1) + 'w';
  }
  return count.toString();
};

const getCategoryName = (categoryId: number | null) => {
  if (!categoryId) return '直播';
  const category = categoryList.value.find(c => c.id === categoryId);
  return category?.name || '直播';
};

onMounted(() => {
  getCategoryList();
  getLiveList();
  window.addEventListener('resize', () => {
    isMobile.value = window.innerWidth <= 768;
  });
});
</script>

<template>
  <div class="live-list-container" :class="{ 'mobile': isMobile }">
    <div class="live-content-inner">
      <template v-if="!isMobile">
        <div class="live-list-header">
          <div class="header-left">
            <div class="logo-icon">🎬</div>
            <div class="header-text">
              <h1>直播</h1>
              <p class="header-subtitle">发现精彩内容</p>
            </div>
          </div>
          <div class="header-search">
            <div class="search-box">
              <span class="search-icon">🔍</span>
              <input 
                v-model="searchKeyword" 
                type="text" 
                placeholder="搜索直播..." 
                class="search-input"
                @keyup.enter="handleSearch"
              />
              <span v-if="searchKeyword" class="clear-icon" @click="clearSearch">✕</span>
            </div>
          </div>
        </div>
        
        <div class="category-tabs">
          <span 
            class="tab" 
            :class="{ active: activeCategory === null }"
            @click="handleCategoryClick(null)"
          >推荐</span>
          <span 
            v-for="category in categoryList" 
            :key="category.id"
            class="tab" 
            :class="{ active: activeCategory === category.id }"
            @click="handleCategoryClick(category.id)"
          >{{ category.name }}</span>
        </div>
      </template>

      <div class="live-list-content">
        <div v-if="loading" class="loading">
          <div class="loading-spinner"></div>
          <span>加载中...</span>
        </div>
        <div v-else-if="liveList.length === 0" class="empty">
          <div class="empty-icon">📺</div>
          <p>暂无直播</p>
        </div>
        <div v-else class="live-list" :class="{ 'mobile-list': isMobile }">
          <div 
            v-for="live in liveList" 
            :key="live.id" 
            class="live-item" 
            @click="handleLiveClick(live.id)"
          >
            <div class="live-cover">
              <img :src="live.cover || 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=live%20streaming%20cover%20abstract%20colorful%20modern&image_size=square'" :alt="live.title" />
              <div v-if="live.status === 1" class="live-status">
                <span class="status-dot"></span>
                直播中
              </div>
              <div class="view-badge">
                <span class="view-icon">👁</span>
                {{ formatCount(live.viewCount || 0) }}
              </div>
            </div>
            <div class="live-info">
              <div class="info-top">
                <div class="host-avatar">
                  <img v-if="live.userAvatar" :src="live.userAvatar" alt="主播头像" />
                  <span v-else class="avatar-placeholder">{{ live.userNickname?.charAt(0) || 'U' }}</span>
                </div>
                <div class="info-text">
                  <h3 class="live-title">{{ live.title }}</h3>
                  <p class="host-name">{{ live.userNickname || '用户' + live.userId }}</p>
                </div>
              </div>
              <div class="info-bottom">
                <span class="category-tag">{{ getCategoryName(live.categoryId) }}</span>
                <span class="like-count">❤️ {{ formatCount(live.likeCount || 0) }}</span>
              </div>
            </div>
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

.live-list-container {
  width: 100%;
  min-height: 100vh;
  background: linear-gradient(180deg, #0f0f0f 0%, #1a1a1a 100%);
  padding: 0;
  margin: 0;
}

.live-content-inner {
  max-width: 1400px;
  margin: 0 auto;
  padding: 20px;
  width: 100%;
}

.live-list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo-icon {
  font-size: 32px;
  animation: bounce 2s infinite;
}

@keyframes bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-5px); }
}

.header-text h1 {
  font-size: 28px;
  color: #fff;
  font-weight: 700;
  margin: 0;
  letter-spacing: -0.5px;
}

.header-subtitle {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.8);
  margin: 4px 0 0 0;
}

.header-search {
  display: flex;
  align-items: center;
}

.search-box {
  display: flex;
  align-items: center;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 20px;
  padding: 8px 16px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  transition: all 0.3s ease;
  min-width: 280px;
}

.search-box:focus-within {
  background: rgba(255, 255, 255, 0.15);
  border-color: #ff4757;
  box-shadow: 0 0 0 3px rgba(255, 71, 87, 0.1);
}

.search-icon {
  font-size: 16px;
  margin-right: 8px;
  opacity: 0.6;
}

.search-box .search-input {
  flex: 1;
  background: transparent;
  border: none;
  outline: none;
  color: #fff;
  font-size: 14px;
  padding: 4px 0;
}

.search-box .search-input::placeholder {
  color: rgba(255, 255, 255, 0.6);
}

.clear-icon {
  font-size: 14px;
  opacity: 0.6;
  cursor: pointer;
  padding: 4px;
  border-radius: 50%;
  transition: all 0.2s ease;
  margin-left: 8px;
}

.clear-icon:hover {
  opacity: 1;
  background: rgba(255, 255, 255, 0.1);
}

.category-tabs {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
  overflow-x: auto;
  padding-bottom: 8px;
  scrollbar-width: none;
}

.category-tabs::-webkit-scrollbar {
  display: none;
}

.tab {
  padding: 8px 20px;
  background: rgba(255, 255, 255, 0.08);
  border-radius: 20px;
  color: rgba(255, 255, 255, 0.7);
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  white-space: nowrap;
}

.tab:hover {
  background: rgba(255, 255, 255, 0.15);
  color: #fff;
}

.tab.active {
  background: linear-gradient(135deg, #ff4757 0%, #ff6b81 100%);
  color: #fff;
}

.live-list-content {
  min-height: 600px;
}

.loading, .empty {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  height: 60vh;
  gap: 16px;
  color: rgba(255, 255, 255, 0.85);
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 3px solid rgba(255, 255, 255, 0.1);
  border-top-color: #ff4757;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.empty-icon {
  font-size: 64px;
  opacity: 0.5;
}

.empty p {
  font-size: 16px;
  margin: 0;
}

.live-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

.live-item {
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.08);
}

.live-item:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.4);
  border-color: rgba(255, 71, 87, 0.3);
}

.live-cover {
  position: relative;
  aspect-ratio: 16/10;
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
  transform: scale(1.08);
}

.live-status {
  position: absolute;
  top: 10px;
  left: 10px;
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 4px 10px;
  background: linear-gradient(135deg, rgba(255, 71, 87, 0.95) 0%, rgba(255, 107, 129, 0.95) 100%);
  color: white;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 600;
  z-index: 2;
}

.status-dot {
  width: 6px;
  height: 6px;
  background: #fff;
  border-radius: 50%;
  animation: pulse 1.5s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.view-badge {
  position: absolute;
  bottom: 10px;
  right: 10px;
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 10px;
  background: rgba(0, 0, 0, 0.7);
  border-radius: 4px;
  color: #fff;
  font-size: 12px;
  font-weight: 600;
  z-index: 2;
}

.view-icon {
  font-size: 14px;
}

.live-info {
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.info-top {
  display: flex;
  gap: 10px;
}

.host-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  overflow: hidden;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  flex-shrink: 0;
}

.host-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 14px;
  font-weight: 600;
}

.info-text {
  flex: 1;
  min-width: 0;
}

.live-title {
  font-size: 14px;
  font-weight: 600;
  margin: 0 0 4px 0;
  color: #fff;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.host-name {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.8);
  margin: 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.info-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 8px;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
}

.category-tag {
  padding: 4px 10px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 4px;
  font-size: 11px;
  color: rgba(255, 255, 255, 0.9);
}

.like-count {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.8);
  display: flex;
  align-items: center;
  gap: 4px;
}

@media (max-width: 768px) {
  .live-list-container.mobile {
    background: #000;
  }

  .live-content-inner {
    padding: 16px;
  }

  .live-list-header {
    margin-bottom: 16px;
    padding-bottom: 12px;
  }

  .logo-icon {
    font-size: 24px;
  }

  .header-text h1 {
    font-size: 22px;
  }

  .header-subtitle {
    display: none;
  }

  .search-box {
    min-width: 200px;
    padding: 6px 12px;
  }

  .search-icon {
    font-size: 14px;
  }

  .search-box .search-input {
    font-size: 13px;
  }

  .category-tabs {
    margin-bottom: 16px;
    gap: 8px;
  }

  .tab {
    padding: 6px 14px;
    font-size: 13px;
  }

  .live-list.mobile-list {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }

  .live-item {
    border-radius: 8px;
  }

  .live-cover {
    aspect-ratio: 4/5;
  }

  .live-status {
    top: 6px;
    left: 6px;
    padding: 3px 8px;
    font-size: 10px;
  }

  .view-badge {
    bottom: 6px;
    right: 6px;
    padding: 3px 6px;
    font-size: 10px;
  }

  .live-info {
    padding: 10px;
    gap: 8px;
  }

  .host-avatar {
    width: 28px;
    height: 28px;
  }

  .avatar-placeholder {
    font-size: 12px;
  }

  .live-title {
    font-size: 13px;
  }

  .host-name {
    font-size: 11px;
  }

  .info-bottom {
    padding-top: 6px;
  }

  .category-tag {
    padding: 2px 6px;
    font-size: 10px;
  }

  .like-count {
    font-size: 10px;
  }
}

@media (max-width: 480px) {
  .live-content-inner {
    padding: 12px;
  }

  .live-list.mobile-list {
    gap: 8px;
  }

  .live-cover {
    aspect-ratio: 9/16;
  }

  .live-info {
    padding: 8px;
  }

  .info-top {
    gap: 6px;
  }

  .host-avatar {
    width: 24px;
    height: 24px;
  }

  .live-title {
    font-size: 12px;
  }

  .category-tag {
    display: none;
  }
}

@media (min-width: 1200px) {
  .live-list {
    grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
    gap: 24px;
  }

  .live-cover {
    aspect-ratio: 16/9;
  }
}

@media (min-width: 1600px) {
  .live-content-inner {
    max-width: 1600px;
  }

  .live-list {
    grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  }
}
</style>
