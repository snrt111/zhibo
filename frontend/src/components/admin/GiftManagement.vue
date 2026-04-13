<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { message, Modal } from 'ant-design-vue';
import { giftApi } from '../../api/gift';
import { handleResponseAsync, errorHandler } from '../../utils/errorHandler';
import ImageUploader from '../common/ImageUploader.vue';

const router = useRouter();
const loading = ref(false);
const giftList = ref<any[]>([]);
const filteredGiftList = ref<any[]>([]);
const showEditModal = ref(false);
const editingGift = ref<any>(null);
const isNewGift = ref(false);
const viewMode = ref<'card' | 'list'>('card');

const pagination = ref({
  current: 1,
  pageSize: 5,
  total: 0
});

const filterKeyword = ref('');
const filterMinPrice = ref<number | null>(null);
const filterMaxPrice = ref<number | null>(null);

const applyFilters = () => {
  filteredGiftList.value = giftList.value.filter(gift => {
    if (filterKeyword.value) {
      const keyword = filterKeyword.value.toLowerCase();
      if (!gift.name.toLowerCase().includes(keyword)) {
        return false;
      }
    }
    if (filterMinPrice.value !== null && gift.price < filterMinPrice.value) {
      return false;
    }
    if (filterMaxPrice.value !== null && gift.price > filterMaxPrice.value) {
      return false;
    }
    return true;
  });
};

const clearFilters = () => {
  filterKeyword.value = '';
  filterMinPrice.value = null;
  filterMaxPrice.value = null;
  pagination.value.current = 1;
  getGiftList();
};

const formData = ref({
  name: '',
  price: 0,
  image: '',
  isHot: false
});

const getGiftList = async () => {
  loading.value = true;
  try {
    const params: any = {
      page: pagination.value.current,
      size: pagination.value.pageSize
    };
    
    if (filterKeyword.value) {
      params.keyword = filterKeyword.value;
    }
    if (filterMinPrice.value !== null) {
      params.minPrice = filterMinPrice.value;
    }
    if (filterMaxPrice.value !== null) {
      params.maxPrice = filterMaxPrice.value;
    }
    
    const response = await fetch(`/api/gift/admin/list?${new URLSearchParams(params)}`, {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    });
    const result = await response.json();
    if (result.code === 200) {
      giftList.value = result.data.records || [];
      pagination.value.total = result.data.total || 0;
      filteredGiftList.value = giftList.value;
    } else {
      message.error(result.message || '获取礼物列表失败');
    }
  } catch (error) {
    errorHandler.handle(error);
  } finally {
    loading.value = false;
  }
};

const handlePageChange = (page: number) => {
  pagination.value.current = page;
  getGiftList();
};

const handlePageSizeChange = () => {
  pagination.value.current = 1;
  getGiftList();
};

const handleAdd = () => {
  isNewGift.value = true;
  editingGift.value = null;
  formData.value = {
    name: '',
    price: 0,
    image: '',
    isHot: false
  };
  showEditModal.value = true;
};

const handleEdit = (gift: any) => {
  isNewGift.value = false;
  editingGift.value = gift;
  formData.value = {
    name: gift.name || '',
    price: gift.price || 0,
    image: gift.image || gift.icon || '',
    isHot: gift.isHot || false
  };
  showEditModal.value = true;
};

const handleDelete = async (giftId: number) => {
  Modal.confirm({
    title: '删除礼物',
    content: '确定要删除这个礼物吗？此操作不可恢复。',
    okText: '确定',
    cancelText: '取消',
    onOk: async () => {
      try {
        const response = await fetch(`/api/gift/delete/${giftId}`, {
          method: 'DELETE',
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        });
        const result = await response.json();
        if (result.code === 200) {
          message.success('删除礼物成功');
          getGiftList();
        } else {
          message.error(result.message || '删除礼物失败');
        }
      } catch (error) {
        errorHandler.handle(error);
      }
    }
  });
};

const handleSave = async () => {
  if (!formData.value.name) {
    message.error('请输入礼物名称');
    return;
  }
  if (!formData.value.price || formData.value.price <= 0) {
    message.error('请输入有效的礼物价格');
    return;
  }

  try {
    const url = isNewGift.value ? '/api/gift/create' : `/api/gift/update/${editingGift.value.id}`;

    // 使用 FormData 上传文件
    const formDataToSend = new FormData();
    formDataToSend.append('name', formData.value.name);
    formDataToSend.append('price', formData.value.price.toString());
    formDataToSend.append('isHot', formData.value.isHot ? 'true' : 'false');

    // 处理图片：
    // 1. Base64 图片需要转换为文件上传
    // 2. 本地文件需要获取文件对象上传
    // 3. 已经是文件服务 URL 的直接传递 iconUrl 参数
    if (formData.value.image && formData.value.image.startsWith('data:')) {
      // Base64 图片转换为文件
      const response = await fetch(formData.value.image);
      const blob = await response.blob();
      const file = new File([blob], 'gift-icon.png', { type: blob.type });
      formDataToSend.append('icon', file);
    } else if (formData.value.image && !formData.value.image.startsWith('http')) {
      // 本地文件路径，需要获取文件
      const fileInput = document.querySelector('input[type="file"]') as HTMLInputElement;
      if (fileInput && fileInput.files && fileInput.files[0]) {
        formDataToSend.append('icon', fileInput.files[0]);
      }
    } else if (formData.value.image && formData.value.image.startsWith('http')) {
      // 已经是文件服务 URL，传递 iconUrl 参数
      formDataToSend.append('iconUrl', formData.value.image);
    }

    const response = await fetch(url, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      },
      body: formDataToSend
    });
    const result = await response.json();
    if (result.code === 200) {
      message.success(isNewGift.value ? '添加礼物成功' : '更新礼物成功');
      showEditModal.value = false;
      getGiftList();
    } else {
      message.error(result.message || '操作失败');
    }
  } catch (error) {
    errorHandler.handle(error);
  }
};

const handleNavigate = (path: string) => {
  router.push(path);
};

const handleCloseModal = () => {
  showEditModal.value = false;
  editingGift.value = null;
};

onMounted(() => {
  getGiftList();
});
</script>

<template>
  <div class="admin-dashboard-container">
    <div class="admin-sidebar">
      <div class="sidebar-header">
        <h3>管理中心</h3>
      </div>
      <ul>
        <li @click="handleNavigate('/admin')">
          <span class="sidebar-icon">📊</span>
          <span class="sidebar-text">仪表板</span>
        </li>
        <li @click="handleNavigate('/admin/analytics')">
          <span class="sidebar-icon">📈</span>
          <span class="sidebar-text">数据分析</span>
        </li>
        <li @click="handleNavigate('/admin/monitor')">
          <span class="sidebar-icon">📊</span>
          <span class="sidebar-text">系统监控</span>
        </li>
        <li @click="handleNavigate('/admin/live')">
          <span class="sidebar-icon">🎥</span>
          <span class="sidebar-text">直播管理</span>
        </li>
        <li @click="handleNavigate('/admin/user')">
          <span class="sidebar-icon">👥</span>
          <span class="sidebar-text">用户管理</span>
        </li>
        <li class="active" @click="handleNavigate('/admin/gift')">
          <span class="sidebar-icon">🎁</span>
          <span class="sidebar-text">礼物管理</span>
        </li>
        <li @click="handleNavigate('/admin/ai-config')">
          <span class="sidebar-icon">🤖</span>
          <span class="sidebar-text">AI配置管理</span>
        </li>
        <li @click="handleNavigate('/admin/audit')">
          <span class="sidebar-icon">🔍</span>
          <span class="sidebar-text">内容审核</span>
        </li>
        <li @click="handleNavigate('/admin/report')">
          <span class="sidebar-icon">⚠️</span>
          <span class="sidebar-text">举报管理</span>
        </li>
        <li @click="handleNavigate('/admin/withdraw')">
          <span class="sidebar-icon">💰</span>
          <span class="sidebar-text">提现审核</span>
        </li>
      </ul>
    </div>
    <div class="admin-content">
      <div v-if="loading" class="loading">加载中...</div>
      <div v-else class="gift-management-content">
        <div class="content-header">
          <div class="header-left">
            <h2>礼物管理</h2>
            <p class="content-subtitle">管理平台所有礼物</p>
          </div>
          <div class="header-right">
            <div class="view-switcher">
              <button 
                :class="['view-btn', { active: viewMode === 'card' }]" 
                @click="viewMode = 'card'"
                title="卡片视图"
              >
                <svg viewBox="0 0 24 24" width="18" height="18" fill="currentColor">
                  <rect x="3" y="3" width="7" height="7" rx="1"/>
                  <rect x="14" y="3" width="7" height="7" rx="1"/>
                  <rect x="3" y="14" width="7" height="7" rx="1"/>
                  <rect x="14" y="14" width="7" height="7" rx="1"/>
                </svg>
              </button>
              <button 
                :class="['view-btn', { active: viewMode === 'list' }]" 
                @click="viewMode = 'list'"
                title="列表视图"
              >
                <svg viewBox="0 0 24 24" width="18" height="18" fill="currentColor">
                  <rect x="3" y="4" width="18" height="2" rx="1"/>
                  <rect x="3" y="11" width="18" height="2" rx="1"/>
                  <rect x="3" y="18" width="18" height="2" rx="1"/>
                </svg>
              </button>
            </div>
            <button class="add-button" @click="handleAdd">添加礼物</button>
          </div>
        </div>
        <div class="filter-bar">
          <div class="filter-item">
            <label>搜索：</label>
            <input 
              type="text" 
              v-model="filterKeyword" 
              @input="applyFilters" 
              placeholder="搜索礼物名称"
              class="filter-input"
            />
          </div>
          <div class="filter-item">
            <label>价格范围：</label>
            <input 
              type="number" 
              v-model.number="filterMinPrice" 
              @input="applyFilters" 
              placeholder="最低价"
              class="filter-input small"
            />
            <span class="range-separator">-</span>
            <input 
              type="number" 
              v-model.number="filterMaxPrice" 
              @input="applyFilters" 
              placeholder="最高价"
              class="filter-input small"
            />
          </div>
          <button @click="clearFilters" class="clear-filter-button">
            清除筛选
          </button>
        </div>
        <div v-if="viewMode === 'card'" class="gift-grid">
          <div v-for="gift in filteredGiftList" :key="gift.id" class="gift-card" :class="{ hot: gift.isHot }">
            <div v-if="gift.isHot" class="hot-badge">🔥 热门</div>
            <div class="gift-icon">
              <img v-if="gift.icon || gift.image" :src="gift.icon || gift.image" :alt="gift.name" />
              <span v-else class="gift-placeholder">🎁</span>
            </div>
            <div class="gift-info">
              <h3 class="gift-name">{{ gift.name }}</h3>
              <p class="gift-price">￥{{ gift.price }}</p>
            </div>
            <div class="gift-actions">
              <button class="edit-button" @click="handleEdit(gift)">编辑</button>
              <button class="delete-button" @click="handleDelete(gift.id)">删除</button>
            </div>
          </div>
          <div v-if="filteredGiftList.length === 0" class="empty-state">
            <p>暂无礼物数据</p>
          </div>
        </div>
        <div v-else class="gift-list-container">
          <table class="gift-table">
            <thead>
              <tr>
                <th class="th-index">序号</th>
                <th class="th-icon">图标</th>
                <th class="th-name">名称</th>
                <th class="th-price">价格</th>
                <th class="th-hot">热门</th>
                <th class="th-actions">操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(gift, index) in filteredGiftList" :key="gift.id" class="gift-row" :class="{ hot: gift.isHot }">
                <td class="td-index">{{ index + 1 }}</td>
                <td class="td-icon">
                  <div class="gift-icon-small">
                    <img v-if="gift.icon || gift.image" :src="gift.icon || gift.image" :alt="gift.name" />
                    <span v-else class="gift-placeholder-small">🎁</span>
                  </div>
                </td>
                <td class="td-name">
                  {{ gift.name }}
                  <span v-if="gift.isHot" class="hot-tag-inline">🔥</span>
                </td>
                <td class="td-price">￥{{ gift.price }}</td>
                <td class="td-hot">
                  <span v-if="gift.isHot" class="hot-status">是</span>
                  <span v-else class="normal-status">否</span>
                </td>
                <td class="td-actions">
                  <div class="actions-wrapper">
                    <button class="edit-button-small" @click="handleEdit(gift)">编辑</button>
                    <button class="delete-button-small" @click="handleDelete(gift.id)">删除</button>
                  </div>
                </td>
              </tr>
              <tr v-if="filteredGiftList.length === 0">
                <td colspan="6" class="empty-state-list">暂无礼物数据</td>
              </tr>
            </tbody>
          </table>
        </div>
        <div class="pagination-container">
          <div class="pagination-info">
            共 {{ pagination.total }} 条记录
          </div>
          <div class="pagination-controls">
            <div class="page-size-selector">
              <span class="page-size-label">每页显示：</span>
              <select 
                v-model="pagination.pageSize" 
                @change="handlePageSizeChange"
                class="page-size-select"
              >
                <option value="5">5条</option>
                <option value="10">10条</option>
                <option value="20">20条</option>
                <option value="50">50条</option>
                <option value="100">100条</option>
                <option value="200">200条</option>
              </select>
            </div>
            <a-pagination
              v-model:current="pagination.current"
              :pageSize="pagination.pageSize"
              :total="pagination.total"
              :show-size-changer="false"
              :show-quick-jumper="true"
              :simple="false"
              :show-less-items="false"
              @change="handlePageChange"
            />
          </div>
        </div>
      </div>
    </div>

    <!-- 编辑/添加弹窗 -->
    <div v-if="showEditModal" class="modal-overlay" @click="handleCloseModal">
      <div class="modal-content" @click.stop>
        <h3>{{ isNewGift ? '添加礼物' : '编辑礼物' }}</h3>
        <div class="form-item">
          <label>礼物名称</label>
          <input type="text" v-model="formData.name" placeholder="请输入礼物名称" />
        </div>
        <div class="form-item">
          <label>礼物价格</label>
          <input type="number" v-model.number="formData.price" placeholder="请输入礼物价格" min="0" step="0.01" />
        </div>
        <div class="form-item">
          <label>图标图片</label>
          <ImageUploader v-model="formData.image" type="gift" />
        </div>
        <div class="form-item">
          <label class="checkbox-label">
            <input type="checkbox" v-model="formData.isHot" />
            <span>设为热门礼物</span>
          </label>
        </div>

        <div class="modal-actions">
          <button class="cancel-button" @click="handleCloseModal">取消</button>
          <button class="confirm-button" @click="handleSave">保存</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.admin-dashboard-container {
  display: flex;
  min-height: calc(100vh - 72px);
  background: linear-gradient(180deg, #0f0f0f 0%, #1a1a1a 100%);
  margin: -32px;
  padding: 0;
  border-radius: 0;
  box-shadow: none;
}

.admin-sidebar {
  width: 240px;
  background: linear-gradient(180deg, #1a1a2e 0%, #16213e 100%);
  border-right: 1px solid rgba(255, 255, 255, 0.08);
  padding: 0;
  position: fixed;
  left: 0;
  top: 72px;
  bottom: 0;
  z-index: 100;
  overflow-y: auto;
  transition: all 0.3s ease;
}

.sidebar-header {
  padding: 24px 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.sidebar-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #fff;
}

.admin-sidebar ul {
  list-style: none;
  margin: 0;
  padding: 16px 0;
}

.admin-sidebar li {
  display: flex;
  align-items: center;
  padding: 14px 20px;
  cursor: pointer;
  color: rgba(255, 255, 255, 0.85);
  transition: all 0.3s ease;
  border-left: 3px solid transparent;
}

.admin-sidebar li:hover {
  color: #fff;
  background: rgba(255, 71, 87, 0.15);
  border-left-color: #ff4757;
}

.admin-sidebar li.active {
  color: #fff;
  background: linear-gradient(90deg, rgba(255, 71, 87, 0.2) 0%, rgba(255, 107, 129, 0.1) 100%);
  border-left-color: #ff4757;
}

.sidebar-icon {
  font-size: 18px;
  margin-right: 12px;
  width: 24px;
  text-align: center;
}

.sidebar-text {
  font-size: 14px;
  font-weight: 500;
}

.admin-content {
  flex: 1;
  margin-left: 240px;
  padding: 0;
  min-height: calc(100vh - 72px);
  overflow-y: auto;
  transition: all 0.3s ease;
}

.gift-management-content {
  width: 100%;
  min-height: 100%;
  padding: 24px;
}

.content-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.header-left h2 {
  margin: 0 0 8px 0;
  font-size: 28px;
  color: #fff;
  font-weight: 600;
}

.content-subtitle {
  margin: 0;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.8);
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.view-switcher {
  display: flex;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 8px;
  padding: 4px;
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.view-btn {
  padding: 8px 12px;
  background: transparent;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  color: rgba(255, 255, 255, 0.6);
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.view-btn:hover {
  color: rgba(255, 255, 255, 0.9);
  background: rgba(255, 255, 255, 0.05);
}

.view-btn.active {
  background: linear-gradient(135deg, #ff4757 0%, #ff6b81 100%);
  color: white;
}

.add-button {
  padding: 10px 24px;
  background: linear-gradient(135deg, #ff4757 0%, #ff6b81 100%);
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.add-button:hover {
  background: linear-gradient(135deg, #ff6b81 0%, #ff4757 100%);
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(255, 71, 87, 0.3);
}

.filter-bar {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 8px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.filter-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.filter-item label {
  color: rgba(255, 255, 255, 0.7);
  font-size: 14px;
  white-space: nowrap;
}

.filter-input {
  padding: 8px 12px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 6px;
  background: rgba(255, 255, 255, 0.05);
  color: #fff;
  font-size: 14px;
  outline: none;
  transition: all 0.3s ease;
  min-width: 180px;
}

.filter-input.small {
  min-width: 100px;
  width: 100px;
}

.filter-input:focus {
  border-color: #ff4757;
  background: rgba(255, 71, 87, 0.1);
}

.range-separator {
  color: rgba(255, 255, 255, 0.5);
}

.clear-filter-button {
  padding: 8px 16px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 6px;
  background: rgba(255, 255, 255, 0.05);
  color: rgba(255, 255, 255, 0.8);
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.clear-filter-button:hover {
  background: rgba(255, 71, 87, 0.2);
  color: #ff6b81;
  border-color: #ff4757;
}

.gift-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

.gift-card {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 12px;
  padding: 24px;
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}

.gift-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.3);
  border-color: rgba(255, 71, 87, 0.3);
}

.gift-card.hot {
  border-color: rgba(255, 107, 107, 0.5);
  background: linear-gradient(135deg, rgba(255, 107, 107, 0.1) 0%, rgba(255, 142, 83, 0.05) 100%);
  position: relative;
  overflow: hidden;
}

.gift-card.hot:hover {
  border-color: rgba(255, 107, 107, 0.8);
  box-shadow: 0 12px 40px rgba(255, 107, 107, 0.2);
}

.gift-card .hot-badge {
  position: absolute;
  top: 8px;
  right: 8px;
  background: linear-gradient(135deg, #FF6B6B 0%, #FF8E53 100%);
  color: white;
  font-size: 11px;
  font-weight: 600;
  padding: 3px 8px;
  border-radius: 12px;
}

.gift-icon {
  width: 80px;
  height: 80px;
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  overflow: hidden;
}

.gift-icon img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.gift-placeholder {
  font-size: 40px;
}

.gift-info {
  width: 100%;
}

.gift-name {
  margin: 0 0 8px 0;
  font-size: 18px;
  font-weight: 600;
  color: #fff;
}

.gift-price {
  margin: 0 0 8px 0;
  font-size: 20px;
  font-weight: 600;
  color: #ff6b81;
}

.gift-desc {
  margin: 0;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.8);
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.gift-actions {
  display: flex;
  gap: 12px;
  margin-top: 16px;
  width: 100%;
}

.edit-button, .delete-button {
  flex: 1;
  padding: 8px 16px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.edit-button {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.edit-button:hover {
  background: linear-gradient(135deg, #764ba2 0%, #667eea 100%);
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.3);
}

.delete-button {
  background: linear-gradient(135deg, #ff4757 0%, #ff6b81 100%);
  color: white;
}

.delete-button:hover {
  background: linear-gradient(135deg, #ff6b81 0%, #ff4757 100%);
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(255, 71, 87, 0.3);
}

.empty-state {
  grid-column: 1 / -1;
  display: flex;
  justify-content: center;
  align-items: center;
  height: 300px;
  color: rgba(255, 255, 255, 0.7);
  font-size: 14px;
}

.pagination-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  background: rgba(255, 255, 255, 0.03);
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  margin-top: 16px;
  border-radius: 0 0 12px 12px;
}

.pagination-info {
  color: rgba(255, 255, 255, 0.7);
  font-size: 14px;
}

.pagination-controls {
  display: flex;
  align-items: center;
  gap: 16px;
}

.page-size-selector {
  display: flex;
  align-items: center;
  gap: 8px;
}

.page-size-label {
  color: rgba(255, 255, 255, 0.7);
  font-size: 14px;
}

.page-size-select {
  padding: 6px 12px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 4px;
  color: rgba(255, 255, 255, 0.9);
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
  outline: none;
}

.page-size-select:hover {
  border-color: rgba(255, 71, 87, 0.5);
  background: rgba(255, 255, 255, 0.08);
}

.page-size-select:focus {
  border-color: #ff4757;
  box-shadow: 0 0 0 3px rgba(255, 71, 87, 0.2);
}

.page-size-select option {
  background: #1a1a2e;
  color: #fff;
}

.pagination-container :deep(.ant-pagination) {
  display: flex;
  align-items: center;
  gap: 4px;
}

.pagination-container :deep(.ant-pagination-item) {
  background: rgba(255, 255, 255, 0.05);
  border-color: rgba(255, 255, 255, 0.2);
  border-radius: 4px;
  color: rgba(255, 255, 255, 0.85);
  transition: all 0.3s ease;
  min-width: 32px;
  height: 32px;
  line-height: 32px;
}

.pagination-container :deep(.ant-pagination-item:hover) {
  background: rgba(255, 71, 87, 0.2);
  border-color: #ff4757;
  color: #fff;
}

.pagination-container :deep(.ant-pagination-item-active) {
  background: #fff;
  border-color: #fff;
  color: #ff4757;
  font-weight: 600;
}

.pagination-container :deep(.ant-pagination-prev),
.pagination-container :deep(.ant-pagination-next) {
  background: linear-gradient(135deg, rgba(255, 71, 87, 0.15) 0%, rgba(255, 107, 129, 0.15) 100%);
  border-color: rgba(255, 71, 87, 0.3);
  border-radius: 6px;
  color: rgba(255, 255, 255, 0.9);
  transition: all 0.3s ease;
  min-width: 36px;
  height: 36px;
  line-height: 36px;
  font-weight: 500;
}

.pagination-container :deep(.ant-pagination-jump-prev),
.pagination-container :deep(.ant-pagination-jump-next) {
  background: rgba(255, 255, 255, 0.05);
  border-color: rgba(255, 255, 255, 0.2);
  border-radius: 4px;
  color: rgba(255, 255, 255, 0.85);
  transition: all 0.3s ease;
  min-width: 32px;
  height: 32px;
  line-height: 32px;
}

.pagination-container :deep(.ant-pagination-prev:hover),
.pagination-container :deep(.ant-pagination-next:hover) {
  background: linear-gradient(135deg, rgba(255, 71, 87, 0.3) 0%, rgba(255, 107, 129, 0.3) 100%);
  border-color: #ff4757;
  color: #fff;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(255, 71, 87, 0.3);
}

.pagination-container :deep(.ant-pagination-jump-prev:hover),
.pagination-container :deep(.ant-pagination-jump-next:hover) {
  background: rgba(255, 71, 87, 0.2);
  border-color: #ff4757;
  color: #fff;
}

.pagination-container :deep(.ant-pagination-disabled) {
  background: rgba(255, 255, 255, 0.02);
  border-color: rgba(255, 255, 255, 0.1);
  color: rgba(255, 255, 255, 0.3);
  cursor: not-allowed;
  opacity: 0.5;
}

.pagination-container :deep(.ant-pagination-disabled:hover) {
  background: rgba(255, 255, 255, 0.02);
  border-color: rgba(255, 255, 255, 0.1);
  color: rgba(255, 255, 255, 0.3);
  transform: none;
  box-shadow: none;
}

.pagination-container :deep(.ant-pagination-options) {
  display: none;
}

.pagination-container :deep(.ant-pagination-total-text) {
  display: none;
}

.gift-list-container {
  background: rgba(255, 255, 255, 0.03);
  border-radius: 12px;
  overflow: hidden;
}

.gift-table {
  width: 100%;
  border-collapse: collapse;
}

.gift-table thead {
  background: rgba(255, 255, 255, 0.05);
}

.gift-table th {
  padding: 16px 20px;
  text-align: left;
  font-size: 13px;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.8);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.gift-table .th-index {
  width: 80px;
  text-align: center;
}

.gift-table .th-icon {
  width: 80px;
}

.gift-table .th-name {
  width: 150px;
}

.gift-table .th-price {
  width: 100px;
}

.gift-table .th-hot {
  width: 80px;
  text-align: center;
}

.gift-table .th-actions {
  width: 160px;
  text-align: center;
}

.gift-row {
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
  transition: background 0.2s ease;
}

.gift-row:hover {
  background: rgba(255, 255, 255, 0.03);
}

.gift-row.hot {
  background: linear-gradient(90deg, rgba(255, 107, 107, 0.08) 0%, transparent 100%);
}

.gift-row.hot:hover {
  background: linear-gradient(90deg, rgba(255, 107, 107, 0.15) 0%, rgba(255, 255, 255, 0.03) 100%);
}

.hot-tag-inline {
  margin-left: 6px;
  font-size: 12px;
}

.hot-status {
  color: #FF6B6B;
  font-weight: 600;
}

.normal-status {
  color: rgba(255, 255, 255, 0.4);
}

.gift-table td {
  padding: 16px 20px;
  color: rgba(255, 255, 255, 0.9);
}

.td-index {
  text-align: center;
  color: rgba(255, 255, 255, 0.5);
  font-size: 13px;
}

.td-icon {
  padding: 12px 20px;
}

.gift-icon-small {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 8px;
  overflow: hidden;
}

.gift-icon-small img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.gift-placeholder-small {
  font-size: 20px;
}

.td-name {
  font-weight: 500;
}

.td-price {
  color: #ff6b81;
  font-weight: 600;
}

.td-desc {
  color: rgba(255, 255, 255, 0.6);
  font-size: 13px;
  max-width: 300px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.td-actions {
  text-align: center;
}

.actions-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 10px;
}

.edit-button-small, .delete-button-small {
  padding: 6px 14px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 12px;
  font-weight: 500;
  transition: all 0.2s ease;
  flex-shrink: 0;
}

.edit-button-small {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.edit-button-small:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 6px rgba(102, 126, 234, 0.3);
}

.delete-button-small {
  background: linear-gradient(135deg, #ff4757 0%, #ff6b81 100%);
  color: white;
}

.delete-button-small:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 6px rgba(255, 71, 87, 0.3);
}

.empty-state-list {
  text-align: center;
  padding: 60px 20px;
  color: rgba(255, 255, 255, 0.5);
  font-size: 14px;
}

.loading {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 600px;
  font-size: 18px;
  color: rgba(255, 255, 255, 0.85);
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.7);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 12px;
  padding: 32px;
  width: 500px;
  max-width: 90%;
  box-shadow: 0 12px 48px rgba(0, 0, 0, 0.4);
}

.modal-content h3 {
  margin: 0 0 24px 0;
  font-size: 20px;
  color: #fff;
  font-weight: 600;
}

.form-item {
  margin-bottom: 20px;
}

.form-item label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
  color: rgba(255, 255, 255, 0.9);
  font-size: 14px;
}

.form-item input, .form-item textarea {
  width: 100%;
  padding: 12px 16px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 6px;
  font-size: 14px;
  box-sizing: border-box;
  transition: all 0.3s ease;
  background: rgba(255, 255, 255, 0.05);
  color: #fff;
}

.form-item input:focus, .form-item textarea:focus {
  outline: none;
  border-color: #ff4757;
  box-shadow: 0 0 0 3px rgba(255, 71, 87, 0.2);
}

.form-item textarea {
  height: 100px;
  resize: vertical;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 24px;
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  color: rgba(255, 255, 255, 0.8);
  font-size: 14px;
}

.checkbox-label input[type="checkbox"] {
  width: 18px;
  height: 18px;
  cursor: pointer;
  accent-color: #ff4757;
}

.cancel-button, .confirm-button {
  padding: 10px 24px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.cancel-button {
  background: rgba(255, 255, 255, 0.1);
  color: rgba(255, 255, 255, 0.8);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.cancel-button:hover {
  background: rgba(255, 255, 255, 0.15);
}

.confirm-button {
  background: linear-gradient(135deg, #ff4757 0%, #ff6b81 100%);
  color: white;
}

.confirm-button:hover {
  background: linear-gradient(135deg, #ff6b81 0%, #ff4757 100%);
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(255, 71, 87, 0.3);
}

@media (max-width: 768px) {
  .admin-sidebar {
    width: 200px;
  }
  
  .admin-content {
    margin-left: 200px;
    padding: 20px;
  }
  
  .gift-grid {
    grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
    gap: 16px;
    padding: 0 16px;
  }
  
  .content-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
    padding: 0 16px;
  }
  
  .content-header h2 {
    font-size: 24px;
  }
}
</style>
