<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { message, Modal } from 'ant-design-vue';
import { watchHistoryApi } from '../api/watchHistory';
import { handleResponseAsync, errorHandler } from '../utils/errorHandler';

const router = useRouter();
const loading = ref(false);
const historyList = ref<any[]>([]);
const pagination = ref({
  current: 1,
  pageSize: 20,
  total: 0
});

const fetchWatchHistory = async (page: number = 1) => {
  loading.value = true;
  try {
    const response = await watchHistoryApi.getUserWatchHistory(page, pagination.value.pageSize);
    await handleResponseAsync(response, (data) => {
      historyList.value = data.records || [];
      pagination.value.current = data.current || 1;
      pagination.value.total = data.total || 0;
    });
  } catch (error) {
    errorHandler.handle(error);
  } finally {
    loading.value = false;
  }
};

const handlePageChange = (page: number) => {
  fetchWatchHistory(page);
};

const goToLive = (liveId: number) => {
  router.push(`/live/${liveId}`);
};

const formatDuration = (seconds: number) => {
  if (seconds < 60) {
    return `${seconds}秒`;
  } else if (seconds < 3600) {
    return `${Math.floor(seconds / 60)}分钟`;
  } else {
    const hours = Math.floor(seconds / 3600);
    const minutes = Math.floor((seconds % 3600) / 60);
    return `${hours}小时${minutes}分钟`;
  }
};

const formatTime = (timeStr: string) => {
  if (!timeStr) return '';
  const date = new Date(timeStr);
  return date.toLocaleString('zh-CN');
};

const handleDelete = (historyId: number) => {
  Modal.confirm({
    title: '确认删除',
    content: '确定要删除这条观看记录吗？',
    onOk: async () => {
      try {
        const response = await watchHistoryApi.deleteWatchHistory(historyId);
        await handleResponseAsync(response, () => {
          message.success('删除成功');
          fetchWatchHistory(pagination.value.current);
        });
      } catch (error) {
        errorHandler.handle(error);
      }
    }
  });
};

const handleClearAll = () => {
  Modal.confirm({
    title: '确认清空',
    content: '确定要清空所有观看历史吗？此操作不可恢复。',
    onOk: async () => {
      try {
        const response = await watchHistoryApi.clearUserWatchHistory();
        await handleResponseAsync(response, () => {
          message.success('观看历史已清空');
          fetchWatchHistory(1);
        });
      } catch (error) {
        errorHandler.handle(error);
      }
    }
  });
};

onMounted(() => {
  fetchWatchHistory();
});
</script>

<template>
  <div class="watch-history-container">
    <div class="page-header">
      <h2>观看历史</h2>
      <a-button 
        v-if="historyList.length > 0"
        type="primary" 
        danger 
        @click="handleClearAll"
      >
        清空历史
      </a-button>
    </div>

    <a-spin :spinning="loading">
      <div v-if="historyList.length === 0 && !loading" class="empty-state">
        <a-empty description="暂无观看记录" />
      </div>

      <div v-else class="history-list">
        <div 
          v-for="item in historyList" 
          :key="item.id" 
          class="history-item"
          @click="goToLive(item.liveId)"
        >
          <div class="live-cover">
            <img :src="item.liveCover || '/default-cover.jpg'" :alt="item.liveTitle" />
            <div class="duration-badge">{{ formatDuration(item.watchDuration) }}</div>
          </div>
          <div class="live-info">
            <h3 class="live-title">{{ item.liveTitle }}</h3>
            <p class="anchor-name">主播：{{ item.anchorName }}</p>
            <p class="watch-time">观看时间：{{ formatTime(item.updatedAt) }}</p>
          </div>
          <div class="actions" @click.stop>
            <a-button 
              type="link" 
              danger 
              @click="handleDelete(item.id)"
            >
              删除
            </a-button>
          </div>
        </div>
      </div>

      <div v-if="historyList.length > 0" class="pagination-wrapper">
        <a-pagination
          v-model:current="pagination.current"
          :total="pagination.total"
          :pageSize="pagination.pageSize"
          @change="handlePageChange"
          show-quick-jumper
          show-total
        />
      </div>
    </a-spin>
  </div>
</template>

<style scoped>
.watch-history-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-header h2 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
}

.empty-state {
  padding: 60px 0;
  text-align: center;
}

.history-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.history-item {
  display: flex;
  align-items: center;
  padding: 16px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  cursor: pointer;
  transition: all 0.3s ease;
}

.history-item:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
  transform: translateY(-2px);
}

.live-cover {
  position: relative;
  width: 160px;
  height: 90px;
  border-radius: 8px;
  overflow: hidden;
  flex-shrink: 0;
}

.live-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.duration-badge {
  position: absolute;
  bottom: 4px;
  right: 4px;
  background: rgba(0, 0, 0, 0.7);
  color: #fff;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.live-info {
  flex: 1;
  margin-left: 16px;
  min-width: 0;
}

.live-title {
  margin: 0 0 8px 0;
  font-size: 16px;
  font-weight: 500;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.anchor-name {
  margin: 0 0 4px 0;
  color: #666;
  font-size: 14px;
}

.watch-time {
  margin: 0;
  color: #999;
  font-size: 12px;
}

.actions {
  flex-shrink: 0;
  margin-left: 16px;
}

.pagination-wrapper {
  margin-top: 24px;
  text-align: center;
}

@media (max-width: 768px) {
  .watch-history-container {
    padding: 16px;
  }

  .history-item {
    flex-direction: column;
    align-items: flex-start;
  }

  .live-cover {
    width: 100%;
    height: 180px;
  }

  .live-info {
    margin-left: 0;
    margin-top: 12px;
    width: 100%;
  }

  .actions {
    margin-left: 0;
    margin-top: 12px;
    width: 100%;
    text-align: right;
  }
}
</style>
