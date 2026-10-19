<template>
  <header class="h-14 border-b border-mina-700/40 bg-mina-950/80 backdrop-blur-md px-6 flex items-center justify-between z-10 shrink-0 select-none">
    <!-- Breadcrumbs Corporativo (About Face: Orientação espacial sem ruído) -->
    <div class="flex items-center gap-2.5 text-xs">
      <div class="flex items-center gap-1.5 px-2 py-0.5 rounded-md bg-mina-900 border border-mina-700/30 text-[11px] font-mono font-medium text-slate-400">
        <span class="w-1.5 h-1.5 rounded-full bg-amber-400"></span>
        <span>CCO-MINERAÇÃO</span>
      </div>
      <span class="text-slate-600">/</span>
      <h1 class="text-slate-100 font-semibold tracking-tight text-xs sm:text-sm">{{ currentTitle }}</h1>
    </div>

    <!-- Quick Actions and Telemetry Status (RVMF: Rich Modeless Feedback) -->
    <div class="flex items-center gap-3">
      <!-- Sync Status Banner if offline queue -->
      <transition enter-active-class="transition duration-150 ease-out" enter-from-class="opacity-0 scale-95" enter-to-class="opacity-100 scale-100">
        <div v-if="offlineStore.syncMessage" class="px-3 py-1 bg-amber-500/15 border border-amber-500/30 rounded-lg text-xs text-amber-300 font-medium flex items-center gap-2">
          <RefreshCw class="w-3.5 h-3.5 animate-spin text-amber-400" />
          <span>{{ offlineStore.syncMessage }}</span>
        </div>
      </transition>

      <!-- Live Polling Indicator -->
      <div class="flex items-center gap-2 px-3 py-1.5 rounded-lg bg-mina-900 border border-mina-700/30 text-xs">
        <span class="relative flex h-2 w-2">
          <span class="animate-ping absolute inline-flex h-full w-full rounded-full bg-emerald-400 opacity-75"></span>
          <span class="relative inline-flex rounded-full h-2 w-2 bg-emerald-500"></span>
        </span>
        <span class="text-[11px] font-mono text-slate-300 tabular-nums">TELEMETRIA: 10s</span>
      </div>

      <!-- Manual Refresh Button (Direct action, interactive lift) -->
      <button
        @click="refreshData"
        :disabled="loading"
        class="flex items-center gap-2 px-3 py-1.5 rounded-lg bg-mina-900 hover:bg-mina-800 text-slate-200 text-xs font-medium border border-mina-700/40 transition-all interactive-lift disabled:opacity-50"
        title="Atualizar leituras da frota imediatamente"
      >
        <RefreshCw class="w-3.5 h-3.5 text-slate-400" :class="{ 'animate-spin': loading }" />
        <span class="hidden sm:inline">Atualizar</span>
      </button>

      <!-- Switch to Field Terminal (High visibility, ergonomic) -->
      <router-link
        to="/operador"
        class="flex items-center gap-2 px-3.5 py-1.5 rounded-lg bg-amber-400 hover:bg-amber-300 text-mina-950 text-xs font-bold transition-all interactive-lift shadow-sm"
      >
        <Smartphone class="w-3.5 h-3.5 text-mina-950" />
        <span>Terminal de Campo</span>
      </router-link>
    </div>
  </header>
</template>

<script setup>
import { computed, ref } from 'vue';
import { useRoute } from 'vue-router';
import { useTowerStore } from '../stores/towers';
import { useOfflineSyncStore } from '../stores/offlineSync';
import { RefreshCw, Smartphone } from 'lucide-vue-next';

const route = useRoute();
const towerStore = useTowerStore();
const offlineStore = useOfflineSyncStore();
const loading = ref(false);

const currentTitle = computed(() => {
  const map = {
    '/dashboard': 'Dashboard de Indicadores Operacionais',
    '/mapa': 'Mapeamento Georreferenciado da Frota',
    '/torres': 'Inventário & Telemetria de Torres',
    '/lote': 'Ações Operacionais em Lote',
    '/manutencoes': 'Ordens de Serviço & Horímetro',
    '/auditoria': 'Trilha de Auditoria & Segurança',
    '/operador': 'Terminal de Campo do Operador',
    '/simulador-iot': 'Bancada Virtual de Integração IoT',
  };
  return map[route.path] || 'Painel de Gestão';
});

const refreshData = async () => {
  loading.value = true;
  try {
    await towerStore.fetchTowers();
  } finally {
    setTimeout(() => { loading.value = false; }, 350);
  }
};
</script>
