<template>
  <div class="withdraw-content">
    <div v-if="loading" class="loading">加载中...</div>
    <div v-else>
      <div class="content-header">
        <div class="header-left">
          <h2>提现管理</h2>
          <p class="content-subtitle">管理您的提现申请和记录</p>
        </div>
      </div>

      <div class="withdraw-tabs-container">
        <a-tabs v-model:activeKey="activeKey" class="withdraw-tabs">
          <a-tab-pane key="apply" tab="申请提现">
            <div class="balance-info">
              <div class="balance-card">
                <div class="balance-item">
                  <div class="balance-label">可用余额</div>
                  <div class="balance-value available">¥{{ balance.availableBalance?.toFixed(2) || '0.00' }}</div>
                </div>
                <div class="balance-item">
                  <div class="balance-label">冻结金额</div>
                  <div class="balance-value frozen">¥{{ balance.frozenBalance?.toFixed(2) || '0.00' }}</div>
                </div>
                <div class="balance-item">
                  <div class="balance-label">总收入</div>
                  <div class="balance-value income">¥{{ balance.totalIncome?.toFixed(2) || '0.00' }}</div>
                </div>
                <div class="balance-item">
                  <div class="balance-label">总提现</div>
                  <div class="balance-value withdraw">¥{{ balance.totalWithdraw?.toFixed(2) || '0.00' }}</div>
                </div>
              </div>
            </div>

            <div class="withdraw-form-container">
              <a-form :model="withdrawForm" layout="vertical" class="withdraw-form">
                <a-form-item label="提现金额" required>
                  <a-input-number
                    v-model:value="withdrawForm.amount"
                    :min="10"
                    :max="balance.availableBalance || 0"
                    :precision="2"
                    style="width: 100%"
                    placeholder="最低提现金额10元"
                    class="form-input"
                  />
                </a-form-item>
                <a-form-item label="开户人姓名" required>
                  <a-input v-model:value="withdrawForm.accountName" placeholder="请输入开户人姓名" class="form-input" />
                </a-form-item>
                <a-form-item label="银行名称" required>
                  <a-input v-model:value="withdrawForm.bankName" placeholder="请输入银行名称" class="form-input" />
                </a-form-item>
                <a-form-item label="银行卡号" required>
                  <a-input v-model:value="withdrawForm.bankCard" placeholder="请输入银行卡号" class="form-input" />
                </a-form-item>
                <a-form-item>
                  <a-button type="primary" @click="handleApply" :loading="loading" :disabled="!canApply" class="submit-button">
                    申请提现
                  </a-button>
                </a-form-item>
              </a-form>
            </div>
          </a-tab-pane>

          <a-tab-pane key="history" tab="提现记录">
            <div class="withdraw-history-container">
              <a-table
                :columns="withdrawColumns"
                :data-source="withdrawList"
                :loading="listLoading"
                :pagination="pagination"
                @change="handleTableChange"
                row-key="id"
                class="withdraw-table"
              >
                <template #bodyCell="{ column, record }">
                  <template v-if="column.key === 'status'">
                    <a-tag :color="getStatusColor(record.status)" class="status-tag">
                      {{ getStatusText(record.status) }}
                    </a-tag>
                  </template>
                  <template v-else-if="column.key === 'amount'">
                    <span class="amount-text">¥{{ record.amount?.toFixed(2) }}</span>
                  </template>
                </template>
              </a-table>
              <div v-if="withdrawList.length === 0 && !listLoading" class="empty-state">
                <p>暂无提现记录</p>
              </div>
            </div>
          </a-tab-pane>
        </a-tabs>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { message } from 'ant-design-vue';
import { withdrawApi } from '../../api/withdraw';
import { handleResponseAsync, errorHandler } from '../../utils/errorHandler';

const activeKey = ref('apply');
const loading = ref(false);
const listLoading = ref(false);

const balance = ref<any>({
  availableBalance: 0,
  frozenBalance: 0,
  totalIncome: 0,
  totalWithdraw: 0
});

const withdrawForm = ref({
  amount: null as number | null,
  accountName: '',
  bankName: '',
  bankCard: ''
});

const withdrawList = ref<any[]>([]);
const pagination = ref({
  current: 1,
  pageSize: 20,
  total: 0
});

const canApply = computed(() => {
  return withdrawForm.value.amount && withdrawForm.value.amount >= 10 &&
    withdrawForm.value.accountName && withdrawForm.value.bankName && withdrawForm.value.bankCard;
});

const withdrawColumns = [
  { title: '提现金额', dataIndex: 'amount', key: 'amount' },
  { title: '银行', dataIndex: 'bankName', key: 'bankName' },
  { title: '银行卡号', dataIndex: 'bankCard', key: 'bankCard' },
  { title: '状态', dataIndex: 'status', key: 'status' },
  { title: '申请时间', dataIndex: 'createdAt', key: 'createdAt' },
  { title: '审核时间', dataIndex: 'auditTime', key: 'auditTime' },
  { title: '审核备注', dataIndex: 'auditRemark', key: 'auditRemark' }
];

const getStatusColor = (status: number) => {
  const colors: Record<number, string> = {
    0: 'orange',
    1: 'green',
    2: 'red',
    3: 'blue'
  };
  return colors[status] || 'default';
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

onMounted(() => {
  loadBalance();
  loadWithdrawList();
});

const loadBalance = async () => {
  try {
    const res = await withdrawApi.getBalance();
    await handleResponseAsync(res, (data) => {
      balance.value = data || balance.value;
    });
  } catch (error) {
    errorHandler.handle(error);
  }
};

const loadWithdrawList = async () => {
  listLoading.value = true;
  try {
    const res = await withdrawApi.getMyWithdrawList(pagination.value.current - 1, pagination.value.pageSize);
    await handleResponseAsync(res, (data) => {
      withdrawList.value = data?.records || [];
      pagination.value.total = data?.total || 0;
    });
  } catch (error) {
    errorHandler.handle(error);
  } finally {
    listLoading.value = false;
  }
};

const handleApply = async () => {
  if (!withdrawForm.value.amount || !withdrawForm.value.accountName || !withdrawForm.value.bankName || !withdrawForm.value.bankCard) {
    message.error('请填写完整的提现信息');
    return;
  }

  loading.value = true;
  try {
    const res = await withdrawApi.applyWithdraw({
      amount: withdrawForm.value.amount,
      accountName: withdrawForm.value.accountName,
      bankName: withdrawForm.value.bankName,
      bankCard: withdrawForm.value.bankCard
    });
    await handleResponseAsync(res, () => {
      message.success('提现申请提交成功');
      withdrawForm.value = { amount: null, accountName: '', bankName: '', bankCard: '' };
      loadBalance();
      loadWithdrawList();
    });
  } catch (error) {
    errorHandler.handle(error);
  } finally {
    loading.value = false;
  }
};

const handleTableChange = (pag: any) => {
  pagination.value.current = pag.current;
  pagination.value.pageSize = pag.pageSize;
  loadWithdrawList();
};
</script>

<style scoped>
.withdraw-content {
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
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 32px;
  padding-bottom: 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.header-left h2 {
  margin: 0 0 8px 0;
  font-size: 28px;
  font-weight: 600;
  color: #fff;
}

.content-subtitle {
  margin: 0;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.6);
}

.withdraw-tabs-container {
  background: rgba(255, 255, 255, 0.03);
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.3);
  border: 1px solid rgba(255, 255, 255, 0.08);
}

.withdraw-tabs :deep(.ant-tabs-nav) {
  background: transparent !important;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1) !important;
  margin-bottom: 24px !important;
}

.withdraw-tabs :deep(.ant-tabs-tab) {
  color: rgba(255, 255, 255, 0.7) !important;
  font-weight: 500 !important;
}

.withdraw-tabs :deep(.ant-tabs-tab-active) {
  color: #ff4757 !important;
  font-weight: 600 !important;
}

.withdraw-tabs :deep(.ant-tabs-ink-bar) {
  background: linear-gradient(90deg, #ff4757 0%, #ff6b81 100%) !important;
}

.balance-info {
  margin-bottom: 32px;
}

.balance-card {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.balance-item {
  background: rgba(255, 255, 255, 0.05);
  border-radius: 12px;
  padding: 24px;
  border: 1px solid rgba(255, 255, 255, 0.08);
  transition: all 0.3s ease;
}

.balance-item:hover {
  background: rgba(255, 255, 255, 0.08);
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.3);
}

.balance-label {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.6);
  margin-bottom: 12px;
}

.balance-value {
  font-size: 28px;
  font-weight: 700;
  color: #fff;
}

.balance-value.available {
  color: #52c41a;
}

.balance-value.frozen {
  color: #faad14;
}

.balance-value.income {
  color: #1890ff;
}

.balance-value.withdraw {
  color: #ff6b81;
}

.withdraw-form-container {
  max-width: 600px;
  background: rgba(255, 255, 255, 0.03);
  border-radius: 12px;
  padding: 32px;
  border: 1px solid rgba(255, 255, 255, 0.08);
}

.withdraw-form :deep(.ant-form-item-label > label) {
  color: rgba(255, 255, 255, 0.9) !important;
  font-weight: 500 !important;
}

.form-input :deep(.ant-input),
.form-input :deep(.ant-input-number) {
  background: rgba(255, 255, 255, 0.05) !important;
  border: 1px solid rgba(255, 255, 255, 0.2) !important;
  color: #fff !important;
  border-radius: 8px !important;
  padding: 12px 16px !important;
  font-size: 14px !important;
}

.form-input :deep(.ant-input:focus),
.form-input :deep(.ant-input-number:focus) {
  background: rgba(255, 255, 255, 0.08) !important;
  border-color: #ff4757 !important;
  box-shadow: 0 0 0 2px rgba(255, 71, 87, 0.1) !important;
}

.form-input :deep(.ant-input::placeholder) {
  color: rgba(255, 255, 255, 0.4) !important;
}

.submit-button {
  width: 100%;
  height: 44px;
  font-size: 16px;
  font-weight: 600;
  background: linear-gradient(135deg, #ff4757 0%, #ff6b81 100%) !important;
  border: none !important;
  border-radius: 8px !important;
  margin-top: 16px;
}

.submit-button:hover:not(:disabled) {
  background: linear-gradient(135deg, #ff6b81 0%, #ff4757 100%) !important;
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(255, 71, 87, 0.3);
}

.withdraw-history-container {
  background: rgba(255, 255, 255, 0.03);
  border-radius: 12px;
  padding: 24px;
  border: 1px solid rgba(255, 255, 255, 0.08);
}

.withdraw-table {
  background: transparent !important;
}

.withdraw-table :deep(.ant-table-thead > tr > th) {
  background: rgba(255, 255, 255, 0.05) !important;
  color: rgba(255, 255, 255, 0.9) !important;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1) !important;
  font-weight: 600;
}

.withdraw-table :deep(.ant-table-tbody > tr > td) {
  background: transparent !important;
  color: rgba(255, 255, 255, 0.8) !important;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05) !important;
}

.withdraw-table :deep(.ant-table-tbody > tr:hover > td) {
  background: rgba(255, 255, 255, 0.02) !important;
}

.amount-text {
  font-size: 16px;
  font-weight: 600;
  color: #ff6b81;
}

.status-tag {
  font-size: 12px;
  font-weight: 500;
}

.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 300px;
  color: rgba(255, 255, 255, 0.7);
  font-size: 14px;
  background: rgba(255, 255, 255, 0.02);
  border-radius: 8px;
  margin-top: 16px;
}

@media (max-width: 768px) {
  .balance-card {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .content-header h2 {
    font-size: 24px;
  }
}
</style>