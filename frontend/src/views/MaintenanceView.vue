<template>
  <div class="space-y-6 max-w-6xl mx-auto">
    <!-- Header -->
    <header class="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 pb-4 border-b border-white/5">
      <div>
        <h1 class="text-xl font-bold text-white tracking-tight font-sans">
          Gestão de Manutenções & Ordens de Serviço
        </h1>
        <p class="text-xs text-slate-400 mt-0.5">
          Registro de ocorrências de campo, intervenções corretivas e controle de horímetro preventivo
        </p>
      </div>

      <button
        @click="showCreateModal = true"
        class="px-4 py-2 rounded-xl bg-rose-600 hover:bg-rose-500 text-white font-bold text-xs flex items-center gap-2 transition-all interactive-lift shadow-sm"
      >
        <Plus class="w-4 h-4" />
        <span>Nova Ordem de Serviço</span>
      </button>
    </header>

    <!-- Modeless Feedback Notification -->
    <transition enter-active-class="transition duration-150 ease-out" enter-from-class="opacity-0 -translate-y-2" enter-to-class="opacity-100 translate-y-0">
      <div v-if="feedbackMessage" class="p-3.5 bg-emerald-500/15 border border-emerald-500/30 rounded-2xl text-xs text-emerald-300 font-medium flex items-center justify-between shadow-lg">
        <div class="flex items-center gap-2.5">
          <CircleCheck class="w-4 h-4 text-emerald-400 shrink-0" />
          <span>{{ feedbackMessage }}</span>
        </div>
        <button @click="feedbackMessage = ''" class="text-emerald-400/80 hover:text-white p-1">✕</button>
      </div>
    </transition>

    <!-- Alertas Preventivos de Horímetro Excedido -->
    <div v-if="preventiveTowers.length > 0" class="p-4 bg-amber-500/10 border border-amber-500/30 rounded-2xl space-y-2.5 shadow-sm">
      <div class="flex items-center gap-2 text-amber-400 font-bold text-xs uppercase tracking-wider font-mono">
        <Clock class="w-4 h-4" />
        <span>Manutenções Preventivas por Horímetro Excedido</span>
      </div>
      <p class="text-xs text-slate-300">
        As seguintes torres atingiram o limite programado de horas contínuas de operação:
      </p>
      <div class="flex flex-wrap gap-2.5 pt-1">
        <div
          v-for="t in preventiveTowers"
          :key="t.id"
          class="px-3 py-1.5 rounded-xl bg-slate-900 border border-amber-500/40 text-xs flex items-center gap-2 text-amber-200"
        >
          <span class="font-mono font-bold">{{ t.codigoIdentificacao }}</span>
          <span class="text-slate-400 font-mono text-[11px]">({{ formatHorimetro(t.horasAtividadeAcumuladas) }}h / limite: {{ formatHorimetro(t.limiteHorasPreventiva) }}h)</span>
          <button
            @click="quickOpenPreventive(t)"
            class="text-[10px] font-bold px-2 py-0.5 rounded-lg bg-amber-400 text-slate-950 hover:bg-amber-300 transition-all ml-1 font-mono interactive-lift"
          >
            Emitir OS
          </button>
        </div>
      </div>
    </div>

    <!-- Lista de Ordens de Serviço (Cartões Alinhados e Polidos) -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 items-start gap-4">
      <div
        v-for="order in maintenanceStore.orders"
        :key="order.id"
        class="tactile-panel rounded-2xl p-4.5 flex flex-col gap-3.5 transition-all group"
      >
        <div class="space-y-3">
          <!-- Cabeçalho do Card: Código, Título e Badge sem quebra indesejada -->
          <div class="flex justify-between items-start gap-3">
            <div class="min-w-0 flex-1">
              <span class="text-[11px] font-mono font-bold px-2.5 py-0.5 rounded-md bg-slate-900 text-amber-400 border border-white/5 inline-block">
                {{ order.codigoTorre }}
              </span>
              <h3 class="font-bold text-sm text-white mt-2 leading-snug">{{ order.tipoFalha }}</h3>
              <p class="text-xs text-slate-400 mt-0.5 font-sans">{{ order.setorNome }}</p>
            </div>

            <!-- Badge de Status: whitespace-nowrap e shrink-0 evitam quebra de texto feia -->
            <span
              class="px-2.5 py-1 rounded-full text-[10px] font-mono font-bold border whitespace-nowrap shrink-0 text-center leading-none"
              :class="{
                'bg-amber-500/10 text-amber-300 border-amber-500/30': order.status === 'PENDENTE',
                'bg-sky-500/10 text-sky-300 border-sky-500/30': order.status === 'EM_ANDAMENTO',
                'bg-emerald-500/10 text-emerald-300 border-emerald-500/30': order.status === 'CONCLUIDA'
              }"
            >
              {{ order.statusLabel }}
            </span>
          </div>

          <p class="text-xs text-slate-300 bg-slate-950/80 p-3 rounded-xl border border-white/5 font-sans leading-relaxed">
            {{ order.descricao }}
          </p>

          <div class="grid grid-cols-2 gap-2 text-[11px] pt-2 border-t border-white/5 font-sans">
            <div>
              <span class="block text-[10px] text-slate-500 font-mono">RESPONSÁVEL:</span>
              <span class="text-slate-200 font-medium truncate block">{{ order.tecnicoResponsavel }}</span>
            </div>
            <div>
              <span class="block text-[10px] text-slate-500 font-mono">HORÍMETRO:</span>
              <span class="text-white font-mono font-bold tabular-nums">{{ formatHorimetro(order.horasTorreNaAbertura) }} h</span>
            </div>
          </div>
        </div>

        <!-- Rodapé do Card: Data e Ação Sempre Perfeitamente Alinhadas -->
        <div class="pt-3 border-t border-white/5 flex items-center justify-between min-h-[40px] text-xs">
          <span class="text-[11px] text-slate-500 font-mono tabular-nums">
            {{ formatDate(order.dataAbertura) }}
          </span>

          <div class="flex items-center gap-2">
            <!-- Ações Operacionais -->
            <button
              v-if="order.status === 'PENDENTE'"
              @click="updateStatus(order.id, 'EM_ANDAMENTO')"
              class="px-3 py-1.5 rounded-xl bg-sky-500/10 hover:bg-sky-500/20 text-sky-300 border border-sky-500/30 font-semibold text-xs transition-all interactive-lift"
            >
              Iniciar Reparo
            </button>

            <button
              v-if="order.status !== 'CONCLUIDA'"
              @click="openResolutionModal(order)"
              class="px-3.5 py-1.5 rounded-xl bg-emerald-500 hover:bg-emerald-400 text-slate-950 font-bold text-xs transition-all interactive-lift shadow-sm font-sans"
            >
              Concluir OS
            </button>

            <!-- Exclusão de OS -->
            <button
              @click="deleteOrder(order.id)"
              title="Excluir Ordem de Serviço"
              class="p-1.5 text-slate-500 hover:text-rose-400 rounded-lg hover:bg-rose-500/10 transition-colors"
            >
              <Trash2 class="w-3.5 h-3.5" />
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Modal de Criação de OS -->
    <div
      v-if="showCreateModal"
      class="fixed inset-0 bg-black/80 backdrop-blur-sm z-50 flex items-center justify-center p-4 animate-fadeIn"
    >
      <div class="tactile-panel max-w-md w-full p-6 rounded-2xl shadow-2xl space-y-4 border border-white/10">
        <div class="flex justify-between items-center border-b border-white/5 pb-3">
          <h3 class="font-bold text-white text-sm font-mono tracking-wider">
            Abertura de Chamado Técnico
          </h3>
          <button @click="showCreateModal = false" class="text-slate-400 hover:text-white text-xs p-1">✕</button>
        </div>

        <form @submit.prevent="handleCreateOrder" class="space-y-3.5">
          <div>
            <label class="block text-xs font-semibold text-slate-300 mb-1">Torre Afetada</label>
            <select
              v-model="newOrder.torreId"
              required
              class="w-full bg-slate-950 border border-slate-700/60 rounded-xl px-3.5 py-2 text-xs text-white focus:outline-none focus:border-amber-400"
            >
              <option v-for="t in towerStore.towers" :key="t.id" :value="t.id">
                {{ t.codigoIdentificacao }} — {{ t.setorNome }}
              </option>
            </select>
          </div>

          <div>
            <label class="block text-xs font-semibold text-slate-300 mb-1">Tipo da Ocorrência</label>
            <input
              v-model="newOrder.tipoFalha"
              required
              placeholder="ex: Quebra de Módulo LED, Descarga da Bateria"
              class="w-full bg-slate-950 border border-slate-700/60 rounded-xl px-3.5 py-2 text-xs text-white placeholder-slate-500 focus:outline-none focus:border-amber-400"
            />
          </div>

          <div>
            <label class="block text-xs font-semibold text-slate-300 mb-1">Gravidade da Falha</label>
            <div class="grid grid-cols-3 gap-2">
              <button
                type="button"
                @click="newOrder.gravidade = 'BAIXA'"
                class="py-1.5 text-xs rounded-xl border text-center font-bold transition-all"
                :class="newOrder.gravidade === 'BAIXA' ? 'bg-emerald-500/20 text-emerald-300 border-emerald-500/50' : 'bg-slate-950 text-slate-400 border-white/5'"
              >
                Baixa
              </button>
              <button
                type="button"
                @click="newOrder.gravidade = 'MEDIA'"
                class="py-1.5 text-xs rounded-xl border text-center font-bold transition-all"
                :class="newOrder.gravidade === 'MEDIA' ? 'bg-amber-500/20 text-amber-300 border-amber-500/50' : 'bg-slate-950 text-slate-400 border-white/5'"
              >
                Média
              </button>
              <button
                type="button"
                @click="newOrder.gravidade = 'ALTA'"
                class="py-1.5 text-xs rounded-xl border text-center font-bold transition-all"
                :class="newOrder.gravidade === 'ALTA' ? 'bg-rose-500/20 text-rose-300 border-rose-500/50' : 'bg-slate-950 text-slate-400 border-white/5'"
              >
                Alta
              </button>
            </div>
          </div>

          <div>
            <label class="block text-xs font-semibold text-slate-300 mb-1">Descrição Detalhada</label>
            <textarea
              v-model="newOrder.descricao"
              required
              rows="3"
              placeholder="Descreva as condições observadas em campo pelos operadores..."
              class="w-full bg-slate-950 border border-slate-700/60 rounded-xl px-3.5 py-2 text-xs text-white placeholder-slate-500 focus:outline-none focus:border-amber-400"
            ></textarea>
          </div>

          <div class="pt-3 flex justify-end gap-2.5 border-t border-white/5">
            <button
              type="button"
              @click="showCreateModal = false"
              class="px-4 py-2 rounded-xl bg-slate-900 hover:bg-slate-800 text-xs font-medium text-slate-300 transition-colors"
            >
              Cancelar
            </button>
            <button
              type="submit"
              class="px-4 py-2 rounded-xl bg-rose-600 hover:bg-rose-500 text-white font-bold text-xs transition-all interactive-lift shadow-sm"
            >
              Emitir Ordem de Serviço
            </button>
          </div>
        </form>
      </div>
    </div>

    <!-- Modal Elegante de Conclusão de OS -->
    <div
      v-if="orderToFinish"
      class="fixed inset-0 bg-black/80 backdrop-blur-sm z-50 flex items-center justify-center p-4 animate-fadeIn"
    >
      <div class="tactile-panel max-w-md w-full p-6 rounded-2xl shadow-2xl space-y-4 border border-white/10">
        <div class="flex justify-between items-center border-b border-white/5 pb-3">
          <h3 class="font-bold text-white text-sm font-mono tracking-wider">
            Concluir Manutenção: {{ orderToFinish.codigoTorre }}
          </h3>
          <button @click="orderToFinish = null" class="text-slate-400 hover:text-white text-xs p-1">✕</button>
        </div>

        <form @submit.prevent="confirmFinishOrder" class="space-y-3.5">
          <p class="text-xs text-slate-300">
            Ao concluir a manutenção, o status da torre será restabelecido automaticamente para <strong>"Operacional"</strong>.
          </p>

          <div>
            <label class="block text-xs font-semibold text-slate-300 mb-1">Parecer Técnico / Reparo Efetuado</label>
            <textarea
              v-model="resolutionNotes"
              required
              rows="3"
              class="w-full bg-slate-950 border border-slate-700/60 rounded-xl px-3.5 py-2 text-xs text-white focus:outline-none focus:border-emerald-400"
            ></textarea>
          </div>

          <div class="pt-3 flex justify-end gap-2.5 border-t border-white/5">
            <button
              type="button"
              @click="orderToFinish = null"
              class="px-4 py-2 rounded-xl bg-slate-900 hover:bg-slate-800 text-xs font-medium text-slate-300 transition-colors"
            >
              Cancelar
            </button>
            <button
              type="submit"
              class="px-4 py-2 rounded-xl bg-emerald-500 hover:bg-emerald-400 text-slate-950 font-bold text-xs transition-all interactive-lift shadow-sm"
            >
              Restabelecer Torre
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
import api from '../services/api';
import { Plus, Clock, CircleCheck, Trash2 } from 'lucide-vue-next';

const maintenanceStore = useMaintenanceStore();
const towerStore = useTowerStore();

const showCreateModal = ref(false);
const orderToFinish = ref(null);
const resolutionNotes = ref('Manutenção e testes operacionais executados com sucesso.');
const feedbackMessage = ref('');

const newOrder = ref({
  torreId: null,
  tipoFalha: '',
  gravidade: 'MEDIA',
  descricao: '',
  tecnicoResponsavel: 'Equipe de Manutenção Elétrica'
});

const showFeedback = (msg) => {
  feedbackMessage.value = msg;
  setTimeout(() => {
    if (feedbackMessage.value === msg) {
      feedbackMessage.value = '';
    }
  }, 4000);
};

// Formatação limpa de horímetro (elimina dizimas de ponto flutuante como 218.45000000000002)
const formatHorimetro = (val) => {
  if (val === null || val === undefined) return '0.0';
  const num = Number(val);
  return Number.isInteger(num) ? num.toString() : num.toFixed(1);
};

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
    showFeedback('Ordem de serviço registrada com sucesso! A torre foi movida para "Em Manutenção".');
  } catch (err) {
    showFeedback('Falha ao registrar chamado de manutenção.');
  }
};

const quickOpenPreventive = (tower) => {
  newOrder.value.torreId = tower.id;
  newOrder.value.tipoFalha = 'Revisão Preventiva de Horímetro';
  newOrder.value.gravidade = 'ALTA';
  newOrder.value.descricao = `Torre atingiu ${formatHorimetro(tower.horasAtividadeAcumuladas)}h contínuas (limite: ${formatHorimetro(tower.limiteHorasPreventiva)}h). Revisão necessária.`;
  showCreateModal.value = true;
};

const updateStatus = async (id, status) => {
  await maintenanceStore.updateOrder(id, status);
  showFeedback('Status da ordem de serviço atualizado.');
};

const openResolutionModal = (order) => {
  orderToFinish.value = order;
  resolutionNotes.value = 'Manutenção e testes operacionais executados com sucesso.';
};

const confirmFinishOrder = async () => {
  if (!orderToFinish.value) return;
  await maintenanceStore.updateOrder(orderToFinish.value.id, 'CONCLUIDA', resolutionNotes.value);
  await towerStore.fetchTowers();
  showFeedback(`Manutenção da torre ${orderToFinish.value.codigoTorre} concluída com sucesso!`);
  orderToFinish.value = null;
};

const deleteOrder = async (id) => {
  try {
    await api.delete(`/maintenances/${id}`);
    await maintenanceStore.fetchOrders();
    showFeedback('Ordem de serviço excluída com sucesso.');
  } catch (err) {
    showFeedback('Não foi possível excluir a ordem.');
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
