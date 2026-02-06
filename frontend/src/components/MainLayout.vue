<script setup lang="ts">
import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import { message } from 'ant-design-vue';

const router = useRouter();

const token = ref(localStorage.getItem('token'));
const userType = ref(localStorage.getItem('userType'));
const username = ref(localStorage.getItem('username') || '');

const isLoggedIn = computed(() => !!token.value);
const isAdmin = computed(() => userType.value === '2');
const isAnchor = computed(() => userType.value === '1');

const handleLogout = () => {
  localStorage.removeItem('token');
  localStorage.removeItem('userType');
  localStorage.removeItem('username');
  token.value = null;
  userType.value = null;
  username.value = '';
  message.success('登出成功');
  router.push('/login');
};

const goToLogin = () => {
  router.push('/login');
};

const goToRegister = () => {
  router.push('/register');
};

const goToHome = () => {
  router.push('/');
};
</script>

<template>
  <div class="main-layout">
    <div class="header">
      <div class="logo" @click="goToHome">直播平台</div>
      <div class="nav-menu">
        <router-link to="/" class="nav-item">首页</router-link>
        <template v-if="isLoggedIn">
          <template v-if="isAdmin">
            <router-link to="/admin" class="nav-item">管理后台</router-link>
          </template>
          <template v-if="isAnchor || isAdmin">
            <router-link to="/anchor" class="nav-item">主播控制台</router-link>
          </template>
        </template>
      </div>
      <div class="user-area">
        <template v-if="isLoggedIn">
          <span class="username">欢迎，{{ username || '用户' }}</span>
          <button class="logout-btn" @click="handleLogout">登出</button>
        </template>
        <template v-else>
          <button class="login-btn" @click="goToLogin">登录</button>
          <button class="register-btn" @click="goToRegister">注册</button>
        </template>
      </div>
    </div>
    <div class="content">
      <div class="content-inner">
        <router-view />
      </div>
    </div>
  </div>
</template>

<style scoped>
.main-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #f0f2f5;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 72px;
  padding: 0 32px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.15);
  position: sticky;
  top: 0;
  z-index: 100;
}

.logo {
  font-size: 28px;
  font-weight: 700;
  color: white;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
}

.logo:hover {
  opacity: 0.9;
  transform: translateY(-1px);
}

.nav-menu {
  display: flex;
  align-items: center;
  gap: 16px;
}

.nav-item {
  padding: 10px 20px;
  color: white;
  text-decoration: none;
  border-radius: 6px;
  transition: all 0.3s ease;
  font-size: 16px;
  font-weight: 500;
  position: relative;
}

.nav-item:hover {
  background-color: rgba(255, 255, 255, 0.2);
  transform: translateY(-1px);
}

.nav-item.router-link-active {
  background-color: rgba(255, 255, 255, 0.3);
  font-weight: 600;
}

.nav-item.router-link-active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 80%;
  height: 2px;
  background-color: white;
  border-radius: 1px;
}

.user-area {
  display: flex;
  align-items: center;
  gap: 16px;
}

.username {
  color: white;
  font-size: 16px;
  font-weight: 500;
  margin-right: 8px;
}

.logout-btn, .login-btn, .register-btn {
  padding: 8px 20px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.logout-btn {
  background-color: rgba(255, 255, 255, 0.2);
  color: white;
}

.logout-btn:hover {
  background-color: rgba(255, 255, 255, 0.3);
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
}

.login-btn {
  background-color: transparent;
  color: white;
  border: 1px solid white;
}

.login-btn:hover {
  background-color: rgba(255, 255, 255, 0.1);
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
}

.register-btn {
  background-color: white;
  color: #667eea;
}

.register-btn:hover {
  background-color: #f0f0f0;
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
}

.content {
  flex: 1;
  background-color: #f0f2f5;
  padding: 32px;
  min-height: calc(100vh - 72px);
  display: flex;
  justify-content: center;
}

.content-inner {
  width: 100%;
  max-width: 1400px;
  min-height: 100%;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  padding: 32px;
}

/* 特殊页面样式 */
.content-inner:has(> div.admin-dashboard-container),
.content-inner:has(> div.anchor-dashboard-container),
.content-inner:has(> div.live-list-container) {
  background-color: transparent;
  border-radius: 0;
  box-shadow: none;
  padding: 0;
  margin: 0;
}

/* 响应式调整 */
@media (max-width: 1200px) {
  .header {
    padding: 0 24px;
  }
  
  .content {
    padding: 24px;
  }
  
  .content-inner {
    padding: 24px;
  }
  
  .content-inner:has(> div.admin-dashboard-container),
  .content-inner:has(> div.anchor-dashboard-container),
  .content-inner:has(> div.live-list-container) {
    padding: 0;
  }
}

@media (max-width: 992px) {
  .logo {
    font-size: 24px;
  }
  
  .nav-item {
    font-size: 14px;
    padding: 8px 16px;
  }
  
  .header {
    height: 64px;
    padding: 0 20px;
  }
  
  .content {
    padding: 20px;
  }
  
  .content-inner {
    padding: 20px;
  }
  
  .content-inner:has(> div.admin-dashboard-container),
  .content-inner:has(> div.anchor-dashboard-container),
  .content-inner:has(> div.live-list-container) {
    padding: 0;
  }
}
</style>
