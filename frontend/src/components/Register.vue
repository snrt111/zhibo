<script setup lang="ts">
import { ref, reactive, computed } from 'vue';
import { useRouter } from 'vue-router';
import { message } from 'ant-design-vue';
import { userApi } from '../api/user';
import { handleResponseAsync, errorHandler } from '../utils/errorHandler';

const router = useRouter();
const loading = ref(false);
const countdown = ref(0);
const activeTab = ref<'username' | 'phone' | 'email'>('username');

const formState = reactive({
  // 用户名注册
  username: '',
  password: '',
  confirmPassword: '',
  nickname: '',
  // 手机号注册
  phone: '',
  phoneCode: '',
  phonePassword: '',
  phoneConfirmPassword: '',
  phoneNickname: '',
  // 邮箱注册
  email: '',
  emailCode: '',
  emailPassword: '',
  emailConfirmPassword: '',
  emailNickname: ''
});

const validateEmail = (email: string) => {
  const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return re.test(email);
};

const validatePhone = (phone: string) => {
  const re = /^1[3-9]\d{9}$/;
  return re.test(phone);
};

// 发送验证码
const sendCode = async (type: 'phone' | 'email') => {
  const value = type === 'phone' ? formState.phone : formState.email;
  
  if (type === 'phone') {
    if (!validatePhone(value)) {
      message.error('请输入有效的手机号码');
      return;
    }
  } else {
    if (!validateEmail(value)) {
      message.error('请输入有效的邮箱地址');
      return;
    }
  }

  try {
    const response = type === 'phone' 
      ? await userApi.sendSmsCode(value)
      : await userApi.sendEmailCode(value);
    
    if (response.code === 200) {
      message.success('验证码已发送');
      countdown.value = 60;
      const timer = setInterval(() => {
        countdown.value--;
        if (countdown.value <= 0) {
          clearInterval(timer);
        }
      }, 1000);
    } else {
      message.error(response.message || '发送失败');
    }
  } catch (error) {
    errorHandler.handle(error);
  }
};

// 用户名注册
const handleUsernameRegister = async () => {
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

  loading.value = true;
  try {
    const response = await userApi.register({
      username: formState.username,
      password: formState.password,
      nickname: formState.nickname || formState.username
    });
    await handleResponseAsync(response, () => {
      message.success('注册成功，请登录');
      router.push('/login');
    });
  } catch (error) {
    errorHandler.handle(error);
  } finally {
    loading.value = false;
  }
};

// 手机号注册
const handlePhoneRegister = async () => {
  if (!formState.phone || !formState.phoneCode || !formState.phonePassword) {
    message.error('请填写完整信息');
    return;
  }

  if (!validatePhone(formState.phone)) {
    message.error('请输入有效的手机号码');
    return;
  }

  if (formState.phonePassword.length < 6) {
    message.error('密码至少6个字符');
    return;
  }

  if (formState.phonePassword !== formState.phoneConfirmPassword) {
    message.error('两次输入的密码不一致');
    return;
  }

  loading.value = true;
  try {
    const response = await userApi.registerWithPhone(
      formState.phone,
      formState.phoneCode,
      formState.phonePassword,
      formState.phoneNickname
    );
    await handleResponseAsync(response, () => {
      message.success('注册成功，请登录');
      router.push('/login');
    });
  } catch (error) {
    errorHandler.handle(error);
  } finally {
    loading.value = false;
  }
};

// 邮箱注册
const handleEmailRegister = async () => {
  if (!formState.email || !formState.emailCode || !formState.emailPassword) {
    message.error('请填写完整信息');
    return;
  }

  if (!validateEmail(formState.email)) {
    message.error('请输入有效的邮箱地址');
    return;
  }

  if (formState.emailPassword.length < 6) {
    message.error('密码至少6个字符');
    return;
  }

  if (formState.emailPassword !== formState.emailConfirmPassword) {
    message.error('两次输入的密码不一致');
    return;
  }

  loading.value = true;
  try {
    const response = await userApi.registerWithEmail(
      formState.email,
      formState.emailCode,
      formState.emailPassword,
      formState.emailNickname
    );
    await handleResponseAsync(response, () => {
      message.success('注册成功，请登录');
      router.push('/login');
    });
  } catch (error) {
    errorHandler.handle(error);
  } finally {
    loading.value = false;
  }
};

const handleSubmit = () => {
  if (activeTab.value === 'username') {
    handleUsernameRegister();
  } else if (activeTab.value === 'phone') {
    handlePhoneRegister();
  } else {
    handleEmailRegister();
  }
};

const goToLogin = () => {
  router.push('/login');
};
</script>

<template>
  <div class="register-container">
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
    <div class="register-wrapper">
      <div class="register-card">
        <div class="register-header">
          <div class="logo-icon">🎬</div>
          <h2>创建账号</h2>
          <p class="subtitle">加入直播平台，开启精彩之旅</p>
        </div>

        <!-- 注册方式切换 -->
        <div class="register-tabs">
          <div 
            class="tab-item" 
            :class="{ active: activeTab === 'username' }"
            @click="activeTab = 'username'"
          >
            用户名注册
          </div>
          <div 
            class="tab-item" 
            :class="{ active: activeTab === 'phone' }"
            @click="activeTab = 'phone'"
          >
            手机号注册
          </div>
          <div 
            class="tab-item" 
            :class="{ active: activeTab === 'email' }"
            @click="activeTab = 'email'"
          >
            邮箱注册
          </div>
        </div>

        <!-- 用户名注册表单 -->
        <div v-if="activeTab === 'username'" class="form-section">
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
              placeholder="请输入昵称（可选，默认使用用户名）"
            />
          </div>
        </div>

        <!-- 手机号注册表单 -->
        <div v-if="activeTab === 'phone'" class="form-section">
          <div class="form-item">
            <label for="phone">手机号 <span class="required">*</span></label>
            <div class="input-with-button">
              <input 
                type="tel" 
                id="phone" 
                v-model="formState.phone" 
                placeholder="请输入手机号码"
              />
              <button 
                class="code-btn" 
                @click="sendCode('phone')"
                :disabled="countdown > 0"
              >
                {{ countdown > 0 ? `${countdown}s` : '获取验证码' }}
              </button>
            </div>
          </div>
          <div class="form-item">
            <label for="phoneCode">验证码 <span class="required">*</span></label>
            <input 
              type="text" 
              id="phoneCode" 
              v-model="formState.phoneCode" 
              placeholder="请输入验证码"
            />
          </div>
          <div class="form-item">
            <label for="phonePassword">密码 <span class="required">*</span></label>
            <input 
              type="password" 
              id="phonePassword" 
              v-model="formState.phonePassword" 
              placeholder="请输入密码（至少6个字符）"
            />
          </div>
          <div class="form-item">
            <label for="phoneConfirmPassword">确认密码 <span class="required">*</span></label>
            <input 
              type="password" 
              id="phoneConfirmPassword" 
              v-model="formState.phoneConfirmPassword" 
              placeholder="请再次输入密码"
            />
          </div>
          <div class="form-item">
            <label for="phoneNickname">昵称</label>
            <input 
              type="text" 
              id="phoneNickname" 
              v-model="formState.phoneNickname" 
              placeholder="请输入昵称（可选）"
            />
          </div>
        </div>

        <!-- 邮箱注册表单 -->
        <div v-if="activeTab === 'email'" class="form-section">
          <div class="form-item">
            <label for="email">邮箱 <span class="required">*</span></label>
            <div class="input-with-button">
              <input 
                type="email" 
                id="email" 
                v-model="formState.email" 
                placeholder="请输入邮箱地址"
              />
              <button 
                class="code-btn" 
                @click="sendCode('email')"
                :disabled="countdown > 0"
              >
                {{ countdown > 0 ? `${countdown}s` : '获取验证码' }}
              </button>
            </div>
          </div>
          <div class="form-item">
            <label for="emailCode">验证码 <span class="required">*</span></label>
            <input 
              type="text" 
              id="emailCode" 
              v-model="formState.emailCode" 
              placeholder="请输入验证码"
            />
          </div>
          <div class="form-item">
            <label for="emailPassword">密码 <span class="required">*</span></label>
            <input 
              type="password" 
              id="emailPassword" 
              v-model="formState.emailPassword" 
              placeholder="请输入密码（至少6个字符）"
            />
          </div>
          <div class="form-item">
            <label for="emailConfirmPassword">确认密码 <span class="required">*</span></label>
            <input 
              type="password" 
              id="emailConfirmPassword" 
              v-model="formState.emailConfirmPassword" 
              placeholder="请再次输入密码"
            />
          </div>
          <div class="form-item">
            <label for="emailNickname">昵称</label>
            <input 
              type="text" 
              id="emailNickname" 
              v-model="formState.emailNickname" 
              placeholder="请输入昵称（可选）"
            />
          </div>
        </div>

        <button 
          class="register-button" 
          @click="handleSubmit" 
          :disabled="loading"
        >
          {{ loading ? '注册中...' : '立即注册' }}
        </button>
        <div class="login-link">
          已有账号？<a @click="goToLogin">立即登录</a>
        </div>
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
  position: relative;
  overflow: hidden;
  background: linear-gradient(135deg, #0f0f0f 0%, #1a1a2e 50%, #16213e 100%);
  padding: 20px 0;
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

.register-wrapper {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 460px;
  padding: 20px;
}

.register-card {
  background: rgba(255, 255, 255, 0.08);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 16px;
  padding: 32px;
  box-shadow: 0 25px 50px rgba(0, 0, 0, 0.3);
}

.register-header {
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

.register-header h2 {
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

/* 注册方式切换标签 */
.register-tabs {
  display: flex;
  margin-bottom: 24px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.tab-item {
  flex: 1;
  text-align: center;
  padding: 12px 0;
  color: rgba(255, 255, 255, 0.6);
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s ease;
  position: relative;
}

.tab-item:hover {
  color: rgba(255, 255, 255, 0.9);
}

.tab-item.active {
  color: #ff4757;
}

.tab-item.active::after {
  content: '';
  position: absolute;
  bottom: -1px;
  left: 20%;
  right: 20%;
  height: 2px;
  background: linear-gradient(90deg, transparent, #ff4757, transparent);
}

.form-section {
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.form-item {
  margin-bottom: 16px;
}

.form-item label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
  color: rgba(255, 255, 255, 0.9);
  font-size: 13px;
}

.form-item .required {
  color: #ff4757;
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

/* 带按钮的输入框 */
.input-with-button {
  display: flex;
  gap: 10px;
}

.input-with-button input {
  flex: 1;
}

.code-btn {
  padding: 12px 16px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 10px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  white-space: nowrap;
  min-width: 100px;
}

.code-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.code-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

.register-button {
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

.register-button:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(255, 71, 87, 0.4);
}

.register-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

.login-link {
  margin-top: 20px;
  text-align: center;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.6);
}

.login-link a {
  color: #ff4757;
  cursor: pointer;
  font-weight: 500;
  transition: color 0.3s ease;
}

.login-link a:hover {
  color: #ff6b81;
  text-decoration: underline;
}

@media (max-width: 500px) {
  .register-wrapper {
    padding: 10px;
  }
  
  .register-card {
    padding: 24px 16px;
  }
  
  .register-header h2 {
    font-size: 20px;
  }

  .tab-item {
    font-size: 13px;
    padding: 10px 0;
  }

  .code-btn {
    padding: 10px 12px;
    font-size: 12px;
    min-width: 90px;
  }
}
</style>
