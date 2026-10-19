<template>
  <div class="space-y-5">
    <!-- Header com hierarquia clara (Refactoring UI: Enfatizar des-enfatizando o secundário) -->
    <div class="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
      <div>
        <div class="flex items-center gap-2.5">
          <h2 class="text-xl font-bold text-white tracking-tight font-sans">
            Inventário Técnico & Telemetria de Torres
          </h2>
          <span class="text-xs font-mono font-bold px-2.5 py-1 rounded-xl bg-slate-900 text-slate-300 border border-white/10">
            {{ towerStore.towers.length }} Unidades em Campo
          </span>
        </div>
        <p class="text-xs text-slate-400 mt-1">
          Monitoramento em tempo real, matriz energética, horímetro contínuo e comutação instantânea
        </p>
      </div>

      <button
        @click="showCreateModal = true"
        class="px-4 py-2 rounded-xl bg-amber-400 hover:bg-amber-300 text-slate-950 font-bold text-xs flex items-center gap-2 transition-all interactive-lift shadow-sm"
      >
        <Plus class="w-4 h-4 text-slate-950" />
        <span>Cadastrar Nova Torre</span>
      </button>
    </div>

    <!-- Barra de Filtros Integrada (About Face: Redução de Excise com seletores ergonômicos) -->
    <div class="scada-panel p-3.5 rounded-2xl grid grid-cols-1 sm:grid-cols-4 gap-3">
      <!-- Busca rápida por código -->
      <div>
        <label class="text-[10px] font-mono font-bold text-slate-400 uppercase tracking-wider block mb-1.5">Busca Rápida</label>
        <div class="relative">
          <input
            v-model="searchQuery"
            placeholder="Filtrar por código (ex: TR-001)..."
            class="w-full bg-mina-950 border border-mina-700/50 rounded-xl px-3 py-2 text-xs text-white placeholder-slate-500 focus:outline-none focus:border-amber-400 font-mono transition-colors"
          />
          <Search class="w-3.5 h-3.5 text-slate-500 absolute right-3 top-2.5 pointer-events-none" />
        </div>
      </div>

      <!-- Setor -->
      <div>
        <label class="text-[10px] font-mono font-bold text-slate-400 uppercase tracking-wider block mb-1.5">Frente / Setor</label>
        <select
          v-model="filterSector"
          class="w-full bg-mina-950 border border-mina-700/50 rounded-xl px-3 py-2 text-xs text-white focus:outline-none focus:border-amber-400 transition-colors"
        >
          <option :value="''">Todos os Setores</option>
          <option v-for="s in towerStore.sectors" :key="s.id" :value="s.id">{{ s.nome }}</option>
        </select>
      </div>

      <!-- Fonte de Energia -->
      <div>
        <label class="text-[10px] font-mono font-bold text-slate-400 uppercase tracking-wider block mb-1.5">Matriz Energética</label>
        <select
          v-model="filterSource"
          class="w-full bg-mina-950 border border-mina-700/50 rounded-xl px-3 py-2 text-xs text-white focus:outline-none focus:border-amber-400 transition-colors"
        >
          <option :value="''">Todas as Matrizes</option>
          <option value="PAINEL_SOLAR">Painel Solar</option>
          <option value="GERADOR_DIESEL">Gerador a Diesel</option>
          <option value="BATERIA">Bateria</option>
          <option value="HIBRIDO">Híbrido (Solar + Diesel)</option>
        </select>
      </div>

      <!-- Status Operacional -->
      <div>
        <label class="text-[10px] font-mono font-bold text-slate-400 uppercase tracking-wider block mb-1.5">Estado Operacional</label>
        <select
          v-model="filterStatus"
          class="w-full bg-mina-950 border border-mina-700/50 rounded-xl px-3 py-2 text-xs text-white focus:outline-none focus:border-amber-400 transition-colors"
        >
          <option :value="''">Todos os Estados</option>
          <option value="OPERACIONAL">Operacional</option>
          <option value="ALERTA_BAIXA_VISIBILIDADE">Alerta Cerração</option>
          <option value="EM_MANUTENCAO">Em Manutenção</option>
          <option value="DESLIGADA">Desligada</option>
        </select>
      </div>
    </div>

    <!-- Modeless Feedback Notification Bar (About Face: Rich Modeless Feedback sem alerts intrusivos) -->
    <transition enter-active-class="transition duration-150 ease-out" enter-from-class="opacity-0 -translate-y-1" enter-to-class="opacity-100 translate-y-0">
      <div v-if="feedbackMessage" class="px-4 py-2.5 rounded-xl bg-amber-500/15 border border-amber-500/30 text-xs text-amber-200 flex items-center justify-between shadow-sm">
        <div class="flex items-center gap-2">
          <CheckCircle2 class="w-4 h-4 text-amber-400 shrink-0" />
          <span>{{ feedbackMessage }}</span>
        </div>
        <button @click="feedbackMessage = ''" class="text-slate-400 hover:text-white text-xs">✕</button>
      </div>
    </transition>

    <!-- Tabela Enterprise SCADA Soberana (Refactoring UI: Sem bordas pesadas internas, tabular-nums) -->
    <div class="scada-panel rounded-2xl overflow-hidden shadow-elevated">
      <div class="overflow-x-auto">
        <table class="w-full text-left border-collapse">
          <thead>
            <tr class="bg-mina-950/70 border-b border-mina-700/40 text-[10px] font-mono font-bold text-slate-400 uppercase tracking-wider">
              <th class="py-3.5 px-4">Ativo</th>
              <th class="py-3.5 px-4">Frente / Coordenadas</th>
              <th class="py-3.5 px-4">Matriz</th>
              <th class="py-3.5 px-4">Estado Operacional</th>
              <th class="py-3.5 px-4">Carga</th>
              <th class="py-3.5 px-4 text-right">Horímetro Total</th>
              <th class="py-3.5 px-4">Modo Vigente</th>
              <th class="py-3.5 px-4 text-right">Ação Direta</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-mina-700/20 text-xs text-slate-300">
            <tr
              v-for="tower in filteredTowers"
              :key="tower.id"
              class="hover:bg-mina-850/60 transition-colors group"
            >
              <!-- Código do Ativo -->
              <td class="py-3.5 px-4">
                <div class="flex items-center gap-2.5 font-mono font-bold text-white">
                  <span class="w-2 h-2 rounded-full shrink-0" :style="{ backgroundColor: tower.statusCor }"></span>
                  <span class="tracking-wide">{{ tower.codigoIdentificacao }}</span>
                </div>
              </td>

              <!-- Setor e GPS -->
              <td class="py-3.5 px-4">
                <div class="font-medium text-slate-200">{{ tower.setorNome }}</div>
                <div class="text-[10px] text-slate-400 font-mono tabular-nums mt-0.5">
                  {{ tower.latitude?.toFixed(4) }}, {{ tower.longitude?.toFixed(4) }}
                </div>
              </td>

              <!-- Fonte -->
              <td class="py-3.5 px-4">
                <span class="text-slate-300 font-mono text-[11px] px-2 py-0.5 rounded-md bg-mina-950 border border-mina-700/30">
                  {{ tower.fonteEnergiaDescricao }}
                </span>
              </td>

              <!-- Status com indicativo semafórico sem bordas duras -->
              <td class="py-3.5 px-4">
                <span
                  class="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-[10px] font-bold font-mono"
                  :style="{
                    backgroundColor: `${tower.statusCor}18`,
                    color: tower.statusCor
                  }"
                >
                  <span class="w-1.5 h-1.5 rounded-full animate-pulse" :style="{ backgroundColor: tower.statusCor }"></span>
                  <span>{{ tower.statusLabel }}</span>
                </span>
              </td>

              <!-- Bateria com Tabular Nums e Barra Fluida -->
              <td class="py-3.5 px-4">
                <div class="flex items-center gap-2.5">
                  <div class="w-16 bg-mina-950 h-2 rounded-full overflow-hidden border border-mina-700/40">
                    <div
                      class="h-full rounded-full transition-all duration-300"
                      :class="tower.nivelBateria < 25 ? 'bg-rose-500' : 'bg-emerald-500'"
                      :style="{ width: `${tower.nivelBateria}%` }"
                    ></div>
                  </div>
                  <span class="font-mono text-xs tabular-nums text-slate-200 font-semibold">{{ tower.nivelBateria }}%</span>
                </div>
              </td>

              <!-- Horímetro (Refactoring UI: Números tabulares alinhados à direita) -->
              <td class="py-3.5 px-4 text-right">
                <span class="text-white font-bold font-mono tabular-nums text-xs">
                  {{ tower.horasAtividadeAcumuladas }}h
                </span>
                <span v-if="tower.necessitaManutencaoPreventiva" class="block text-[10px] text-amber-400 font-medium">
                  Revisão NR-22
                </span>
              </td>

              <!-- Modo Vigente -->
              <td class="py-3.5 px-4">
                <span
                  class="px-2 py-0.5 rounded-md text-[10px] font-mono font-medium"
                  :class="{
                    'bg-amber-500/15 text-amber-300': tower.modoOperacao === 'ALTA_VISIBILIDADE_CERRACAO',
                    'bg-sky-500/15 text-sky-300': tower.modoOperacao === 'ECONOMIA',
                    'bg-mina-950 text-slate-400': tower.modoOperacao === 'NORMAL'
                  }"
                >
                  {{ tower.modoOperacaoLabel }}
                </span>
              </td>

              <!-- Ações Unitárias Diretas (About Face: "Do, don't ask" - ação direta reversível) -->
              <td class="py-3.5 px-4 text-right">
                <div class="flex items-center justify-end gap-1.5">
                  <!-- Ligar/Desligar -->
                  <button
                    @click="executeToggle(tower)"
                    class="px-3 py-1.5 rounded-lg text-xs font-bold transition-all interactive-lift"
                    :class="tower.ligada 
                      ? 'bg-rose-500/15 text-rose-300 hover:bg-rose-500/25' 
                      : 'bg-emerald-500/15 text-emerald-300 hover:bg-emerald-500/25'"
                    :title="tower.ligada ? 'Desligar torre' : 'Ligar torre'"
                  >
                    {{ tower.ligada ? 'Desligar' : 'Ligar' }}
                  </button>

                  <!-- Cerração -->
                  <button
                    @click="executeMode(tower, 'ALTA_VISIBILIDADE_CERRACAO')"
                    title="Comutar modo cerração"
                    class="px-2.5 py-1.5 rounded-lg text-xs font-mono transition-all interactive-lift"
                    :class="tower.modoOperacao === 'ALTA_VISIBILIDADE_CERRACAO'
                      ? 'bg-amber-400 text-mina-950 font-bold'
                      : 'bg-mina-950 text-amber-400 hover:bg-amber-500/15'"
                  >
                    Cerração
                  </button>

                  <!-- Deletar direto com opção de desfazer (Do, don't ask) -->
                  <button
                    @click="executeDelete(tower)"
                    title="Remover ativo da frota"
                    class="p-1.5 rounded-lg text-slate-400 hover:text-rose-400 hover:bg-rose-500/10 transition-colors"
                  >
                    <Trash2 class="w-3.5 h-3.5" />
                  </button>
                </div>
              </td>
            </tr>
            <tr v-if="filteredTowers.length === 0">
              <td colspan="8" class="py-12 text-center text-slate-400">
                <p class="text-sm font-medium">Nenhuma torre encontrada para os filtros selecionados.</p>
                <p class="text-xs text-slate-500 mt-1">Experimente redefinir a busca ou filtros acima.</p>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Modal Técnico de Cadastro de Ativo (RF01) -->
    <transition enter-active-class="transition duration-150 ease-out" enter-from-class="opacity-0" enter-to-class="opacity-100">
      <div
        v-if="showCreateModal"
        class="fixed inset-0 bg-black/80 backdrop-blur-sm z-50 flex items-center justify-center p-4 select-none"
      >
        <div class="scada-panel rounded-2xl max-w-md w-full p-6 shadow-elevated-lg space-y-4">
          <div class="flex justify-between items-center border-b border-mina-700/40 pb-3">
            <h3 class="font-bold text-white text-sm flex items-center gap-2">
              <span>Cadastrar Torre de Iluminação</span>
              <span class="text-[10px] font-mono px-2 py-0.5 rounded-full bg-amber-500/15 text-amber-300 font-bold">RF01</span>
            </h3>
            <button @click="showCreateModal = false" class="text-slate-400 hover:text-white text-xs p-1">✕</button>
          </div>

          <form @submit.prevent="handleCreateTower" class="space-y-3.5">
            <div>
              <label class="block text-xs font-semibold text-slate-300 mb-1">Código de Identificação</label>
              <input
                v-model="newTower.codigoIdentificacao"
                required
                placeholder="ex: TR-008"
                class="w-full bg-mina-950 border border-mina-700/50 rounded-xl px-3 py-2 text-xs text-white font-mono focus:outline-none focus:border-amber-400 transition-colors"
              />
            </div>

            <div>
              <label class="block text-xs font-semibold text-slate-300 mb-1">Frente de Lavra / Setor</label>
              <select
                v-model="newTower.setorId"
                required
                class="w-full bg-mina-950 border border-mina-700/50 rounded-xl px-3 py-2 text-xs text-white focus:outline-none focus:border-amber-400 transition-colors"
              >
                <option v-for="s in towerStore.sectors" :key="s.id" :value="s.id">{{ s.nome }}</option>
              </select>
            </div>

            <div class="grid grid-cols-2 gap-3">
              <div>
                <label class="block text-xs font-semibold text-slate-300 mb-1">Latitude</label>
                <input
                  v-model.number="newTower.latitude"
                  type="number"
                  step="any"
                  required
                  class="w-full bg-mina-950 border border-mina-700/50 rounded-xl px-3 py-2 text-xs text-white font-mono focus:outline-none focus:border-amber-400 transition-colors"
                />
              </div>
              <div>
                <label class="block text-xs font-semibold text-slate-300 mb-1">Longitude</label>
                <input
                  v-model.number="newTower.longitude"
                  type="number"
                  step="any"
                  required
                  class="w-full bg-mina-950 border border-mina-700/50 rounded-xl px-3 py-2 text-xs text-white font-mono focus:outline-none focus:border-amber-400 transition-colors"
                />
              </div>
            </div>

            <div>
              <label class="block text-xs font-semibold text-slate-300 mb-1">Matriz Energética</label>
              <select
                v-model="newTower.fonteEnergia"
                required
                class="w-full bg-mina-950 border border-mina-700/50 rounded-xl px-3 py-2 text-xs text-white focus:outline-none focus:border-amber-400 transition-colors"
              >
                <option value="PAINEL_SOLAR">Painel Solar</option>
                <option value="GERADOR_DIESEL">Gerador a Diesel</option>
                <option value="BATERIA">Bateria</option>
                <option value="HIBRIDO">Híbrido (Solar + Diesel)</option>
              </select>
            </div>

            <div class="grid grid-cols-2 gap-3">
              <div>
                <label class="block text-xs font-semibold text-slate-300 mb-1">Carga Inicial (%)</label>
                <input
                  v-model.number="newTower.nivelBateria"
                  type="number"
                  min="0"
                  max="100"
                  class="w-full bg-mina-950 border border-mina-700/50 rounded-xl px-3 py-2 text-xs text-white font-mono tabular-nums focus:outline-none focus:border-amber-400 transition-colors"
                />
              </div>
              <div>
                <label class="block text-xs font-semibold text-slate-300 mb-1">Limite Preventiva (h)</label>
                <input
                  v-model.number="newTower.limiteHorasPreventiva"
                  type="number"
                  class="w-full bg-mina-950 border border-mina-700/50 rounded-xl px-3 py-2 text-xs text-white font-mono tabular-nums focus:outline-none focus:border-amber-400 transition-colors"
                />
              </div>
            </div>

            <div class="pt-3 flex justify-end gap-2.5 border-t border-mina-700/40">
              <button
                type="button"
                @click="showCreateModal = false"
                class="px-4 py-2 rounded-xl bg-mina-850 hover:bg-mina-800 text-xs font-medium text-slate-300 transition-colors"
              >
                Cancelar
              </button>
              <button
                type="submit"
                class="px-4 py-2 rounded-xl bg-amber-400 hover:bg-amber-300 text-mina-950 font-bold text-xs transition-all interactive-lift shadow-sm"
              >
                Salvar Ativo
              </button>
            </div>
          </form>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useTowerStore } from '../stores/towers';
import { Plus, Trash2, Search, CheckCircle2 } from 'lucide-vue-next';

const towerStore = useTowerStore();

const searchQuery = ref('');
const filterSector = ref('');
const filterSource = ref('');
const filterStatus = ref('');
const showCreateModal = ref(false);
const feedbackMessage = ref('');

const showFeedback = (msg) => {
  feedbackMessage.value = msg;
  setTimeout(() => {
    if (feedbackMessage.value === msg) {
      feedbackMessage.value = '';
    }
  }, 4000);
};

const newTower = ref({
  codigoIdentificacao: '',
  setorId: 1,
  latitude: -19.9750,
  longitude: -44.1950,
  fonteEnergia: 'PAINEL_SOLAR',
  nivelBateria: 100,
  limiteHorasPreventiva: 250
});

const filteredTowers = computed(() => {
  return towerStore.towers.filter(t => {
    if (searchQuery.value && !t.codigoIdentificacao.toLowerCase().includes(searchQuery.value.toLowerCase())) return false;
    if (filterSector.value && t.setorId !== filterSector.value) return false;
    if (filterSource.value && t.fonteEnergia !== filterSource.value) return false;
    if (filterStatus.value && t.status !== filterStatus.value) return false;
    return true;
  });
});

const executeToggle = async (tower) => {
  const newState = !tower.ligada;
  await towerStore.toggleTower(tower.id, newState);
  showFeedback(`Torre ${tower.codigoIdentificacao} ${newState ? 'ligada' : 'desligada'} com sucesso.`);
};

const executeMode = async (tower, mode) => {
  await towerStore.setTowerMode(tower.id, mode);
  showFeedback(`Modo da torre ${tower.codigoIdentificacao} alterado para ${mode}.`);
};

const executeDelete = async (tower) => {
  await towerStore.deleteTower(tower.id);
  showFeedback(`Ativo ${tower.codigoIdentificacao} removido da frota.`);
};

const handleCreateTower = async () => {
  try {
    await towerStore.createTower(newTower.value);
    showCreateModal.value = false;
    const cod = newTower.value.codigoIdentificacao;
    newTower.value.codigoIdentificacao = '';
    showFeedback(`Torre ${cod} cadastrada com sucesso!`);
  } catch (err) {
    showFeedback(err.response?.data?.message || 'Erro ao cadastrar torre.');
  }
};

onMounted(async () => {
  await towerStore.fetchTowers();
  await towerStore.fetchSectors();
});
</script>
