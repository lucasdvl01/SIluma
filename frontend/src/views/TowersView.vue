<template>
  <div class="space-y-4">
    <!-- Header -->
    <div class="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
      <div>
        <h2 class="text-xl font-bold text-white tracking-tight flex items-center gap-2">
          <span>Inventário Técnico & Telemetria de Torres</span>
          <span class="text-[10px] font-mono px-2 py-0.5 rounded bg-[#1e293b] text-slate-300 border border-[#334155]">
            RF01 • RF02 • RF03
          </span>
        </h2>
        <p class="text-xs text-slate-400">
          Cadastro de novos ativos, parâmetros elétricos e acionamento remoto unitário
        </p>
      </div>

      <button
        @click="showCreateModal = true"
        class="px-3.5 py-2 rounded-lg bg-amber-500 hover:bg-amber-400 text-black font-bold text-xs flex items-center gap-2 transition-colors"
      >
        <Plus class="w-4 h-4 text-black" />
        <span>Cadastrar Torre (RF01)</span>
      </button>
    </div>

    <!-- Barra de Filtros -->
    <div class="grid grid-cols-1 sm:grid-cols-3 gap-3 bg-[#0e1626] border border-[#1c273c] p-3 rounded-xl">
      <div>
        <label class="text-[10px] font-mono font-bold text-slate-400 uppercase tracking-wider block mb-1">Setor / Trecho</label>
        <select
          v-model="filterSector"
          class="w-full bg-[#080d17] border border-[#1e2a40] rounded-lg px-3 py-1.5 text-xs text-white focus:outline-none focus:border-amber-500"
        >
          <option :value="''">Todos os Setores</option>
          <option v-for="s in towerStore.sectors" :key="s.id" :value="s.id">{{ s.nome }}</option>
        </select>
      </div>

      <div>
        <label class="text-[10px] font-mono font-bold text-slate-400 uppercase tracking-wider block mb-1">Fonte de Energia</label>
        <select
          v-model="filterSource"
          class="w-full bg-[#080d17] border border-[#1e2a40] rounded-lg px-3 py-1.5 text-xs text-white focus:outline-none focus:border-amber-500"
        >
          <option :value="''">Todas as Fontes</option>
          <option value="PAINEL_SOLAR">Painel Solar</option>
          <option value="GERADOR_DIESEL">Gerador a Diesel</option>
          <option value="BATERIA">Bateria</option>
          <option value="HIBRIDO">Híbrido (Solar + Diesel)</option>
        </select>
      </div>

      <div>
        <label class="text-[10px] font-mono font-bold text-slate-400 uppercase tracking-wider block mb-1">Status Operacional</label>
        <select
          v-model="filterStatus"
          class="w-full bg-[#080d17] border border-[#1e2a40] rounded-lg px-3 py-1.5 text-xs text-white focus:outline-none focus:border-amber-500"
        >
          <option :value="''">Todos os Estados</option>
          <option value="OPERACIONAL">Operacional (Verde)</option>
          <option value="ALERTA_BAIXA_VISIBILIDADE">Alerta Cerração (Âmbar)</option>
          <option value="EM_MANUTENCAO">Em Manutenção (Vermelho)</option>
          <option value="DESLIGADA">Desligada (Cinza)</option>
        </select>
      </div>
    </div>

    <!-- Tabela Enterprise SCADA -->
    <div class="bg-[#0e1626] border border-[#1c273c] rounded-xl overflow-hidden shadow-lg">
      <div class="overflow-x-auto">
        <table class="w-full text-left border-collapse">
          <thead>
            <tr class="border-b border-[#1c273c] bg-[#090e1a] text-[10px] font-mono font-bold text-slate-400 uppercase tracking-wider">
              <th class="p-3">Código</th>
              <th class="p-3">Setor / Coordenadas</th>
              <th class="p-3">Matriz Energética</th>
              <th class="p-3">Estado</th>
              <th class="p-3">Bateria</th>
              <th class="p-3">Horímetro Total</th>
              <th class="p-3">Modo Vigente</th>
              <th class="p-3 text-right">Comandos</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-[#162033] text-xs text-slate-300">
            <tr
              v-for="tower in filteredTowers"
              :key="tower.id"
              class="hover:bg-[#121c2e] transition-colors"
            >
              <!-- Código -->
              <td class="p-3 font-mono font-bold text-white flex items-center gap-2">
                <span class="w-2 h-2 rounded-full" :style="{ backgroundColor: tower.statusCor }"></span>
                <span>{{ tower.codigoIdentificacao }}</span>
              </td>

              <!-- Setor e GPS -->
              <td class="p-3">
                <div class="font-medium text-slate-200">{{ tower.setorNome }}</div>
                <div class="text-[10px] text-slate-500 font-mono">{{ tower.latitude?.toFixed(4) }}, {{ tower.longitude?.toFixed(4) }}</div>
              </td>

              <!-- Fonte -->
              <td class="p-3">
                <span class="text-slate-300 font-mono text-[11px]">
                  {{ tower.fonteEnergiaDescricao }}
                </span>
              </td>

              <!-- Status com indicativo semafórico -->
              <td class="p-3">
                <span
                  class="inline-flex items-center gap-1.5 px-2 py-0.5 rounded text-[10px] font-bold font-mono border"
                  :style="{
                    backgroundColor: `${tower.statusCor}15`,
                    borderColor: `${tower.statusCor}40`,
                    color: tower.statusCor
                  }"
                >
                  <span class="w-1.5 h-1.5 rounded-full" :style="{ backgroundColor: tower.statusCor }"></span>
                  {{ tower.statusLabel }}
                </span>
              </td>

              <!-- Bateria -->
              <td class="p-3">
                <div class="flex items-center gap-2">
                  <div class="w-14 bg-[#162033] h-1.5 rounded-full overflow-hidden">
                    <div
                      class="h-full rounded-full transition-all"
                      :class="tower.nivelBateria < 25 ? 'bg-rose-500' : 'bg-emerald-500'"
                      :style="{ width: `${tower.nivelBateria}%` }"
                    ></div>
                  </div>
                  <span class="font-mono text-xs">{{ tower.nivelBateria }}%</span>
                </div>
              </td>

              <!-- Horímetro -->
              <td class="p-3 font-mono text-xs">
                <span class="text-white font-bold">{{ tower.horasAtividadeAcumuladas }} h</span>
                <span v-if="tower.necessitaManutencaoPreventiva" class="block text-[10px] text-amber-400 font-sans font-medium">
                  Revisão requerida
                </span>
              </td>

              <!-- Modo -->
              <td class="p-3 font-mono text-[11px]">
                <span
                  class="px-1.5 py-0.5 rounded text-[10px] border"
                  :class="{
                    'bg-amber-500/10 text-amber-400 border-amber-500/30': tower.modoOperacao === 'ALTA_VISIBILIDADE_CERRACAO',
                    'bg-sky-500/10 text-sky-400 border-sky-500/30': tower.modoOperacao === 'ECONOMIA',
                    'bg-[#162033] text-slate-300 border-[#22314d]': tower.modoOperacao === 'NORMAL'
                  }"
                >
                  {{ tower.modoOperacaoLabel }}
                </span>
              </td>

              <!-- Ações Unitárias -->
              <td class="p-3 text-right">
                <div class="flex items-center justify-end gap-1.5">
                  <button
                    @click="towerStore.toggleTower(tower.id, !tower.ligada)"
                    class="px-2.5 py-1 rounded text-xs font-semibold transition-colors"
                    :class="tower.ligada ? 'bg-rose-500/10 text-rose-400 border border-rose-500/30 hover:bg-rose-500/20' : 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/30 hover:bg-emerald-500/20'"
                  >
                    {{ tower.ligada ? 'Desligar' : 'Ligar' }}
                  </button>

                  <button
                    @click="towerStore.setTowerMode(tower.id, 'ALTA_VISIBILIDADE_CERRACAO')"
                    title="Ativar modo cerração"
                    class="px-2 py-1 rounded bg-amber-500/10 hover:bg-amber-500/20 text-amber-400 border border-amber-500/30 text-xs font-mono"
                  >
                    Cerração
                  </button>

                  <button
                    @click="confirmDelete(tower)"
                    title="Excluir ativo"
                    class="p-1 rounded text-slate-500 hover:text-rose-400 hover:bg-rose-500/10 transition-colors"
                  >
                    <Trash2 class="w-3.5 h-3.5" />
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Modal Técnico de Cadastro de Ativo (RF01) -->
    <div
      v-if="showCreateModal"
      class="fixed inset-0 bg-black/80 backdrop-blur-sm z-50 flex items-center justify-center p-4"
    >
      <div class="bg-[#0e1626] border border-[#1c273c] rounded-xl max-w-md w-full p-6 shadow-2xl space-y-4">
        <div class="flex justify-between items-center border-b border-[#1c273c] pb-3">
          <h3 class="font-bold text-white text-sm flex items-center gap-2">
            <span>Cadastrar Torre de Iluminação</span>
            <span class="text-[10px] font-mono px-1.5 py-0.2 rounded bg-amber-500/10 text-amber-400 border border-amber-500/30">RF01</span>
          </h3>
          <button @click="showCreateModal = false" class="text-slate-400 hover:text-white text-xs">✕</button>
        </div>

        <form @submit.prevent="handleCreateTower" class="space-y-3">
          <div>
            <label class="block text-xs font-semibold text-slate-300 mb-1">Código de Identificação</label>
            <input
              v-model="newTower.codigoIdentificacao"
              required
              placeholder="ex: TR-008"
              class="w-full bg-[#080d17] border border-[#1e2a40] rounded-lg px-3 py-1.5 text-xs text-white font-mono focus:outline-none focus:border-amber-500"
            />
          </div>

          <div>
            <label class="block text-xs font-semibold text-slate-300 mb-1">Setor Alocado na Mina</label>
            <select
              v-model="newTower.setorId"
              required
              class="w-full bg-[#080d17] border border-[#1e2a40] rounded-lg px-3 py-1.5 text-xs text-white focus:outline-none focus:border-amber-500"
            >
              <option v-for="s in towerStore.sectors" :key="s.id" :value="s.id">{{ s.nome }}</option>
            </select>
          </div>

          <div class="grid grid-cols-2 gap-2">
            <div>
              <label class="block text-xs font-semibold text-slate-300 mb-1">Latitude</label>
              <input
                v-model.number="newTower.latitude"
                type="number"
                step="any"
                required
                class="w-full bg-[#080d17] border border-[#1e2a40] rounded-lg px-3 py-1.5 text-xs text-white font-mono focus:outline-none focus:border-amber-500"
              />
            </div>
            <div>
              <label class="block text-xs font-semibold text-slate-300 mb-1">Longitude</label>
              <input
                v-model.number="newTower.longitude"
                type="number"
                step="any"
                required
                class="w-full bg-[#080d17] border border-[#1e2a40] rounded-lg px-3 py-1.5 text-xs text-white font-mono focus:outline-none focus:border-amber-500"
              />
            </div>
          </div>

          <div>
            <label class="block text-xs font-semibold text-slate-300 mb-1">Matriz de Energia</label>
            <select
              v-model="newTower.fonteEnergia"
              required
              class="w-full bg-[#080d17] border border-[#1e2a40] rounded-lg px-3 py-1.5 text-xs text-white focus:outline-none focus:border-amber-500"
            >
              <option value="PAINEL_SOLAR">Painel Solar</option>
              <option value="GERADOR_DIESEL">Gerador a Diesel</option>
              <option value="BATERIA">Bateria</option>
              <option value="HIBRIDO">Híbrido (Solar + Diesel)</option>
            </select>
          </div>

          <div class="grid grid-cols-2 gap-2">
            <div>
              <label class="block text-xs font-semibold text-slate-300 mb-1">Carga Bateria (%)</label>
              <input
                v-model.number="newTower.nivelBateria"
                type="number"
                min="0"
                max="100"
                class="w-full bg-[#080d17] border border-[#1e2a40] rounded-lg px-3 py-1.5 text-xs text-white font-mono focus:outline-none focus:border-amber-500"
              />
            </div>
            <div>
              <label class="block text-xs font-semibold text-slate-300 mb-1">Limite Preventiva (h)</label>
              <input
                v-model.number="newTower.limiteHorasPreventiva"
                type="number"
                class="w-full bg-[#080d17] border border-[#1e2a40] rounded-lg px-3 py-1.5 text-xs text-white font-mono focus:outline-none focus:border-amber-500"
              />
            </div>
          </div>

          <div class="pt-3 flex justify-end gap-2 border-t border-[#1c273c]">
            <button
              type="button"
              @click="showCreateModal = false"
              class="px-3 py-1.5 rounded-lg bg-[#162033] hover:bg-[#1f2d47] text-xs font-medium text-slate-300"
            >
              Cancelar
            </button>
            <button
              type="submit"
              class="px-3.5 py-1.5 rounded-lg bg-amber-500 hover:bg-amber-400 text-black font-bold text-xs transition-colors"
            >
              Salvar Ativo
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useTowerStore } from '../stores/towers';
import { Plus, Trash2 } from 'lucide-vue-next';

const towerStore = useTowerStore();

const filterSector = ref('');
const filterSource = ref('');
const filterStatus = ref('');
const showCreateModal = ref(false);

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
    if (filterSector.value && t.setorId !== filterSector.value) return false;
    if (filterSource.value && t.fonteEnergia !== filterSource.value) return false;
    if (filterStatus.value && t.status !== filterStatus.value) return false;
    return true;
  });
});

const handleCreateTower = async () => {
  try {
    await towerStore.createTower(newTower.value);
    showCreateModal.value = false;
    newTower.value.codigoIdentificacao = '';
    alert('Torre de iluminação cadastrada com sucesso!');
  } catch (err) {
    alert(err.response?.data?.message || 'Erro ao cadastrar torre.');
  }
};

const confirmDelete = async (tower) => {
  if (confirm(`Confirma a exclusão do ativo ${tower.codigoIdentificacao}?`)) {
    await towerStore.deleteTower(tower.id);
  }
};

onMounted(async () => {
  await towerStore.fetchTowers();
  await towerStore.fetchSectors();
});
</script>
