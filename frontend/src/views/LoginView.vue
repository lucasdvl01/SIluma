<template>
  <div class="login-page relative flex min-h-screen flex-col items-center justify-center overflow-hidden bg-[#f5f7fa] p-4 selection:bg-amber-200 selection:text-slate-950 sm:p-6">
    <!-- Efeito de iluminação de fundo (Atmospheric Radial Glow) -->
    <div class="pointer-events-none absolute left-1/2 top-1/4 h-[400px] w-[600px] -translate-x-1/2 -translate-y-1/2 rounded-full bg-amber-400/10 blur-[120px]"></div>

    <!-- Container Principal do Login -->
    <div class="login-card relative z-10 w-full max-w-md space-y-6 rounded-3xl border border-slate-200 bg-white p-6 shadow-xl sm:p-8">
      <!-- Cabeçalho Institucional -->
      <div class="space-y-3">
        <div class="flex items-start justify-between gap-3">
          <img src="/si-luma-logo.svg" alt="SI Luma — Sistema de iluminação" class="brand-logo h-auto w-44 max-w-[65%]" />
          <span class="rounded-lg border border-slate-200 bg-slate-50 px-2.5 py-1 text-[11px] font-bold text-slate-600 font-mono">
            SENAI Betim
          </span>
        </div>

        <div>
          <p class="page-eyebrow mb-2">Sistema integrado de iluminação</p>
          <h1 class="font-sans text-xl font-bold tracking-tight text-slate-900">Centro de Controle Operacional</h1>
          <p class="mt-2 text-sm leading-relaxed text-slate-600">
            Gestão e Monitoramento de Iluminação Móvel para Frentes de Lavra
          </p>
        </div>
      </div>

      <!-- Alerta de Erro -->
      <div v-if="errorMessage" class="login-error flex items-center gap-2.5 rounded-xl border border-rose-200 bg-rose-50 p-3.5 text-xs text-rose-700">
        <TriangleAlert class="h-4 w-4 shrink-0 text-rose-600" />
        <span>{{ errorMessage }}</span>
      </div>

      <!-- Formulário de Autenticação -->
      <form @submit.prevent="handleLogin" class="space-y-4">
        <div>
          <label class="mb-1.5 block text-xs font-semibold text-slate-700">E-mail Corporativo</label>
          <input
            v-model="email"
            type="email"
            required
            placeholder="usuario@mineracao.com.br"
            class="min-h-12 w-full rounded-xl border border-slate-300 bg-white px-4 py-3 text-sm text-slate-900 placeholder-slate-400 transition-colors focus:border-amber-600 focus:outline-none font-sans"
          />
        </div>

        <div>
          <label class="mb-1.5 block text-xs font-semibold text-slate-700">Senha de Acesso</label>
          <input
            v-model="senha"
            type="password"
            required
            placeholder="Digite sua senha"
            class="min-h-12 w-full rounded-xl border border-slate-300 bg-white px-4 py-3 text-sm text-slate-900 placeholder-slate-400 transition-colors focus:border-amber-600 focus:outline-none font-sans"
          />
        </div>

        <button
          type="submit"
          :disabled="loading"
          class="interactive-lift flex min-h-12 w-full items-center justify-center gap-2 rounded-xl bg-amber-500 py-3 text-sm font-bold text-slate-950 shadow-md shadow-amber-500/10 transition-all hover:bg-amber-400 disabled:opacity-50"
        >
          <Loader2 v-if="loading" class="w-4 h-4 animate-spin text-slate-950" />
          <span v-else>Entrar no Sistema</span>
        </button>
      </form>

      <!-- Acesso Rápido para Avaliação -->
      <div class="space-y-2.5 border-t border-slate-100 pt-4">
        <span class="block text-center text-[10px] font-bold uppercase tracking-wider text-slate-500 font-mono">
          Credenciais Predefinidas de Acesso
        </span>
        <div class="grid grid-cols-2 gap-2.5">
          <button
            type="button"
            @click="fillAdmin"
            class="interactive-lift rounded-xl border border-slate-200 bg-slate-50 p-3 text-left transition-all hover:bg-slate-100"
          >
            <div class="text-xs font-bold text-amber-800">Gestor CCO</div>
            <div class="mt-0.5 text-[11px] text-slate-500">Perfil Admin (Painel Web)</div>
          </button>

          <button
            type="button"
            @click="fillOperador"
            class="interactive-lift rounded-xl border border-slate-200 bg-slate-50 p-3 text-left transition-all hover:bg-slate-100"
          >
            <div class="text-xs font-bold text-sky-700">Operador</div>
            <div class="mt-0.5 text-[11px] text-slate-500">Terminal Móvel de Campo</div>
          </button>
        </div>
      </div>
    </div>

    <ThemeToggle class="absolute right-4 top-4 z-10 sm:right-6 sm:top-6" />

    <!-- Rodapé Técnico -->
    <footer class="mt-6 text-center text-xs text-slate-500 font-mono">
      SI Luma • Iluminação inteligente para minas
    </footer>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '../stores/auth';
import ThemeToggle from '../components/ThemeToggle.vue';
import { TriangleAlert, Loader2 } from 'lucide-vue-next';

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
