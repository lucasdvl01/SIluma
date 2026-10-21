<template>
  <button
    type="button"
    class="theme-toggle inline-flex h-10 w-10 shrink-0 items-center justify-center rounded-xl border border-slate-200 bg-white text-slate-600 transition-colors hover:bg-slate-50 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-amber-500"
    :aria-label="themeStore.isDark ? 'Ativar modo claro' : 'Ativar modo escuro'"
    :aria-pressed="themeStore.isDark"
    :title="themeStore.isDark ? 'Ativar modo claro' : 'Ativar modo escuro'"
    @click="themeStore.toggle"
  >
    <Sun v-if="themeStore.isDark" class="h-4 w-4" />
    <Moon v-else class="h-4 w-4" />
  </button>
</template>

<script setup>
import { Moon, Sun } from 'lucide-vue-next';
import { useThemeStore } from '../stores/theme';

const themeStore = useThemeStore();
</script>
