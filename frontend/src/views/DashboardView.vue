<template>
  <div class="space-y-6">
    <!-- Header Executivo CCO: Limpo, Autoritário e Sem Jargões de IA -->
    <header class="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 pb-4 border-b border-white/5">
      <div>
        <h1 class="text-xl font-bold text-white tracking-tight font-sans">
          Painel de Controle & Indicadores da Frota
        </h1>
        <p class="text-xs text-slate-400 mt-0.5">
          Visão consolidada de disponibilidade, consumo elétrico e autonomia das torres na cava
        </p>
      </div>

      <div class="flex items-center gap-3">
        <!-- Badge de Conexão em Tempo Real -->
        <div class="flex items-center gap-2 px-3 py-1.5 rounded-xl bg-slate-900 border border-white/10 text-xs">
          <span class="relative flex h-2 w-2">
            <span class="animate-ping absolute inline-flex h-full w-full rounded-full bg-emerald-400 opacity-75"></span>
            <span class="relative inline-flex rounded-full h-2 w-2 bg-emerald-500"></span>
          </span>
          <span class="text-[11px] font-mono text-slate-300 font-medium">TELEMETRIA ATIVA</span>
        </div>

        <button
          @click="fetchStats"
          :disabled="loading"
          class="interactive-lift px-3.5 py-1.5 rounded-xl bg-slate-900 hover:bg-slate-800 text-xs font-semibold text-slate-200 border border-white/10 flex items-center gap-2 transition-all shadow-sm disabled:opacity-50"
        >
          <RefreshCw class="w-3.5 h-3.5 text-slate-400" :class="{ 'animate-spin': loading }" />
          <span>Atualizar</span>
        </button>
      </div>
    </header>

    <!-- Métricas Principais CCO: Formato Industrial de Alta Hierarquia -->
    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
      <!-- Card 1: Torres em Operação -->
      <div class="tactile-panel rounded-2xl p-4 flex flex-col justify-between">
        <div class="flex justify-between items-start">
          <div>
            <span class="text-[11px] font-bold text-slate-400 uppercase tracking-wider block font-mono">Disponibilidade de Campo</span>
            <div class="text-2xl font-bold font-mono text-white mt-2 tabular-nums flex items-baseline gap-1.5">
              <span>{{ stats.torresOperacionais + stats.torresAlertaVisibilidade }}</span>
              <span class="text-xs font-medium text-slate-500 font-mono">/ {{ stats.totalTorres }} ativas</span>
            </div>
          </div>
          <div class="w-10 h-10 rounded-xl bg-emerald-500/10 border border-emerald-500/20 flex items-center justify-center text-emerald-400 shrink-0">
            <RadioTower class="w-5 h-5" />
          </div>
        </div>
        <div class="mt-4 pt-3 border-t border-white/5">
          <div class="flex justify-between text-[11px] text-slate-400 mb-1.5">
            <span>Taxa Operacional</span>
            <span class="font-mono text-emerald-400 font-bold tabular-nums">
              {{ Math.round(((stats.torresOperacionais + stats.torresAlertaVisibilidade) / (stats.totalTorres || 1)) * 100) }}%
            </span>
          </div>
          <div class="w-full bg-slate-950 h-1.5 rounded-full overflow-hidden border border-white/5">
            <div
              class="bg-emerald-500 h-full rounded-full transition-all duration-500"
              :style="{ width: `${((stats.torresOperacionais + stats.torresAlertaVisibilidade) / (stats.totalTorres || 1)) * 100}%` }"
            ></div>
          </div>
        </div>
      </div>

      <!-- Card 2: Consumo Energético -->
      <div class="tactile-panel rounded-2xl p-4 flex flex-col justify-between">
        <div class="flex justify-between items-start">
          <div>
            <span class="text-[11px] font-bold text-slate-400 uppercase tracking-wider block font-mono">Consumo da Frota</span>
            <div class="text-2xl font-bold font-mono text-amber-400 mt-2 tabular-nums flex items-baseline gap-1">
              <span>{{ stats.consumoTotalKWh }}</span>
              <span class="text-xs font-normal text-slate-400 font-sans">kWh</span>
            </div>
          </div>
          <div class="w-10 h-10 rounded-xl bg-amber-500/10 border border-amber-500/20 flex items-center justify-center text-amber-400 shrink-0">
            <Zap class="w-5 h-5" />
          </div>
        </div>
        <div class="mt-4 pt-3 border-t border-white/5 flex items-center justify-between text-[11px] text-slate-400">
          <span>Horímetro Total:</span>
          <span class="font-mono text-slate-200 font-bold tabular-nums">{{ stats.totalHorasAtividade }} horas</span>
        </div>
      </div>

      <!-- Card 3: Diesel Economizado -->
      <div class="tactile-panel rounded-2xl p-4 flex flex-col justify-between">
        <div class="flex justify-between items-start">
          <div>
            <span class="text-[11px] font-bold text-slate-400 uppercase tracking-wider block font-mono">Diesel Evitado</span>
            <div class="text-2xl font-bold font-mono text-sky-400 mt-2 tabular-nums flex items-baseline gap-1">
              <span>{{ stats.litrosDieselEconomizados }}</span>
              <span class="text-xs font-normal text-slate-400 font-sans">Litros</span>
            </div>
          </div>
          <div class="w-10 h-10 rounded-xl bg-sky-500/10 border border-sky-500/20 flex items-center justify-center text-sky-400 shrink-0">
            <Fuel class="w-5 h-5" />
          </div>
        </div>
        <div class="mt-4 pt-3 border-t border-white/5 flex items-center justify-between text-[11px] text-slate-400">
          <span>Geração Solar/LED:</span>
          <span class="font-mono text-sky-300 font-bold tabular-nums">{{ stats.economiaGeradaKWh }} kWh</span>
        </div>
      </div>

      <!-- Card 4: Redução de CO2 -->
      <div class="tactile-panel rounded-2xl p-4 flex flex-col justify-between">
        <div class="flex justify-between items-start">
          <div>
            <span class="text-[11px] font-bold text-slate-400 uppercase tracking-wider block font-mono">Pegada de Carbono</span>
            <div class="text-2xl font-bold font-mono text-emerald-400 mt-2 tabular-nums flex items-baseline gap-1">
              <span>{{ stats.reducaoCO2Kg }}</span>
              <span class="text-xs font-normal text-slate-400 font-sans">kg CO₂</span>
            </div>
          </div>
          <div class="w-10 h-10 rounded-xl bg-emerald-500/10 border border-emerald-500/20 flex items-center justify-center text-emerald-400 shrink-0">
            <Leaf class="w-5 h-5" />
          </div>
        </div>
        <div class="mt-4 pt-3 border-t border-white/5 flex items-center justify-between text-[11px] text-slate-400">
          <span>Eficiência Ambiental:</span>
          <span class="text-emerald-400 font-semibold flex items-center gap-1 font-mono text-[10px]">
            <span class="w-1.5 h-1.5 rounded-full bg-emerald-400"></span> 100% RENOVÁVEL
          </span>
        </div>
      </div>
    </div>

    <!-- Seção de Gráficos Analíticos -->
    <div class="grid grid-cols-1 lg:grid-cols-3 gap-5">
      <!-- Gráfico 1: Status da Frota -->
      <div class="tactile-panel rounded-2xl p-5 flex flex-col">
        <div class="flex justify-between items-center mb-4 pb-2 border-b border-white/5">
          <span class="text-xs font-bold text-white uppercase tracking-wider font-mono">Estado Atual da Frota</span>
          <span class="text-[10px] font-mono text-slate-500">DISTRIBUIÇÃO</span>
        </div>
        <div class="relative flex-1 min-h-[220px] flex items-center justify-center">
          <canvas ref="statusChartCanvas"></canvas>
        </div>
      </div>

      <!-- Gráfico 2: Atividade por Setor -->
      <div class="tactile-panel rounded-2xl p-5 flex flex-col">
        <div class="flex justify-between items-center mb-4 pb-2 border-b border-white/5">
          <span class="text-xs font-bold text-white uppercase tracking-wider font-mono">Torres por Frente da Mina</span>
          <span class="text-[10px] font-mono text-slate-500">LOCALIZAÇÃO</span>
        </div>
        <div class="relative flex-1 min-h-[220px]">
          <canvas ref="sectorChartCanvas"></canvas>
        </div>
      </div>

      <!-- Gráfico 3: Matriz Energética -->
      <div class="tactile-panel rounded-2xl p-5 flex flex-col">
        <div class="flex justify-between items-center mb-4 pb-2 border-b border-white/5">
          <span class="text-xs font-bold text-white uppercase tracking-wider font-mono">Fontes de Alimentação</span>
          <span class="text-[10px] font-mono text-slate-500">MATRIZ</span>
        </div>
        <div class="relative flex-1 min-h-[220px] flex items-center justify-center">
          <canvas ref="powerChartCanvas"></canvas>
        </div>
      </div>
    </div>

    <!-- Ocorrências em Campo & Horímetro -->
    <div class="tactile-panel rounded-2xl p-5">
      <div class="flex items-center justify-between mb-4 pb-3 border-b border-white/5">
        <div class="flex items-center gap-2.5">
          <div class="w-8 h-8 rounded-lg bg-amber-500/10 border border-amber-500/20 flex items-center justify-center text-amber-400">
            <TriangleAlert class="w-4 h-4" />
          </div>
          <div>
            <h2 class="text-xs font-bold text-white uppercase tracking-wider font-mono">
              Alertas Recentes da Frota
            </h2>
            <p class="text-[11px] text-slate-400">Cerração, bateria crítica e vencimento de revisão preventiva</p>
          </div>
        </div>
        <span class="text-xs font-mono text-slate-400 bg-slate-900 px-2.5 py-1 rounded-lg border border-white/5">
          {{ stats.alertasRecentes?.length || 0 }} registros
        </span>
      </div>

      <div v-if="stats.alertasRecentes && stats.alertasRecentes.length > 0" class="divide-y divide-white/5">
        <div
          v-for="(alerta, idx) in stats.alertasRecentes"
          :key="idx"
          class="py-3 flex items-center justify-between text-xs hover:bg-white/[0.02] px-2 rounded-xl transition-colors"
        >
          <div class="flex items-center gap-3">
            <span
              class="font-mono font-bold text-xs px-2.5 py-1 rounded-lg border"
              :class="alerta.nivelGravidade === 'CRÍTICO' ? 'bg-rose-500/10 text-rose-400 border-rose-500/30' : 'bg-amber-500/10 text-amber-400 border-amber-500/30'"
            >
              {{ alerta.codigoTorre }}
            </span>
            <div>
              <span class="text-slate-200 font-semibold text-xs">{{ alerta.tipoAlerta }}</span>
              <span class="text-[10px] text-slate-500 font-mono ml-2">Severidade: {{ alerta.nivelGravidade }}</span>
            </div>
          </div>
          <span class="font-mono text-slate-400 text-xs tabular-nums">{{ alerta.dataHora }}</span>
        </div>
      </div>
      <div v-else class="text-center py-8 text-slate-500 text-xs font-mono">
        Nenhuma anomalia ativa na frota. Todas as torres operando nos parâmetros nominais.
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue';
import api from '../services/api';
import { RefreshCw, RadioTower, Zap, Fuel, Leaf, TriangleAlert } from 'lucide-vue-next';
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
          backgroundColor: ['#10b981', '#f59e0b', '#64748b', '#ef4444'],
          borderWidth: 2,
          borderColor: '#0b111d',
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: {
            position: 'bottom',
            labels: {
              boxWidth: 10,
              padding: 12,
              font: { size: 10, family: 'Plus Jakarta Sans', weight: '600' },
              color: '#94a3b8'
            }
          }
        },
        cutout: '72%'
      }
    });
  }

  // Chart 2: Setores Bar Chart
  if (sectorChartCanvas.value) {
    if (sectorChartInstance) sectorChartInstance.destroy();

    const sectors = stats.value.distribuicaoSetores || [];
    sectorChartInstance = new Chart(sectorChartCanvas.value, {
      type: 'bar',
      data: {
        labels: sectors.map(s => s.setorNome),
        datasets: [{
          label: 'Torres na Frente',
          data: sectors.map(s => s.quantidadeTorres),
          backgroundColor: '#38bdf8',
          borderRadius: 6,
          borderSkipped: false
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: { display: false }
        },
        scales: {
          x: {
            grid: { display: false },
            ticks: { color: '#94a3b8', font: { size: 10 } }
          },
          y: {
            grid: { color: 'rgba(255, 255, 255, 0.05)' },
            ticks: { color: '#94a3b8', stepSize: 1, font: { size: 10 } }
          }
        }
      }
    });
  }

  // Chart 3: Power Matrix Doughnut
  if (powerChartCanvas.value) {
    if (powerChartInstance) powerChartInstance.destroy();

    const powerSources = stats.value.distribuicaoFonteEnergia || {};
    powerChartInstance = new Chart(powerChartCanvas.value, {
      type: 'doughnut',
      data: {
        labels: Object.keys(powerSources),
        datasets: [{
          data: Object.values(powerSources),
          backgroundColor: ['#f59e0b', '#10b981', '#6366f1', '#64748b'],
          borderWidth: 2,
          borderColor: '#0b111d',
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: {
            position: 'bottom',
            labels: {
              boxWidth: 10,
              padding: 12,
              font: { size: 10, family: 'Plus Jakarta Sans', weight: '600' },
              color: '#94a3b8'
            }
          }
        },
        cutout: '68%'
      }
    });
  }
};

onMounted(() => {
  fetchStats();
});
</script>
