<script setup lang="ts">
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import { message } from 'ant-design-vue';
import { userApi } from '../api/user';

const router = useRouter();
const loading = ref(false);
const formState = reactive({
  username: '',
  password: ''
});

const goToRegister = () => {
  router.push('/register');
};

const handleSubmit = async () => {
  if (!formState.username || !formState.password) {
    message.error('请输入用户名和密码');
    return;
  }

  loading.value = true;
  try {
    const response = await userApi.login({
      username: formState.username,
      password: formState.password
    });

    if (response.code === 200) {
      // 保存token到localStorage
      localStorage.setItem('token', response.token || '');
      localStorage.setItem('userType', response.user?.userType?.toString() || '');
      localStorage.setItem('username', response.user?.username || '');
      message.success('登录成功');
      
      // 根据用户类型跳转到不同页面
      const type = response.user?.userType;
      if (type === 2) {
        router.push('/admin');
      } else if (type === 1) {
        router.push('/anchor');
      } else {
        router.push('/');
      }
    } else {
      message.error(response.message || '登录失败');
    }
  } catch (error) {
    console.error('登录错误:', error);
    message.error('登录失败，请检查网络连接');
  } finally {
    loading.value = false;
  }
};
</script>

<template>
  <div class="login-container">
    <div class="login-form">
      <h2>直播平台登录</h2>
      <div class="form-item">
        <label for="username">用户名</label>
        <input 
          type="text" 
          id="username" 
          v-model="formState.username" 
          placeholder="请输入用户名"
        />
      </div>
      <div class="form-item">
        <label for="password">密码</label>
        <input 
          type="password" 
          id="password" 
          v-model="formState.password" 
          placeholder="请输入密码"
        />
      </div>
      <button 
        class="login-button" 
        @click="handleSubmit" 
        :disabled="loading"
      >
        {{ loading ? '登录中...' : '登录' }}
      </button>
      <div class="register-link">
        还没有账号？<a @click="goToRegister">立即注册</a>
      </div>
      <div class="login-tips">
        <p>默认账号：</p>
        <p>管理员: admin / 123456</p>
        <p>主播: anchor1 / 123456</p>
        <p>用户: user1 / 123456</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #f0f2f5;
}

.login-form {
  width: 400px;
  padding: 40px;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.login-form h2 {
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

.login-button {
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

.login-button:hover {
  background-color: #40a9ff;
}

.login-button:disabled {
  background-color: #1890ff;
  opacity: 0.6;
  cursor: not-allowed;
}

.register-link {
  margin-top: 20px;
  text-align: center;
  font-size: 14px;
  color: #666;
}

.register-link a {
  color: #1890ff;
  cursor: pointer;
}

.register-link a:hover {
  text-decoration: underline;
}

.login-tips {
  margin-top: 30px;
  text-align: center;
  font-size: 14px;
  color: #666;
}

.login-tips p {
  margin: 5px 0;
}
</style>