import { createRouter, createWebHistory } from 'vue-router';
import { useAuthStore } from '../stores/auth';

import LoginView from '../views/LoginView.vue';
import DashboardView from '../views/DashboardView.vue';
import MapView from '../views/MapView.vue';
import TowersView from '../views/TowersView.vue';
import BatchControlView from '../views/BatchControlView.vue';
import MaintenanceView from '../views/MaintenanceView.vue';
import AuditView from '../views/AuditView.vue';
import OperatorView from '../views/OperatorView.vue';
import IoTSimulatorView from '../views/IoTSimulatorView.vue';

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: LoginView,
    meta: { public: true }
  },
  {
    path: '/',
    redirect: () => {
      const auth = useAuthStore();
      return auth.isOperador ? '/operador' : '/dashboard';
    }
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: DashboardView,
    meta: { requiresAuth: true, adminOnly: true }
  },
  {
    path: '/mapa',
    name: 'Mapa',
    component: MapView,
    meta: { requiresAuth: true, adminOnly: true }
  },
  {
    path: '/torres',
    name: 'Torres',
    component: TowersView,
    meta: { requiresAuth: true, adminOnly: true }
  },
  {
    path: '/lote',
    name: 'ComandosEmLote',
    component: BatchControlView,
    meta: { requiresAuth: true, adminOnly: true }
  },
  {
    path: '/manutencoes',
    name: 'Manutencoes',
    component: MaintenanceView,
    meta: { requiresAuth: true }
  },
  {
    path: '/auditoria',
    name: 'Auditoria',
    component: AuditView,
    meta: { requiresAuth: true, adminOnly: true }
  },
  {
    path: '/operador',
    name: 'AppOperador',
    component: OperatorView,
    meta: { requiresAuth: true }
  },
  {
    path: '/simulador-iot',
    name: 'SimuladorIoT',
    component: IoTSimulatorView,
    meta: { requiresAuth: true }
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/'
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

// Guarda de Rotas e RBAC (RNF-W03, RNF-A05)
router.beforeEach((to, from, next) => {
  const authStore = useAuthStore();
  authStore.resetInactivityTimer();

  if (to.meta.public) {
    if (authStore.isAuthenticated) {
      return next(authStore.isOperador ? '/operador' : '/dashboard');
    }
    return next();
  }

  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    return next('/login');
  }

  // Restrição de acesso a perfis: Operador não acessa rotas de Admin
  if (to.meta.adminOnly && authStore.isOperador) {
    return next('/operador');
  }

  next();
});

export default router;
