<template>
  <header class="app-navbar relative z-30 flex h-16 shrink-0 select-none items-center justify-between gap-2 border-b border-slate-200 bg-white/95 px-3 backdrop-blur-xl sm:px-6">
    <!-- Breadcrumbs Corporativo (About Face: Orientação espacial sem ruído) -->
    <div class="flex min-w-0 items-center gap-2 text-xs sm:gap-2.5">
      <button
        type="button"
        class="inline-flex min-h-11 min-w-11 items-center justify-center rounded-lg text-slate-600 transition-colors hover:bg-slate-100 hover:text-slate-900 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-amber-500 lg:hidden"
        :aria-expanded="navigationOpen"
        aria-controls="primary-navigation"
        :aria-label="navigationOpen ? 'Fechar menu de navegação' : 'Abrir menu de navegação'"
        @click="$emit('toggle-navigation')"
      >
        <Menu v-if="!navigationOpen" class="h-5 w-5" />
        <X v-else class="h-5 w-5" />
      </button>
      <div class="hidden items-center gap-2 rounded-full border border-slate-200 bg-slate-50 px-3 py-1.5 text-[10px] font-mono font-semibold tracking-wider text-slate-600 sm:flex">
        <span class="h-1.5 w-1.5 rounded-full bg-amber-500"></span>
        <span>SI Luma <span class="text-slate-400">/</span> CCO</span>
      </div>
      <span class="hidden text-slate-300 sm:inline">/</span>
      <p class="truncate text-sm font-semibold tracking-tight text-slate-800 sm:text-base">{{ currentTitle }}</p>
    </div>

    <!-- Quick Actions and Telemetry Status (RVMF: Rich Modeless Feedback) -->
    <div class="flex shrink-0 items-center gap-1.5 sm:gap-3">
      <ThemeToggle />

      <!-- Sync Status Banner if offline queue -->
      <transition enter-active-class="transition duration-150 ease-out" enter-from-class="opacity-0 scale-95" enter-to-class="opacity-100 scale-100">
        <div v-if="offlineStore.syncMessage" role="status" aria-live="polite" class="flex max-w-40 items-center gap-2 rounded-lg border border-amber-500/30 bg-amber-500/15 px-2 py-1 text-xs font-medium text-amber-200 sm:max-w-64 sm:px-3">
          <RefreshCw v-if="offlineStore.syncing" class="w-3.5 h-3.5 animate-spin text-amber-400" />
          <CheckCircle2 v-else-if="offlineStore.pendingCount === 0" class="w-3.5 h-3.5 text-emerald-400" />
          <WifiOff v-else class="w-3.5 h-3.5 text-amber-400" />
          <span class="truncate">{{ offlineStore.syncMessage }}</span>
        </div>
      </transition>

      <div
        class="hidden items-center gap-2 rounded-lg border px-3 py-1.5 text-xs sm:flex"
        :class="offlineStore.simulationOffline ? 'border-amber-500/30 bg-amber-500/10' : offlineStore.isOnline ? 'border-emerald-500/20 bg-emerald-500/10' : 'border-rose-500/30 bg-rose-500/10'"
        role="status"
        aria-live="polite"
      >
        <span class="h-2 w-2 rounded-full" :class="offlineStore.simulationOffline ? 'bg-amber-400' : offlineStore.isOnline ? 'bg-emerald-400' : 'bg-rose-400'"></span>
        <span class="text-[11px] font-medium text-slate-200">{{ offlineStore.simulationOffline ? 'Offline simulado' : offlineStore.isOnline ? 'Rede detectada' : 'Sem rede' }}</span>
      </div>

      <!-- Manual Refresh Button (Direct action, interactive lift) -->
      <button
        @click="refreshData"
        :disabled="loading"
        class="inline-flex min-h-11 min-w-11 items-center justify-center gap-2 rounded-lg border border-slate-200 bg-slate-50 px-3 text-xs font-medium text-slate-700 transition-colors hover:bg-slate-100 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-amber-500 disabled:cursor-wait disabled:opacity-50 sm:min-h-9 sm:min-w-0"
        aria-label="Atualizar dados das torres"
        title="Atualizar dados das torres"
      >
        <RefreshCw class="h-4 w-4 text-slate-500" :class="{ 'animate-spin': loading }" />
        <span class="hidden sm:inline">Atualizar</span>
      </button>

      <!-- Switch to Field Terminal (High visibility, ergonomic) -->
      <router-link
        to="/operador"
        class="hidden min-h-9 items-center gap-2 rounded-lg bg-amber-400 px-3.5 py-1.5 text-xs font-bold text-mina-950 shadow-sm transition-colors hover:bg-amber-300 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-amber-200 sm:flex"
      >
        <Smartphone class="w-3.5 h-3.5 text-mina-950" />
        <span>Terminal de Campo</span>
      </router-link>
    </div>

    <div
      v-if="refreshError"
      role="alert"
      class="absolute right-3 top-full z-50 mt-2 flex max-w-[calc(100vw-1.5rem)] items-start gap-3 rounded-xl border border-rose-500/40 bg-rose-950 p-3 text-sm text-rose-100 shadow-elevated sm:right-6 sm:max-w-sm"
    >
      <span>{{ refreshError }}</span>
      <button
        type="button"
        aria-label="Dispensar aviso de falha ao atualizar"
        class="min-h-11 min-w-11 shrink-0 rounded-lg text-rose-200 hover:bg-rose-400/10"
        @click="refreshError = ''"
      >
        <X class="mx-auto h-4 w-4" />
      </button>
    </div>
  </header>
</template>

<script setup>
import { computed, ref } from 'vue';
import { useRoute } from 'vue-router';
import { useTowerStore } from '../stores/towers';
import { useOfflineSyncStore } from '../stores/offlineSync';
import ThemeToggle from './ThemeToggle.vue';
import { CheckCircle2, Menu, RefreshCw, Smartphone, WifiOff, X } from 'lucide-vue-next';

defineProps({
  navigationOpen: {
    type: Boolean,
    default: false
  }
});

defineEmits(['toggle-navigation']);
const route = useRoute();
const towerStore = useTowerStore();
const offlineStore = useOfflineSyncStore();
const loading = ref(false);
const refreshError = ref('');

const currentTitle = computed(() => {
  const map = {
    '/dashboard': 'Painel da frota',
    '/mapa': 'Mapa operacional',
    '/torres': 'Frota de torres',
    '/lote': 'Comandos em lote',
    '/manutencoes': 'Manutenções',
    '/auditoria': 'Auditoria',
    '/operador': 'Terminal de campo',
    '/simulador-iot': 'Simulador IoT',
  };
  return map[route.path] || 'Painel de Gestão';
});

const refreshData = async () => {
  if (loading.value) return;
  loading.value = true;
  refreshError.value = '';
  try {
    await towerStore.fetchTowers();
  } catch (error) {
    console.error('Erro ao atualizar os dados das torres:', error);
    refreshError.value = 'Não foi possível atualizar as torres. Verifique a conexão e tente novamente.';
  } finally {
    loading.value = false;
  }
};
</script>
