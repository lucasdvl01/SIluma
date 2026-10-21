<template>
  <div class="max-w-[1600px] mx-auto space-y-5 sm:space-y-6 pb-6">
    <!-- Header Executivo CCO: Limpo, Autoritário e Sem Jargões de IA -->
    <header class="page-header flex flex-col sm:flex-row justify-between items-start sm:items-center gap-5">
      <div>
        <p class="page-eyebrow mb-2">Centro de controle operacional</p>
        <h1 class="font-sans">
          Painel de Controle & Indicadores da Frota
        </h1>
        <p class="mt-2">
          Estado da frota, alertas e estimativas operacionais das torres
        </p>
      </div>

      <div class="flex items-center gap-3">
        <!-- Badge de Conexão em Tempo Real -->
        <div
          role="status"
          aria-live="polite"
          class="flex items-center gap-2 rounded-xl border px-3 py-2 text-xs"
          :class="statusTone"
        >
          <span class="h-2 w-2 shrink-0 rounded-full" :class="statusDotTone"></span>
          <span class="text-[11px] font-medium">{{ statusLabel }}</span>
        </div>

        <button
          @click="fetchStats"
          :disabled="loading"
          class="interactive-lift inline-flex min-h-11 items-center gap-2 rounded-xl border border-white/10 bg-slate-900 px-3.5 py-1.5 text-xs font-semibold text-slate-200 shadow-sm transition-all hover:bg-slate-800 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-amber-400 disabled:cursor-wait disabled:opacity-50"
          :aria-label="loading ? 'Atualizando indicadores' : 'Atualizar indicadores da frota'"
        >
          <RefreshCw class="w-3.5 h-3.5 text-slate-400" :class="{ 'animate-spin': loading }" />
          <span>{{ loading ? 'Atualizando' : 'Atualizar' }}</span>
        </button>
      </div>
    </header>

    <div
      v-if="errorMessage"
      role="alert"
      class="flex flex-col gap-3 rounded-xl border border-rose-500/30 bg-rose-500/10 p-4 text-sm text-rose-200 sm:flex-row sm:items-center sm:justify-between"
    >
      <p>{{ hasLoaded ? 'Não foi possível atualizar. Os últimos dados carregados estão mantidos.' : 'Não foi possível carregar os indicadores. Verifique a conexão com o sistema.' }}</p>
      <button
        type="button"
        class="inline-flex min-h-11 shrink-0 items-center justify-center rounded-lg border border-rose-400/30 px-3 font-semibold transition-colors hover:bg-rose-400/10 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-rose-300"
        @click="fetchStats"
      >
        Tentar novamente
      </button>
    </div>

    <p v-if="hasLoaded && lastUpdatedAt" class="text-xs text-slate-400" aria-live="polite">
      Dados do painel atualizados às <time :datetime="lastUpdatedAt.toISOString()">{{ formattedLastUpdated }}</time>.
      Atualização automática a cada 10 segundos enquanto esta tela estiver aberta.
    </p>

    <!-- Métricas Principais CCO: Formato Industrial de Alta Hierarquia -->
    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
      <!-- Card 1: Torres em Operação -->
      <div class="tactile-panel rounded-2xl p-4 flex flex-col justify-between">
        <div class="flex justify-between items-start">
          <div>
            <span class="text-[11px] font-bold text-slate-300 uppercase tracking-wider block font-mono">Operacionais + alertas</span>
            <div class="text-2xl font-bold font-mono text-white mt-2 tabular-nums flex items-baseline gap-1.5">
              <span>{{ hasLoaded ? stats.torresOperacionais + stats.torresAlertaVisibilidade : '—' }}</span>
              <span class="text-xs font-medium text-slate-400 font-mono">/ {{ hasLoaded ? stats.totalTorres : '—' }} torres</span>
            </div>
          </div>
          <div class="w-10 h-10 rounded-xl bg-emerald-500/10 border border-emerald-500/20 flex items-center justify-center text-emerald-400 shrink-0">
            <RadioTower class="w-5 h-5" />
          </div>
        </div>
        <div class="mt-4 pt-3 border-t border-white/5">
          <div class="flex justify-between text-[11px] text-slate-400 mb-1.5">
            <span>Parte do total cadastrado</span>
            <span class="font-mono text-emerald-400 font-bold tabular-nums">
              {{ hasLoaded && operationalRate !== null ? `${operationalRate}%` : '—' }}
            </span>
          </div>
          <div class="w-full bg-slate-950 h-1.5 rounded-full overflow-hidden border border-white/5">
            <div
              class="bg-emerald-500 h-full rounded-full transition-all duration-500"
              :style="{ width: `${operationalRate ?? 0}%` }"
            ></div>
          </div>
        </div>
      </div>

      <!-- Card 2: Consumo Energético -->
      <div class="tactile-panel rounded-2xl p-4 flex flex-col justify-between">
        <div class="flex justify-between items-start">
          <div>
            <span class="text-[11px] font-bold text-slate-300 uppercase tracking-wider block font-mono">Consumo energético</span>
            <div class="text-2xl font-bold font-mono text-amber-400 mt-2 tabular-nums flex items-baseline gap-1">
              <span>{{ hasLoaded ? stats.consumoTotalKWh : '—' }}</span>
              <span class="text-xs font-normal text-slate-400 font-sans">kWh</span>
            </div>
          </div>
          <div class="w-10 h-10 rounded-xl bg-amber-500/10 border border-amber-500/20 flex items-center justify-center text-amber-400 shrink-0">
            <Zap class="w-5 h-5" />
          </div>
        </div>
        <div class="mt-4 pt-3 border-t border-white/5 flex items-center justify-between text-[11px] text-slate-400">
          <span>Horímetro Total:</span>
          <span class="font-mono text-slate-200 font-bold tabular-nums">{{ hasLoaded ? `${stats.totalHorasAtividade} h` : '—' }}</span>
        </div>
      </div>

      <!-- Card 3: Diesel Economizado -->
      <div class="tactile-panel rounded-2xl p-4 flex flex-col justify-between">
        <div class="flex justify-between items-start">
          <div>
            <span class="text-[11px] font-bold text-slate-300 uppercase tracking-wider block font-mono">Diesel estimado evitado</span>
            <div class="text-2xl font-bold font-mono text-sky-400 mt-2 tabular-nums flex items-baseline gap-1">
              <span>{{ hasLoaded ? stats.litrosDieselEconomizados : '—' }}</span>
              <span class="text-xs font-normal text-slate-300 font-sans">L</span>
            </div>
          </div>
          <div class="w-10 h-10 rounded-xl bg-sky-500/10 border border-sky-500/20 flex items-center justify-center text-sky-400 shrink-0">
            <Fuel class="w-5 h-5" />
          </div>
        </div>
        <div class="mt-4 pt-3 border-t border-white/5 flex items-center justify-between text-[11px] text-slate-400">
          <span>Energia equivalente estimada:</span>
          <span class="font-mono text-sky-300 font-bold tabular-nums">{{ hasLoaded ? `${stats.economiaGeradaKWh} kWh` : '—' }}</span>
        </div>
      </div>

      <!-- Card 4: Redução de CO2 -->
      <div class="tactile-panel rounded-2xl p-4 flex flex-col justify-between">
        <div class="flex justify-between items-start">
          <div>
            <span class="text-[11px] font-bold text-slate-300 uppercase tracking-wider block font-mono">CO₂ estimado evitado</span>
            <div class="text-2xl font-bold font-mono text-emerald-400 mt-2 tabular-nums flex items-baseline gap-1">
              <span>{{ hasLoaded ? stats.reducaoCO2Kg : '—' }}</span>
              <span class="text-xs font-normal text-slate-400 font-sans">kg CO₂</span>
            </div>
          </div>
          <div class="w-10 h-10 rounded-xl bg-emerald-500/10 border border-emerald-500/20 flex items-center justify-center text-emerald-400 shrink-0">
            <Leaf class="w-5 h-5" />
          </div>
        </div>
        <div class="mt-4 pt-3 border-t border-white/5 flex items-center justify-between text-[11px] text-slate-400">
          <span>Baseado em fatores médios</span>
          <span class="text-emerald-300 font-semibold font-mono text-[10px]">ESTIMATIVA</span>
        </div>
      </div>
    </div>

    <details class="rounded-xl border border-mina-700/50 bg-mina-900/70 px-4 py-3 text-sm text-slate-300">
      <summary class="min-h-11 cursor-pointer content-center font-semibold text-slate-100 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-amber-400">
        Como as estimativas de energia e emissões são calculadas
      </summary>
      <p class="mt-2 max-w-4xl leading-relaxed">
        Não são medições diretas. O consumo usa registros de atividade quando disponíveis e, sem registros, estima 1,2 kWh por hora acumulada da torre. A energia equivalente aplica fatores de 75% e da proporção de torres com fonte sustentável; diesel e CO₂ são então estimados com 3 kWh por litro e 2,68 kg de CO₂ por litro. Resultados reais dependem de telemetria e calibração dos equipamentos.
      </p>
    </details>

    <!-- Seção de Gráficos Analíticos -->
    <div class="grid grid-cols-1 lg:grid-cols-3 gap-5">
      <!-- Gráfico 1: Status da Frota -->
      <div class="tactile-panel rounded-2xl p-5 flex flex-col">
        <div class="flex justify-between items-center mb-4 pb-2 border-b border-white/5">
          <span class="text-xs font-bold text-white uppercase tracking-wider font-mono">Estado Atual da Frota</span>
          <span class="text-[10px] font-mono text-slate-500">DISTRIBUIÇÃO</span>
        </div>
        <div class="relative flex-1 min-h-[220px] flex items-center justify-center" role="img" aria-label="Distribuição atual das torres por estado">
          <canvas v-if="hasStatusData" ref="statusChartCanvas"></canvas>
          <p v-else class="max-w-xs text-center text-sm text-slate-300">
            {{ hasLoaded ? 'Ainda não há torres para mostrar neste gráfico.' : errorMessage ? 'Não foi possível carregar os dados.' : 'Carregando dados do painel…' }}
          </p>
        </div>
      </div>

      <!-- Gráfico 2: Atividade por Setor -->
      <div class="tactile-panel rounded-2xl p-5 flex flex-col">
        <div class="flex justify-between items-center mb-4 pb-2 border-b border-white/5">
          <span class="text-xs font-bold text-white uppercase tracking-wider font-mono">Torres por Frente da Mina</span>
          <span class="text-[10px] font-mono text-slate-500">LOCALIZAÇÃO</span>
        </div>
        <div class="relative flex-1 min-h-[220px]" role="img" aria-label="Quantidade de torres cadastradas em cada frente da mina">
          <canvas v-if="hasSectorData" ref="sectorChartCanvas"></canvas>
          <p v-else class="flex min-h-[220px] items-center justify-center text-center text-sm text-slate-300">
            {{ hasLoaded ? 'Não há frentes com torres cadastradas para mostrar.' : errorMessage ? 'Não foi possível carregar os dados.' : 'Carregando dados do painel…' }}
          </p>
        </div>
      </div>

      <!-- Gráfico 3: Matriz Energética -->
      <div class="tactile-panel rounded-2xl p-5 flex flex-col">
        <div class="flex justify-between items-center mb-4 pb-2 border-b border-white/5">
          <span class="text-xs font-bold text-white uppercase tracking-wider font-mono">Fontes de Alimentação</span>
          <span class="text-[10px] font-mono text-slate-500">MATRIZ</span>
        </div>
        <div class="relative flex-1 min-h-[220px] flex items-center justify-center" role="img" aria-label="Distribuição das torres por fonte de energia">
          <canvas v-if="hasPowerData" ref="powerChartCanvas"></canvas>
          <p v-else class="max-w-xs text-center text-sm text-slate-300">
            {{ hasLoaded ? 'Ainda não há fontes de energia cadastradas para mostrar.' : errorMessage ? 'Não foi possível carregar os dados.' : 'Carregando dados do painel…' }}
          </p>
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
        <span class="text-xs font-mono text-slate-300 bg-slate-900 px-2.5 py-1 rounded-lg border border-white/5">
        {{ hasLoaded ? `${stats.alertasRecentes?.length || 0} registros` : '— registros' }}
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
      <div v-else-if="hasLoaded" class="text-center py-8 text-sm text-slate-300">
        Nenhum alerta registrado pelos critérios monitorados no momento.
      </div>
      <div v-else class="text-center py-8 text-sm text-slate-300">
        {{ errorMessage ? 'Não foi possível carregar os alertas.' : 'Carregando alertas…' }}
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, onMounted, onUnmounted, nextTick, watch } from 'vue';
import api from '../services/api';
import { RefreshCw, RadioTower, Zap, Fuel, Leaf, TriangleAlert } from 'lucide-vue-next';
import Chart from 'chart.js/auto';
import { useThemeStore } from '../stores/theme';

const themeStore = useThemeStore();
const loading = ref(false);
const hasLoaded = ref(false);
const errorMessage = ref('');
const isOnline = ref(navigator.onLine);
const lastUpdatedAt = ref(null);
let pollingTimer = null;
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

const operationalRate = computed(() => {
  if (!hasLoaded.value || stats.value.totalTorres === 0) return null;
  return Math.min(100, Math.round(
    ((stats.value.torresOperacionais + stats.value.torresAlertaVisibilidade) / stats.value.totalTorres) * 100
  ));
});

const hasStatusData = computed(() =>
  Object.values(stats.value.distribuicaoStatus || {}).some(value => value > 0)
);
const hasSectorData = computed(() =>
  (stats.value.distribuicaoSetores || []).some(sector => sector.totalTorres > 0)
);
const hasPowerData = computed(() =>
  Object.values(stats.value.distribuicaoFonteEnergia || {}).some(value => value > 0)
);
const formattedLastUpdated = computed(() => lastUpdatedAt.value
  ? new Intl.DateTimeFormat('pt-BR', { hour: '2-digit', minute: '2-digit', second: '2-digit' }).format(lastUpdatedAt.value)
  : '');
const statusLabel = computed(() => {
  if (loading.value) return hasLoaded.value ? 'Atualizando dados' : 'Carregando dados';
  if (!isOnline.value) return 'Sem conexão de rede';
  if (errorMessage.value) return 'Dados desatualizados';
  return 'Dados carregados';
});
const statusTone = computed(() => {
  if (!isOnline.value || errorMessage.value) return 'border-amber-500/30 bg-amber-500/10';
  return 'border-emerald-500/20 bg-emerald-500/10';
});
const statusDotTone = computed(() => {
  if (!isOnline.value || errorMessage.value) return 'bg-amber-400';
  return loading.value ? 'animate-pulse bg-sky-400' : 'bg-emerald-400';
});

const fetchStats = async () => {
  if (loading.value) return;
  loading.value = true;
  errorMessage.value = '';
  try {
    const res = await api.get('/dashboard/stats');
    stats.value = res.data;
    hasLoaded.value = true;
    lastUpdatedAt.value = new Date();
    await nextTick();
    renderCharts();
  } catch (err) {
    console.error('Erro ao carregar dashboard:', err);
    errorMessage.value = 'A conexão com a API falhou.';
  } finally {
    loading.value = false;
  }
};

const renderCharts = () => {
  const chartTextColor = themeStore.isDark ? '#94a3b8' : '#64748b';
  const chartBorderColor = themeStore.isDark ? '#0b111d' : '#ffffff';
  const chartGridColor = themeStore.isDark ? 'rgba(255, 255, 255, 0.05)' : '#e2e8f0';

  // Chart 1: Status Doughnut
  if (!hasStatusData.value) {
    statusChartInstance?.destroy();
    statusChartInstance = null;
  }
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
          borderColor: chartBorderColor,
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
              color: chartTextColor
            }
          }
        },
        cutout: '72%'
      }
    });
  }

  // Chart 2: Setores Bar Chart
  if (!hasSectorData.value) {
    sectorChartInstance?.destroy();
    sectorChartInstance = null;
  }
  if (sectorChartCanvas.value) {
    if (sectorChartInstance) sectorChartInstance.destroy();

    const sectors = stats.value.distribuicaoSetores || [];
    sectorChartInstance = new Chart(sectorChartCanvas.value, {
      type: 'bar',
      data: {
        labels: sectors.map(s => s.setorNome),
        datasets: [{
          label: 'Torres na Frente',
          data: sectors.map(s => s.totalTorres),
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
            ticks: { color: chartTextColor, font: { size: 10 } }
          },
          y: {
            grid: { color: chartGridColor },
            ticks: { color: chartTextColor, stepSize: 1, font: { size: 10 } }
          }
        }
      }
    });
  }

  // Chart 3: Power Matrix Doughnut
  if (!hasPowerData.value) {
    powerChartInstance?.destroy();
    powerChartInstance = null;
  }
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
          borderColor: chartBorderColor,
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
              color: chartTextColor
            }
          }
        },
        cutout: '68%'
      }
    });
  }
};

const handleOnline = () => {
  isOnline.value = true;
  fetchStats();
};
const handleOffline = () => {
  isOnline.value = false;
};

onMounted(() => {
  window.addEventListener('online', handleOnline);
  window.addEventListener('offline', handleOffline);
  fetchStats();
  pollingTimer = window.setInterval(() => {
    if (document.visibilityState === 'visible' && navigator.onLine) fetchStats();
  }, 10000);
});

watch(() => themeStore.mode, async () => {
  await nextTick();
  renderCharts();
});

onUnmounted(() => {
  window.removeEventListener('online', handleOnline);
  window.removeEventListener('offline', handleOffline);
  if (pollingTimer !== null) window.clearInterval(pollingTimer);
  statusChartInstance?.destroy();
  sectorChartInstance?.destroy();
  powerChartInstance?.destroy();
});
</script>
