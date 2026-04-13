<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { message } from 'ant-design-vue';
import { userApi } from '../../api/user';
import { handleResponseAsync, errorHandler } from '../../utils/errorHandler';

const router = useRouter();
const loading = ref(false);
const userList = ref<any[]>([]);

const filterStatus = ref<number | null>(null);
const filterUserType = ref<number | null>(null);
const filterKeyword = ref('');

const pagination = ref({
  current: 1,
  pageSize: 5,
  total: 0
});

const getUserList = async () => {
  loading.value = true;
  try {
    const response = await fetch(`/api/user/admin/list?page=${pagination.value.current}&size=${pagination.value.pageSize}&status=${filterStatus.value ?? ''}&userType=${filterUserType.value ?? ''}&keyword=${filterKeyword.value}`, {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    });
    const result = await response.json();
    if (result.code === 200) {
      userList.value = result.data?.records || [];
      pagination.value.total = result.data?.total || 0;
    } else {
      message.error(result.message || '获取用户列表失败');
    }
  } catch (error) {
    errorHandler.handle(error);
  } finally {
    loading.value = false;
  }
};

const handlePageChange = (page: number) => {
  pagination.value.current = page;
  getUserList();
};

const handlePageSizeChange = () => {
  pagination.value.current = 1;
  getUserList();
};

const handleDelete = async (userId: number) => {
  try {
    const response = await userApi.deleteUser(userId);
    await handleResponseAsync(response, () => {
      message.success('删除用户成功');
      getUserList();
    });
  } catch (error) {
    errorHandler.handle(error);
  }
};

const handleToggleStatus = async (user: any) => {
  try {
    const newStatus = user.status === 1 ? 0 : 1;
    const response = await userApi.updateUserStatus(user.id, newStatus);
    await handleResponseAsync(response, () => {
      message.success('更新用户状态成功');
      getUserList();
    });
  } catch (error) {
    errorHandler.handle(error);
  }
};

const handleNavigate = (path: string) => {
  router.push(path);
};

onMounted(() => {
  getUserList();
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
        <li @click="handleNavigate('/admin/live')">
          <span class="sidebar-icon">🎥</span>
          <span class="sidebar-text">直播管理</span>
        </li>
        <li class="active" @click="handleNavigate('/admin/user')">
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
      <div v-else class="user-management-content">
        <div class="content-header">
          <h2>用户管理</h2>
          <p class="content-subtitle">管理平台所有用户账号</p>
        </div>
        <div class="filter-bar">
          <div class="filter-item">
            <label>搜索：</label>
            <input 
              type="text" 
              v-model="filterKeyword" 
              @keyup.enter="getUserList"
              placeholder="搜索用户名"
              class="filter-input"
            />
          </div>
          <div class="filter-item">
            <label>状态：</label>
            <select v-model="filterStatus" @change="getUserList" class="filter-select">
              <option :value="null">全部</option>
              <option :value="1">启用</option>
              <option :value="0">禁用</option>
            </select>
          </div>
          <div class="filter-item">
            <label>类型：</label>
            <select v-model="filterUserType" @change="getUserList" class="filter-select">
              <option :value="null">全部</option>
              <option :value="1">管理员</option>
              <option :value="0">普通用户</option>
            </select>
          </div>
          <button @click="getUserList" class="search-button">查询</button>
          <button @click="filterStatus = null; filterUserType = null; filterKeyword = ''; getUserList()" class="clear-filter-button">
            清除筛选
          </button>
        </div>
        <div class="table-container">
          <table class="user-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>用户名</th>
                <th>用户类型</th>
                <th>状态</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="user in userList" :key="user.id" class="table-row">
                <td>{{ user.id }}</td>
                <td class="username-cell">{{ user.username }}</td>
                <td>
                  <span v-if="user.userType === 1" class="type-badge admin">管理员</span>
                  <span v-else class="type-badge user">普通用户</span>
                </td>
                <td>
                  <span v-if="user.status === 1" class="status-badge active">启用</span>
                  <span v-else class="status-badge inactive">禁用</span>
                </td>
                <td>
                  <button 
                    class="toggle-button" 
                    @click="handleToggleStatus(user)"
                  >
                    {{ user.status === 1 ? '禁用' : '启用' }}
                  </button>
                  <button 
                    class="delete-button" 
                    @click="handleDelete(user.id)"
                  >
                    删除
                  </button>
                </td>
              </tr>
              <tr v-if="userList.length === 0" class="empty-row">
                <td colspan="5" class="empty-cell">
                  <p>暂无用户数据</p>
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

.user-management-content {
  width: 100%;
  min-height: 100%;
  padding: 24px;
}

.content-header {
  margin-bottom: 24px;
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

.table-container {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 12px;
  overflow: hidden;
}

.user-table {
  width: 100%;
  border-collapse: collapse;
}

.user-table th, .user-table td {
  padding: 16px 20px;
  text-align: left;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  color: rgba(255, 255, 255, 0.85);
}

.user-table td:first-child {
  color: #ff4757;
  font-weight: 600;
}

.user-table th {
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

.username-cell {
  font-weight: 500;
  color: #fff;
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.type-badge, .status-badge {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
  white-space: nowrap;
}

.type-badge.admin {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.2) 0%, rgba(118, 75, 162, 0.2) 100%);
  color: #a29bfe;
}

.type-badge.anchor {
  background: linear-gradient(135deg, rgba(0, 184, 148, 0.2) 0%, rgba(0, 206, 201, 0.2) 100%);
  color: #00cec9;
}

.type-badge.user {
  background: rgba(255, 255, 255, 0.1);
  color: rgba(255, 255, 255, 0.85);
}

.status-badge.active {
  background: linear-gradient(135deg, rgba(0, 184, 148, 0.2) 0%, rgba(0, 206, 201, 0.2) 100%);
  color: #00cec9;
}

.status-badge.inactive {
  background: linear-gradient(135deg, rgba(255, 71, 87, 0.2) 0%, rgba(255, 107, 129, 0.2) 100%);
  color: #ff6b81;
}

.toggle-button, .delete-button {
  padding: 8px 16px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
  margin-right: 10px;
}

.toggle-button {
  background: linear-gradient(135deg, #fdcb6e 0%, #e17055 100%);
  color: white;
}

.toggle-button:hover {
  background: linear-gradient(135deg, #e17055 0%, #fdcb6e 100%);
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(253, 203, 110, 0.3);
}

.delete-button {
  background: linear-gradient(135deg, #ff4757 0%, #ff6b81 100%);
  color: white;
}

.delete-button:hover {
  background: linear-gradient(135deg, #ff6b81 0%, #ff4757 100%);
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(255, 71, 87, 0.3);
}

.empty-row {
  height: 300px;
}

.empty-cell {
  text-align: center;
  color: rgba(255, 255, 255, 0.5);
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
  
  .user-table th, .user-table td {
    padding: 12px 16px;
    font-size: 14px;
  }
  
  .edit-button, .delete-button {
    padding: 6px 12px;
    font-size: 12px;
    margin-right: 8px;
  }
}
</style>
