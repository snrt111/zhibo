<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue';
import { useRouter } from 'vue-router';
import { message, Badge, Popover, List, Avatar, Button, Empty } from 'ant-design-vue';
import { BellOutlined } from '@ant-design/icons-vue';
import FloatingToolbar from './FloatingToolbar.vue';
import { notificationApi } from '../api/notification';
import { handleResponseAsync, errorHandler } from '../utils/errorHandler';
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

const router = useRouter();

const token = ref(localStorage.getItem('token'));
const userType = ref(localStorage.getItem('userType'));
const username = ref(localStorage.getItem('username') || '');
const avatar = ref(localStorage.getItem('avatar') || '');
const showDropdown = ref(false);
const userId = ref(localStorage.getItem('userId') || '');

// 通知相关
const unreadCount = ref(0);
const notificationList = ref<any[]>([]);
const showNotificationPopover = ref(false);
const stompClient = ref<Client | null>(null);

const handleStorageChange = (e: StorageEvent) => {
  if (e.key === 'avatar') {
    avatar.value = e.newValue || '';
  }
};

const handleAvatarUpdate = () => {
  avatar.value = localStorage.getItem('avatar') || '';
};

onMounted(() => {
  window.addEventListener('storage', handleStorageChange);
  window.addEventListener('avatar-updated', handleAvatarUpdate);
});

onUnmounted(() => {
  window.removeEventListener('storage', handleStorageChange);
  window.removeEventListener('avatar-updated', handleAvatarUpdate);
});

const getAvatarSvg = () => {
  const name = username.value || 'U';
  const initial = name.charAt(0).toUpperCase();
  const svg = '<svg xmlns="http://www.w3.org/2000/svg" width="40" height="40"><rect width="40" height="40" fill="#667eea" rx="20"/><text x="50%" y="50%" font-size="18" fill="white" text-anchor="middle" dy=".35em">' + initial + '</text></svg>';
  return 'data:image/svg+xml;base64,' + btoa(svg);
};

const isLoggedIn = computed(() => !!token.value);
const isAdmin = computed(() => userType.value === '1');

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

const goToProfile = () => {
  router.push('/profile');
};

const goToWatchHistory = () => {
  router.push('/watch-history');
};

const goToSecurity = () => {
  router.push('/security');
};

const goToNotifications = () => {
  router.push('/notifications');
};

// 获取未读通知数量
const fetchUnreadCount = async () => {
  if (!isLoggedIn.value) return;
  try {
    const response = await notificationApi.getUnreadCount();
    await handleResponseAsync(response, (data) => {
      unreadCount.value = data || 0;
    });
  } catch (error) {
    console.error('获取未读通知数量失败:', error);
  }
};

// 获取通知列表
const fetchNotificationList = async () => {
  if (!isLoggedIn.value) return;
  try {
    const response = await notificationApi.getNotificationList(5);
    await handleResponseAsync(response, (data) => {
      notificationList.value = data || [];
    });
  } catch (error) {
    console.error('获取通知列表失败:', error);
  }
};

// 标记通知为已读
const handleNotificationClick = async (item: any) => {
  if (item.status === 1) {
    try {
      await notificationApi.markAsRead(item.id);
      item.status = 2;
      unreadCount.value = Math.max(0, unreadCount.value - 1);
    } catch (error) {
      console.error('标记已读失败:', error);
    }
  }
  // 跳转到直播间
  if (item.liveId) {
    router.push(`/live/${item.liveId}`);
    showNotificationPopover.value = false;
  }
};

// 查看全部通知
const viewAllNotifications = () => {
  showNotificationPopover.value = false;
  router.push('/notifications');
};

// 连接WebSocket接收实时通知
const connectWebSocket = () => {
  if (!isLoggedIn.value || !userId.value) return;

  const client = new Client({
    webSocketFactory: () => new SockJS('http://localhost:8080/ws'),
    connectHeaders: {
      Authorization: `Bearer ${token.value}`
    },
    debug: (str) => {
      console.log('STOMP: ' + str);
    },
    reconnectDelay: 5000,
    heartbeatIncoming: 4000,
    heartbeatOutgoing: 4000,
  });

  client.onConnect = () => {
    console.log('WebSocket连接成功');
    // 订阅用户通知频道
    client.subscribe(`/topic/user/${userId.value}/notifications`, (message) => {
      const notification = JSON.parse(message.body);
      console.log('收到实时通知:', notification);
      // 显示通知提示
      message.info(notification.message);
      // 刷新未读数量
      fetchUnreadCount();
      // 刷新通知列表
      fetchNotificationList();
    });
  };

  client.onDisconnect = () => {
    console.log('WebSocket连接断开');
  };

  client.activate();
  stompClient.value = client;
};

// 断开WebSocket连接
const disconnectWebSocket = () => {
  if (stompClient.value) {
    stompClient.value.deactivate();
    stompClient.value = null;
  }
};

// 格式化时间
const formatTime = (timeStr: string) => {
  if (!timeStr) return '';
  const date = new Date(timeStr);
  const now = new Date();
  const diff = now.getTime() - date.getTime();

  if (diff < 60000) return '刚刚';
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`;
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`;
  return date.toLocaleDateString('zh-CN');
};

onMounted(() => {
  window.addEventListener('storage', handleStorageChange);
  window.addEventListener('avatar-updated', handleAvatarUpdate);
  
  if (isLoggedIn.value) {
    fetchUnreadCount();
    fetchNotificationList();
    connectWebSocket();
  }
});

onUnmounted(() => {
  window.removeEventListener('storage', handleStorageChange);
  window.removeEventListener('avatar-updated', handleAvatarUpdate);
  disconnectWebSocket();
});
</script>

<template>
  <div class="main-layout">
    <div class="header">
      <div class="logo" @click="goToHome">直播平台</div>
      <div class="nav-menu">
        <router-link to="/" class="nav-item">首页</router-link>
        <template v-if="isLoggedIn">
          <router-link to="/anchor" class="nav-item">主播中心</router-link>
        </template>
        <template v-if="isLoggedIn && isAdmin">
          <router-link to="/admin" class="nav-item">管理后台</router-link>
        </template>
      </div>
      <div class="user-area">
        <template v-if="isLoggedIn">
          <!-- 通知图标 -->
          <Popover
            v-model:open="showNotificationPopover"
            placement="bottomRight"
            trigger="click"
            :overlay-style="{ width: '360px' }"
          >
            <div class="notification-icon" @click="fetchNotificationList">
              <Badge :count="unreadCount" :overflow-count="99">
                <BellOutlined style="font-size: 20px; color: white;" />
              </Badge>
            </div>
            <template #content>
              <div class="notification-popover">
                <div class="notification-popover-header">
                  <span class="notification-title">开播提醒</span>
                  <Button type="link" size="small" @click="viewAllNotifications">
                    查看全部
                  </Button>
                </div>
                <div class="notification-list">
                  <List
                    :data-source="notificationList"
                    :locale="{ emptyText: '暂无新通知' }"
                  >
                    <template #renderItem="{ item }">
                      <List.Item
                        :class="{ 'unread-item': item.status === 1 }"
                        @click="handleNotificationClick(item)"
                      >
                        <List.Item.Meta>
                          <template #avatar>
                            <Avatar :src="item.liveCover" shape="square" :size="48" />
                          </template>
                          <template #title>
                            <span :class="{ 'unread-text': item.status === 1 }">
                              {{ item.liveTitle }}
                            </span>
                          </template>
                          <template #description>
                            <span class="notification-time">{{ formatTime(item.sentAt || item.createdAt) }}</span>
                          </template>
                        </List.Item.Meta>
                      </List.Item>
                    </template>
                  </List>
                </div>
              </div>
            </template>
          </Popover>

          <div 
            class="user-menu"
            @mouseenter="showDropdown = true"
            @mouseleave="showDropdown = false"
          >
            <img :src="avatar || getAvatarSvg()" class="user-avatar" />
            <span class="username">欢迎，{{ username || '用户' }}</span>
            <div class="user-dropdown" v-show="showDropdown">
              <button class="profile-btn" @click="goToProfile">个人中心</button>
              <button class="profile-btn" @click="goToWatchHistory">观看历史</button>
              <button class="profile-btn" @click="goToNotifications">通知设置</button>
              <button class="profile-btn" @click="goToSecurity">账号安全</button>
              <button class="logout-btn" @click="handleLogout">登出</button>
            </div>
          </div>
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
    <FloatingToolbar />
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
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.3);
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
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

.notification-icon {
  padding: 8px;
  border-radius: 50%;
  cursor: pointer;
  transition: background-color 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.notification-icon:hover {
  background-color: rgba(255, 255, 255, 0.15);
}

.notification-popover {
  max-height: 400px;
  overflow-y: auto;
}

.notification-popover-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
}

.notification-title {
  font-size: 16px;
  font-weight: 500;
  color: #262626;
}

.notification-list {
  padding: 8px 0;
}

.notification-time {
  font-size: 12px;
  color: #8c8c8c;
}

.unread-item {
  background-color: #f6ffed;
}

.unread-text {
  font-weight: 500;
  color: #262626;
}

:deep(.ant-list-item) {
  cursor: pointer;
  padding: 12px 16px;
  transition: background-color 0.3s;
}

:deep(.ant-list-item:hover) {
  background-color: #f5f5f5;
}

:deep(.ant-list-item.unread-item:hover) {
  background-color: #e6f7ff;
}

.user-menu {
  position: relative;
  display: flex;
  align-items: center;
  padding: 8px 12px;
  border-radius: 6px;
  cursor: pointer;
  transition: background 0.3s ease;
}

.user-menu::after {
  content: '';
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  height: 20px;
}

.user-menu:hover {
  background-color: rgba(255, 255, 255, 0.15);
}

.user-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  margin-right: 10px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  object-fit: cover;
}

.user-menu .username {
  color: white;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
}

.user-dropdown {
  position: absolute;
  top: calc(100% + 8px);
  right: 0;
  background: white;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  padding: 8px 0;
  min-width: 120px;
  z-index: 1000;
}

.user-dropdown::before {
  content: '';
  position: absolute;
  top: -6px;
  right: 20px;
  width: 12px;
  height: 12px;
  background: white;
  transform: rotate(45deg);
  box-shadow: -2px -2px 4px rgba(0, 0, 0, 0.05);
}

.profile-btn {
  width: 100%;
  padding: 10px 16px;
  border: none;
  background: transparent;
  color: #333;
  font-size: 14px;
  text-align: left;
  cursor: pointer;
  transition: background 0.2s;
}

.profile-btn:hover {
  background: #f5f5f5;
}

.user-dropdown .logout-btn {
  width: 100%;
  padding: 10px 16px;
  border: none;
  background: transparent;
  color: #ff4d4f;
  font-size: 14px;
  text-align: left;
  cursor: pointer;
  transition: background 0.2s;
  box-shadow: none;
  border-radius: 0;
}

.user-dropdown .logout-btn:hover {
  background: #fff1f0;
  transform: none;
  box-shadow: none;
}

.username {
  color: white;
  font-size: 16px;
  font-weight: 500;
  margin-right: 8px;
}

.login-btn, .register-btn {
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
  background: linear-gradient(135deg, #e94560 0%, #ff6b6b 100%);
  color: white;
}

.register-btn:hover {
  background: linear-gradient(135deg, #ff6b6b 0%, #e94560 100%);
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(233, 69, 96, 0.3);
}

.content {
  flex: 1;
  background-color: #030303;
  padding: 0px;
  min-height: calc(100vh - 72px);
  display: flex;
  justify-content: center;
}

.content-inner {
  width: 100%;
  max-width: 1400px;
  min-height: 100%;
  background-color: #2d2a2a;
  border-radius: 0px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  padding: 2px;
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
    padding: 0px;
  }
  
  .content-inner {
    padding: 0px;
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
    display: none;
  }
  
  .content {
    padding: 0px;
  }
  
  .content-inner {
    padding: 0px;
  }
  
  .content-inner:has(> div.admin-dashboard-container),
  .content-inner:has(> div.anchor-dashboard-container),
  .content-inner:has(> div.live-list-container) {
    padding: 0;
  }
}
</style>
