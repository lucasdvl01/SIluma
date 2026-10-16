<template>
  <div class="space-y-6">
    <!-- Header Page -->
    <div class="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
      <div>
        <h2 class="text-2xl font-black text-white tracking-tight flex items-center gap-2">
          <span>Painel de Indicadores & Eficiência</span>
          <span class="text-xs px-2.5 py-0.5 rounded-full bg-emerald-500/10 text-emerald-400 border border-emerald-500/20 font-bold">
            RF05
          </span>
        </h2>
        <p class="text-xs text-slate-400">
          Visão holística em tempo real da operação autônoma de iluminação das minas
        </p>
      </div>

      <div class="flex items-center gap-2">
        <button
          @click="fetchStats"
          :disabled="loading"
          class="px-3.5 py-2 rounded-xl bg-mina-900 hover:bg-mina-800 text-xs font-semibold text-slate-300 border border-mina-darkBorder flex items-center gap-2 transition-all"
        >
          <RefreshCw class="w-3.5 h-3.5" :class="{ 'animate-spin': loading }" />
          Recarregar Métricas
        </button>
      </div>
    </div>

    <!-- KPI Cards (RF05) -->
    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
      <!-- Card 1: Torres em Funcionamento -->
      <div class="bg-mina-900 border border-mina-darkBorder rounded-2xl p-5 relative overflow-hidden">
        <div class="flex justify-between items-start">
          <div>
            <p class="text-xs font-semibold text-slate-400">Torres em Operação</p>
            <h3 class="text-3xl font-black text-white mt-1">
              {{ stats.torresOperacionais + stats.torresAlertaVisibilidade }}
              <span class="text-sm font-semibold text-slate-500">/ {{ stats.totalTorres }}</span>
            </h3>
          </div>
          <div class="w-10 h-10 rounded-xl bg-emerald-500/15 text-emerald-400 flex items-center justify-center font-bold">
            💡
          </div>
        </div>
        <div class="mt-4 flex items-center gap-2 text-xs">
          <span class="flex items-center text-emerald-400 font-bold">
            {{ Math.round(((stats.torresOperacionais + stats.torresAlertaVisibilidade) / (stats.totalTorres || 1)) * 100) }}%
          </span>
          <span class="text-slate-500">da frota com iluminação ativa</span>
        </div>
        <div class="w-full bg-mina-800 h-1.5 rounded-full mt-2 overflow-hidden">
          <div
            class="bg-emerald-500 h-full rounded-full transition-all duration-500"
            :style="{ width: `${((stats.torresOperacionais + stats.torresAlertaVisibilidade) / (stats.totalTorres || 1)) * 100}%` }"
          ></div>
        </div>
      </div>

      <!-- Card 2: Consumo Energético Global -->
      <div class="bg-mina-900 border border-mina-darkBorder rounded-2xl p-5 relative overflow-hidden">
        <div class="flex justify-between items-start">
          <div>
            <p class="text-xs font-semibold text-slate-400">Consumo Energético</p>
            <h3 class="text-3xl font-black text-amber-400 mt-1">
              {{ stats.consumoTotalKWh }} <span class="text-xs font-bold text-slate-400">kWh</span>
            </h3>
          </div>
          <div class="w-10 h-10 rounded-xl bg-amber-500/15 text-amber-400 flex items-center justify-center font-bold">
            ⚡
          </div>
        </div>
        <div class="mt-4 flex items-center gap-2 text-xs">
          <span class="text-slate-400 font-medium">Horas contínuas registradas:</span>
          <span class="text-white font-bold">{{ stats.totalHorasAtividade }}h</span>
        </div>
      </div>

      <!-- Card 3: Diesel Economizado -->
      <div class="bg-mina-900 border border-mina-darkBorder rounded-2xl p-5 relative overflow-hidden">
        <div class="flex justify-between items-start">
          <div>
            <p class="text-xs font-semibold text-slate-400">Diesel Economizado</p>
            <h3 class="text-3xl font-black text-sky-400 mt-1">
              {{ stats.litrosDieselEconomizados }} <span class="text-xs font-bold text-slate-400">L</span>
            </h3>
          </div>
          <div class="w-10 h-10 rounded-xl bg-sky-500/15 text-sky-400 flex items-center justify-center font-bold">
            🛢️
          </div>
        </div>
        <div class="mt-4 flex items-center gap-2 text-xs">
          <span class="text-sky-300 font-semibold">Tecnologia Solar & Híbrida</span>
        </div>
      </div>

      <!-- Card 4: Redução de CO2 -->
      <div class="bg-mina-900 border border-mina-darkBorder rounded-2xl p-5 relative overflow-hidden">
        <div class="flex justify-between items-start">
          <div>
            <p class="text-xs font-semibold text-slate-400">CO₂ Não Emitido</p>
            <h3 class="text-3xl font-black text-emerald-400 mt-1">
              {{ stats.reducaoCO2Kg }} <span class="text-xs font-bold text-slate-400">kg</span>
            </h3>
          </div>
          <div class="w-10 h-10 rounded-xl bg-emerald-500/15 text-emerald-400 flex items-center justify-center font-bold">
            🌱
          </div>
        </div>
        <div class="mt-4 flex items-center gap-2 text-xs">
          <span class="text-emerald-400 font-bold">Sustentabilidade SENAI</span>
        </div>
      </div>
    </div>

    <!-- Gráficos de Eficiência e Setores (RF05) -->
    <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
      <!-- Gráfico 1: Status da Frota (Doughnut) -->
      <div class="bg-mina-900 border border-mina-darkBorder rounded-2xl p-5 flex flex-col">
        <h4 class="text-sm font-bold text-white mb-1 flex items-center justify-between">
          <span>Status da Frota em Campo</span>
          <span class="text-[10px] text-slate-500 uppercase tracking-wider">Cores Semafóricas</span>
        </h4>
        <p class="text-xs text-slate-400 mb-4">Monitoramento do estado operacional das luminárias</p>
        <div class="relative flex-1 min-h-[220px] flex items-center justify-center">
          <canvas ref="statusChartCanvas"></canvas>
        </div>
      </div>

      <!-- Gráfico 2: Consumo por Setor da Mina (Bar) -->
      <div class="bg-mina-900 border border-mina-darkBorder rounded-2xl p-5 flex flex-col">
        <h4 class="text-sm font-bold text-white mb-1">Distribuição por Setor / Frentes</h4>
        <p class="text-xs text-slate-400 mb-4">Total de torres ativas por trecho da lavra</p>
        <div class="relative flex-1 min-h-[220px]">
          <canvas ref="sectorChartCanvas"></canvas>
        </div>
      </div>

      <!-- Gráfico 3: Matriz Energética (Pie) -->
      <div class="bg-mina-900 border border-mina-darkBorder rounded-2xl p-5 flex flex-col">
        <h4 class="text-sm font-bold text-white mb-1">Matriz Energética da Frota</h4>
        <p class="text-xs text-slate-400 mb-4">Fontes de energia utilizadas (Solar, Diesel, Bateria, Híbrido)</p>
        <div class="relative flex-1 min-h-[220px] flex items-center justify-center">
          <canvas ref="powerChartCanvas"></canvas>
        </div>
      </div>
    </div>

    <!-- Alertas Operacionais e Preventivos (RF02, RF04) -->
    <div class="bg-mina-900 border border-mina-darkBorder rounded-2xl p-5">
      <div class="flex items-center justify-between mb-4">
        <div>
          <h4 class="text-sm font-bold text-white flex items-center gap-2">
            <AlertTriangle class="w-4 h-4 text-amber-500" />
            Alertas Críticos & Preventivos em Campo
          </h4>
          <p class="text-xs text-slate-400">Detecções automatizadas de cerração (IoT), limite de horímetro e baterias</p>
        </div>
        <span class="text-xs px-2.5 py-1 rounded-full bg-mina-800 text-slate-300 font-bold">
          {{ stats.alertasRecentes?.length || 0 }} pendentes
        </span>
      </div>

      <div v-if="stats.alertasRecentes && stats.alertasRecentes.length > 0" class="space-y-2">
        <div
          v-for="(alerta, idx) in stats.alertasRecentes"
          :key="idx"
          class="p-3.5 rounded-xl border flex items-center justify-between transition-all"
          :class="alerta.nivelGravidade === 'CRÍTICO' ? 'bg-red-500/10 border-red-500/30 text-red-200' : 'bg-amber-500/10 border-amber-500/30 text-amber-200'"
        >
          <div class="flex items-center gap-3">
            <span class="px-2 py-0.5 rounded font-black text-xs" :class="alerta.nivelGravidade === 'CRÍTICO' ? 'bg-red-500/30 text-red-300' : 'bg-amber-500/30 text-amber-300'">
              {{ alerta.codigoTorre }}
            </span>
            <div>
              <p class="text-xs font-bold">{{ alerta.tipoAlerta }}</p>
              <p class="text-[11px] text-slate-400">Gravidade: {{ alerta.nivelGravidade }}</p>
            </div>
          </div>
          <span class="text-[11px] text-slate-400 font-mono">{{ alerta.dataHora }}</span>
        </div>
      </div>
      <div v-else class="text-center py-6 text-slate-500 text-xs">
        ✅ Todas as torres estão operando em condições nominais sem alertas críticos.
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue';
import api from '../services/api';
import { RefreshCw, AlertTriangle } from 'lucide-vue-next';
import Chart from 'chart.js/auto';

const loading = ref(false);
const stats = ref({
  totalTorres: 0,
  torresOperacionais: 0,
  torresDesligadas: 0,
  torresAlertaVisibilidade: 0,
  torresEmManutencao: 0,
  consumoTotalKWh: 0,
  economiaGeradaKWh: 0,
  litrosDieselEconomizados: 0,
  reducaoCO2Kg: 0,
  totalHorasAtividade: 0,
  distribuicaoStatus: {},
  distribuicaoFonteEnergia: {},
  distribuicaoSetores: [],
  alertasRecentes: []
});

const statusChartCanvas = ref(null);
const sectorChartCanvas = ref(null);
const powerChartCanvas = ref(null);

let statusChartInstance = null;
let sectorChartInstance = null;
let powerChartInstance = null;

const fetchStats = async () => {
  loading.value = true;
  try {
    const res = await api.get('/dashboard/stats');
    stats.value = res.data;
    await nextTick();
    renderCharts();
  } catch (err) {
    console.error('Erro ao carregar dashboard:', err);
  } finally {
    loading.value = false;
  }
};

const renderCharts = () => {
  // Chart 1: Status Doughnut
  if (statusChartCanvas.value) {
    if (statusChartInstance) statusChartInstance.destroy();

    const labels = Object.keys(stats.value.distribuicaoStatus || {});
    const data = Object.values(stats.value.distribuicaoStatus || {});

    statusChartInstance = new Chart(statusChartCanvas.value, {
      type: 'doughnut',
      data: {
        labels: labels,
        datasets: [{
          data: data,
          backgroundColor: ['#10b981', '#f59e0b', '#6b7280', '#ef4444'],
          borderWidth: 2,
          borderColor: '#111b2d',
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: {
            position: 'bottom',
            labels: { color: '#94a3b8', font: { size: 10, family: 'Plus Jakarta Sans' }, boxWidth: 10 }
          }
        },
        cutout: '70%'
      }
    });
  }

  // Chart 2: Sectors Bar
  if (sectorChartCanvas.value) {
    if (sectorChartInstance) sectorChartInstance.destroy();

    const sectorLabels = (stats.value.distribuicaoSetores || []).map(s => s.setorNome);
    const ativasData = (stats.value.distribuicaoSetores || []).map(s => s.torresAtivas);
    const totalData = (stats.value.distribuicaoSetores || []).map(s => s.totalTorres);

    sectorChartInstance = new Chart(sectorChartCanvas.value, {
      type: 'bar',
      data: {
        labels: sectorLabels,
        datasets: [
          {
            label: 'Torres Ligadas',
            data: ativasData,
            backgroundColor: '#f59e0b',
            borderRadius: 6
          },
          {
            label: 'Total Alocadas',
            data: totalData,
            backgroundColor: '#1e3250',
            borderRadius: 6
          }
        ]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        scales: {
          x: { ticks: { color: '#94a3b8', font: { size: 9 } }, grid: { display: false } },
          y: { ticks: { color: '#94a3b8', stepSize: 1 }, grid: { color: '#1c2e4a' } }
        },
        plugins: {
          legend: {
            position: 'bottom',
            labels: { color: '#94a3b8', font: { size: 10 }, boxWidth: 10 }
          }
        }
      }
    });
  }

  // Chart 3: Power Source Pie
  if (powerChartCanvas.value) {
    if (powerChartInstance) powerChartInstance.destroy();

    const labels = Object.keys(stats.value.distribuicaoFonteEnergia || {});
    const data = Object.values(stats.value.distribuicaoFonteEnergia || {});

    powerChartInstance = new Chart(powerChartCanvas.value, {
      type: 'pie',
      data: {
        labels: labels,
        datasets: [{
          data: data,
          backgroundColor: ['#38bdf8', '#fbbf24', '#a855f7', '#34d399'],
          borderWidth: 2,
          borderColor: '#111b2d',
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: {
            position: 'bottom',
            labels: { color: '#94a3b8', font: { size: 10 }, boxWidth: 10 }
          }
        }
      }
    });
  }
};

let pollTimer = null;
onMounted(() => {
  fetchStats();
  pollTimer = setInterval(fetchStats, 10000);
});

onBeforeUnmount(() => {
  if (pollTimer) clearInterval(pollTimer);
  if (statusChartInstance) statusChartInstance.destroy();
  if (sectorChartInstance) sectorChartInstance.destroy();
  if (powerChartInstance) powerChartInstance.destroy();
});
</script>
