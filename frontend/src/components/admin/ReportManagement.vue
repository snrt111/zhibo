<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { message } from 'ant-design-vue';
import { reportApi } from '../../api/report';
import { handleResponseAsync, errorHandler } from '../../utils/errorHandler';

const router = useRouter();

const loading = ref(false);
const filterStatus = ref<number | null>(null);
const filterTargetType = ref<number | null>(null);
const reportList = ref<any[]>([]);
const pagination = ref({
  current: 1,
  pageSize: 5,
  total: 0
});

const detailModalVisible = ref(false);
const currentReport = ref<any>(null);
const handleForm = ref({
  status: 1,
  handleResult: ''
});

const handleNavigate = (path: string) => {
  router.push(path);
};

const getTargetTypeText = (type: number) => {
  const texts: Record<number, string> = {
    1: '直播',
    2: '用户',
    3: '评论',
    4: '弹幕'
  };
  return texts[type] || '未知';
};

const getStatusText = (status: number) => {
  const texts: Record<number, string> = {
    0: '待处理',
    1: '已处理',
    2: '已关闭'
  };
  return texts[status] || '未知';
};

const getStatusClass = (status: number) => {
  const classes: Record<number, string> = {
    0: 'pending',
    1: 'processed',
    2: 'closed'
  };
  return classes[status] || 'default';
};

const formatDateTime = (dateStr: string | undefined) => {
  if (!dateStr) return '-';
  return dateStr.replace('T', ' ').substring(0, 19);
};

onMounted(() => {
  loadReportList();
});

const loadReportList = async () => {
  loading.value = true;
  try {
    const res = await reportApi.getReportList(pagination.value.current - 1, pagination.value.pageSize, filterStatus.value, filterTargetType.value);
    await handleResponseAsync(res, (data) => {
      reportList.value = data?.records || [];
      pagination.value.total = data?.total || 0;
    });
  } catch (error) {
    errorHandler.handle(error);
  } finally {
    loading.value = false;
  }
};

const handleDetail = (record: any) => {
  currentReport.value = record;
  handleForm.value = { status: 1, handleResult: '' };
  detailModalVisible.value = true;
};

const confirmHandle = async () => {
  if (!currentReport.value) return;

  try {
    const res = await reportApi.handleReport(currentReport.value.id, handleForm.value.status, handleForm.value.handleResult);
    await handleResponseAsync(res, () => {
      message.success('处理成功');
      detailModalVisible.value = false;
      loadReportList();
    });
  } catch (error) {
    errorHandler.handle(error);
  }
};

const handleTableChange = (pag: any) => {
  pagination.value.current = pag.current;
  loadReportList();
};

const handlePageSizeChange = () => {
  pagination.value.current = 1;
  loadReportList();
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
        <li @click="handleNavigate('/admin/audit')">
          <span class="sidebar-icon">🔍</span>
          <span class="sidebar-text">内容审核</span>
        </li>
        <li class="active" @click="handleNavigate('/admin/report')">
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
      <div v-else class="report-management-content">
        <div class="content-header">
          <h2>举报管理</h2>
          <p class="content-subtitle">管理和处理用户举报</p>
        </div>

        <div class="filter-bar">
          <div class="filter-item">
            <label>状态：</label>
            <select v-model="filterStatus" @change="loadReportList" class="filter-select">
              <option :value="null">全部</option>
              <option :value="0">待处理</option>
              <option :value="1">已处理</option>
              <option :value="2">已关闭</option>
            </select>
          </div>
          <div class="filter-item">
            <label>类型：</label>
            <select v-model="filterTargetType" @change="loadReportList" class="filter-select">
              <option :value="null">全部</option>
              <option :value="1">直播</option>
              <option :value="2">用户</option>
              <option :value="3">评论</option>
              <option :value="4">弹幕</option>
            </select>
          </div>
          <button @click="loadReportList" class="search-button">查询</button>
        </div>

        <div class="table-container">
          <table class="report-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>举报人ID</th>
                <th>举报类型</th>
                <th>目标ID</th>
                <th>举报原因</th>
                <th>状态</th>
                <th>举报时间</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="report in reportList" :key="report.id" class="table-row">
                <td>{{ report.id }}</td>
                <td>{{ report.reporterId }}</td>
                <td>
                  <span class="type-badge">{{ getTargetTypeText(report.targetType) }}</span>
                </td>
                <td>{{ report.targetId }}</td>
                <td class="reason-cell">{{ report.reason }}</td>
                <td>
                  <span :class="['status-badge', getStatusClass(report.status)]">
                    {{ getStatusText(report.status) }}
                  </span>
                </td>
                <td>{{ formatDateTime(report.createdAt) }}</td>
                <td>
                  <button v-if="report.status === 0" class="handle-button" @click="handleDetail(report)">处理</button>
                  <button v-else class="view-button" @click="handleDetail(report)">查看</button>
                </td>
              </tr>
              <tr v-if="reportList.length === 0" class="empty-row">
                <td colspan="8" class="empty-cell">
                  <p>暂无举报数据</p>
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
                @change="handleTableChange"
              />
            </div>
          </div>
        </div>
      </div>
    </div>

    <div v-if="detailModalVisible" class="modal-overlay" @click.self="detailModalVisible = false">
      <div class="modal-container">
        <div class="modal-header">
          <h3>{{ currentReport?.status === 0 ? '处理举报' : '举报详情' }}</h3>
          <button class="close-button" @click="detailModalVisible = false">×</button>
        </div>
        <div class="modal-body">
          <div v-if="currentReport" class="detail-content">
            <div class="detail-item">
              <span class="detail-label">举报类型：</span>
              <span class="detail-value">{{ getTargetTypeText(currentReport.targetType) }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">目标ID：</span>
              <span class="detail-value">{{ currentReport.targetId }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">举报原因：</span>
              <span class="detail-value">{{ currentReport.reason }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">详细描述：</span>
              <span class="detail-value">{{ currentReport.description || '-' }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">证据：</span>
              <span class="detail-value">{{ currentReport.evidence || '-' }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">举报时间：</span>
              <span class="detail-value">{{ formatDateTime(currentReport.createdAt) }}</span>
            </div>
            <div v-if="currentReport.status !== 0" class="detail-item">
              <span class="detail-label">处理结果：</span>
              <span class="detail-value">{{ currentReport.handleResult || '-' }}</span>
            </div>

            <div v-if="currentReport.status === 0" class="handle-form">
              <div class="form-item">
                <label class="form-label">处理结果：</label>
                <div class="radio-group">
                  <label class="radio-item">
                    <input type="radio" v-model="handleForm.status" :value="1" />
                    <span>通过（采取措施）</span>
                  </label>
                  <label class="radio-item">
                    <input type="radio" v-model="handleForm.status" :value="2" />
                    <span>驳回（不采取措施）</span>
                  </label>
                </div>
              </div>
              <div class="form-item">
                <label class="form-label">处理说明：</label>
                <textarea v-model="handleForm.handleResult" placeholder="请输入处理说明" class="form-textarea"></textarea>
              </div>
            </div>
          </div>
        </div>
        <div v-if="currentReport?.status === 0" class="modal-footer">
          <button class="cancel-button" @click="detailModalVisible = false">取消</button>
          <button class="confirm-button" @click="confirmHandle">确认处理</button>
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

.report-management-content {
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
  padding: 8px 20px;
  border: none;
  border-radius: 6px;
  background: linear-gradient(135deg, #ff4757 0%, #ff6b81 100%);
  color: #fff;
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

.table-container {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 12px;
  overflow: hidden;
}

.report-table {
  width: 100%;
  border-collapse: collapse;
}

.report-table th, .report-table td {
  padding: 16px 20px;
  text-align: left;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  color: rgba(255, 255, 255, 0.85);
}

.report-table td:first-child {
  color: #ff4757;
  font-weight: 600;
}

.report-table th {
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

.reason-cell {
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
  white-space: nowrap;
  background: rgba(255, 255, 255, 0.1);
  color: rgba(255, 255, 255, 0.85);
}

.status-badge {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
  white-space: nowrap;
}

.status-badge.pending {
  background: linear-gradient(135deg, rgba(253, 203, 110, 0.2) 0%, rgba(225, 112, 85, 0.2) 100%);
  color: #fdcb6e;
}

.status-badge.processed {
  background: linear-gradient(135deg, rgba(0, 184, 148, 0.2) 0%, rgba(0, 206, 201, 0.2) 100%);
  color: #00cec9;
}

.status-badge.closed {
  background: rgba(255, 255, 255, 0.1);
  color: rgba(255, 255, 255, 0.6);
}

.handle-button, .view-button {
  padding: 6px 16px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.handle-button {
  background: linear-gradient(135deg, #ff4757 0%, #ff6b81 100%);
  color: white;
}

.handle-button:hover {
  background: linear-gradient(135deg, #ff6b81 0%, #ff4757 100%);
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(255, 71, 87, 0.3);
}

.view-button {
  background: rgba(255, 255, 255, 0.1);
  color: rgba(255, 255, 255, 0.85);
}

.view-button:hover {
  background: rgba(255, 255, 255, 0.2);
}

.empty-row {
  height: 300px;
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

.empty-cell p {
  margin: 0;
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
  z-index: 1000;
}

.modal-container {
  background: linear-gradient(180deg, #1a1a2e 0%, #16213e 100%);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  width: 600px;
  max-height: 80vh;
  overflow: hidden;
  display: flex;
  flex-direction: column;
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
  color: #fff;
  font-weight: 600;
}

.close-button {
  background: none;
  border: none;
  color: rgba(255, 255, 255, 0.6);
  font-size: 24px;
  cursor: pointer;
  transition: all 0.3s ease;
  line-height: 1;
}

.close-button:hover {
  color: #ff4757;
}

.modal-body {
  padding: 24px;
  overflow-y: auto;
  flex: 1;
}

.detail-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.detail-item {
  display: flex;
  gap: 8px;
}

.detail-label {
  color: rgba(255, 255, 255, 0.6);
  font-size: 14px;
  min-width: 80px;
}

.detail-value {
  color: rgba(255, 255, 255, 0.9);
  font-size: 14px;
  flex: 1;
}

.handle-form {
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.form-item {
  margin-bottom: 16px;
}

.form-label {
  display: block;
  color: rgba(255, 255, 255, 0.8);
  font-size: 14px;
  margin-bottom: 8px;
}

.radio-group {
  display: flex;
  gap: 24px;
}

.radio-item {
  display: flex;
  align-items: center;
  gap: 8px;
  color: rgba(255, 255, 255, 0.85);
  font-size: 14px;
  cursor: pointer;
}

.radio-item input {
  cursor: pointer;
}

.form-textarea {
  width: 100%;
  padding: 12px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 6px;
  background: rgba(255, 255, 255, 0.05);
  color: #fff;
  font-size: 14px;
  outline: none;
  resize: vertical;
  min-height: 80px;
  transition: all 0.3s ease;
  box-sizing: border-box;
}

.form-textarea:focus {
  border-color: #ff4757;
  background: rgba(255, 71, 87, 0.1);
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 16px 24px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.cancel-button {
  padding: 8px 20px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 6px;
  background: rgba(255, 255, 255, 0.05);
  color: rgba(255, 255, 255, 0.8);
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.cancel-button:hover {
  background: rgba(255, 255, 255, 0.1);
}

.confirm-button {
  padding: 8px 20px;
  border: none;
  border-radius: 6px;
  background: linear-gradient(135deg, #ff4757 0%, #ff6b81 100%);
  color: #fff;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.confirm-button:hover {
  background: linear-gradient(135deg, #ff6b81 0%, #ff4757 100%);
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(255, 71, 87, 0.3);
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
  
  .report-table th, .report-table td {
    padding: 12px 16px;
    font-size: 14px;
  }
  
  .handle-button, .view-button {
    padding: 6px 12px;
    font-size: 12px;
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
