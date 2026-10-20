import { defineStore } from 'pinia';
import api from '../services/api';

export const useOfflineSyncStore = defineStore('offlineSync', {
  state: () => ({
    isOnline: navigator.onLine,
    simulationOffline: false,
    queue: JSON.parse(localStorage.getItem('offline_commands_queue') || '[]'),
    syncing: false,
    syncMessage: '',
    initialized: false,
  }),

  getters: {
    pendingCount: (state) => state.queue.length,
  },

  actions: {
    init() {
      if (this.initialized) return;
      this.initialized = true;

      window.addEventListener('online', () => {
        this.isOnline = !this.simulationOffline;
        if (this.isOnline) this.syncPendingCommands();
      });

      window.addEventListener('offline', () => {
        this.isOnline = false;
      });
    },

    setOfflineSimulation(enabled) {
      this.simulationOffline = enabled;
      this.isOnline = navigator.onLine && !enabled;
      if (this.isOnline) this.syncPendingCommands();
    },

    // RNF-A10: Feedback tátil (vibração) no dispositivo móvel ao acionar comando
    triggerHapticFeedback() {
      if ('vibrate' in navigator) {
        try {
          navigator.vibrate([40, 30, 40]);
        } catch (e) {
          // ignora caso navegador restrinja
        }
      }
    },

    async dispatchCommand(towerId, action, mode = null) {
      this.triggerHapticFeedback();

      if (this.isOnline) {
        // Envio online direto
        try {
          const res = await api.post(`/towers/${towerId}/action`, { acao: action, modo: mode });
          return { success: true, offline: false, data: res.data };
        } catch (error) {
          // Falha na requisição (ex: perda de sinal momentânea) -> salva offline
          return this.enqueueOffline(towerId, action, mode);
        }
      } else {
        // RF13: Operação Offline -> enfileira localmente
        return this.enqueueOffline(towerId, action, mode);
      }
    },

    enqueueOffline(towerId, action, mode) {
      const item = {
        queueId: Date.now() + Math.random().toString(36).substring(2, 6),
        towerId,
        action,
        mode,
        timestamp: new Date().toISOString()
      };

      this.queue.push(item);
      localStorage.setItem('offline_commands_queue', JSON.stringify(this.queue));

      return {
        success: true,
        offline: true,
        message: 'Comando armazenado na fila offline. Será sincronizado automaticamente ao reconectar.'
      };
    },

    // RNF-A14: Ao reconectar, sincroniza ações pendentes com o backend
    async syncPendingCommands() {
      if (this.queue.length === 0 || this.syncing) return;

      this.syncing = true;
      this.syncMessage = `Sincronizando ${this.queue.length} comando(s) pendente(s)...`;

      const remaining = [];

      for (const item of this.queue) {
        try {
          await api.post(`/towers/${item.towerId}/action`, {
            acao: item.action,
            modo: item.mode
          });
        } catch (err) {
          console.error('Erro ao sincronizar comando offline:', item, err);
          remaining.push(item);
        }
      }

      this.queue = remaining;
      localStorage.setItem('offline_commands_queue', JSON.stringify(this.queue));
      this.syncing = false;
      this.syncMessage = remaining.length === 0 
        ? 'Todos os comandos offline foram sincronizados com sucesso!' 
        : `${remaining.length} comando(s) ainda pendentes.`;

      setTimeout(() => {
        this.syncMessage = '';
      }, 5000);
    }
  }
});
