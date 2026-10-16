<template>
  <div class="space-y-6 max-w-5xl mx-auto">
    <!-- Header -->
    <div class="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
      <div>
        <h2 class="text-2xl font-black text-white tracking-tight flex items-center gap-2">
          <span>Bancada de Testes IoT (Simulador ESP32 / Arduino)</span>
          <span class="text-xs px-2.5 py-0.5 rounded-full bg-emerald-500/10 text-emerald-400 border border-emerald-500/20 font-bold">
            RF06 • RNF-C10
          </span>
        </h2>
        <p class="text-xs text-slate-400">
          Simulador virtual completo do circuito IoT conversando em tempo real com a API Spring Boot via HTTP REST
        </p>
      </div>

      <div class="flex items-center gap-2">
        <span class="text-xs font-mono px-2.5 py-1 rounded-lg bg-mina-800 text-slate-300 border border-mina-darkBorder">
          Target: /api/iot/telemetry
        </span>
      </div>
    </div>

    <!-- Layout da Bancada Interativa -->
    <div class="grid grid-cols-1 lg:grid-cols-12 gap-6">
      <!-- Painel Esquerdo: Controles do Hardware e Sensores (7 colunas) -->
      <div class="lg:col-span-7 bg-mina-900 border border-mina-darkBorder rounded-2xl p-6 shadow-2xl space-y-6">
        <div class="flex items-center justify-between border-b border-mina-darkBorder pb-4">
          <div class="flex items-center gap-3">
            <div class="w-10 h-10 rounded-xl bg-emerald-500/15 border border-emerald-500/30 flex items-center justify-center text-xl font-bold text-emerald-400">
              🔌
            </div>
            <div>
              <h3 class="font-extrabold text-sm text-white">Placa ESP32 DevKit v1</h3>
              <p class="text-[11px] text-slate-400">Firmware HTTPClient • Wi-Fi Station</p>
            </div>
          </div>

          <div class="flex items-center gap-2">
            <span class="flex items-center gap-1.5 text-xs text-emerald-400 font-bold bg-emerald-500/10 px-2.5 py-1 rounded-full border border-emerald-500/20">
              <span class="w-2 h-2 rounded-full bg-emerald-400 animate-pulse"></span>
              Wi-Fi Conectado
            </span>
          </div>
        </div>

        <!-- Seletor da Torre a Simular -->
        <div>
          <label class="block text-xs font-semibold text-slate-300 mb-1.5">Torre Vinculada ao Dispositivo:</label>
          <select
            v-model="selectedCode"
            class="w-full bg-mina-800 border border-mina-darkBorder rounded-xl px-3.5 py-2.5 text-sm text-white font-mono font-bold focus:outline-none focus:border-amber-500"
          >
            <option v-for="t in towerStore.towers" :key="t.id" :value="t.codigoIdentificacao">
              {{ t.codigoIdentificacao }} — {{ t.setorNome }} ({{ t.statusLabel }})
            </option>
          </select>
        </div>

        <!-- Sensor LDR: Slider de Luminosidade (RF06) -->
        <div class="p-4 bg-mina-950/60 border border-mina-darkBorder rounded-xl space-y-3">
          <div class="flex justify-between items-center">
            <div>
              <label class="text-xs font-extrabold text-white flex items-center gap-2">
                <span>☀️ Sensor de Luminosidade (LDR - GPIO 34)</span>
                <span
                  class="text-[10px] px-2 py-0.5 rounded font-black uppercase"
                  :class="ldrValue < 300 ? 'bg-amber-500/20 text-amber-300 border border-amber-500/40 animate-pulse' : 'bg-slate-800 text-slate-400'"
                >
                  {{ ldrValue < 300 ? '⚠️ Cerração / Noite (< 300)' : 'Luz Normal' }}
                </span>
              </label>
              <p class="text-[11px] text-slate-400">Valores abaixo de 300 ativam o Alerta de Baixa Visibilidade</p>
            </div>
            <span class="text-sm font-mono font-black text-amber-400 bg-mina-800 px-2.5 py-1 rounded-lg">
              {{ ldrValue }} / 1023
            </span>
          </div>

          <input
            v-model.number="ldrValue"
            type="range"
            min="0"
            max="1023"
            class="w-full accent-amber-500 cursor-pointer h-2 bg-mina-800 rounded-lg"
          />

          <!-- Botões de Atalho para Condições Ambientais -->
          <div class="flex gap-2 pt-1">
            <button
              @click="ldrValue = 120"
              class="px-2.5 py-1 rounded-lg bg-mina-800 hover:bg-mina-700 text-[11px] font-semibold text-slate-300 transition-colors"
            >
              🌫️ Simular Forte Cerração (120)
            </button>
            <button
              @click="ldrValue = 250"
              class="px-2.5 py-1 rounded-lg bg-mina-800 hover:bg-mina-700 text-[11px] font-semibold text-slate-300 transition-colors"
            >
              🌙 Simular Noite (250)
            </button>
            <button
              @click="ldrValue = 850"
              class="px-2.5 py-1 rounded-lg bg-mina-800 hover:bg-mina-700 text-[11px] font-semibold text-slate-300 transition-colors"
            >
              ☀️ Simular Dia Ensolarado (850)
            </button>
          </div>
        </div>

        <!-- Botão Físico de Cerração (GPIO 14) & Bateria -->
        <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <!-- Botão Cerração -->
          <div class="p-4 bg-mina-950/60 border border-mina-darkBorder rounded-xl space-y-2 flex flex-col justify-between">
            <div>
              <label class="text-xs font-extrabold text-white block">Botão de Emergência Cerração</label>
              <p class="text-[11px] text-slate-400">Simula acionamento no pino digital GPIO 14</p>
            </div>
            <button
              @click="triggerFogButton"
              class="w-full py-3 rounded-xl font-black text-xs uppercase tracking-wider transition-all shadow-md active:scale-95 flex items-center justify-center gap-2"
              :class="fogButtonPressed ? 'bg-amber-600 text-white animate-pulse' : 'bg-gradient-to-r from-amber-500 to-amber-600 text-black hover:brightness-110'"
            >
              <span>{{ fogButtonPressed ? '🚨 BOTÃO ACIONADO!' : '🔘 Pressionar Botão Cerração' }}</span>
            </button>
          </div>

          <!-- Slider de Bateria -->
          <div class="p-4 bg-mina-950/60 border border-mina-darkBorder rounded-xl space-y-2">
            <div class="flex justify-between items-center">
              <label class="text-xs font-extrabold text-white">Nível de Bateria</label>
              <span class="text-xs font-mono font-bold text-emerald-400">{{ batteryLevel }}%</span>
            </div>
            <input
              v-model.number="batteryLevel"
              type="range"
              min="5"
              max="100"
              class="w-full accent-emerald-500 cursor-pointer h-2 bg-mina-800 rounded-lg"
            />
            <p class="text-[10px] text-slate-500">Transmite telemetria de carga para a torre</p>
          </div>
        </div>

        <!-- Ações de Transmissão -->
        <div class="flex flex-col sm:flex-row items-center gap-3 pt-2">
          <button
            @click="sendTelemetry"
            :disabled="transmitting"
            class="w-full sm:w-auto px-5 py-3 bg-gradient-to-r from-emerald-500 to-emerald-600 hover:from-emerald-400 hover:to-emerald-500 text-black font-black rounded-xl text-xs shadow-lg shadow-emerald-500/20 transition-all flex items-center justify-center gap-2 disabled:opacity-50"
          >
            <span>📡 Enviar Requisição POST Telemetria</span>
          </button>

          <label class="flex items-center gap-2 text-xs font-semibold text-slate-300 cursor-pointer select-none">
            <input
              type="checkbox"
              v-model="autoLoop"
              class="w-4 h-4 rounded accent-emerald-500"
            />
            <span>Transmissão Automática Contínua (a cada 3s)</span>
          </label>
        </div>
      </div>

      <!-- Painel Direito: Resposta do Circuito Virtual e Log HTTP (5 colunas) -->
      <div class="lg:col-span-5 space-y-6">
        <!-- Visualização da Luminária / LED RGB (Item 9 SENAI) -->
        <div class="bg-mina-900 border border-mina-darkBorder rounded-2xl p-6 shadow-2xl space-y-4 text-center">
          <h4 class="text-xs font-extrabold text-slate-400 uppercase tracking-wider">
            Luminária da Torre (LED RGB - GPIOs 25, 26, 27)
          </h4>

          <!-- Simulação Gráfica da Lâmpada LED -->
          <div class="py-6 flex flex-col items-center justify-center">
            <div
              class="w-28 h-28 rounded-full border-4 border-mina-800 flex items-center justify-center transition-all duration-500 relative"
              :style="{
                backgroundColor: ledColorHex,
                boxShadow: ledGlowStyle
              }"
            >
              <div class="text-4xl select-none">
                {{ ledOn ? '💡' : '🌑' }}
              </div>
            </div>

            <!-- Legenda da Luz -->
            <div class="mt-4">
              <span class="text-sm font-black text-white block">{{ ledStatusTitle }}</span>
              <span class="text-xs font-mono text-slate-400">PWM: {{ ledPwmValue }} / 255 ({{ Math.round((ledPwmValue / 255) * 100) }}%)</span>
            </div>
          </div>
        </div>

        <!-- Terminal / Log HTTP em Tempo Real -->
        <div class="bg-mina-950 border border-mina-darkBorder rounded-2xl p-4 shadow-2xl space-y-2">
          <div class="flex items-center justify-between border-b border-mina-darkBorder pb-2">
            <span class="text-[11px] font-bold text-slate-400 font-mono flex items-center gap-2">
              <span class="w-2 h-2 rounded-full bg-emerald-500"></span>
              Console de Comunicação HTTP
            </span>
            <button @click="terminalLogs = []" class="text-[10px] text-slate-500 hover:text-slate-300">
              Limpar
            </button>
          </div>

          <div class="h-56 overflow-y-auto space-y-1.5 font-mono text-[10px] text-slate-300 p-1">
            <div
              v-for="(log, idx) in terminalLogs"
              :key="idx"
              :class="log.type === 'response' ? 'text-emerald-400' : (log.type === 'alert' ? 'text-amber-400 font-bold' : 'text-slate-400')"
            >
              {{ log.text }}
            </div>
            <div v-if="terminalLogs.length === 0" class="text-slate-600 italic text-center pt-8">
              Nenhuma telemetria enviada ainda. Ajuste os sensores e clique em "Enviar Requisição".
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, watch } from 'vue';
import { useTowerStore } from '../stores/towers';
import api from '../services/api';

const towerStore = useTowerStore();

const selectedCode = ref('TR-001');
const ldrValue = ref(750);
const batteryLevel = ref(88.0);
const fogButtonPressed = ref(false);
const autoLoop = ref(false);
const transmitting = ref(false);

const ledOn = ref(true);
const ledModo = ref('NORMAL');
const ledPwmValue = ref(200);

const terminalLogs = ref([]);

let autoTimer = null;

const ledColorHex = computed(() => {
  if (!ledOn.value) return '#1f2937';
  if (ledModo.value === 'ALTA_VISIBILIDADE_CERRACAO') return '#f59e0b'; // Âmbar
  if (ledModo.value === 'ECONOMIA') return '#38bdf8'; // Azul
  return '#10b981'; // Verde operacional
});

const ledGlowStyle = computed(() => {
  if (!ledOn.value) return 'none';
  if (ledModo.value === 'ALTA_VISIBILIDADE_CERRACAO') return '0 0 35px rgba(245, 158, 11, 0.6)';
  if (ledModo.value === 'ECONOMIA') return '0 0 25px rgba(56, 189, 248, 0.5)';
  return '0 0 25px rgba(16, 185, 129, 0.5)';
});

const ledStatusTitle = computed(() => {
  if (!ledOn.value) return 'Luminária Apagada (Desligada)';
  if (ledModo.value === 'ALTA_VISIBILIDADE_CERRACAO') return 'Alta Visibilidade Cerração (Âmbar 100%)';
  if (ledModo.value === 'ECONOMIA') return 'Modo Econômico (Azul 50%)';
  return 'Modo Normal Operacional (Verde)';
});

const addLog = (text, type = 'info') => {
  const time = new Date().toLocaleTimeString('pt-BR');
  terminalLogs.value.unshift({ text: `[${time}] ${text}`, type });
  if (terminalLogs.value.length > 50) terminalLogs.value.pop();
};

const sendTelemetry = async () => {
  transmitting.value = true;
  const payload = {
    codigoTorre: selectedCode.value,
    luminosidadeLdr: ldrValue.value,
    statusLed: ledOn.value,
    nivelBateria: batteryLevel.value,
    pressaoBotaoCerracao: fogButtonPressed.value
  };

  addLog(`> POST /api/iot/telemetry: ${JSON.stringify(payload)}`, 'request');

  try {
    const res = await api.post('/iot/telemetry', payload);
    const data = res.data;

    ledOn.value = data.instrucaoLed;
    ledModo.value = data.modoLuz;
    ledPwmValue.value = data.intensidadePwm;

    if (data.alertaAtivado) {
      addLog(`< 200 OK: 🚨 ${data.mensagem}`, 'alert');
    } else {
      addLog(`< 200 OK: LED=${data.instrucaoLed ? 'ON' : 'OFF'} | Modo=${data.modoLuz} | PWM=${data.intensidadePwm}`, 'response');
    }

    // Atualiza lojas
    await towerStore.fetchTowers();
  } catch (err) {
    addLog(`! ERRO HTTP: ${err.message}`, 'alert');
  } finally {
    transmitting.value = false;
    if (fogButtonPressed.value) {
      fogButtonPressed.value = false; // reset do pulso do botão
    }
  }
};

const triggerFogButton = () => {
  fogButtonPressed.value = true;
  sendTelemetry();
};

watch(() => autoLoop.value, (val) => {
  if (val) {
    sendTelemetry();
    autoTimer = setInterval(sendTelemetry, 3000);
  } else if (autoTimer) {
    clearInterval(autoTimer);
    autoTimer = null;
  }
});

onMounted(async () => {
  await towerStore.fetchTowers();
  if (towerStore.towers.length > 0) {
    selectedCode.value = towerStore.towers[0].codigoIdentificacao;
  }
  addLog('Simulador IoT iniciado. Pronto para testes de LDR e LED.');
});

onBeforeUnmount(() => {
  if (autoTimer) clearInterval(autoTimer);
});
</script>
