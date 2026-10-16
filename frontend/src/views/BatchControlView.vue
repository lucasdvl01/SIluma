<template>
  <div class="space-y-6 max-w-5xl mx-auto">
    <!-- Header -->
    <div>
      <h2 class="text-2xl font-black text-white tracking-tight flex items-center gap-2">
        <span>Comandos e Ações em Lote (Áreas Remotas)</span>
        <span class="text-xs px-2.5 py-0.5 rounded-full bg-cyan-500/10 text-cyan-400 border border-cyan-500/20 font-bold">
          RF09
        </span>
      </h2>
      <p class="text-xs text-slate-400">
        Agrupamento de torres por setor e acionamento operacional simultâneo (ex: ativar modo cerração para um trecho inteiro de estrada)
      </p>
    </div>

    <!-- Step 1: Seleção de Setor -->
    <div class="bg-mina-900 border border-mina-darkBorder rounded-2xl p-6 shadow-xl space-y-4">
      <h3 class="text-sm font-bold text-white flex items-center gap-2">
        <span class="w-6 h-6 rounded-lg bg-amber-500 text-black flex items-center justify-center text-xs font-black">1</span>
        Selecione o Setor ou Trecho Remoto da Mina
      </h3>

      <div class="grid grid-cols-1 md:grid-cols-2 gap-3">
        <div
          v-for="sector in towerStore.sectors"
          :key="sector.id"
          @click="selectedSector = sector"
          class="p-4 rounded-xl border cursor-pointer transition-all flex items-start justify-between"
          :class="selectedSector?.id === sector.id ? 'bg-amber-500/10 border-amber-500 text-white ring-1 ring-amber-500' : 'bg-mina-800/60 border-mina-darkBorder hover:bg-mina-800 text-slate-300'"
        >
          <div>
            <div class="flex items-center gap-2">
              <span class="text-xs font-mono font-bold px-2 py-0.5 rounded bg-mina-700 text-amber-400">
                {{ sector.codigo }}
              </span>
              <h4 class="font-bold text-sm text-white">{{ sector.nome }}</h4>
            </div>
            <p class="text-xs text-slate-400 mt-1">{{ sector.areaMina }} • {{ sector.frentesDeLavra }}</p>
          </div>

          <div class="text-right">
            <span class="text-xs font-black text-amber-400 block">{{ getTowersCount(sector.id) }} torres</span>
            <span class="text-[10px] text-slate-500">{{ getActiveTowersCount(sector.id) }} ligadas</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Step 2: Painel de Ações Rápidas em Lote -->
    <div v-if="selectedSector" class="bg-mina-900 border border-mina-darkBorder rounded-2xl p-6 shadow-xl space-y-6 animate-in fade-in">
      <div class="flex items-center justify-between border-b border-mina-darkBorder pb-4">
        <div>
          <h3 class="text-sm font-bold text-white flex items-center gap-2">
            <span class="w-6 h-6 rounded-lg bg-amber-500 text-black flex items-center justify-center text-xs font-black">2</span>
            Ações Simultâneas para: <span class="text-amber-400">{{ selectedSector.nome }}</span>
          </h3>
          <p class="text-xs text-slate-400 mt-0.5">
            O comando será propagado imediatamente para todas as {{ getTowersCount(selectedSector.id) }} torres alocadas nesta frente
          </p>
        </div>
      </div>

      <!-- Grid de Ações -->
      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <!-- Ação Principal: Modo Cerração em Todo o Trecho (RF09) -->
        <button
          @click="executeBatch('MODO_CERRACAO', 'ALTA_VISIBILIDADE_CERRACAO')"
          :disabled="executing"
          class="p-5 rounded-xl bg-gradient-to-br from-amber-600 to-amber-800 hover:from-amber-500 hover:to-amber-700 text-white text-left shadow-lg shadow-amber-500/20 transition-all flex flex-col justify-between group disabled:opacity-50"
        >
          <div>
            <div class="text-2xl mb-2">🌫️</div>
            <h5 class="font-black text-sm text-white group-hover:text-amber-200">Ativar Modo Cerração</h5>
            <p class="text-[11px] text-amber-100/70 mt-1">
              Potência máxima e foco contra neblina/poeira para todo o trecho
            </p>
          </div>
          <span class="text-[10px] font-bold mt-4 uppercase tracking-wider text-amber-200">Disparar Lote →</span>
        </button>

        <!-- Ligar Todas -->
        <button
          @click="executeBatch('LIGAR_TODAS')"
          :disabled="executing"
          class="p-5 rounded-xl bg-emerald-600/20 hover:bg-emerald-600/30 border border-emerald-500/40 text-left transition-all flex flex-col justify-between group disabled:opacity-50"
        >
          <div>
            <div class="text-2xl mb-2">💡</div>
            <h5 class="font-black text-sm text-emerald-400 group-hover:text-emerald-300">Ligar Todas as Torres</h5>
            <p class="text-[11px] text-slate-400 mt-1">
              Aciona a iluminação de todas as torres inativas do setor
            </p>
          </div>
          <span class="text-[10px] font-bold mt-4 uppercase tracking-wider text-emerald-400">Disparar Lote →</span>
        </button>

        <!-- Desligar Todas -->
        <button
          @click="executeBatch('DESLIGAR_TODAS')"
          :disabled="executing"
          class="p-5 rounded-xl bg-rose-600/20 hover:bg-rose-600/30 border border-rose-500/40 text-left transition-all flex flex-col justify-between group disabled:opacity-50"
        >
          <div>
            <div class="text-2xl mb-2">🛑</div>
            <h5 class="font-black text-sm text-rose-400 group-hover:text-rose-300">Desligar Todas</h5>
            <p class="text-[11px] text-slate-400 mt-1">
              Desliga e consolida horímetro de todas as torres do setor
            </p>
          </div>
          <span class="text-[10px] font-bold mt-4 uppercase tracking-wider text-rose-400">Disparar Lote →</span>
        </button>

        <!-- Modo Eco Setor -->
        <button
          @click="executeBatch('ALTERAR_MODO', 'ECONOMIA')"
          :disabled="executing"
          class="p-5 rounded-xl bg-sky-600/20 hover:bg-sky-600/30 border border-sky-500/40 text-left transition-all flex flex-col justify-between group disabled:opacity-50"
        >
          <div>
            <div class="text-2xl mb-2">🌱</div>
            <h5 class="font-black text-sm text-sky-400 group-hover:text-sky-300">Modo Economia (50%)</h5>
            <p class="text-[11px] text-slate-400 mt-1">
              Reduz intensidade das luminárias para economia de bateria
            </p>
          </div>
          <span class="text-[10px] font-bold mt-4 uppercase tracking-wider text-sky-400">Disparar Lote →</span>
        </button>
      </div>

      <!-- Feedback de Execução -->
      <div v-if="lastMessage" class="p-3.5 bg-emerald-500/15 border border-emerald-500/30 rounded-xl text-xs text-emerald-300 font-bold flex items-center gap-2">
        <span>✅</span>
        <span>{{ lastMessage }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useTowerStore } from '../stores/towers';

const towerStore = useTowerStore();
const selectedSector = ref(null);
const executing = ref(false);
const lastMessage = ref('');

const getTowersCount = (sectorId) => {
  return towerStore.towers.filter(t => t.setorId === sectorId).length;
};

const getActiveTowersCount = (sectorId) => {
  return towerStore.towers.filter(t => t.setorId === sectorId && t.ligada).length;
};

const executeBatch = async (acao, modo = null) => {
  if (!selectedSector.value) return;
  executing.value = true;
  lastMessage.value = '';

  try {
    const updated = await towerStore.executeBatchAction(selectedSector.value.id, acao, modo);
    lastMessage.value = `Comando em lote '${acao}' aplicado com sucesso a ${updated.length} torres do setor '${selectedSector.value.nome}'!`;
  } catch (err) {
    alert(err.response?.data?.message || 'Erro ao executar comando em lote.');
  } finally {
    executing.value = false;
  }
};

onMounted(async () => {
  await towerStore.fetchTowers();
  const sectors = await towerStore.fetchSectors();
  if (sectors && sectors.length > 0) {
    selectedSector.value = sectors[1] || sectors[0]; // Seleciona estrada por padrão
  }
});
</script>
