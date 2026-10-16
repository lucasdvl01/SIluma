<template>
  <div class="max-w-md mx-auto space-y-3 pb-12 select-none">
    <!-- Header Rugged Industrial de Campo (RNF-A09) -->
    <div class="bg-[#0e1626] border border-[#1c273c] p-3.5 rounded-xl flex items-center justify-between shadow-lg">
      <div class="flex items-center gap-2.5">
        <div class="w-8 h-8 rounded-lg bg-amber-500/10 border border-amber-500/30 flex items-center justify-center text-amber-500 shrink-0">
          <Smartphone class="w-4 h-4" />
        </div>
        <div>
          <h2 class="text-xs font-bold text-white uppercase tracking-wider font-mono">Terminal de Campo</h2>
          <p class="text-[10px] text-slate-400">Operação Móvel • Mina CCO</p>
        </div>
      </div>

      <!-- Indicador de Rede / Modo Offline (RF13, RNF-A11) -->
      <button
        @click="toggleSimulateOffline"
        class="px-2.5 py-1 rounded text-[10px] font-mono font-bold border transition-colors flex items-center gap-1.5"
        :class="offlineStore.isOnline ? 'bg-emerald-500/10 text-emerald-400 border-emerald-500/30' : 'bg-rose-500/10 text-rose-400 border-rose-500/30'"
        title="Alternar simulação de conexão"
      >
        <span class="w-1.5 h-1.5 rounded-full" :class="offlineStore.isOnline ? 'bg-emerald-500' : 'bg-rose-500'"></span>
        <span>{{ offlineStore.isOnline ? 'ONLINE' : 'OFFLINE' }}</span>
      </button>
    </div>

    <!-- Fila de Comandos Offline (RF13, RNF-A14) -->
    <div
      v-if="offlineStore.pendingCount > 0"
      class="p-3 bg-amber-500/10 border border-amber-500/30 rounded-xl flex items-center justify-between text-xs text-amber-200"
    >
      <div class="flex items-center gap-2">
        <Clock class="w-4 h-4 text-amber-400 shrink-0" />
        <span class="text-[11px]"><strong>{{ offlineStore.pendingCount }} comando(s)</strong> em fila offline.</span>
      </div>
      <button
        v-if="offlineStore.isOnline"
        @click="offlineStore.syncPendingCommands()"
        class="px-2 py-0.5 bg-amber-500 hover:bg-amber-400 text-black font-bold rounded text-[10px] uppercase font-mono"
      >
        Sincronizar
      </button>
    </div>

    <!-- Seletor de Frente / Setor -->
    <div class="flex items-center gap-2 bg-[#0e1626] border border-[#1c273c] p-2.5 rounded-xl">
      <span class="text-[11px] text-slate-400 font-mono font-bold pl-1">SETOR:</span>
      <select
        v-model="selectedSector"
        class="flex-1 bg-[#080d17] border border-[#1e2a40] rounded-lg px-2.5 py-1.5 text-xs text-white focus:outline-none focus:border-amber-500 font-mono"
      >
        <option :value="''">Todas as Frentes de Lavra</option>
        <option v-for="s in towerStore.sectors" :key="s.id" :value="s.id">{{ s.nome }}</option>
      </select>
    </div>

    <!-- Lista de Cartões Táticos para Campo (RNF-A08, RNF-A09 - Máximo 3 Toques) -->
    <div class="space-y-3">
      <div
        v-for="tower in displayedTowers"
        :key="tower.id"
        class="bg-[#0e1626] border border-[#1c273c] rounded-xl p-3.5 space-y-3 transition-colors"
        :class="{
          'border-amber-500/60 bg-[#141c2c]': tower.alertaBaixaVisibilidade || tower.modoOperacao === 'ALTA_VISIBILIDADE_CERRACAO',
          'border-rose-500/40': tower.status === 'EM_MANUTENCAO'
        }"
      >
        <!-- Cabeçalho do Ativo -->
        <div class="flex items-center justify-between">
          <div>
            <div class="flex items-center gap-2">
              <span class="w-2.5 h-2.5 rounded-full" :style="{ backgroundColor: tower.statusCor }"></span>
              <h3 class="text-base font-bold text-white font-mono leading-none">{{ tower.codigoIdentificacao }}</h3>
            </div>
            <p class="text-[11px] text-slate-400 mt-0.5">{{ tower.setorNome }}</p>
          </div>

          <div class="text-right">
            <span
              class="px-2 py-0.5 rounded text-[10px] font-mono font-bold border"
              :style="{
                backgroundColor: `${tower.statusCor}15`,
                borderColor: `${tower.statusCor}40`,
                color: tower.statusCor
              }"
            >
              {{ tower.statusLabel }}
            </span>
            <div class="text-[10px] text-slate-400 font-mono mt-1">
              Bateria: {{ tower.nivelBateria }}% • {{ tower.horasAtividadeAcumuladas }}h
            </div>
          </div>
        </div>

        <!-- Botões de Ação Tátil Grande (RNF-A08) -->
        <div class="grid grid-cols-2 gap-2 pt-1">
          <!-- Botão Ligar/Desligar -->
          <button
            @click="handleTowerAction(tower.id, tower.ligada ? 'DESLIGAR' : 'LIGAR')"
            class="py-2.5 rounded-lg font-bold text-xs flex items-center justify-center gap-1.5 shadow transition-colors active:scale-95"
            :class="tower.ligada ? 'bg-rose-600 hover:bg-rose-500 text-white' : 'bg-emerald-600 hover:bg-emerald-500 text-white'"
          >
            <Power class="w-3.5 h-3.5" />
            <span>{{ tower.ligada ? 'DESLIGAR' : 'LIGAR' }}</span>
          </button>

          <!-- Botão Cerração -->
          <button
            @click="handleTowerAction(tower.id, 'MODO_CERRACAO', 'ALTA_VISIBILIDADE_CERRACAO')"
            class="py-2.5 rounded-lg font-bold text-xs flex items-center justify-center gap-1.5 transition-colors active:scale-95"
            :class="tower.modoOperacao === 'ALTA_VISIBILIDADE_CERRACAO' ? 'bg-amber-500 text-black font-extrabold' : 'bg-[#080d17] text-amber-400 border border-amber-500/30 hover:bg-[#162033]'"
          >
            <CloudFog class="w-3.5 h-3.5" />
            <span>CERRAÇÃO</span>
          </button>
        </div>

        <!-- Botão Rápido de Reporte de Falha -->
        <button
          @click="quickReport(tower)"
          class="w-full py-1 text-center text-[10px] font-mono text-slate-400 hover:text-rose-400 rounded hover:bg-[#080d17] transition-colors border border-transparent hover:border-[#1e2a40]"
        >
          Reportar Falha Técnica nesta Torre
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useTowerStore } from '../stores/towers';
import { useOfflineSyncStore } from '../stores/offlineSync';
import { useMaintenanceStore } from '../stores/maintenance';
import { Smartphone, Clock, Power, CloudFog } from 'lucide-vue-next';

const towerStore = useTowerStore();
const offlineStore = useOfflineSyncStore();
const maintenanceStore = useMaintenanceStore();

const selectedSector = ref('');

const displayedTowers = computed(() => {
  if (!selectedSector.value) return towerStore.towers;
  return towerStore.towers.filter(t => t.setorId === selectedSector.value);
});

const handleTowerAction = async (towerId, action, mode = null) => {
  const result = await offlineStore.dispatchCommand(towerId, action, mode);
  if (result.offline) {
    alert(result.message);
  } else {
    await towerStore.fetchTowers();
  }
};

const toggleSimulateOffline = () => {
  offlineStore.isOnline = !offlineStore.isOnline;
  if (offlineStore.isOnline) {
    offlineStore.syncPendingCommands();
  }
};

const quickReport = async (tower) => {
  const falha = prompt(`Reportar falha para a torre ${tower.codigoIdentificacao}:`, 'Problema elétrico / falha mecânica');
  if (falha) {
    await maintenanceStore.createOrder({
      torreId: tower.id,
      tipoFalha: falha,
      gravidade: 'ALTA',
      descricao: 'Falha reportada diretamente pelo operador de campo via terminal móvel.',
      tecnicoResponsavel: 'Plantão Técnico da Mina'
    });
    await towerStore.fetchTowers();
    alert('Chamado registrado e enviado à equipe técnica!');
  }
};

onMounted(async () => {
  offlineStore.init();
  await towerStore.fetchTowers();
  await towerStore.fetchSectors();
  towerStore.startPolling(10000);
});
</script>
