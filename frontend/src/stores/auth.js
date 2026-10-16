import { defineStore } from 'pinia';
import api from '../services/api';

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('token') || null,
    user: JSON.parse(localStorage.getItem('user') || 'null'),
    inactivityTimeout: null,
  }),

  getters: {
    isAuthenticated: (state) => !!state.token,
    isAdmin: (state) => state.user?.perfil === 'ADMIN',
    isOperador: (state) => state.user?.perfil === 'OPERADOR',
    userName: (state) => state.user?.nome || 'Usuário',
    userRole: (state) => state.user?.perfil || '',
  },

  actions: {
    async login(email, senha) {
      const response = await api.post('/auth/login', { email, senha });
      const data = response.data;
      this.token = data.token;
      this.user = {
        nome: data.nome,
        email: data.email,
        perfil: data.perfil,
      };

      localStorage.setItem('token', this.token);
      localStorage.setItem('user', JSON.stringify(this.user));

      this.startInactivityTimer();
      return this.user;
    },

    logout() {
      this.token = null;
      this.user = null;
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      if (this.inactivityTimeout) {
        clearTimeout(this.inactivityTimeout);
      }
    },

    // RNF-A07: Logout automático após 15 minutos sem uso
    startInactivityTimer() {
      if (this.inactivityTimeout) {
        clearTimeout(this.inactivityTimeout);
      }
      const FIFTEEN_MINUTES = 15 * 60 * 1000;
      this.inactivityTimeout = setTimeout(() => {
        alert('Sessão expirada por inatividade (15 minutos). Por favor, realize o login novamente.');
        this.logout();
        window.location.href = '/login';
      }, FIFTEEN_MINUTES);
    },

    resetInactivityTimer() {
      if (this.isAuthenticated) {
        this.startInactivityTimer();
      }
    }
  },
});
