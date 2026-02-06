<script setup lang="ts">
import { computed } from 'vue';
import { useRoute } from 'vue-router';
import MainLayout from './components/MainLayout.vue';

const route = useRoute();

const isAuthPage = computed(() => {
  return route.path === '/login' || route.path === '/register';
});
</script>

<template>
  <div class="app">
    <template v-if="isAuthPage">
      <router-view />
    </template>
    <template v-else>
      <MainLayout />
    </template>
  </div>
</template>

<style scoped>
.app {
  min-height: 100vh;
  width: 100%;
}
</style>
