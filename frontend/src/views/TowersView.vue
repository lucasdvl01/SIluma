<template>
  <div class="space-y-6">
    <!-- Header -->
    <div class="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
      <div>
        <h2 class="text-2xl font-black text-white tracking-tight flex items-center gap-2">
          <span>Gestão da Frota de Torres</span>
          <span class="text-xs px-2.5 py-0.5 rounded-full bg-amber-500/10 text-amber-400 border border-amber-500/20 font-bold">
            RF01 • RF02 • RF03
          </span>
        </h2>
        <p class="text-xs text-slate-400">
          Inventário, monitoramento de parâmetros e acionamento remoto individual
        </p>
      </div>

      <button
        @click="showCreateModal = true"
        class="px-4 py-2.5 rounded-xl bg-gradient-to-r from-amber-500 to-amber-600 hover:from-amber-400 hover:to-amber-500 text-black font-extrabold text-xs shadow-lg shadow-amber-500/25 flex items-center gap-2 transition-all"
      >
        <Plus class="w-4 h-4" />
        Cadastrar Nova Torre (RF01)
      </button>
    </div>

    <!-- Filters Bar -->
    <div class="grid grid-cols-1 sm:grid-cols-3 gap-3 bg-mina-900 border border-mina-darkBorder p-3 rounded-2xl">
      <div>
        <label class="text-[10px] font-bold text-slate-400 uppercase tracking-wider block mb-1">Filtrar por Setor</label>
        <select
          v-model="filterSector"
          class="w-full bg-mina-800 border border-mina-darkBorder rounded-xl px-3 py-2 text-xs text-white focus:outline-none focus:border-amber-500"
        >
          <option :value="''">Todos os Setores</option>
          <option v-for="s in towerStore.sectors" :key="s.id" :value="s.id">{{ s.nome }}</option>
        </select>
      </div>

      <div>
        <label class="text-[10px] font-bold text-slate-400 uppercase tracking-wider block mb-1">Fonte de Energia</label>
        <select
          v-model="filterSource"
          class="w-full bg-mina-800 border border-mina-darkBorder rounded-xl px-3 py-2 text-xs text-white focus:outline-none focus:border-amber-500"
        >
          <option :value="''">Todas as Fontes</option>
          <option value="PAINEL_SOLAR">Painel Solar</option>
          <option value="GERADOR_DIESEL">Gerador a Diesel</option>
          <option value="BATERIA">Bateria</option>
          <option value="HIBRIDO">Híbrido (Solar + Diesel)</option>
        </select>
      </div>

      <div>
        <label class="text-[10px] font-bold text-slate-400 uppercase tracking-wider block mb-1">Status Operacional</label>
        <select
          v-model="filterStatus"
          class="w-full bg-mina-800 border border-mina-darkBorder rounded-xl px-3 py-2 text-xs text-white focus:outline-none focus:border-amber-500"
        >
          <option :value="''">Todos os Estados</option>
          <option value="OPERACIONAL">Operacional (Verde)</option>
          <option value="ALERTA_BAIXA_VISIBILIDADE">Cerração / Alerta (Amarelo)</option>
          <option value="EM_MANUTENCAO">Em Manutenção (Vermelho)</option>
          <option value="DESLIGADA">Desligada (Cinza)</option>
        </select>
      </div>
    </div>

    <!-- Table -->
    <div class="bg-mina-900 border border-mina-darkBorder rounded-2xl overflow-hidden shadow-xl">
      <div class="overflow-x-auto">
        <table class="w-full text-left border-collapse">
          <thead>
            <tr class="border-b border-mina-darkBorder bg-mina-800/40 text-[11px] font-bold text-slate-400 uppercase tracking-wider">
              <th class="p-4">Identificação</th>
              <th class="p-4">Setor / Trecho</th>
              <th class="p-4">Fonte de Energia</th>
              <th class="p-4">Estado (RF02)</th>
              <th class="p-4">Bateria</th>
              <th class="p-4">Horímetro</th>
              <th class="p-4">Modo Atual</th>
              <th class="p-4 text-right">Controle (RF03)</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-mina-darkBorder text-xs text-slate-300">
            <tr
              v-for="tower in filteredTowers"
              :key="tower.id"
              class="hover:bg-mina-800/40 transition-colors"
            >
              <!-- Identificação -->
              <td class="p-4 font-mono font-bold text-white flex items-center gap-2">
                <span class="w-2.5 h-2.5 rounded-full" :style="{ backgroundColor: tower.statusCor }"></span>
                {{ tower.codigoIdentificacao }}
              </td>

              <!-- Setor -->
              <td class="p-4">
                <div class="font-semibold text-white">{{ tower.setorNome }}</div>
                <div class="text-[10px] text-slate-500 font-mono">{{ tower.latitude?.toFixed(4) }}, {{ tower.longitude?.toFixed(4) }}</div>
              </td>

              <!-- Fonte de Energia -->
              <td class="p-4">
                <span class="inline-block px-2.5 py-1 rounded-md text-[11px] font-medium bg-mina-800 border border-mina-darkBorder text-slate-300">
                  {{ tower.fonteEnergiaDescricao }}
                </span>
              </td>

              <!-- Status -->
              <td class="p-4">
                <span
                  class="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-[11px] font-bold"
                  :style="{ backgroundColor: `${tower.statusCor}20`, color: tower.statusCor }"
                >
                  <span class="w-1.5 h-1.5 rounded-full" :style="{ backgroundColor: tower.statusCor }"></span>
                  {{ tower.statusLabel }}
                </span>
              </td>

              <!-- Bateria / Combustível -->
              <td class="p-4">
                <div class="flex items-center gap-2">
                  <div class="w-16 bg-mina-800 h-2 rounded-full overflow-hidden">
                    <div
                      class="h-full rounded-full transition-all"
                      :class="tower.nivelBateria < 25 ? 'bg-rose-500' : 'bg-emerald-500'"
                      :style="{ width: `${tower.nivelBateria}%` }"
                    ></div>
                  </div>
                  <span class="font-bold font-mono">{{ tower.nivelBateria }}%</span>
                </div>
              </td>

              <!-- Horímetro -->
              <td class="p-4 font-mono">
                <span class="font-bold text-white">{{ tower.horasAtividadeAcumuladas }}h</span>
                <span v-if="tower.necessitaManutencaoPreventiva" class="block text-[10px] text-amber-400 font-sans font-bold">
                  ⚠️ Revisão necessária!
                </span>
              </td>

              <!-- Modo Atual -->
              <td class="p-4">
                <span
                  class="px-2 py-0.5 rounded text-[10px] font-bold"
                  :class="{
                    'bg-amber-500/20 text-amber-300 border border-amber-500/30': tower.modoOperacao === 'ALTA_VISIBILIDADE_CERRACAO',
                    'bg-sky-500/20 text-sky-300': tower.modoOperacao === 'ECONOMIA',
                    'bg-slate-700/50 text-slate-300': tower.modoOperacao === 'NORMAL'
                  }"
                >
                  {{ tower.modoOperacaoLabel }}
                </span>
              </td>

              <!-- Ações -->
              <td class="p-4 text-right">
                <div class="flex items-center justify-end gap-1.5">
                  <!-- Toggle Liga/Desliga -->
                  <button
                    @click="towerStore.toggleTower(tower.id, !tower.ligada)"
                    class="px-2.5 py-1 rounded-lg text-xs font-bold transition-all"
                    :class="tower.ligada ? 'bg-rose-500/20 text-rose-300 hover:bg-rose-500/30' : 'bg-emerald-500/20 text-emerald-300 hover:bg-emerald-500/30'"
                  >
                    {{ tower.ligada ? 'Desligar' : 'Ligar' }}
                  </button>

                  <!-- Botão rápido Cerração -->
                  <button
                    @click="towerStore.setTowerMode(tower.id, 'ALTA_VISIBILIDADE_CERRACAO')"
                    title="Ativar modo cerração"
                    class="p-1.5 rounded-lg bg-amber-500/20 hover:bg-amber-500/30 text-amber-300"
                  >
                    <CloudFog class="w-3.5 h-3.5" />
                  </button>

                  <!-- Excluir -->
                  <button
                    @click="confirmDelete(tower)"
                    title="Excluir torre"
                    class="p-1.5 rounded-lg hover:bg-rose-500/20 text-slate-500 hover:text-rose-400"
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

    <!-- Modal de Cadastro de Torre (RF01) -->
    <div
      v-if="showCreateModal"
      class="fixed inset-0 bg-black/75 backdrop-blur-sm z-50 flex items-center justify-center p-4 animate-in fade-in"
    >
      <div class="bg-mina-900 border border-mina-darkBorder rounded-2xl max-w-md w-full p-6 shadow-2xl space-y-4">
        <div class="flex justify-between items-center border-b border-mina-darkBorder pb-3">
          <h3 class="font-extrabold text-white text-base flex items-center gap-2">
            <span>Cadastrar Torre de Iluminação</span>
            <span class="text-xs px-2 py-0.5 rounded bg-amber-500/20 text-amber-400 font-bold">RF01</span>
          </h3>
          <button @click="showCreateModal = false" class="text-slate-400 hover:text-white">✕</button>
        </div>

        <form @submit.prevent="handleCreateTower" class="space-y-3">
          <div>
            <label class="block text-xs font-semibold text-slate-300 mb-1">Código / ID Único</label>
            <input
              v-model="newTower.codigoIdentificacao"
              required
              placeholder="ex: TR-008"
              class="w-full bg-mina-800 border border-mina-darkBorder rounded-xl px-3 py-2 text-sm text-white focus:outline-none focus:border-amber-500"
            />
          </div>

          <div>
            <label class="block text-xs font-semibold text-slate-300 mb-1">Setor Alocado na Mina</label>
            <select
              v-model="newTower.setorId"
              required
              class="w-full bg-mina-800 border border-mina-darkBorder rounded-xl px-3 py-2 text-sm text-white focus:outline-none focus:border-amber-500"
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
                class="w-full bg-mina-800 border border-mina-darkBorder rounded-xl px-3 py-2 text-sm text-white focus:outline-none focus:border-amber-500"
              />
            </div>
            <div>
              <label class="block text-xs font-semibold text-slate-300 mb-1">Longitude</label>
              <input
                v-model.number="newTower.longitude"
                type="number"
                step="any"
                required
                class="w-full bg-mina-800 border border-mina-darkBorder rounded-xl px-3 py-2 text-sm text-white focus:outline-none focus:border-amber-500"
              />
            </div>
          </div>

          <div>
            <label class="block text-xs font-semibold text-slate-300 mb-1">Fonte de Energia</label>
            <select
              v-model="newTower.fonteEnergia"
              required
              class="w-full bg-mina-800 border border-mina-darkBorder rounded-xl px-3 py-2 text-sm text-white focus:outline-none focus:border-amber-500"
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
                class="w-full bg-mina-800 border border-mina-darkBorder rounded-xl px-3 py-2 text-sm text-white focus:outline-none focus:border-amber-500"
              />
            </div>
            <div>
              <label class="block text-xs font-semibold text-slate-300 mb-1">Limite Preventiva (h)</label>
              <input
                v-model.number="newTower.limiteHorasPreventiva"
                type="number"
                class="w-full bg-mina-800 border border-mina-darkBorder rounded-xl px-3 py-2 text-sm text-white focus:outline-none focus:border-amber-500"
              />
            </div>
          </div>

          <div class="pt-3 flex justify-end gap-2 border-t border-mina-darkBorder">
            <button
              type="button"
              @click="showCreateModal = false"
              class="px-4 py-2 rounded-xl bg-mina-800 hover:bg-mina-700 text-xs font-semibold text-slate-300"
            >
              Cancelar
            </button>
            <button
              type="submit"
              class="px-4 py-2 rounded-xl bg-amber-500 hover:bg-amber-400 text-black font-extrabold text-xs shadow-md transition-all"
            >
              Salvar Torre
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
import { Plus, Trash2, CloudFog } from 'lucide-vue-next';

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
  if (confirm(`Tem certeza que deseja excluir a torre ${tower.codigoIdentificacao}?`)) {
    await towerStore.deleteTower(tower.id);
  }
};

onMounted(async () => {
  await towerStore.fetchTowers();
  await towerStore.fetchSectors();
});
</script>
