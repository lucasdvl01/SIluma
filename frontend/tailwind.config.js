/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{vue,js,ts,jsx,tsx}",
  ],
  darkMode: 'class',
  theme: {
    extend: {
      fontFamily: {
        sans: ['"Plus Jakarta Sans"', '-apple-system', 'BlinkMacSystemFont', '"Segoe UI"', 'Roboto', 'sans-serif'],
        mono: ['"JetBrains Mono"', 'ui-monospace', 'SFMono-Regular', 'Menlo', 'Monaco', 'Consolas', 'monospace'],
      },
      colors: {
        mina: {
          // Paleta calibrada HSL com rotação sutil para temperatura de aço industrial
          950: '#060911', // Canvas profundo (base de alto contraste)
          900: '#0b111d', // Painéis e superfícies primárias
          850: '#101827', // Cards e superfícies elevadas
          800: '#172236', // Elementos interativos / hover
          700: '#22324f', // Bordas sutis de alta definição
          600: '#32486f', // Bordas de foco / realce
          accent: '#f59e0b', // Amarelo cromo de sinalização industrial
          amber: '#d97706',
          emerald: '#10b981', // Operacional seguro
          danger: '#ef4444', // Falha crítica
          darkCard: '#0e1524',
          darkBorder: '#1c273c'
        }
      },
      boxShadow: {
        // Sombras bifásicas (Refactoring UI: Luz Direta + Oclusão de Contato Ambiente)
        'elevated-sm': '0 1px 2px 0 rgba(0, 0, 0, 0.4), 0 1px 3px 1px rgba(0, 0, 0, 0.2)',
        'elevated': '0 4px 12px -2px rgba(0, 0, 0, 0.5), 0 2px 4px -1px rgba(0, 0, 0, 0.4)',
        'elevated-lg': '0 12px 28px -4px rgba(0, 0, 0, 0.65), 0 4px 8px -2px rgba(0, 0, 0, 0.5)',
        'glow-amber': '0 0 15px -2px rgba(245, 158, 11, 0.25)',
        'glow-emerald': '0 0 15px -2px rgba(16, 185, 129, 0.25)',
        'glow-danger': '0 0 15px -2px rgba(239, 68, 68, 0.25)',
      }
    },
  },
  plugins: [],
}
