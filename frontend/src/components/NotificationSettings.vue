<template>
  <div class="notification-settings-container">
    <a-card title="通知设置" :bordered="false" class="notification-card">
      <a-form :model="settings" layout="vertical">
        <a-form-item>
          <div class="setting-item">
            <div class="setting-info">
              <div class="setting-title">关注主播开播提醒</div>
              <div class="setting-desc">当您关注的主播开始直播时，会收到通知提醒</div>
            </div>
            <a-switch
              v-model:checked="settings.followLiveStart"
              :loading="loading"
              @change="handleSettingChange('followLiveStart', $event)"
            />
          </div>
        </a-form-item>

        <a-divider />

        <a-form-item>
          <div class="setting-item">
            <div class="setting-info">
              <div class="setting-title">系统通知</div>
              <div class="setting-desc">接收平台公告、活动通知等重要消息</div>
            </div>
            <a-switch
              v-model:checked="settings.systemNotification"
              :loading="loading"
              @change="handleSettingChange('systemNotification', $event)"
            />
          </div>
        </a-form-item>

        <a-divider />

        <a-form-item>
          <div class="setting-item">
            <div class="setting-info">
              <div class="setting-title">礼物通知</div>
              <div class="setting-desc">当收到礼物时，会收到通知提醒</div>
            </div>
            <a-switch
              v-model:checked="settings.giftNotification"
              :loading="loading"
              @change="handleSettingChange('giftNotification', $event)"
            />
          </div>
        </a-form-item>
      </a-form>
    </a-card>

    <!-- 开播提醒历史 -->
    <a-card title="开播提醒" :bordered="false" class="notification-history-card" style="margin-top: 20px;">
      <div class="notification-header">
        <span>共 {{ notificationList.length }} 条提醒</span>
        <a-button type="link" @click="handleMarkAllRead" :disabled="unreadCount === 0">
          全部标记为已读
        </a-button>
      </div>

      <a-list
        :data-source="notificationList"
        :loading="listLoading"
        :locale="{ emptyText: '暂无开播提醒' }"
      >
        <template #renderItem="{ item }">
          <a-list-item
            :class="{ 'unread': item.status === 1 }"
            @click="handleNotificationClick(item)"
          >
            <a-list-item-meta>
              <template #avatar>
                <a-avatar :src="item.liveCover" shape="square" size="large" />
              </template>
              <template #title>
                <span :class="{ 'unread-text': item.status === 1 }">
                  {{ item.liveTitle }}
                </span>
                <a-tag v-if="item.status === 1" color="red" size="small">未读</a-tag>
              </template>
              <template #description>
                <div class="notification-meta">
                  <span>{{ formatTime(item.sentAt || item.createdAt) }}</span>
                </div>
              </template>
            </a-list-item-meta>
            <template #actions>
              <a-button type="link" danger @click.stop="handleDeleteNotification(item.id)">
                删除
              </a-button>
            </template>
          </a-list-item>
        </template>
      </a-list>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { message } from 'ant-design-vue';
import { useRouter } from 'vue-router';
import { notificationApi } from '../api/notification';
import { handleResponseAsync, errorHandler } from '../utils/errorHandler';

const router = useRouter();

interface NotificationSetting {
  followLiveStart: boolean;
  systemNotification: boolean;
  giftNotification: boolean;
}

interface LiveStartNotification {
  id: number;
  userId: number;
  anchorId: number;
  liveId?: number;
  liveTitle: string;
  liveCover?: string;
  status: number;
  sentAt?: string;
  readAt?: string;
  createdAt: string;
}

const settings = reactive<NotificationSetting>({
  followLiveStart: true,
  systemNotification: true,
  giftNotification: true
});

const notificationList = ref<LiveStartNotification[]>([]);
const unreadCount = ref(0);
const loading = ref(false);
const listLoading = ref(false);

const fetchSettings = async () => {
  try {
    const response = await notificationApi.getUserSetting();
    await handleResponseAsync(response, (data) => {
      settings.followLiveStart = data.followLiveStart === 1;
      settings.systemNotification = data.systemNotification === 1;
      settings.giftNotification = data.giftNotification === 1;
    });
  } catch (error) {
    errorHandler.handle(error);
  }
};

const fetchNotifications = async () => {
  listLoading.value = true;
  try {
    const response = await notificationApi.getNotificationList(50);
    await handleResponseAsync(response, (data) => {
      notificationList.value = data || [];
    });
  } catch (error) {
    errorHandler.handle(error);
  } finally {
    listLoading.value = false;
  }
};

const fetchUnreadCount = async () => {
  try {
    const response = await notificationApi.getUnreadCount();
    await handleResponseAsync(response, (data) => {
      unreadCount.value = data || 0;
    });
  } catch (error) {
    errorHandler.handle(error);
  }
};

const handleSettingChange = async (key: keyof NotificationSetting, value: boolean) => {
  loading.value = true;
  try {
    const updateData: Record<string, number> = {};
    updateData[key] = value ? 1 : 0;

    const response = await notificationApi.updateUserSetting(updateData);
    await handleResponseAsync(response, () => {
      message.success('设置已更新');
    });
  } catch (error) {
    errorHandler.handle(error);
    // 恢复设置
    await fetchSettings();
  } finally {
    loading.value = false;
  }
};

const handleMarkAllRead = async () => {
  try {
    const response = await notificationApi.markAllAsRead();
    await handleResponseAsync(response, () => {
      message.success('已全部标记为已读');
      fetchNotifications();
      fetchUnreadCount();
    });
  } catch (error) {
    errorHandler.handle(error);
  }
};

const handleNotificationClick = async (item: LiveStartNotification) => {
  // 标记为已读
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
  }
};

const handleDeleteNotification = async (notificationId: number) => {
  try {
    const response = await notificationApi.deleteNotification(notificationId);
    await handleResponseAsync(response, () => {
      message.success('删除成功');
      fetchNotifications();
      fetchUnreadCount();
    });
  } catch (error) {
    errorHandler.handle(error);
  }
};

const formatTime = (timeStr: string) => {
  if (!timeStr) return '';
  const date = new Date(timeStr);
  const now = new Date();
  const diff = now.getTime() - date.getTime();

  // 小于1分钟
  if (diff < 60000) {
    return '刚刚';
  }
  // 小于1小时
  if (diff < 3600000) {
    return `${Math.floor(diff / 60000)}分钟前`;
  }
  // 小于24小时
  if (diff < 86400000) {
    return `${Math.floor(diff / 3600000)}小时前`;
  }
  // 小于7天
  if (diff < 604800000) {
    return `${Math.floor(diff / 86400000)}天前`;
  }

  return date.toLocaleDateString('zh-CN');
};

onMounted(() => {
  fetchSettings();
  fetchNotifications();
  fetchUnreadCount();
});
</script>

<style scoped>
.notification-settings-container {
  max-width: 800px;
  margin: 0 auto;
}

.notification-card,
.notification-history-card {
  background: #fff;
  border-radius: 8px;
}

.setting-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
}

.setting-info {
  flex: 1;
}

.setting-title {
  font-size: 16px;
  font-weight: 500;
  color: #262626;
  margin-bottom: 4px;
}

.setting-desc {
  font-size: 14px;
  color: #8c8c8c;
}

.notification-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding: 0 12px;
  color: #8c8c8c;
}

.unread {
  background-color: #f6ffed;
}

.unread-text {
  font-weight: 500;
  color: #262626;
}

.notification-meta {
  font-size: 12px;
  color: #8c8c8c;
}

:deep(.ant-list-item) {
  cursor: pointer;
  transition: background-color 0.3s;
}

:deep(.ant-list-item:hover) {
  background-color: #f5f5f5;
}

:deep(.ant-list-item.unread:hover) {
  background-color: #e6f7ff;
}
</style>
