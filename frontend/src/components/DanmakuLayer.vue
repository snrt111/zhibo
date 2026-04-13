<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch } from 'vue';

interface DanmakuItem {
  id: number;
  content: string;
  color: string;
  username?: string;
  userId?: number;
  top: number;
  speed: number;
  fontSize: number;
}

const props = defineProps<{
  danmakuList: any[];
  visible: boolean;
}>();

const containerRef = ref<HTMLElement | null>(null);
const danmakuItems = ref<DanmakuItem[]>([]);
const processedIds = ref<Set<number>>(new Set());
const lanes = ref<boolean[]>([]);
const isInitialized = ref(false);
const initialListLength = ref(0);
const laneCount = 8;

const initLanes = () => {
  lanes.value = new Array(laneCount).fill(false);
};

const getAvailableLane = (): number => {
  const availableLanes = lanes.value
    .map((occupied, index) => ({ occupied, index }))
    .filter(item => !item.occupied)
    .map(item => item.index);
  
  if (availableLanes.length === 0) {
    return Math.floor(Math.random() * laneCount);
  }
  
  return availableLanes[Math.floor(Math.random() * availableLanes.length)];
};

const getFontSizeByLevel = (level: number): number => {
  switch (level) {
    case 0: return 14;
    case 2: return 24;
    default: return 18;
  }
};

const addDanmaku = (danmaku: any) => {
  if (!props.visible) return;
  
  const lane = getAvailableLane();
  const containerHeight = containerRef.value?.clientHeight || 400;
  const laneHeight = containerHeight / laneCount;
  
  const speed = 8 + Math.random() * 4;
  const fontSize = getFontSizeByLevel(danmaku.fontSize ?? 1);
  
  const newDanmaku: DanmakuItem = {
    id: danmaku.id ? Number(danmaku.id) : Date.now() + Math.random(),
    content: danmaku.content,
    color: danmaku.color || '#FFFFFF',
    username: danmaku.username,
    userId: danmaku.userId,
    top: lane * laneHeight + 20,
    speed,
    fontSize
  };
  
  lanes.value[lane] = true;
  
  setTimeout(() => {
    lanes.value[lane] = false;
  }, 3000);
  
  danmakuItems.value.push(newDanmaku);
  
  setTimeout(() => {
    const index = danmakuItems.value.findIndex(d => d.id === newDanmaku.id);
    if (index > -1) {
      danmakuItems.value.splice(index, 1);
    }
  }, 10000);
};

watch(() => props.danmakuList.length, (newLength, oldLength) => {
  if (!isInitialized.value) {
    isInitialized.value = true;
    initialListLength.value = newLength;
    return;
  }
  
  const actualOldLength = oldLength || initialListLength.value;
  if (newLength > actualOldLength) {
    const newItems = props.danmakuList.slice(actualOldLength);
    newItems.forEach((item, index) => {
      setTimeout(() => {
        addDanmaku(item);
      }, index * 100);
    });
  }
});

const clearDanmaku = () => {
  danmakuItems.value = [];
  processedIds.value.clear();
};

defineExpose({
  clearDanmaku
});

onMounted(() => {
  initLanes();
});

onUnmounted(() => {
  danmakuItems.value = [];
});
</script>

<template>
  <div 
    v-if="visible" 
    ref="containerRef" 
    class="danmaku-layer"
  >
    <div
      v-for="item in danmakuItems"
      :key="item.id"
      class="danmaku-item"
      :style="{
        top: item.top + 'px',
        color: item.color,
        fontSize: item.fontSize + 'px',
        animationDuration: item.speed + 's'
      }"
    >
      <span v-if="item.username" class="danmaku-username">{{ item.username }}:</span>
      <span class="danmaku-content">{{ item.content }}</span>
    </div>
  </div>
</template>

<style scoped>
.danmaku-layer {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
  overflow: hidden;
  z-index: 10;
}

.danmaku-item {
  position: absolute;
  left: 100%;
  white-space: nowrap;
  text-shadow: 
    1px 1px 2px rgba(0, 0, 0, 0.8),
    -1px -1px 2px rgba(0, 0, 0, 0.8),
    1px -1px 2px rgba(0, 0, 0, 0.8),
    -1px 1px 2px rgba(0, 0, 0, 0.8);
  font-weight: 500;
  animation: danmakuScroll linear forwards;
  will-change: left;
}

.danmaku-username {
  color: #FFD700;
  margin-right: 4px;
  font-weight: 600;
}

.danmaku-content {
  letter-spacing: 0.5px;
}

@keyframes danmakuScroll {
  0% {
    left: 100%;
  }
  100% {
    left: -20%;
  }
}
</style>
