<template>
  <div class="min-h-screen bg-[#060a12] flex flex-col justify-center items-center p-4 selection:bg-amber-400 selection:text-slate-950 relative overflow-hidden">
    <!-- Efeito de iluminação de fundo (Atmospheric Radial Glow) -->
    <div class="absolute top-1/4 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[600px] h-[400px] bg-amber-500/10 rounded-full blur-[120px] pointer-events-none"></div>

    <!-- Container Principal do Login -->
    <div class="w-full max-w-md tactile-panel rounded-2xl shadow-2xl p-8 space-y-6 relative z-10 border border-white/10">
      <!-- Cabeçalho Institucional -->
      <div class="space-y-3">
        <div class="flex items-center justify-between">
          <div class="flex items-center gap-2.5">
            <div class="w-9 h-9 rounded-xl bg-amber-400/15 border border-amber-400/30 flex items-center justify-center text-amber-400 shadow-sm">
              <Radio class="w-4 h-4" />
            </div>
            <span class="font-mono font-extrabold text-base tracking-wider uppercase text-white">MINAS<span class="text-amber-400">LUX</span></span>
          </div>
          <span class="text-[11px] font-mono px-2.5 py-1 rounded-lg bg-slate-900 text-slate-300 border border-white/10 font-bold">
            SENAI Betim
          </span>
        </div>

        <div>
          <h1 class="text-lg font-bold text-white tracking-tight font-sans">Centro de Controle Operacional</h1>
          <p class="text-xs text-slate-400 mt-0.5">
            Gestão e Monitoramento de Iluminação Móvel para Frentes de Lavra
          </p>
        </div>
      </div>

      <!-- Alerta de Erro -->
      <div v-if="errorMessage" class="p-3.5 bg-rose-500/15 border border-rose-500/30 rounded-xl text-xs text-rose-300 flex items-center gap-2.5">
        <TriangleAlert class="w-4 h-4 shrink-0 text-rose-400" />
        <span>{{ errorMessage }}</span>
      </div>

      <!-- Formulário de Autenticação -->
      <form @submit.prevent="handleLogin" class="space-y-4">
        <div>
          <label class="block text-xs font-semibold text-slate-300 mb-1.5">E-mail Corporativo</label>
          <input
            v-model="email"
            type="email"
            required
            placeholder="usuario@mineracao.com.br"
            class="w-full bg-slate-950 border border-slate-700/60 rounded-xl px-3.5 py-2.5 text-xs text-white placeholder-slate-500 focus:outline-none focus:border-amber-400 transition-colors font-sans"
          />
        </div>

        <div>
          <label class="block text-xs font-semibold text-slate-300 mb-1.5">Senha de Acesso</label>
          <input
            v-model="senha"
            type="password"
            required
            placeholder="Digite sua senha"
            class="w-full bg-slate-950 border border-slate-700/60 rounded-xl px-3.5 py-2.5 text-xs text-white placeholder-slate-500 focus:outline-none focus:border-amber-400 transition-colors font-sans"
          />
        </div>

        <button
          type="submit"
          :disabled="loading"
          class="w-full py-3 bg-amber-400 hover:bg-amber-300 text-slate-950 font-bold rounded-xl text-xs transition-all interactive-lift flex items-center justify-center gap-2 disabled:opacity-50 shadow-md shadow-amber-500/10"
        >
          <Loader2 v-if="loading" class="w-4 h-4 animate-spin text-slate-950" />
          <span v-else>Entrar no Sistema</span>
        </button>
      </form>

      <!-- Acesso Rápido para Avaliação -->
      <div class="pt-4 border-t border-white/5 space-y-2.5">
        <span class="text-[10px] font-mono text-slate-400 uppercase tracking-wider block text-center font-bold">
          Credenciais Predefinidas de Acesso
        </span>
        <div class="grid grid-cols-2 gap-2.5">
          <button
            type="button"
            @click="fillAdmin"
            class="p-3 rounded-xl bg-slate-950/80 hover:bg-slate-900 border border-white/5 text-left transition-all interactive-lift"
          >
            <div class="text-xs font-bold text-amber-400">Gestor CCO</div>
            <div class="text-[11px] text-slate-400 mt-0.5">Perfil Admin (Painel Web)</div>
          </button>

          <button
            type="button"
            @click="fillOperador"
            class="p-3 rounded-xl bg-slate-950/80 hover:bg-slate-900 border border-white/5 text-left transition-all interactive-lift"
          >
            <div class="text-xs font-bold text-sky-400">Operador</div>
            <div class="text-[11px] text-slate-400 mt-0.5">Terminal Móvel de Campo</div>
          </button>
        </div>
      </div>
    </div>

    <!-- Rodapé Técnico -->
    <footer class="mt-6 text-center text-xs text-slate-500 font-mono">
      MinasLux • Iluminação Inteligente para Minas • 2026
    </footer>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '../stores/auth';
import { Radio, TriangleAlert, Loader2 } from 'lucide-vue-next';

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
