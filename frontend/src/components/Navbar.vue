<template>
  <header class="h-14 border-b border-[#1a2336] bg-[#090e1a]/95 backdrop-blur px-5 flex items-center justify-between z-10 shrink-0">
    <!-- Breadcrumbs Corporativo (RNF-W08) -->
    <div class="flex items-center gap-2 text-xs">
      <span class="text-slate-400 font-mono font-medium">CCO-MINERAÇÃO</span>
      <span class="text-slate-600">/</span>
      <span class="text-white font-semibold">{{ currentTitle }}</span>
    </div>

    <!-- Quick Actions and Telemetry Status -->
    <div class="flex items-center gap-3">
      <!-- Sync Status Banner if offline queue -->
      <div v-if="offlineStore.syncMessage" class="px-2.5 py-1 bg-amber-500/10 border border-amber-500/30 rounded text-xs text-amber-300 font-medium flex items-center gap-1.5">
        <RefreshCw class="w-3 h-3 animate-spin text-amber-400" />
        {{ offlineStore.syncMessage }}
      </div>

      <!-- Live Polling Indicator -->
      <div class="flex items-center gap-2 px-2.5 py-1 rounded bg-[#0f172a] border border-[#1e293b] text-xs">
        <span class="w-2 h-2 rounded-full bg-emerald-500"></span>
        <span class="text-[11px] font-mono text-slate-300">TELEMETRIA: 10s</span>
      </div>

      <!-- Manual Refresh Button -->
      <button
        @click="refreshData"
        :disabled="loading"
        class="flex items-center gap-1.5 px-2.5 py-1 rounded bg-[#0f172a] hover:bg-[#1e293b] text-slate-200 text-xs font-medium border border-[#1e293b] transition-colors"
        title="Atualizar leituras da frota"
      >
        <RefreshCw class="w-3 h-3 text-slate-400" :class="{ 'animate-spin': loading }" />
        <span>Atualizar</span>
      </button>

      <!-- Switch to Field Terminal -->
      <router-link
        to="/operador"
        class="flex items-center gap-1.5 px-3 py-1 rounded bg-amber-500 hover:bg-amber-400 text-black text-xs font-bold transition-colors"
      >
        <Smartphone class="w-3.5 h-3.5 text-black" />
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
