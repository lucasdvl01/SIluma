import { defineStore } from 'pinia';
import api from '../services/api';

export const useTowerStore = defineStore('towers', {
  state: () => ({
    towers: [],
    sectors: [],
    selectedTower: null,
    loading: false,
    error: null,
    pollingInterval: null,
  }),

  getters: {
    totalTowers: (state) => state.towers.length,
    activeTowers: (state) => state.towers.filter(t => t.ligada).length,
    fogAlertTowers: (state) => state.towers.filter(t => t.alertaBaixaVisibilidade || t.status === 'ALERTA_BAIXA_VISIBILIDADE'),
    maintenanceTowers: (state) => state.towers.filter(t => t.status === 'EM_MANUTENCAO' || t.necessitaManutencaoPreventiva),
  },

  actions: {
    async fetchTowers() {
      try {
        const response = await api.get('/towers');
        this.towers = response.data;
        // Atualiza cache local para suporte offline (RNF-A03, RNF-A11)
        localStorage.setItem('cached_towers', JSON.stringify(this.towers));
        return this.towers;
      } catch (err) {
        // Fallback para cache local se estiver offline
        const cached = localStorage.getItem('cached_towers');
        if (cached) {
          this.towers = JSON.parse(cached);
        }
        throw err;
      }
    },

    async fetchSectors() {
      try {
        const response = await api.get('/sectors');
        this.sectors = response.data;
        return this.sectors;
      } catch (err) {
        console.error('Erro ao buscar setores:', err);
      }
    },

    async toggleTower(id, ligar) {
      const acao = ligar ? 'LIGAR' : 'DESLIGAR';
      const response = await api.post(`/towers/${id}/action`, { acao });
      await this.fetchTowers();
      return response.data;
    },

    async setTowerMode(id, modo) {
      const response = await api.post(`/towers/${id}/action`, {
        acao: 'ALTERAR_MODO',
        modo: modo
      });
      await this.fetchTowers();
      return response.data;
    },

    async executeBatchAction(setorId, acao, modo = null) {
      const response = await api.post('/towers/batch', {
        setorId,
        acao,
        modo
      });
      await this.fetchTowers();
      return response.data;
    },

    async createTower(towerData) {
      const response = await api.post('/towers', towerData);
      await this.fetchTowers();
      return response.data;
    },

    async deleteTower(id) {
      await api.delete(`/towers/${id}`);
      await this.fetchTowers();
    },

    // RNF-W11, RNF-A13: Atualização em tempo real via polling a cada 10s
    startPolling(intervalMs = 10000) {
      if (this.pollingInterval) return;
      this.fetchTowers();
      this.pollingInterval = setInterval(() => {
        this.fetchTowers();
      }, intervalMs);
    },

    stopPolling() {
      if (this.pollingInterval) {
        clearInterval(this.pollingInterval);
        this.pollingInterval = null;
      }
    }
  }
});
