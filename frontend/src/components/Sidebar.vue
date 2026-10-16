<template>
  <aside class="w-64 bg-mina-900 border-r border-mina-darkBorder flex flex-col h-screen select-none shrink-0">
    <!-- Brand / Logo -->
    <div class="p-5 border-b border-mina-darkBorder flex items-center gap-3">
      <div class="w-10 h-10 rounded-xl bg-gradient-to-br from-amber-500 to-amber-700 flex items-center justify-center text-xl font-black text-black shadow-lg shadow-amber-500/20">
        ⛏️
      </div>
      <div>
        <h1 class="font-extrabold text-sm tracking-wider uppercase text-slate-100 flex items-center gap-1.5">
          Minas<span class="text-amber-500">Lux</span>
        </h1>
        <p class="text-[11px] text-slate-400 font-medium">SENAI Betim • CCO</p>
      </div>
    </div>

    <!-- Navigation Menu -->
    <nav class="flex-1 p-3 space-y-1 overflow-y-auto">
      <div class="px-3 py-2 text-[10px] font-bold text-slate-500 uppercase tracking-widest">
        {{ authStore.isAdmin ? 'Painel de Controle' : 'Operação de Campo' }}
      </div>

      <!-- Links para ADMIN -->
      <template v-if="authStore.isAdmin">
        <router-link
          to="/dashboard"
          class="flex items-center gap-3 px-3.5 py-2.5 rounded-lg text-sm font-semibold transition-all"
          :class="$route.path === '/dashboard' ? 'bg-amber-500/10 text-amber-400 border border-amber-500/30' : 'text-slate-300 hover:bg-mina-800/60 hover:text-white'"
        >
          <BarChart3 class="w-4 h-4 text-amber-500" />
          Dashboard Indicadores
        </router-link>

        <router-link
          to="/mapa"
          class="flex items-center gap-3 px-3.5 py-2.5 rounded-lg text-sm font-semibold transition-all"
          :class="$route.path === '/mapa' ? 'bg-amber-500/10 text-amber-400 border border-amber-500/30' : 'text-slate-300 hover:bg-mina-800/60 hover:text-white'"
        >
          <MapPin class="w-4 h-4 text-emerald-400" />
          Mapa da Mina
        </router-link>

        <router-link
          to="/torres"
          class="flex items-center gap-3 px-3.5 py-2.5 rounded-lg text-sm font-semibold transition-all"
          :class="$route.path === '/torres' ? 'bg-amber-500/10 text-amber-400 border border-amber-500/30' : 'text-slate-300 hover:bg-mina-800/60 hover:text-white'"
        >
          <Zap class="w-4 h-4 text-yellow-400" />
          Frota de Torres
        </router-link>

        <router-link
          to="/lote"
          class="flex items-center gap-3 px-3.5 py-2.5 rounded-lg text-sm font-semibold transition-all"
          :class="$route.path === '/lote' ? 'bg-amber-500/10 text-amber-400 border border-amber-500/30' : 'text-slate-300 hover:bg-mina-800/60 hover:text-white'"
        >
          <Layers class="w-4 h-4 text-cyan-400" />
          Comandos em Lote
        </router-link>
      </template>

      <!-- Links Comuns -->
      <router-link
        to="/manutencoes"
        class="flex items-center gap-3 px-3.5 py-2.5 rounded-lg text-sm font-semibold transition-all"
        :class="$route.path === '/manutencoes' ? 'bg-amber-500/10 text-amber-400 border border-amber-500/30' : 'text-slate-300 hover:bg-mina-800/60 hover:text-white'"
      >
        <Wrench class="w-4 h-4 text-rose-400" />
        Manutenções & Falhas
      </router-link>

      <router-link
        to="/operador"
        class="flex items-center gap-3 px-3.5 py-2.5 rounded-lg text-sm font-semibold transition-all"
        :class="$route.path === '/operador' ? 'bg-amber-500/10 text-amber-400 border border-amber-500/30' : 'text-slate-300 hover:bg-mina-800/60 hover:text-white'"
      >
        <Smartphone class="w-4 h-4 text-sky-400" />
        App Operador (Mobile)
      </router-link>

      <router-link
        to="/simulador-iot"
        class="flex items-center gap-3 px-3.5 py-2.5 rounded-lg text-sm font-semibold transition-all"
        :class="$route.path === '/simulador-iot' ? 'bg-amber-500/10 text-amber-400 border border-amber-500/30' : 'text-slate-300 hover:bg-mina-800/60 hover:text-white'"
      >
        <Cpu class="w-4 h-4 text-emerald-400" />
        Simulador IoT (ESP32)
      </router-link>

      <router-link
        v-if="authStore.isAdmin"
        to="/auditoria"
        class="flex items-center gap-3 px-3.5 py-2.5 rounded-lg text-sm font-semibold transition-all"
        :class="$route.path === '/auditoria' ? 'bg-amber-500/10 text-amber-400 border border-amber-500/30' : 'text-slate-300 hover:bg-mina-800/60 hover:text-white'"
      >
        <ShieldCheck class="w-4 h-4 text-indigo-400" />
        Auditoria & Logs
      </router-link>
    </nav>

    <!-- Network status & User info footer -->
    <div class="p-4 border-t border-mina-darkBorder bg-mina-950/40 space-y-3">
      <!-- Status Conexão -->
      <div class="flex items-center justify-between text-xs px-2 py-1.5 rounded-md bg-mina-800/40">
        <span class="flex items-center gap-2">
          <span class="w-2 h-2 rounded-full" :class="offlineStore.isOnline ? 'bg-emerald-400 animate-pulse' : 'bg-red-400'"></span>
          <span class="text-slate-400">{{ offlineStore.isOnline ? 'Online' : 'Modo Offline' }}</span>
        </span>
        <span v-if="offlineStore.pendingCount > 0" class="text-[10px] px-1.5 py-0.5 rounded bg-amber-500/20 text-amber-400 font-bold">
          {{ offlineStore.pendingCount }} na fila
        </span>
      </div>

      <!-- User Card -->
      <div class="flex items-center justify-between pt-1">
        <div class="flex items-center gap-2.5 min-w-0">
          <div class="w-8 h-8 rounded-lg bg-mina-700 flex items-center justify-center text-xs font-bold text-amber-400 shrink-0">
            {{ authStore.userName.substring(0, 2).toUpperCase() }}
          </div>
          <div class="min-w-0">
            <p class="text-xs font-bold text-white truncate">{{ authStore.userName }}</p>
            <span class="inline-block text-[10px] font-semibold px-1.5 py-0.2 rounded" :class="authStore.isAdmin ? 'bg-amber-500/20 text-amber-300' : 'bg-sky-500/20 text-sky-300'">
              {{ authStore.userRole }}
            </span>
          </div>
        </div>
        <button
          @click="logout"
          title="Sair do Sistema"
          class="p-1.5 text-slate-400 hover:text-rose-400 hover:bg-rose-500/10 rounded-lg transition-colors"
        >
          <LogOut class="w-4 h-4" />
        </button>
      </div>
    </div>
  </aside>
</template>

<script setup>
import { useAuthStore } from '../stores/auth';
import { useOfflineSyncStore } from '../stores/offlineSync';
import { useRouter } from 'vue-router';
import {
  BarChart3,
  MapPin,
  Zap,
  Layers,
  Wrench,
  Smartphone,
  Cpu,
  ShieldCheck,
  LogOut
} from 'lucide-vue-next';

const authStore = useAuthStore();
const offlineStore = useOfflineSyncStore();
const router = useRouter();

const logout = () => {
  authStore.logout();
  router.push('/login');
};
</script>
