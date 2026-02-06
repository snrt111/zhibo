<script setup lang="ts">
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import { message } from 'ant-design-vue';
import { userApi } from '../api/user';

const router = useRouter();
const loading = ref(false);
const formState = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  nickname: '',
  email: '',
  phone: ''
});

const validateEmail = (email: string) => {
  const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return re.test(email);
};

const validatePhone = (phone: string) => {
  const re = /^1[3-9]\d{9}$/;
  return re.test(phone);
};

const handleSubmit = async () => {
  if (!formState.username || !formState.password || !formState.confirmPassword) {
    message.error('请填写完整信息');
    return;
  }

  if (formState.username.length < 3) {
    message.error('用户名至少3个字符');
    return;
  }

  if (formState.password.length < 6) {
    message.error('密码至少6个字符');
    return;
  }

  if (formState.password !== formState.confirmPassword) {
    message.error('两次输入的密码不一致');
    return;
  }

  if (formState.email && !validateEmail(formState.email)) {
    message.error('请输入有效的邮箱地址');
    return;
  }

  if (formState.phone && !validatePhone(formState.phone)) {
    message.error('请输入有效的手机号码');
    return;
  }

  loading.value = true;
  try {
    const response = await userApi.register({
      username: formState.username,
      password: formState.password,
      nickname: formState.nickname || formState.username,
      email: formState.email || undefined,
      phone: formState.phone || undefined
    });

    if (response.code === 200) {
      message.success('注册成功，请登录');
      router.push('/login');
    } else {
      message.error(response.message || '注册失败');
    }
  } catch (error: any) {
    console.error('注册错误:', error);
    message.error(error.message || '注册失败，请检查网络连接');
  } finally {
    loading.value = false;
  }
};

const goToLogin = () => {
  router.push('/login');
};
</script>

<template>
  <div class="register-container">
    <div class="register-form">
      <h2>直播平台注册</h2>
      <div class="form-item">
        <label for="username">用户名 <span class="required">*</span></label>
        <input 
          type="text" 
          id="username" 
          v-model="formState.username" 
          placeholder="请输入用户名（至少3个字符）"
        />
      </div>
      <div class="form-item">
        <label for="password">密码 <span class="required">*</span></label>
        <input 
          type="password" 
          id="password" 
          v-model="formState.password" 
          placeholder="请输入密码（至少6个字符）"
        />
      </div>
      <div class="form-item">
        <label for="confirmPassword">确认密码 <span class="required">*</span></label>
        <input 
          type="password" 
          id="confirmPassword" 
          v-model="formState.confirmPassword" 
          placeholder="请再次输入密码"
        />
      </div>
      <div class="form-item">
        <label for="nickname">昵称</label>
        <input 
          type="text" 
          id="nickname" 
          v-model="formState.nickname" 
          placeholder="请输入昵称（可选）"
        />
      </div>
      <div class="form-item">
        <label for="email">邮箱</label>
        <input 
          type="email" 
          id="email" 
          v-model="formState.email" 
          placeholder="请输入邮箱地址（可选）"
        />
      </div>
      <div class="form-item">
        <label for="phone">手机号</label>
        <input 
          type="tel" 
          id="phone" 
          v-model="formState.phone" 
          placeholder="请输入手机号码（可选）"
        />
      </div>
      <button 
        class="register-button" 
        @click="handleSubmit" 
        :disabled="loading"
      >
        {{ loading ? '注册中...' : '注册' }}
      </button>
      <div class="login-link">
        已有账号？<a @click="goToLogin">立即登录</a>
      </div>
    </div>
  </div>
</template>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #f0f2f5;
}

.register-form {
  width: 450px;
  padding: 40px;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.register-form h2 {
  text-align: center;
  margin-bottom: 30px;
  color: #1890ff;
}

.form-item {
  margin-bottom: 20px;
}

.form-item label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
}

.form-item .required {
  color: #ff4d4f;
}

.form-item input {
  width: 100%;
  padding: 12px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  font-size: 16px;
}

.form-item input:focus {
  outline: none;
  border-color: #1890ff;
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);
}

.register-button {
  width: 100%;
  padding: 12px;
  background-color: #1890ff;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 16px;
  cursor: pointer;
  margin-top: 20px;
}

.register-button:hover {
  background-color: #40a9ff;
}

.register-button:disabled {
  background-color: #1890ff;
  opacity: 0.6;
  cursor: not-allowed;
}

.login-link {
  margin-top: 20px;
  text-align: center;
  font-size: 14px;
  color: #666;
}

.login-link a {
  color: #1890ff;
  cursor: pointer;
}

.login-link a:hover {
  text-decoration: underline;
}
</style>
