<template>
  <div class="image-uploader">
    <input
      ref="fileInput"
      type="file"
      accept="image/*"
      class="file-input"
      @change="handleFileChange"
    />
    <div v-if="!imageUrl" class="upload-area" @click="triggerFileInput" @dragover.prevent @drop.prevent="handleDrop">
      <div class="upload-placeholder">
        <div class="upload-icon">📷</div>
        <div class="upload-text">点击或拖拽上传图片</div>
        <div class="upload-tip">支持 JPG、PNG、GIF、WebP 格式，最大 10MB</div>
      </div>
    </div>
    <div v-else class="preview-area">
      <img :src="imageUrl" alt="预览" class="preview-image" />
      <div class="preview-actions">
        <button class="change-btn" @click="triggerFileInput">更换图片</button>
        <button class="delete-btn" @click="clearImage">删除</button>
      </div>
    </div>
    <div v-if="uploading" class="uploading-mask">
      <div class="uploading-spinner"></div>
      <div class="uploading-text">上传中...</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue';
import { message } from 'ant-design-vue';
import { compressImage, compressGiftImage } from '../../utils/imageCompress';

const props = defineProps<{
  modelValue?: string;
  directory?: string;
  type?: 'default' | 'avatar' | 'gift';
}>();

const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void;
  (e: 'change', value: string): void;
}>();

const fileInput = ref<HTMLInputElement | null>(null);
const imageUrl = ref(props.modelValue || '');
const uploading = ref(false);

watch(() => props.modelValue, (newVal) => {
  if (newVal !== undefined) {
    // 直接使用MinIO返回的URL，不做任何转换
    imageUrl.value = newVal;
  }
});

const triggerFileInput = () => {
  fileInput.value?.click();
};

const handleFileChange = (e: Event) => {
  const target = e.target as HTMLInputElement;
  const file = target.files?.[0];
  if (file) {
    processFile(file);
  }
};

const handleDrop = (e: DragEvent) => {
  const file = e.dataTransfer?.files?.[0];
  if (file) {
    processFile(file);
  }
};

const processFile = async (file: File) => {
  if (!file.type.startsWith('image/')) {
    message.error('请上传图片文件');
    return;
  }
  if (file.size > 10 * 1024 * 1024) {
    message.error('图片大小不能超过 10MB');
    return;
  }

  uploading.value = true;
  try {
    let compressedBlob: Blob;
    if (props.type === 'gift') {
      compressedBlob = await compressGiftImage(file);
    } else {
      compressedBlob = await compressImage(file, {
        maxWidth: 1200,
        maxHeight: 1200,
        quality: 0.6,
        maxSize: 200 * 1024
      });
    }

    const fileName = file.name.replace(/\.[^/.]+$/, '') + '.jpg';
    const formData = new FormData();
    formData.append('file', compressedBlob, fileName);

    const response = await fetch('/api/upload/image', {
      method: 'POST',
      headers: {
        'Authorization': 'Bearer ' + (localStorage.getItem('token') || '')
      },
      body: formData
    });

    const result = await response.json();
    if (result.code === 200) {
      const url = result.data.url;
      // 直接使用MinIO返回的完整URL
      console.log('Image uploaded successfully, MinIO URL:', url);
      imageUrl.value = url;
      emit('update:modelValue', url);
      emit('change', url);
      message.success('图片上传成功');
    } else {
      message.error(result.message || '图片上传失败');
    }
  } catch (error) {
    console.error('Upload error:', error);
    message.error('图片上传失败');
  } finally {
    uploading.value = false;
  }
};

const clearImage = () => {
  imageUrl.value = '';
  emit('update:modelValue', '');
  emit('change', '');
  if (fileInput.value) {
    fileInput.value.value = '';
  }
};
</script>

<style scoped>
.image-uploader {
  position: relative;
  width: 100%;
}

.upload-area {
  border: 2px dashed rgba(255, 255, 255, 0.2);
  border-radius: 8px;
  padding: 24px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  background-color: rgba(255, 255, 255, 0.05);
}

.upload-area:hover {
  border-color: #ff4757;
  background-color: rgba(255, 71, 87, 0.1);
}

.file-input {
  display: none;
}

.upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.upload-icon {
  font-size: 48px;
}

.upload-text {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.9);
  font-weight: 500;
}

.upload-tip {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.5);
}

.preview-area {
  position: relative;
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 8px;
  overflow: hidden;
  background-color: rgba(255, 255, 255, 0.05);
}

.preview-image {
  width: 100%;
  max-height: 150px;
  object-fit: contain;
  display: block;
  padding: 12px;
  box-sizing: border-box;
}

.preview-actions {
  display: flex;
  gap: 12px;
  padding: 12px;
  background-color: rgba(255, 255, 255, 0.03);
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.change-btn, .delete-btn {
  flex: 1;
  padding: 6px 12px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 13px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.change-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.change-btn:hover {
  background: linear-gradient(135deg, #764ba2 0%, #667eea 100%);
  transform: translateY(-1px);
  box-shadow: 0 2px 6px rgba(102, 126, 234, 0.3);
}

.delete-btn {
  background: linear-gradient(135deg, #ff4757 0%, #ff6b81 100%);
  color: white;
}

.delete-btn:hover {
  background: linear-gradient(135deg, #ff6b81 0%, #ff4757 100%);
  transform: translateY(-1px);
  box-shadow: 0 2px 6px rgba(255, 71, 87, 0.3);
}

.uploading-mask {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(26, 26, 46, 0.9);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  z-index: 10;
}

.uploading-spinner {
  width: 32px;
  height: 32px;
  border: 3px solid rgba(255, 255, 255, 0.1);
  border-top: 3px solid #ff4757;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.uploading-text {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.8);
}
</style>
