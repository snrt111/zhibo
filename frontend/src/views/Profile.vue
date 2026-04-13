<template>
  <div class="profile-container">
    <a-card title="个人中心" :bordered="false" class="profile-card">
      <a-tabs v-model:activeKey="activeKey">
        <a-tab-pane key="info" tab="基本信息">
          <a-form :model="profileForm" layout="vertical">
            <a-form-item label="头像">
              <a-upload
                name="file"
                :show-upload-list="false"
                :before-upload="beforeUpload"
                :custom-request="customRequest"
                @change="handleAvatarChange"
                class="avatar-uploader"
              >
                <img v-if="profileForm.avatar" :src="profileForm.avatar" class="avatar" />
                <div v-else class="avatar-placeholder">
                  <PlusOutlined />
                  <div style="margin-top: 8px">上传头像</div>
                </div>
              </a-upload>
            </a-form-item>
            <a-form-item label="用户名">
              <a-input v-model:value="profileForm.username" disabled />
            </a-form-item>
            <a-form-item label="昵称">
              <a-input v-model:value="profileForm.nickname" placeholder="请输入昵称" />
            </a-form-item>
            <a-form-item label="邮箱">
              <a-input v-model:value="profileForm.email" placeholder="请输入邮箱" />
            </a-form-item>
            <a-form-item label="手机号">
              <a-input v-model:value="profileForm.phone" placeholder="请输入手机号" />
            </a-form-item>
            <a-form-item label="性别">
              <a-radio-group v-model:value="profileForm.gender">
                <a-radio :value="0">未知</a-radio>
                <a-radio :value="1">男</a-radio>
                <a-radio :value="2">女</a-radio>
              </a-radio-group>
            </a-form-item>
            <a-form-item>
              <a-button type="primary" @click="handleSaveProfile">保存信息</a-button>
            </a-form-item>
          </a-form>
        </a-tab-pane>

        <a-tab-pane key="password" tab="修改密码">
          <a-form :model="passwordForm" layout="vertical">
            <a-form-item label="原密码">
              <a-input-password v-model:value="passwordForm.oldPassword" placeholder="请输入原密码" />
            </a-form-item>
            <a-form-item label="新密码">
              <a-input-password v-model:value="passwordForm.newPassword" placeholder="请输入新密码" />
            </a-form-item>
            <a-form-item label="确认新密码">
              <a-input-password v-model:value="passwordForm.confirmPassword" placeholder="请再次输入新密码" />
            </a-form-item>
            <a-form-item>
              <a-button type="primary" @click="handleChangePassword">修改密码</a-button>
            </a-form-item>
          </a-form>
        </a-tab-pane>

        <a-tab-pane key="follow" tab="关注列表">
          <a-space direction="vertical" style="width: 100%">
            <a-card title="我关注的" :bordered="false" size="small">
              <a-list :data-source="followingList" :locale="{ emptyText: '暂无关注' }">
                <template #renderItem="{ item }">
                  <a-list-item>
                    <a-list-item-meta>
                      <template #avatar>
                        <a-avatar :src="item.followedAvatar" />
                      </template>
                      <template #title>
                        {{ item.followedNickname || item.followedUsername }}
                      </template>
                      <template #description>
                        关注于 {{ formatDate(item.followedAt) }}
                      </template>
                    </a-list-item-meta>
                    <template #actions>
                      <a-button type="link" danger @click="handleUnfollow(item.followedUserId)">取消关注</a-button>
                    </template>
                  </a-list-item>
                </template>
              </a-list>
            </a-card>

            <a-card title="我的粉丝" :bordered="false" size="small">
              <a-list :data-source="followerList" :locale="{ emptyText: '暂无粉丝' }">
                <template #renderItem="{ item }">
                  <a-list-item>
                    <a-list-item-meta>
                      <template #avatar>
                        <a-avatar :src="item.followerAvatar" />
                      </template>
                      <template #title>
                        {{ item.followerNickname || item.followerUsername }}
                      </template>
                      <template #description>
                        关注于 {{ formatDate(item.followedAt) }}
                      </template>
                    </a-list-item-meta>
                  </a-list-item>
                </template>
              </a-list>
            </a-card>
          </a-space>
        </a-tab-pane>

        <a-tab-pane key="balance" tab="余额管理">
          <a-card title="账户余额" :bordered="false" class="balance-card">
            <a-descriptions :column="2" bordered>
              <a-descriptions-item label="可用余额">
                <span class="balance-amount">¥{{ balance.availableBalance?.toFixed(2) || '0.00' }}</span>
              </a-descriptions-item>
              <a-descriptions-item label="冻结金额">
                <span class="frozen-amount">¥{{ balance.frozenBalance?.toFixed(2) || '0.00' }}</span>
              </a-descriptions-item>
              <a-descriptions-item label="总收入">
                ¥{{ balance.totalIncome?.toFixed(2) || '0.00' }}
              </a-descriptions-item>
              <a-descriptions-item label="总提现">
                ¥{{ balance.totalWithdraw?.toFixed(2) || '0.00' }}
              </a-descriptions-item>
            </a-descriptions>
            <div class="balance-actions">
              <a-button type="primary" size="large" style="margin-right: 12px;" @click="showRechargeModal = true">
                充值
              </a-button>
              <a-button type="default" size="large" @click="handleNavigateToWithdraw">
                申请提现
              </a-button>
            </div>
          </a-card>
        </a-tab-pane>
      </a-tabs>
    </a-card>

    <!-- 充值模态框 -->
    <a-modal
      v-model:visible="showRechargeModal"
      title="充值"
      @ok="handleRecharge"
      @cancel="showRechargeModal = false"
    >
      <a-form :model="rechargeForm" layout="vertical">
        <a-form-item label="充值金额">
          <a-input-number
            v-model:value="rechargeForm.amount"
            :min="1"
            :step="1"
            :precision="2"
            style="width: 100%"
          />
        </a-form-item>
        <a-form-item label="支付方式">
          <a-select v-model:value="rechargeForm.paymentMethod" style="width: 100%">
            <a-select-option value="alipay">支付宝</a-select-option>
            <a-select-option value="wechat">微信支付</a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { message } from 'ant-design-vue';
import { PlusOutlined } from '@ant-design/icons-vue';
import { useRouter } from 'vue-router';
import { userApi } from '../api/user';
import { followApi } from '../api/follow';
import { withdrawApi, UserBalance } from '../api/withdraw';
import { rechargeApi } from '../api/recharge';
import { handleResponseAsync, errorHandler } from '../utils/errorHandler';
import { compressAvatar } from '../utils/imageCompress';

const router = useRouter();
const activeKey = ref('info');

const uploadAction = '/api/upload/avatar';
const uploadHeaders = {
  'Authorization': 'Bearer ' + (localStorage.getItem('token') || '')
};

const profileForm = ref({
  username: '',
  nickname: '',
  email: '',
  phone: '',
  avatar: '',
  gender: 0
});

const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
});

const followingList = ref<any[]>([]);
const followerList = ref<any[]>([]);
const balance = ref<UserBalance>({
  totalIncome: 0,
  availableBalance: 0,
  frozenBalance: 0,
  totalWithdraw: 0
});

const showRechargeModal = ref(false);
const rechargeForm = ref({
  amount: 100,
  paymentMethod: 'alipay'
});

onMounted(() => {
  loadUserInfo();
  loadFollowingList();
  loadFollowerList();
  loadBalance();
});

const loadUserInfo = async () => {
  try {
    const res = await userApi.getUserInfo();
    await handleResponseAsync(res, (data) => {
      if (data) {
        profileForm.value = {
          username: data.username || '',
          nickname: data.nickname || '',
          email: data.email || '',
          phone: data.phone || '',
          avatar: data.avatar || '',
          gender: data.gender || 0
        };
      }
    });
  } catch (error) {
    errorHandler.handle(error);
  }
};

const loadBalance = async () => {
  try {
    const res = await withdrawApi.getBalance();
    await handleResponseAsync(res, (data) => {
      if (data) {
        balance.value = {
          totalIncome: data.totalIncome || 0,
          availableBalance: data.availableBalance || 0,
          frozenBalance: data.frozenBalance || 0,
          totalWithdraw: data.totalWithdraw || 0
        };
      }
    });
  } catch (error) {
    errorHandler.handle(error);
  }
};

const loadFollowingList = async () => {
  try {
    const res = await followApi.getFollowingList();
    await handleResponseAsync(res, (data) => {
      followingList.value = data?.list || [];
    });
  } catch (error) {
    errorHandler.handle(error);
  }
};

const loadFollowerList = async () => {
  try {
    const res = await followApi.getFollowerList();
    await handleResponseAsync(res, (data) => {
      followerList.value = data?.list || [];
    });
  } catch (error) {
    errorHandler.handle(error);
  }
};

const beforeUpload = async (file: any) => {
  const isImage = file.type === 'image/jpeg' || file.type === 'image/png' || file.type === 'image/gif' || file.type === 'image/webp';
  if (!isImage) {
    message.error('只能上传 JPG/PNG/GIF/WebP 格式的图片!');
    return false;
  }
  const isLt10M = file.size / 1024 / 1024 < 10;
  if (!isLt10M) {
    message.error('头像大小不能超过 10MB!');
    return false;
  }
  return true;
};

const customRequest = async (options: any) => {
  const { file, onSuccess, onError, onProgress } = options;
  try {
    onProgress({ percent: 10 });
    const compressedBlob = await compressAvatar(file);
    onProgress({ percent: 30 });

    const formData = new FormData();
    formData.append('file', compressedBlob, file.name);

    const response = await fetch('/api/upload/avatar', {
      method: 'POST',
      headers: {
        'Authorization': 'Bearer ' + (localStorage.getItem('token') || '')
      },
      body: formData
    });

    onProgress({ percent: 90 });
    const result = await response.json();
    if (result.code === 200) {
      onSuccess(result);
    } else {
      onError(new Error(result.message || '上传失败'));
    }
  } catch (error) {
    onError(error);
  }
};

const handleAvatarChange = (info: any) => {
  if (info.file.status === 'uploading') {
    return;
  }
  if (info.file.status === 'done') {
    const response = info.file.response;
    if (response && response.code === 200) {
      profileForm.value.avatar = response.data.url;
      message.success('头像上传成功');
    } else {
      message.error('头像上传失败');
    }
  }
};

const handleSaveProfile = async () => {
  try {
    const res = await userApi.updateProfile({
      nickname: profileForm.value.nickname,
      email: profileForm.value.email,
      phone: profileForm.value.phone,
      gender: profileForm.value.gender,
      avatar: profileForm.value.avatar
    });
    await handleResponseAsync(res, () => {
      message.success('个人信息更新成功');
      if (profileForm.value.avatar) {
        localStorage.setItem('avatar', profileForm.value.avatar);
        window.dispatchEvent(new Event('avatar-updated'));
      }
      loadUserInfo();
    });
  } catch (error) {
    errorHandler.handle(error);
  }
};

const handleChangePassword = async () => {
  if (!passwordForm.value.oldPassword) {
    message.error('请输入原密码');
    return;
  }
  if (!passwordForm.value.newPassword) {
    message.error('请输入新密码');
    return;
  }
  if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
    message.error('两次输入的密码不一致');
    return;
  }
  try {
    const res = await userApi.changePassword(passwordForm.value.oldPassword, passwordForm.value.newPassword);
    await handleResponseAsync(res, () => {
      message.success('密码修改成功');
      passwordForm.value = { oldPassword: '', newPassword: '', confirmPassword: '' };
    });
  } catch (error) {
    errorHandler.handle(error);
  }
};

const handleUnfollow = async (userId: number) => {
  try {
    const res = await followApi.unfollow(userId);
    await handleResponseAsync(res, () => {
      message.success('取消关注成功');
      loadFollowingList();
    });
  } catch (error) {
    errorHandler.handle(error);
  }
};

const handleNavigateToWithdraw = () => {
  router.push('/anchor/withdraw');
};

const formatDate = (date: string) => {
  if (!date) return '';
  return new Date(date).toLocaleString();
};

const handleRecharge = async () => {
  if (!rechargeForm.value.amount || rechargeForm.value.amount < 1) {
    message.error('请输入有效的充值金额');
    return;
  }

  try {
    const response = await rechargeApi.createRecharge(rechargeForm.value.amount, rechargeForm.value.paymentMethod);
    await handleResponseAsync(response, async (recharge) => {
      // 模拟支付成功，实际项目中应该跳转到支付页面
      await rechargeApi.completeRecharge(recharge.id!);
      message.success('充值成功');
      showRechargeModal.value = false;
      loadBalance();
    });
  } catch (error) {
    errorHandler.handle(error);
  }
};
</script>

<style scoped>
.profile-container {
  padding: 24px;
  background: linear-gradient(180deg, #0f0f0f 0%, #1a1a1a 100%);
  min-height: calc(100vh - 72px);
  margin: -32px;
}

.profile-card {
  max-width: 800px;
  margin: 0 auto;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 12px;
}

.profile-card :deep(.ant-card-head) {
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  color: #fff;
  min-height: 48px;
}

.profile-card :deep(.ant-card-head-title) {
  color: #fff;
  font-weight: 600;
  font-size: 15px;
}

.profile-card :deep(.ant-tabs-nav) {
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.profile-card :deep(.ant-tabs-tab) {
  color: rgba(255, 255, 255, 0.65);
}

.profile-card :deep(.ant-tabs-tab-active) {
  color: #ff4757;
}

.profile-card :deep(.ant-tabs-ink-bar) {
  background: #ff4757;
}

.profile-card :deep(.ant-form-item-label > label) {
  color: rgba(255, 255, 255, 0.85);
}

.profile-card :deep(.ant-input) {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.2);
  color: #fff;
}

.profile-card :deep(.ant-input-disabled) {
  background: rgba(255, 255, 255, 0.03);
  color: rgba(255, 255, 255, 0.6);
}

.profile-card :deep(.ant-input::placeholder) {
  color: rgba(255, 255, 255, 0.4);
}

.profile-card :deep(.ant-input-password) {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.profile-card :deep(.ant-input-password .ant-input) {
  background: transparent;
  border: none;
}

.profile-card :deep(.ant-radio-wrapper) {
  color: rgba(255, 255, 255, 0.85);
}

.profile-card :deep(.ant-radio-inner) {
  background: rgba(255, 255, 255, 0.05);
  border-color: rgba(255, 255, 255, 0.3);
}

.profile-card :deep(.ant-radio-checked .ant-radio-inner) {
  border-color: #ff4757;
}

.profile-card :deep(.ant-radio-checked .ant-radio-inner::after) {
  background-color: #ff4757;
}

.profile-card :deep(.ant-btn-primary) {
  background: linear-gradient(135deg, #ff4757 0%, #ff6b81 100%);
  border: none;
}

.profile-card :deep(.ant-btn-primary:hover) {
  background: linear-gradient(135deg, #ff6b81 0%, #ff4757 100%);
}

.avatar-uploader {
  display: flex;
  align-items: center;
}

.avatar {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  object-fit: cover;
}

.avatar-placeholder {
  width: 100px;
  height: 100px;
  border: 1px dashed rgba(255, 255, 255, 0.3);
  border-radius: 50%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: rgba(255, 255, 255, 0.6);
}

.avatar-placeholder:hover {
  border-color: #ff4757;
  color: #ff4757;
}

.profile-card :deep(.ant-card) {
  background: rgba(255, 255, 255, 0.03) !important;
  border: 1px solid rgba(255, 255, 255, 0.08) !important;
}

.profile-card :deep(.ant-card .ant-card-head) {
  border-bottom: 1px solid rgba(255, 255, 255, 0.1) !important;
  background: transparent !important;
  color: #fff !important;
}

.profile-card :deep(.ant-card .ant-card-head-title) {
  color: #fff !important;
  font-weight: 600 !important;
  font-size: 15px !important;
}

.profile-card :deep(.ant-list-item) {
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  padding: 16px 0;
}

.profile-card :deep(.ant-list-item:last-child) {
  border-bottom: none;
}

.profile-card :deep(.ant-list-item-meta-title) {
  color: #fff !important;
  font-weight: 500;
  font-size: 14px;
  margin-bottom: 4px;
}

.profile-card :deep(.ant-list-item-meta-description) {
  color: rgba(255, 255, 255, 0.75) !important;
  font-size: 13px;
}

.profile-card :deep(.ant-btn-link) {
  color: #ff4757;
  font-size: 13px;
}

.profile-card :deep(.ant-btn-link:hover) {
  color: #ff6b81;
}

.profile-card :deep(.ant-empty) {
  color: rgba(255, 255, 255, 0.7);
}

.profile-card :deep(.ant-empty-description) {
  color: rgba(255, 255, 255, 0.7);
  font-size: 14px;
}

.balance-card {
  margin-top: 20px;
}

.balance-card :deep(.ant-descriptions-item-label) {
  color: rgba(255, 255, 255, 0.85);
  font-weight: 500;
}

.balance-card :deep(.ant-descriptions-item-content) {
  color: #fff;
}

.balance-amount {
  font-size: 24px;
  font-weight: 600;
  color: #52c41a;
}

.frozen-amount {
  font-size: 24px;
  font-weight: 600;
  color: #faad14;
}

.balance-actions {
  margin-top: 24px;
  text-align: center;
}

.balance-actions :deep(.ant-btn-primary) {
  background: linear-gradient(135deg, #ff4757 0%, #ff6b81 100%);
  border: none;
  min-width: 200px;
  height: 44px;
  font-size: 16px;
  font-weight: 600;
}

.balance-actions :deep(.ant-btn-primary:hover) {
  background: linear-gradient(135deg, #ff6b81 0%, #ff4757 100%);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(255, 71, 87, 0.3);
}
</style>
