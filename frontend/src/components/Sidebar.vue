<template>
  <aside class="w-64 bg-[#0a0f1d] border-r border-[#1a2336] flex flex-col h-screen select-none shrink-0">
    <!-- Brand / Cabeçalho Institucional CCO -->
    <div class="px-5 py-4 border-b border-[#1a2336] flex items-center gap-3">
      <div class="w-9 h-9 rounded-lg bg-amber-500/10 border border-amber-500/30 flex items-center justify-center text-amber-400 shrink-0">
        <Radio class="w-5 h-5 text-amber-500" />
      </div>
      <div class="min-w-0">
        <div class="flex items-center gap-1.5">
          <span class="font-extrabold text-sm tracking-wider uppercase text-white font-mono">MINAS<span class="text-amber-500">LUX</span></span>
          <span class="text-[9px] font-bold px-1.5 py-0.2 rounded bg-[#1e293b] text-slate-400 border border-[#334155]">v1.0</span>
        </div>
        <p class="text-[11px] text-slate-400 font-medium truncate">SENAI Betim • CETEM</p>
      </div>
    </div>

    <!-- Navigation Menu -->
    <nav class="flex-1 p-3 space-y-1 overflow-y-auto">
      <div class="px-3 pt-2 pb-1.5 text-[10px] font-bold text-slate-500 uppercase tracking-widest">
        {{ authStore.isAdmin ? 'Gestão da Mina (CCO)' : 'Terminal Operacional' }}
      </div>

      <!-- Links para ADMIN -->
      <template v-if="authStore.isAdmin">
        <router-link
          to="/dashboard"
          class="flex items-center gap-3 px-3 py-2 rounded-lg text-xs font-semibold transition-colors"
          :class="$route.path === '/dashboard' ? 'bg-amber-500/10 text-amber-400 border border-amber-500/30' : 'text-slate-300 hover:bg-[#131b2e] hover:text-white'"
        >
          <BarChart3 class="w-4 h-4 text-amber-400 shrink-0" />
          Dashboard Indicadores
        </router-link>

        <router-link
          to="/mapa"
          class="flex items-center gap-3 px-3 py-2 rounded-lg text-xs font-semibold transition-colors"
          :class="$route.path === '/mapa' ? 'bg-amber-500/10 text-amber-400 border border-amber-500/30' : 'text-slate-300 hover:bg-[#131b2e] hover:text-white'"
        >
          <MapPin class="w-4 h-4 text-emerald-400 shrink-0" />
          Mapa Operacional
        </router-link>

        <router-link
          to="/torres"
          class="flex items-center gap-3 px-3 py-2 rounded-lg text-xs font-semibold transition-colors"
          :class="$route.path === '/torres' ? 'bg-amber-500/10 text-amber-400 border border-amber-500/30' : 'text-slate-300 hover:bg-[#131b2e] hover:text-white'"
        >
          <RadioTower class="w-4 h-4 text-sky-400 shrink-0" />
          Frota de Torres
        </router-link>

        <router-link
          to="/lote"
          class="flex items-center gap-3 px-3 py-2 rounded-lg text-xs font-semibold transition-colors"
          :class="$route.path === '/lote' ? 'bg-amber-500/10 text-amber-400 border border-amber-500/30' : 'text-slate-300 hover:bg-[#131b2e] hover:text-white'"
        >
          <Layers class="w-4 h-4 text-cyan-400 shrink-0" />
          Comandos em Lote
        </router-link>
      </template>

      <!-- Links Comuns -->
      <router-link
        to="/manutencoes"
        class="flex items-center gap-3 px-3 py-2 rounded-lg text-xs font-semibold transition-colors"
        :class="$route.path === '/manutencoes' ? 'bg-amber-500/10 text-amber-400 border border-amber-500/30' : 'text-slate-300 hover:bg-[#131b2e] hover:text-white'"
      >
        <Wrench class="w-4 h-4 text-rose-400 shrink-0" />
        Manutenções & Horímetro
      </router-link>

      <router-link
        to="/operador"
        class="flex items-center gap-3 px-3 py-2 rounded-lg text-xs font-semibold transition-colors"
        :class="$route.path === '/operador' ? 'bg-amber-500/10 text-amber-400 border border-amber-500/30' : 'text-slate-300 hover:bg-[#131b2e] hover:text-white'"
      >
        <Smartphone class="w-4 h-4 text-amber-400 shrink-0" />
        Terminal Operador (Campo)
      </router-link>

      <router-link
        to="/simulador-iot"
        class="flex items-center gap-3 px-3 py-2 rounded-lg text-xs font-semibold transition-colors"
        :class="$route.path === '/simulador-iot' ? 'bg-amber-500/10 text-amber-400 border border-amber-500/30' : 'text-slate-300 hover:bg-[#131b2e] hover:text-white'"
      >
        <Cpu class="w-4 h-4 text-emerald-400 shrink-0" />
        Bancada IoT (ESP32)
      </router-link>

      <router-link
        v-if="authStore.isAdmin"
        to="/auditoria"
        class="flex items-center gap-3 px-3 py-2 rounded-lg text-xs font-semibold transition-colors"
        :class="$route.path === '/auditoria' ? 'bg-amber-500/10 text-amber-400 border border-amber-500/30' : 'text-slate-300 hover:bg-[#131b2e] hover:text-white'"
      >
        <ShieldCheck class="w-4 h-4 text-indigo-400 shrink-0" />
        Auditoria & Histórico
      </router-link>
    </nav>

    <!-- Footer de Conexão e Perfil do Usuário -->
    <div class="p-3 border-t border-[#1a2336] bg-[#070b14] space-y-2.5">
      <!-- Status Rede -->
      <div class="flex items-center justify-between text-[11px] px-2.5 py-1.5 rounded-lg bg-[#0e1626] border border-[#1a2336]">
        <div class="flex items-center gap-2">
          <span class="w-2 h-2 rounded-full" :class="offlineStore.isOnline ? 'bg-emerald-500' : 'bg-rose-500'"></span>
          <span class="text-slate-300 font-medium">{{ offlineStore.isOnline ? 'Rede Telemetria: Ativa' : 'Operação Local (Offline)' }}</span>
        </div>
        <span v-if="offlineStore.pendingCount > 0" class="text-[10px] px-1.5 py-0.2 rounded bg-amber-500/20 text-amber-400 font-mono font-bold">
          {{ offlineStore.pendingCount }} fila
        </span>
      </div>

      <!-- User Profile Card -->
      <div class="flex items-center justify-between pt-1">
        <div class="flex items-center gap-2 min-w-0">
          <div class="w-7 h-7 rounded-md bg-[#1e293b] border border-[#334155] flex items-center justify-center text-[11px] font-mono font-bold text-amber-400 shrink-0">
            {{ authStore.userName.substring(0, 2).toUpperCase() }}
          </div>
          <div class="min-w-0">
            <p class="text-xs font-bold text-slate-100 truncate leading-none">{{ authStore.userName }}</p>
            <p class="text-[10px] text-slate-400 font-mono mt-0.5">{{ authStore.userRole }}</p>
          </div>
        </div>
        <button
          @click="logout"
          title="Encerrar Sessão"
          class="p-1.5 text-slate-400 hover:text-rose-400 hover:bg-rose-500/10 rounded-md transition-colors"
        >
          <LogOut class="w-3.5 h-3.5" />
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
