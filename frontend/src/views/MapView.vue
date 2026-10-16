<template>
  <div class="h-[calc(100vh-8rem)] flex flex-col space-y-4">
    <!-- Top Filter Bar -->
    <div class="flex flex-wrap items-center justify-between gap-3 bg-mina-900 border border-mina-darkBorder p-3 rounded-2xl">
      <div class="flex items-center gap-3">
        <h3 class="text-sm font-extrabold text-white flex items-center gap-2">
          <span>Mapa Operacional da Mina</span>
          <span class="text-[10px] px-2 py-0.5 rounded bg-emerald-500/10 text-emerald-400 font-bold border border-emerald-500/30">RF08</span>
        </h3>
        <!-- Legenda Cores Semafóricas (RF02) -->
        <div class="hidden sm:flex items-center gap-3 text-xs pl-4 border-l border-mina-darkBorder">
          <span class="flex items-center gap-1.5"><span class="w-2.5 h-2.5 rounded-full bg-emerald-500"></span> Operacional</span>
          <span class="flex items-center gap-1.5"><span class="w-2.5 h-2.5 rounded-full bg-amber-500 animate-pulse"></span> Cerração / Alerta</span>
          <span class="flex items-center gap-1.5"><span class="w-2.5 h-2.5 rounded-full bg-red-500"></span> Manutenção</span>
          <span class="flex items-center gap-1.5"><span class="w-2.5 h-2.5 rounded-full bg-gray-500"></span> Desligada</span>
        </div>
      </div>

      <!-- Filtro por Setor -->
      <div class="flex items-center gap-2">
        <select
          v-model="selectedSectorId"
          class="bg-mina-800 border border-mina-darkBorder rounded-xl px-3 py-1.5 text-xs text-white focus:outline-none focus:border-amber-500"
        >
          <option :value="null">Todos os Setores da Mina</option>
          <option v-for="s in towerStore.sectors" :key="s.id" :value="s.id">
            {{ s.nome }}
          </option>
        </select>

        <button
          @click="resetView"
          class="px-3 py-1.5 rounded-xl bg-mina-800 hover:bg-mina-700 text-xs font-semibold text-slate-300 border border-mina-darkBorder transition-all"
        >
          Centralizar Mina
        </button>
      </div>
    </div>

    <!-- Map & Control Drawer Container -->
    <div class="flex-1 relative rounded-2xl overflow-hidden border border-mina-darkBorder shadow-2xl flex">
      <!-- Leaflet Map Container -->
      <div id="map-container" class="flex-1 h-full w-full z-0"></div>

      <!-- Quick Tower Control Drawer (quando uma torre é selecionada) -->
      <div
        v-if="selectedTower"
        class="absolute top-4 right-4 w-80 bg-mina-900/95 border border-mina-darkBorder backdrop-blur-xl rounded-2xl shadow-2xl p-5 z-[500] space-y-4 animate-in slide-in-from-right"
      >
        <div class="flex items-center justify-between border-b border-mina-darkBorder pb-3">
          <div>
            <h4 class="text-base font-black text-white flex items-center gap-2">
              {{ selectedTower.codigoIdentificacao }}
              <span
                class="w-2.5 h-2.5 rounded-full"
                :style="{ backgroundColor: selectedTower.statusCor }"
              ></span>
            </h4>
            <p class="text-[11px] text-slate-400">{{ selectedTower.setorNome }}</p>
          </div>
          <button
            @click="selectedTower = null"
            class="p-1 rounded-lg hover:bg-mina-800 text-slate-400 hover:text-white"
          >
            ✕
          </button>
        </div>

        <!-- Status Badges -->
        <div class="grid grid-cols-2 gap-2 text-xs">
          <div class="p-2.5 rounded-xl bg-mina-800/80 border border-mina-darkBorder">
            <span class="text-[10px] text-slate-400 block">Status Operacional</span>
            <span class="font-bold text-white">{{ selectedTower.statusLabel }}</span>
          </div>
          <div class="p-2.5 rounded-xl bg-mina-800/80 border border-mina-darkBorder">
            <span class="text-[10px] text-slate-400 block">Modo de Luz</span>
            <span class="font-bold text-amber-400">{{ selectedTower.modoOperacaoLabel }}</span>
          </div>
          <div class="p-2.5 rounded-xl bg-mina-800/80 border border-mina-darkBorder">
            <span class="text-[10px] text-slate-400 block">Bateria</span>
            <span class="font-bold text-emerald-400">{{ selectedTower.nivelBateria }}%</span>
          </div>
          <div class="p-2.5 rounded-xl bg-mina-800/80 border border-mina-darkBorder">
            <span class="text-[10px] text-slate-400 block">Horímetro</span>
            <span class="font-bold text-sky-400">{{ selectedTower.horasAtividadeAcumuladas }}h</span>
          </div>
        </div>

        <!-- Alerta de Horímetro Preventiva -->
        <div
          v-if="selectedTower.necessitaManutencaoPreventiva"
          class="p-2.5 bg-amber-500/15 border border-amber-500/30 rounded-xl text-xs text-amber-300 font-semibold flex items-center gap-2"
        >
          <span class="text-base">⚠️</span>
          <span>Atingiu {{ selectedTower.horasAtividadeAcumuladas }}h / limite preventiva ({{ selectedTower.limiteHorasPreventiva }}h)!</span>
        </div>

        <!-- Controles Operacionais Imediatos (RF03) -->
        <div class="space-y-2 pt-2 border-t border-mina-darkBorder">
          <label class="text-[11px] font-bold text-slate-400 uppercase tracking-wider block">Acionamento Remoto (RF03)</label>
          <div class="grid grid-cols-2 gap-2">
            <button
              @click="toggleTowerPower(selectedTower, true)"
              :disabled="selectedTower.ligada"
              class="py-2.5 rounded-xl bg-emerald-600 hover:bg-emerald-500 disabled:opacity-40 text-white font-extrabold text-xs transition-all flex items-center justify-center gap-1.5"
            >
              Ligar Luz
            </button>
            <button
              @click="toggleTowerPower(selectedTower, false)"
              :disabled="!selectedTower.ligada"
              class="py-2.5 rounded-xl bg-rose-600 hover:bg-rose-500 disabled:opacity-40 text-white font-extrabold text-xs transition-all flex items-center justify-center gap-1.5"
            >
              Desligar
            </button>
          </div>

          <label class="text-[11px] font-bold text-slate-400 uppercase tracking-wider block pt-2">Modo de Energia / Iluminação</label>
          <div class="grid grid-cols-3 gap-1.5">
            <button
              @click="changeMode(selectedTower, 'NORMAL')"
              class="py-2 px-1 text-[11px] font-bold rounded-lg border transition-all text-center"
              :class="selectedTower.modoOperacao === 'NORMAL' ? 'bg-amber-500 text-black border-amber-400' : 'bg-mina-800 text-slate-300 border-mina-darkBorder hover:bg-mina-700'"
            >
              Normal
            </button>
            <button
              @click="changeMode(selectedTower, 'ECONOMIA')"
              class="py-2 px-1 text-[11px] font-bold rounded-lg border transition-all text-center"
              :class="selectedTower.modoOperacao === 'ECONOMIA' ? 'bg-sky-500 text-black border-sky-400' : 'bg-mina-800 text-slate-300 border-mina-darkBorder hover:bg-mina-700'"
            >
              Eco (50%)
            </button>
            <button
              @click="changeMode(selectedTower, 'ALTA_VISIBILIDADE_CERRACAO')"
              class="py-2 px-1 text-[11px] font-bold rounded-lg border transition-all text-center"
              :class="selectedTower.modoOperacao === 'ALTA_VISIBILIDADE_CERRACAO' ? 'bg-amber-600 text-white border-amber-400 animate-pulse' : 'bg-mina-800 text-slate-300 border-mina-darkBorder hover:bg-mina-700'"
            >
              Cerração
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch, onBeforeUnmount } from 'vue';
import { useTowerStore } from '../stores/towers';
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
  // Coordenadas centrais da mina (Betim / MG)
  const defaultCenter = [-19.9750, -44.1950];

  map = L.map('map-container', {
    zoomControl: true,
    attributionControl: false
  }).setView(defaultCenter, 15);

  // Camada de Imagem de Satélite de Alta Resolução (Esri Satellite) para visual de mina a céu aberto
  L.tileLayer('https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}', {
    maxZoom: 18,
    attribution: 'Tiles &copy; Esri &mdash; Source: Esri, i-cubed, USDA, USGS, AEX, GeoEye, Getmapping, Aerogrid, IGN, IGP, UPR-EGP, and the GIS User Community'
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

    let markerColor = '#10b981'; // Verde normal
    let pulseClass = '';

    if (isMaint) {
      markerColor = '#ef4444'; // Vermelho
    } else if (isFog) {
      markerColor = '#f59e0b'; // Amarelo/Âmbar
      pulseClass = 'animate-ping';
    } else if (isOff) {
      markerColor = '#6b7280'; // Cinza
    }

    const htmlIcon = `
      <div class="relative cursor-pointer group flex items-center justify-center">
        ${isFog ? `<div class="absolute w-8 h-8 rounded-full bg-amber-400 opacity-60 ${pulseClass}"></div>` : ''}
        <div class="w-7 h-7 rounded-full border-2 border-white flex items-center justify-center shadow-lg text-[11px] font-black text-black z-10" style="background-color: ${markerColor}">
          💡
        </div>
        <div class="absolute -bottom-5 px-1.5 py-0.5 rounded bg-black/80 text-white font-mono text-[9px] font-bold border border-white/20 whitespace-nowrap">
          ${tower.codigoIdentificacao}
        </div>
      </div>
    `;

    const icon = L.divIcon({
      html: htmlIcon,
      className: 'custom-tower-marker',
      iconSize: [28, 28],
      iconAnchor: [14, 14]
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
  // Atualiza torre selecionada
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
.custom-tower-marker {
  background: transparent !important;
  border: none !important;
}
</style>
