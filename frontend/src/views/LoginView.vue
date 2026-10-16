<template>
  <div class="min-h-screen bg-mina-950 flex flex-col justify-center items-center p-4 relative overflow-hidden">
    <!-- Decorative mining background patterns -->
    <div class="absolute inset-0 bg-[radial-gradient(#1e3250_1px,transparent_1px)] [background-size:24px_24px] opacity-25"></div>
    <div class="absolute -top-40 -left-40 w-96 h-96 bg-amber-500/10 rounded-full blur-3xl pointer-events-none"></div>
    <div class="absolute -bottom-40 -right-40 w-96 h-96 bg-sky-500/10 rounded-full blur-3xl pointer-events-none"></div>

    <!-- Login Card -->
    <div class="w-full max-w-md bg-mina-900/90 border border-mina-darkBorder backdrop-blur-xl rounded-2xl shadow-2xl p-8 z-10 space-y-6">
      <!-- Header -->
      <div class="text-center space-y-2">
        <div class="w-14 h-14 mx-auto rounded-2xl bg-gradient-to-br from-amber-500 to-amber-600 flex items-center justify-center text-2xl shadow-xl shadow-amber-500/30">
          ⛏️
        </div>
        <h2 class="text-2xl font-black tracking-tight text-white">
          Minas<span class="text-amber-500">Lux</span>
        </h2>
        <p class="text-xs text-slate-400">
          Sistema de Gestão e Monitoramento de Iluminação Inteligente para Minas
        </p>
        <span class="inline-block text-[11px] font-bold px-2 py-0.5 rounded-full bg-amber-500/10 text-amber-400 border border-amber-500/30">
          SENAI Betim • CETEM
        </span>
      </div>

      <!-- Error message -->
      <div v-if="errorMessage" class="p-3 bg-rose-500/15 border border-rose-500/30 rounded-xl text-xs text-rose-300 flex items-center gap-2">
        <AlertTriangle class="w-4 h-4 shrink-0 text-rose-400" />
        <span>{{ errorMessage }}</span>
      </div>

      <!-- Form -->
      <form @submit.prevent="handleLogin" class="space-y-4">
        <div>
          <label class="block text-xs font-semibold text-slate-300 mb-1.5">E-mail Corporativo</label>
          <div class="relative">
            <input
              v-model="email"
              type="email"
              required
              placeholder="ex: operador@mineracao.com.br"
              class="w-full bg-mina-800 border border-mina-darkBorder rounded-xl px-4 py-2.5 text-sm text-white placeholder-slate-500 focus:outline-none focus:border-amber-500 focus:ring-1 focus:ring-amber-500 transition-all"
            />
          </div>
        </div>

        <div>
          <label class="block text-xs font-semibold text-slate-300 mb-1.5">Senha de Acesso</label>
          <input
            v-model="senha"
            type="password"
            required
            placeholder="Mínimo 8 caracteres"
            class="w-full bg-mina-800 border border-mina-darkBorder rounded-xl px-4 py-2.5 text-sm text-white placeholder-slate-500 focus:outline-none focus:border-amber-500 focus:ring-1 focus:ring-amber-500 transition-all"
          />
        </div>

        <button
          type="submit"
          :disabled="loading"
          class="w-full py-3 bg-gradient-to-r from-amber-500 to-amber-600 hover:from-amber-400 hover:to-amber-500 text-black font-extrabold rounded-xl text-sm shadow-lg shadow-amber-500/25 transition-all flex items-center justify-center gap-2 disabled:opacity-50"
        >
          <Loader2 v-if="loading" class="w-4 h-4 animate-spin" />
          <span v-else>Entrar no Sistema</span>
        </button>
      </form>

      <!-- Pre-fill Quick Demo Access -->
      <div class="pt-2 border-t border-mina-darkBorder space-y-2">
        <p class="text-[11px] text-center font-bold text-slate-500 uppercase tracking-wider">Acesso Rápido para Avaliação</p>
        <div class="grid grid-cols-2 gap-2">
          <button
            type="button"
            @click="fillAdmin"
            class="p-2.5 rounded-xl bg-mina-800/80 hover:bg-mina-700/80 border border-mina-darkBorder text-left transition-all group"
          >
            <div class="text-[11px] font-bold text-amber-400 group-hover:text-amber-300">Gestor CCO</div>
            <div class="text-[10px] text-slate-400 truncate">Perfil ADMIN (Total)</div>
          </button>

          <button
            type="button"
            @click="fillOperador"
            class="p-2.5 rounded-xl bg-mina-800/80 hover:bg-mina-700/80 border border-mina-darkBorder text-left transition-all group"
          >
            <div class="text-[11px] font-bold text-sky-400 group-hover:text-sky-300">Operador Campo</div>
            <div class="text-[10px] text-slate-400 truncate">Perfil OPERADOR</div>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '../stores/auth';
import { AlertTriangle, Loader2 } from 'lucide-vue-next';

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
