<template>
  <div class="h-[calc(100vh-7.5rem)] flex flex-col space-y-3">
    <!-- Barra Superior de Controle Georreferenciado -->
    <div class="flex flex-wrap items-center justify-between gap-3 bg-[#0e1626] border border-[#1c273c] px-4 py-2.5 rounded-xl">
      <div class="flex items-center gap-4">
        <div class="flex items-center gap-2">
          <MapPin class="w-4 h-4 text-emerald-400" />
          <h3 class="text-xs font-bold text-white uppercase tracking-wider">
            Posicionamento Geográfico da Frota
          </h3>
          <span class="text-[9px] font-mono px-1.5 py-0.2 rounded bg-[#162033] text-slate-300 border border-[#22314d]">RF08</span>
        </div>

        <!-- Legenda de Cores Semafóricas (RF02) -->
        <div class="hidden sm:flex items-center gap-3 text-xs pl-4 border-l border-[#1c273c]">
          <span class="flex items-center gap-1.5"><span class="w-2 h-2 rounded-full bg-emerald-500"></span> Operacional</span>
          <span class="flex items-center gap-1.5"><span class="w-2 h-2 rounded-full bg-amber-500 animate-pulse"></span> Alerta Cerração</span>
          <span class="flex items-center gap-1.5"><span class="w-2 h-2 rounded-full bg-rose-500"></span> Manutenção</span>
          <span class="flex items-center gap-1.5"><span class="w-2 h-2 rounded-full bg-slate-500"></span> Desligada</span>
        </div>
      </div>

      <!-- Filtro por Setor da Mina -->
      <div class="flex items-center gap-2">
        <select
          v-model="selectedSectorId"
          class="bg-[#080d17] border border-[#1e2a40] rounded-lg px-2.5 py-1 text-xs text-white focus:outline-none focus:border-amber-500"
        >
          <option :value="null">Todas as Frentes da Mina</option>
          <option v-for="s in towerStore.sectors" :key="s.id" :value="s.id">
            {{ s.nome }}
          </option>
        </select>

        <button
          @click="resetView"
          class="px-2.5 py-1 rounded-lg bg-[#080d17] hover:bg-[#162033] text-xs font-medium text-slate-300 border border-[#1e2a40] transition-colors"
        >
          Centralizar Cava
        </button>
      </div>
    </div>

    <!-- Container do Mapa e Drawer Lateral -->
    <div class="flex-1 relative rounded-xl overflow-hidden border border-[#1c273c] flex">
      <!-- Mapa Leaflet -->
      <div id="map-container" class="flex-1 h-full w-full z-0"></div>

      <!-- Drawer de Telemetria e Controle Remoto Imediato (RF03) -->
      <div
        v-if="selectedTower"
        class="absolute top-3 right-3 w-80 bg-[#0e1626]/95 border border-[#1c273c] backdrop-blur rounded-xl shadow-2xl p-4 z-[500] space-y-4"
      >
        <div class="flex items-center justify-between border-b border-[#1c273c] pb-2.5">
          <div class="flex items-center gap-2">
            <span
              class="w-2.5 h-2.5 rounded-full"
              :style="{ backgroundColor: selectedTower.statusCor }"
            ></span>
            <div>
              <h4 class="text-sm font-bold text-white font-mono leading-none">
                {{ selectedTower.codigoIdentificacao }}
              </h4>
              <p class="text-[10px] text-slate-400 mt-0.5">{{ selectedTower.setorNome }}</p>
            </div>
          </div>
          <button
            @click="selectedTower = null"
            class="p-1 rounded hover:bg-[#162033] text-slate-400 hover:text-white text-xs"
          >
            ✕
          </button>
        </div>

        <!-- Telemetria Técnica da Torre -->
        <div class="grid grid-cols-2 gap-2 text-xs">
          <div class="p-2 rounded bg-[#080d17] border border-[#162033]">
            <span class="text-[10px] text-slate-400 block font-mono">ESTADO</span>
            <span class="font-bold text-white text-xs">{{ selectedTower.statusLabel }}</span>
          </div>
          <div class="p-2 rounded bg-[#080d17] border border-[#162033]">
            <span class="text-[10px] text-slate-400 block font-mono">MODO LUZ</span>
            <span class="font-bold text-amber-400 text-xs">{{ selectedTower.modoOperacaoLabel }}</span>
          </div>
          <div class="p-2 rounded bg-[#080d17] border border-[#162033]">
            <span class="text-[10px] text-slate-400 block font-mono">CARGA BATERIA</span>
            <span class="font-bold text-emerald-400 font-mono text-xs">{{ selectedTower.nivelBateria }}%</span>
          </div>
          <div class="p-2 rounded bg-[#080d17] border border-[#162033]">
            <span class="text-[10px] text-slate-400 block font-mono">HORÍMETRO TOTAL</span>
            <span class="font-bold text-sky-400 font-mono text-xs">{{ selectedTower.horasAtividadeAcumuladas }} h</span>
          </div>
        </div>

        <!-- Alerta de Horímetro Preventiva -->
        <div
          v-if="selectedTower.necessitaManutencaoPreventiva"
          class="p-2 bg-amber-500/10 border border-amber-500/30 rounded text-xs text-amber-300 font-medium flex items-center gap-1.5"
        >
          <AlertTriangle class="w-3.5 h-3.5 text-amber-400 shrink-0" />
          <span>Atingiu {{ selectedTower.horasAtividadeAcumuladas }}h / limite preventiva ({{ selectedTower.limiteHorasPreventiva }}h)!</span>
        </div>

        <!-- Painel de Acionamento Remoto (RF03) -->
        <div class="space-y-2 pt-2 border-t border-[#1c273c]">
          <span class="text-[10px] font-mono font-bold text-slate-400 uppercase tracking-wider block">Acionamento Operacional (RF03)</span>
          <div class="grid grid-cols-2 gap-2">
            <button
              @click="toggleTowerPower(selectedTower, true)"
              :disabled="selectedTower.ligada"
              class="py-2 rounded bg-emerald-600 hover:bg-emerald-500 disabled:opacity-40 text-white font-bold text-xs transition-colors flex items-center justify-center gap-1.5"
            >
              Ligar
            </button>
            <button
              @click="toggleTowerPower(selectedTower, false)"
              :disabled="!selectedTower.ligada"
              class="py-2 rounded bg-rose-600 hover:bg-rose-500 disabled:opacity-40 text-white font-bold text-xs transition-colors flex items-center justify-center gap-1.5"
            >
              Desligar
            </button>
          </div>

          <span class="text-[10px] font-mono font-bold text-slate-400 uppercase tracking-wider block pt-1.5">Ajuste de Modo</span>
          <div class="grid grid-cols-3 gap-1.5">
            <button
              @click="changeMode(selectedTower, 'NORMAL')"
              class="py-1.5 text-[11px] font-medium rounded border transition-colors text-center"
              :class="selectedTower.modoOperacao === 'NORMAL' ? 'bg-amber-500 text-black border-amber-400 font-bold' : 'bg-[#080d17] text-slate-300 border-[#1e2a40] hover:bg-[#162033]'"
            >
              Normal
            </button>
            <button
              @click="changeMode(selectedTower, 'ECONOMIA')"
              class="py-1.5 text-[11px] font-medium rounded border transition-colors text-center"
              :class="selectedTower.modoOperacao === 'ECONOMIA' ? 'bg-sky-500 text-black border-sky-400 font-bold' : 'bg-[#080d17] text-slate-300 border-[#1e2a40] hover:bg-[#162033]'"
            >
              Eco 50%
            </button>
            <button
              @click="changeMode(selectedTower, 'ALTA_VISIBILIDADE_CERRACAO')"
              class="py-1.5 text-[11px] font-medium rounded border transition-colors text-center"
              :class="selectedTower.modoOperacao === 'ALTA_VISIBILIDADE_CERRACAO' ? 'bg-amber-600 text-white border-amber-500 font-bold' : 'bg-[#080d17] text-slate-300 border-[#1e2a40] hover:bg-[#162033]'"
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
import { MapPin, AlertTriangle } from 'lucide-vue-next';
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

    // Marcador SVG industrial de precisão técnica
    const htmlIcon = `
      <div class="relative cursor-pointer flex flex-col items-center">
        ${isFog ? `<div class="absolute -top-1 w-6 h-6 rounded-full bg-amber-400/40 animate-ping"></div>` : ''}
        <div class="w-5 h-5 rounded-full border-2 border-[#0e1626] flex items-center justify-center shadow-md text-white font-mono font-bold text-[9px]" style="background-color: ${markerColor}">
          <div class="w-1.5 h-1.5 rounded-full bg-white"></div>
        </div>
        <div class="mt-0.5 px-1 py-0.2 rounded bg-[#0a0f1d] border border-[#1e2a40] text-slate-200 font-mono text-[9px] font-bold whitespace-nowrap">
          ${tower.codigoIdentificacao}
        </div>
      </div>
    `;

    const icon = L.divIcon({
      html: htmlIcon,
      className: 'custom-scada-marker',
      iconSize: [24, 28],
      iconAnchor: [12, 14]
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
