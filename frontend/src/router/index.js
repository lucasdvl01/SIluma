import { createRouter, createWebHistory } from 'vue-router';
import { useAuthStore } from '../stores/auth';

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/LoginView.vue'),
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
    component: () => import('../views/DashboardView.vue'),
    meta: { requiresAuth: true, adminOnly: true }
  },
  {
    path: '/mapa',
    name: 'Mapa',
    component: () => import('../views/MapView.vue'),
    meta: { requiresAuth: true, adminOnly: true }
  },
  {
    path: '/torres',
    name: 'Torres',
    component: () => import('../views/TowersView.vue'),
    meta: { requiresAuth: true, adminOnly: true }
  },
  {
    path: '/lote',
    name: 'ComandosEmLote',
    component: () => import('../views/BatchControlView.vue'),
    meta: { requiresAuth: true, adminOnly: true }
  },
  {
    path: '/manutencoes',
    name: 'Manutencoes',
    component: () => import('../views/MaintenanceView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/auditoria',
    name: 'Auditoria',
    component: () => import('../views/AuditView.vue'),
    meta: { requiresAuth: true, adminOnly: true }
  },
  {
    path: '/operador',
    name: 'AppOperador',
    component: () => import('../views/OperatorView.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/simulador-iot',
    name: 'SimuladorIoT',
    component: () => import('../views/IoTSimulatorView.vue'),
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
