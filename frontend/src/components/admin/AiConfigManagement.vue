<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { message } from 'ant-design-vue';
import { aiConfigApi, type AiModelConfig } from '../../api/aiConfig';
import { handleResponseAsync, errorHandler } from '../../utils/errorHandler';

const router = useRouter();
const loading = ref(false);
const configList = ref<AiModelConfig[]>([]);
const filteredConfigList = ref<AiModelConfig[]>([]);
const showEditModal = ref(false);
const editingConfig = ref<AiModelConfig | null>(null);
const isNewConfig = ref(false);

const pagination = ref({
  current: 1,
  pageSize: 5,
  total: 0
});

const filterKeyword = ref('');
const filterType = ref<string | null>(null);
const filterStatus = ref<number | null>(null);

const applyFilters = () => {
  filteredConfigList.value = configList.value.filter(config => {
    if (filterKeyword.value) {
      const keyword = filterKeyword.value.toLowerCase();
      if (!config.name.toLowerCase().includes(keyword)) {
        return false;
      }
    }
    if (filterType.value && config.type !== filterType.value) {
      return false;
    }
    if (filterStatus.value !== null && config.status !== filterStatus.value) {
      return false;
    }
    return true;
  });
};

const clearFilters = () => {
  filterKeyword.value = '';
  filterType.value = null;
  filterStatus.value = null;
  pagination.value.current = 1;
  getConfigList();
};

const formData = ref({
  name: '',
  type: 'thirdparty' as 'ollama' | 'thirdparty' | 'image',
  baseUrl: '',
  apiKey: '',
  modelName: '',
  status: 1,
  priority: 0
});

const modelTypes = [
  { label: 'Ollama本地模型', value: 'ollama' },
  { label: '第三方API', value: 'thirdparty' },
  { label: '图片生成模型', value: 'image' }
];

const typeMap: Record<string, { color: string; text: string }> = {
  ollama: { color: '#52c41a', text: 'Ollama' },
  thirdparty: { color: '#1890ff', text: '第三方API' },
  image: { color: '#fa8c16', text: '图片生成' }
};

const getConfigList = async () => {
  loading.value = true;
  try {
    const response = await aiConfigApi.getList(pagination.value.current, pagination.value.pageSize);
    await handleResponseAsync(response, (data) => {
      configList.value = data?.records || [];
      pagination.value.total = data?.total || 0;
      applyFilters();
    });
  } catch (error) {
    errorHandler.handle(error);
  } finally {
    loading.value = false;
  }
};

const handlePageChange = (page: number) => {
  pagination.value.current = page;
  getConfigList();
};

const handlePageSizeChange = () => {
  pagination.value.current = 1;
  getConfigList();
};

const handleAdd = () => {
  isNewConfig.value = true;
  editingConfig.value = null;
  formData.value = {
    name: '',
    type: 'thirdparty',
    baseUrl: '',
    apiKey: '',
    modelName: '',
    status: 1,
    priority: 0
  };
  showEditModal.value = true;
};

const handleEdit = (config: AiModelConfig) => {
  isNewConfig.value = false;
  editingConfig.value = config;
  formData.value = {
    name: config.name || '',
    type: config.type as 'ollama' | 'thirdparty' | 'image',
    baseUrl: config.baseUrl || '',
    apiKey: config.apiKey || '',
    modelName: config.modelName || '',
    status: config.status || 1,
    priority: config.priority || 0
  };
  showEditModal.value = true;
};

const handleDelete = async (id: number) => {
  if (!confirm('确定要删除这个配置吗？')) {
    return;
  }
  try {
    const response = await aiConfigApi.delete(id);
    await handleResponseAsync(response, () => {
      message.success('删除成功');
      getConfigList();
    });
  } catch (error) {
    errorHandler.handle(error);
  }
};

const handleStatusChange = async (id: number, status: number) => {
  try {
    const response = await aiConfigApi.updateStatus(id, status);
    await handleResponseAsync(response, () => {
      message.success('状态更新成功');
      getConfigList();
    });
  } catch (error) {
    errorHandler.handle(error);
    getConfigList();
  }
};

const handleSave = async () => {
  if (!formData.value.name) {
    message.error('请输入模型名称');
    return;
  }
  if (!formData.value.baseUrl) {
    message.error('请输入API地址');
    return;
  }
  if (!formData.value.modelName) {
    message.error('请输入模型标识');
    return;
  }

  try {
    if (isNewConfig.value) {
      const response = await aiConfigApi.create(formData.value);
      await handleResponseAsync(response, () => {
        message.success('添加成功');
        showEditModal.value = false;
        getConfigList();
      });
    } else {
      const response = await aiConfigApi.update({
        ...formData.value,
        id: editingConfig.value!.id!
      });
      await handleResponseAsync(response, () => {
        message.success('更新成功');
        showEditModal.value = false;
        getConfigList();
      });
    }
  } catch (error) {
    errorHandler.handle(error);
  }
};

const handleNavigate = (path: string) => {
  router.push(path);
};

const handleCloseModal = () => {
  showEditModal.value = false;
  editingConfig.value = null;
};

onMounted(() => {
  getConfigList();
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
        <li @click="handleNavigate('/admin/user')">
          <span class="sidebar-icon">👥</span>
          <span class="sidebar-text">用户管理</span>
        </li>
        <li @click="handleNavigate('/admin/gift')">
          <span class="sidebar-icon">🎁</span>
          <span class="sidebar-text">礼物管理</span>
        </li>
        <li class="active" @click="handleNavigate('/admin/ai-config')">
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
      <div v-else class="ai-config-content">
        <div class="content-header">
          <div class="header-left">
            <h2>AI配置管理</h2>
            <p class="content-subtitle">管理AI模型配置</p>
          </div>
          <button class="add-button" @click="handleAdd">添加配置</button>
        </div>
        <div class="filter-bar">
          <div class="filter-item">
            <label>搜索：</label>
            <input 
              type="text" 
              v-model="filterKeyword" 
              @input="applyFilters" 
              placeholder="搜索模型名称"
              class="filter-input"
            />
          </div>
          <div class="filter-item">
            <label>类型：</label>
            <select v-model="filterType" @change="applyFilters" class="filter-select">
              <option :value="null">全部</option>
              <option value="ollama">Ollama</option>
              <option value="thirdparty">第三方API</option>
              <option value="image">图片生成</option>
            </select>
          </div>
          <div class="filter-item">
            <label>状态：</label>
            <select v-model="filterStatus" @change="applyFilters" class="filter-select">
              <option :value="null">全部</option>
              <option :value="1">启用</option>
              <option :value="0">禁用</option>
            </select>
          </div>
          <button @click="clearFilters" class="clear-filter-button">
            清除筛选
          </button>
        </div>
        <div class="config-list-container">
          <table class="config-table">
            <thead>
              <tr>
                <th class="th-index">序号</th>
                <th class="th-name">模型名称</th>
                <th class="th-type">类型</th>
                <th class="th-url">API地址</th>
                <th class="th-model">模型标识</th>
                <th class="th-priority">优先级</th>
                <th class="th-status">状态</th>
                <th class="th-actions">操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(config, index) in filteredConfigList" :key="config.id" class="config-row">
                <td class="td-index">{{ index + 1 }}</td>
                <td class="td-name">{{ config.name }}</td>
                <td class="td-type">
                  <span class="type-tag" :style="{ backgroundColor: typeMap[config.type]?.color || '#999' }">
                    {{ typeMap[config.type]?.text || config.type }}
                  </span>
                </td>
                <td class="td-url">{{ config.baseUrl }}</td>
                <td class="td-model">{{ config.modelName }}</td>
                <td class="td-priority">{{ config.priority }}</td>
                <td class="td-status">
                  <button
                    :class="['status-switch', { active: config.status === 1 }]"
                    @click="handleStatusChange(config.id!, config.status === 1 ? 0 : 1)"
                  >
                    {{ config.status === 1 ? '启用' : '禁用' }}
                  </button>
                </td>
                <td class="td-actions">
                  <div class="actions-wrapper">
                    <button class="edit-button-small" @click="handleEdit(config)">编辑</button>
                    <button class="delete-button-small" @click="handleDelete(config.id!)">删除</button>
                  </div>
                </td>
              </tr>
              <tr v-if="filteredConfigList.length === 0">
                <td colspan="8" class="empty-state-list">暂无配置数据</td>
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

    <!-- 编辑/添加弹窗 -->
    <div v-if="showEditModal" class="modal-overlay" @click="handleCloseModal">
      <div class="modal-content" @click.stop>
        <h3>{{ isNewConfig ? '添加配置' : '编辑配置' }}</h3>
        <div class="form-item">
          <label>模型名称</label>
          <input type="text" v-model="formData.name" placeholder="例如：Ollama本地模型" />
        </div>
        <div class="form-item">
          <label>模型类型</label>
          <select v-model="formData.type">
            <option v-for="item in modelTypes" :key="item.value" :value="item.value">
              {{ item.label }}
            </option>
          </select>
        </div>
        <div class="form-item">
          <label>API地址</label>
          <input type="text" v-model="formData.baseUrl" placeholder="例如：http://localhost:11434" />
        </div>
        <div class="form-item">
          <label>API密钥</label>
          <input type="password" v-model="formData.apiKey" placeholder="可选，根据API要求填写" />
        </div>
        <div class="form-item">
          <label>模型标识</label>
          <input type="text" v-model="formData.modelName" placeholder="例如：llava、dall-e-3" />
        </div>
        <div class="form-item">
          <label>优先级</label>
          <input type="number" v-model.number="formData.priority" min="0" />
        </div>
        <div class="form-item">
          <label>状态</label>
          <div class="status-toggle">
            <button
              :class="['toggle-btn', { active: formData.status === 1 }]"
              @click="formData.status = 1"
            >启用</button>
            <button
              :class="['toggle-btn', { active: formData.status === 0 }]"
              @click="formData.status = 0"
            >禁用</button>
          </div>
        </div>
        <div class="modal-actions">
          <button class="cancel-button" @click="handleCloseModal">取消</button>
          <button class="confirm-button" @click="handleSave">保存</button>
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

.ai-config-content {
  width: 100%;
  min-height: 100%;
  padding: 24px;
}

.content-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.header-left h2 {
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

.add-button {
  padding: 10px 24px;
  background: linear-gradient(135deg, #ff4757 0%, #ff6b81 100%);
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.add-button:hover {
  background: linear-gradient(135deg, #ff6b81 0%, #ff4757 100%);
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(255, 71, 87, 0.3);
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
  transition: all 0.3s ease;
  min-width: 120px;
  cursor: pointer;
}

.filter-select:focus {
  border-color: #ff4757;
  background: rgba(255, 71, 87, 0.1);
}

.filter-select option {
  background: #1a1a2e;
  color: #fff;
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

.config-list-container {
  background: rgba(255, 255, 255, 0.03);
  border-radius: 12px;
  overflow: hidden;
}

.config-table {
  width: 100%;
  border-collapse: collapse;
}

.config-table thead {
  background: rgba(255, 255, 255, 0.05);
}

.config-table th {
  padding: 16px 20px;
  text-align: left;
  font-size: 13px;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.8);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.config-table .th-index {
  width: 80px;
  text-align: center;
}

.config-table .th-name {
  width: 150px;
}

.config-table .th-type {
  width: 120px;
}

.config-table .th-url {
  min-width: 180px;
}

.config-table .th-model {
  width: 150px;
}

.config-table .th-priority {
  width: 80px;
  text-align: center;
}

.config-table .th-status {
  width: 100px;
  text-align: center;
}

.config-table .th-actions {
  width: 160px;
  text-align: center;
}

.config-row {
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
  transition: background 0.2s ease;
}

.config-row:hover {
  background: rgba(255, 255, 255, 0.03);
}

.config-table td {
  padding: 16px 20px;
  color: rgba(255, 255, 255, 0.9);
}

.td-index {
  text-align: center;
  color: rgba(255, 255, 255, 0.5);
  font-size: 13px;
}

.td-name {
  font-weight: 500;
}

.td-type {
  text-align: center;
}

.type-tag {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 4px;
  font-size: 12px;
  color: #fff;
  font-weight: 500;
}

.td-url {
  color: rgba(255, 255, 255, 0.7);
  font-size: 13px;
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.td-model {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.8);
}

.td-priority {
  text-align: center;
  color: #ff6b81;
  font-weight: 600;
}

.td-status {
  text-align: center;
}

.status-switch {
  padding: 4px 12px;
  border: none;
  border-radius: 4px;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  background: rgba(255, 255, 255, 0.1);
  color: rgba(255, 255, 255, 0.6);
}

.status-switch.active {
  background: linear-gradient(135deg, #52c41a 0%, #73d13d 100%);
  color: #fff;
}

.status-switch:not(.active):hover {
  background: rgba(255, 255, 255, 0.15);
  color: rgba(255, 255, 255, 0.8);
}

.td-actions {
  text-align: center;
}

.actions-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 10px;
}

.edit-button-small, .delete-button-small {
  padding: 6px 14px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
  font-weight: 500;
  transition: all 0.2s ease;
  flex-shrink: 0;
}

.edit-button-small {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.edit-button-small:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 6px rgba(102, 126, 234, 0.3);
}

.delete-button-small {
  background: linear-gradient(135deg, #ff4757 0%, #ff6b81 100%);
  color: white;
}

.delete-button-small:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 6px rgba(255, 71, 87, 0.3);
}

.empty-state-list {
  text-align: center;
  padding: 60px 20px;
  color: rgba(255, 255, 255, 0.5);
  font-size: 14px;
}

.loading {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 600px;
  font-size: 18px;
  color: rgba(255, 255, 255, 0.85);
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.7);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  padding: 32px;
  width: 500px;
  max-width: 90%;
  max-height: 80vh;
  overflow-y: auto;
  box-shadow: 0 12px 48px rgba(0, 0, 0, 0.4);
}

.modal-content h3 {
  margin: 0 0 24px 0;
  font-size: 20px;
  color: #fff;
  font-weight: 600;
}

.form-item {
  margin-bottom: 20px;
}

.form-item label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
  color: rgba(255, 255, 255, 0.9);
  font-size: 14px;
}

.form-item input, .form-item select {
  width: 100%;
  padding: 12px 16px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 6px;
  font-size: 14px;
  box-sizing: border-box;
  transition: all 0.3s ease;
  background: rgba(255, 255, 255, 0.05);
  color: #fff;
}

.form-item input:focus, .form-item select:focus {
  outline: none;
  border-color: #ff4757;
  box-shadow: 0 0 0 3px rgba(255, 71, 87, 0.2);
}

.form-item select {
  cursor: pointer;
}

.form-item select option {
  background: #1a1a2e;
  color: #fff;
}

.status-toggle {
  display: flex;
  gap: 10px;
}

.toggle-btn {
  flex: 1;
  padding: 10px 16px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 6px;
  background: rgba(255, 255, 255, 0.05);
  color: rgba(255, 255, 255, 0.6);
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 14px;
}

.toggle-btn:hover {
  border-color: rgba(255, 255, 255, 0.3);
  color: rgba(255, 255, 255, 0.8);
}

.toggle-btn.active {
  background: linear-gradient(135deg, #ff4757 0%, #ff6b81 100%);
  border-color: transparent;
  color: #fff;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 24px;
}

.cancel-button, .confirm-button {
  padding: 10px 24px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.cancel-button {
  background: rgba(255, 255, 255, 0.1);
  color: rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.cancel-button:hover {
  background: rgba(255, 255, 255, 0.15);
}

.confirm-button {
  background: linear-gradient(135deg, #ff4757 0%, #ff6b81 100%);
  color: white;
}

.confirm-button:hover {
  background: linear-gradient(135deg, #ff6b81 0%, #ff4757 100%);
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(255, 71, 87, 0.3);
}

@media (max-width: 768px) {
  .admin-sidebar {
    width: 200px;
  }

  .admin-content {
    margin-left: 200px;
    padding: 20px;
  }
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
</style>
