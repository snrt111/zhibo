<template>
  <div class="gift-icon" :class="[size, { 'with-glow': glow }]">
    <img v-if="icon" :src="icon" :alt="name" class="gift-image" />
    <svg v-else-if="name === '鲜花' || name === 'flower'" viewBox="0 0 64 64" fill="none" xmlns="http://www.w3.org/2000/svg">
      <defs>
        <linearGradient id="flowerGrad" x1="0%" y1="0%" x2="100%" y2="100%">
          <stop offset="0%" style="stop-color:#FF6B9D"/>
          <stop offset="100%" style="stop-color:#FF1744"/>
        </linearGradient>
        <linearGradient id="stemGrad" x1="0%" y1="0%" x2="0%" y2="100%">
          <stop offset="0%" style="stop-color:#4CAF50"/>
          <stop offset="100%" style="stop-color:#2E7D32"/>
        </linearGradient>
      </defs>
      <ellipse cx="22" cy="20" rx="12" ry="10" fill="url(#flowerGrad)" opacity="0.9"/>
      <ellipse cx="42" cy="20" rx="12" ry="10" fill="url(#flowerGrad)" opacity="0.9"/>
      <ellipse cx="32" cy="14" rx="12" ry="10" fill="url(#flowerGrad)" opacity="0.9"/>
      <ellipse cx="26" cy="30" rx="12" ry="10" fill="url(#flowerGrad)" opacity="0.9"/>
      <ellipse cx="38" cy="30" rx="12" ry="10" fill="url(#flowerGrad)" opacity="0.9"/>
      <circle cx="32" cy="24" r="8" fill="#FFEB3B"/>
      <path d="M32 32 Q32 48 32 58" stroke="url(#stemGrad)" stroke-width="4" fill="none" stroke-linecap="round"/>
      <path d="M26 44 Q20 40 18 46" stroke="url(#stemGrad)" stroke-width="3" fill="none" stroke-linecap="round"/>
      <ellipse cx="16" cy="48" rx="6" ry="4" fill="#4CAF50" opacity="0.8"/>
      <path d="M38 44 Q44 40 46 46" stroke="url(#stemGrad)" stroke-width="3" fill="none" stroke-linecap="round"/>
      <ellipse cx="48" cy="48" rx="6" ry="4" fill="#4CAF50" opacity="0.8"/>
    </svg>

    <svg v-else-if="name === '掌声' || name === 'clap'" viewBox="0 0 64 64" fill="none" xmlns="http://www.w3.org/2000/svg">
      <defs>
        <linearGradient id="handGrad" x1="0%" y1="0%" x2="100%" y2="100%">
          <stop offset="0%" style="stop-color:#FFCCBC"/>
          <stop offset="100%" style="stop-color:#FF8A65"/>
        </linearGradient>
      </defs>
      <path d="M18 50 Q14 40 16 30 Q18 22 24 20 Q26 24 24 30 Q22 38 20 46 Q18 50 18 50" fill="url(#handGrad)" stroke="#E64A19" stroke-width="1"/>
      <path d="M24 48 Q20 38 22 28 Q24 20 30 18 Q32 22 30 28 Q28 36 26 44 Q24 48 24 48" fill="url(#handGrad)" stroke="#E64A19" stroke-width="1"/>
      <path d="M30 46 Q26 36 28 26 Q30 18 36 16 Q38 20 36 26 Q34 34 32 42 Q30 46 30 46" fill="url(#handGrad)" stroke="#E64A19" stroke-width="1"/>
      <path d="M36 44 Q32 34 34 24 Q36 16 42 14 Q44 18 42 24 Q40 32 38 40 Q36 44 36 44" fill="url(#handGrad)" stroke="#E64A19" stroke-width="1"/>
      <path d="M42 42 Q38 32 40 22 Q42 14 48 12 Q50 16 48 22 Q46 30 44 38 Q42 42 42 42" fill="url(#handGrad)" stroke="#E64A19" stroke-width="1"/>
      <ellipse cx="32" cy="54" rx="14" ry="6" fill="url(#handGrad)" stroke="#E64A19" stroke-width="1"/>
      <path d="M10 20 L14 16 M12 24 L16 20" stroke="#FF5722" stroke-width="2" stroke-linecap="round"/>
      <path d="M50 20 L54 16 M52 24 L56 20" stroke="#FF5722" stroke-width="2" stroke-linecap="round"/>
      <path d="M8 30 L12 26 M52 30 L56 26" stroke="#FF9800" stroke-width="2" stroke-linecap="round"/>
    </svg>

    <svg v-else-if="name === '火箭' || name === 'rocket'" viewBox="0 0 64 64" fill="none" xmlns="http://www.w3.org/2000/svg">
      <defs>
        <linearGradient id="rocketBodyGrad" x1="0%" y1="0%" x2="100%" y2="100%">
          <stop offset="0%" style="stop-color:#E3F2FD"/>
          <stop offset="100%" style="stop-color:#64B5F6"/>
        </linearGradient>
        <linearGradient id="flameGrad" x1="0%" y1="0%" x2="0%" y2="100%">
          <stop offset="0%" style="stop-color:#FF6D00"/>
          <stop offset="50%" style="stop-color:#FFAB40"/>
          <stop offset="100%" style="stop-color:#FFEB3B"/>
        </linearGradient>
      </defs>
      <path d="M32 4 L24 20 L20 50 L32 56 L44 50 L40 20 Z" fill="url(#rocketBodyGrad)" stroke="#1976D2" stroke-width="1.5"/>
      <ellipse cx="32" cy="16" rx="6" ry="4" fill="#1976D2"/>
      <ellipse cx="32" cy="16" rx="4" ry="2.5" fill="#4FC3F7"/>
      <path d="M20 50 L14 58 L18 48 Z" fill="#FF5722" stroke="#E64A19" stroke-width="1"/>
      <path d="M44 50 L50 58 L46 48 Z" fill="#FF5722" stroke="#E64A19" stroke-width="1"/>
      <path d="M26 52 L24 60 L30 54 Z" fill="url(#flameGrad)"/>
      <path d="M32 54 L32 62 L36 54 Z" fill="url(#flameGrad)"/>
      <path d="M38 52 L40 60 L34 54 Z" fill="url(#flameGrad)"/>
      <circle cx="20" cy="30" r="3" fill="#FF5722"/>
      <circle cx="44" cy="30" r="3" fill="#FF5722"/>
      <path d="M8 12 L12 8 M10 16 L14 12" stroke="#9E9E9E" stroke-width="1.5" stroke-linecap="round" opacity="0.6"/>
      <path d="M52 12 L56 8 M54 16 L58 12" stroke="#9E9E9E" stroke-width="1.5" stroke-linecap="round" opacity="0.6"/>
      <path d="M4 24 L8 20 M6 28 L10 24" stroke="#BDBDBD" stroke-width="1" stroke-linecap="round" opacity="0.4"/>
      <path d="M56 24 L60 20 M58 28 L62 24" stroke="#BDBDBD" stroke-width="1" stroke-linecap="round" opacity="0.4"/>
    </svg>

    <svg v-else-if="name === '跑车' || name === 'car'" viewBox="0 0 64 64" fill="none" xmlns="http://www.w3.org/2000/svg">
      <defs>
        <linearGradient id="carBodyGrad" x1="0%" y1="0%" x2="100%" y2="100%">
          <stop offset="0%" style="stop-color:#EF5350"/>
          <stop offset="100%" style="stop-color:#C62828"/>
        </linearGradient>
        <linearGradient id="wheelGrad" x1="0%" y1="0%" x2="100%" y2="100%">
          <stop offset="0%" style="stop-color:#424242"/>
          <stop offset="100%" style="stop-color:#212121"/>
        </linearGradient>
      </defs>
      <path d="M8 40 Q8 32 16 28 L20 24 Q28 20 40 20 L52 24 Q60 28 60 36 L60 44 Q60 48 56 48 L52 48 L8 48 Q4 48 4 44 L8 40" fill="url(#carBodyGrad)" stroke="#B71C1C" stroke-width="1"/>
      <path d="M20 28 L18 36 L40 36 L38 28 Q30 24 20 28" fill="#1565C0" opacity="0.6"/>
      <path d="M40 28 L44 28 L46 36 L40 36 Z" fill="#1565C0" opacity="0.6"/>
      <circle cx="18" cy="48" r="8" fill="url(#wheelGrad)"/>
      <circle cx="18" cy="48" r="4" fill="#757575"/>
      <circle cx="18" cy="48" r="2" fill="#BDBDBD"/>
      <circle cx="48" cy="48" r="8" fill="url(#wheelGrad)"/>
      <circle cx="48" cy="48" r="4" fill="#757575"/>
      <circle cx="48" cy="48" r="2" fill="#BDBDBD"/>
      <path d="M4 44 L8 40 L8 48 L4 44 Z" fill="#212121"/>
      <path d="M56 48 L60 44 L60 36 L56 48 Z" fill="#FF5722"/>
      <rect x="52" y="32" width="6" height="4" rx="1" fill="#FFEB3B"/>
      <ellipse cx="12" cy="36" rx="3" ry="2" fill="#90CAF9"/>
      <path d="M28 42 L32 40 L36 42 L32 44 Z" fill="#FFEB3B"/>
    </svg>

    <svg v-else-if="name === '城堡' || name === 'castle'" viewBox="0 0 64 64" fill="none" xmlns="http://www.w3.org/2000/svg">
      <defs>
        <linearGradient id="castleGrad" x1="0%" y1="0%" x2="0%" y2="100%">
          <stop offset="0%" style="stop-color:#FFD54F"/>
          <stop offset="100%" style="stop-color:#FF8F00"/>
        </linearGradient>
        <linearGradient id="roofGrad" x1="0%" y1="0%" x2="0%" y2="100%">
          <stop offset="0%" style="stop-color:#D32F2F"/>
          <stop offset="100%" style="stop-color:#B71C1C"/>
        </linearGradient>
      </defs>
      <rect x="12" y="28" width="40" height="32" fill="url(#castleGrad)" stroke="#E65100" stroke-width="1"/>
      <rect x="8" y="20" width="12" height="40" fill="url(#castleGrad)" stroke="#E65100" stroke-width="1"/>
      <rect x="44" y="20" width="12" height="40" fill="url(#castleGrad)" stroke="#E65100" stroke-width="1"/>
      <rect x="24" y="12" width="16" height="48" fill="url(#castleGrad)" stroke="#E65100" stroke-width="1"/>
      <path d="M8 20 L14 8 L20 20 Z" fill="url(#roofGrad)"/>
      <path d="M44 20 L50 8 L56 20 Z" fill="url(#roofGrad)"/>
      <path d="M24 12 L32 0 L40 12 Z" fill="url(#roofGrad)"/>
      <rect x="28" y="24" width="8" height="12" rx="2" fill="#4FC3F7" stroke="#0288D1" stroke-width="1"/>
      <rect x="10" y="32" width="8" height="10" rx="1" fill="#4FC3F7" stroke="#0288D1" stroke-width="1"/>
      <rect x="46" y="32" width="8" height="10" rx="1" fill="#4FC3F7" stroke="#0288D1" stroke-width="1"/>
      <path d="M28 52 Q32 44 36 52 L28 52" fill="#5D4037" stroke="#3E2723" stroke-width="1"/>
      <rect x="30" y="48" width="4" height="4" fill="#FFEB3B"/>
      <circle cx="14" cy="50" r="2" fill="#5D4037"/>
      <circle cx="50" cy="50" r="2" fill="#5D4037"/>
      <rect x="16" y="48" width="4" height="4" fill="#8D6E63"/>
      <rect x="44" y="48" width="4" height="4" fill="#8D6E63"/>
      <path d="M4 56 L60 56" stroke="#795548" stroke-width="3" stroke-linecap="round"/>
    </svg>

    <svg v-else viewBox="0 0 64 64" fill="none" xmlns="http://www.w3.org/2000/svg">
      <defs>
        <linearGradient id="defaultGiftGrad" x1="0%" y1="0%" x2="100%" y2="100%">
          <stop offset="0%" style="stop-color:#FF5722"/>
          <stop offset="100%" style="stop-color:#E64A19"/>
        </linearGradient>
        <linearGradient id="ribbonGrad" x1="0%" y1="0%" x2="100%" y2="100%">
          <stop offset="0%" style="stop-color:#FFEB3B"/>
          <stop offset="100%" style="stop-color:#FFC107"/>
        </linearGradient>
      </defs>
      <rect x="12" y="24" width="40" height="32" rx="2" fill="url(#defaultGiftGrad)" stroke="#BF360C" stroke-width="1.5"/>
      <rect x="30" y="24" width="4" height="32" fill="url(#ribbonGrad)"/>
      <rect x="12" y="36" width="40" height="4" fill="url(#ribbonGrad)"/>
      <path d="M28 12 Q32 4 36 12 L36 24 L28 24 Z" fill="#F44336"/>
      <circle cx="32" cy="16" r="4" fill="#FFEB3B"/>
      <path d="M16 20 L20 16 L24 20" stroke="#FF5722" stroke-width="2" fill="none" stroke-linecap="round"/>
      <path d="M40 20 L44 16 L48 20" stroke="#FF5722" stroke-width="2" fill="none" stroke-linecap="round"/>
    </svg>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';

const props = defineProps<{
  name: string;
  icon?: string;
  size?: 'small' | 'medium' | 'large';
  glow?: boolean;
}>();

const size = computed(() => props.size || 'medium');
</script>

<style scoped>
.gift-icon {
  display: flex;
  align-items: center;
  justify-content: center;
}

.gift-icon.small {
  width: 32px;
  height: 32px;
}

.gift-icon.medium {
  width: 48px;
  height: 48px;
}

.gift-icon.large {
  width: 64px;
  height: 64px;
}

.gift-icon svg {
  width: 100%;
  height: 100%;
}

.gift-icon.with-glow {
  filter: drop-shadow(0 0 8px rgba(255, 193, 7, 0.6));
}

.gift-icon.with-glow svg {
  animation: pulse 1.5s ease-in-out infinite;
}

.gift-image {
  width: 100%;
  height: 100%;
  object-fit: contain;
  border-radius: 8px;
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.05);
  }
}
</style>
