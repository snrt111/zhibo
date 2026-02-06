<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { message } from 'ant-design-vue';

const router = useRouter();
const loading = ref(false);
const userList = ref<any[]>([]);

const getUserList = async () => {
  loading.value = true;
  try {
    // 这里假设userApi有一个getUserList方法，实际需要根据后端API调整
    // const response = await userApi.getUserList();
    // if (response.code === 200) {
    //   userList.value = response.data || [];
    // } else {
    //   message.error(response.message || '获取用户列表失败');
    // }
    
    // 模拟数据
    userList.value = [
      { id: 1, username: 'admin', userType: 1, status: 1 },
      { id: 2, username: 'anchor1', userType: 2, status: 1 },
      { id: 3, username: 'user1', userType: 3, status: 1 }
    ];
  } catch (error) {
    console.error('获取用户列表错误:', error);
    message.error('获取用户列表失败，请检查网络连接');
  } finally {
    loading.value = false;
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
        <li @click="handleNavigate('/admin/live')">
          <span class="sidebar-icon">🎥</span>
          <span class="sidebar-text">直播管理</span>
        </li>
        <li class="active" @click="handleNavigate('/admin/user')">
          <span class="sidebar-icon">👥</span>
          <span class="sidebar-text">用户管理</span>
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
                  <span v-else-if="user.userType === 2" class="type-badge anchor">主播</span>
                  <span v-else class="type-badge user">普通用户</span>
                </td>
                <td>
                  <span v-if="user.status === 1" class="status-badge active">启用</span>
                  <span v-else class="status-badge inactive">禁用</span>
                </td>
                <td>
                  <button class="edit-button">编辑</button>
                  <button class="delete-button">删除</button>
                </td>
              </tr>
              <tr v-if="userList.length === 0" class="empty-row">
                <td colspan="5" class="empty-cell">
                  <p>暂无用户数据</p>
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

.user-management-content {
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

.user-table {
  width: 100%;
  border-collapse: collapse;
}

.user-table th, .user-table td {
  padding: 16px 20px;
  text-align: left;
  border-bottom: 1px solid #f0f0f0;
}

.user-table th {
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

.username-cell {
  font-weight: 500;
  color: #333;
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
  background-color: #e6f7ff;
  color: #1890ff;
}

.type-badge.anchor {
  background-color: #f6ffed;
  color: #52c41a;
}

.type-badge.user {
  background-color: #f5f5f5;
  color: #8c8c8c;
}

.status-badge.active {
  background-color: #f6ffed;
  color: #52c41a;
}

.status-badge.inactive {
  background-color: #fff1f0;
  color: #ff4d4f;
}

.edit-button, .delete-button {
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
  margin-right: 10px;
}

.edit-button {
  background-color: #1890ff;
  color: white;
}

.edit-button:hover {
  background-color: #40a9ff;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(24, 144, 255, 0.3);
}

.delete-button {
  background-color: #ff4d4f;
  color: white;
}

.delete-button:hover {
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