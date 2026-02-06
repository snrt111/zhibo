<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { message } from 'ant-design-vue';
import { analyticsApi } from '../../api/analytics';

const router = useRouter();
const loading = ref(false);
const overviewData = ref<any>(null);

const getOverview = async () => {
  loading.value = true;
  try {
    const response = await analyticsApi.getOverview();
    if (response.code === 200) {
      overviewData.value = response.data;
    } else {
      message.error(response.message || '获取概览数据失败');
    }
  } catch (error) {
    console.error('获取概览数据错误:', error);
    message.error('获取概览数据失败，请检查网络连接');
  } finally {
    loading.value = false;
  }
};

const handleNavigate = (path: string) => {
  router.push(path);
};

onMounted(() => {
  getOverview();
});
</script>

<template>
  <div class="admin-dashboard-container">
    <div class="admin-sidebar">
      <div class="sidebar-header">
        <h3>管理中心</h3>
      </div>
      <ul>
        <li class="active" @click="handleNavigate('/admin')">
          <span class="sidebar-icon">📊</span>
          <span class="sidebar-text">仪表板</span>
        </li>
        <li @click="handleNavigate('/admin/analytics')">
          <span class="sidebar-icon">📈</span>
          <span class="sidebar-text">数据分析</span>
        </li>
        <li @click="handleNavigate('/admin/live')">
          <span class="sidebar-icon">🎥</span>
          <span class="sidebar-text">直播管理</span>
        </li>
        <li @click="handleNavigate('/admin/user')">
          <span class="sidebar-icon">👥</span>
          <span class="sidebar-text">用户管理</span>
        </li>
      </ul>
    </div>
    <div class="admin-content">
      <div v-if="loading" class="loading">加载中...</div>
      <div v-else class="dashboard-content">
        <div class="content-header">
          <h2>平台概览</h2>
          <p class="content-subtitle">实时监控平台运营数据</p>
        </div>
        <div class="overview-cards">
          <div class="overview-card">
            <div class="card-header">
              <h3>总直播数</h3>
              <span class="card-icon">🎥</span>
            </div>
            <div class="card-value">{{ overviewData?.totalLiveCount || 0 }}</div>
          </div>
          <div class="overview-card">
            <div class="card-header">
              <h3>总用户数</h3>
              <span class="card-icon">👥</span>
            </div>
            <div class="card-value">{{ overviewData?.totalUserCount || 0 }}</div>
          </div>
          <div class="overview-card">
            <div class="card-header">
              <h3>总观看数</h3>
              <span class="card-icon">👁</span>
            </div>
            <div class="card-value">{{ overviewData?.totalViewCount || 0 }}</div>
          </div>
          <div class="overview-card">
            <div class="card-header">
              <h3>总礼物收入</h3>
              <span class="card-icon">🎁</span>
            </div>
            <div class="card-value">￥{{ overviewData?.totalGiftIncome || 0 }}</div>
          </div>
        </div>
        <div class="dashboard-sections">
          <div class="dashboard-section">
            <div class="section-header">
              <h3>热门直播</h3>
            </div>
            <div class="hot-lives">
              <div 
                v-for="(live, index) in overviewData?.hotLives" 
                :key="index" 
                class="hot-live-item"
              >
                <span class="item-title">{{ live.title }}</span>
                <span class="item-value">{{ live.viewCount }} 观看</span>
              </div>
              <div v-if="!overviewData?.hotLives || overviewData.hotLives.length === 0" class="empty-list">
                <p>暂无热门直播数据</p>
              </div>
            </div>
          </div>
          <div class="dashboard-section">
            <div class="section-header">
              <h3>热门主播</h3>
            </div>
            <div class="hot-anchors">
              <div 
                v-for="(anchor, index) in overviewData?.hotAnchors" 
                :key="index" 
                class="hot-anchor-item"
              >
                <span class="item-title">{{ anchor.username }}</span>
                <span class="item-value">{{ anchor.liveCount }} 直播</span>
              </div>
              <div v-if="!overviewData?.hotAnchors || overviewData.hotAnchors.length === 0" class="empty-list">
                <p>暂无热门主播数据</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.admin-dashboard-container {
  display: flex;
  min-height: 100%;
  background-color: #f0f2f5;
  margin: -32px;
  padding: 0;
  border-radius: 0;
  box-shadow: none;
}

.admin-sidebar {
  width: 240px;
  background-color: white;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.08);
  padding: 0;
  position: fixed;
  left: 0;
  top: 72px;
  bottom: 0;
  z-index: 100;
  overflow-y: auto;
  transition: all 0.3s ease;
}

.sidebar-header {
  padding: 24px 20px;
  border-bottom: 1px solid #f0f0f0;
}

.sidebar-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.admin-sidebar ul {
  list-style: none;
  margin: 0;
  padding: 16px 0;
}

.admin-sidebar li {
  display: flex;
  align-items: center;
  padding: 14px 20px;
  cursor: pointer;
  color: #666;
  transition: all 0.3s ease;
  border-left: 3px solid transparent;
}

.admin-sidebar li:hover {
  color: #1890ff;
  background-color: #f0f7ff;
  border-left-color: #1890ff;
}

.admin-sidebar li.active {
  color: #1890ff;
  background-color: #e6f7ff;
  border-left-color: #1890ff;
}

.sidebar-icon {
  font-size: 18px;
  margin-right: 12px;
  width: 24px;
  text-align: center;
}

.sidebar-text {
  font-size: 14px;
  font-weight: 500;
}

.admin-content {
  flex: 1;
  margin-left: 240px;
  padding: 30px;
  overflow-y: auto;
  transition: all 0.3s ease;
}

.dashboard-content {
  max-width: 1400px;
  margin: 0 auto;
  width: 100%;
}

.content-header {
  margin-bottom: 30px;
  padding: 0 20px;
}

.content-header h2 {
  margin: 0 0 8px 0;
  font-size: 32px;
  color: #333;
  font-weight: 600;
}

.content-subtitle {
  margin: 0;
  font-size: 14px;
  color: #999;
}

.overview-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
  margin-bottom: 40px;
  padding: 0 20px;
}

.overview-card {
  background-color: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
}

.overview-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.card-header h3 {
  margin: 0;
  font-size: 14px;
  font-weight: 500;
  color: #666;
}

.card-icon {
  font-size: 20px;
}

.card-value {
  font-size: 32px;
  font-weight: 600;
  color: #333;
  line-height: 1;
}

.dashboard-sections {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
  padding: 0 20px;
}

.dashboard-section {
  background-color: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
}

.section-header {
  margin-bottom: 20px;
}

.section-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.hot-lives, .hot-anchors {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.hot-live-item, .hot-anchor-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background-color: #f9f9f9;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.hot-live-item:hover, .hot-anchor-item:hover {
  background-color: #f0f0f0;
  transform: translateX(4px);
}

.item-title {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-right: 12px;
}

.item-value {
  font-size: 14px;
  font-weight: 500;
  color: #666;
  white-space: nowrap;
}

.empty-list {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 200px;
  color: #999;
  font-size: 14px;
}

.loading {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 600px;
  font-size: 18px;
  color: #666;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .overview-cards {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .dashboard-sections {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .admin-sidebar {
    width: 200px;
  }
  
  .admin-content {
    margin-left: 200px;
    padding: 20px;
  }
  
  .overview-cards {
    grid-template-columns: 1fr;
    padding: 0 16px;
  }
  
  .dashboard-sections {
    padding: 0 16px;
  }
  
  .content-header {
    padding: 0 16px;
  }
  
  .content-header h2 {
    font-size: 24px;
  }
}
</style>