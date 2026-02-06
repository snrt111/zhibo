<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { message } from 'ant-design-vue';
import { liveApi } from '../../api/live';

const router = useRouter();
const loading = ref(false);
const liveList = ref<any[]>([]);

const getLiveList = async () => {
  loading.value = true;
  try {
    const response = await liveApi.getLiveList();
    if (response.code === 200) {
      liveList.value = response.data || [];
    } else {
      message.error(response.message || '获取直播列表失败');
    }
  } catch (error) {
    console.error('获取直播列表错误:', error);
    message.error('获取直播列表失败，请检查网络连接');
  } finally {
    loading.value = false;
  }
};

const handleStartLive = async (id: number) => {
  try {
    const response = await liveApi.startLive(id);
    if (response.code === 200) {
      message.success('开始直播成功');
      getLiveList();
    } else {
      message.error(response.message || '开始直播失败');
    }
  } catch (error) {
    console.error('开始直播错误:', error);
    message.error('开始直播失败，请检查网络连接');
  }
};

const handleEndLive = async (id: number) => {
  try {
    const response = await liveApi.endLive(id);
    if (response.code === 200) {
      message.success('结束直播成功');
      getLiveList();
    } else {
      message.error(response.message || '结束直播失败');
    }
  } catch (error) {
    console.error('结束直播错误:', error);
    message.error('结束直播失败，请检查网络连接');
  }
};

const handleNavigate = (path: string) => {
  router.push(path);
};

onMounted(() => {
  getLiveList();
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
        <li @click="handleNavigate('/admin/analytics')">
          <span class="sidebar-icon">📈</span>
          <span class="sidebar-text">数据分析</span>
        </li>
        <li class="active" @click="handleNavigate('/admin/live')">
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
      <div v-else class="live-management-content">
        <div class="content-header">
          <h2>直播管理</h2>
          <p class="content-subtitle">管理所有直播流和状态</p>
        </div>
        <div class="table-container">
          <table class="live-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>标题</th>
                <th>主播ID</th>
                <th>状态</th>
                <th>观看数</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="live in liveList" :key="live.id" class="table-row">
                <td>{{ live.id }}</td>
                <td class="title-cell">{{ live.title }}</td>
                <td>{{ live.anchorId }}</td>
                <td>
                  <span v-if="live.status === 1" class="status-badge live">直播中</span>
                  <span v-else class="status-badge ended">已结束</span>
                </td>
                <td>{{ live.viewCount || 0 }}</td>
                <td>
                  <button 
                    v-if="live.status !== 1" 
                    class="start-button" 
                    @click="handleStartLive(live.id)"
                  >
                    开始直播
                  </button>
                  <button 
                    v-else 
                    class="end-button" 
                    @click="handleEndLive(live.id)"
                  >
                    结束直播
                  </button>
                </td>
              </tr>
              <tr v-if="liveList.length === 0" class="empty-row">
                <td colspan="6" class="empty-cell">
                  <p>暂无直播数据</p>
                </td>
              </tr>
            </tbody>
          </table>
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

.live-management-content {
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

.table-container {
  background-color: white;
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  overflow: hidden;
  margin: 0 20px;
}

.live-table {
  width: 100%;
  border-collapse: collapse;
}

.live-table th, .live-table td {
  padding: 16px 20px;
  text-align: left;
  border-bottom: 1px solid #f0f0f0;
}

.live-table th {
  background-color: #f9f9f9;
  font-weight: 600;
  color: #333;
  font-size: 14px;
  white-space: nowrap;
}

.table-row {
  transition: all 0.3s ease;
}

.table-row:hover {
  background-color: #f9f9f9;
  transform: translateY(-1px);
}

.title-cell {
  font-weight: 500;
  color: #333;
  max-width: 300px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.status-badge {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
  white-space: nowrap;
}

.status-badge.live {
  background-color: #fff1f0;
  color: #ff4d4f;
}

.status-badge.ended {
  background-color: #f5f5f5;
  color: #8c8c8c;
}

.start-button, .end-button {
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
  white-space: nowrap;
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

.empty-row {
  height: 300px;
}

.empty-cell {
  text-align: center;
  color: #999;
  font-size: 14px;
}

.empty-cell p {
  margin: 0;
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
  .admin-content {
    margin-left: 240px;
  }
  
  .table-container {
    margin: 0 16px;
  }
  
  .content-header {
    padding: 0 16px;
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
  
  .content-header h2 {
    font-size: 24px;
  }
  
  .live-table th, .live-table td {
    padding: 12px 16px;
    font-size: 14px;
  }
  
  .start-button, .end-button {
    padding: 6px 12px;
    font-size: 12px;
  }
}
</style>