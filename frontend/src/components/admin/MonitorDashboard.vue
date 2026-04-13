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
        <li @click="handleNavigate('/admin/analytics')">
          <span class="sidebar-icon">📈</span>
          <span class="sidebar-text">数据分析</span>
        </li>
        <li class="active" @click="handleNavigate('/admin/monitor')">
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
      <div class="monitor-dashboard">
        <div class="content-header">
          <h2>系统监控</h2>
          <p class="content-subtitle">实时监控系统运行状态</p>
        </div>
        <div class="overview-cards">
          <!-- 系统概览卡片 -->
          <div class="overview-card">
            <div class="card-header">
              <h3>在线用户数</h3>
              <span class="card-icon">👥</span>
            </div>
            <div class="card-value">{{ metrics.onlineUserCount }}</div>
          </div>
          <div class="overview-card">
            <div class="card-header">
              <h3>直播流数量</h3>
              <span class="card-icon">🎥</span>
            </div>
            <div class="card-value">{{ metrics.liveStreamCount }}</div>
          </div>
          <div class="overview-card">
            <div class="card-header">
              <h3>弹幕数量</h3>
              <span class="card-icon">💬</span>
            </div>
            <div class="card-value">{{ metrics.danmakuCount }}</div>
          </div>
          <div class="overview-card">
            <div class="card-header">
              <h3>礼物数量</h3>
              <span class="card-icon">🎁</span>
            </div>
            <div class="card-value">{{ metrics.giftCount }}</div>
          </div>
        </div>

        <!-- 图表区域 -->
        <div class="chart-sections">
          <div class="chart-section">
            <div class="section-header">
              <h3>在线用户趋势</h3>
            </div>
            <div class="chart-container">
              <v-chart :option="onlineUserOption" style="width: 100%; height: 300px;" />
            </div>
          </div>
          <div class="chart-section">
            <div class="section-header">
              <h3>直播流趋势</h3>
            </div>
            <div class="chart-container">
              <v-chart :option="liveStreamOption" style="width: 100%; height: 300px;" />
            </div>
          </div>
          <div class="chart-section">
            <div class="section-header">
              <h3>弹幕趋势</h3>
            </div>
            <div class="chart-container">
              <v-chart :option="danmakuOption" style="width: 100%; height: 300px;" />
            </div>
          </div>
          <div class="chart-section">
            <div class="section-header">
              <h3>礼物趋势</h3>
            </div>
            <div class="chart-container">
              <v-chart :option="giftOption" style="width: 100%; height: 300px;" />
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { use } from 'echarts/core'
import { BarChart, LineChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, TitleComponent, LegendComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import VChart from 'vue-echarts'

use([
  BarChart,
  LineChart,
  GridComponent,
  TooltipComponent,
  TitleComponent,
  LegendComponent,
  CanvasRenderer
])

const router = useRouter()

const handleNavigate = (path: string) => {
  router.push(path)
}

// 监控数据
const metrics = ref({
  onlineUserCount: 0,
  liveStreamCount: 0,
  danmakuCount: 0,
  giftCount: 0
})

// 趋势数据
const timeData = ref<string[]>([])
const onlineUserData = ref<number[]>([])
const liveStreamData = ref<number[]>([])
const danmakuData = ref<number[]>([])
const giftData = ref<number[]>([])

// 生成时间标签
const generateTimeLabels = () => {
  const now = new Date()
  const timeLabels: string[] = []
  for (let i = 23; i >= 0; i--) {
    const hour = new Date(now.getTime() - i * 60 * 60 * 1000)
    timeLabels.push(`${hour.getHours()}:00`)
  }
  timeData.value = timeLabels
}

// 从后端API获取监控数据
const fetchMetrics = async () => {
  try {
    const response = await fetch('/api/metrics/realtime')
    const data = await response.json()
    
    if (data.code === 200 && data.data) {
      // 更新实时指标
      metrics.value = {
        onlineUserCount: data.data.onlineUserCount || 0,
        liveStreamCount: data.data.liveStreamCount || 0,
        danmakuCount: data.data.danmakuCount || 0,
        giftCount: data.data.giftCount || 0
      }
      
      // 更新趋势数据（这里简单处理，实际项目中可以从后端获取历史数据）
      // 为了保持图表显示，我们使用当前值作为最新数据点
      const currentTime = new Date().getHours() + ':00'
      const index = timeData.value.indexOf(currentTime)
      
      if (index !== -1) {
        // 更新对应时间点的数据
        onlineUserData.value[index] = metrics.value.onlineUserCount
        liveStreamData.value[index] = metrics.value.liveStreamCount
        danmakuData.value[index] = metrics.value.danmakuCount
        giftData.value[index] = metrics.value.giftCount
      } else {
        // 如果时间点不存在，添加到数组末尾并移除第一个元素
        onlineUserData.value.push(metrics.value.onlineUserCount)
        liveStreamData.value.push(metrics.value.liveStreamCount)
        danmakuData.value.push(metrics.value.danmakuCount)
        giftData.value.push(metrics.value.giftCount)
        
        if (onlineUserData.value.length > 24) {
          onlineUserData.value.shift()
          liveStreamData.value.shift()
          danmakuData.value.shift()
          giftData.value.shift()
        }
      }
    }
  } catch (error) {
    console.error('获取监控数据失败:', error)
  }
}

// 图表配置
const onlineUserOption = computed(() => ({
  tooltip: {
    trigger: 'axis',
    backgroundColor: 'rgba(26, 32, 44, 0.9)',
    borderColor: 'rgba(255, 255, 255, 0.1)',
    textStyle: {
      color: '#fff'
    }
  },
  xAxis: {
    type: 'category',
    data: timeData.value,
    axisLine: {
      lineStyle: {
        color: 'rgba(255, 255, 255, 0.1)'
      }
    },
    axisLabel: {
      color: 'rgba(255, 255, 255, 0.7)'
    }
  },
  yAxis: {
    type: 'value',
    axisLine: {
      lineStyle: {
        color: 'rgba(255, 255, 255, 0.1)'
      }
    },
    axisLabel: {
      color: 'rgba(255, 255, 255, 0.7)'
    },
    splitLine: {
      lineStyle: {
        color: 'rgba(255, 255, 255, 0.05)'
      }
    }
  },
  series: [{
    data: onlineUserData.value,
    type: 'line',
    smooth: true,
    lineStyle: {
      color: '#3f8600'
    },
    areaStyle: {
      color: {
        type: 'linear',
        x: 0,
        y: 0,
        x2: 0,
        y2: 1,
        colorStops: [{
          offset: 0, color: 'rgba(63, 134, 0, 0.3)'
        }, {
          offset: 1, color: 'rgba(63, 134, 0, 0.1)'
        }]
      }
    }
  }]
}))

const liveStreamOption = computed(() => ({
  tooltip: {
    trigger: 'axis',
    backgroundColor: 'rgba(26, 32, 44, 0.9)',
    borderColor: 'rgba(255, 255, 255, 0.1)',
    textStyle: {
      color: '#fff'
    }
  },
  xAxis: {
    type: 'category',
    data: timeData.value,
    axisLine: {
      lineStyle: {
        color: 'rgba(255, 255, 255, 0.1)'
      }
    },
    axisLabel: {
      color: 'rgba(255, 255, 255, 0.7)'
    }
  },
  yAxis: {
    type: 'value',
    axisLine: {
      lineStyle: {
        color: 'rgba(255, 255, 255, 0.1)'
      }
    },
    axisLabel: {
      color: 'rgba(255, 255, 255, 0.7)'
    },
    splitLine: {
      lineStyle: {
        color: 'rgba(255, 255, 255, 0.05)'
      }
    }
  },
  series: [{
    data: liveStreamData.value,
    type: 'line',
    smooth: true,
    lineStyle: {
      color: '#1890ff'
    },
    areaStyle: {
      color: {
        type: 'linear',
        x: 0,
        y: 0,
        x2: 0,
        y2: 1,
        colorStops: [{
          offset: 0, color: 'rgba(24, 144, 255, 0.3)'
        }, {
          offset: 1, color: 'rgba(24, 144, 255, 0.1)'
        }]
      }
    }
  }]
}))

const danmakuOption = computed(() => ({
  tooltip: {
    trigger: 'axis',
    backgroundColor: 'rgba(26, 32, 44, 0.9)',
    borderColor: 'rgba(255, 255, 255, 0.1)',
    textStyle: {
      color: '#fff'
    }
  },
  xAxis: {
    type: 'category',
    data: timeData.value,
    axisLine: {
      lineStyle: {
        color: 'rgba(255, 255, 255, 0.1)'
      }
    },
    axisLabel: {
      color: 'rgba(255, 255, 255, 0.7)'
    }
  },
  yAxis: {
    type: 'value',
    axisLine: {
      lineStyle: {
        color: 'rgba(255, 255, 255, 0.1)'
      }
    },
    axisLabel: {
      color: 'rgba(255, 255, 255, 0.7)'
    },
    splitLine: {
      lineStyle: {
        color: 'rgba(255, 255, 255, 0.05)'
      }
    }
  },
  series: [{
    data: danmakuData.value,
    type: 'bar',
    itemStyle: {
      color: '#faad14'
    }
  }]
}))

const giftOption = computed(() => ({
  tooltip: {
    trigger: 'axis',
    backgroundColor: 'rgba(26, 32, 44, 0.9)',
    borderColor: 'rgba(255, 255, 255, 0.1)',
    textStyle: {
      color: '#fff'
    }
  },
  xAxis: {
    type: 'category',
    data: timeData.value,
    axisLine: {
      lineStyle: {
        color: 'rgba(255, 255, 255, 0.1)'
      }
    },
    axisLabel: {
      color: 'rgba(255, 255, 255, 0.7)'
    }
  },
  yAxis: {
    type: 'value',
    axisLine: {
      lineStyle: {
        color: 'rgba(255, 255, 255, 0.1)'
      }
    },
    axisLabel: {
      color: 'rgba(255, 255, 255, 0.7)'
    },
    splitLine: {
      lineStyle: {
        color: 'rgba(255, 255, 255, 0.05)'
      }
    }
  },
  series: [{
    data: giftData.value,
    type: 'bar',
    itemStyle: {
      color: '#f5222d'
    }
  }]
}))

onMounted(() => {
  generateTimeLabels()
  // 初始化趋势数据数组
  onlineUserData.value = new Array(24).fill(0)
  liveStreamData.value = new Array(24).fill(0)
  danmakuData.value = new Array(24).fill(0)
  giftData.value = new Array(24).fill(0)
  
  // 首次获取数据
  fetchMetrics()
  
  // 每5秒更新一次数据
  setInterval(fetchMetrics, 5000)
})
</script>

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

.monitor-dashboard {
  width: 100%;
  min-height: 100%;
  padding: 24px;
}

.content-header {
  margin-bottom: 30px;
  padding-bottom: 16px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
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

.overview-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 32px;
}

.overview-card {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 12px;
  padding: 24px;
  transition: all 0.3s ease;
}

.overview-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.3);
  border-color: rgba(255, 71, 87, 0.3);
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
  color: rgba(255, 255, 255, 0.85);
}

.card-icon {
  font-size: 20px;
}

.card-value {
  font-size: 32px;
  font-weight: 600;
  color: #fff;
  line-height: 1;
}

.chart-sections {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.chart-section {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 12px;
  padding: 24px;
  transition: all 0.3s ease;
}

.chart-section:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.3);
  border-color: rgba(255, 71, 87, 0.3);
}

.section-header {
  margin-bottom: 20px;
  padding-bottom: 12px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.section-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #fff;
}

.chart-container {
  width: 100%;
  height: 300px;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .overview-cards {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .chart-sections {
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
  
  .monitor-dashboard {
    padding: 20px;
  }
  
  .overview-cards {
    grid-template-columns: 1fr;
    padding: 0 16px;
  }
  
  .chart-sections {
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
