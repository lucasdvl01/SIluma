/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{vue,js,ts,jsx,tsx}",
  ],
  darkMode: 'class',
  theme: {
    extend: {
      colors: {
        mina: {
          950: '#070b12',
          900: '#0b1321',
          800: '#142136',
          700: '#1e3250',
          600: '#2b4772',
          accent: '#f59e0b', // Amarelo industrial de mineração
          amber: '#d97706',
          emerald: '#10b981',
          danger: '#ef4444',
          darkCard: '#111b2d',
          darkBorder: '#1c2e4a'
        }
      }
    },
  },
  plugins: [],
}
