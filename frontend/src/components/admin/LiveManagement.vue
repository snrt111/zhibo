<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { message, Modal } from 'ant-design-vue';
import { liveApi } from '../../api/live';
import { handleResponseAsync, errorHandler } from '../../utils/errorHandler';

const router = useRouter();
const loading = ref(false);
const liveList = ref<any[]>([]);
const categoryList = ref<any[]>([]);
const selectedIds = ref<number[]>([]);
const batchLoading = ref(false);

const filterStatus = ref<number | null>(null);
const filterCategory = ref<number | null>(null);
const filterKeyword = ref('');

const pagination = ref({
  current: 1,
  pageSize: 5,
  total: 0
});

const isAllSelected = computed(() => {
  return selectedIds.value.length === liveList.value.length && liveList.value.length > 0;
});

const hasSelection = computed(() => {
  return selectedIds.value.length > 0;
});

const toggleSelectAll = () => {
  if (isAllSelected.value) {
    selectedIds.value = [];
  } else {
    selectedIds.value = liveList.value.map(live => live.id);
  }
};

const toggleSelect = (id: number) => {
  const index = selectedIds.value.indexOf(id);
  if (index > -1) {
    selectedIds.value.splice(index, 1);
  } else {
    selectedIds.value.push(id);
  }
};

const isSelected = (id: number) => {
  return selectedIds.value.includes(id);
};

const getCategoryList = async () => {
  try {
    const response = await liveApi.getCategoryList();
    await handleResponseAsync(response, (data) => {
      categoryList.value = data || [];
    });
  } catch (error) {
    categoryList.value = [];
  }
};

const getCategoryName = (categoryId: number | null) => {
  if (!categoryId) return '-';
  const category = categoryList.value.find(c => c.id === categoryId);
  return category?.name || '-';
};

const getLiveList = async () => {
  loading.value = true;
  try {
    const response = await fetch(`/api/live/admin/list?page=${pagination.value.current}&size=${pagination.value.pageSize}&status=${filterStatus.value ?? ''}&categoryId=${filterCategory.value ?? ''}&keyword=${filterKeyword.value}`, {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    });
    const result = await response.json();
    if (result.code === 200) {
      liveList.value = result.data?.records || [];
      pagination.value.total = result.data?.total || 0;
    } else {
      message.error(result.message || '获取直播列表失败');
    }
  } catch (error) {
    errorHandler.handle(error);
  } finally {
    loading.value = false;
  }
};

const handlePageChange = (page: number) => {
  pagination.value.current = page;
  getLiveList();
};

const handlePageSizeChange = () => {
  pagination.value.current = 1;
  getLiveList();
};

const handleStartLive = async (id: number) => {
  try {
    const response = await liveApi.startLive(id);
    await handleResponseAsync(response, () => {
      message.success('开始直播成功');
      getLiveList();
    });
  } catch (error) {
    errorHandler.handle(error);
  }
};

const handleEndLive = async (id: number) => {
  try {
    const response = await liveApi.endLive(id);
    await handleResponseAsync(response, () => {
      message.success('结束直播成功');
      getLiveList();
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
      getLiveList();
    });
  } catch (error) {
    errorHandler.handle(error);
  }
};

const handleBatchEnd = () => {
  Modal.confirm({
    title: '确认批量结束直播',
    content: `确定要结束选中的 ${selectedIds.value.length} 个直播吗？`,
    okText: '确定',
    cancelText: '取消',
    okType: 'danger',
    onOk: async () => {
      batchLoading.value = true;
      try {
        const response = await liveApi.batchEnd(selectedIds.value);
        await handleResponseAsync(response, () => {
          message.success('批量结束直播成功');
          selectedIds.value = [];
          getLiveList();
        });
      } catch (error) {
        errorHandler.handle(error);
      } finally {
        batchLoading.value = false;
      }
    }
  });
};

const handleBatchDelete = () => {
  Modal.confirm({
    title: '确认批量删除直播',
    content: `确定要删除选中的 ${selectedIds.value.length} 个直播吗？此操作不可恢复。`,
    okText: '确定',
    cancelText: '取消',
    okType: 'danger',
    onOk: async () => {
      batchLoading.value = true;
      try {
        const response = await liveApi.batchDelete(selectedIds.value);
        await handleResponseAsync(response, () => {
          message.success('批量删除直播成功');
          selectedIds.value = [];
          getLiveList();
        });
      } catch (error) {
        errorHandler.handle(error);
      } finally {
        batchLoading.value = false;
      }
    }
  });
};

const handleNavigate = (path: string) => {
  router.push(path);
};

onMounted(() => {
  getCategoryList();
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
        <li @click="handleNavigate('/admin/monitor')">
          <span class="sidebar-icon">📊</span>
          <span class="sidebar-text">系统监控</span>
        </li>
        <li class="active" @click="handleNavigate('/admin/live')">
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
      <div v-else class="live-management-content">
        <div class="content-header">
          <div class="header-left">
            <h2>直播管理</h2>
            <p class="content-subtitle">管理所有直播流和状态</p>
          </div>
          <div v-if="hasSelection" class="batch-actions">
            <span class="selection-info">已选择 {{ selectedIds.length }} 项</span>
            <button class="batch-button end" @click="handleBatchEnd" :disabled="batchLoading">
              批量结束
            </button>
            <button class="batch-button delete" @click="handleBatchDelete" :disabled="batchLoading">
              批量删除
            </button>
          </div>
        </div>
        <div class="filter-bar">
          <div class="filter-item">
            <label>搜索：</label>
            <input 
              type="text" 
              v-model="filterKeyword" 
              @keyup.enter="getLiveList"
              placeholder="搜索标题"
              class="filter-input"
            />
          </div>
          <div class="filter-item">
            <label>状态：</label>
            <select v-model="filterStatus" @change="getLiveList" class="filter-select">
              <option :value="null">全部</option>
              <option :value="0">未开始</option>
              <option :value="1">直播中</option>
              <option :value="2">已结束</option>
            </select>
          </div>
          <div class="filter-item">
            <label>分类：</label>
            <select v-model="filterCategory" @change="getLiveList" class="filter-select">
              <option :value="null">全部</option>
              <option v-for="cat in categoryList" :key="cat.id" :value="cat.id">{{ cat.name }}</option>
            </select>
          </div>
          <button @click="getLiveList" class="search-button">查询</button>
          <button @click="filterStatus = null; filterCategory = null; filterKeyword = ''; getLiveList()" class="clear-filter-button">
            清除筛选
          </button>
        </div>
        <div class="table-container">
          <table class="live-table">
            <thead>
              <tr>
                <th class="checkbox-cell">
                  <input type="checkbox" :checked="isAllSelected" @change="toggleSelectAll" class="select-checkbox" />
                </th>
                <th>ID</th>
                <th>标题</th>
                <th>主播ID</th>
                <th>分类</th>
                <th>状态</th>
                <th>观看数</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="live in liveList" :key="live.id" class="table-row" :class="{ 'selected': isSelected(live.id) }">
                <td class="checkbox-cell">
                  <input type="checkbox" :checked="isSelected(live.id)" @change="toggleSelect(live.id)" class="select-checkbox" />
                </td>
                <td class="text-cell">{{ live.id }}</td>
                <td class="title-cell">{{ live.title }}</td>
                <td class="text-cell">{{ live.userId }}</td>
                <td class="text-cell">{{ getCategoryName(live.categoryId) }}</td>
                <td>
                  <span v-if="live.status === 0" class="status-badge pending">未开始</span>
                  <span v-else-if="live.status === 1" class="status-badge live">直播中</span>
                  <span v-else class="status-badge ended">已结束</span>
                </td>
                <td class="text-cell">{{ live.viewCount || 0 }}</td>
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
                  <button 
                    class="delete-button" 
                    @click="handleDeleteLive(live.id)"
                  >
                    删除
                  </button>
                </td>
              </tr>
              <tr v-if="liveList.length === 0" class="empty-row">
                <td colspan="8" class="empty-cell">
                  <p>暂无直播数据</p>
                </td>
              </tr>
            </tbody>
          </table>
          <div class="pagination-container">
            <div class="pagination-info">
              共 {{ pagination.total }} 条记录
            </div>
            <div class="pagination-controls">
              <div class="page-size-selector">
                <span class="page-size-label">每页显示：</span>
                <select 
                  v-model="pagination.pageSize" 
                  @change="handlePageSizeChange"
                  class="page-size-select"
                >
                  <option value="5">5条</option>
                  <option value="10">10条</option>
                  <option value="20">20条</option>
                  <option value="50">50条</option>
                  <option value="100">100条</option>
                  <option value="200">200条</option>
                </select>
              </div>
              <a-pagination
                v-model:current="pagination.current"
                :pageSize="pagination.pageSize"
                :total="pagination.total"
                :show-size-changer="false"
                :show-quick-jumper="true"
                :simple="false"
                :show-less-items="false"
                @change="handlePageChange"
              />
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

.live-management-content {
  width: 100%;
  min-height: 100%;
  padding: 24px;
}

.content-header {
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  flex: 1;
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

.filter-bar {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 8px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.filter-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.filter-item label {
  color: rgba(255, 255, 255, 0.7);
  font-size: 14px;
  white-space: nowrap;
}

.filter-input {
  padding: 8px 12px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 6px;
  background: rgba(255, 255, 255, 0.05);
  color: #fff;
  font-size: 14px;
  outline: none;
  transition: all 0.3s ease;
  min-width: 180px;
}

.filter-input:focus {
  border-color: #ff4757;
  background: rgba(255, 71, 87, 0.1);
}

.filter-select {
  padding: 8px 12px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 6px;
  background: rgba(255, 255, 255, 0.05);
  color: #fff;
  font-size: 14px;
  outline: none;
  cursor: pointer;
  transition: all 0.3s ease;
  min-width: 120px;
}

.filter-select:focus {
  border-color: #ff4757;
  background: rgba(255, 71, 87, 0.1);
}

.filter-select option {
  background: #1a1a2e;
  color: #fff;
}

.search-button {
  padding: 8px 16px;
  border: none;
  border-radius: 6px;
  background: linear-gradient(135deg, #ff4757 0%, #ff6b81 100%);
  color: white;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.search-button:hover {
  background: linear-gradient(135deg, #ff6b81 0%, #ff4757 100%);
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(255, 71, 87, 0.3);
}

.clear-filter-button {
  padding: 8px 16px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 6px;
  background: rgba(255, 255, 255, 0.05);
  color: rgba(255, 255, 255, 0.8);
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.clear-filter-button:hover {
  background: rgba(255, 71, 87, 0.2);
  color: #ff6b81;
  border-color: #ff4757;
}

.batch-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.selection-info {
  color: rgba(255, 255, 255, 0.7);
  font-size: 14px;
}

.batch-button {
  padding: 8px 16px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.batch-button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.batch-button.end {
  background: linear-gradient(135deg, #ff4757 0%, #ff6b81 100%);
  color: white;
}

.batch-button.end:hover:not(:disabled) {
  background: linear-gradient(135deg, #ff6b81 0%, #ff4757 100%);
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(255, 71, 87, 0.3);
}

.batch-button.delete {
  background: rgba(255, 255, 255, 0.1);
  color: rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.batch-button.delete:hover:not(:disabled) {
  background: rgba(255, 71, 87, 0.2);
  color: #ff6b81;
  border-color: #ff4757;
  transform: translateY(-1px);
}

.table-container {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 12px;
  overflow: hidden;
}

.live-table {
  width: 100%;
  border-collapse: collapse;
}

.live-table th, .live-table td {
  padding: 16px 20px;
  text-align: left;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

.live-table th {
  background: rgba(255, 255, 255, 0.05);
  font-weight: 600;
  color: rgba(255, 255, 255, 0.9);
  font-size: 14px;
  white-space: nowrap;
}

.table-row {
  transition: all 0.3s ease;
}

.table-row:hover {
  background: rgba(255, 71, 87, 0.1);
}

.table-row.selected {
  background: rgba(255, 71, 87, 0.15);
}

.checkbox-cell {
  width: 50px;
  padding: 16px 12px;
}

.select-checkbox {
  width: 18px;
  height: 18px;
  cursor: pointer;
  accent-color: #ff4757;
}

.title-cell {
  font-weight: 500;
  color: #fff;
  max-width: 300px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.text-cell {
  color: rgba(255, 255, 255, 0.85);
}

.status-badge {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
  white-space: nowrap;
}

.status-badge.live {
  background: linear-gradient(135deg, rgba(255, 71, 87, 0.2) 0%, rgba(255, 107, 129, 0.2) 100%);
  color: #ff6b81;
}

.status-badge.pending {
  background: rgba(255, 193, 7, 0.2);
  color: #ffc107;
}

.status-badge.ended {
  background: rgba(255, 255, 255, 0.1);
  color: rgba(255, 255, 255, 0.8);
}

.start-button, .end-button {
  padding: 8px 16px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
  white-space: nowrap;
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
  margin-left: 8px;
  padding: 8px 16px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
  white-space: nowrap;
}

.delete-button:hover {
  background: rgba(255, 71, 87, 0.2);
  color: #ff6b81;
  border-color: #ff4757;
  transform: translateY(-1px);
}

.empty-row {
  height: 300px;
}

.empty-cell {
  text-align: center;
  color: rgba(255, 255, 255, 0.7);
  font-size: 14px;
}

.empty-cell p {
  margin: 0;
}

.pagination-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  background: rgba(255, 255, 255, 0.03);
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  margin-top: 16px;
  border-radius: 0 0 12px 12px;
}

.pagination-info {
  color: rgba(255, 255, 255, 0.7);
  font-size: 14px;
}

.pagination-controls {
  display: flex;
  align-items: center;
  gap: 16px;
}

.page-size-selector {
  display: flex;
  align-items: center;
  gap: 8px;
}

.page-size-label {
  color: rgba(255, 255, 255, 0.7);
  font-size: 14px;
}

.page-size-select {
  padding: 6px 12px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 4px;
  color: rgba(255, 255, 255, 0.9);
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
  outline: none;
}

.page-size-select:hover {
  border-color: rgba(255, 71, 87, 0.5);
  background: rgba(255, 255, 255, 0.08);
}

.page-size-select:focus {
  border-color: #ff4757;
  box-shadow: 0 0 0 3px rgba(255, 71, 87, 0.2);
}

.page-size-select option {
  background: #1a1a2e;
  color: #fff;
}

.pagination-container :deep(.ant-pagination) {
  display: flex;
  align-items: center;
  gap: 4px;
}

.pagination-container :deep(.ant-pagination-item) {
  background: rgba(255, 255, 255, 0.05);
  border-color: rgba(255, 255, 255, 0.2);
  border-radius: 4px;
  color: rgba(255, 255, 255, 0.85);
  transition: all 0.3s ease;
  min-width: 32px;
  height: 32px;
  line-height: 32px;
}

.pagination-container :deep(.ant-pagination-item:hover) {
  background: rgba(255, 71, 87, 0.2);
  border-color: #ff4757;
  color: #fff;
}

.pagination-container :deep(.ant-pagination-item-active) {
  background: #fff;
  border-color: #fff;
  color: #ff4757;
  font-weight: 600;
}

.pagination-container :deep(.ant-pagination-prev),
.pagination-container :deep(.ant-pagination-next) {
  background: linear-gradient(135deg, rgba(255, 71, 87, 0.15) 0%, rgba(255, 107, 129, 0.15) 100%);
  border-color: rgba(255, 71, 87, 0.3);
  border-radius: 6px;
  color: rgba(255, 255, 255, 0.9);
  transition: all 0.3s ease;
  min-width: 36px;
  height: 36px;
  line-height: 36px;
  font-weight: 500;
}

.pagination-container :deep(.ant-pagination-jump-prev),
.pagination-container :deep(.ant-pagination-jump-next) {
  background: rgba(255, 255, 255, 0.05);
  border-color: rgba(255, 255, 255, 0.2);
  border-radius: 4px;
  color: rgba(255, 255, 255, 0.85);
  transition: all 0.3s ease;
  min-width: 32px;
  height: 32px;
  line-height: 32px;
}

.pagination-container :deep(.ant-pagination-prev:hover),
.pagination-container :deep(.ant-pagination-next:hover) {
  background: linear-gradient(135deg, rgba(255, 71, 87, 0.3) 0%, rgba(255, 107, 129, 0.3) 100%);
  border-color: #ff4757;
  color: #fff;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(255, 71, 87, 0.3);
}

.pagination-container :deep(.ant-pagination-jump-prev:hover),
.pagination-container :deep(.ant-pagination-jump-next:hover) {
  background: rgba(255, 71, 87, 0.2);
  border-color: #ff4757;
  color: #fff;
}

.pagination-container :deep(.ant-pagination-disabled) {
  background: rgba(255, 255, 255, 0.02);
  border-color: rgba(255, 255, 255, 0.1);
  color: rgba(255, 255, 255, 0.3);
  cursor: not-allowed;
  opacity: 0.5;
}

.pagination-container :deep(.ant-pagination-disabled:hover) {
  background: rgba(255, 255, 255, 0.02);
  border-color: rgba(255, 255, 255, 0.1);
  color: rgba(255, 255, 255, 0.3);
  transform: none;
  box-shadow: none;
}

.pagination-container :deep(.ant-pagination-options) {
  display: none;
}

.pagination-container :deep(.ant-pagination-total-text) {
  display: none;
}

.loading {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 600px;
  font-size: 18px;
  color: rgba(255, 255, 255, 0.6);
}

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
