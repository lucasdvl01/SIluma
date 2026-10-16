<template>
  <div class="space-y-5">
    <!-- Header Page -->
    <div class="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
      <div>
        <h2 class="text-xl font-bold text-white tracking-tight flex items-center gap-2">
          <span>Painel de Indicadores Operacionais & Eficiência</span>
          <span class="text-[10px] font-mono px-2 py-0.5 rounded bg-[#1e293b] text-slate-300 border border-[#334155]">
            RF05
          </span>
        </h2>
        <p class="text-xs text-slate-400">
          Monitoramento consolidado de consumo, autonomia e disponibilidade da frota de torres
        </p>
      </div>

      <div class="flex items-center gap-2">
        <button
          @click="fetchStats"
          :disabled="loading"
          class="px-3 py-1.5 rounded bg-[#0e1626] hover:bg-[#16233b] text-xs font-semibold text-slate-300 border border-[#1c273c] flex items-center gap-2 transition-colors"
        >
          <RefreshCw class="w-3.5 h-3.5" :class="{ 'animate-spin': loading }" />
          <span>Atualizar Métricas</span>
        </button>
      </div>
    </div>

    <!-- Metricas Principais CCO (RF05) -->
    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
      <!-- Card 1: Torres em Operação -->
      <div class="bg-[#0e1626] border border-[#1c273c] rounded-xl p-4">
        <div class="flex justify-between items-start">
          <div>
            <span class="text-[11px] font-medium text-slate-400 uppercase tracking-wider block">Torres Ativas no Campo</span>
            <div class="text-2xl font-bold font-mono text-white mt-1">
              {{ stats.torresOperacionais + stats.torresAlertaVisibilidade }}
              <span class="text-xs font-normal text-slate-500 font-sans">/ {{ stats.totalTorres }} total</span>
            </div>
          </div>
          <div class="w-8 h-8 rounded bg-emerald-500/10 border border-emerald-500/20 flex items-center justify-center text-emerald-400">
            <RadioTower class="w-4 h-4" />
          </div>
        </div>
        <div class="mt-3">
          <div class="flex justify-between text-[11px] text-slate-400 mb-1">
            <span>Disponibilidade Operacional</span>
            <span class="font-mono text-emerald-400 font-bold">
              {{ Math.round(((stats.torresOperacionais + stats.torresAlertaVisibilidade) / (stats.totalTorres || 1)) * 100) }}%
            </span>
          </div>
          <div class="w-full bg-[#162033] h-1.5 rounded-full overflow-hidden">
            <div
              class="bg-emerald-500 h-full rounded-full transition-all duration-300"
              :style="{ width: `${((stats.torresOperacionais + stats.torresAlertaVisibilidade) / (stats.totalTorres || 1)) * 100}%` }"
            ></div>
          </div>
        </div>
      </div>

      <!-- Card 2: Consumo Energético -->
      <div class="bg-[#0e1626] border border-[#1c273c] rounded-xl p-4">
        <div class="flex justify-between items-start">
          <div>
            <span class="text-[11px] font-medium text-slate-400 uppercase tracking-wider block">Consumo Global</span>
            <div class="text-2xl font-bold font-mono text-amber-400 mt-1">
              {{ stats.consumoTotalKWh }} <span class="text-xs font-normal text-slate-400 font-sans">kWh</span>
            </div>
          </div>
          <div class="w-8 h-8 rounded bg-amber-500/10 border border-amber-500/20 flex items-center justify-center text-amber-400">
            <Zap class="w-4 h-4" />
          </div>
        </div>
        <div class="mt-3 flex items-center justify-between text-[11px] text-slate-400 pt-1 border-t border-[#162033]">
          <span>Horímetro Consolidado:</span>
          <span class="font-mono text-slate-200 font-bold">{{ stats.totalHorasAtividade }} h</span>
        </div>
      </div>

      <!-- Card 3: Diesel Economizado -->
      <div class="bg-[#0e1626] border border-[#1c273c] rounded-xl p-4">
        <div class="flex justify-between items-start">
          <div>
            <span class="text-[11px] font-medium text-slate-400 uppercase tracking-wider block">Diesel Evitado</span>
            <div class="text-2xl font-bold font-mono text-sky-400 mt-1">
              {{ stats.litrosDieselEconomizados }} <span class="text-xs font-normal text-slate-400 font-sans">Litros</span>
            </div>
          </div>
          <div class="w-8 h-8 rounded bg-sky-500/10 border border-sky-500/20 flex items-center justify-center text-sky-400">
            <Fuel class="w-4 h-4" />
          </div>
        </div>
        <div class="mt-3 flex items-center justify-between text-[11px] text-slate-400 pt-1 border-t border-[#162033]">
          <span>Geração Sustentável:</span>
          <span class="font-mono text-sky-300 font-bold">{{ stats.economiaGeradaKWh }} kWh</span>
        </div>
      </div>

      <!-- Card 4: Redução de Emissões de CO2 -->
      <div class="bg-[#0e1626] border border-[#1c273c] rounded-xl p-4">
        <div class="flex justify-between items-start">
          <div>
            <span class="text-[11px] font-medium text-slate-400 uppercase tracking-wider block">Redução de CO₂</span>
            <div class="text-2xl font-bold font-mono text-emerald-400 mt-1">
              {{ stats.reducaoCO2Kg }} <span class="text-xs font-normal text-slate-400 font-sans">kg</span>
            </div>
          </div>
          <div class="w-8 h-8 rounded bg-emerald-500/10 border border-emerald-500/20 flex items-center justify-center text-emerald-400">
            <Leaf class="w-4 h-4" />
          </div>
        </div>
        <div class="mt-3 flex items-center justify-between text-[11px] text-slate-400 pt-1 border-t border-[#162033]">
          <span>Eficiência Ambiental:</span>
          <span class="text-emerald-400 font-bold">Painéis Solares</span>
        </div>
      </div>
    </div>

    <!-- Seção de Gráficos Técnicos (RF05) -->
    <div class="grid grid-cols-1 lg:grid-cols-3 gap-5">
      <!-- Gráfico 1: Status da Frota -->
      <div class="bg-[#0e1626] border border-[#1c273c] rounded-xl p-4 flex flex-col">
        <div class="flex justify-between items-center mb-3">
          <span class="text-xs font-bold text-white uppercase tracking-wider">Distribuição por Status</span>
          <span class="text-[10px] font-mono text-slate-400">SCADA CORES</span>
        </div>
        <div class="relative flex-1 min-h-[210px] flex items-center justify-center">
          <canvas ref="statusChartCanvas"></canvas>
        </div>
      </div>

      <!-- Gráfico 2: Atividade por Setor -->
      <div class="bg-[#0e1626] border border-[#1c273c] rounded-xl p-4 flex flex-col">
        <div class="flex justify-between items-center mb-3">
          <span class="text-xs font-bold text-white uppercase tracking-wider">Torres por Setor da Mina</span>
          <span class="text-[10px] font-mono text-slate-400">FRENTES</span>
        </div>
        <div class="relative flex-1 min-h-[210px]">
          <canvas ref="sectorChartCanvas"></canvas>
        </div>
      </div>

      <!-- Gráfico 3: Matriz Energética -->
      <div class="bg-[#0e1626] border border-[#1c273c] rounded-xl p-4 flex flex-col">
        <div class="flex justify-between items-center mb-3">
          <span class="text-xs font-bold text-white uppercase tracking-wider">Matriz de Fontes</span>
          <span class="text-[10px] font-mono text-slate-400">ENERGIA</span>
        </div>
        <div class="relative flex-1 min-h-[210px] flex items-center justify-center">
          <canvas ref="powerChartCanvas"></canvas>
        </div>
      </div>
    </div>

    <!-- Feed de Ocorrências e Alertas Preventivos (RF02, RF04) -->
    <div class="bg-[#0e1626] border border-[#1c273c] rounded-xl p-4">
      <div class="flex items-center justify-between mb-3 border-b border-[#1c273c] pb-2.5">
        <div class="flex items-center gap-2">
          <AlertTriangle class="w-4 h-4 text-amber-500" />
          <span class="text-xs font-bold text-white uppercase tracking-wider">
            Ocorrências em Campo & Horímetro Preventivo
          </span>
        </div>
        <span class="text-[11px] font-mono text-slate-400">
          {{ stats.alertasRecentes?.length || 0 }} registros ativos
        </span>
      </div>

      <div v-if="stats.alertasRecentes && stats.alertasRecentes.length > 0" class="divide-y divide-[#162033]">
        <div
          v-for="(alerta, idx) in stats.alertasRecentes"
          :key="idx"
          class="py-2.5 flex items-center justify-between text-xs"
        >
          <div class="flex items-center gap-3">
            <span
              class="font-mono font-bold text-[11px] px-2 py-0.5 rounded border"
              :class="alerta.nivelGravidade === 'CRÍTICO' ? 'bg-red-500/10 text-red-400 border-red-500/30' : 'bg-amber-500/10 text-amber-400 border-amber-500/30'"
            >
              {{ alerta.codigoTorre }}
            </span>
            <div>
              <span class="text-slate-200 font-medium">{{ alerta.tipoAlerta }}</span>
              <span class="text-[10px] text-slate-500 font-mono ml-2">Nível: {{ alerta.nivelGravidade }}</span>
            </div>
          </div>
          <span class="font-mono text-slate-500 text-[10px]">{{ alerta.dataHora }}</span>
        </div>
      </div>
      <div v-else class="text-center py-4 text-slate-500 text-xs font-mono">
        Nenhuma ocorrência crítica ativa no momento.
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue';
import api from '../services/api';
import { RefreshCw, RadioTower, Zap, Fuel, Leaf, AlertTriangle } from 'lucide-vue-next';
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
          borderWidth: 1,
          borderColor: '#0e1626',
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: {
            position: 'bottom',
            labels: { color: '#94a3b8', font: { size: 10 }, boxWidth: 8 }
          }
        },
        cutout: '72%'
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
            label: 'Ativas',
            data: ativasData,
            backgroundColor: '#f59e0b',
            borderRadius: 3
          },
          {
            label: 'Total',
            data: totalData,
            backgroundColor: '#1e293b',
            borderRadius: 3
          }
        ]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        scales: {
          x: { ticks: { color: '#94a3b8', font: { size: 9 } }, grid: { display: false } },
          y: { ticks: { color: '#94a3b8', stepSize: 1 }, grid: { color: '#162033' } }
        },
        plugins: {
          legend: {
            position: 'bottom',
            labels: { color: '#94a3b8', font: { size: 10 }, boxWidth: 8 }
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
          borderWidth: 1,
          borderColor: '#0e1626',
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: {
            position: 'bottom',
            labels: { color: '#94a3b8', font: { size: 10 }, boxWidth: 8 }
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
