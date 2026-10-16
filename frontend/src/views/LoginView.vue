<template>
  <div class="min-h-screen bg-[#070b14] flex flex-col justify-center items-center p-4 selection:bg-amber-500 selection:text-black">
    <!-- Container Principal do Login -->
    <div class="w-full max-w-md bg-[#0e1626] border border-[#1c273c] rounded-xl shadow-xl p-8 space-y-6">
      <!-- Cabeçalho Institucional -->
      <div class="space-y-3">
        <div class="flex items-center justify-between">
          <div class="flex items-center gap-2">
            <div class="w-8 h-8 rounded-lg bg-amber-500/10 border border-amber-500/30 flex items-center justify-center text-amber-500">
              <Radio class="w-4 h-4" />
            </div>
            <span class="font-mono font-extrabold text-base tracking-wider uppercase text-white">MINAS<span class="text-amber-500">LUX</span></span>
          </div>
          <span class="text-[10px] font-mono px-2 py-0.5 rounded bg-[#162033] text-slate-300 border border-[#22314d]">
            SENAI Betim
          </span>
        </div>

        <div>
          <h2 class="text-lg font-bold text-white tracking-tight">Centro de Controle Operacional</h2>
          <p class="text-xs text-slate-400 mt-0.5">
            Gestão e Monitoramento de Iluminação Móvel para Frentes de Lavra
          </p>
        </div>
      </div>

      <!-- Alerta de Erro -->
      <div v-if="errorMessage" class="p-3 bg-rose-500/10 border border-rose-500/30 rounded-lg text-xs text-rose-300 flex items-center gap-2">
        <AlertTriangle class="w-4 h-4 shrink-0 text-rose-400" />
        <span>{{ errorMessage }}</span>
      </div>

      <!-- Formulário de Autenticação (RF07) -->
      <form @submit.prevent="handleLogin" class="space-y-4">
        <div>
          <label class="block text-xs font-semibold text-slate-300 mb-1">E-mail Corporativo</label>
          <input
            v-model="email"
            type="email"
            required
            placeholder="usuario@mineracao.com.br"
            class="w-full bg-[#080d17] border border-[#1e2a40] rounded-lg px-3.5 py-2 text-xs text-white placeholder-slate-500 focus:outline-none focus:border-amber-500 transition-colors"
          />
        </div>

        <div>
          <div class="flex justify-between items-center mb-1">
            <label class="block text-xs font-semibold text-slate-300">Senha de Acesso</label>
            <span class="text-[10px] text-slate-500 font-mono">BCrypt 10+</span>
          </div>
          <input
            v-model="senha"
            type="password"
            required
            placeholder="Mínimo de 8 caracteres"
            class="w-full bg-[#080d17] border border-[#1e2a40] rounded-lg px-3.5 py-2 text-xs text-white placeholder-slate-500 focus:outline-none focus:border-amber-500 transition-colors"
          />
        </div>

        <button
          type="submit"
          :disabled="loading"
          class="w-full py-2.5 bg-amber-500 hover:bg-amber-400 text-black font-bold rounded-lg text-xs transition-colors flex items-center justify-center gap-2 disabled:opacity-50"
        >
          <Loader2 v-if="loading" class="w-3.5 h-3.5 animate-spin" />
          <span v-else>Autenticar no CCO</span>
        </button>
      </form>

      <!-- Acesso Rápido para Avaliação dos Professores / Examinadores -->
      <div class="pt-4 border-t border-[#1a2336] space-y-2">
        <span class="text-[10px] font-mono text-slate-500 uppercase tracking-wider block text-center">
          Credenciais Predefinidas de Avaliação
        </span>
        <div class="grid grid-cols-2 gap-2">
          <button
            type="button"
            @click="fillAdmin"
            class="p-2.5 rounded-lg bg-[#080d17] hover:bg-[#131d30] border border-[#1e2a40] text-left transition-colors"
          >
            <div class="text-[11px] font-bold text-amber-400">Gestor CCO</div>
            <div class="text-[10px] text-slate-400">Perfil ADMIN (Acesso Total)</div>
          </button>

          <button
            type="button"
            @click="fillOperador"
            class="p-2.5 rounded-lg bg-[#080d17] hover:bg-[#131d30] border border-[#1e2a40] text-left transition-colors"
          >
            <div class="text-[11px] font-bold text-sky-400">Operador de Campo</div>
            <div class="text-[10px] text-slate-400">Perfil OPERADOR (Mobile)</div>
          </button>
        </div>
      </div>
    </div>

    <!-- Rodapé Técnico -->
    <div class="mt-6 text-center text-[11px] text-slate-400 font-mono">
      Sistema em conformidade com os requisitos consolidados do SENAI Betim • 2026
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '../stores/auth';
import { Radio, AlertTriangle, Loader2 } from 'lucide-vue-next';

const email = ref('admin@mineracao.com.br');
const senha = ref('admin123');
const loading = ref(false);
const errorMessage = ref('');

const authStore = useAuthStore();
const router = useRouter();

const handleLogin = async () => {
  loading.value = true;
  errorMessage.value = '';
  try {
    const user = await authStore.login(email.value, senha.value);
    if (user.perfil === 'OPERADOR') {
      router.push('/operador');
    } else {
      router.push('/dashboard');
    }
  } catch (err) {
    errorMessage.value = err.response?.data?.message || 'Falha na autenticação. Verifique seu e-mail e senha.';
  } finally {
    loading.value = false;
  }
};

const fillAdmin = () => {
  email.value = 'admin@mineracao.com.br';
  senha.value = 'admin123';
};

const fillOperador = () => {
  email.value = 'operador@mineracao.com.br';
  senha.value = 'operador123';
};
</script>
