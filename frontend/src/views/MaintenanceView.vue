<template>
  <div class="space-y-5">
    <!-- Header -->
    <div class="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
      <div>
        <h2 class="text-xl font-bold text-white tracking-tight flex items-center gap-2">
          <span>Gestão de Manutenções & Ordens de Serviço (OS)</span>
          <span class="text-[10px] font-mono px-2 py-0.5 rounded bg-[#1e293b] text-slate-300 border border-[#334155]">
            RF04
          </span>
        </h2>
        <p class="text-xs text-slate-400">
          Registro de panes reportadas em campo, agendamento de intervenções e controle de horímetro preventivo
        </p>
      </div>

      <button
        @click="showCreateModal = true"
        class="px-3.5 py-2 rounded-lg bg-rose-600 hover:bg-rose-500 text-white font-bold text-xs flex items-center gap-2 transition-colors"
      >
        <Plus class="w-4 h-4" />
        <span>Abrir Ordem de Serviço (RF04)</span>
      </button>
    </div>

    <!-- Alertas Preventivos baseados em Horímetro -->
    <div v-if="preventiveTowers.length > 0" class="p-3.5 bg-amber-500/10 border border-amber-500/30 rounded-xl space-y-2">
      <div class="flex items-center gap-2 text-amber-400 font-bold text-xs uppercase tracking-wider font-mono">
        <Clock class="w-4 h-4" />
        <span>Alerta Técnico: Manutenções Preventivas por Horímetro Excedido</span>
      </div>
      <p class="text-xs text-slate-300">
        As seguintes torres atingiram o limite programado de horas contínuas de operação:
      </p>
      <div class="flex flex-wrap gap-2 pt-1">
        <div
          v-for="t in preventiveTowers"
          :key="t.id"
          class="px-2.5 py-1 rounded-lg bg-[#0e1626] border border-amber-500/40 text-xs flex items-center gap-2 text-amber-200"
        >
          <span class="font-mono font-bold">{{ t.codigoIdentificacao }}</span>
          <span class="text-slate-400 font-mono">({{ t.horasAtividadeAcumuladas }}h / limite: {{ t.limiteHorasPreventiva }}h)</span>
          <button
            @click="quickOpenPreventive(t)"
            class="text-[10px] font-bold px-1.5 py-0.2 rounded bg-amber-500 text-black hover:bg-amber-400 transition-colors ml-1 font-mono"
          >
            Abrir OS
          </button>
        </div>
      </div>
    </div>

    <!-- Lista de Ordens de Serviço (Cartões Estilo SAP/Maximo) -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
      <div
        v-for="order in maintenanceStore.orders"
        :key="order.id"
        class="bg-[#0e1626] border border-[#1c273c] rounded-xl p-4 flex flex-col justify-between space-y-3 hover:border-[#273752] transition-colors"
      >
        <div class="space-y-2.5">
          <div class="flex justify-between items-start">
            <div>
              <span class="text-[10px] font-mono font-bold px-2 py-0.5 rounded bg-[#080d17] text-amber-400 border border-[#1e2a40]">
                {{ order.codigoTorre }}
              </span>
              <h4 class="font-bold text-xs text-white mt-1.5">{{ order.tipoFalha }}</h4>
              <p class="text-[10px] text-slate-400">{{ order.setorNome }}</p>
            </div>

            <span
              class="px-2 py-0.5 rounded text-[10px] font-mono font-bold border"
              :class="{
                'bg-amber-500/10 text-amber-300 border-amber-500/30': order.status === 'PENDENTE',
                'bg-sky-500/10 text-sky-300 border-sky-500/30': order.status === 'EM_ANDAMENTO',
                'bg-emerald-500/10 text-emerald-300 border-emerald-500/30': order.status === 'CONCLUIDA'
              }"
            >
              {{ order.statusLabel }}
            </span>
          </div>

          <p class="text-xs text-slate-300 bg-[#080d17] p-2.5 rounded-lg border border-[#162033]">
            {{ order.descricao }}
          </p>

          <div class="grid grid-cols-2 gap-2 text-[11px] pt-1 border-t border-[#162033]">
            <div>
              <span class="block text-[10px] text-slate-500 font-mono">TÉCNICO:</span>
              <span class="text-slate-200 font-medium">{{ order.tecnicoResponsavel }}</span>
            </div>
            <div>
              <span class="block text-[10px] text-slate-500 font-mono">HORÍMETRO:</span>
              <span class="text-white font-mono font-bold">{{ order.horasTorreNaAbertura }} h</span>
            </div>
          </div>
        </div>

        <!-- Ações da OS -->
        <div class="pt-2.5 border-t border-[#1c273c] flex items-center justify-between text-xs">
          <span class="text-[10px] text-slate-500 font-mono">
            {{ formatDate(order.dataAbertura) }}
          </span>

          <div class="flex gap-1.5">
            <button
              v-if="order.status === 'PENDENTE'"
              @click="updateStatus(order.id, 'EM_ANDAMENTO')"
              class="px-2.5 py-1 rounded bg-sky-500/10 hover:bg-sky-500/20 text-sky-300 border border-sky-500/30 font-medium text-xs transition-colors"
            >
              Iniciar Reparo
            </button>
            <button
              v-if="order.status !== 'CONCLUIDA'"
              @click="finishOrder(order)"
              class="px-2.5 py-1 rounded bg-emerald-600 hover:bg-emerald-500 text-white font-bold text-xs transition-colors"
            >
              Concluir OS
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Modal de Registro de Falha (RF04) -->
    <div
      v-if="showCreateModal"
      class="fixed inset-0 bg-black/80 backdrop-blur-sm z-50 flex items-center justify-center p-4"
    >
      <div class="bg-[#0e1626] border border-[#1c273c] rounded-xl max-w-md w-full p-6 shadow-2xl space-y-4">
        <div class="flex justify-between items-center border-b border-[#1c273c] pb-3">
          <h3 class="font-bold text-white text-sm flex items-center gap-2">
            <span>Abertura de Chamado Técnico</span>
            <span class="text-[10px] font-mono px-1.5 py-0.2 rounded bg-rose-500/10 text-rose-400 border border-rose-500/30">RF04</span>
          </h3>
          <button @click="showCreateModal = false" class="text-slate-400 hover:text-white text-xs">✕</button>
        </div>

        <form @submit.prevent="handleCreateOrder" class="space-y-3">
          <div>
            <label class="block text-xs font-semibold text-slate-300 mb-1">Torre Afetada</label>
            <select
              v-model="newOrder.torreId"
              required
              class="w-full bg-[#080d17] border border-[#1e2a40] rounded-lg px-3 py-1.5 text-xs text-white focus:outline-none focus:border-amber-500"
            >
              <option v-for="t in towerStore.towers" :key="t.id" :value="t.id">
                {{ t.codigoIdentificacao }} — {{ t.setorNome }}
              </option>
            </select>
          </div>

          <div>
            <label class="block text-xs font-semibold text-slate-300 mb-1">Tipo da Falha / Ocorrência</label>
            <input
              v-model="newOrder.tipoFalha"
              required
              placeholder="ex: Quebra de Módulo LED, Bateria, Falha Sensor LDR"
              class="w-full bg-[#080d17] border border-[#1e2a40] rounded-lg px-3 py-1.5 text-xs text-white focus:outline-none focus:border-amber-500"
            />
          </div>

          <div class="grid grid-cols-2 gap-2">
            <div>
              <label class="block text-xs font-semibold text-slate-300 mb-1">Gravidade</label>
              <select
                v-model="newOrder.gravidade"
                class="w-full bg-[#080d17] border border-[#1e2a40] rounded-lg px-3 py-1.5 text-xs text-white focus:outline-none focus:border-amber-500"
              >
                <option value="BAIXA">Baixa</option>
                <option value="MEDIA">Média</option>
                <option value="ALTA">Alta</option>
                <option value="CRITICA">Crítica</option>
              </select>
            </div>
            <div>
              <label class="block text-xs font-semibold text-slate-300 mb-1">Técnico Encarregado</label>
              <input
                v-model="newOrder.tecnicoResponsavel"
                required
                placeholder="Nome do técnico"
                class="w-full bg-[#080d17] border border-[#1e2a40] rounded-lg px-3 py-1.5 text-xs text-white focus:outline-none focus:border-amber-500"
              />
            </div>
          </div>

          <div>
            <label class="block text-xs font-semibold text-slate-300 mb-1">Descrição Técnica do Problema</label>
            <textarea
              v-model="newOrder.descricao"
              required
              rows="3"
              placeholder="Descreva as condições observadas em campo pelos operadores..."
              class="w-full bg-[#080d17] border border-[#1e2a40] rounded-lg px-3 py-1.5 text-xs text-white focus:outline-none focus:border-amber-500"
            ></textarea>
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
              class="px-3.5 py-1.5 rounded-lg bg-rose-600 hover:bg-rose-500 text-white font-bold text-xs transition-colors"
            >
              Emitir OS
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useMaintenanceStore } from '../stores/maintenance';
import { useTowerStore } from '../stores/towers';
import { Plus, Clock } from 'lucide-vue-next';

const maintenanceStore = useMaintenanceStore();
const towerStore = useTowerStore();

const showCreateModal = ref(false);
const newOrder = ref({
  torreId: null,
  tipoFalha: '',
  gravidade: 'MEDIA',
  descricao: '',
  tecnicoResponsavel: 'Equipe de Manutenção Elétrica'
});

const preventiveTowers = computed(() => {
  return towerStore.towers.filter(t => t.necessitaManutencaoPreventiva);
});

const handleCreateOrder = async () => {
  try {
    await maintenanceStore.createOrder(newOrder.value);
    await towerStore.fetchTowers();
    showCreateModal.value = false;
    newOrder.value.tipoFalha = '';
    newOrder.value.descricao = '';
    alert('Ordem de serviço registrada com sucesso. A torre foi movida para o status "Em Manutenção".');
  } catch (err) {
    alert(err.response?.data?.message || 'Erro ao registrar chamado.');
  }
};

const quickOpenPreventive = (tower) => {
  newOrder.value.torreId = tower.id;
  newOrder.value.tipoFalha = 'Revisão Preventiva de Horímetro';
  newOrder.value.gravidade = 'ALTA';
  newOrder.value.descricao = `Torre atingiu ${tower.horasAtividadeAcumuladas} horas contínuas (limite: ${tower.limiteHorasPreventiva}h). Revisão necessária dos módulos e baterias.`;
  showCreateModal.value = true;
};

const updateStatus = async (id, status) => {
  await maintenanceStore.updateOrder(id, status);
};

const finishOrder = async (order) => {
  const obs = prompt('Insira as observações sobre o reparo efetuado:', 'Manutenção e testes operacionais executados com sucesso.');
  if (obs !== null) {
    await maintenanceStore.updateOrder(order.id, 'CONCLUIDA', obs);
    await towerStore.fetchTowers();
    alert('Manutenção concluída! A torre foi restabelecida para o status "Operacional".');
  }
};

const formatDate = (dateStr) => {
  if (!dateStr) return '';
  const d = new Date(dateStr);
  return d.toLocaleDateString('pt-BR') + ' ' + d.toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' });
};

onMounted(async () => {
  await towerStore.fetchTowers();
  await maintenanceStore.fetchOrders();
  if (towerStore.towers.length > 0) {
    newOrder.value.torreId = towerStore.towers[0].id;
  }
});
</script>
