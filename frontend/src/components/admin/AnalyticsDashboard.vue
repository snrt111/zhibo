<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { analyticsApi } from '../../api/analytics';
import { handleResponseAsync, errorHandler } from '../../utils/errorHandler';
import VChart from 'vue-echarts';
import { use } from 'echarts/core';
import { CanvasRenderer } from 'echarts/renderers';
import { LineChart, BarChart, PieChart } from 'echarts/charts';
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent,
} from 'echarts/components';

use([
  CanvasRenderer,
  LineChart,
  BarChart,
  PieChart,
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent,
]);

const router = useRouter();
const loading = ref(false);
const liveTrendData = ref<any[]>([]);
const userTrendData = ref<any[]>([]);
const giftIncomeData = ref<any[]>([]);
const hotLivesData = ref<any[]>([]);
const hotAnchorsData = ref<any[]>([]);

const liveTrendOption = computed(() => ({
  tooltip: {
    trigger: 'axis',
    formatter: (params: any) => {
      const data = params[0];
      return `${data.name}<br/>直播数量: ${data.value}`;
    },
  },
  grid: {
    left: '3%',
    right: '4%',
    bottom: '3%',
    containLabel: true,
  },
  xAxis: {
    type: 'category',
    data: liveTrendData.value.map((item) => item.date),
    axisLabel: {
      rotate: 45,
      color: 'rgba(255, 255, 255, 0.85)',
    },
    axisLine: {
      lineStyle: {
        color: 'rgba(255, 255, 255, 0.2)',
      },
    },
    splitLine: {
      lineStyle: {
        color: 'rgba(255, 255, 255, 0.1)',
      },
    },
  },
  yAxis: {
    type: 'value',
    name: '直播数量',
    nameTextStyle: {
      color: 'rgba(255, 255, 255, 0.85)',
    },
    axisLabel: {
      color: 'rgba(255, 255, 255, 0.85)',
    },
    axisLine: {
      lineStyle: {
        color: 'rgba(255, 255, 255, 0.2)',
      },
    },
    splitLine: {
      lineStyle: {
        color: 'rgba(255, 255, 255, 0.1)',
      },
    },
  },
  series: [
    {
      name: '直播数量',
      type: 'line',
      smooth: true,
      data: liveTrendData.value.map((item) => item.count),
      areaStyle: {
        opacity: 0.3,
      },
      lineStyle: {
        width: 3,
      },
      itemStyle: {
        color: '#5470c6',
      },
    },
  ],
}));

const userTrendOption = computed(() => ({
  tooltip: {
    trigger: 'axis',
    formatter: (params: any) => {
      const data = params[0];
      return `${data.name}<br/>新增用户: ${data.value}`;
    },
  },
  grid: {
    left: '3%',
    right: '4%',
    bottom: '3%',
    containLabel: true,
  },
  xAxis: {
    type: 'category',
    data: userTrendData.value.map((item) => item.date),
    axisLabel: {
      rotate: 45,
      color: 'rgba(255, 255, 255, 0.85)',
    },
    axisLine: {
      lineStyle: {
        color: 'rgba(255, 255, 255, 0.2)',
      },
    },
    splitLine: {
      lineStyle: {
        color: 'rgba(255, 255, 255, 0.1)',
      },
    },
  },
  yAxis: {
    type: 'value',
    name: '新增用户',
    nameTextStyle: {
      color: 'rgba(255, 255, 255, 0.85)',
    },
    axisLabel: {
      color: 'rgba(255, 255, 255, 0.85)',
    },
    axisLine: {
      lineStyle: {
        color: 'rgba(255, 255, 255, 0.2)',
      },
    },
    splitLine: {
      lineStyle: {
        color: 'rgba(255, 255, 255, 0.1)',
      },
    },
  },
  series: [
    {
      name: '新增用户',
      type: 'bar',
      data: userTrendData.value.map((item) => item.count),
      itemStyle: {
        color: '#91cc75',
        borderRadius: [4, 4, 0, 0],
      },
    },
  ],
}));

const giftIncomeOption = computed(() => ({
  tooltip: {
    trigger: 'axis',
    formatter: (params: any) => {
      const data = params[0];
      return `${data.name}<br/>礼物收入: ¥${data.value}`;
    },
  },
  grid: {
    left: '3%',
    right: '4%',
    bottom: '3%',
    containLabel: true,
  },
  xAxis: {
    type: 'category',
    data: giftIncomeData.value.map((item) => item.date),
    axisLabel: {
      rotate: 45,
      color: 'rgba(255, 255, 255, 0.85)',
    },
    axisLine: {
      lineStyle: {
        color: 'rgba(255, 255, 255, 0.2)',
      },
    },
    splitLine: {
      lineStyle: {
        color: 'rgba(255, 255, 255, 0.1)',
      },
    },
  },
  yAxis: {
    type: 'value',
    name: '礼物收入(元)',
    nameTextStyle: {
      color: 'rgba(255, 255, 255, 0.85)',
    },
    axisLabel: {
      color: 'rgba(255, 255, 255, 0.85)',
    },
    axisLine: {
      lineStyle: {
        color: 'rgba(255, 255, 255, 0.2)',
      },
    },
    splitLine: {
      lineStyle: {
        color: 'rgba(255, 255, 255, 0.1)',
      },
    },
  },
  series: [
    {
      name: '礼物收入',
      type: 'line',
      smooth: true,
      data: giftIncomeData.value.map((item) => item.amount),
      areaStyle: {
        opacity: 0.3,
      },
      lineStyle: {
        width: 3,
      },
      itemStyle: {
        color: '#fac858',
      },
    },
  ],
}));

const getLiveTrend = async () => {
  try {
    const response = await analyticsApi.getLiveTrend();
    await handleResponseAsync(response, (data) => {
      if (data) {
        liveTrendData.value = data;
      }
    });
  } catch (error) {
    errorHandler.handle(error, false);
  }
};

const getUserTrend = async () => {
  try {
    const response = await analyticsApi.getUserTrend();
    await handleResponseAsync(response, (data) => {
      if (data) {
        userTrendData.value = data;
      }
    });
  } catch (error) {
    errorHandler.handle(error, false);
  }
};

const getGiftIncome = async () => {
  try {
    const response = await analyticsApi.getGiftIncome();
    await handleResponseAsync(response, (data) => {
      if (data) {
        giftIncomeData.value = data;
      }
    });
  } catch (error) {
    errorHandler.handle(error, false);
  }
};

const getHotLives = async () => {
  try {
    const response = await analyticsApi.getHotLives();
    await handleResponseAsync(response, (data) => {
      if (data) {
        hotLivesData.value = data;
      }
    });
  } catch (error) {
    errorHandler.handle(error, false);
  }
};

const getHotAnchors = async () => {
  try {
    const response = await analyticsApi.getHotAnchors();
    await handleResponseAsync(response, (data) => {
      if (data) {
        hotAnchorsData.value = data;
      }
    });
  } catch (error) {
    errorHandler.handle(error, false);
  }
};

const handleNavigate = (path: string) => {
  router.push(path);
};

const formatNumber = (num: number) => {
  if (num >= 10000) {
    return (num / 10000).toFixed(1) + '万';
  }
  return num.toString();
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
        <li @click="handleNavigate('/admin/monitor')">
          <span class="sidebar-icon">📊</span>
          <span class="sidebar-text">系统监控</span>
        </li>
        <li @click="handleNavigate('/admin/live')">
          <span class="sidebar-icon">🎥</span>
          <span class="sidebar-text">直播管理</span>
        </li>
        <li @click="handleNavigate('/admin/user')">
          <span class="sidebar-icon">👥</span>
          <span class="sidebar-text">用户管理</span>
        </li>
        <li @click="handleNavigate('/admin/gift')">
          <span class="sidebar-icon">🎁</span>
          <span class="sidebar-text">礼物管理</span>
        </li>
        <li @click="handleNavigate('/admin/ai-config')">
          <span class="sidebar-icon">🤖</span>
          <span class="sidebar-text">AI配置管理</span>
        </li>
        <li @click="handleNavigate('/admin/audit')">
          <span class="sidebar-icon">🔍</span>
          <span class="sidebar-text">内容审核</span>
        </li>
        <li @click="handleNavigate('/admin/report')">
          <span class="sidebar-icon">⚠️</span>
          <span class="sidebar-text">举报管理</span>
        </li>
        <li @click="handleNavigate('/admin/withdraw')">
          <span class="sidebar-icon">💰</span>
          <span class="sidebar-text">提现审核</span>
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

        <div class="trend-charts">
          <div class="trend-chart">
            <div class="chart-header">
              <h3>直播趋势</h3>
              <span class="chart-icon">📈</span>
            </div>
            <div class="chart-container">
              <v-chart
                v-if="liveTrendData.length > 0"
                :option="liveTrendOption"
                autoresize
                style="height: 300px; width: 100%"
              />
              <div v-else class="empty-chart">
                <p>暂无直播趋势数据</p>
              </div>
            </div>
          </div>
          <div class="trend-chart">
            <div class="chart-header">
              <h3>用户趋势</h3>
              <span class="chart-icon">👥</span>
            </div>
            <div class="chart-container">
              <v-chart
                v-if="userTrendData.length > 0"
                :option="userTrendOption"
                autoresize
                style="height: 300px; width: 100%"
              />
              <div v-else class="empty-chart">
                <p>暂无用户趋势数据</p>
              </div>
            </div>
          </div>
          <div class="trend-chart full-width">
            <div class="chart-header">
              <h3>礼物收入趋势</h3>
              <span class="chart-icon">🎁</span>
            </div>
            <div class="chart-container">
              <v-chart
                v-if="giftIncomeData.length > 0"
                :option="giftIncomeOption"
                autoresize
                style="height: 300px; width: 100%"
              />
              <div v-else class="empty-chart">
                <p>暂无礼物收入数据</p>
              </div>
            </div>
          </div>
        </div>

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
                <span class="rank" :class="{ 'top-three': index < 3 }">
                  {{ Number(index) + 1 }}
                </span>
                <div class="item-content">
                  <span class="title">{{ live.title }}</span>
                  <span class="anchor">主播: {{ live.anchorName }}</span>
                </div>
                <span class="value">{{ formatNumber(live.viewCount) }} 观看</span>
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
                <span class="rank" :class="{ 'top-three': index < 3 }">
                  {{ Number(index) + 1 }}
                </span>
                <div class="item-content">
                  <span class="title">{{ anchor.nickname || anchor.username }}</span>
                  <span class="anchor">{{ anchor.liveCount }} 场直播</span>
                </div>
                <span class="value">¥{{ formatNumber(anchor.totalIncome) }}</span>
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
  min-height: calc(100vh - 72px);
  background: linear-gradient(180deg, #0f0f0f 0%, #1a1a1a 100%);
  margin: -32px;
  padding: 0;
  border-radius: 0;
  box-shadow: none;
}

.admin-sidebar {
  width: 240px;
  background: linear-gradient(180deg, #1a1a2e 0%, #16213e 100%);
  border-right: 1px solid rgba(255, 255, 255, 0.08);
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
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.sidebar-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #fff;
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
  color: rgba(255, 255, 255, 0.85);
  transition: all 0.3s ease;
  border-left: 3px solid transparent;
}

.admin-sidebar li:hover {
  color: #fff;
  background: rgba(255, 71, 87, 0.15);
  border-left-color: #ff4757;
}

.admin-sidebar li.active {
  color: #fff;
  background: linear-gradient(90deg, rgba(255, 71, 87, 0.2) 0%, rgba(255, 107, 129, 0.1) 100%);
  border-left-color: #ff4757;
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
  padding: 0;
  min-height: calc(100vh - 72px);
  overflow-y: auto;
  transition: all 0.3s ease;
}

.analytics-content {
  max-width: 1600px;
  margin: 0 auto;
  width: 100%;
  padding: 32px;
}

.content-header {
  margin-bottom: 32px;
}

.content-header h2 {
  margin: 0 0 8px 0;
  font-size: 28px;
  color: #fff;
  font-weight: 600;
}

.content-subtitle {
  margin: 0;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.8);
}

.trend-charts {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
  margin-bottom: 32px;
}

.trend-chart {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 12px;
  padding: 24px;
  transition: all 0.3s ease;
}

.trend-chart:hover {
  background: rgba(255, 255, 255, 0.08);
  border-color: rgba(255, 255, 255, 0.12);
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
  color: #fff;
}

.chart-icon {
  font-size: 20px;
}

.chart-container {
  height: 300px;
}

.empty-chart {
  height: 300px;
  display: flex;
  justify-content: center;
  align-items: center;
  background: rgba(255, 255, 255, 0.03);
  border-radius: 8px;
  border: 1px dashed rgba(255, 255, 255, 0.15);
  color: rgba(255, 255, 255, 0.7);
  font-size: 14px;
}

.hot-lists {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
}

.hot-list {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 12px;
  padding: 24px;
  transition: all 0.3s ease;
}

.hot-list:hover {
  background: rgba(255, 255, 255, 0.08);
  border-color: rgba(255, 255, 255, 0.12);
}

.list-header {
  margin-bottom: 20px;
}

.list-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #fff;
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
  background: rgba(255, 255, 255, 0.03);
  border-radius: 8px;
  transition: all 0.3s ease;
}

.hot-item:hover {
  background: rgba(255, 255, 255, 0.06);
  transform: translateX(4px);
}

.hot-item .rank {
  width: 30px;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.7);
  font-size: 14px;
  text-align: center;
}

.hot-item .rank.top-three {
  color: #ff6b81;
  font-size: 16px;
}

.hot-item .item-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  margin-left: 16px;
  gap: 4px;
}

.hot-item .title {
  font-size: 14px;
  font-weight: 500;
  color: #fff;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.hot-item .anchor {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.7);
}

.hot-item .value {
  color: #00cec9;
  font-size: 14px;
  font-weight: 600;
  white-space: nowrap;
}

.empty-list {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 200px;
  color: rgba(255, 255, 255, 0.7);
  font-size: 14px;
  background: rgba(255, 255, 255, 0.03);
  border-radius: 8px;
  border: 1px dashed rgba(255, 255, 255, 0.15);
}

.loading {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 600px;
  font-size: 18px;
  color: rgba(255, 255, 255, 0.85);
}

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
  }

  .analytics-content {
    padding: 20px;
  }

  .content-header h2 {
    font-size: 24px;
  }

  .trend-chart,
  .hot-list {
    padding: 20px;
  }

  .chart-container,
  .empty-chart {
    height: 250px;
  }
}
</style>
