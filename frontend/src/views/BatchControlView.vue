<template>
  <div class="space-y-6 max-w-5xl mx-auto">
    <!-- Header -->
    <header class="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 pb-4 border-b border-white/5">
      <div>
        <h1 class="text-xl font-bold text-white tracking-tight font-sans">
          Ações Coletivas por Frente da Mina
        </h1>
        <p class="text-xs text-slate-400 mt-0.5">
          Disparo simultâneo de comandos operacionais para setores inteiros da mina (ex: cerração coletiva em frentes de escoamento)
        </p>
      </div>

      <div class="flex items-center gap-2 px-3 py-1.5 rounded-xl bg-slate-900 border border-white/10 text-xs font-mono text-slate-400">
        <span>{{ towerStore.sectors.length }} Frentes Mapeadas</span>
      </div>
    </header>

    <!-- Feedback Modeless -->
    <transition enter-active-class="transition duration-150 ease-out" enter-from-class="opacity-0 -translate-y-2" enter-to-class="opacity-100 translate-y-0">
      <div v-if="lastMessage" class="p-3.5 bg-emerald-500/15 border border-emerald-500/30 rounded-2xl text-xs text-emerald-300 font-medium flex items-center justify-between shadow-lg">
        <div class="flex items-center gap-2.5">
          <CircleCheck class="w-4 h-4 text-emerald-400 shrink-0" />
          <span>{{ lastMessage }}</span>
        </div>
        <button @click="lastMessage = ''" class="text-emerald-400/80 hover:text-white p-1">✕</button>
      </div>
    </transition>

    <!-- Etapa 1: Seleção da Frente de Lavra -->
    <section class="tactile-panel rounded-2xl p-5 space-y-4">
      <div class="flex items-center justify-between border-b border-white/5 pb-3">
        <span class="text-xs font-bold text-white uppercase tracking-wider font-mono flex items-center gap-2.5">
          <span class="w-5 h-5 rounded-md bg-amber-400 text-slate-950 flex items-center justify-center text-[11px] font-bold">1</span>
          Selecione a Frente da Mina
        </span>
        <span class="text-[11px] text-slate-400 font-mono">Toque para selecionar</span>
      </div>

      <div class="grid grid-cols-1 md:grid-cols-2 gap-3.5">
        <div
          v-for="sector in towerStore.sectors"
          :key="sector.id"
          @click="selectedSector = sector"
          class="p-4 rounded-xl border cursor-pointer transition-all interactive-lift flex items-start justify-between"
          :class="selectedSector?.id === sector.id 
            ? 'bg-amber-500/10 border-amber-500/60 shadow-[0_0_15px_rgba(245,158,11,0.15)] text-white' 
            : 'bg-slate-900/60 border-white/5 hover:border-white/15 text-slate-300'"
        >
          <div>
            <div class="flex items-center gap-2">
              <span class="text-[10px] font-mono font-bold px-2 py-0.5 rounded-md bg-slate-800 text-amber-400 border border-slate-700">
                {{ sector.codigo }}
              </span>
              <h3 class="font-bold text-sm text-white">{{ sector.nome }}</h3>
            </div>
            <p class="text-xs text-slate-400 mt-1 font-sans">{{ sector.areaMina }} • {{ sector.frentesDeLavra }}</p>
          </div>

          <div class="text-right font-mono">
            <span class="text-xs font-bold text-slate-200 block">{{ getTowersCount(sector.id) }} torres</span>
            <span class="text-[11px] text-emerald-400 font-bold">{{ getActiveTowersCount(sector.id) }} acesas</span>
          </div>
        </div>
      </div>
    </section>

    <!-- Etapa 2: Ações Coletivas -->
    <section v-if="selectedSector" class="tactile-panel rounded-2xl p-5 space-y-4 animate-fadeIn">
      <div class="flex items-center justify-between border-b border-white/5 pb-3">
        <div>
          <span class="text-xs font-bold text-white uppercase tracking-wider font-mono flex items-center gap-2.5">
            <span class="w-5 h-5 rounded-md bg-amber-400 text-slate-950 flex items-center justify-center text-[11px] font-bold">2</span>
            Comandos Coletivos para: <span class="text-amber-400 font-mono">{{ selectedSector.nome }}</span>
          </span>
          <p class="text-[11px] text-slate-400 mt-0.5">
            O comando será enviado simultaneamente para as {{ getTowersCount(selectedSector.id) }} torres cadastradas nesta frente.
          </p>
        </div>
      </div>

      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-3.5 pt-1">
        <!-- Ligar Todas -->
        <button
          @click="executeBatch('LIGAR')"
          :disabled="executing"
          class="p-4 rounded-xl bg-emerald-500/10 hover:bg-emerald-500/20 border border-emerald-500/30 text-left transition-all interactive-lift flex flex-col justify-between disabled:opacity-50"
        >
          <div>
            <div class="w-9 h-9 rounded-lg bg-emerald-500/20 flex items-center justify-center text-emerald-400 mb-3">
              <Power class="w-5 h-5" />
            </div>
            <h4 class="font-bold text-xs text-emerald-300">Ligar Todas as Torres</h4>
            <p class="text-[11px] text-slate-400 mt-1">
              Ativa todas as torres com modo normal (80% fluxo luminoso)
            </p>
          </div>
          <span class="text-[10px] font-mono font-bold mt-4 uppercase tracking-wider text-emerald-400">Disparar Lote →</span>
        </button>

        <!-- Modo Cerração Geral -->
        <button
          @click="executeBatch('ALTERAR_MODO', 'ALTA_VISIBILIDADE_CERRACAO')"
          :disabled="executing"
          class="p-4 rounded-xl bg-amber-500/10 hover:bg-amber-500/20 border border-amber-500/40 text-left transition-all interactive-lift flex flex-col justify-between disabled:opacity-50 shadow-[0_0_12px_rgba(245,158,11,0.1)]"
        >
          <div>
            <div class="w-9 h-9 rounded-lg bg-amber-500/20 flex items-center justify-center text-amber-400 mb-3">
              <CloudFog class="w-5 h-5" />
            </div>
            <h4 class="font-bold text-xs text-amber-300">Modo Cerração Geral</h4>
            <p class="text-[11px] text-slate-400 mt-1">
              Comuta para feixe âmbar de penetração visual máxima
            </p>
          </div>
          <span class="text-[10px] font-mono font-bold mt-4 uppercase tracking-wider text-amber-400">Disparar Lote →</span>
        </button>

        <!-- Modo Eco -->
        <button
          @click="executeBatch('ALTERAR_MODO', 'ECONOMIA')"
          :disabled="executing"
          class="p-4 rounded-xl bg-sky-500/10 hover:bg-sky-500/20 border border-sky-500/30 text-left transition-all interactive-lift flex flex-col justify-between disabled:opacity-50"
        >
          <div>
            <div class="w-9 h-9 rounded-lg bg-sky-500/20 flex items-center justify-center text-sky-400 mb-3">
              <Leaf class="w-5 h-5" />
            </div>
            <h4 class="font-bold text-xs text-sky-300">Modo Economia (50%)</h4>
            <p class="text-[11px] text-slate-400 mt-1">
              Reduz potência para conservação prolongada de bateria
            </p>
          </div>
          <span class="text-[10px] font-mono font-bold mt-4 uppercase tracking-wider text-sky-400">Disparar Lote →</span>
        </button>

        <!-- Desligar Todas -->
        <button
          @click="executeBatch('DESLIGAR')"
          :disabled="executing"
          class="p-4 rounded-xl bg-rose-500/10 hover:bg-rose-500/20 border border-rose-500/30 text-left transition-all interactive-lift flex flex-col justify-between disabled:opacity-50"
        >
          <div>
            <div class="w-9 h-9 rounded-lg bg-rose-500/20 flex items-center justify-center text-rose-400 mb-3">
              <PowerOff class="w-5 h-5" />
            </div>
            <h4 class="font-bold text-xs text-rose-300">Desligar Todo o Setor</h4>
            <p class="text-[11px] text-slate-400 mt-1">
              Apaga e consolida horímetro de todas as torres
            </p>
          </div>
          <span class="text-[10px] font-mono font-bold mt-4 uppercase tracking-wider text-rose-400">Disparar Lote →</span>
        </button>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useTowerStore } from '../stores/towers';
import { CloudFog, Power, PowerOff, Leaf, CircleCheck } from 'lucide-vue-next';

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
    lastMessage.value = `Comando coletivo '${acao}' executado com sucesso em ${updated.length} torres do setor '${selectedSector.value.nome}'.`;
  } catch (err) {
    lastMessage.value = 'Falha ao processar comando em lote.';
  } finally {
    executing.value = false;
  }
};

onMounted(async () => {
  await towerStore.fetchTowers();
  await towerStore.fetchSectors();
  if (towerStore.sectors.length > 0) {
    selectedSector.value = towerStore.sectors[0];
  }
});
</script>
