<template>
  <div class="min-h-screen bg-mina-950 text-slate-100 flex">
    <!-- Exibe Sidebar e Navbar apenas se não estiver na tela de login -->
    <template v-if="!isLoginPage">
      <Sidebar />
      <div class="flex-1 flex flex-col min-w-0 h-screen overflow-hidden">
        <Navbar />
        <main class="flex-1 overflow-y-auto p-4 sm:p-6 lg:p-8">
          <router-view />
        </main>
      </div>
    </template>

    <template v-else>
      <router-view />
    </template>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import { useRoute } from 'vue-router';
import Sidebar from './components/Sidebar.vue';
import Navbar from './components/Navbar.vue';

const route = useRoute();
const isLoginPage = computed(() => route.path === '/login');
</script>
