<script setup lang="ts">
import { ref } from 'vue';
import ImageGenerator from './tools/ImageGenerator.vue';

const isExpanded = ref(false);
const activeTool = ref<string | null>(null);

const tools = [
  {
    id: 'image-generator',
    name: '图片生成',
    icon: '🖼️',
    component: ImageGenerator
  }
];

const toggleToolbar = () => {
  isExpanded.value = !isExpanded.value;
  if (!isExpanded.value) {
    activeTool.value = null;
  }
};

const openTool = (toolId: string) => {
  activeTool.value = activeTool.value === toolId ? null : toolId;
};

const closeTool = () => {
  activeTool.value = null;
};
</script>

<template>
  <div class="floating-toolbar">
    <div class="toolbar-container" :class="{ expanded: isExpanded }">
      <button class="toggle-btn" @click="toggleToolbar" :title="isExpanded ? '收起工具' : '工具'">
        <span class="toggle-icon">{{ isExpanded ? '✕' : '🛠️' }}</span>
      </button>
      
      <transition name="slide">
        <div v-if="isExpanded" class="tools-list">
          <div class="tools-header">
            <span class="tools-title">工具箱</span>
          </div>
          <div class="tools-grid">
            <button
              v-for="tool in tools"
              :key="tool.id"
              class="tool-btn"
              :class="{ active: activeTool === tool.id }"
              @click="openTool(tool.id)"
              :title="tool.name"
            >
              <span class="tool-icon">{{ tool.icon }}</span>
              <span class="tool-name">{{ tool.name }}</span>
            </button>
          </div>
        </div>
      </transition>
    </div>
    
    <transition name="fade">
      <div v-if="activeTool" class="tool-panel">
        <div class="tool-panel-header">
          <span class="tool-panel-title">
            {{ tools.find(t => t.id === activeTool)?.icon }}
            {{ tools.find(t => t.id === activeTool)?.name }}
          </span>
          <button class="close-btn" @click="closeTool">✕</button>
        </div>
        <div class="tool-panel-content">
          <component :is="tools.find(t => t.id === activeTool)?.component" />
        </div>
      </div>
    </transition>
  </div>
</template>

<style scoped>
.floating-toolbar {
  position: fixed;
  right: 24px;
  bottom: 24px;
  z-index: 9999;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 12px;
}

.toolbar-container {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 12px;
}

.toggle-btn {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.4);
  transition: all 0.3s ease;
}

.toggle-btn:hover {
  transform: scale(1.1);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.5);
}

.toggle-icon {
  font-size: 24px;
}

.tools-list {
  background: white;
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
  padding: 16px;
  min-width: 200px;
}

.tools-header {
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
  margin-bottom: 12px;
}

.tools-title {
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

.tools-grid {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.tool-btn {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  border: none;
  background: #f8f9fa;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s ease;
  text-align: left;
}

.tool-btn:hover {
  background: #e8eaed;
  transform: translateX(-4px);
}

.tool-btn.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.tool-icon {
  font-size: 20px;
}

.tool-name {
  font-size: 14px;
  font-weight: 500;
}

.tool-panel {
  position: fixed;
  right: 92px;
  bottom: 24px;
  width: 420px;
  max-height: 70vh;
  background: white;
  border-radius: 16px;
  box-shadow: 0 12px 48px rgba(0, 0, 0, 0.2);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.tool-panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.tool-panel-title {
  font-size: 16px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 8px;
}

.close-btn {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  border: none;
  background: rgba(255, 255, 255, 0.2);
  color: white;
  cursor: pointer;
  font-size: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
}

.close-btn:hover {
  background: rgba(255, 255, 255, 0.3);
}

.tool-panel-content {
  flex: 1;
  overflow-y: auto;
  max-height: calc(70vh - 60px);
}

.slide-enter-active,
.slide-leave-active {
  transition: all 0.3s ease;
}

.slide-enter-from,
.slide-leave-to {
  opacity: 0;
  transform: translateX(20px);
}

.fade-enter-active,
.fade-leave-active {
  transition: all 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: scale(0.95);
}

@media (max-width: 768px) {
  .floating-toolbar {
    right: 16px;
    bottom: 16px;
  }
  
  .tool-panel {
    right: 16px;
    left: 16px;
    width: auto;
    max-height: 80vh;
  }
  
  .toggle-btn {
    width: 48px;
    height: 48px;
  }
}
</style>
