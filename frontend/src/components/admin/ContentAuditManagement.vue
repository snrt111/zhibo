<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { message, Modal } from 'ant-design-vue';
import { auditApi } from '../../api/audit';
import { handleResponseAsync, errorHandler } from '../../utils/errorHandler';

const router = useRouter();
const activeKey = ref('audit');

const auditLoading = ref(false);
const filterStatus = ref<number | undefined>(0);
const filterContentType = ref<number | undefined>();
const filterRiskLevel = ref<number | undefined>();
const auditList = ref<any[]>([]);
const auditPagination = ref({ current: 1, pageSize: 5, total: 0 });

const sensitiveLoading = ref(false);
const filterCategory = ref<number | undefined>();
const filterEnabled = ref<number | undefined>();
const sensitiveWordList = ref<any[]>([]);
const sensitivePagination = ref({ current: 1, pageSize: 5, total: 0 });

const auditModalVisible = ref(false);
const currentAudit = ref<any>(null);
const auditForm = ref({ status: 1, auditResult: '' });

const addModalVisible = ref(false);
const addForm = ref({ word: '', category: 0, level: 1 });

const editModalVisible = ref(false);
const editForm = ref({ id: 0, word: '', category: 0, level: 1 });

const auditColumns = [
  { title: 'ID', dataIndex: 'id', key: 'id', width: 80 },
  { title: '内容类型', dataIndex: 'contentType', key: 'contentType', width: 100 },
  { title: '内容ID', dataIndex: 'contentId', key: 'contentId', width: 100 },
  { title: '内容', dataIndex: 'content', key: 'content', ellipsis: true },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '风险等级', dataIndex: 'riskLevel', key: 'riskLevel', width: 100 },
  { title: '自动审核', dataIndex: 'autoAudit', key: 'autoAudit', width: 100 },
  { title: '创建时间', dataIndex: 'createdAt', key: 'createdAt' },
  { title: '操作', key: 'action', width: 150, fixed: 'right' as const }
];

const sensitiveColumns = [
  { title: 'ID', dataIndex: 'id', key: 'id', width: 80 },
  { title: '敏感词', dataIndex: 'word', key: 'word' },
  { title: '分类', dataIndex: 'category', key: 'category', width: 100 },
  { title: '风险等级', dataIndex: 'level', key: 'level', width: 100 },
  { title: '状态', dataIndex: 'enabled', key: 'enabled', width: 100 },
  { title: '创建时间', dataIndex: 'createdAt', key: 'createdAt' },
  { title: '操作', key: 'action', width: 100, fixed: 'right' as const }
];

const getContentTypeText = (type: number) => {
  const texts: Record<number, string> = { 1: '直播标题', 2: '用户信息', 3: '评论', 4: '弹幕' };
  return texts[type] || '未知';
};

const getStatusColor = (status: number) => {
  const colors: Record<number, string> = { 0: 'orange', 1: 'green', 2: 'red' };
  return colors[status] || 'default';
};

const getStatusText = (status: number) => {
  const texts: Record<number, string> = { 0: '待审核', 1: '通过', 2: '拒绝' };
  return texts[status] || '未知';
};

const getRiskColor = (level: number) => {
  const colors: Record<number, string> = { 0: 'green', 1: 'orange', 2: 'red' };
  return colors[level] || 'default';
};

const getRiskText = (level: number) => {
  const texts: Record<number, string> = { 0: '无风险', 1: '低风险', 2: '高风险' };
  return texts[level] || '未知';
};

const getCategoryText = (category: number) => {
  const texts: Record<number, string> = { 0: '其他', 1: '政治', 2: '色情', 3: '暴力', 4: '广告' };
  return texts[category] || '未知';
};

const formatDateTime = (dateStr: string | undefined) => {
  if (!dateStr) return '-';
  return dateStr.replace('T', ' ').substring(0, 19);
};

const getLevelText = (level: number) => {
  const texts: Record<number, string> = { 1: '低', 2: '中', 3: '高' };
  return texts[level] || '未知';
};

const handleNavigate = (path: string) => {
  router.push(path);
};

onMounted(() => {
  loadAuditList();
  loadSensitiveWordList();
});

const loadAuditList = async () => {
  auditLoading.value = true;
  try {
    const res = await auditApi.getAuditList(auditPagination.value.current - 1, auditPagination.value.pageSize, filterStatus.value, filterContentType.value, filterRiskLevel.value);
    await handleResponseAsync(res, (data) => {
      auditList.value = data?.records || [];
      auditPagination.value.total = data?.total || 0;
    });
  } catch (error) {
    errorHandler.handle(error);
  } finally {
    auditLoading.value = false;
  }
};

const loadSensitiveWordList = async () => {
  sensitiveLoading.value = true;
  try {
    const res = await auditApi.getSensitiveWordList(sensitivePagination.value.current - 1, sensitivePagination.value.pageSize, filterCategory.value, filterEnabled.value);
    await handleResponseAsync(res, (data) => {
      sensitiveWordList.value = data?.records || [];
      sensitivePagination.value.total = data?.total || 0;
    });
  } catch (error) {
    errorHandler.handle(error);
  } finally {
    sensitiveLoading.value = false;
  }
};

const handleAudit = (record: any, status: number) => {
  currentAudit.value = record;
  auditForm.value = { status, auditResult: '' };
  auditModalVisible.value = true;
};

const confirmAudit = async () => {
  if (!currentAudit.value) return;
  try {
    const res = await auditApi.manualAudit(currentAudit.value.id, auditForm.value.status, auditForm.value.auditResult);
    await handleResponseAsync(res, () => {
      message.success('审核成功');
      auditModalVisible.value = false;
      loadAuditList();
    });
  } catch (error) {
    errorHandler.handle(error);
  }
};

const showAddModal = () => {
  addForm.value = { word: '', category: 0, level: 1 };
  addModalVisible.value = true;
};

const addWord = async () => {
  if (!addForm.value.word) {
    message.error('请输入敏感词');
    return;
  }
  try {
    const res = await auditApi.addSensitiveWord(addForm.value.word, addForm.value.category, addForm.value.level);
    await handleResponseAsync(res, () => {
      message.success('添加成功');
      addModalVisible.value = false;
      loadSensitiveWordList();
    });
  } catch (error) {
    errorHandler.handle(error);
  }
};

const toggleWord = async (id: number, enabled: number) => {
  try {
    const res = await auditApi.toggleSensitiveWord(id, enabled);
    await handleResponseAsync(res, () => {
      message.success('操作成功');
      loadSensitiveWordList();
    });
  } catch (error) {
    errorHandler.handle(error);
  }
};

const deleteWord = (id: number) => {
  Modal.confirm({
    title: '确认删除',
    content: '确认删除该敏感词吗？',
    okText: '确认',
    cancelText: '取消',
    onOk: async () => {
      try {
        const res = await auditApi.deleteSensitiveWord(id);
        await handleResponseAsync(res, () => {
          message.success('删除成功');
          loadSensitiveWordList();
        });
      } catch (error) {
        errorHandler.handle(error);
      }
    }
  });
};

const reloadWords = async () => {
  try {
    const res = await auditApi.reloadSensitiveWords();
    await handleResponseAsync(res, () => {
      message.success('重新加载成功');
    });
  } catch (error) {
    errorHandler.handle(error);
  }
};

const showEditModal = (item: any) => {
  editForm.value = {
    id: item.id,
    word: item.word,
    category: item.category || 0,
    level: item.level || 1
  };
  editModalVisible.value = true;
};

const editWord = async () => {
  if (!editForm.value.word) {
    message.error('请输入敏感词');
    return;
  }
  try {
    const res = await auditApi.updateSensitiveWord(editForm.value.id, editForm.value.word, editForm.value.category, editForm.value.level);
    await handleResponseAsync(res, () => {
      message.success('更新成功');
      editModalVisible.value = false;
      loadSensitiveWordList();
    });
  } catch (error) {
    errorHandler.handle(error);
  }
};

const handleAuditTableChange = (pag: any) => {
  auditPagination.value.current = pag.current;
  loadAuditList();
};

const handleAuditPageSizeChange = () => {
  auditPagination.value.current = 1;
  loadAuditList();
};

const handleSensitiveTableChange = (pag: any) => {
  sensitivePagination.value.current = pag.current;
  loadSensitiveWordList();
};

const handleSensitivePageSizeChange = () => {
  sensitivePagination.value.current = 1;
  loadSensitiveWordList();
};
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
        <li @click="handleNavigate('/admin/ai-config')">
          <span class="sidebar-icon">🤖</span>
          <span class="sidebar-text">AI配置管理</span>
        </li>
        <li class="active" @click="handleNavigate('/admin/audit')">
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
      <div v-if="auditLoading && sensitiveLoading" class="loading">加载中...</div>
      <div v-else class="audit-management-content">
        <div class="content-header">
          <h2>内容审核管理</h2>
          <p class="content-subtitle">管理平台内容审核和敏感词</p>
        </div>
        <div class="tabs-container">
          <div class="tabs-header">
            <div 
              class="tab-item" 
              :class="{ active: activeKey === 'audit' }"
              @click="activeKey = 'audit'"
            >
              内容审核
            </div>
            <div 
              class="tab-item" 
              :class="{ active: activeKey === 'sensitive' }"
              @click="activeKey = 'sensitive'"
            >
              敏感词管理
            </div>
          </div>
          <div class="tabs-content">
            <div v-if="activeKey === 'audit'" class="tab-panel">
              <div class="filter-bar">
                <div class="filter-item">
                  <label>状态：</label>
                  <select v-model="filterStatus" class="filter-select">
                    <option :value="undefined">全部</option>
                    <option :value="0">待审核</option>
                    <option :value="1">通过</option>
                    <option :value="2">拒绝</option>
                  </select>
                </div>
                <div class="filter-item">
                  <label>类型：</label>
                  <select v-model="filterContentType" class="filter-select">
                    <option :value="undefined">全部</option>
                    <option :value="1">直播标题</option>
                    <option :value="2">用户信息</option>
                    <option :value="3">评论</option>
                    <option :value="4">弹幕</option>
                  </select>
                </div>
                <div class="filter-item">
                  <label>风险等级：</label>
                  <select v-model="filterRiskLevel" class="filter-select">
                    <option :value="undefined">全部</option>
                    <option :value="0">无风险</option>
                    <option :value="1">低风险</option>
                    <option :value="2">高风险</option>
                  </select>
                </div>
                <button @click="loadAuditList" class="search-button">
                  查询
                </button>
              </div>
              <div class="table-container">
                <table class="audit-table">
                  <thead>
                    <tr>
                      <th>ID</th>
                      <th>内容类型</th>
                      <th>内容ID</th>
                      <th>内容</th>
                      <th>状态</th>
                      <th>风险等级</th>
                      <th>自动审核</th>
                      <th>创建时间</th>
                      <th>操作</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="item in auditList" :key="item.id" class="table-row">
                      <td>{{ item.id }}</td>
                      <td>
                        <span class="type-badge">{{ getContentTypeText(item.contentType) }}</span>
                      </td>
                      <td>{{ item.contentId }}</td>
                      <td class="content-cell">{{ item.content }}</td>
                      <td>
                        <span :class="['status-badge', getStatusText(item.status)]">
                          {{ getStatusText(item.status) }}
                        </span>
                      </td>
                      <td>
                        <span :class="['risk-badge', getRiskText(item.riskLevel)]">
                          {{ getRiskText(item.riskLevel) }}
                        </span>
                      </td>
                      <td>{{ item.autoAudit === 1 ? '是' : '否' }}</td>
                      <td>{{ formatDateTime(item.createdAt) }}</td>
                      <td>
                        <template v-if="item.status === 0">
                          <button class="pass-button" @click="handleAudit(item, 1)">通过</button>
                          <button class="reject-button" @click="handleAudit(item, 2)">拒绝</button>
                        </template>
                        <span v-else>-</span>
                      </td>
                    </tr>
                    <tr v-if="auditList.length === 0" class="empty-row">
                      <td colspan="9" class="empty-cell">暂无数据</td>
                    </tr>
                  </tbody>
                </table>
                <div class="pagination-container">
                  <div class="pagination-info">
                    共 {{ auditPagination.total }} 条记录
                  </div>
                  <div class="pagination-controls">
                    <div class="page-size-selector">
                      <span class="page-size-label">每页显示：</span>
                      <select 
                        v-model="auditPagination.pageSize" 
                        @change="handleAuditPageSizeChange"
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
                      v-model:current="auditPagination.current"
                      :pageSize="auditPagination.pageSize"
                      :total="auditPagination.total"
                      :show-size-changer="false"
                      :show-quick-jumper="true"
                      :simple="false"
                      :show-less-items="false"
                      @change="handleAuditTableChange"
                    />
                  </div>
                </div>
              </div>
            </div>
            <div v-if="activeKey === 'sensitive'" class="tab-panel">
              <div class="filter-bar">
                <div class="filter-item">
                  <label>分类：</label>
                  <select v-model="filterCategory" class="filter-select">
                    <option :value="undefined">全部</option>
                    <option :value="0">其他</option>
                    <option :value="1">政治</option>
                    <option :value="2">色情</option>
                    <option :value="3">暴力</option>
                    <option :value="4">广告</option>
                  </select>
                </div>
                <div class="filter-item">
                  <label>状态：</label>
                  <select v-model="filterEnabled" class="filter-select">
                    <option :value="undefined">全部</option>
                    <option :value="1">启用</option>
                    <option :value="0">禁用</option>
                  </select>
                </div>
                <button @click="loadSensitiveWordList" class="search-button">查询</button>
                <button @click="showAddModal" class="add-button">添加</button>
                <button @click="reloadWords" class="reload-button">重新加载</button>
              </div>
              <div class="table-container">
                <table class="audit-table">
                  <thead>
                    <tr>
                      <th>ID</th>
                      <th>敏感词</th>
                      <th>分类</th>
                      <th>风险等级</th>
                      <th>状态</th>
                      <th>创建时间</th>
                      <th>操作</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="item in sensitiveWordList" :key="item.id" class="table-row">
                      <td>{{ item.id }}</td>
                      <td>{{ item.word }}</td>
                      <td>{{ getCategoryText(item.category) }}</td>
                      <td>{{ getLevelText(item.level) }}</td>
                      <td>
                        <label class="switch-label">
                          <input 
                            type="checkbox" 
                            :checked="item.enabled === 1"
                            @change="toggleWord(item.id, ($event.target as HTMLInputElement).checked ? 1 : 0)"
                            class="switch-input"
                          />
                          <span class="switch-slider"></span>
                        </label>
                      </td>
                      <td>{{ formatDateTime(item.createdAt) }}</td>
                      <td>
                        <button class="edit-button" @click="showEditModal(item)">编辑</button>
                        <button class="delete-button" @click="deleteWord(item.id)">删除</button>
                      </td>
                    </tr>
                    <tr v-if="sensitiveWordList.length === 0" class="empty-row">
                      <td colspan="7" class="empty-cell">暂无数据</td>
                    </tr>
                  </tbody>
                </table>
                <div class="pagination-container">
                  <div class="pagination-info">
                    共 {{ sensitivePagination.total }} 条记录
                  </div>
                  <div class="pagination-controls">
                    <div class="page-size-selector">
                      <span class="page-size-label">每页显示：</span>
                      <select 
                        v-model="sensitivePagination.pageSize" 
                        @change="handleSensitivePageSizeChange"
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
                      v-model:current="sensitivePagination.current"
                      :pageSize="sensitivePagination.pageSize"
                      :total="sensitivePagination.total"
                      :show-size-changer="false"
                      :show-quick-jumper="true"
                      :simple="false"
                      :show-less-items="false"
                      @change="handleSensitiveTableChange"
                    />
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="modal-overlay" v-if="auditModalVisible" @click.self="auditModalVisible = false">
      <div class="modal-content">
        <div class="modal-header">
          <h3>审核内容</h3>
          <button class="modal-close" @click="auditModalVisible = false">×</button>
        </div>
        <div class="modal-body">
          <div class="info-list">
            <div class="info-item">
              <span class="info-label">内容类型：</span>
              <span class="info-value">{{ getContentTypeText(currentAudit?.contentType) }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">内容：</span>
              <span class="info-value">{{ currentAudit?.content }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">自动审核结果：</span>
              <span class="info-value">{{ currentAudit?.auditResult || '-' }}</span>
            </div>
          </div>
          <div class="form-group">
            <label>审核结果 <span class="required">*</span></label>
            <div class="radio-group">
              <label class="radio-item">
                <input type="radio" v-model="auditForm.status" :value="1" />
                <span>通过</span>
              </label>
              <label class="radio-item">
                <input type="radio" v-model="auditForm.status" :value="2" />
                <span>拒绝</span>
              </label>
            </div>
          </div>
          <div class="form-group">
            <label>审核说明</label>
            <textarea v-model="auditForm.auditResult" placeholder="请输入审核说明" rows="3"></textarea>
          </div>
        </div>
        <div class="modal-footer">
          <button class="cancel-button" @click="auditModalVisible = false">取消</button>
          <button class="confirm-button" @click="confirmAudit">确认</button>
        </div>
      </div>
    </div>

    <div class="modal-overlay" v-if="addModalVisible" @click.self="addModalVisible = false">
      <div class="modal-content">
        <div class="modal-header">
          <h3>添加敏感词</h3>
          <button class="modal-close" @click="addModalVisible = false">×</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>敏感词 <span class="required">*</span></label>
            <input type="text" v-model="addForm.word" placeholder="请输入敏感词" />
          </div>
          <div class="form-group">
            <label>分类</label>
            <select v-model="addForm.category">
              <option :value="0">其他</option>
              <option :value="1">政治</option>
              <option :value="2">色情</option>
              <option :value="3">暴力</option>
              <option :value="4">广告</option>
            </select>
          </div>
          <div class="form-group">
            <label>风险等级</label>
            <select v-model="addForm.level">
              <option :value="1">低</option>
              <option :value="2">中</option>
              <option :value="3">高</option>
            </select>
          </div>
        </div>
        <div class="modal-footer">
          <button class="cancel-button" @click="addModalVisible = false">取消</button>
          <button class="confirm-button" @click="addWord">确认</button>
        </div>
      </div>
    </div>

    <div class="modal-overlay" v-if="editModalVisible" @click.self="editModalVisible = false">
      <div class="modal-content">
        <div class="modal-header">
          <h3>编辑敏感词</h3>
          <button class="modal-close" @click="editModalVisible = false">×</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>敏感词 <span class="required">*</span></label>
            <input type="text" v-model="editForm.word" placeholder="请输入敏感词" />
          </div>
          <div class="form-group">
            <label>分类</label>
            <select v-model="editForm.category">
              <option :value="0">其他</option>
              <option :value="1">政治</option>
              <option :value="2">色情</option>
              <option :value="3">暴力</option>
              <option :value="4">广告</option>
            </select>
          </div>
          <div class="form-group">
            <label>风险等级</label>
            <select v-model="editForm.level">
              <option :value="1">低</option>
              <option :value="2">中</option>
              <option :value="3">高</option>
            </select>
          </div>
        </div>
        <div class="modal-footer">
          <button class="cancel-button" @click="editModalVisible = false">取消</button>
          <button class="confirm-button" @click="editWord">确认</button>
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

.audit-management-content {
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

.tabs-container {
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 12px;
  overflow: hidden;
}

.tabs-header {
  display: flex;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  background: rgba(255, 255, 255, 0.02);
}

.tab-item {
  padding: 16px 32px;
  cursor: pointer;
  color: rgba(255, 255, 255, 0.6);
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
  border-bottom: 2px solid transparent;
}

.tab-item:hover {
  color: rgba(255, 255, 255, 0.85);
}

.tab-item.active {
  color: #ff4757;
  border-bottom-color: #ff4757;
}

.tabs-content {
  padding: 24px;
}

.tab-panel {
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
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

.search-button, .add-button, .reload-button {
  padding: 8px 16px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.search-button {
  background: linear-gradient(135deg, #ff4757 0%, #ff6b81 100%);
  color: white;
}

.search-button:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(255, 71, 87, 0.3);
}

.add-button {
  background: linear-gradient(135deg, #00cec9 0%, #00b894 100%);
  color: white;
}

.add-button:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(0, 206, 201, 0.3);
}

.reload-button {
  background: rgba(255, 255, 255, 0.1);
  color: rgba(255, 255, 255, 0.85);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.reload-button:hover {
  background: rgba(255, 255, 255, 0.15);
}

.table-container {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 12px;
  overflow: hidden;
}

.audit-table {
  width: 100%;
  border-collapse: collapse;
}

.audit-table th, .audit-table td {
  padding: 16px 20px;
  text-align: left;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  color: rgba(255, 255, 255, 0.85);
}

.audit-table th {
  background: rgba(255, 255, 255, 0.05);
  font-weight: 600;
  color: rgba(255, 255, 255, 0.9);
  font-size: 14px;
  white-space: nowrap;
}

.audit-table td:first-child {
  color: #ff4757;
  font-weight: 600;
}

.table-row {
  transition: all 0.3s ease;
}

.table-row:hover {
  background: rgba(255, 71, 87, 0.1);
}

.content-cell {
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.type-badge {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
  background: rgba(102, 126, 234, 0.2);
  color: #a29bfe;
}

.status-badge {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.status-badge.待审核 {
  background: rgba(253, 203, 110, 0.2);
  color: #fdcb6e;
}

.status-badge.通过 {
  background: rgba(0, 206, 201, 0.2);
  color: #00cec9;
}

.status-badge.拒绝 {
  background: rgba(255, 71, 87, 0.2);
  color: #ff4757;
}

.risk-badge {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.risk-badge.无风险 {
  background: rgba(0, 206, 201, 0.2);
  color: #00cec9;
}

.risk-badge.低风险 {
  background: rgba(253, 203, 110, 0.2);
  color: #fdcb6e;
}

.risk-badge.高风险 {
  background: rgba(255, 71, 87, 0.2);
  color: #ff4757;
}

.pass-button, .reject-button, .delete-button {
  padding: 6px 12px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 13px;
  font-weight: 500;
  transition: all 0.3s ease;
  margin-right: 8px;
}

.pass-button {
  background: linear-gradient(135deg, #00cec9 0%, #00b894 100%);
  color: white;
}

.pass-button:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(0, 206, 201, 0.3);
}

.reject-button, .delete-button {
  background: linear-gradient(135deg, #ff4757 0%, #ff6b81 100%);
  color: white;
}

.reject-button:hover, .delete-button:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(255, 71, 87, 0.3);
}

.edit-button {
  padding: 6px 12px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 13px;
  font-weight: 500;
  transition: all 0.3s ease;
  background: linear-gradient(135deg, #3498db 0%, #2980b9 100%);
  color: white;
  margin-right: 8px;
}

.edit-button:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(52, 152, 219, 0.3);
}

.switch-label {
  position: relative;
  display: inline-block;
  width: 44px;
  height: 22px;
}

.switch-input {
  opacity: 0;
  width: 0;
  height: 0;
}

.switch-slider {
  position: absolute;
  cursor: pointer;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(255, 255, 255, 0.2);
  transition: 0.3s;
  border-radius: 22px;
}

.switch-slider:before {
  position: absolute;
  content: "";
  height: 18px;
  width: 18px;
  left: 2px;
  bottom: 2px;
  background-color: white;
  transition: 0.3s;
  border-radius: 50%;
}

.switch-input:checked + .switch-slider {
  background-color: #00cec9;
}

.switch-input:checked + .switch-slider:before {
  transform: translateX(22px);
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
  padding: 16px;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
}

.pagination button {
  padding: 8px 16px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 6px;
  background: rgba(255, 255, 255, 0.05);
  color: rgba(255, 255, 255, 0.85);
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.pagination button:hover:not(:disabled) {
  border-color: #ff4757;
  color: #ff4757;
}

.pagination button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.pagination span {
  color: rgba(255, 255, 255, 0.7);
  font-size: 14px;
}

.empty-row {
  height: 200px;
}

.empty-row td {
  vertical-align: middle;
  text-align: center;
}

.empty-cell {
  text-align: center;
  color: rgba(255, 255, 255, 0.5);
  font-size: 14px;
}

.loading {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 600px;
  font-size: 18px;
  color: rgba(255, 255, 255, 0.6);
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 2000;
}

.modal-content {
  background: linear-gradient(180deg, #1a1a2e 0%, #16213e 100%);
  border-radius: 12px;
  width: 500px;
  max-width: 90vw;
  max-height: 80vh;
  overflow: hidden;
  animation: modalFadeIn 0.3s ease;
}

@keyframes modalFadeIn {
  from { opacity: 0; transform: scale(0.9); }
  to { opacity: 1; transform: scale(1); }
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.modal-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #fff;
}

.modal-close {
  width: 32px;
  height: 32px;
  border: none;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  color: #fff;
  font-size: 20px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.modal-close:hover {
  background: rgba(255, 255, 255, 0.2);
}

.modal-body {
  padding: 24px;
  max-height: 50vh;
  overflow-y: auto;
}

.info-list {
  margin-bottom: 20px;
}

.info-item {
  display: flex;
  padding: 12px 0;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

.info-label {
  color: rgba(255, 255, 255, 0.6);
  font-size: 14px;
  min-width: 100px;
}

.info-value {
  color: #fff;
  font-size: 14px;
  flex: 1;
  word-break: break-word;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  color: rgba(255, 255, 255, 0.85);
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 8px;
}

.form-group .required {
  color: #ff4757;
}

.form-group input,
.form-group select,
.form-group textarea {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 6px;
  background: rgba(255, 255, 255, 0.05);
  color: #fff;
  font-size: 14px;
  outline: none;
  transition: all 0.3s ease;
  box-sizing: border-box;
}

.form-group input:focus,
.form-group select:focus,
.form-group textarea:focus {
  border-color: #ff4757;
  background: rgba(255, 71, 87, 0.1);
}

.form-group textarea {
  resize: vertical;
  min-height: 80px;
}

.form-group select option {
  background: #1a1a2e;
  color: #fff;
}

.radio-group {
  display: flex;
  gap: 24px;
}

.radio-item {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  color: rgba(255, 255, 255, 0.85);
  font-size: 14px;
}

.radio-item input {
  accent-color: #ff4757;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 20px 24px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  background: rgba(0, 0, 0, 0.2);
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
  color: rgba(255, 255, 255, 0.85);
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
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(255, 71, 87, 0.3);
}

@media (max-width: 1200px) {
  .admin-content {
    margin-left: 240px;
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
