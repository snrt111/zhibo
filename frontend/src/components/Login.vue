<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { message } from 'ant-design-vue';
import { userApi } from '../api/user';
import { handleResponseAsync, errorHandler } from '../utils/errorHandler';

const router = useRouter();
const route = useRoute();
const loading = ref(false);
const loginType = ref<'password' | 'phone' | 'email'>('password');
const formState = reactive({
  username: '',
  password: '',
  phone: '',
  email: '',
  code: ''
});
const codeCountdown = ref(0);
const codeButtonText = ref('获取验证码');

const goToRegister = () => {
  router.push('/register');
};

const handleSubmit = async () => {
  if (loginType.value === 'password') {
    await handlePasswordLogin();
  } else if (loginType.value === 'phone') {
    await handlePhoneLogin();
  } else {
    await handleEmailLogin();
  }
};

const handleLoginSuccess = (data: any) => {
  const userData = data || {};
  localStorage.setItem('token', userData.token || '');
  localStorage.setItem('userType', userData.user?.userType?.toString() || '');
  localStorage.setItem('username', userData.user?.username || '');
  localStorage.setItem('avatar', userData.user?.avatar || '');
  localStorage.setItem('userId', userData.user?.id?.toString() || '');
  message.success('登录成功');
  
  const redirect = (route.query.redirect as string) || '/';
  const type = userData.user?.userType;
  
  if (redirect === '/admin' && type !== 1) {
    router.push('/');
  } else {
    router.push(redirect);
  }
};

const handlePasswordLogin = async () => {
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
    await handleResponseAsync(response, handleLoginSuccess);
  } catch (error) {
    errorHandler.handle(error);
  } finally {
    loading.value = false;
  }
};

const handlePhoneLogin = async () => {
  if (!formState.phone || !formState.code) {
    message.error('请输入手机号和验证码');
    return;
  }

  loading.value = true;
  try {
    const response = await userApi.loginWithPhone(formState.phone, formState.code);
    await handleResponseAsync(response, handleLoginSuccess);
  } catch (error) {
    errorHandler.handle(error);
  } finally {
    loading.value = false;
  }
};

const handleEmailLogin = async () => {
  if (!formState.email || !formState.code) {
    message.error('请输入邮箱和验证码');
    return;
  }

  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  if (!emailRegex.test(formState.email)) {
    message.error('请输入有效的邮箱地址');
    return;
  }

  loading.value = true;
  try {
    const response = await userApi.loginWithEmail(formState.email, formState.code);
    await handleResponseAsync(response, handleLoginSuccess);
  } catch (error) {
    errorHandler.handle(error);
  } finally {
    loading.value = false;
  }
};

const sendCode = async () => {
  if (loginType.value === 'phone') {
    if (!formState.phone) {
      message.error('请输入手机号');
      return;
    }

    if (codeCountdown.value > 0) {
      return;
    }

    try {
      const response = await userApi.sendSmsCode(formState.phone);
      await handleResponseAsync(response, () => {
        message.success('验证码发送成功');
        codeCountdown.value = 60;
        codeButtonText.value = `${codeCountdown.value}秒后重新获取`;
        const timer = setInterval(() => {
          codeCountdown.value--;
          if (codeCountdown.value <= 0) {
            clearInterval(timer);
            codeButtonText.value = '获取验证码';
          } else {
            codeButtonText.value = `${codeCountdown.value}秒后重新获取`;
          }
        }, 1000);
      });
    } catch (error) {
      errorHandler.handle(error);
    }
  } else if (loginType.value === 'email') {
    if (!formState.email) {
      message.error('请输入邮箱');
      return;
    }

    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(formState.email)) {
      message.error('请输入有效的邮箱地址');
      return;
    }

    if (codeCountdown.value > 0) {
      return;
    }

    try {
      const response = await userApi.sendEmailCode(formState.email);
      await handleResponseAsync(response, () => {
        message.success('验证码发送成功');
        codeCountdown.value = 60;
        codeButtonText.value = `${codeCountdown.value}秒后重新获取`;
        const timer = setInterval(() => {
          codeCountdown.value--;
          if (codeCountdown.value <= 0) {
            clearInterval(timer);
            codeButtonText.value = '获取验证码';
          } else {
            codeButtonText.value = `${codeCountdown.value}秒后重新获取`;
          }
        }, 1000);
      });
    } catch (error) {
      errorHandler.handle(error);
    }
  }
};

const handleOAuth2Login = async (provider: string) => {
  try {
    if (import.meta.env.VITE_MOCK_MODE === 'true') {
      const mockResponse = {
        code: 200,
        message: "登录成功",
        data: {
          token: "mock_token_" + provider,
          user: {
            id: 9999,
            username: provider + "_mock_user",
            nickname: provider + "模拟用户",
            avatar: "https://via.placeholder.com/100",
            userType: 0,
            status: 1
          }
        }
      };
      handleLoginSuccess(mockResponse.data);
      return;
    }
    const response = await userApi.getOAuth2AuthorizeUrl(provider);
    await handleResponseAsync(response, (data) => {
      const authorizeUrl = data || '';
      if (authorizeUrl) {
        window.location.href = authorizeUrl;
      } else {
        message.error('获取授权URL失败');
      }
    });
  } catch (error) {
    errorHandler.handle(error);
  }
};

const handleOAuth2Callback = async () => {
  const urlParams = new URLSearchParams(window.location.search);
  const code = urlParams.get('code');
  const state = urlParams.get('state');

  if (code && state) {
    const provider = state.split('_')[0];
    loading.value = true;
    try {
      let response;
      switch (provider) {
        case 'github':
          response = await userApi.loginWithGithub(code, state);
          break;
        case 'wechat':
          response = await userApi.loginWithWechat(code, state);
          break;
        case 'qq':
          response = await userApi.loginWithQQ(code, state);
          break;
        case 'weibo':
          response = await userApi.loginWithWeibo(code, state);
          break;
        default:
          message.error('不支持的登录方式');
          return;
      }
      await handleResponseAsync(response, handleLoginSuccess);
    } catch (error) {
      errorHandler.handle(error);
    } finally {
      loading.value = false;
    }
  }
};

onMounted(() => {
  handleOAuth2Callback();
});
</script>

<template>
  <div class="login-container">
    <div class="dynamic-bg">
      <div class="bg-gradient bg-1"></div>
      <div class="bg-gradient bg-2"></div>
      <div class="bg-gradient bg-3"></div>
      <div class="bg-particles">
        <span v-for="n in 20" :key="n" class="particle" :style="{
          left: Math.random() * 100 + '%',
          animationDelay: Math.random() * 5 + 's',
          animationDuration: (15 + Math.random() * 10) + 's'
        }"></span>
      </div>
    </div>
    <div class="login-wrapper">
      <div class="login-card">
        <div class="login-header">
          <div class="logo-icon">🎬</div>
          <h2>直播平台</h2>
          <p class="subtitle">欢迎回来，精彩直播等你开启</p>
        </div>
        <div class="login-tabs">
          <span 
            :class="['tab', { active: loginType === 'password' }]" 
            @click="loginType = 'password'"
          >密码登录</span>
          <span 
            :class="['tab', { active: loginType === 'phone' }]" 
            @click="loginType = 'phone'"
          >手机号登录</span>
          <span 
            :class="['tab', { active: loginType === 'email' }]" 
            @click="loginType = 'email'"
          >邮箱登录</span>
        </div>
        <template v-if="loginType === 'password'">
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
        </template>
        <template v-else-if="loginType === 'phone'">
          <div class="form-item">
            <label for="phone">手机号</label>
            <input 
              type="tel" 
              id="phone" 
              v-model="formState.phone" 
              placeholder="请输入手机号"
            />
          </div>
          <div class="form-item code-item">
            <label for="code">验证码</label>
            <input 
              type="text" 
              id="code" 
              v-model="formState.code" 
              placeholder="请输入验证码"
            />
            <button 
              class="code-button" 
              @click="sendCode" 
              :disabled="codeCountdown > 0"
            >
              {{ codeButtonText }}
            </button>
          </div>
        </template>
        <template v-else>
          <div class="form-item">
            <label for="email">邮箱</label>
            <input 
              type="email" 
              id="email" 
              v-model="formState.email" 
              placeholder="请输入邮箱地址"
            />
          </div>
          <div class="form-item code-item">
            <label for="code">验证码</label>
            <input 
              type="text" 
              id="code" 
              v-model="formState.code" 
              placeholder="请输入验证码"
            />
            <button 
              class="code-button" 
              @click="sendCode" 
              :disabled="codeCountdown > 0"
            >
              {{ codeButtonText }}
            </button>
          </div>
        </template>
        <button 
          class="login-button" 
          @click="handleSubmit" 
          :disabled="loading"
        >
          {{ loading ? '登录中...' : '立即登录' }}
        </button>
        <div class="oauth-login">
          <div class="divider">
            <span>其他登录方式</span>
          </div>
          <div class="oauth-buttons">
            <button class="oauth-btn github" @click="handleOAuth2Login('github')">
              <span class="icon">🐙</span>
            </button>
            <button class="oauth-btn wechat" @click="handleOAuth2Login('wechat')">
              <span class="icon">💬</span>
            </button>
            <button class="oauth-btn qq" @click="handleOAuth2Login('qq')">
              <span class="icon">🐧</span>
            </button>
            <button class="oauth-btn weibo" @click="handleOAuth2Login('weibo')">
              <span class="icon">📱</span>
            </button>
          </div>
        </div>
        <div class="register-link">
          还没有账号？<a @click="goToRegister">立即注册</a>
        </div>
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
  position: relative;
  overflow: hidden;
  background: linear-gradient(135deg, #0f0f0f 0%, #1a1a2e 50%, #16213e 100%);
}

.dynamic-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  overflow: hidden;
  z-index: 0;
}

.bg-gradient {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.3;
  animation: float 20s ease-in-out infinite;
}

.bg-1 {
  width: 600px;
  height: 600px;
  background: linear-gradient(135deg, #ff4757 0%, #ff6b81 100%);
  top: -200px;
  left: -200px;
  animation-delay: 0s;
}

.bg-2 {
  width: 500px;
  height: 500px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  bottom: -150px;
  right: -150px;
  animation-delay: -7s;
}

.bg-3 {
  width: 400px;
  height: 400px;
  background: linear-gradient(135deg, #00cec9 0%, #0984e3 100%);
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  animation-delay: -14s;
}

@keyframes float {
  0%, 100% {
    transform: translate(0, 0) scale(1);
  }
  25% {
    transform: translate(30px, -30px) scale(1.1);
  }
  50% {
    transform: translate(-20px, 20px) scale(0.9);
  }
  75% {
    transform: translate(20px, 30px) scale(1.05);
  }
}

.bg-particles {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
}

.particle {
  position: absolute;
  width: 4px;
  height: 4px;
  background: rgba(255, 255, 255, 0.3);
  border-radius: 50%;
  bottom: -10px;
  animation: rise linear infinite;
}

@keyframes rise {
  0% {
    transform: translateY(0) scale(0);
    opacity: 0;
  }
  10% {
    opacity: 1;
    transform: translateY(-10vh) scale(1);
  }
  90% {
    opacity: 1;
  }
  100% {
    transform: translateY(-100vh) scale(0);
    opacity: 0;
  }
}

.login-wrapper {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 440px;
  padding: 20px;
}

.login-card {
  background: rgba(255, 255, 255, 0.08);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 16px;
  padding: 32px;
  box-shadow: 0 25px 50px rgba(0, 0, 0, 0.3);
}

.login-header {
  text-align: center;
  margin-bottom: 24px;
}

.logo-icon {
  font-size: 40px;
  margin-bottom: 12px;
  animation: bounce 2s ease-in-out infinite;
}

@keyframes bounce {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-5px);
  }
}

.login-header h2 {
  font-size: 24px;
  font-weight: 700;
  color: #fff;
  margin: 0 0 6px 0;
  letter-spacing: 2px;
}

.subtitle {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.6);
  margin: 0;
}

.login-tabs {
  display: flex;
  margin-bottom: 24px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 10px;
  padding: 3px;
}

.login-tabs .tab {
  flex: 1;
  text-align: center;
  padding: 10px 0;
  cursor: pointer;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.6);
  border-radius: 8px;
  transition: all 0.3s ease;
}

.login-tabs .tab:hover {
  color: rgba(255, 255, 255, 0.9);
}

.login-tabs .tab.active {
  background: linear-gradient(135deg, #ff4757 0%, #ff6b81 100%);
  color: #fff;
  box-shadow: 0 4px 15px rgba(255, 71, 87, 0.3);
}

.form-item {
  margin-bottom: 18px;
}

.form-item label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
  color: rgba(255, 255, 255, 0.9);
  font-size: 13px;
}

.form-item input {
  width: 100%;
  padding: 12px 14px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 10px;
  font-size: 14px;
  color: #fff;
  transition: all 0.3s ease;
  box-sizing: border-box;
}

.form-item input::placeholder {
  color: rgba(255, 255, 255, 0.4);
}

.form-item input:focus {
  outline: none;
  border-color: #ff4757;
  background: rgba(255, 71, 87, 0.1);
  box-shadow: 0 0 0 3px rgba(255, 71, 87, 0.2);
}

.code-item {
  display: flex;
  gap: 10px;
  align-items: flex-end;
}

.code-item input {
  flex: 1;
}

.code-button {
  padding: 12px 16px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 10px;
  font-size: 13px;
  cursor: pointer;
  white-space: nowrap;
  font-weight: 500;
  transition: all 0.3s ease;
}

.code-button:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
}

.code-button:disabled {
  background: rgba(255, 255, 255, 0.1);
  color: rgba(255, 255, 255, 0.5);
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

.login-button {
  width: 100%;
  padding: 12px;
  background: linear-gradient(135deg, #ff4757 0%, #ff6b81 100%);
  color: white;
  border: none;
  border-radius: 10px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  margin-top: 8px;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(255, 71, 87, 0.3);
}

.login-button:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(255, 71, 87, 0.4);
}

.login-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

.oauth-login {
  margin-top: 24px;
}

.divider {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
}

.divider::before,
.divider::after {
  content: '';
  flex: 1;
  height: 1px;
  background: rgba(255, 255, 255, 0.1);
}

.divider span {
  padding: 0 12px;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.5);
}

.oauth-buttons {
  display: flex;
  justify-content: center;
  gap: 16px;
}

.oauth-btn {
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.05);
  cursor: pointer;
  transition: all 0.3s ease;
}

.oauth-btn .icon {
  font-size: 20px;
}

.oauth-btn.github:hover {
  border-color: #333;
  background: rgba(51, 51, 51, 0.1);
  transform: translateY(-3px);
}

.oauth-btn.wechat:hover {
  border-color: #07c160;
  background: rgba(7, 193, 96, 0.1);
  transform: translateY(-3px);
}

.oauth-btn.qq:hover {
  border-color: #12b7f5;
  background: rgba(18, 183, 245, 0.1);
  transform: translateY(-3px);
}

.oauth-btn.weibo:hover {
  border-color: #e6162d;
  background: rgba(230, 22, 45, 0.1);
  transform: translateY(-3px);
}

.register-link {
  margin-top: 20px;
  text-align: center;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.6);
}

.register-link a {
  color: #ff4757;
  cursor: pointer;
  font-weight: 500;
  transition: color 0.3s ease;
}

.register-link a:hover {
  color: #ff6b81;
  text-decoration: underline;
}

@media (max-width: 500px) {
  .login-wrapper {
    padding: 10px;
  }
  
  .login-card {
    padding: 30px 20px;
  }
  
  .login-header h2 {
    font-size: 24px;
  }
}
</style>
