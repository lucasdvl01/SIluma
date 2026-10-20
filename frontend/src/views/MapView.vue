<template>
  <div class="flex h-[calc(100dvh-10rem)] min-h-[520px] flex-col space-y-4">
    <!-- Barra Superior de Controle Georreferenciado -->
    <header class="page-header flex flex-wrap items-start justify-between gap-4 !p-4 sm:!px-5 sm:!py-4">
      <div class="flex min-w-0 flex-col gap-3 xl:flex-row xl:items-center">
        <div class="flex items-center gap-3">
          <div class="flex h-10 w-10 shrink-0 items-center justify-center rounded-xl border border-emerald-500/20 bg-emerald-500/10 text-emerald-400">
            <MapPin class="w-4 h-4" />
          </div>
          <div class="min-w-0">
            <p class="page-eyebrow mb-1">Georreferenciamento</p>
            <h1 class="font-sans">Posicionamento geográfico da frota</h1>
            <p class="mt-1">Coordenadas das torres por frente da mina</p>
          </div>
        </div>

        <!-- Legenda de Cores Semafóricas -->
        <div class="hidden items-center gap-3.5 border-l border-white/5 pl-4 font-mono text-[11px] text-slate-500 sm:flex">
          <span class="flex items-center gap-1.5"><span class="h-2 w-2 rounded-full bg-emerald-400"></span> Operacional</span>
          <span class="flex items-center gap-1.5"><span class="h-2 w-2 rounded-full bg-amber-400"></span> Cerração</span>
          <span class="flex items-center gap-1.5"><span class="h-2 w-2 rounded-full bg-rose-500"></span> Manutenção</span>
          <span class="flex items-center gap-1.5"><span class="h-2 w-2 rounded-full bg-slate-500"></span> Desligada</span>
        </div>
      </div>

      <!-- Filtro por Setor da Mina -->
      <div class="flex items-center gap-2">
        <select
          v-model="selectedSectorId"
          class="bg-slate-950 border border-slate-700/60 rounded-xl px-3 py-1.5 text-xs text-white focus:outline-none focus:border-amber-400 font-sans"
        >
          <option :value="null">Todas as Frentes da Mina</option>
          <option v-for="s in towerStore.sectors" :key="s.id" :value="s.id">
            {{ s.nome }}
          </option>
        </select>

        <button
          @click="resetView"
          class="px-3 py-1.5 rounded-xl bg-slate-900 hover:bg-slate-800 text-xs font-semibold text-slate-300 border border-white/10 transition-all interactive-lift shadow-sm"
        >
          Centralizar Cava
        </button>
      </div>
    </header>

    <!-- Container do Mapa e Drawer Lateral -->
    <div class="relative flex min-h-[420px] flex-1">
      <div class="absolute inset-0 z-0 overflow-hidden rounded-2xl border border-white/10 shadow-2xl">
        <!-- Mapa Leaflet -->
        <div id="map-container" class="h-full w-full"></div>
      </div>

      <!-- Drawer de Telemetria e Controle Remoto Imediato -->
      <transition enter-active-class="transition duration-200 ease-out" enter-from-class="opacity-0 translate-x-4" enter-to-class="opacity-100 translate-x-0" leave-active-class="transition duration-150 ease-in" leave-from-class="opacity-100" leave-to-class="opacity-0">
        <div
          v-if="selectedTower"
          class="absolute inset-x-3 top-3 z-[500] max-h-[90%] space-y-4 overflow-y-auto overscroll-contain rounded-2xl border border-white/10 p-5 shadow-2xl backdrop-blur-xl tactile-panel sm:inset-x-auto sm:right-4 sm:top-4 sm:w-[21rem]"
          aria-label="Controles e dados da torre selecionada"
        >
          <div class="flex items-center justify-between border-b border-white/5 pb-3">
            <div class="flex items-center gap-2.5">
              <span
                class="w-3 h-3 rounded-full"
                :style="{ backgroundColor: selectedTower.statusCor }"
              ></span>
              <div>
                <h4 class="text-sm font-bold text-white font-mono leading-none tracking-wider">
                  {{ selectedTower.codigoIdentificacao }}
                </h4>
                <p class="text-[11px] text-slate-400 mt-1">{{ selectedTower.setorNome }}</p>
              </div>
            </div>
            <button
              @click="selectedTower = null"
              class="p-1 rounded-lg hover:bg-white/10 text-slate-400 hover:text-white text-xs transition-colors"
            >
              ✕
            </button>
          </div>

          <!-- Telemetria Técnica da Torre -->
          <div class="grid grid-cols-2 gap-2 text-xs">
            <div class="p-2.5 rounded-xl bg-slate-950/80 border border-white/5">
              <span class="text-[10px] text-slate-400 block font-mono">ESTADO</span>
              <span class="font-bold text-white text-xs mt-0.5 block">{{ selectedTower.statusLabel }}</span>
            </div>
            <div class="p-2.5 rounded-xl bg-slate-950/80 border border-white/5">
              <span class="text-[10px] text-slate-400 block font-mono">MODO DO FEIXE</span>
              <span class="font-bold text-amber-400 text-xs mt-0.5 block">{{ selectedTower.modoOperacaoLabel }}</span>
            </div>
            <div class="p-2.5 rounded-xl bg-slate-950/80 border border-white/5">
              <span class="text-[10px] text-slate-400 block font-mono">BATERIA</span>
              <span class="font-bold text-emerald-400 font-mono text-xs mt-0.5 block">{{ selectedTower.nivelBateria }}%</span>
            </div>
            <div class="p-2.5 rounded-xl bg-slate-950/80 border border-white/5">
              <span class="text-[10px] text-slate-400 block font-mono">HORÍMETRO</span>
              <span class="font-bold text-sky-400 font-mono text-xs mt-0.5 block">{{ selectedTower.horasAtividadeAcumuladas }} h</span>
            </div>
          </div>

          <!-- Alerta de Horímetro Preventivo -->
          <div
            v-if="selectedTower.necessitaManutencaoPreventiva"
            class="p-2.5 bg-amber-500/10 border border-amber-500/30 rounded-xl text-xs text-amber-300 font-medium flex items-center gap-2"
          >
            <TriangleAlert class="w-4 h-4 text-amber-400 shrink-0" />
            <span>Limite preventiva atingido ({{ selectedTower.limiteHorasPreventiva }}h)!</span>
          </div>

          <!-- Acionamento Inequívoco de Energia -->
          <div class="space-y-2 pt-2 border-t border-white/5">
            <div class="flex items-center justify-between">
              <span class="text-[11px] font-mono font-bold text-slate-400 uppercase tracking-wider">Luminária:</span>
              <span
                class="px-2 py-0.5 rounded text-[10px] font-mono font-bold"
                :class="selectedTower.ligada ? 'bg-emerald-500/20 text-emerald-300' : 'bg-slate-800 text-slate-400'"
              >
                {{ selectedTower.ligada ? 'ACESAS' : 'APAGADA' }}
              </span>
            </div>

            <div class="segmented-control w-full flex">
              <button
                @click="toggleTowerPower(selectedTower, false)"
                class="flex-1 py-1.5 rounded-lg text-xs font-mono font-bold transition-all text-center"
                :class="!selectedTower.ligada ? 'bg-slate-700 text-white shadow-sm' : 'text-slate-400 hover:text-slate-200'"
              >
                Apagada
              </button>
              <button
                @click="toggleTowerPower(selectedTower, true)"
                class="flex-1 py-1.5 rounded-lg text-xs font-mono font-bold transition-all text-center"
                :class="selectedTower.ligada ? 'bg-emerald-500 text-slate-950 shadow-sm font-extrabold' : 'text-slate-400 hover:text-emerald-300'"
              >
                Acesa
              </button>
            </div>

            <span class="text-[10px] font-mono font-bold text-slate-400 uppercase tracking-wider block pt-1.5">Intensidade & Cor</span>
            <div class="grid grid-cols-3 gap-1.5">
              <button
                @click="changeMode(selectedTower, 'NORMAL')"
                class="py-1.5 text-[11px] font-medium rounded-xl border transition-all text-center"
                :class="selectedTower.modoOperacao === 'NORMAL' ? 'bg-emerald-500/20 text-emerald-300 border-emerald-500/60 font-bold shadow-sm' : 'bg-slate-950 text-slate-400 border-white/5 hover:bg-slate-900'"
              >
                Normal
              </button>
              <button
                @click="changeMode(selectedTower, 'ECONOMIA')"
                class="py-1.5 text-[11px] font-medium rounded-xl border transition-all text-center"
                :class="selectedTower.modoOperacao === 'ECONOMIA' ? 'bg-sky-500/20 text-sky-300 border-sky-400 font-bold shadow-sm' : 'bg-slate-950 text-slate-400 border-white/5 hover:bg-slate-900'"
              >
                Eco
              </button>
              <button
                @click="changeMode(selectedTower, 'ALTA_VISIBILIDADE_CERRACAO')"
                class="py-1.5 text-[11px] font-medium rounded-xl border transition-all text-center"
                :class="selectedTower.modoOperacao === 'ALTA_VISIBILIDADE_CERRACAO' ? 'bg-amber-400 text-slate-950 border-amber-400 font-extrabold shadow-sm' : 'bg-slate-950 text-amber-400/80 border-white/5 hover:bg-slate-900'"
              >
                Cerração
              </button>
            </div>
          </div>
        </div>
      </transition>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch, onBeforeUnmount } from 'vue';
import { useTowerStore } from '../stores/towers';
import { MapPin, TriangleAlert } from 'lucide-vue-next';
import L from 'leaflet';

const towerStore = useTowerStore();
const selectedSectorId = ref(null);
const selectedTower = ref(null);

let map = null;
let markersLayer = null;

const filteredTowers = computed(() => {
  if (!selectedSectorId.value) return towerStore.towers;
  return towerStore.towers.filter(t => t.setorId === selectedSectorId.value);
});

const initMap = () => {
  const defaultCenter = [-19.9750, -44.1950];

  map = L.map('map-container', {
    zoomControl: true,
    attributionControl: false
  }).setView(defaultCenter, 15);

  // Satélite de Alta Precisão (Esri World Imagery)
  L.tileLayer('https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}', {
    maxZoom: 18
  }).addTo(map);

  markersLayer = L.layerGroup().addTo(map);
  renderMarkers();
};

const renderMarkers = () => {
  if (!markersLayer) return;
  markersLayer.clearLayers();

  filteredTowers.value.forEach(tower => {
    const isFog = tower.alertaBaixaVisibilidade || tower.modoOperacao === 'ALTA_VISIBILIDADE_CERRACAO';
    const isMaint = tower.status === 'EM_MANUTENCAO';
    const isOff = !tower.ligada;

    let markerColor = '#10b981'; // Verde operacional

    if (isMaint) {
      markerColor = '#ef4444'; // Vermelho
    } else if (isFog) {
      markerColor = '#f59e0b'; // Âmbar cerração
    } else if (isOff) {
      markerColor = '#64748b'; // Cinza
    }

    // Marcador com feixe de luz realista na cava
    const htmlIcon = `
      <div class="relative cursor-pointer flex flex-col items-center">
        ${tower.ligada ? `<div class="absolute -top-3.5 -left-3.5 w-14 h-14 rounded-full pointer-events-none opacity-45" style="background: radial-gradient(circle, ${markerColor} 0%, transparent 70%)"></div>` : ''}
        ${isFog ? `<div class="absolute -top-1 w-7 h-7 rounded-full bg-amber-400/40 animate-ping"></div>` : ''}
        <div class="w-6 h-6 rounded-full border-2 border-slate-950 flex items-center justify-center shadow-lg text-white font-mono font-bold text-[9px]" style="background-color: ${markerColor}">
          <div class="w-1.5 h-1.5 rounded-full bg-white"></div>
        </div>
        <div class="mt-1 px-1.5 py-0.5 rounded-md bg-slate-950/90 border border-white/10 text-slate-100 font-mono text-[9px] font-bold whitespace-nowrap shadow-md">
          ${tower.codigoIdentificacao}
        </div>
      </div>
    `;

    const icon = L.divIcon({
      html: htmlIcon,
      className: 'custom-scada-marker',
      iconSize: [28, 32],
      iconAnchor: [14, 16]
    });

    const marker = L.marker([tower.latitude, tower.longitude], { icon });
    marker.on('click', () => {
      selectedTower.value = tower;
    });

    markersLayer.addLayer(marker);
  });
};

const toggleTowerPower = async (tower, ligar) => {
  await towerStore.toggleTower(tower.id, ligar);
  const updated = towerStore.towers.find(t => t.id === tower.id);
  if (updated) selectedTower.value = updated;
};

const changeMode = async (tower, modo) => {
  await towerStore.setTowerMode(tower.id, modo);
  const updated = towerStore.towers.find(t => t.id === tower.id);
  if (updated) selectedTower.value = updated;
};

const resetView = () => {
  if (map) {
    map.setView([-19.9750, -44.1950], 15);
  }
};

watch(() => filteredTowers.value, () => {
  renderMarkers();
}, { deep: true });

onMounted(async () => {
  await towerStore.fetchTowers();
  await towerStore.fetchSectors();
  initMap();
  towerStore.startPolling(8000);
});

onBeforeUnmount(() => {
  towerStore.stopPolling();
  if (map) map.remove();
});
</script>

<style>
.custom-scada-marker {
  background: transparent !important;
  border: none !important;
}
</style>
