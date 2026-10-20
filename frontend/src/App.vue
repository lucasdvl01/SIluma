<template>
  <div
    class="app-shell flex bg-[#f5f7fa] text-slate-800"
    :class="isLoginPage ? 'min-h-screen' : 'h-screen h-[100dvh] overflow-hidden'"
  >
    <!-- Exibe Sidebar e Navbar apenas se não estiver na tela de login -->
    <template v-if="!isLoginPage">
      <Sidebar :mobile-open="mobileNavOpen" @close="mobileNavOpen = false" />
      <div class="flex-1 flex flex-col min-w-0 min-h-0">
        <Navbar :navigation-open="mobileNavOpen" @toggle-navigation="mobileNavOpen = !mobileNavOpen" />
        <main
          class="app-main flex-1 min-h-0 overflow-y-auto overscroll-contain p-3 sm:p-5 lg:p-8"
          :class="{ 'operator-terminal-shell': route.path === '/operador' }"
        >
          <router-view />
        </main>
      </div>
    </template>

    <template v-else>
      <main class="min-h-screen w-full flex-1">
        <router-view />
      </main>
    </template>
  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue';
import { useRoute } from 'vue-router';
import { useOfflineSyncStore } from './stores/offlineSync';
import { useThemeStore } from './stores/theme';
import Sidebar from './components/Sidebar.vue';
import Navbar from './components/Navbar.vue';

const route = useRoute();
const offlineStore = useOfflineSyncStore();
const themeStore = useThemeStore();
const mobileNavOpen = ref(false);
const isLoginPage = computed(() => route.path === '/login');

themeStore.initialize();
offlineStore.init();

watch(() => route.path, () => {
  mobileNavOpen.value = false;
});
</script>
