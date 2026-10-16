<template>
  <header class="h-16 border-b border-mina-darkBorder bg-mina-900/60 backdrop-blur-md px-6 flex items-center justify-between z-10">
    <!-- Breadcrumbs (RNF-W08) -->
    <div class="flex items-center gap-2 text-xs">
      <span class="text-slate-500 font-medium">CCO Mineração</span>
      <span class="text-slate-600">/</span>
      <span class="text-amber-500 font-semibold capitalize">{{ currentTitle }}</span>
    </div>

    <!-- Quick stats and Actions -->
    <div class="flex items-center gap-4">
      <!-- Sync Status Banner if offline queue -->
      <div v-if="offlineStore.syncMessage" class="px-3 py-1 bg-amber-500/20 border border-amber-500/40 rounded-lg text-xs text-amber-300 font-semibold flex items-center gap-2 animate-pulse">
        <RefreshCw class="w-3.5 h-3.5 animate-spin" />
        {{ offlineStore.syncMessage }}
      </div>

      <!-- Live Polling Indicator -->
      <div class="flex items-center gap-2 px-3 py-1 rounded-full bg-mina-800/80 border border-mina-darkBorder text-xs text-slate-300">
        <span class="relative flex h-2 w-2">
          <span class="animate-ping absolute inline-flex h-full w-full rounded-full bg-emerald-400 opacity-75"></span>
          <span class="relative inline-flex rounded-full h-2 w-2 bg-emerald-500"></span>
        </span>
        <span class="text-[11px] font-medium text-slate-300">Monitoramento Contínuo (10s)</span>
      </div>

      <!-- Manual Refresh Button -->
      <button
        @click="refreshData"
        :disabled="loading"
        class="flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-mina-800 hover:bg-mina-700 text-slate-200 text-xs font-semibold border border-mina-darkBorder transition-all"
        title="Atualizar dados agora"
      >
        <RefreshCw class="w-3.5 h-3.5" :class="{ 'animate-spin': loading }" />
        Atualizar
      </button>

      <!-- Quick Operator Mode switch -->
      <router-link
        to="/operador"
        class="flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-gradient-to-r from-amber-500 to-amber-600 text-black text-xs font-bold shadow-md shadow-amber-500/20 hover:brightness-110 transition-all"
      >
        <Smartphone class="w-3.5 h-3.5" />
        Modo Campo
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
    '/dashboard': 'Dashboard de Indicadores',
    '/mapa': 'Mapa Georreferenciado da Frota',
    '/torres': 'Inventário & Gestão de Torres',
    '/lote': 'Comandos Operacionais em Lote',
    '/manutencoes': 'Gestão de Manutenções & Falhas',
    '/auditoria': 'Registro de Auditoria & Histórico',
    '/operador': 'Interface do Operador de Campo',
    '/simulador-iot': 'Simulador de Hardware IoT (ESP32)',
  };
  return map[route.path] || 'Painel de Gestão';
});

const refreshData = async () => {
  loading.value = true;
  try {
    await towerStore.fetchTowers();
  } finally {
    setTimeout(() => { loading.value = false; }, 400);
  }
};
</script>
