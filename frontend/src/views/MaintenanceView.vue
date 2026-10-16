<template>
  <div class="space-y-6">
    <!-- Header -->
    <div class="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
      <div>
        <h2 class="text-2xl font-black text-white tracking-tight flex items-center gap-2">
          <span>Gestão de Manutenções & Ordens de Serviço</span>
          <span class="text-xs px-2.5 py-0.5 rounded-full bg-rose-500/10 text-rose-400 border border-rose-500/20 font-bold">
            RF04
          </span>
        </h2>
        <p class="text-xs text-slate-400">
          Registro de panes reportadas em campo, agendamento de intervenções e controle de horímetro preventivo
        </p>
      </div>

      <button
        @click="showCreateModal = true"
        class="px-4 py-2.5 rounded-xl bg-gradient-to-r from-rose-500 to-rose-600 hover:from-rose-400 hover:to-rose-500 text-white font-extrabold text-xs shadow-lg shadow-rose-500/25 flex items-center gap-2 transition-all"
      >
        <Plus class="w-4 h-4" />
        Reportar Falha / Abrir OS (RF04)
      </button>
    </div>

    <!-- Alertas Preventivos baseados em Horímetro -->
    <div v-if="preventiveTowers.length > 0" class="p-4 bg-amber-500/10 border border-amber-500/30 rounded-2xl space-y-2">
      <div class="flex items-center gap-2 text-amber-400 font-bold text-xs uppercase tracking-wider">
        <Clock class="w-4 h-4" />
        Aviso aos Técnicos: Manutenções Preventivas por Horímetro Excedido
      </div>
      <p class="text-xs text-slate-300">
        As seguintes torres ultrapassaram o ciclo limite de operação contínua e requerem intervenção programada:
      </p>
      <div class="flex flex-wrap gap-2 pt-1">
        <div
          v-for="t in preventiveTowers"
          :key="t.id"
          class="px-3 py-1.5 rounded-xl bg-mina-900 border border-amber-500/40 text-xs flex items-center gap-2 text-amber-200"
        >
          <span class="font-bold">{{ t.codigoIdentificacao }}</span>
          <span class="text-slate-400">({{ t.horasAtividadeAcumuladas }}h / lim: {{ t.limiteHorasPreventiva }}h)</span>
          <button
            @click="quickOpenPreventive(t)"
            class="text-[10px] font-bold px-2 py-0.5 rounded bg-amber-500 text-black hover:bg-amber-400 transition-colors ml-1"
          >
            Abrir OS
          </button>
        </div>
      </div>
    </div>

    <!-- Lista de Ordens de Manutenção -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
      <div
        v-for="order in maintenanceStore.orders"
        :key="order.id"
        class="bg-mina-900 border border-mina-darkBorder rounded-2xl p-5 shadow-xl flex flex-col justify-between space-y-4 hover:border-slate-700 transition-all"
      >
        <div class="space-y-3">
          <div class="flex justify-between items-start">
            <div>
              <span class="text-xs font-mono font-bold px-2 py-0.5 rounded bg-mina-800 text-amber-400 border border-mina-darkBorder">
                {{ order.codigoTorre }}
              </span>
              <h4 class="font-extrabold text-sm text-white mt-1.5">{{ order.tipoFalha }}</h4>
              <p class="text-[11px] text-slate-400">{{ order.setorNome }}</p>
            </div>

            <span
              class="px-2 py-0.5 rounded text-[10px] font-bold"
              :class="{
                'bg-amber-500/20 text-amber-300 border border-amber-500/30': order.status === 'PENDENTE',
                'bg-sky-500/20 text-sky-300 border border-sky-500/30': order.status === 'EM_ANDAMENTO',
                'bg-emerald-500/20 text-emerald-300 border border-emerald-500/30': order.status === 'CONCLUIDA'
              }"
            >
              {{ order.statusLabel }}
            </span>
          </div>

          <p class="text-xs text-slate-300 bg-mina-950/60 p-3 rounded-xl border border-mina-darkBorder">
            {{ order.descricao }}
          </p>

          <div class="grid grid-cols-2 gap-2 text-[11px]">
            <div class="text-slate-400">
              <span class="block text-[10px] text-slate-500">Técnico Encarregado:</span>
              <span class="font-semibold text-slate-200">{{ order.tecnicoResponsavel }}</span>
            </div>
            <div class="text-slate-400">
              <span class="block text-[10px] text-slate-500">Horímetro na Falha:</span>
              <span class="font-semibold text-white font-mono">{{ order.horasTorreNaAbertura }}h</span>
            </div>
          </div>
        </div>

        <!-- Ações da OS -->
        <div class="pt-3 border-t border-mina-darkBorder flex items-center justify-between">
          <span class="text-[10px] text-slate-500">
            Abertura: {{ formatDate(order.dataAbertura) }}
          </span>

          <div class="flex gap-2">
            <button
              v-if="order.status === 'PENDENTE'"
              @click="updateStatus(order.id, 'EM_ANDAMENTO')"
              class="px-2.5 py-1 rounded-lg bg-sky-600/20 hover:bg-sky-600/30 text-sky-300 font-bold text-xs transition-all"
            >
              Iniciar Reparo
            </button>
            <button
              v-if="order.status !== 'CONCLUIDA'"
              @click="finishOrder(order)"
              class="px-2.5 py-1 rounded-lg bg-emerald-600 hover:bg-emerald-500 text-white font-bold text-xs transition-all"
            >
              Concluir OS
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Modal Reportar Falha (RF04) -->
    <div
      v-if="showCreateModal"
      class="fixed inset-0 bg-black/75 backdrop-blur-sm z-50 flex items-center justify-center p-4 animate-in fade-in"
    >
      <div class="bg-mina-900 border border-mina-darkBorder rounded-2xl max-w-md w-full p-6 shadow-2xl space-y-4">
        <div class="flex justify-between items-center border-b border-mina-darkBorder pb-3">
          <h3 class="font-extrabold text-white text-base flex items-center gap-2">
            <span>Reportar Falha / Chamado Técnico</span>
            <span class="text-xs px-2 py-0.5 rounded bg-rose-500/20 text-rose-400 font-bold">RF04</span>
          </h3>
          <button @click="showCreateModal = false" class="text-slate-400 hover:text-white">✕</button>
        </div>

        <form @submit.prevent="handleCreateOrder" class="space-y-3">
          <div>
            <label class="block text-xs font-semibold text-slate-300 mb-1">Selecione a Torre</label>
            <select
              v-model="newOrder.torreId"
              required
              class="w-full bg-mina-800 border border-mina-darkBorder rounded-xl px-3 py-2 text-sm text-white focus:outline-none focus:border-amber-500"
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
              placeholder="ex: Queima de Módulo LED, Bateria, Falha Sensor LDR"
              class="w-full bg-mina-800 border border-mina-darkBorder rounded-xl px-3 py-2 text-sm text-white focus:outline-none focus:border-amber-500"
            />
          </div>

          <div class="grid grid-cols-2 gap-2">
            <div>
              <label class="block text-xs font-semibold text-slate-300 mb-1">Gravidade</label>
              <select
                v-model="newOrder.gravidade"
                class="w-full bg-mina-800 border border-mina-darkBorder rounded-xl px-3 py-2 text-sm text-white focus:outline-none focus:border-amber-500"
              >
                <option value="BAIXA">Baixa</option>
                <option value="MEDIA">Média</option>
                <option value="ALTA">Alta</option>
                <option value="CRITICA">Crítica</option>
              </select>
            </div>
            <div>
              <label class="block text-xs font-semibold text-slate-300 mb-1">Técnico Responsável</label>
              <input
                v-model="newOrder.tecnicoResponsavel"
                required
                placeholder="Nome do técnico"
                class="w-full bg-mina-800 border border-mina-darkBorder rounded-xl px-3 py-2 text-sm text-white focus:outline-none focus:border-amber-500"
              />
            </div>
          </div>

          <div>
            <label class="block text-xs font-semibold text-slate-300 mb-1">Descrição Detalhada do Problema</label>
            <textarea
              v-model="newOrder.descricao"
              required
              rows="3"
              placeholder="Descreva as condições observadas em campo pelos operadores..."
              class="w-full bg-mina-800 border border-mina-darkBorder rounded-xl px-3 py-2 text-sm text-white focus:outline-none focus:border-amber-500"
            ></textarea>
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
              class="px-4 py-2 rounded-xl bg-rose-600 hover:bg-rose-500 text-white font-extrabold text-xs shadow-md transition-all"
            >
              Registrar Chamado
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
    alert('Ordem de manutenção registrada com sucesso! A torre foi movida para o status "Em Manutenção".');
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
  const obs = prompt('Insira observações sobre o reparo efetuado:', 'Manutenção e testes operacionais executados com sucesso.');
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
