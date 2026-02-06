<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { analyticsApi } from '../../api/analytics';

const router = useRouter();
const loading = ref(false);
const liveTrendData = ref<any>(null);
const userTrendData = ref<any>(null);
const giftIncomeData = ref<any>(null);
const hotLivesData = ref<any>(null);
const hotAnchorsData = ref<any>(null);

const getLiveTrend = async () => {
  try {
    const response = await analyticsApi.getLiveTrend();
    if (response.code === 200) {
      liveTrendData.value = response.data;
    }
  } catch (error) {
    console.error('获取直播趋势数据错误:', error);
  }
};

const getUserTrend = async () => {
  try {
    const response = await analyticsApi.getUserTrend();
    if (response.code === 200) {
      userTrendData.value = response.data;
    }
  } catch (error) {
    console.error('获取用户趋势数据错误:', error);
  }
};

const getGiftIncome = async () => {
  try {
    const response = await analyticsApi.getGiftIncome();
    if (response.code === 200) {
      giftIncomeData.value = response.data;
    }
  } catch (error) {
    console.error('获取礼物收入数据错误:', error);
  }
};

const getHotLives = async () => {
  try {
    const response = await analyticsApi.getHotLives();
    if (response.code === 200) {
      hotLivesData.value = response.data;
    }
  } catch (error) {
    console.error('获取热门直播数据错误:', error);
  }
};

const getHotAnchors = async () => {
  try {
    const response = await analyticsApi.getHotAnchors();
    if (response.code === 200) {
      hotAnchorsData.value = response.data;
    }
  } catch (error) {
    console.error('获取热门主播数据错误:', error);
  }
};

const handleNavigate = (path: string) => {
  router.push(path);
};

onMounted(() => {
  getLiveTrend();
  getUserTrend();
  getGiftIncome();
  getHotLives();
  getHotAnchors();
});
</script>

<template>
  <div class="admin-dashboard-container">
    <div class="admin-sidebar">
      <div class="sidebar-header">
        <h3>管理中心</h3>
      </div>
      <ul>
        <li @click="handleNavigate('/admin')">
          <span class="sidebar-icon">📊</span>
          <span class="sidebar-text">仪表板</span>
        </li>
        <li class="active" @click="handleNavigate('/admin/analytics')">
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
      <div v-else class="analytics-content">
        <div class="content-header">
          <h2>数据分析</h2>
          <p class="content-subtitle">实时分析平台运营数据</p>
        </div>
        
        <!-- 趋势图表 -->
        <div class="trend-charts">
          <div class="trend-chart">
            <div class="chart-header">
              <h3>直播趋势</h3>
              <span class="chart-icon">📈</span>
            </div>
            <div class="chart-placeholder">
              <!-- 这里可以使用ECharts等图表库 -->
              <p>直播趋势图表</p>
            </div>
          </div>
          <div class="trend-chart">
            <div class="chart-header">
              <h3>用户趋势</h3>
              <span class="chart-icon">👥</span>
            </div>
            <div class="chart-placeholder">
              <!-- 这里可以使用ECharts等图表库 -->
              <p>用户趋势图表</p>
            </div>
          </div>
          <div class="trend-chart full-width">
            <div class="chart-header">
              <h3>礼物收入趋势</h3>
              <span class="chart-icon">🎁</span>
            </div>
            <div class="chart-placeholder">
              <!-- 这里可以使用ECharts等图表库 -->
              <p>礼物收入趋势图表</p>
            </div>
          </div>
        </div>
        
        <!-- 热门列表 -->
        <div class="hot-lists">
          <div class="hot-list">
            <div class="list-header">
              <h3>热门直播</h3>
            </div>
            <div class="hot-items">
              <div 
                v-for="(live, index) in hotLivesData" 
                :key="index" 
                class="hot-item"
              >
                <span class="rank">{{ Number(index) + 1 }}</span>
                <span class="title">{{ live.title }}</span>
                <span class="value">{{ live.viewCount }} 观看</span>
              </div>
              <div v-if="!hotLivesData || hotLivesData.length === 0" class="empty-list">
                <p>暂无热门直播数据</p>
              </div>
            </div>
          </div>
          <div class="hot-list">
            <div class="list-header">
              <h3>热门主播</h3>
            </div>
            <div class="hot-items">
              <div 
                v-for="(anchor, index) in hotAnchorsData" 
                :key="index" 
                class="hot-item"
              >
                <span class="rank">{{ Number(index) + 1 }}</span>
                <span class="title">{{ anchor.username }}</span>
                <span class="value">{{ anchor.liveCount }} 直播</span>
              </div>
              <div v-if="!hotAnchorsData || hotAnchorsData.length === 0" class="empty-list">
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

.analytics-content {
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

.trend-charts {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
  margin-bottom: 40px;
  padding: 0 20px;
}

.trend-chart {
  background-color: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
}

.trend-chart:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.trend-chart.full-width {
  grid-column: span 2;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.chart-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.chart-icon {
  font-size: 20px;
}

.chart-placeholder {
  height: 300px;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f9f9f9;
  border-radius: 8px;
  border: 1px dashed #e8e8e8;
  color: #999;
  font-size: 14px;
}

.hot-lists {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
  padding: 0 20px;
}

.hot-list {
  background-color: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
}

.hot-list:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.list-header {
  margin-bottom: 20px;
}

.list-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.hot-items {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.hot-item {
  display: flex;
  align-items: center;
  padding: 16px;
  background-color: #f9f9f9;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.hot-item:hover {
  background-color: #f0f0f0;
  transform: translateX(4px);
}

.hot-item .rank {
  width: 30px;
  font-weight: 600;
  color: #1890ff;
  font-size: 14px;
}

.hot-item .title {
  flex: 1;
  margin-left: 16px;
  font-size: 14px;
  font-weight: 500;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.hot-item .value {
  color: #666;
  font-size: 14px;
  font-weight: 500;
  white-space: nowrap;
}

.empty-list {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 200px;
  color: #999;
  font-size: 14px;
  background-color: #f9f9f9;
  border-radius: 8px;
  border: 1px dashed #e8e8e8;
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
  .trend-charts {
    grid-template-columns: 1fr;
  }
  
  .trend-chart.full-width {
    grid-column: span 1;
  }
  
  .hot-lists {
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
  
  .content-header {
    padding: 0 16px;
  }
  
  .content-header h2 {
    font-size: 24px;
  }
  
  .trend-charts,
  .hot-lists {
    padding: 0 16px;
  }
  
  .trend-chart,
  .hot-list {
    padding: 20px;
  }
  
  .chart-placeholder {
    height: 250px;
  }
}
</style>