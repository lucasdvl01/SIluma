<template>
  <div class="space-y-4 max-w-6xl mx-auto">
    <!-- Header -->
    <div class="flex justify-between items-center">
      <div>
        <h2 class="text-xl font-bold text-white tracking-tight flex items-center gap-2">
          <span>Trilha de Auditoria & Segurança Operacional</span>
          <span class="text-[10px] font-mono px-2 py-0.5 rounded bg-[#1e293b] text-slate-300 border border-[#334155]">
            RF12 • RNF-W18
          </span>
        </h2>
        <p class="text-xs text-slate-400">
          Rastreabilidade de comandos de acionamento, alteração de modos operacionais, cadastros e endereços IP
        </p>
      </div>

      <button
        @click="fetchLogs"
        class="px-3 py-1.5 rounded-lg bg-[#0e1626] hover:bg-[#16233b] text-xs font-semibold text-slate-300 border border-[#1c273c] flex items-center gap-2 transition-colors"
      >
        <span>Atualizar Log</span>
      </button>
    </div>

    <!-- Tabela de Auditoria -->
    <div class="bg-[#0e1626] border border-[#1c273c] rounded-xl overflow-hidden shadow-lg">
      <div class="overflow-x-auto">
        <table class="w-full text-left border-collapse">
          <thead>
            <tr class="border-b border-[#1c273c] bg-[#090e1a] text-[10px] font-mono font-bold text-slate-400 uppercase tracking-wider">
              <th class="p-3">Data & Hora</th>
              <th class="p-3">Usuário / Agente</th>
              <th class="p-3">Ação Registrada</th>
              <th class="p-3">Endereço IP</th>
              <th class="p-3">Detalhes do Evento</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-[#162033] text-xs text-slate-300 font-mono">
            <tr
              v-for="log in logs"
              :key="log.id"
              class="hover:bg-[#121c2e] transition-colors"
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
  if (acao.includes('LIGAR')) return 'bg-emerald-500/10 text-emerald-400 border-emerald-500/30';
  if (acao.includes('DESLIGAR')) return 'bg-rose-500/10 text-rose-400 border-rose-500/30';
  if (acao.includes('CERRACAO') || acao.includes('ALERTA')) return 'bg-amber-500/10 text-amber-400 border-amber-500/30';
  if (acao.includes('LOTE')) return 'bg-cyan-500/10 text-cyan-400 border-cyan-500/30';
  return 'bg-[#162033] text-slate-300 border-[#22314d]';
};

onMounted(() => {
  fetchLogs();
});
</script>
