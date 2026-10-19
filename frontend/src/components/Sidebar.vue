<template>
  <aside class="w-64 bg-mina-950/90 backdrop-blur-md border-r border-mina-700/40 flex flex-col h-screen select-none shrink-0 shadow-elevated">
    <!-- Brand / Cabeçalho Institucional CCO (About Face: Postura Soberana) -->
    <div class="px-5 py-4 border-b border-mina-700/40 flex items-center gap-3">
      <div class="w-9 h-9 rounded-xl bg-amber-500/10 border border-amber-500/30 flex items-center justify-center text-amber-400 shrink-0 shadow-sm">
        <Radio class="w-5 h-5 text-amber-400 animate-pulse" />
      </div>
      <div class="min-w-0">
        <div class="flex items-center gap-1.5">
          <span class="font-extrabold text-sm tracking-wider uppercase text-white font-mono">
            MINAS<span class="text-amber-400">LUX</span>
          </span>
          <span class="text-[9px] font-bold px-1.5 py-0.5 rounded-full bg-mina-800 text-slate-400 border border-mina-700/60 font-mono">
            v1.0
          </span>
        </div>
        <p class="text-[11px] text-slate-400 font-medium truncate tracking-tight">SENAI Betim • CETEM</p>
      </div>
    </div>

    <!-- Navigation Menu (Refactoring UI: Contraste suave, sem bordas pesadas por item) -->
    <nav class="flex-1 px-3 py-4 space-y-1 overflow-y-auto">
      <div class="px-3 pt-1 pb-2 text-[10px] font-bold text-slate-400 uppercase tracking-widest font-mono">
        {{ authStore.isAdmin ? 'Gestão da Mina (CCO)' : 'Terminal Operacional' }}
      </div>

      <!-- Links para ADMIN -->
      <template v-if="authStore.isAdmin">
        <router-link
          to="/dashboard"
          class="flex items-center gap-3 px-3 py-2.5 rounded-xl text-xs font-medium transition-all group interactive-lift"
          :class="$route.path === '/dashboard' 
            ? 'bg-amber-500/15 text-amber-300 font-semibold shadow-sm' 
            : 'text-slate-400 hover:text-slate-100 hover:bg-mina-850'"
        >
          <BarChart3 class="w-4 h-4 shrink-0 transition-transform group-hover:scale-110" :class="$route.path === '/dashboard' ? 'text-amber-400' : 'text-slate-400'" />
          <span>Dashboard Indicadores</span>
        </router-link>

        <router-link
          to="/mapa"
          class="flex items-center gap-3 px-3 py-2.5 rounded-xl text-xs font-medium transition-all group interactive-lift"
          :class="$route.path === '/mapa' 
            ? 'bg-emerald-500/15 text-emerald-300 font-semibold shadow-sm' 
            : 'text-slate-400 hover:text-slate-100 hover:bg-mina-850'"
        >
          <MapPin class="w-4 h-4 shrink-0 transition-transform group-hover:scale-110" :class="$route.path === '/mapa' ? 'text-emerald-400' : 'text-slate-400'" />
          <span>Mapa Operacional</span>
        </router-link>

        <router-link
          to="/torres"
          class="flex items-center gap-3 px-3 py-2.5 rounded-xl text-xs font-medium transition-all group interactive-lift"
          :class="$route.path === '/torres' 
            ? 'bg-sky-500/15 text-sky-300 font-semibold shadow-sm' 
            : 'text-slate-400 hover:text-slate-100 hover:bg-mina-850'"
        >
          <RadioTower class="w-4 h-4 shrink-0 transition-transform group-hover:scale-110" :class="$route.path === '/torres' ? 'text-sky-400' : 'text-slate-400'" />
          <span>Frota de Torres</span>
        </router-link>

        <router-link
          to="/lote"
          class="flex items-center gap-3 px-3 py-2.5 rounded-xl text-xs font-medium transition-all group interactive-lift"
          :class="$route.path === '/lote' 
            ? 'bg-cyan-500/15 text-cyan-300 font-semibold shadow-sm' 
            : 'text-slate-400 hover:text-slate-100 hover:bg-mina-850'"
        >
          <Layers class="w-4 h-4 shrink-0 transition-transform group-hover:scale-110" :class="$route.path === '/lote' ? 'text-cyan-400' : 'text-slate-400'" />
          <span>Comandos em Lote</span>
        </router-link>
      </template>

      <!-- Links Comuns -->
      <router-link
        to="/manutencoes"
        class="flex items-center gap-3 px-3 py-2.5 rounded-xl text-xs font-medium transition-all group interactive-lift"
        :class="$route.path === '/manutencoes' 
          ? 'bg-rose-500/15 text-rose-300 font-semibold shadow-sm' 
          : 'text-slate-400 hover:text-slate-100 hover:bg-mina-850'"
      >
        <Wrench class="w-4 h-4 shrink-0 transition-transform group-hover:scale-110" :class="$route.path === '/manutencoes' ? 'text-rose-400' : 'text-slate-400'" />
        <span>Manutenções & Horímetro</span>
      </router-link>

      <router-link
        to="/operador"
        class="flex items-center gap-3 px-3 py-2.5 rounded-xl text-xs font-medium transition-all group interactive-lift"
        :class="$route.path === '/operador' 
          ? 'bg-amber-500/15 text-amber-300 font-semibold shadow-sm' 
          : 'text-slate-400 hover:text-slate-100 hover:bg-mina-850'"
      >
        <Smartphone class="w-4 h-4 shrink-0 transition-transform group-hover:scale-110" :class="$route.path === '/operador' ? 'text-amber-400' : 'text-slate-400'" />
        <span>Terminal Operador (Campo)</span>
      </router-link>

      <router-link
        to="/simulador-iot"
        class="flex items-center gap-3 px-3 py-2.5 rounded-xl text-xs font-medium transition-all group interactive-lift"
        :class="$route.path === '/simulador-iot' 
          ? 'bg-emerald-500/15 text-emerald-300 font-semibold shadow-sm' 
          : 'text-slate-400 hover:text-slate-100 hover:bg-mina-850'"
      >
        <Cpu class="w-4 h-4 shrink-0 transition-transform group-hover:scale-110" :class="$route.path === '/simulador-iot' ? 'text-emerald-400' : 'text-slate-400'" />
        <span>Bancada IoT (ESP32)</span>
      </router-link>

      <router-link
        v-if="authStore.isAdmin"
        to="/auditoria"
        class="flex items-center gap-3 px-3 py-2.5 rounded-xl text-xs font-medium transition-all group interactive-lift"
        :class="$route.path === '/auditoria' 
          ? 'bg-indigo-500/15 text-indigo-300 font-semibold shadow-sm' 
          : 'text-slate-400 hover:text-slate-100 hover:bg-mina-850'"
      >
        <ShieldCheck class="w-4 h-4 shrink-0 transition-transform group-hover:scale-110" :class="$route.path === '/auditoria' ? 'text-indigo-400' : 'text-slate-400'" />
        <span>Auditoria & Histórico</span>
      </router-link>
    </nav>

    <!-- Footer de Conexão e Perfil do Usuário (Rich Modeless Visual Feedback) -->
    <div class="p-3 border-t border-mina-700/40 bg-mina-950/80 space-y-2.5">
      <!-- Status Rede Modeless -->
      <div class="flex items-center justify-between text-[11px] px-3 py-2 rounded-xl bg-mina-900 border border-mina-700/30">
        <div class="flex items-center gap-2.5">
          <span class="relative flex h-2 w-2">
            <span v-if="offlineStore.isOnline" class="animate-ping absolute inline-flex h-full w-full rounded-full bg-emerald-400 opacity-75"></span>
            <span class="relative inline-flex rounded-full h-2 w-2" :class="offlineStore.isOnline ? 'bg-emerald-500' : 'bg-rose-500'"></span>
          </span>
          <span class="text-slate-300 font-medium">
            {{ offlineStore.isOnline ? 'Telemetria: Ativa' : 'Operação Offline' }}
          </span>
        </div>
        <span v-if="offlineStore.pendingCount > 0" class="text-[10px] px-1.5 py-0.5 rounded-full bg-amber-500/20 text-amber-300 font-mono font-bold tabular-nums">
          {{ offlineStore.pendingCount }} fila
        </span>
      </div>

      <!-- User Profile Card -->
      <div class="flex items-center justify-between p-1.5 rounded-xl bg-mina-900/60 border border-mina-700/20">
        <div class="flex items-center gap-2.5 min-w-0">
          <div class="w-8 h-8 rounded-lg bg-mina-800 border border-mina-700 flex items-center justify-center text-xs font-mono font-bold text-amber-400 shrink-0">
            {{ authStore.userName.substring(0, 2).toUpperCase() }}
          </div>
          <div class="min-w-0">
            <p class="text-xs font-bold text-slate-100 truncate leading-tight">{{ authStore.userName }}</p>
            <p class="text-[10px] text-slate-400 font-mono truncate uppercase tracking-wider">{{ authStore.userRole }}</p>
          </div>
        </div>
        <button
          @click="logout"
          title="Encerrar Sessão"
          class="p-2 text-slate-400 hover:text-rose-400 hover:bg-rose-500/10 rounded-lg transition-all"
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
  Radio,
  BarChart3,
  MapPin,
  RadioTower,
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
