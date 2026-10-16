<template>
  <div class="space-y-6 max-w-6xl mx-auto">
    <!-- Header -->
    <div class="flex justify-between items-center">
      <div>
        <h2 class="text-2xl font-black text-white tracking-tight flex items-center gap-2">
          <span>Registro de Auditoria & Histórico de Operações</span>
          <span class="text-xs px-2.5 py-0.5 rounded-full bg-indigo-500/10 text-indigo-400 border border-indigo-500/20 font-bold">
            RF12 • RNF-W18
          </span>
        </h2>
        <p class="text-xs text-slate-400">
          Rastreamento de todas as ações de ligar/desligar, alteração de modo, cadastros e IP dos operadores
        </p>
      </div>

      <button
        @click="fetchLogs"
        class="px-3.5 py-2 rounded-xl bg-mina-900 hover:bg-mina-800 text-xs font-semibold text-slate-300 border border-mina-darkBorder flex items-center gap-2 transition-all"
      >
        Atualizar Registros
      </button>
    </div>

    <!-- Table -->
    <div class="bg-mina-900 border border-mina-darkBorder rounded-2xl overflow-hidden shadow-xl">
      <div class="overflow-x-auto">
        <table class="w-full text-left border-collapse">
          <thead>
            <tr class="border-b border-mina-darkBorder bg-mina-800/40 text-[11px] font-bold text-slate-400 uppercase tracking-wider">
              <th class="p-4">Data & Hora</th>
              <th class="p-4">Usuário / Agente</th>
              <th class="p-4">Ação Executada</th>
              <th class="p-4">Endereço IP</th>
              <th class="p-4">Detalhes da Ação</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-mina-darkBorder text-xs text-slate-300">
            <tr
              v-for="log in logs"
              :key="log.id"
              class="hover:bg-mina-800/40 transition-colors"
            >
              <td class="p-4 font-mono text-[11px] text-slate-400 whitespace-nowrap">
                {{ formatDate(log.dataHora) }}
              </td>

              <td class="p-4 font-semibold text-white">
                {{ log.usuarioEmail }}
              </td>

              <td class="p-4">
                <span
                  class="px-2.5 py-1 rounded-md text-[11px] font-bold font-mono"
                  :class="getActionBadgeClass(log.acao)"
                >
                  {{ log.acao }}
                </span>
              </td>

              <td class="p-4 font-mono text-amber-400 text-xs">
                {{ log.ipOrigem }}
              </td>

              <td class="p-4 text-slate-300 text-xs">
                {{ log.detalhes }}
              </td>
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

const fetchLogs = async () => {
  loading.value = true;
  try {
    const res = await api.get('/audit/logs');
    logs.value = res.data;
  } catch (err) {
    console.error('Erro ao buscar auditoria:', err);
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
  if (acao.includes('LIGAR')) return 'bg-emerald-500/20 text-emerald-300 border border-emerald-500/30';
  if (acao.includes('DESLIGAR')) return 'bg-rose-500/20 text-rose-300 border border-rose-500/30';
  if (acao.includes('CERRACAO') || acao.includes('ALERTA')) return 'bg-amber-500/20 text-amber-300 border border-amber-500/30';
  if (acao.includes('LOTE')) return 'bg-cyan-500/20 text-cyan-300 border border-cyan-500/30';
  return 'bg-slate-700 text-slate-200';
};

onMounted(() => {
  fetchLogs();
});
</script>
