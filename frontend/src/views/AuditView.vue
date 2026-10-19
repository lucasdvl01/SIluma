<template>
  <div class="space-y-4 max-w-6xl mx-auto">
    <!-- Header -->
    <header class="flex justify-between items-center pb-4 border-b border-white/5">
      <div>
        <div class="flex items-center gap-2.5">
          <h1 class="text-xl font-bold text-white tracking-tight font-sans">
            Trilha de Auditoria & Conformidade
          </h1>
          <span class="text-xs font-mono font-bold px-2.5 py-1 rounded-xl bg-slate-900 text-slate-300 border border-white/10">
            Registro Imutável
          </span>
        </div>
        <p class="text-xs text-slate-400 mt-0.5">
          Rastreabilidade de comandos, alteração de modos operacionais, cadastros e endereços IP de origem
        </p>
      </div>

      <button
        @click="fetchLogs"
        class="px-3.5 py-2 rounded-xl bg-slate-900 hover:bg-slate-800 text-xs font-semibold text-slate-200 border border-white/10 flex items-center gap-2 transition-all interactive-lift shadow-sm"
      >
        <span>Atualizar Log</span>
      </button>
    </header>

    <!-- Tabela de Auditoria -->
    <div class="tactile-panel rounded-2xl overflow-hidden shadow-2xl">
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
