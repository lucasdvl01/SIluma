import { defineStore } from 'pinia';
import api from '../services/api';

export const useMaintenanceStore = defineStore('maintenance', {
  state: () => ({
    orders: [],
    loading: false,
  }),

  actions: {
    async fetchOrders(status = null) {
      this.loading = true;
      try {
        const url = status ? `/maintenances?status=${status}` : '/maintenances';
        const response = await api.get(url);
        this.orders = response.data;
        return this.orders;
      } finally {
        this.loading = false;
      }
    },

    async createOrder(orderData) {
      const response = await api.post('/maintenances', orderData);
      await this.fetchOrders();
      return response.data;
    },

    async updateOrder(id, status, observacoes = '') {
      const response = await api.put(`/maintenances/${id}`, {
        status,
        observacoesResolucao: observacoes,
      });
      await this.fetchOrders();
      return response.data;
    }
  }
});
