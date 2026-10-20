<template>
  <div class="space-y-6 max-w-6xl mx-auto">
    <!-- Top Header SCADA -->
    <div class="page-header flex flex-col sm:flex-row justify-between items-start sm:items-center gap-5">
      <div>
        <div class="flex flex-wrap items-center gap-2 mb-1">
          <span class="p-1.5 rounded-xl bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">
            <Cpu class="w-4 h-4" />
          </span>
          <h1 class="font-sans">
            Bancada de Diagnóstico & Telemetria IoT
          </h1>
          <span class="text-xs font-mono font-bold px-2.5 py-1 rounded-xl bg-slate-900 text-slate-300 border border-white/10">
            Hardware ESP32
          </span>
        </div>
        <p class="mt-2">
          Ensaio e validação do microcontrolador ESP32 / Arduino em comunicação direta com a API Spring Boot via HTTP REST
        </p>
      </div>

      <div class="flex items-center gap-3">
        <div class="flex items-center gap-2 px-2.5 py-1 rounded bg-slate-900 border border-slate-800 text-xs">
          <span class="w-2 h-2 rounded-full bg-emerald-500"></span>
          <span class="font-mono text-slate-300 text-[11px]">Interface de telemetria IoT</span>
        </div>
        <span class="text-xs font-mono px-2.5 py-1 rounded bg-slate-900 text-amber-400 border border-slate-800">
          POST /api/iot/telemetry
        </span>
      </div>
    </div>

    <!-- Layout da Bancada Interativa -->
    <div class="grid grid-cols-1 lg:grid-cols-12 gap-6">
      <!-- Painel Esquerdo: Sensores e Entradas do Dispositivo (7 colunas) -->
      <div class="lg:col-span-7 tactile-panel rounded-2xl p-5 sm:p-6 space-y-5">
        <!-- Cabeçalho do Módulo Hardware -->
        <div class="flex items-center justify-between border-b border-slate-800 pb-3">
          <div class="flex items-center gap-3">
            <div class="w-9 h-9 rounded bg-slate-800 border border-slate-700 flex items-center justify-center text-slate-200">
              <Cpu class="w-5 h-5 text-emerald-400" />
            </div>
            <div>
              <h3 class="text-sm font-semibold text-slate-100">Dispositivo de Campo: ESP32 DevKit v1</h3>
              <p class="text-[11px] font-mono text-slate-400">ADC 10-bit • GPIO 34 (LDR) • GPIO 14 (Botão) • GPIO 25/26/27 (RGB)</p>
            </div>
          </div>
          <span class="text-[11px] font-mono font-medium text-emerald-400 bg-emerald-950/40 border border-emerald-800/40 px-2 py-0.5 rounded">
            FIRMWARE v1.4.0
          </span>
        </div>

        <!-- Seletor da Torre a Simular -->
        <div>
          <label class="block text-xs font-medium text-slate-300 mb-1.5 flex items-center gap-1.5">
            <Radio class="w-3.5 h-3.5 text-slate-400" />
            Ativo Vinculado (Torre de Iluminação):
          </label>
          <select
            v-model="selectedCode"
            class="w-full bg-slate-950 border border-slate-800 rounded px-3 py-2 text-xs font-mono text-slate-100 focus:outline-none focus:border-amber-500"
          >
            <option v-for="t in towerStore.towers" :key="t.id" :value="t.codigoIdentificacao">
              {{ t.codigoIdentificacao }} — {{ t.setorNome }} [{{ t.statusLabel }}]
            </option>
          </select>
        </div>

        <!-- Sensor LDR: Slider de Luminosidade (RF06) -->
        <div class="p-4 bg-slate-950 border border-slate-800 rounded space-y-3">
          <div class="flex justify-between items-center">
            <div>
              <div class="flex items-center gap-2">
                <Sun class="w-3.5 h-3.5 text-amber-400" />
                <span class="text-xs font-semibold text-slate-200">Sensor Fotoelétrico LDR (GPIO 34 / ADC)</span>
                <span
                  class="text-[10px] font-mono px-2 py-0.5 rounded uppercase font-semibold"
                  :class="ldrValue < 300 ? 'bg-amber-500/10 text-amber-400 border border-amber-500/30' : 'bg-slate-800 text-slate-400 border border-slate-700'"
                >
                  {{ ldrValue < 300 ? 'ALERTA: CERRAÇÃO / BAIXA LUZ (<300)' : 'NÍVEL REGULAR' }}
                </span>
              </div>
              <p class="text-[11px] text-slate-400 mt-0.5">
                Valores analógicos inferiores a 300 ativam automaticamente o modo Cerração no CCO
              </p>
            </div>
            <div class="text-right">
              <span class="text-sm font-mono font-bold text-amber-400 bg-slate-900 border border-slate-800 px-2.5 py-1 rounded">
                {{ ldrValue }} <span class="text-[10px] text-slate-400">/ 1023</span>
              </span>
            </div>
          </div>

          <input
            v-model.number="ldrValue"
            type="range"
            min="0"
            max="1023"
            class="w-full accent-amber-500 cursor-pointer h-1.5 bg-slate-800 rounded"
          />

          <!-- Botões de Atalho para Condições Ambientais -->
          <div class="flex flex-wrap gap-2 pt-1">
            <button
              @click="ldrValue = 120"
              class="px-2.5 py-1 rounded bg-slate-800 hover:bg-slate-700 text-[11px] font-medium text-slate-300 border border-slate-700 flex items-center gap-1.5 transition-colors"
            >
              <CloudFog class="w-3 h-3 text-amber-400" />
              Cerração Densa (120)
            </button>
            <button
              @click="ldrValue = 250"
              class="px-2.5 py-1 rounded bg-slate-800 hover:bg-slate-700 text-[11px] font-medium text-slate-300 border border-slate-700 flex items-center gap-1.5 transition-colors"
            >
              <Moon class="w-3 h-3 text-blue-400" />
              Noite Fechada (250)
            </button>
            <button
              @click="ldrValue = 850"
              class="px-2.5 py-1 rounded bg-slate-800 hover:bg-slate-700 text-[11px] font-medium text-slate-300 border border-slate-700 flex items-center gap-1.5 transition-colors"
            >
              <Sun class="w-3 h-3 text-amber-300" />
              Dia Ensolarado (850)
            </button>
          </div>
        </div>

        <!-- Botão Físico de Cerração (GPIO 14) & Bateria -->
        <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <!-- Entrada Digital Cerração -->
          <div class="p-4 bg-slate-950 border border-slate-800 rounded space-y-3 flex flex-col justify-between">
            <div>
              <div class="flex items-center gap-1.5">
                <AlertTriangle class="w-3.5 h-3.5 text-amber-400" />
                <span class="text-xs font-semibold text-slate-200">Botão de Emergência Cerração</span>
              </div>
              <p class="text-[11px] text-slate-400 mt-1">Interrupção por hardware no pino digital GPIO 14</p>
            </div>
            <button
              @click="triggerFogButton"
              class="w-full py-2.5 rounded font-mono text-xs uppercase tracking-wider font-semibold border transition-all flex items-center justify-center gap-2"
              :class="fogButtonPressed 
                ? 'bg-amber-500 text-slate-950 border-amber-400 font-bold' 
                : 'bg-slate-800 hover:bg-slate-700 text-slate-200 border-slate-700 active:bg-amber-600'"
            >
              <Power class="w-3.5 h-3.5" />
              <span>{{ fogButtonPressed ? 'SINAL GPIO 14 ATIVO' : 'Pressionar Pulso Cerração' }}</span>
            </button>
          </div>

          <!-- Medidor de Bateria -->
          <div class="p-4 bg-slate-950 border border-slate-800 rounded space-y-3">
            <div class="flex justify-between items-center">
              <div class="flex items-center gap-1.5">
                <Battery class="w-3.5 h-3.5 text-emerald-400" />
                <span class="text-xs font-semibold text-slate-200">Carga da Bateria (Banco 24V)</span>
              </div>
              <span class="text-xs font-mono font-bold text-emerald-400">{{ batteryLevel }}%</span>
            </div>
            <input
              v-model.number="batteryLevel"
              type="range"
              min="5"
              max="100"
              class="w-full accent-emerald-500 cursor-pointer h-1.5 bg-slate-800 rounded"
            />
            <div class="flex justify-between text-[10px] font-mono text-slate-500">
              <span>Descarga (5%)</span>
              <span>Nominal (25.6V)</span>
              <span>100%</span>
            </div>
          </div>
        </div>

        <!-- Ações de Transmissão SCADA -->
        <div class="flex flex-col sm:flex-row items-center justify-between gap-4 pt-2 border-t border-slate-800">
          <button
            @click="sendTelemetry"
            :disabled="transmitting"
            class="w-full sm:w-auto px-4 py-2 bg-emerald-600 hover:bg-emerald-500 text-slate-950 font-bold rounded text-xs transition-colors flex items-center justify-center gap-2 disabled:opacity-50"
          >
            <Radio class="w-3.5 h-3.5" />
            <span>{{ transmitting ? 'Transmitindo...' : 'Disparar Pacote Telemetria (POST)' }}</span>
          </button>

          <label class="flex items-center gap-2 text-xs font-medium text-slate-300 cursor-pointer select-none">
            <input
              type="checkbox"
              v-model="autoLoop"
              class="w-3.5 h-3.5 rounded accent-emerald-500"
            />
            <span>Transmissão Periódica Cíclica (a cada 3s)</span>
          </label>
        </div>
      </div>

      <!-- Painel Direito: Atuador LED RGB e Console HTTP (5 colunas) -->
      <div class="lg:col-span-5 space-y-6">
        <!-- Visualização da Luminária / LED RGB (Item 9 SENAI) -->
        <div class="tactile-panel rounded-2xl p-5 sm:p-6 space-y-4">
          <div class="flex items-center justify-between border-b border-slate-800 pb-3">
            <div class="flex items-center gap-2">
              <Zap class="w-4 h-4 text-amber-400" />
              <h4 class="text-xs font-bold text-slate-200 uppercase tracking-wider">
                Atuador: Luminária LED RGB (GPIO 25, 26, 27)
              </h4>
            </div>
            <span class="text-[10px] font-mono px-2 py-0.5 rounded bg-slate-800 text-slate-400 border border-slate-700">
              RETORNO SCADA
            </span>
          </div>

          <!-- Lâmpada Industrial de Alta Precisão (Sem Glow de Vibecoding) -->
          <div class="py-5 flex flex-col items-center justify-center bg-slate-950 border border-slate-800/80 rounded">
            <div
              class="w-24 h-24 rounded-full border-4 flex items-center justify-center transition-all duration-300 relative"
              :style="{
                borderColor: ledColorHex,
                backgroundColor: ledOn ? `${ledColorHex}22` : '#0f172a'
              }"
            >
              <div 
                class="w-12 h-12 rounded-full flex items-center justify-center transition-all duration-300"
                :style="{ backgroundColor: ledColorHex }"
              >
                <Zap class="w-6 h-6 text-slate-950" />
              </div>
            </div>

            <!-- Informações Técnicas da Luminária -->
            <div class="mt-4 text-center space-y-1">
              <span class="text-xs font-bold text-slate-100 block">{{ ledStatusTitle }}</span>
              <div class="flex items-center justify-center gap-3 text-xs font-mono text-slate-400">
                <span>Estado: <strong :class="ledOn ? 'text-emerald-400' : 'text-slate-500'">{{ ledOn ? 'ATIVO' : 'DESLIGADO' }}</strong></span>
                <span>•</span>
                <span>Modulação PWM: <strong class="text-slate-200">{{ ledPwmValue }}/255 ({{ Math.round((ledPwmValue / 255) * 100) }}%)</strong></span>
              </div>
            </div>
          </div>
        </div>

        <!-- Terminal / Log HTTP em Tempo Real -->
        <div class="tactile-panel rounded-2xl p-4 sm:p-5 space-y-3">
          <div class="flex items-center justify-between border-b border-slate-800 pb-2">
            <div class="flex items-center gap-2 text-xs font-mono text-slate-300">
              <Terminal class="w-3.5 h-3.5 text-emerald-400" />
              <span>Console de Telemetria e Eventos HTTP</span>
            </div>
            <button
              @click="terminalLogs = []"
              class="text-[10px] font-mono text-slate-400 hover:text-slate-200 px-2 py-0.5 rounded bg-slate-900 border border-slate-800 transition-colors"
            >
              Limpar
            </button>
          </div>

          <div class="h-60 overflow-y-auto space-y-1 font-mono text-[11px] text-slate-300 p-1 divide-y divide-slate-900/60">
            <div
              v-for="(log, idx) in terminalLogs"
              :key="idx"
              class="pt-1 leading-relaxed"
              :class="log.type === 'response' ? 'text-emerald-400' : (log.type === 'alert' ? 'text-amber-400 font-semibold' : 'text-slate-400')"
            >
              {{ log.text }}
            </div>
            <div v-if="terminalLogs.length === 0" class="text-slate-600 italic text-center pt-10 text-xs">
              Barramento ocioso. Acione os sensores e clique em "Disparar Pacote Telemetria".
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
import {
  Cpu,
  Radio,
  Sun,
  Moon,
  CloudFog,
  AlertTriangle,
  Power,
  Battery,
  Zap,
  Terminal
} from 'lucide-vue-next';

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
  if (!ledOn.value) return '#475569';
  if (ledModo.value === 'ALTA_VISIBILIDADE_CERRACAO') return '#f59e0b'; // Âmbar
  if (ledModo.value === 'ECONOMIA') return '#38bdf8'; // Azul
  return '#10b981'; // Verde operacional
});

const ledStatusTitle = computed(() => {
  if (!ledOn.value) return 'Luminária Apagada (Desligada)';
  if (ledModo.value === 'ALTA_VISIBILIDADE_CERRACAO') return 'Alta Visibilidade Cerração (Âmbar 100%)';
  if (ledModo.value === 'ECONOMIA') return 'Modo Econômico de Operação (Azul 50%)';
  return 'Modo Normal Operacional (Verde 80%)';
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
      addLog(`< 200 OK: ALERTA [${data.mensagem}]`, 'alert');
    } else {
      addLog(`< 200 OK: LED=${data.instrucaoLed ? 'ON' : 'OFF'} | Modo=${data.modoLuz} | PWM=${data.intensidadePwm}`, 'response');
    }

    await towerStore.fetchTowers();
  } catch (err) {
    addLog(`! ERRO HTTP: ${err.message}`, 'alert');
  } finally {
    transmitting.value = false;
    if (fogButtonPressed.value) {
      fogButtonPressed.value = false;
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
  addLog('Simulador IoT conectado ao barramento. Pronto para testes de LDR e atuadores.');
});

onBeforeUnmount(() => {
  if (autoTimer) clearInterval(autoTimer);
});
</script>
