<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { message } from 'ant-design-vue';
import { toolsApi } from '../../api/tools';

interface GeneratedImage {
  id: number;
  prompt: string;
  imageUrl: string;
  createdAt: string;
}

const prompt = ref('');
const isGenerating = ref(false);
const generatedImages = ref<GeneratedImage[]>([]);
const previewImage = ref<string | null>(null);
const modelConfig = ref<{ id: number; name: string; type: string } | null>(null);
const availableModels = ref<{ id: number; name: string; type: string }[]>([]);
const selectedModelId = ref<number | null>(null);

const loadModels = async () => {
  try {
    const response = await toolsApi.getImageModels();
    if (response.code === 200 && response.data) {
      availableModels.value = response.data;
      if (availableModels.value.length > 0) {
        const defaultModel = availableModels.value.find(m => m.type === 'ollama') || availableModels.value[0];
        selectedModelId.value = defaultModel.id;
        modelConfig.value = defaultModel;
      }
    }
  } catch (error) {
    console.error('Failed to load models:', error);
  }
};

const loadHistory = async () => {
  try {
    const response = await toolsApi.getImageHistory();
    if (response.code === 200 && response.data) {
      generatedImages.value = response.data;
    }
  } catch (error) {
    console.error('Failed to load history:', error);
  }
};

onMounted(() => {
  loadModels();
  loadHistory();
});

const generateImage = async () => {
  if (!prompt.value.trim()) {
    message.warning('请输入图片描述');
    return;
  }
  
  if (!selectedModelId.value) {
    message.warning('请选择AI模型');
    return;
  }

  isGenerating.value = true;
  
  try {
    const response = await toolsApi.generateImage({
      prompt: prompt.value,
      modelId: selectedModelId.value
    });
    
    if (response.code === 200 && response.data) {
      generatedImages.value.unshift(response.data);
      previewImage.value = response.data.imageUrl;
      message.success('图片生成成功');
    } else {
      message.error(response.message || '图片生成失败');
    }
  } catch (error: any) {
    console.error('Image generation failed:', error);
    message.error(error.message || '图片生成失败，请检查AI模型服务');
  } finally {
    isGenerating.value = false;
  }
};

const selectModel = (model: { id: number; name: string; type: string }) => {
  selectedModelId.value = model.id;
  modelConfig.value = model;
};

const copyUrl = (url: string) => {
  navigator.clipboard.writeText(url);
  message.success('链接已复制');
};

const showPreview = (url: string) => {
  previewImage.value = url;
};

const closePreview = () => {
  previewImage.value = null;
};

const clearPrompt = () => {
  prompt.value = '';
};
</script>

<template>
  <div class="image-generator">
    <div class="generator-section">
      <div class="model-selector">
        <label class="section-label">选择模型</label>
        <div v-if="availableModels.length === 0" class="no-models-tip">
          <div class="tip-icon">⚠️</div>
          <div class="tip-content">
            <div class="tip-title">暂无可用的AI模型</div>
            <div class="tip-desc">请联系管理员在后台管理中配置AI模型后再使用此功能</div>
          </div>
        </div>
        <div v-else class="model-list">
          <button
            v-for="model in availableModels"
            :key="model.id"
            class="model-btn"
            :class="{ active: selectedModelId === model.id }"
            @click="selectModel(model)"
          >
            <span class="model-icon">{{ model.type === 'ollama' ? '🐳' : '☁️' }}</span>
            <span class="model-name">{{ model.name }}</span>
          </button>
        </div>
      </div>
      
      <div class="prompt-section">
        <label class="section-label">图片描述</label>
        <div class="prompt-input-wrapper">
          <textarea
            v-model="prompt"
            class="prompt-input"
            placeholder="描述你想要生成的图片，例如：一只可爱的猫咪在花园里玩耍，阳光明媚，色彩鲜艳"
            rows="4"
          ></textarea>
          <button v-if="prompt" class="clear-btn" @click="clearPrompt">✕</button>
        </div>
      </div>
      
      <button
        class="generate-btn"
        :disabled="isGenerating || !prompt.trim() || availableModels.length === 0"
        @click="generateImage"
      >
        <span v-if="isGenerating" class="loading-spinner"></span>
        <span>{{ isGenerating ? '生成中...' : (availableModels.length === 0 ? '⚠️ 请先配置AI模型' : '🎨 生成图片') }}</span>
      </button>
    </div>
    
    <div v-if="generatedImages.length > 0" class="history-section">
      <div class="history-header">
        <span class="history-title">生成历史</span>
        <span class="history-count">{{ generatedImages.length }} 张</span>
      </div>
      <div class="image-grid">
        <div
          v-for="image in generatedImages"
          :key="image.id"
          class="image-card"
          @click="showPreview(image.imageUrl)"
        >
          <img :src="image.imageUrl" :alt="image.prompt" class="image-thumb" />
          <div class="image-info">
            <div class="image-prompt">{{ image.prompt }}</div>
            <div class="image-actions">
              <button class="action-btn" @click.stop="copyUrl(image.imageUrl)">📋 复制链接</button>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <div v-if="previewImage" class="preview-overlay" @click="closePreview">
      <div class="preview-container" @click.stop>
        <img :src="previewImage" class="preview-image" />
        <button class="preview-close" @click="closePreview">✕</button>
        <div class="preview-actions">
          <button class="preview-action-btn" @click="copyUrl(previewImage)">📋 复制链接</button>
          <a :href="previewImage" target="_blank" class="preview-action-btn">🗔 新窗口打开</a>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.image-generator {
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.generator-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.section-label {
  display: block;
  font-size: 13px;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
}

.model-selector {
  margin-bottom: 4px;
}

.model-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.model-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 14px;
  border: 2px solid #e8e8e8;
  background: white;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
  font-size: 13px;
}

.model-btn:hover {
  border-color: #667eea;
}

.model-btn.active {
  border-color: #667eea;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
}

.model-icon {
  font-size: 16px;
}

.model-name {
  font-weight: 500;
  color: #333;
}

.no-models-tip {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 16px;
  background: linear-gradient(135deg, #fff7e6 0%, #fff1d9 100%);
  border: 1px solid #ffd591;
  border-radius: 10px;
}

.tip-icon {
  font-size: 24px;
}

.tip-content {
  flex: 1;
}

.tip-title {
  font-size: 14px;
  font-weight: 600;
  color: #d46b08;
  margin-bottom: 4px;
}

.tip-desc {
  font-size: 12px;
  color: #d48806;
  line-height: 1.5;
}

.prompt-section {
  margin-bottom: 4px;
}

.prompt-input-wrapper {
  position: relative;
}

.prompt-input {
  width: 100%;
  padding: 12px;
  padding-right: 36px;
  border: 2px solid #e8e8e8;
  border-radius: 10px;
  font-size: 14px;
  resize: vertical;
  min-height: 100px;
  transition: all 0.2s ease;
  box-sizing: border-box;
  font-family: inherit;
}

.prompt-input:focus {
  outline: none;
  border-color: #667eea;
}

.clear-btn {
  position: absolute;
  right: 10px;
  top: 10px;
  width: 24px;
  height: 24px;
  border: none;
  background: rgba(0, 0, 0, 0.1);
  border-radius: 50%;
  cursor: pointer;
  font-size: 12px;
  color: #666;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
}

.clear-btn:hover {
  background: rgba(0, 0, 0, 0.2);
}

.generate-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 14px 24px;
  border: none;
  border-radius: 10px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.generate-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

.generate-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.loading-spinner {
  width: 18px;
  height: 18px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.history-section {
  border-top: 1px solid #f0f0f0;
  padding-top: 16px;
}

.history-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.history-title {
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

.history-count {
  font-size: 12px;
  color: #999;
}

.image-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  max-height: 300px;
  overflow-y: auto;
}

.image-card {
  border-radius: 10px;
  overflow: hidden;
  background: #f8f9fa;
  cursor: pointer;
  transition: all 0.2s ease;
}

.image-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.image-thumb {
  width: 100%;
  height: 120px;
  object-fit: cover;
}

.image-info {
  padding: 10px;
}

.image-prompt {
  font-size: 12px;
  color: #666;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  line-clamp: 2;
  margin-bottom: 8px;
}

.image-actions {
  display: flex;
  gap: 8px;
}

.action-btn {
  padding: 4px 10px;
  border: none;
  background: #e8eaed;
  border-radius: 4px;
  font-size: 11px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.action-btn:hover {
  background: #667eea;
  color: white;
}

.preview-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.8);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10000;
  padding: 20px;
}

.preview-container {
  position: relative;
  max-width: 90vw;
  max-height: 90vh;
  background: white;
  border-radius: 12px;
  overflow: hidden;
}

.preview-image {
  max-width: 100%;
  max-height: 70vh;
  display: block;
}

.preview-close {
  position: absolute;
  top: 12px;
  right: 12px;
  width: 32px;
  height: 32px;
  border: none;
  background: rgba(0, 0, 0, 0.5);
  color: white;
  border-radius: 50%;
  cursor: pointer;
  font-size: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
}

.preview-close:hover {
  background: rgba(0, 0, 0, 0.7);
}

.preview-actions {
  display: flex;
  gap: 12px;
  padding: 16px;
  justify-content: center;
}

.preview-action-btn {
  padding: 10px 20px;
  border: none;
  background: #667eea;
  color: white;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
  text-decoration: none;
  display: inline-flex;
  align-items: center;
  transition: all 0.2s ease;
}

.preview-action-btn:hover {
  background: #764ba2;
}
</style>
