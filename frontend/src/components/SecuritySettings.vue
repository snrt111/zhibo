<template>
  <div class="security-settings-container">
    <a-card title="账号安全" :bordered="false" class="security-card">
      <!-- 安全状态概览 -->
      <div class="security-overview">
        <h3>安全状态</h3>
        <a-row :gutter="16">
          <a-col :span="8">
            <div class="security-item" :class="{ 'is-bound': securityInfo.hasPassword }">
              <LockOutlined class="security-icon" />
              <div class="security-info">
                <div class="security-title">登录密码</div>
                <div class="security-status">{{ securityInfo.hasPassword ? '已设置' : '未设置' }}</div>
              </div>
            </div>
          </a-col>
          <a-col :span="8">
            <div class="security-item" :class="{ 'is-bound': securityInfo.hasPhone }">
              <PhoneOutlined class="security-icon" />
              <div class="security-info">
                <div class="security-title">手机绑定</div>
                <div class="security-status">{{ securityInfo.hasPhone ? securityInfo.phone : '未绑定' }}</div>
              </div>
            </div>
          </a-col>
          <a-col :span="8">
            <div class="security-item" :class="{ 'is-bound': securityInfo.hasEmail }">
              <MailOutlined class="security-icon" />
              <div class="security-info">
                <div class="security-title">邮箱绑定</div>
                <div class="security-status">{{ securityInfo.hasEmail ? securityInfo.email : '未绑定' }}</div>
              </div>
            </div>
          </a-col>
        </a-row>
      </div>

      <a-divider />

      <!-- 修改密码 -->
      <div class="security-section">
        <h3>修改密码</h3>
        <a-form :model="passwordForm" layout="vertical">
          <a-form-item label="原密码" :rules="[{ required: true, message: '请输入原密码' }]">
            <a-input-password v-model:value="passwordForm.oldPassword" placeholder="请输入原密码" />
          </a-form-item>
          <a-form-item label="新密码" :rules="[{ required: true, message: '请输入新密码' }, { min: 6, message: '密码至少6个字符' }]">
            <a-input-password v-model:value="passwordForm.newPassword" placeholder="请输入新密码" />
          </a-form-item>
          <a-form-item label="确认新密码" :rules="[{ required: true, message: '请确认新密码' }]">
            <a-input-password v-model:value="passwordForm.confirmPassword" placeholder="请再次输入新密码" />
          </a-form-item>
          <a-form-item>
            <a-button type="primary" @click="handleChangePassword" :loading="passwordLoading">
              修改密码
            </a-button>
          </a-form-item>
        </a-form>
      </div>

      <a-divider />

      <!-- 手机绑定 -->
      <div class="security-section">
        <h3>手机绑定</h3>
        <div v-if="securityInfo.hasPhone" class="bound-info">
          <p>已绑定手机号：{{ securityInfo.phone }}</p>
          <a-button type="primary" danger @click="handleUnbindPhone" :loading="unbindPhoneLoading">
            解绑手机
          </a-button>
        </div>
        <a-form v-else :model="phoneForm" layout="vertical">
          <a-form-item label="手机号" :rules="[{ required: true, message: '请输入手机号' }, { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号' }]">
            <a-input v-model:value="phoneForm.phone" placeholder="请输入手机号">
              <template #addonAfter>
                <a-button type="link" size="small" @click="handleSendPhoneCode" :disabled="phoneCountdown > 0" :loading="sendPhoneCodeLoading">
                  {{ phoneCountdown > 0 ? `${phoneCountdown}秒后重试` : '获取验证码' }}
                </a-button>
              </template>
            </a-input>
          </a-form-item>
          <a-form-item label="验证码" :rules="[{ required: true, message: '请输入验证码' }]">
            <a-input v-model:value="phoneForm.code" placeholder="请输入验证码" />
          </a-form-item>
          <a-form-item>
            <a-button type="primary" @click="handleBindPhone" :loading="bindPhoneLoading">
              绑定手机
            </a-button>
          </a-form-item>
        </a-form>
      </div>

      <a-divider />

      <!-- 邮箱绑定 -->
      <div class="security-section">
        <h3>邮箱绑定</h3>
        <div v-if="securityInfo.hasEmail" class="bound-info">
          <p>已绑定邮箱：{{ securityInfo.email }}</p>
          <a-button type="primary" danger @click="handleUnbindEmail" :loading="unbindEmailLoading">
            解绑邮箱
          </a-button>
        </div>
        <a-form v-else :model="emailForm" layout="vertical">
          <a-form-item label="邮箱" :rules="[{ required: true, message: '请输入邮箱' }, { type: 'email', message: '请输入正确的邮箱' }]">
            <a-input v-model:value="emailForm.email" placeholder="请输入邮箱">
              <template #addonAfter>
                <a-button type="link" size="small" @click="handleSendEmailCode" :disabled="emailCountdown > 0" :loading="sendEmailCodeLoading">
                  {{ emailCountdown > 0 ? `${emailCountdown}秒后重试` : '获取验证码' }}
                </a-button>
              </template>
            </a-input>
          </a-form-item>
          <a-form-item label="验证码" :rules="[{ required: true, message: '请输入验证码' }]">
            <a-input v-model:value="emailForm.code" placeholder="请输入验证码" />
          </a-form-item>
          <a-form-item>
            <a-button type="primary" @click="handleBindEmail" :loading="bindEmailLoading">
              绑定邮箱
            </a-button>
          </a-form-item>
        </a-form>
      </div>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { message } from 'ant-design-vue';
import { LockOutlined, PhoneOutlined, MailOutlined } from '@ant-design/icons-vue';
import { userApi } from '../api/user';
import { handleResponseAsync, errorHandler } from '../utils/errorHandler';

const securityInfo = reactive({
  hasPassword: false,
  hasPhone: false,
  hasEmail: false,
  phone: '',
  email: ''
});

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
});

const phoneForm = reactive({
  phone: '',
  code: ''
});

const emailForm = reactive({
  email: '',
  code: ''
});

const passwordLoading = ref(false);
const bindPhoneLoading = ref(false);
const bindEmailLoading = ref(false);
const unbindPhoneLoading = ref(false);
const unbindEmailLoading = ref(false);
const sendPhoneCodeLoading = ref(false);
const sendEmailCodeLoading = ref(false);
const phoneCountdown = ref(0);
const emailCountdown = ref(0);

const fetchSecurityInfo = async () => {
  try {
    const response = await userApi.getSecurityInfo();
    await handleResponseAsync(response, (data) => {
      Object.assign(securityInfo, data);
    });
  } catch (error) {
    errorHandler.handle(error);
  }
};

const handleChangePassword = async () => {
  if (!passwordForm.oldPassword || !passwordForm.newPassword || !passwordForm.confirmPassword) {
    message.error('请填写所有密码字段');
    return;
  }

  if (passwordForm.newPassword.length < 6) {
    message.error('新密码至少6个字符');
    return;
  }

  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    message.error('两次输入的新密码不一致');
    return;
  }

  passwordLoading.value = true;
  try {
    const response = await userApi.changePassword(passwordForm.oldPassword, passwordForm.newPassword);
    console.log('修改密码响应:', response);
    await handleResponseAsync(response, () => {
      message.success('密码修改成功');
      passwordForm.oldPassword = '';
      passwordForm.newPassword = '';
      passwordForm.confirmPassword = '';
    });
  } catch (error) {
    console.error('修改密码错误:', error);
    errorHandler.handle(error);
  } finally {
    passwordLoading.value = false;
  }
};

const handleSendPhoneCode = async () => {
  if (!phoneForm.phone || !/^1[3-9]\d{9}$/.test(phoneForm.phone)) {
    message.error('请输入正确的手机号');
    return;
  }

  sendPhoneCodeLoading.value = true;
  try {
    const response = await userApi.sendSmsCode(phoneForm.phone);
    await handleResponseAsync(response, () => {
      message.success('验证码已发送');
      phoneCountdown.value = 60;
      const timer = setInterval(() => {
        phoneCountdown.value--;
        if (phoneCountdown.value <= 0) {
          clearInterval(timer);
        }
      }, 1000);
    });
  } catch (error) {
    errorHandler.handle(error);
  } finally {
    sendPhoneCodeLoading.value = false;
  }
};

const handleBindPhone = async () => {
  if (!phoneForm.phone || !phoneForm.code) {
    message.error('请填写手机号和验证码');
    return;
  }

  bindPhoneLoading.value = true;
  try {
    const response = await userApi.bindPhone(phoneForm.phone, phoneForm.code);
    await handleResponseAsync(response, () => {
      message.success('手机号绑定成功');
      phoneForm.phone = '';
      phoneForm.code = '';
      fetchSecurityInfo();
    });
  } catch (error) {
    errorHandler.handle(error);
  } finally {
    bindPhoneLoading.value = false;
  }
};

const handleUnbindPhone = async () => {
  unbindPhoneLoading.value = true;
  try {
    const response = await userApi.unbindPhone();
    await handleResponseAsync(response, () => {
      message.success('手机号解绑成功');
      fetchSecurityInfo();
    });
  } catch (error) {
    errorHandler.handle(error);
  } finally {
    unbindPhoneLoading.value = false;
  }
};

const handleSendEmailCode = async () => {
  if (!emailForm.email || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(emailForm.email)) {
    message.error('请输入正确的邮箱');
    return;
  }

  sendEmailCodeLoading.value = true;
  try {
    const response = await userApi.sendEmailCode(emailForm.email);
    await handleResponseAsync(response, () => {
      message.success('验证码已发送');
      emailCountdown.value = 60;
      const timer = setInterval(() => {
        emailCountdown.value--;
        if (emailCountdown.value <= 0) {
          clearInterval(timer);
        }
      }, 1000);
    });
  } catch (error) {
    errorHandler.handle(error);
  } finally {
    sendEmailCodeLoading.value = false;
  }
};

const handleBindEmail = async () => {
  if (!emailForm.email || !emailForm.code) {
    message.error('请填写邮箱和验证码');
    return;
  }

  bindEmailLoading.value = true;
  try {
    const response = await userApi.bindEmail(emailForm.email, emailForm.code);
    console.log('绑定邮箱响应:', response);
    await handleResponseAsync(response, () => {
      message.success('邮箱绑定成功');
      emailForm.email = '';
      emailForm.code = '';
      fetchSecurityInfo();
    });
  } catch (error) {
    console.error('绑定邮箱错误:', error);
    errorHandler.handle(error);
  } finally {
    bindEmailLoading.value = false;
  }
};

const handleUnbindEmail = async () => {
  unbindEmailLoading.value = true;
  try {
    const response = await userApi.unbindEmail();
    await handleResponseAsync(response, () => {
      message.success('邮箱解绑成功');
      fetchSecurityInfo();
    });
  } catch (error) {
    errorHandler.handle(error);
  } finally {
    unbindEmailLoading.value = false;
  }
};

onMounted(() => {
  fetchSecurityInfo();
});
</script>

<style scoped>
.security-settings-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 24px;
}

.security-card {
  background: #fff;
  border-radius: 8px;
}

.security-overview {
  margin-bottom: 24px;
}

.security-overview h3 {
  margin-bottom: 16px;
  font-size: 16px;
  font-weight: 600;
}

.security-item {
  display: flex;
  align-items: center;
  padding: 16px;
  background: #f5f5f5;
  border-radius: 8px;
  border: 1px solid #e8e8e8;
}

.security-item.is-bound {
  background: #f6ffed;
  border-color: #b7eb8f;
}

.security-icon {
  font-size: 24px;
  color: #999;
  margin-right: 12px;
}

.security-item.is-bound .security-icon {
  color: #52c41a;
}

.security-info {
  flex: 1;
}

.security-title {
  font-size: 14px;
  color: #333;
  margin-bottom: 4px;
}

.security-status {
  font-size: 12px;
  color: #999;
}

.security-item.is-bound .security-status {
  color: #52c41a;
}

.security-section {
  margin-bottom: 24px;
}

.security-section h3 {
  margin-bottom: 16px;
  font-size: 16px;
  font-weight: 600;
}

.bound-info {
  padding: 16px;
  background: #f6ffed;
  border-radius: 8px;
  border: 1px solid #b7eb8f;
}

.bound-info p {
  margin-bottom: 12px;
  color: #333;
}
</style>
