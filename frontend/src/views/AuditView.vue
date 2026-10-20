<template>
  <div class="space-y-6 max-w-6xl mx-auto pb-6">
    <!-- Header -->
    <header class="page-header flex flex-col sm:flex-row justify-between items-start sm:items-center gap-5">
      <div>
        <div class="flex flex-wrap items-center gap-2.5">
          <h1 class="font-sans">
            Trilha de Auditoria & Conformidade
          </h1>
          <span class="text-xs font-mono font-bold px-2.5 py-1 rounded-xl bg-amber-500/10 text-amber-400 border border-amber-500/20">
            Registro Imutável
          </span>
        </div>
        <p class="mt-2">
          Rastreabilidade de comandos, alteração de modos operacionais, cadastros e endereços IP de origem
        </p>
      </div>

      <button
        @click="fetchLogs"
        :disabled="loading"
        class="min-h-11 px-4 py-2.5 rounded-xl bg-amber-400 hover:bg-amber-300 text-xs font-semibold text-slate-950 flex items-center gap-2 transition-all interactive-lift shadow-sm disabled:cursor-wait disabled:opacity-60"
      >
        <span>{{ loading ? 'Atualizando…' : 'Atualizar log' }}</span>
      </button>
    </header>

    <div v-if="errorMessage" role="alert" class="rounded-xl border border-rose-500/30 bg-rose-500/10 px-4 py-3 text-sm text-rose-200">
      {{ errorMessage }}
    </div>

    <!-- Tabela de Auditoria -->
    <div class="tactile-panel rounded-2xl overflow-hidden">
      <div class="overflow-x-auto">
        <table class="w-full min-w-[760px] text-left border-collapse">
          <thead>
            <tr class="border-b border-white/10 bg-slate-50 text-[10px] font-mono font-bold text-slate-500 uppercase tracking-wider">
              <th class="p-3">Data & Hora</th>
              <th class="p-3">Usuário / Agente</th>
              <th class="p-3">Ação Registrada</th>
              <th class="p-3">Endereço IP</th>
              <th class="p-3">Detalhes do Evento</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-white/5 text-xs text-slate-300 font-mono">
            <tr
              v-for="log in logs"
              :key="log.id"
              class="transition-colors hover:bg-slate-50"
            >
              <td class="p-3 text-[11px] text-slate-400 whitespace-nowrap">
                {{ formatDate(log.dataHora) }}
              </td>

              <td class="p-3 font-sans font-medium text-white">
                {{ log.usuarioEmail }}
              </td>

              <td class="p-3">
                <span
                  class="px-2 py-0.5 rounded text-[10px] font-bold border"
                  :class="getActionBadgeClass(log.acao)"
                >
                  {{ log.acao }}
                </span>
              </td>

              <td class="p-3 text-amber-400 text-xs">
                {{ log.ipOrigem }}
              </td>

              <td class="p-3 font-sans text-slate-300 text-xs">
                {{ log.detalhes }}
              </td>
            </tr>
            <tr v-if="!loading && !errorMessage && logs.length === 0">
              <td colspan="5" class="px-6 py-12 text-center font-sans">
                <p class="text-sm font-semibold text-white">Nenhum registro de auditoria</p>
                <p class="mt-1 text-xs text-slate-500">As ações registradas aparecerão aqui.</p>
              </td>
            </tr>
            <tr v-if="loading && logs.length === 0">
              <td colspan="5" class="px-6 py-12 text-center font-sans text-sm text-slate-500">Carregando registros…</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import api from '../services/api';

const logs = ref([]);
const loading = ref(false);
const errorMessage = ref('');

const fetchLogs = async () => {
  loading.value = true;
  errorMessage.value = '';
  try {
    const res = await api.get('/audit/logs');
    logs.value = res.data;
  } catch (err) {
    console.error('Erro ao buscar auditoria:', err);
    errorMessage.value = 'Não foi possível carregar os registros. Verifique a conexão e tente novamente.';
  } finally {
    loading.value = false;
  }
};

const formatDate = (dateStr) => {
  if (!dateStr) return '';
  const d = new Date(dateStr);
  return d.toLocaleDateString('pt-BR') + ' ' + d.toLocaleTimeString('pt-BR');
};

const getActionBadgeClass = (acao) => {
  if (acao.includes('LIGAR')) return 'bg-emerald-500/10 text-emerald-400 border-emerald-500/30';
  if (acao.includes('DESLIGAR')) return 'bg-rose-500/10 text-rose-400 border-rose-500/30';
  if (acao.includes('CERRACAO') || acao.includes('ALERTA')) return 'bg-amber-500/10 text-amber-400 border-amber-500/30';
  if (acao.includes('LOTE')) return 'bg-cyan-500/10 text-cyan-400 border-cyan-500/30';
  return 'bg-slate-100 text-slate-700 border-slate-200';
};

onMounted(() => {
  fetchLogs();
});
</script>
