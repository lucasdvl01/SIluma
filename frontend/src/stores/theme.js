import { defineStore } from 'pinia';

const THEME_STORAGE_KEY = 'si-luma-theme';

export const useThemeStore = defineStore('theme', {
  state: () => ({
    mode: localStorage.getItem(THEME_STORAGE_KEY) === 'dark' ? 'dark' : 'light',
  }),

  getters: {
    isDark: (state) => state.mode === 'dark',
  },

  actions: {
    initialize() {
      this.applyTheme();
    },

    toggle() {
      this.mode = this.isDark ? 'light' : 'dark';
      this.applyTheme();
    },

    applyTheme() {
      document.documentElement.dataset.theme = this.mode;
      document.documentElement.style.colorScheme = this.mode;
      localStorage.setItem(THEME_STORAGE_KEY, this.mode);
    },
  },
});
