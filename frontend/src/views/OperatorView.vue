<template>
  <div class="max-w-2xl mx-auto space-y-4 pb-20 select-none">
    
    <!-- Top Cockpit: Identificação do Terminal & Estado de Rede -->
    <header class="tactile-panel rounded-2xl p-4 flex items-center justify-between">
      <div class="flex items-center gap-3.5">
        <div class="w-11 h-11 rounded-xl bg-amber-500/10 border border-amber-500/20 flex items-center justify-center text-amber-400 shrink-0 shadow-inner">
          <Smartphone class="w-5 h-5" />
        </div>
        <div>
          <div class="flex items-center gap-2">
            <h1 class="text-sm font-bold text-white tracking-wide font-sans">Terminal Móvel de Operação</h1>
            <span class="px-2 py-0.5 rounded-full text-[10px] font-mono font-bold bg-slate-800 text-slate-300 border border-slate-700/60">
              Campo
            </span>
          </div>
          <p class="text-xs text-slate-400 mt-0.5">Frente de Lavra Ativa • Mina de Ferro</p>
        </div>
      </div>

      <!-- Comutador Físico de Rede (Do, don't ask: alternância tátil direta) -->
      <button
        @click="toggleSimulateOffline"
        class="group flex items-center gap-2.5 px-3.5 py-2 rounded-xl text-xs font-mono font-bold transition-all interactive-lift border"
        :class="offlineStore.isOnline 
          ? 'bg-emerald-950/60 text-emerald-300 border-emerald-500/30 hover:border-emerald-500/50' 
          : 'bg-rose-950/60 text-rose-300 border-rose-500/30 hover:border-rose-500/50'"
        title="Alternar entre modo conectado e simulação de perda de sinal"
      >
        <span class="relative flex h-2.5 w-2.5">
          <span v-if="offlineStore.isOnline" class="animate-ping absolute inline-flex h-full w-full rounded-full bg-emerald-400 opacity-75"></span>
          <span class="relative inline-flex rounded-full h-2.5 w-2.5" :class="offlineStore.isOnline ? 'bg-emerald-400' : 'bg-rose-500'"></span>
        </span>
        <span class="tracking-wider">{{ offlineStore.isOnline ? 'SINAL OK' : 'OFFLINE' }}</span>
      </button>
    </header>

    <!-- Modeless Toast de Confirmação Rápida -->
    <transition enter-active-class="transition duration-200 ease-out" enter-from-class="opacity-0 -translate-y-2 scale-95" enter-to-class="opacity-100 translate-y-0 scale-100" leave-active-class="transition duration-150 ease-in" leave-from-class="opacity-100" leave-to-class="opacity-0">
      <div v-if="statusFeedback" class="p-3.5 rounded-xl bg-amber-500/15 border border-amber-500/30 text-amber-200 text-xs font-medium flex items-center justify-between shadow-lg backdrop-blur-md">
        <div class="flex items-center gap-2.5">
          <CheckCircle2 class="w-4 h-4 text-amber-400 shrink-0" />
          <span>{{ statusFeedback }}</span>
        </div>
        <button @click="statusFeedback = ''" class="text-amber-400/80 hover:text-white p-1">✕</button>
      </div>
    </transition>

    <!-- Fila Offline Pendente (com botão de sincronização em 1 toque) -->
    <div
      v-if="offlineStore.pendingCount > 0"
      class="p-3.5 bg-amber-500/15 border border-amber-500/40 rounded-2xl flex items-center justify-between text-xs text-amber-200 shadow-md animate-pulse"
    >
      <div class="flex items-center gap-2.5">
        <Clock class="w-4 h-4 text-amber-400 shrink-0" />
        <span><strong>{{ offlineStore.pendingCount }} comando(s)</strong> gravados localmente sem sinal.</span>
      </div>
      <button
        v-if="offlineStore.isOnline"
        @click="syncNow"
        class="px-3.5 py-1.5 bg-amber-400 hover:bg-amber-300 text-slate-950 font-bold rounded-lg text-xs font-mono uppercase interactive-lift transition-all shadow"
      >
        Sincronizar
      </button>
    </div>

    <!-- Cockpit de Pílulas Táticas de 1 Toque (Glanceability de 1 Segundo) -->
    <div class="tactile-panel p-3 rounded-2xl space-y-3">
      <div class="flex items-center justify-between px-1">
        <span class="text-[11px] font-bold text-slate-400 uppercase tracking-wider font-mono">Filtro Rápido por Estado:</span>
        <span class="text-[11px] text-slate-400 font-mono">{{ filteredTowers.length }} de {{ towerStore.towers.length }} torres</span>
      </div>

      <div class="grid grid-cols-2 sm:grid-cols-4 gap-2">
        <!-- Todas -->
        <button
          @click="statusFilter = 'TODAS'"
          class="p-2.5 rounded-xl border text-left transition-all interactive-lift flex items-center justify-between"
          :class="statusFilter === 'TODAS' 
            ? 'bg-slate-800 text-white border-slate-600 shadow-sm' 
            : 'bg-slate-900/60 text-slate-400 border-white/5 hover:border-white/10 hover:text-slate-200'"
        >
          <span class="text-xs font-semibold">Todas</span>
          <span class="font-mono text-xs font-bold px-1.5 py-0.5 rounded bg-slate-800/80 text-white">{{ towerStore.towers.length }}</span>
        </button>

        <!-- Acessas -->
        <button
          @click="statusFilter = 'LIGADAS'"
          class="p-2.5 rounded-xl border text-left transition-all interactive-lift flex items-center justify-between"
          :class="statusFilter === 'LIGADAS' 
            ? 'bg-emerald-950/80 text-emerald-300 border-emerald-500/50 shadow-sm' 
            : 'bg-slate-900/60 text-slate-400 border-white/5 hover:border-emerald-500/20 hover:text-emerald-300'"
        >
          <div class="flex items-center gap-1.5">
            <span class="w-2 h-2 rounded-full bg-emerald-400 shadow-sm"></span>
            <span class="text-xs font-semibold">Acesas</span>
          </div>
          <span class="font-mono text-xs font-bold px-1.5 py-0.5 rounded bg-emerald-900/40 text-emerald-300">{{ activeTowersCount }}</span>
        </button>

        <!-- Cerração -->
        <button
          @click="statusFilter = 'CERRACAO'"
          class="p-2.5 rounded-xl border text-left transition-all interactive-lift flex items-center justify-between"
          :class="statusFilter === 'CERRACAO' 
            ? 'bg-amber-950/80 text-amber-300 border-amber-500/50 shadow-sm' 
            : 'bg-slate-900/60 text-slate-400 border-white/5 hover:border-amber-500/20 hover:text-amber-300'"
        >
          <div class="flex items-center gap-1.5">
            <CloudFog class="w-3.5 h-3.5 text-amber-400" />
            <span class="text-xs font-semibold">Cerração</span>
          </div>
          <span class="font-mono text-xs font-bold px-1.5 py-0.5 rounded bg-amber-900/40 text-amber-300">{{ fogTowersCount }}</span>
        </button>

        <!-- Manutenção -->
        <button
          @click="statusFilter = 'MANUTENCAO'"
          class="p-2.5 rounded-xl border text-left transition-all interactive-lift flex items-center justify-between"
          :class="statusFilter === 'MANUTENCAO' 
            ? 'bg-rose-950/80 text-rose-300 border-rose-500/50 shadow-sm' 
            : 'bg-slate-900/60 text-slate-400 border-white/5 hover:border-rose-500/20 hover:text-rose-300'"
        >
          <div class="flex items-center gap-1.5">
            <Wrench class="w-3.5 h-3.5 text-rose-400" />
            <span class="text-xs font-semibold">Revisão</span>
          </div>
          <span class="font-mono text-xs font-bold px-1.5 py-0.5 rounded bg-rose-900/40 text-rose-300">{{ maintTowersCount }}</span>
        </button>
      </div>

      <!-- Seletor de Frente de Lavra (Setor) -->
      <div class="pt-2 border-t border-white/5 flex items-center gap-2">
        <span class="text-xs text-slate-400 font-mono font-bold shrink-0 pl-1">Frente:</span>
        <select
          v-model="selectedSector"
          class="flex-1 bg-slate-950 border border-slate-700/60 rounded-xl px-3 py-1.5 text-xs text-white focus:outline-none focus:border-amber-400 font-sans"
        >
          <option :value="''">Todas as Frentes de Lavra da Mina</option>
          <option v-for="s in towerStore.sectors" :key="s.id" :value="s.id">{{ s.nome }}</option>
        </select>
      </div>
    </div>

    <!-- Ações Coletivas Rápidas de Setor (Master Control Bar) -->
    <div class="tactile-well p-3 rounded-2xl flex items-center justify-between gap-2 border border-white/5">
      <div class="text-[11px] font-mono text-slate-400 font-semibold pl-1 shrink-0">
        Ações Rápidas da Frente:
      </div>
      <div class="flex items-center gap-2">
        <button
          @click="batchActionCurrentSector('LIGAR')"
          class="px-3 py-1.5 rounded-xl bg-emerald-500/15 hover:bg-emerald-500/25 border border-emerald-500/30 text-emerald-300 text-xs font-bold font-mono transition-all interactive-lift flex items-center gap-1.5"
        >
          <Power class="w-3 h-3" />
          <span>Ligar Todas</span>
        </button>
        <button
          @click="batchActionCurrentSector('ALTERAR_MODO', 'ALTA_VISIBILIDADE_CERRACAO')"
          class="px-3 py-1.5 rounded-xl bg-amber-500/15 hover:bg-amber-500/25 border border-amber-500/30 text-amber-300 text-xs font-bold font-mono transition-all interactive-lift flex items-center gap-1.5"
        >
          <CloudFog class="w-3 h-3" />
          <span>Cerração Geral</span>
        </button>
        <button
          @click="batchActionCurrentSector('DESLIGAR')"
          class="px-2.5 py-1.5 rounded-xl bg-slate-800 hover:bg-slate-700 border border-slate-600 text-slate-300 text-xs font-bold font-mono transition-all interactive-lift"
        >
          Apagar
        </button>
      </div>
    </div>

    <!-- Lista de Cartões Industriais Táticos (Com Metáfora Visual de Iluminação Real) -->
    <div class="space-y-4">
      <div
        v-for="tower in filteredTowers"
        :key="tower.id"
        class="tactile-panel rounded-2xl overflow-hidden relative transition-all"
        :class="{
          'beam-cone-amber border-amber-500/40': tower.ligada && tower.modoOperacao === 'ALTA_VISIBILIDADE_CERRACAO',
          'beam-cone-emerald border-emerald-500/30': tower.ligada && tower.modoOperacao === 'NORMAL',
          'beam-cone-sky border-sky-500/30': tower.ligada && tower.modoOperacao === 'ECONOMIA',
          'opacity-80': !tower.ligada
        }"
      >
        <!-- Faixa de Destaque Semafórica Lateral (Refactoring UI: Accent Border) -->
        <div
          class="absolute left-0 top-0 bottom-0 w-1.5"
          :class="{
            'bg-emerald-500': tower.ligada && tower.status !== 'EM_MANUTENCAO',
            'bg-amber-500 animate-pulse': tower.alertaBaixaVisibilidade || tower.modoOperacao === 'ALTA_VISIBILIDADE_CERRACAO',
            'bg-rose-500': tower.status === 'EM_MANUTENCAO',
            'bg-slate-700': !tower.ligada
          }"
        ></div>

        <div class="p-4 pl-5 space-y-3.5">
          <!-- Cabeçalho do Ativo com Ilustração Gráfica de Luminária -->
          <div class="flex items-center justify-between">
            <div class="flex items-center gap-3">
              <!-- Ícone Físico da Luminária com Feixe Luminescente -->
              <div
                class="w-10 h-10 rounded-xl flex items-center justify-center transition-all shadow-inner border"
                :class="{
                  'bg-amber-500/20 border-amber-500/40 text-amber-300 shadow-[0_0_15px_rgba(245,158,11,0.3)]': tower.ligada && tower.modoOperacao === 'ALTA_VISIBILIDADE_CERRACAO',
                  'bg-emerald-500/20 border-emerald-500/40 text-emerald-300 shadow-[0_0_15px_rgba(16,185,129,0.25)]': tower.ligada && tower.modoOperacao === 'NORMAL',
                  'bg-sky-500/20 border-sky-500/40 text-sky-300': tower.ligada && tower.modoOperacao === 'ECONOMIA',
                  'bg-slate-900 border-white/10 text-slate-600': !tower.ligada
                }"
              >
                <RadioTower class="w-5 h-5" />
              </div>

              <div>
                <div class="flex items-center gap-2">
                  <h3 class="text-base font-bold text-white font-mono tracking-wider">{{ tower.codigoIdentificacao }}</h3>
                  <span
                    v-if="tower.alertaBaixaVisibilidade"
                    class="px-2 py-0.5 rounded-full text-[10px] font-bold font-mono bg-amber-500/20 border border-amber-500/40 text-amber-300 animate-pulse"
                  >
                    ALERTA CERRAÇÃO
                  </span>
                </div>
                <p class="text-xs text-slate-400 font-sans mt-0.5">{{ tower.setorNome }}</p>
              </div>
            </div>

            <!-- Indicador Gráfico de Bateria & Horímetro -->
            <div class="text-right space-y-1">
              <div class="flex items-center justify-end gap-1.5 font-mono text-xs">
                <!-- Segmentos gráficos de bateria -->
                <div class="flex gap-0.5 items-center">
                  <span class="w-1.5 h-3.5 rounded-xs" :class="tower.nivelBateria >= 20 ? (tower.nivelBateria < 40 ? 'bg-amber-400' : 'bg-emerald-400') : 'bg-rose-500'"></span>
                  <span class="w-1.5 h-3.5 rounded-xs" :class="tower.nivelBateria >= 40 ? 'bg-emerald-400' : 'bg-slate-700'"></span>
                  <span class="w-1.5 h-3.5 rounded-xs" :class="tower.nivelBateria >= 70 ? 'bg-emerald-400' : 'bg-slate-700'"></span>
                  <span class="w-1.5 h-3.5 rounded-xs" :class="tower.nivelBateria >= 90 ? 'bg-emerald-400' : 'bg-slate-700'"></span>
                </div>
                <span class="font-bold tabular-nums" :class="tower.nivelBateria < 25 ? 'text-rose-400' : 'text-slate-200'">{{ tower.nivelBateria }}%</span>
              </div>
              <div class="text-[10px] text-slate-400 font-mono tabular-nums">
                {{ tower.horasAtividadeAcumuladas }}h de uso
              </div>
            </div>
          </div>

          <!-- Controle de Energia: Segmented Switch Físico Inequívoco (Elimina Ambiguidade de Ação vs Estado) -->
          <div class="pt-1 flex flex-col sm:flex-row sm:items-center justify-between gap-3 bg-black/40 p-2.5 rounded-xl border border-white/5">
            <div class="flex items-center gap-2">
              <span class="text-xs text-slate-400 font-mono font-bold">LUMINÁRIA:</span>
              <span
                class="px-2 py-0.5 rounded text-[11px] font-mono font-bold"
                :class="tower.ligada 
                  ? (tower.modoOperacao === 'ALTA_VISIBILIDADE_CERRACAO' ? 'bg-amber-500/20 text-amber-300' : 'bg-emerald-500/20 text-emerald-300') 
                  : 'bg-slate-800 text-slate-400'"
              >
                {{ tower.ligada ? '● ACESA EM CAMPO' : '○ APAGADA' }}
              </span>
            </div>

            <!-- Chave Seletora de Dois Estágios (Apagada | Acesa) -->
            <div class="segmented-control shrink-0">
              <button
                @click="setTowerPower(tower, false)"
                class="px-3.5 py-1.5 rounded-lg text-xs font-mono font-bold transition-all"
                :class="!tower.ligada 
                  ? 'bg-slate-700 text-white shadow-sm' 
                  : 'text-slate-400 hover:text-slate-200'"
              >
                Apagada
              </button>
              <button
                @click="setTowerPower(tower, true)"
                class="px-4 py-1.5 rounded-lg text-xs font-mono font-bold transition-all"
                :class="tower.ligada 
                  ? 'bg-emerald-500 text-slate-950 shadow-sm font-extrabold' 
                  : 'text-slate-400 hover:text-emerald-300'"
              >
                Acesa
              </button>
            </div>
          </div>

          <!-- Modos de Operação (Seletor Tátil de 3 Níveis - Ativo quando acesa) -->
          <div v-if="tower.ligada" class="space-y-1.5 pt-1">
            <span class="text-[10px] font-mono uppercase font-bold text-slate-400 tracking-wider">Modo do Feixe de Luz:</span>
            <div class="grid grid-cols-3 gap-2">
              <!-- Normal -->
              <button
                @click="setTowerMode(tower, 'NORMAL')"
                class="py-2 px-2 rounded-xl text-xs font-semibold border text-center transition-all interactive-lift flex flex-col items-center justify-center gap-1"
                :class="tower.modoOperacao === 'NORMAL' 
                  ? 'bg-emerald-500/20 border-emerald-500/60 text-emerald-300 font-bold shadow-sm' 
                  : 'bg-slate-900/60 border-white/5 text-slate-400 hover:text-slate-200 hover:bg-slate-800'"
              >
                <Sun class="w-3.5 h-3.5" />
                <span>Normal (80%)</span>
              </button>

              <!-- Cerração -->
              <button
                @click="setTowerMode(tower, 'ALTA_VISIBILIDADE_CERRACAO')"
                class="py-2 px-2 rounded-xl text-xs font-semibold border text-center transition-all interactive-lift flex flex-col items-center justify-center gap-1"
                :class="tower.modoOperacao === 'ALTA_VISIBILIDADE_CERRACAO' 
                  ? 'bg-amber-400 border-amber-400 text-slate-950 font-extrabold shadow-sm' 
                  : 'bg-slate-900/60 border-white/5 text-amber-400/80 hover:text-amber-300 hover:bg-slate-800'"
              >
                <CloudFog class="w-3.5 h-3.5" />
                <span>Cerração Âmbar</span>
              </button>

              <!-- Economia -->
              <button
                @click="setTowerMode(tower, 'ECONOMIA')"
                class="py-2 px-2 rounded-xl text-xs font-semibold border text-center transition-all interactive-lift flex flex-col items-center justify-center gap-1"
                :class="tower.modoOperacao === 'ECONOMIA' 
                  ? 'bg-sky-500/20 border-sky-500/60 text-sky-300 font-bold shadow-sm' 
                  : 'bg-slate-900/60 border-white/5 text-slate-400 hover:text-slate-200 hover:bg-slate-800'"
              >
                <Leaf class="w-3.5 h-3.5" />
                <span>Eco (50%)</span>
              </button>
            </div>
          </div>

          <!-- Reporte de Anomalia em 1 Toque (Sem digitação de texto obrigatória) -->
          <div class="pt-2 border-t border-white/5">
            <div v-if="activeReportTowerId === tower.id" class="p-3 bg-slate-950 rounded-xl border border-rose-500/30 space-y-2.5 animate-fadeIn">
              <div class="flex items-center justify-between">
                <span class="text-xs font-bold text-rose-300 font-sans">Toque no defeito observado:</span>
                <button @click="activeReportTowerId = null" class="text-xs text-slate-400 hover:text-white">✕ Cancelar</button>
              </div>

              <!-- 4 Chips de 1 Toque para Operadores com Luvas -->
              <div class="grid grid-cols-2 gap-2">
                <button
                  v-for="chip in quickFaultChips"
                  :key="chip"
                  @click="submitOneTapReport(tower, chip)"
                  class="p-2 rounded-lg bg-rose-500/10 hover:bg-rose-500/25 border border-rose-500/20 text-rose-200 text-xs font-medium text-left transition-all interactive-lift"
                >
                  {{ chip }}
                </button>
              </div>
            </div>

            <button
              v-else
              @click="openReport(tower)"
              class="w-full py-2 text-center text-xs font-mono text-slate-400 hover:text-rose-400 rounded-xl hover:bg-rose-500/10 transition-colors flex items-center justify-center gap-1.5"
            >
              <AlertCircle class="w-3.5 h-3.5" />
              <span>Reportar Anomalia Técnica</span>
            </button>
          </div>

        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useTowerStore } from '../stores/towers';
import { useOfflineSyncStore } from '../stores/offlineSync';
import { useMaintenanceStore } from '../stores/maintenance';
import {
  Smartphone,
  RadioTower,
  Sun,
  CloudFog,
  Leaf,
  Clock,
  Power,
  Wrench,
  AlertCircle,
  CheckCircle2
} from 'lucide-vue-next';

const towerStore = useTowerStore();
const offlineStore = useOfflineSyncStore();
const maintenanceStore = useMaintenanceStore();

const statusFilter = ref('TODAS');
const selectedSector = ref('');
const statusFeedback = ref('');
const activeReportTowerId = ref(null);

const quickFaultChips = [
  'Luminária Queimada / Apagada',
  'Bateria com Descarga Anormal',
  'Mastro Mecânico Danificado',
  'Falha no Sensor de Luz (LDR)'
];

const showFeedback = (msg) => {
  statusFeedback.value = msg;
  setTimeout(() => {
    if (statusFeedback.value === msg) {
      statusFeedback.value = '';
    }
  }, 4000);
};

// Contagens para o Cockpit de 1 Segundo
const activeTowersCount = computed(() => towerStore.towers.filter(t => t.ligada).length);
const fogTowersCount = computed(() => towerStore.towers.filter(t => t.alertaBaixaVisibilidade || t.modoOperacao === 'ALTA_VISIBILIDADE_CERRACAO').length);
const maintTowersCount = computed(() => towerStore.towers.filter(t => t.status === 'EM_MANUTENCAO' || t.necessitaManutencaoPreventiva).length);

// Filtragem Combinada (Filtro Rápido de Estado + Setor de Lavra)
const filteredTowers = computed(() => {
  let list = towerStore.towers;

  if (selectedSector.value) {
    list = list.filter(t => t.setorId === selectedSector.value);
  }

  if (statusFilter.value === 'LIGADAS') {
    return list.filter(t => t.ligada);
  }
  if (statusFilter.value === 'CERRACAO') {
    return list.filter(t => t.alertaBaixaVisibilidade || t.modoOperacao === 'ALTA_VISIBILIDADE_CERRACAO');
  }
  if (statusFilter.value === 'MANUTENCAO') {
    return list.filter(t => t.status === 'EM_MANUTENCAO' || t.necessitaManutencaoPreventiva);
  }

  return list;
});

// Ações Operacionais Individuais
const setTowerPower = async (tower, shouldBeOn) => {
  if (tower.ligada === shouldBeOn) return;
  const action = shouldBeOn ? 'LIGAR' : 'DESLIGAR';
  const result = await offlineStore.dispatchCommand(tower.id, action, null);
  if (result.offline) {
    showFeedback(`Comando offline enfileirado para ${tower.codigoIdentificacao}`);
  } else {
    await towerStore.fetchTowers();
    showFeedback(`Torre ${tower.codigoIdentificacao} ${shouldBeOn ? 'acesa' : 'apagada'}`);
  }
};

const setTowerMode = async (tower, mode) => {
  if (tower.modoOperacao === mode) return;
  const result = await offlineStore.dispatchCommand(tower.id, 'ALTERAR_MODO', mode);
  if (result.offline) {
    showFeedback(`Modo offline enfileirado para ${tower.codigoIdentificacao}`);
  } else {
    await towerStore.fetchTowers();
    showFeedback(`Modo alterado para ${mode === 'ALTA_VISIBILIDADE_CERRACAO' ? 'Cerração' : mode} em ${tower.codigoIdentificacao}`);
  }
};

// Ação em Lote para a Frente Atual
const batchActionCurrentSector = async (acao, modo = null) => {
  let sectorId = selectedSector.value;
  if (!sectorId && towerStore.sectors.length > 0) {
    sectorId = towerStore.sectors[0].id;
  }
  if (!sectorId) return;

  try {
    await towerStore.executeBatchAction(sectorId, acao, modo);
    showFeedback(`Comando '${acao}' aplicado a todas as torres da frente!`);
  } catch (err) {
    showFeedback('Falha ao disparar comando coletivo');
  }
};

const toggleSimulateOffline = () => {
  offlineStore.isOnline = !offlineStore.isOnline;
  showFeedback(`Modo de rede do terminal: ${offlineStore.isOnline ? 'ONLINE (Conectado)' : 'OFFLINE (Fila Local)'}`);
  if (offlineStore.isOnline) {
    offlineStore.syncPendingCommands();
  }
};

const syncNow = async () => {
  await offlineStore.syncPendingCommands();
  showFeedback('Fila sincronizada com sucesso!');
};

const openReport = (tower) => {
  activeReportTowerId.value = tower.id;
};

// Reporte de 1 Toque com Chips Industriais
const submitOneTapReport = async (tower, faultTitle) => {
  await maintenanceStore.createOrder({
    torreId: tower.id,
    tipoFalha: faultTitle,
    gravidade: 'ALTA',
    descricao: `Chamado técnico de campo aberto pelo operador: ${faultTitle}.`,
    tecnicoResponsavel: 'Equipe de Manutenção Elétrica da Mina'
  });
  await towerStore.fetchTowers();
  activeReportTowerId.value = null;
  showFeedback(`OS emitida com sucesso para a torre ${tower.codigoIdentificacao}!`);
};

onMounted(async () => {
  offlineStore.init();
  await towerStore.fetchTowers();
  await towerStore.fetchSectors();
  towerStore.startPolling(8000);
});
</script>
