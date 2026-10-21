<template>
  <button
    v-if="mobileOpen"
    type="button"
    class="fixed inset-0 z-40 bg-slate-950/70 backdrop-blur-sm lg:hidden"
    aria-label="Fechar painel lateral"
    @click="emit('close')"
  ></button>

  <aside
    id="primary-navigation"
    aria-label="Navegação principal"
    class="app-sidebar fixed inset-y-0 left-0 z-50 flex h-screen w-72 max-w-[calc(100vw-2rem)] shrink-0 flex-col border-r border-slate-200 bg-white shadow-sm transition-transform duration-200 ease-out lg:static lg:z-auto lg:h-screen lg:w-64 lg:translate-x-0"
    :class="mobileOpen ? 'translate-x-0' : '-translate-x-full lg:translate-x-0'"
  >
    <!-- Brand / Cabeçalho Institucional CCO (About Face: Postura Soberana) -->
    <div class="border-b border-slate-100 px-5 py-5">
      <img
        src="/si-luma-logo.svg"
        alt="SI Luma — Sistema de iluminação"
        class="brand-logo h-auto w-40"
      />
      <p class="mt-2 truncate text-[11px] font-medium tracking-tight text-slate-500">SENAI Betim • CETEM</p>
    </div>

    <!-- Navigation Menu (Refactoring UI: Contraste suave, sem bordas pesadas por item) -->
    <nav class="flex-1 space-y-1 overflow-y-auto px-3 py-4" @click="emit('close')">
      <div class="px-3 pt-2 pb-3 text-[9px] font-bold uppercase tracking-[0.18em] text-slate-500 font-mono">
        {{ authStore.isAdmin ? 'Gestão da Mina (CCO)' : 'Terminal Operacional' }}
      </div>

      <!-- Links para ADMIN -->
      <template v-if="authStore.isAdmin">
        <router-link
          to="/dashboard"
          class="min-h-11 flex items-center gap-3 px-3 py-3 rounded-xl text-[13px] font-medium transition-all group interactive-lift"
          :class="$route.path === '/dashboard' 
            ? 'bg-amber-50 text-amber-900 font-semibold ring-1 ring-amber-200'
            : 'text-slate-600 hover:text-slate-900 hover:bg-slate-50'"
        >
          <BarChart3 class="w-4 h-4 shrink-0 transition-transform group-hover:scale-110" :class="$route.path === '/dashboard' ? 'text-amber-700' : 'text-slate-500'" />
          <span>Dashboard Indicadores</span>
        </router-link>

        <router-link
          to="/mapa"
          class="min-h-11 flex items-center gap-3 px-3 py-3 rounded-xl text-[13px] font-medium transition-all group interactive-lift"
          :class="$route.path === '/mapa' 
            ? 'bg-amber-50 text-amber-900 font-semibold ring-1 ring-amber-200'
            : 'text-slate-600 hover:text-slate-900 hover:bg-slate-50'"
        >
          <MapPin class="w-4 h-4 shrink-0 transition-transform group-hover:scale-110" :class="$route.path === '/mapa' ? 'text-amber-700' : 'text-slate-500'" />
          <span>Mapa Operacional</span>
        </router-link>

        <router-link
          to="/torres"
          class="min-h-11 flex items-center gap-3 px-3 py-3 rounded-xl text-[13px] font-medium transition-all group interactive-lift"
          :class="$route.path === '/torres' 
            ? 'bg-amber-50 text-amber-900 font-semibold ring-1 ring-amber-200'
            : 'text-slate-600 hover:text-slate-900 hover:bg-slate-50'"
        >
          <RadioTower class="w-4 h-4 shrink-0 transition-transform group-hover:scale-110" :class="$route.path === '/torres' ? 'text-amber-700' : 'text-slate-500'" />
          <span>Frota de Torres</span>
        </router-link>

        <router-link
          to="/lote"
          class="min-h-11 flex items-center gap-3 px-3 py-3 rounded-xl text-[13px] font-medium transition-all group interactive-lift"
          :class="$route.path === '/lote' 
            ? 'bg-amber-50 text-amber-900 font-semibold ring-1 ring-amber-200'
            : 'text-slate-600 hover:text-slate-900 hover:bg-slate-50'"
        >
          <Layers class="w-4 h-4 shrink-0 transition-transform group-hover:scale-110" :class="$route.path === '/lote' ? 'text-amber-700' : 'text-slate-500'" />
          <span>Comandos em Lote</span>
        </router-link>
      </template>

      <!-- Links Comuns -->
      <router-link
        to="/manutencoes"
        class="min-h-11 flex items-center gap-3 px-3 py-3 rounded-xl text-[13px] font-medium transition-all group interactive-lift"
        :class="$route.path === '/manutencoes' 
          ? 'bg-amber-50 text-amber-900 font-semibold ring-1 ring-amber-200'
          : 'text-slate-600 hover:text-slate-900 hover:bg-slate-50'"
      >
        <Wrench class="w-4 h-4 shrink-0 transition-transform group-hover:scale-110" :class="$route.path === '/manutencoes' ? 'text-amber-700' : 'text-slate-500'" />
        <span>Manutenções & Horímetro</span>
      </router-link>

      <router-link
        to="/operador"
        class="min-h-11 flex items-center gap-3 px-3 py-3 rounded-xl text-[13px] font-medium transition-all group interactive-lift"
        :class="$route.path === '/operador' 
          ? 'bg-amber-50 text-amber-900 font-semibold ring-1 ring-amber-200'
          : 'text-slate-600 hover:text-slate-900 hover:bg-slate-50'"
      >
        <Smartphone class="w-4 h-4 shrink-0 transition-transform group-hover:scale-110" :class="$route.path === '/operador' ? 'text-amber-700' : 'text-slate-500'" />
        <span>Terminal Operador (Campo)</span>
      </router-link>

      <router-link
        to="/simulador-iot"
        class="min-h-11 flex items-center gap-3 px-3 py-3 rounded-xl text-[13px] font-medium transition-all group interactive-lift"
        :class="$route.path === '/simulador-iot' 
          ? 'bg-amber-50 text-amber-900 font-semibold ring-1 ring-amber-200'
          : 'text-slate-600 hover:text-slate-900 hover:bg-slate-50'"
      >
        <Cpu class="w-4 h-4 shrink-0 transition-transform group-hover:scale-110" :class="$route.path === '/simulador-iot' ? 'text-amber-700' : 'text-slate-500'" />
        <span>Bancada IoT (Simulador)</span>
      </router-link>

      <router-link
        v-if="authStore.isAdmin"
        to="/auditoria"
        class="min-h-11 flex items-center gap-3 px-3 py-3 rounded-xl text-[13px] font-medium transition-all group interactive-lift"
        :class="$route.path === '/auditoria'
          ? 'bg-amber-50 text-amber-900 font-semibold ring-1 ring-amber-200'
          : 'text-slate-600 hover:text-slate-900 hover:bg-slate-50'"
      >
        <ShieldCheck class="w-4 h-4 shrink-0 transition-transform group-hover:scale-110" :class="$route.path === '/auditoria' ? 'text-amber-700' : 'text-slate-500'" />
        <span>Auditoria & Histórico</span>
      </router-link>
    </nav>

    <!-- Footer de Conexão e Perfil do Usuário (Rich Modeless Visual Feedback) -->
    <div class="space-y-2.5 border-t border-slate-100 bg-white p-3">
      <!-- Status Rede Modeless -->
      <div class="flex items-center justify-between rounded-xl border border-slate-200 bg-slate-50 px-3 py-2 text-[11px]">
        <div class="flex items-center gap-2.5">
          <span class="relative flex h-2 w-2">
            <span
              class="relative inline-flex h-2 w-2 rounded-full"
              :class="offlineStore.simulationOffline ? 'bg-amber-400' : offlineStore.isOnline ? 'bg-emerald-500' : 'bg-rose-500'"
            ></span>
          </span>
          <span class="font-medium text-slate-600">
            {{ offlineStore.simulationOffline ? 'Modo de teste offline' : offlineStore.isOnline ? 'Rede detectada' : 'Sem conexão de rede' }}
          </span>
        </div>
        <span v-if="offlineStore.pendingCount > 0" class="text-[10px] px-1.5 py-0.5 rounded-full bg-amber-500/20 text-amber-300 font-mono font-bold tabular-nums">
          {{ offlineStore.pendingCount }} fila
        </span>
      </div>

      <!-- User Profile Card -->
      <div class="flex items-center justify-between rounded-xl border border-slate-200 bg-white p-1.5">
        <div class="flex items-center gap-2.5 min-w-0">
          <div class="flex h-8 w-8 shrink-0 items-center justify-center rounded-lg border border-amber-200 bg-amber-50 text-xs font-mono font-bold text-amber-800">
            {{ authStore.userName.substring(0, 2).toUpperCase() }}
          </div>
          <div class="min-w-0">
            <p class="truncate text-xs font-bold leading-tight text-slate-800">{{ authStore.userName }}</p>
            <p class="truncate text-[10px] uppercase tracking-wider text-slate-500 font-mono">{{ authStore.userRole }}</p>
          </div>
        </div>
        <button
          @click="logout"
          title="Encerrar Sessão"
          class="rounded-lg p-2 text-slate-500 transition-all hover:bg-rose-50 hover:text-rose-600"
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
  RadioTower,
  Layers,
  Wrench,
  Smartphone,
  Cpu,
  ShieldCheck,
  LogOut
} from 'lucide-vue-next';

defineProps({
  mobileOpen: {
    type: Boolean,
    default: false
  }
});

const emit = defineEmits(['close']);
const authStore = useAuthStore();
const offlineStore = useOfflineSyncStore();
const router = useRouter();

const logout = () => {
  authStore.logout();
  router.push('/login');
};
</script>
