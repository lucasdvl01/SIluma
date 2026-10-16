<template>
  <div class="space-y-5 max-w-5xl mx-auto">
    <!-- Header -->
    <div>
      <h2 class="text-xl font-bold text-white tracking-tight flex items-center gap-2">
        <span>Controle e Comandos em Lote (Setores Remotos)</span>
        <span class="text-[10px] font-mono px-2 py-0.5 rounded bg-[#1e293b] text-slate-300 border border-[#334155]">
          RF09
        </span>
      </h2>
      <p class="text-xs text-slate-400">
        Disparo simultâneo de comandos operacionais para setores inteiros da mina (ex: ativação de modo cerração em trecho de escoamento)
      </p>
    </div>

    <!-- Etapa 1: Matriz de Seleção de Setor -->
    <div class="bg-[#0e1626] border border-[#1c273c] rounded-xl p-5 space-y-3">
      <div class="flex items-center justify-between border-b border-[#1c273c] pb-2.5">
        <span class="text-xs font-bold text-white uppercase tracking-wider flex items-center gap-2">
          <span class="w-5 h-5 rounded bg-amber-500 text-black flex items-center justify-center text-[11px] font-mono font-bold">1</span>
          Selecione o Setor ou Frente de Lavra
        </span>
        <span class="text-[11px] text-slate-400 font-mono">4 setores monitorados</span>
      </div>

      <div class="grid grid-cols-1 md:grid-cols-2 gap-3">
        <div
          v-for="sector in towerStore.sectors"
          :key="sector.id"
          @click="selectedSector = sector"
          class="p-3.5 rounded-lg border cursor-pointer transition-colors flex items-start justify-between"
          :class="selectedSector?.id === sector.id ? 'bg-[#152238] border-amber-500/70 text-white' : 'bg-[#080d17] border-[#1c273c] hover:bg-[#0f172a] text-slate-300'"
        >
          <div>
            <div class="flex items-center gap-2">
              <span class="text-[10px] font-mono font-bold px-1.5 py-0.2 rounded bg-[#162033] text-amber-400 border border-[#22314d]">
                {{ sector.codigo }}
              </span>
              <h4 class="font-bold text-xs text-white">{{ sector.nome }}</h4>
            </div>
            <p class="text-[11px] text-slate-400 mt-1">{{ sector.areaMina }} • {{ sector.frentesDeLavra }}</p>
          </div>

          <div class="text-right font-mono">
            <span class="text-xs font-bold text-slate-200 block">{{ getTowersCount(sector.id) }} torres</span>
            <span class="text-[10px] text-emerald-400">{{ getActiveTowersCount(sector.id) }} ativas</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Etapa 2: Painel de Ações Operacionais Simultâneas -->
    <div v-if="selectedSector" class="bg-[#0e1626] border border-[#1c273c] rounded-xl p-5 space-y-4">
      <div class="flex items-center justify-between border-b border-[#1c273c] pb-3">
        <div>
          <span class="text-xs font-bold text-white uppercase tracking-wider flex items-center gap-2">
            <span class="w-5 h-5 rounded bg-amber-500 text-black flex items-center justify-center text-[11px] font-mono font-bold">2</span>
            Ações em Lote para: <span class="text-amber-400 font-mono">{{ selectedSector.nome }}</span>
          </span>
          <p class="text-[11px] text-slate-400 mt-0.5">
            O comando será propagado para todas as {{ getTowersCount(selectedSector.id) }} torres ativas no trecho
          </p>
        </div>
      </div>

      <!-- Grid de Ações -->
      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-3">
        <!-- Modo Cerração em Lote -->
        <button
          @click="executeBatch('MODO_CERRACAO', 'ALTA_VISIBILIDADE_CERRACAO')"
          :disabled="executing"
          class="p-4 rounded-lg bg-amber-500/10 hover:bg-amber-500/20 border border-amber-500/30 text-left transition-colors flex flex-col justify-between disabled:opacity-50"
        >
          <div>
            <CloudFog class="w-5 h-5 text-amber-400 mb-2" />
            <h5 class="font-bold text-xs text-amber-300">Ativar Modo Cerração</h5>
            <p class="text-[10px] text-slate-400 mt-1">
              Potência máxima e foco contra neblina/poeira no trecho
            </p>
          </div>
          <span class="text-[10px] font-mono font-bold mt-3 uppercase tracking-wider text-amber-400">Disparar Lote →</span>
        </button>

        <!-- Ligar Todas -->
        <button
          @click="executeBatch('LIGAR_TODAS')"
          :disabled="executing"
          class="p-4 rounded-lg bg-emerald-500/10 hover:bg-emerald-500/20 border border-emerald-500/30 text-left transition-colors flex flex-col justify-between disabled:opacity-50"
        >
          <div>
            <Power class="w-5 h-5 text-emerald-400 mb-2" />
            <h5 class="font-bold text-xs text-emerald-300">Ligar Todas as Torres</h5>
            <p class="text-[10px] text-slate-400 mt-1">
              Aciona a iluminação de todas as torres do setor
            </p>
          </div>
          <span class="text-[10px] font-mono font-bold mt-3 uppercase tracking-wider text-emerald-400">Disparar Lote →</span>
        </button>

        <!-- Desligar Todas -->
        <button
          @click="executeBatch('DESLIGAR_TODAS')"
          :disabled="executing"
          class="p-4 rounded-lg bg-rose-500/10 hover:bg-rose-500/20 border border-rose-500/30 text-left transition-colors flex flex-col justify-between disabled:opacity-50"
        >
          <div>
            <PowerOff class="w-5 h-5 text-rose-400 mb-2" />
            <h5 class="font-bold text-xs text-rose-300">Desligar Todas</h5>
            <p class="text-[10px] text-slate-400 mt-1">
              Desliga e consolida horímetro de todas as torres
            </p>
          </div>
          <span class="text-[10px] font-mono font-bold mt-3 uppercase tracking-wider text-rose-400">Disparar Lote →</span>
        </button>

        <!-- Modo Eco -->
        <button
          @click="executeBatch('ALTERAR_MODO', 'ECONOMIA')"
          :disabled="executing"
          class="p-4 rounded-lg bg-sky-500/10 hover:bg-sky-500/20 border border-sky-500/30 text-left transition-colors flex flex-col justify-between disabled:opacity-50"
        >
          <div>
            <Leaf class="w-5 h-5 text-sky-400 mb-2" />
            <h5 class="font-bold text-xs text-sky-300">Modo Economia (50%)</h5>
            <p class="text-[10px] text-slate-400 mt-1">
              Reduz intensidade para economia de bateria
            </p>
          </div>
          <span class="text-[10px] font-mono font-bold mt-3 uppercase tracking-wider text-sky-400">Disparar Lote →</span>
        </button>
      </div>

      <!-- Feedback de Execução -->
      <div v-if="lastMessage" class="p-3 bg-emerald-500/10 border border-emerald-500/30 rounded-lg text-xs text-emerald-300 font-medium flex items-center gap-2">
        <CheckCircle2 class="w-4 h-4 text-emerald-400 shrink-0" />
        <span>{{ lastMessage }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useTowerStore } from '../stores/towers';
import { CloudFog, Power, PowerOff, Leaf, CheckCircle2 } from 'lucide-vue-next';

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
    lastMessage.value = `Comando em lote '${acao}' executado com sucesso em ${updated.length} torres do setor '${selectedSector.value.nome}'.`;
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
    selectedSector.value = sectors[1] || sectors[0];
  }
});
</script>
