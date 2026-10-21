/**
 * =====================================================================================
 * SIMULADOR STANDALONE DE TELEMETRIA IOT
 * Projeto: Iluminação Inteligente para Minas - SENAI Betim
 * =====================================================================================
 * Este script em Node.js simula em tempo real múltiplas torres de iluminação enviando
 * telemetria para a API Spring Boot (RF06).
 *
 * Como executar:
 *   node simulator_runner.js
 * =====================================================================================
 */

const http = require('http');

const API_HOST = process.env.API_HOST || 'localhost';
const API_PORT = process.env.API_PORT || 8080;
const API_PATH = '/api/iot/telemetry';

console.log('===========================================================');
console.log('  SIMULADOR DE TELEMETRIA IOT (SENAI BETIM)                ');
console.log('===========================================================');
console.log(`Conectando em http://${API_HOST}:${API_PORT}${API_PATH}\n`);

// Torres simuladas com diferentes condições iniciais
const towers = [
  { codigo: 'TR-001', ldr: 850, bateria: 94.0, fog: false, step: 0 },
  { codigo: 'TR-002', ldr: 180, bateria: 62.0, fog: true, step: 0 }, // Cerração
  { codigo: 'TR-003', ldr: 900, bateria: 78.0, fog: false, step: 0 },
  { codigo: 'TR-005', ldr: 820, bateria: 97.0, fog: false, step: 0 },
  { codigo: 'TR-007', ldr: 880, bateria: 82.0, fog: false, step: 0 }
];

function sendTelemetry(tower) {
  // Simula oscilações realistas de luminosidade e queda lenta de bateria
  tower.step++;
  
  // A cada 10 ciclos na TR-001, simula uma queda de luz (cerração passageira na mina)
  if (tower.codigo === 'TR-001') {
    if (tower.step % 12 >= 5 && tower.step % 12 <= 8) {
      tower.ldr = 150; // Cerração!
      tower.fog = true;
    } else {
      tower.ldr = 800 + Math.floor(Math.random() * 80);
      tower.fog = false;
    }
  } else if (!tower.fog) {
    tower.ldr = Math.max(350, Math.min(1000, tower.ldr + (Math.floor(Math.random() * 50) - 25)));
  }

  // Descarga suave
  tower.bateria = Math.max(15, +(tower.bateria - 0.05).toFixed(1));

  const payload = JSON.stringify({
    codigoTorre: tower.codigo,
    luminosidadeLdr: tower.ldr,
    statusLed: true,
    nivelBateria: tower.bateria,
    pressaoBotaoCerracao: tower.fog
  });

  const options = {
    hostname: API_HOST,
    port: API_PORT,
    path: API_PATH,
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Content-Length': Buffer.byteLength(payload)
    }
  };

  const req = http.request(options, (res) => {
    let data = '';
    res.on('data', (chunk) => data += chunk);
    res.on('end', () => {
      try {
        const json = JSON.parse(data);
        const icon = json.alertaAtivado ? '🌫️ [ALERTA CERRAÇÃO]' : '💡 [OPERACIONAL]';
        console.log(`[${new Date().toLocaleTimeString()}] ${tower.codigo} -> LDR: ${tower.ldr} | Bat: ${tower.bateria}% | Resposta: ${icon} LED=${json.instrucaoLed} Modo=${json.modoLuz}`);
      } catch (e) {
        console.log(`[${new Date().toLocaleTimeString()}] ${tower.codigo} -> Resposta HTTP ${res.statusCode}`);
      }
    });
  });

  req.on('error', (e) => {
    console.error(`[ERRO CONEXÃO] ${tower.codigo}: Não foi possível conectar ao backend (${e.message}). O backend Spring Boot está ativo na porta ${API_PORT}?`);
  });

  req.write(payload);
  req.end();
}

// Envia telemetria alternada a cada 2.5 segundos
let index = 0;
setInterval(() => {
  sendTelemetry(towers[index]);
  index = (index + 1) % towers.length;
}, 2500);

console.log('Simulador iniciado! Pressione Ctrl+C para encerrar.');
