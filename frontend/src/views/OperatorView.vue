<template>
  <div class="max-w-md mx-auto space-y-4 pb-12 select-none">
    <!-- Header Minimalista de Campo (RNF-A09) -->
    <div class="bg-mina-900 border border-mina-darkBorder p-4 rounded-2xl flex items-center justify-between shadow-lg">
      <div class="flex items-center gap-3">
        <div class="w-10 h-10 rounded-xl bg-gradient-to-br from-amber-500 to-amber-600 flex items-center justify-center font-black text-black">
          📱
        </div>
        <div>
          <h2 class="text-sm font-black text-white uppercase tracking-wider">Modo Operador de Campo</h2>
          <p class="text-[11px] text-slate-400">Agilidade Operacional • Mina</p>
        </div>
      </div>

      <!-- Indicador Online / Offline com chave de teste -->
      <div class="flex items-center gap-2">
        <button
          @click="toggleSimulateOffline"
          class="px-2.5 py-1 rounded-lg text-[10px] font-bold border transition-all"
          :class="offlineStore.isOnline ? 'bg-emerald-500/10 text-emerald-400 border-emerald-500/30' : 'bg-rose-500/10 text-rose-400 border-rose-500/30'"
        >
          <span class="inline-block w-2 h-2 rounded-full mr-1" :class="offlineStore.isOnline ? 'bg-emerald-400 animate-pulse' : 'bg-rose-500'"></span>
          {{ offlineStore.isOnline ? 'Conectado' : 'Sem Sinal (Offline)' }}
        </button>
      </div>
    </div>

    <!-- Banner de Fila Offline (RF13, RNF-A14) -->
    <div
      v-if="offlineStore.pendingCount > 0"
      class="p-3 bg-amber-500/20 border border-amber-500/40 rounded-2xl flex items-center justify-between text-xs text-amber-200 animate-pulse"
    >
      <div class="flex items-center gap-2">
        <Clock class="w-4 h-4 text-amber-400" />
        <span><strong>{{ offlineStore.pendingCount }} comando(s)</strong> pendente(s) na fila offline.</span>
      </div>
      <button
        v-if="offlineStore.isOnline"
        @click="offlineStore.syncPendingCommands()"
        class="px-2.5 py-1 bg-amber-500 hover:bg-amber-400 text-black font-extrabold rounded-lg text-[11px]"
      >
        Sincronizar
      </button>
    </div>

    <!-- Quick Filter / Setor -->
    <div class="flex items-center gap-2 bg-mina-900 border border-mina-darkBorder p-2.5 rounded-xl">
      <span class="text-xs text-slate-400 font-bold pl-1">Setor:</span>
      <select
        v-model="selectedSector"
        class="flex-1 bg-mina-800 border border-mina-darkBorder rounded-lg px-2.5 py-1.5 text-xs text-white focus:outline-none"
      >
        <option :value="''">Todas as Torres da Mina</option>
        <option v-for="s in towerStore.sectors" :key="s.id" :value="s.id">{{ s.nome }}</option>
      </select>
    </div>

    <!-- Cards Grandes de Toque Fácil (RNF-A08, RNF-A09 - Máximo 3 Toques) -->
    <div class="space-y-3">
      <div
        v-for="tower in displayedTowers"
        :key="tower.id"
        class="bg-mina-900 border border-mina-darkBorder rounded-2xl p-4 shadow-xl space-y-3 transition-all"
        :class="{
          'border-amber-500/50 bg-amber-500/5': tower.alertaBaixaVisibilidade || tower.modoOperacao === 'ALTA_VISIBILIDADE_CERRACAO',
          'border-rose-500/40 bg-rose-500/5': tower.status === 'EM_MANUTENCAO'
        }"
      >
        <!-- Top Info -->
        <div class="flex items-center justify-between">
          <div>
            <div class="flex items-center gap-2">
              <span class="w-3.5 h-3.5 rounded-full" :style="{ backgroundColor: tower.statusCor }"></span>
              <h3 class="text-lg font-black text-white font-mono">{{ tower.codigoIdentificacao }}</h3>
            </div>
            <p class="text-xs text-slate-400">{{ tower.setorNome }}</p>
          </div>

          <div class="text-right">
            <span
              class="px-2.5 py-0.5 rounded-full text-[11px] font-bold"
              :style="{ backgroundColor: `${tower.statusCor}25`, color: tower.statusCor }"
            >
              {{ tower.statusLabel }}
            </span>
            <div class="text-[11px] text-slate-400 font-mono mt-0.5">
              🔋 {{ tower.nivelBateria }}% • ⏱️ {{ tower.horasAtividadeAcumuladas }}h
            </div>
          </div>
        </div>

        <!-- Botões de Ação Grandes e Imediatos (RNF-A08: até 3 toques) -->
        <div class="grid grid-cols-2 gap-2 pt-1">
          <!-- Botão Ligar/Desligar -->
          <button
            @click="handleTowerAction(tower.id, tower.ligada ? 'DESLIGAR' : 'LIGAR')"
            class="py-3 rounded-xl font-black text-sm flex items-center justify-center gap-2 shadow-md transition-all active:scale-95"
            :class="tower.ligada ? 'bg-rose-600 hover:bg-rose-500 text-white' : 'bg-emerald-600 hover:bg-emerald-500 text-white'"
          >
            <span>{{ tower.ligada ? '🛑 DESLIGAR' : '💡 LIGAR' }}</span>
          </button>

          <!-- Botão Rápido Cerração -->
          <button
            @click="handleTowerAction(tower.id, 'MODO_CERRACAO', 'ALTA_VISIBILIDADE_CERRACAO')"
            class="py-3 rounded-xl font-black text-sm flex items-center justify-center gap-1.5 shadow-md transition-all active:scale-95"
            :class="tower.modoOperacao === 'ALTA_VISIBILIDADE_CERRACAO' ? 'bg-amber-500 text-black animate-pulse' : 'bg-mina-800 text-amber-400 hover:bg-mina-700 border border-amber-500/30'"
          >
            <span>🌫️ CERRAÇÃO</span>
          </button>
        </div>

        <!-- Botão de Reportar Falha Rápido -->
        <button
          @click="quickReport(tower)"
          class="w-full py-1.5 text-center text-[11px] font-semibold text-slate-400 hover:text-rose-400 rounded-lg hover:bg-mina-800 transition-colors"
        >
          ⚠️ Reportar Falha Nesta Torre
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
import { Clock } from 'lucide-vue-next';

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
    // Atualiza lista local
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
  const falha = prompt(`Reportar falha para torre ${tower.codigoIdentificacao}:`, 'Problema elétrico / falha mecânica');
  if (falha) {
    await maintenanceStore.createOrder({
      torreId: tower.id,
      tipoFalha: falha,
      gravidade: 'ALTA',
      descricao: 'Falha reportada diretamente pelo operador de campo via app móvel.',
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
