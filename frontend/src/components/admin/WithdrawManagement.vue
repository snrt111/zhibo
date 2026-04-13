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
        <li @click="handleNavigate('/admin/report')">
          <span class="sidebar-icon">⚠️</span>
          <span class="sidebar-text">举报管理</span>
        </li>
        <li class="active" @click="handleNavigate('/admin/withdraw')">
          <span class="sidebar-icon">💰</span>
          <span class="sidebar-text">提现审核</span>
        </li>
      </ul>
    </div>

    <div class="admin-content">
      <div v-if="loading" class="loading">加载中...</div>
      <div v-else class="withdraw-management-content">
        <div class="content-header">
          <h2>提现审核</h2>
          <p class="content-subtitle">管理和处理用户提现申请</p>
        </div>

        <div class="filter-bar">
          <div class="filter-item">
            <label>状态：</label>
            <select v-model="filterStatus" @change="loadWithdrawList" class="filter-select">
              <option :value="null">全部</option>
              <option :value="0">待审核</option>
              <option :value="1">审核通过</option>
              <option :value="2">审核拒绝</option>
              <option :value="3">已打款</option>
            </select>
          </div>
          <button @click="loadWithdrawList" class="search-button">查询</button>
        </div>

        <div class="table-container">
          <table class="withdraw-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>用户ID</th>
                <th>提现金额</th>
                <th>银行</th>
                <th>卡号</th>
                <th>开户人</th>
                <th>状态</th>
                <th>申请时间</th>
                <th>审核人</th>
                <th>审核时间</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="withdraw in withdrawList" :key="withdraw.id" class="table-row">
                <td>{{ withdraw.id }}</td>
                <td>{{ withdraw.userId }}</td>
                <td class="amount-cell">¥{{ withdraw.amount?.toFixed(2) }}</td>
                <td>{{ withdraw.bankName }}</td>
                <td>{{ withdraw.bankCard }}</td>
                <td>{{ withdraw.accountName }}</td>
                <td>
                  <span :class="['status-badge', getStatusClass(withdraw.status)]">
                    {{ getStatusText(withdraw.status) }}
                  </span>
                </td>
                <td>{{ formatDateTime(withdraw.createdAt) }}</td>
                <td>{{ withdraw.auditUserId || '-' }}</td>
                <td>{{ formatDateTime(withdraw.auditTime) }}</td>
                <td>
                  <div class="action-buttons">
                    <button v-if="withdraw.status === 0" class="handle-button" @click="handleAudit(withdraw, 1)">通过</button>
                    <button v-if="withdraw.status === 0" class="reject-button" @click="handleAudit(withdraw, 2)">拒绝</button>
                    <button v-else-if="withdraw.status === 1" class="pay-button" @click="handlePay(withdraw)">打款</button>
                    <button v-else class="view-button" @click="handleAudit(withdraw, 0)">查看</button>
                  </div>
                </td>
              </tr>
              <tr v-if="withdrawList.length === 0" class="empty-row">
                <td colspan="11" class="empty-cell">
                  <p>暂无提现申请</p>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

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

      <div v-if="auditModalVisible" class="modal-overlay" @click.self="auditModalVisible = false">
        <div class="modal-container">
          <div class="modal-header">
            <h3>{{ currentWithdraw?.status === 0 ? '处理提现申请' : '提现详情' }}</h3>
            <button class="close-button" @click="auditModalVisible = false">×</button>
          </div>
          <div class="modal-body">
            <div v-if="currentWithdraw" class="detail-content">
              <div class="detail-item">
                <span class="detail-label">用户ID：</span>
                <span class="detail-value">{{ currentWithdraw.userId }}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label">提现金额：</span>
                <span class="detail-value">¥{{ currentWithdraw.amount?.toFixed(2) }}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label">银行：</span>
                <span class="detail-value">{{ currentWithdraw.bankName }}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label">卡号：</span>
                <span class="detail-value">{{ currentWithdraw.bankCard }}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label">开户人：</span>
                <span class="detail-value">{{ currentWithdraw.accountName }}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label">状态：</span>
                <span class="detail-value">{{ getStatusText(currentWithdraw.status) }}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label">申请时间：</span>
                <span class="detail-value">{{ formatDateTime(currentWithdraw.createdAt) }}</span>
              </div>
              <div v-if="currentWithdraw.auditTime" class="detail-item">
                <span class="detail-label">审核时间：</span>
                <span class="detail-value">{{ formatDateTime(currentWithdraw.auditTime) }}</span>
              </div>
              <div v-if="currentWithdraw.remark" class="detail-item">
                <span class="detail-label">审核说明：</span>
                <span class="detail-value">{{ currentWithdraw.remark }}</span>
              </div>

              <div v-if="currentWithdraw.status === 0" class="handle-form">
                <div class="form-item">
                  <label class="form-label">审核结果：</label>
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
                <div class="form-item">
                  <label class="form-label">审核说明：</label>
                  <textarea v-model="auditForm.remark" placeholder="请输入审核说明" class="form-textarea"></textarea>
                </div>
              </div>
            </div>
          </div>
          <div v-if="currentWithdraw?.status === 0" class="modal-footer">
            <button class="cancel-button" @click="auditModalVisible = false">取消</button>
            <button class="confirm-button" @click="confirmAudit">确认处理</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { message } from 'ant-design-vue';
import { withdrawApi } from '../../api/withdraw';
import { handleResponseAsync, errorHandler } from '../../utils/errorHandler';

const router = useRouter();

const handleNavigate = (path: string) => {
  router.push(path);
};

const loading = ref(false);
const filterStatus = ref<number | null>(null);
const withdrawList = ref<any[]>([]);
const pagination = ref({
  current: 1,
  pageSize: 5,
  total: 0
});

const auditModalVisible = ref(false);
const currentWithdraw = ref<any>(null);
const auditForm = ref({
  status: 1,
  remark: ''
});

const getStatusClass = (status: number) => {
  const classes: Record<number, string> = {
    0: 'pending',
    1: 'approved',
    2: 'rejected',
    3: 'paid'
  };
  return classes[status] || 'default';
};

const getStatusText = (status: number) => {
  const texts: Record<number, string> = {
    0: '待审核',
    1: '审核通过',
    2: '审核拒绝',
    3: '已打款'
  };
  return texts[status] || '未知';
};

const formatDateTime = (dateTime: string | null | undefined) => {
  if (!dateTime) return '-';
  return dateTime.replace('T', ' ');
};

onMounted(() => {
  loadWithdrawList();
});

const loadWithdrawList = async () => {
  console.log('开始加载提现列表');
  loading.value = true;
  try {
    console.log('请求参数:', {
      page: pagination.value.current - 1,
      pageSize: pagination.value.pageSize,
      status: filterStatus.value
    });
    const res = await withdrawApi.getWithdrawList(pagination.value.current - 1, pagination.value.pageSize, filterStatus.value);
    console.log('API响应:', res);
    await handleResponseAsync(res, (data) => {
      console.log('处理后的数据:', data);
      withdrawList.value = data?.records || [];
      pagination.value.total = data?.total || 0;
      console.log('更新后的withdrawList:', withdrawList.value);
      console.log('更新后的pagination:', pagination.value);
    });
  } catch (error) {
    console.error('加载失败:', error);
    errorHandler.handle(error);
  } finally {
    loading.value = false;
    console.log('加载完成');
  }
};

const handleAudit = (record: any, status: number) => {
  currentWithdraw.value = record;
  auditForm.value = { status, remark: '' };
  auditModalVisible.value = true;
};

const confirmAudit = async () => {
  if (!currentWithdraw.value) return;

  try {
    const res = await withdrawApi.auditWithdraw(currentWithdraw.value.id, auditForm.value.status, auditForm.value.remark);
    await handleResponseAsync(res, () => {
      message.success('审核成功');
      auditModalVisible.value = false;
      loadWithdrawList();
    });
  } catch (error) {
    errorHandler.handle(error);
  }
};

const handlePay = (record: any) => {
  if (confirm(`确认给用户 ${record.userId} 打款 ¥${record.amount?.toFixed(2)} 吗？`)) {
    loadWithdrawList();
  }
};

const handleTableChange = (pag: any) => {
  pagination.value.current = pag.current;
  loadWithdrawList();
};

const handlePageSizeChange = () => {
  pagination.value.current = 1;
  loadWithdrawList();
};
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

.withdraw-management-content {
  width: 100%;
  min-height: 100%;
  padding: 24px;
}

.loading {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 400px;
  color: rgba(255, 255, 255, 0.7);
  font-size: 16px;
}

.content-header {
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid rgba(255, 71, 87, 0.3);
  background: linear-gradient(135deg, rgba(255, 71, 87, 0.1) 0%, rgba(255, 107, 129, 0.05) 100%);
  padding: 20px 24px;
  border-radius: 8px;
}

.content-header h2 {
  margin: 0 0 8px 0;
  font-size: 24px;
  font-weight: 600;
  color: #fff;
}

.content-subtitle {
  margin: 0;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.7);
}

.header-right {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  margin-top: 16px;
}

.search-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 71, 87, 0.3);
  border-radius: 8px;
  flex-wrap: wrap;
  background: linear-gradient(135deg, rgba(255, 71, 87, 0.1) 0%, rgba(255, 107, 129, 0.05) 100%);
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
  box-shadow: 0 0 0 2px rgba(255, 71, 87, 0.2);
}

.search-button {
  padding: 8px 16px;
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
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(255, 71, 87, 0.3);
}

.table-container {
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 8px;
  overflow: hidden;
  margin-bottom: 16px;
}

.withdraw-table {
  width: 100%;
  border-collapse: collapse;
  background: transparent;
}

.withdraw-table th {
  background: linear-gradient(90deg, rgba(255, 71, 87, 0.15) 0%, rgba(255, 107, 129, 0.1) 100%);
  color: rgba(255, 255, 255, 0.95);
  border-bottom: 1px solid rgba(255, 71, 87, 0.3);
  font-weight: 600;
  font-size: 14px;
  padding: 14px 16px;
  text-align: left;
  font-family: 'Arial', sans-serif;
}

.withdraw-table td {
  background: rgba(255, 255, 255, 0.02);
  color: rgba(255, 255, 255, 0.9);
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
  font-size: 14px;
  padding: 14px 16px;
  font-family: 'Arial', sans-serif;
}

.withdraw-table tr:hover td {
  background: rgba(255, 71, 87, 0.1);
}

.amount-cell {
  font-size: 16px;
  font-weight: 600;
  color: #ff6b81;
}

.status-badge {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
  text-align: center;
}

.status-badge.pending {
  background: rgba(255, 173, 20, 0.2);
  color: #ffad14;
}

.status-badge.approved {
  background: rgba(82, 196, 26, 0.2);
  color: #52c41a;
}

.status-badge.rejected {
  background: rgba(255, 77, 79, 0.2);
  color: #ff4d4f;
}

.status-badge.paid {
  background: rgba(24, 144, 255, 0.2);
  color: #1890ff;
}

.action-buttons {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.handle-button {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  border-radius: 6px;
  padding: 6px 16px;
  font-size: 13px;
  font-weight: 500;
  color: #fff;
  cursor: pointer;
  transition: all 0.3s ease;
}

.handle-button:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.reject-button {
  background: linear-gradient(135deg, #ff4757 0%, #ff6b81 100%);
  border: none;
  border-radius: 6px;
  padding: 6px 16px;
  font-size: 13px;
  font-weight: 500;
  color: #fff;
  cursor: pointer;
  transition: all 0.3s ease;
}

.reject-button:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(255, 71, 87, 0.3);
}

.pay-button {
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 6px;
  padding: 6px 16px;
  font-size: 13px;
  font-weight: 500;
  color: rgba(255, 255, 255, 0.9);
  cursor: pointer;
  transition: all 0.3s ease;
}

.pay-button:hover {
  background: rgba(255, 255, 255, 0.15);
  transform: translateY(-1px);
}

.view-button {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 6px;
  padding: 6px 16px;
  font-size: 13px;
  font-weight: 500;
  color: rgba(255, 255, 255, 0.7);
  cursor: pointer;
  transition: all 0.3s ease;
}

.view-button:hover {
  background: rgba(255, 255, 255, 0.1);
  transform: translateY(-1px);
}

.empty-row td {
  text-align: center;
  padding: 40px 20px;
  color: rgba(255, 255, 255, 0.7);
  font-size: 14px;
  background: rgba(255, 255, 255, 0.02);
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

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.7);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-container {
  background: linear-gradient(180deg, #1a1a2e 0%, #16213e 100%);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  box-shadow: 0 12px 48px rgba(0, 0, 0, 0.4);
  width: 90%;
  max-width: 600px;
  max-height: 80vh;
  overflow-y: auto;
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

.close-button {
  background: none;
  border: none;
  color: rgba(255, 255, 255, 0.7);
  font-size: 24px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.close-button:hover {
  color: #fff;
  transform: rotate(90deg);
}

.modal-body {
  padding: 24px;
  color: rgba(255, 255, 255, 0.9);
}

.detail-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.detail-item {
  display: flex;
  gap: 12px;
  align-items: flex-start;
}

.detail-label {
  width: 100px;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.7);
  font-size: 14px;
  white-space: nowrap;
}

.detail-value {
  flex: 1;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.9);
  word-break: break-word;
}

.handle-form {
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.form-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-label {
  font-weight: 600;
  color: rgba(255, 255, 255, 0.9);
  font-size: 14px;
}

.radio-group {
  display: flex;
  gap: 24px;
  flex-wrap: wrap;
}

.radio-item {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.9);
}

.radio-item input[type="radio"] {
  accent-color: #ff4757;
}

.form-textarea {
  padding: 12px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 6px;
  background: rgba(255, 255, 255, 0.05);
  color: #fff;
  font-size: 14px;
  resize: vertical;
  min-height: 100px;
  outline: none;
}

.form-textarea:focus {
  border-color: #ff4757;
  box-shadow: 0 0 0 2px rgba(255, 71, 87, 0.2);
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 20px 24px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.cancel-button {
  padding: 8px 16px;
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
  transform: translateY(-1px);
}

.confirm-button {
  padding: 8px 16px;
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
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(255, 71, 87, 0.3);
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
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
  
  .content-header h2 {
    font-size: 24px;
  }
  
  .filter-bar {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .pagination-container {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .action-buttons {
    flex-direction: column;
    gap: 8px;
  }
  
  .withdraw-table {
    font-size: 12px;
  }
  
  .withdraw-table th,
  .withdraw-table td {
    padding: 10px 8px;
  }
}
</style>