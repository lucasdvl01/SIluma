# Guia de montagem do circuito IoT — SI Luma

Checklist de bancada para montar e testar a torre demonstrativa do SI Luma. O
projeto tem duas montagens diferentes: o circuito originalmente planejado com
ESP32 e a montagem alternativa já realizada com Arduino Mega 2560, que transmite
telemetria pela serial USB.

> **Escopo seguro da demonstração:** LEDs são apenas indicadores visuais. Nenhuma
> montagem deste guia alimenta uma luminária real, comuta cargas de potência ou
> mede um banco de baterias de 24 V.

> **Montagem realizada:** Arduino Mega 2560, LDR avulso no divisor resistivo em
> A7, botão no pino digital 3 e três LEDs separados nos pinos 47, 49 e 51.
> Essa variante não tem Wi-Fi: o firmware descrito pelo usuário imprime JSON no
> Monitor Serial, mas não envia dados ao backend por conta própria. A montagem
> ESP32 com Wi-Fi continua documentada nas seções seguintes.

## Montagem realizada — Arduino Mega 2560

Esta é a configuração da placa montada. Ela é apropriada para testar o sensor,
o botão, os indicadores e a geração de dados seriais. Não requer módulo LDR:
o LDR avulso e um resistor de 10 kΩ formam o divisor de tensão.

### Materiais

- Arduino Mega 2560 e cabo USB de dados.
- Protoboard e jumpers macho-macho.
- LDR avulso e resistor de 10 kΩ.
- Botão push button normalmente aberto.
- LED vermelho, LED verde e LED amarelo.
- Um resistor de 220 Ω em série com cada LED.

### Ligações

| Componente | Pino/conexão no Mega | Observação |
|---|---|---|
| LDR, terminal 1 | 5V | Alimentação do divisor |
| LDR, terminal 2 | A7 e terminal 1 do resistor de 10 kΩ | Nó de leitura analógica |
| Resistor de 10 kΩ, terminal 2 | GND | Referência do divisor |
| Botão, terminal 1 | Pino digital 3 | Entrada com `INPUT_PULLUP` |
| Botão, terminal 2 | GND | Pressionado = `LOW` |
| LED vermelho, ânodo | Pino digital 51, passando por resistor de 220 Ω | Indicador de inicialização/teste |
| LED verde, ânodo | Pino digital 49, passando por resistor de 220 Ω | Indicação normal |
| LED amarelo, ânodo | Pino digital 47, passando por resistor de 220 Ω | Indicação de alerta |
| Cátodo de cada LED | GND | Terra comum |

Confira a perna longa do LED (ânodo) e a perna curta (cátodo); a identificação
física pode variar. Cada LED precisa do seu próprio resistor em série. Desligue
o USB antes de alterar os fios. Nesta montagem Mega, o divisor é alimentado em
5 V e conectado apenas à entrada analógica A7 do Mega; não reaproveite essa
ligação em um ESP32, cujos GPIOs não toleram 5 V.

O botão deve fechar contato entre o pino 3 e GND quando pressionado. O pull-up
interno mantém a entrada em nível alto quando solto. O valor de A7 varia de
0 a 1023; com LDR entre 5V e A7 e resistor de 10 kΩ entre A7 e GND, a leitura
normalmente sobe com mais luz. Confirme isso no Monitor Serial e calibre o
limiar de 300 conforme a iluminação e os componentes da montagem.

### O que o firmware Mega demonstra e o que não demonstra

- O teste inicial acende verde, amarelo e vermelho em sequência; durante o laço,
  verde indica operação normal, amarelo indica alerta de baixa visibilidade e
  vermelho indica falha técnica ou manutenção.
- A cada 3 segundos, o sketch imprime uma linha JSON no Monitor Serial a
  115200 baud. A ponte ignora as mensagens do teste inicial e encaminha os
  objetos JSON ao backend.
- O Arduino Mega 2560 não tem Wi-Fi integrado. O `Serial.println` envia dados
  somente pela USB ao computador. A ponte
  `iot-simulator/serial_http_bridge.ps1` encaminha a telemetria ao endpoint
  local e escreve a resposta do backend de volta pela mesma serial.
- Para usar a ponte, grave `InoSiluma/InoSiluma.ino` no Mega, feche o Monitor
  Serial e execute no PowerShell:

  ```powershell
  .\iot-simulator\serial_http_bridge.ps1 -Port COM6
  ```

  Troque `COM6` pela porta exibida no Gerenciador de Dispositivos. O backend
  deve estar ativo na mesma máquina em `127.0.0.1:8080`. Pressione `Ctrl+C`
  para encerrar a ponte e liberar a porta.
- O sketch processa a cor sugerida pelo backend: amarelo para alerta de baixa
  visibilidade, verde para operação normal, vermelho para falha/manutenção e
  LEDs apagados quando a torre está desligada. A ponte envia o comando de volta
  como um caractere por linha (`A`, `V`, `R`, `Z` ou `O`), evitando interpretar
  JSON na recepção da placa. Os comandos de ligar/desligar feitos na interface
  chegam à placa na próxima telemetria (até cerca de 3 segundos).
- Falha/manutenção tem prioridade e mantém o vermelho aceso. Fora desses casos,
  desligar a torre apaga os LEDs mesmo se o LDR detectar baixa visibilidade;
  ligada, a torre usa amarelo para esse alerta.
- A montagem Mega não tem LED azul; por isso, o modo econômico é representado
  pelo LED amarelo. Os LEDs são indicadores e não acionam uma luminária real.
- `nivelBateria = 92.5` é um valor fixo demonstrativo; o circuito não mede
  bateria. O LED amarelo/vermelho/verde também não comuta uma luminária real.
- O Mega envia em `statusLed` o último comando recebido; a ponte envia ao
  backend os campos de luminosidade, botão e bateria simulada. O backend decide
  o alerta com `pressaoBotaoCerracao` e `luminosidadeLdr`.

## 1. O que o circuito precisa demonstrar

- Ler a luminosidade com um LDR.
- Permitir o acionamento manual do alerta de cerração por botão.
- Enviar as leituras por Wi-Fi para o backend do SI Luma.
- Receber a resposta do backend e representar o estado com um LED RGB:
  - **Âmbar:** alerta de baixa visibilidade / cerração.
  - **Verde:** operação normal.
  - **Azul:** modo economia.
  - **Apagado:** luminária desligada.
- Enviar o código de uma torre existente no sistema (por exemplo, `TR-001`).

## 2. Lista de materiais

### Obrigatórios

- [ ] 1 placa **ESP32 DevKit V1** (o firmware e o diagrama foram preparados para ESP32).
- [ ] 1 cabo USB de dados compatível com a placa.
- [ ] 1 protoboard.
- [ ] Jumpers macho-macho.
- [ ] 1 sensor LDR com módulo analógico e pinos `VCC`, `GND` e `AO`.
- [ ] 1 LED RGB de **cátodo comum**.
- [ ] 3 resistores de **220 Ω**, um para cada canal R, G e B.
- [ ] 1 botão normalmente aberto (push button).
- [ ] Computador com Arduino IDE, acesso ao projeto e conexão à mesma rede Wi-Fi do ESP32 e do backend.

### Se não houver módulo LDR

Pode-se usar um LDR avulso e um resistor fixo de aproximadamente **10 kΩ** para
formar um divisor de tensão. Ligue `3V3 → LDR → nó do GPIO 34 → resistor de 10 kΩ
→ GND`. Essa orientação faz a leitura aumentar com a luz e diminuir no escuro.
Calibre o limiar observando os valores reais no monitor serial; LDRs e resistores
podem variar.

## 3. Pinagem e ligações

| Elemento | ESP32 | Ligação |
|---|---:|---|
| Saída analógica do módulo LDR (`AO`) | GPIO 34 | Sinal analógico de luminosidade |
| Alimentação do módulo LDR (`VCC`) | 3V3 | **Não usar 5 V** no sinal ligado ao ESP32 |
| Terra do módulo LDR (`GND`) | GND | Terra comum |
| Botão de cerração | GPIO 14 | Um lado no GPIO 14; outro lado no GND |
| LED RGB — canal vermelho (`R`) | GPIO 25 | GPIO → resistor 220 Ω → terminal R |
| LED RGB — canal verde (`G`) | GPIO 26 | GPIO → resistor 220 Ω → terminal G |
| LED RGB — canal azul (`B`) | GPIO 27 | GPIO → resistor 220 Ω → terminal B |
| LED RGB — comum (cátodo) | GND | O diagrama atual usa LED RGB de **cátodo comum** |

O botão usa `INPUT_PULLUP` interno: solto = nível alto; pressionado = nível baixo.
Não é necessário resistor externo de pull-up para essa ligação.

### Esquema textual

```text
ESP32 3V3 ───────────── VCC do módulo LDR
ESP32 GND ───────────── GND do módulo LDR
ESP32 GPIO 34 ───────── AO do módulo LDR

ESP32 GPIO 14 ── botão normalmente aberto ── ESP32 GND

ESP32 GPIO 25 ── 220 Ω ── terminal R do LED RGB
ESP32 GPIO 26 ── 220 Ω ── terminal G do LED RGB
ESP32 GPIO 27 ── 220 Ω ── terminal B do LED RGB
ESP32 GND ────────────── comum/cátodo do LED RGB
```

Confirme a identificação dos terminais do LED RGB no datasheet ou com o
multímetro: a ordem física dos pinos varia entre modelos. Cada cor precisa do
seu próprio resistor em série.

## 4. Regras de segurança da montagem

- [ ] Desconecte o USB antes de alterar fios na protoboard.
- [ ] Use somente a alimentação lógica de **3,3 V** do ESP32 para este circuito.
- [ ] Nunca ligue 5 V diretamente a um GPIO ou ao sinal `AO` do ESP32.
- [ ] Nunca conecte bateria/banco de **24 V** ao ESP32, ao LDR ou ao LED deste
  protótipo.
- [ ] Não conecte lâmpada, relé, motor, rede elétrica ou carga de potência aos
  pinos da placa. O LED RGB é somente um indicador.
- [ ] Se um dia for necessário medir uma bateria, use um circuito de medição
  apropriado, dimensionado e protegido para a tensão máxima; isso não faz parte
  do circuito atual.
- [ ] Se a placa, resistor ou fio aquecer, desconecte o USB e revise a ligação.

## 5. Preparação do firmware e da rede

- [ ] Instale o Arduino IDE 2.x.
- [ ] Instale o suporte de placas **ESP32 by Espressif Systems**.
- [ ] Selecione a placa ESP32 Dev Module/DevKit V1 correspondente e a porta COM
  correta.
- [ ] Instale a biblioteca **ArduinoJson 6** (o firmware usa `StaticJsonDocument`).
- [ ] O firmware atual usa `ledcSetup` e `ledcAttachPin`, APIs compatíveis com o
  core Arduino ESP32 2.x. Com core 3.x, adapte o PWM antes de compilar
  (`ledcAttach`/`ledcWrite` por pino, conforme a versão instalada).
- [ ] Abra `iot-simulator/arduino_esp32_firmware.ino`.
- [ ] Configure `ssid` e `password` com a rede Wi-Fi que o ESP32 pode acessar.
- [ ] Configure `CODIGO_TORRE` para uma torre existente, inicialmente `TR-001`.
- [ ] Configure `serverBaseUrl` com o endereço IP da máquina onde o backend roda:
  `http://IP_DA_MAQUINA:8080/api/iot`.
- [ ] Não use `localhost` no ESP32 físico: para a placa, `localhost` aponta para
  a própria placa, não para o computador.
- [ ] Deixe ESP32, computador e backend acessíveis pela mesma rede. Permita
  conexões de entrada na porta 8080 do firewall, se necessário.
- [ ] Compile e grave o firmware; abra o Monitor Serial em **115200 baud**.

Para Wokwi, use a configuração de rede/gateway própria da simulação e o endereço
de backend que ela consegue alcançar. O endereço de gateway da simulação não é
necessariamente o mesmo endereço a usar com a placa física.

## 6. Fluxo de dados que o circuito deve cumprir

1. O ESP32 lê o LDR em GPIO 34 e normaliza a leitura para a faixa **0–1023**.
2. A cada aproximadamente **3 segundos**, ou ao pressionar o botão, envia:

   ```http
   POST /api/iot/telemetry
   Content-Type: application/json
   ```

   ```json
   {
     "codigoTorre": "TR-001",
     "luminosidadeLdr": 180,
     "statusLed": false,
     "nivelBateria": 92.5,
     "pressaoBotaoCerracao": false
   }
   ```

3. O backend considera leitura **menor que 300** ou botão pressionado como
   condição de baixa visibilidade.
4. O backend responde com campos como `instrucaoLed`, `modoLuz`,
   `intensidadePwm` e `alertaAtivado`.
5. O ESP32 atualiza o LED RGB conforme a resposta. Se estiver sem Wi-Fi, o
   firmware atual acende âmbar localmente quando LDR `< 300` ou o botão está
   pressionado.

## 7. Roteiro de teste na bancada

### A. Antes de ligar

- [ ] Conferir cada ligação com a tabela e o esquema.
- [ ] Confirmar cátodo comum do LED e os três resistores de 220 Ω.
- [ ] Confirmar que GPIO 34 recebe somente o sinal analógico de até 3,3 V.
- [ ] Confirmar que o botão liga GPIO 14 ao GND quando pressionado.

### B. Inicialização

- [ ] Ligar o ESP32 por USB.
- [ ] Confirmar no Monitor Serial a inicialização e o nome/código da torre.
- [ ] Confirmar que o ESP32 obteve conexão Wi-Fi e endereço IP.
- [ ] Observar o teste inicial do LED: verde, âmbar, azul e apagado.

### C. Testes funcionais

| Teste | Ação | Resultado esperado |
|---|---|---|
| Luz normal | Iluminar o LDR e obter leitura acima de 300 | Sem alerta de cerração; backend devolve o estado atual da torre |
| Baixa luminosidade | Cobrir o LDR e obter leitura abaixo de 300 | Backend marca alerta de baixa visibilidade; LED fica âmbar |
| Botão manual | Pressionar brevemente o botão de GPIO 14 | Telemetria indica `pressaoBotaoCerracao: true`; alerta/LED âmbar |
| Recuperação | Descobrir o LDR e voltar a leitura para 300 ou mais | Backend pode restaurar o estado normal da torre |
| Desligamento remoto | Desligar `TR-001` pela interface com a ponte ativa | Na próxima telemetria, todos os LEDs ficam apagados |
| Religamento remoto | Ligar `TR-001` pela interface com a ponte ativa | Na próxima telemetria, o LED volta a indicar o modo atual |
| Falha/manutenção | Colocar a torre em falha técnica ou abrir uma ordem de manutenção | LED vermelho permanece aceso |
| Perda de Wi-Fi | Desconectar a rede e cobrir o LDR | Fallback local acende âmbar; serial informa que a rede está indisponível |
| Reconexão | Restaurar Wi-Fi | ESP32 volta a enviar telemetria ao backend |

O valor **300** é o limiar usado pelo backend e pelo simulador web; o teste deve
confirmar a leitura que o seu módulo produz de fato. Se o valor cair quando há
mais luz, revise o módulo/divisor e calibre antes de apresentar.

### D. Conferência no sistema

- [ ] Iniciar o backend e confirmar que está acessível na porta 8080.
- [ ] Entrar no SI Luma e abrir **Bancada IoT (ESP32)**.
- [ ] Confirmar que `TR-001` existe e está selecionável.
- [ ] Conferir se os dados e o alerta aparecem no painel/dashboard.
- [ ] Após enviar alerta, conferir a torre em **Frota** e, se necessário, o
  registro em **Auditoria**.

## 8. O que o protótipo atual simula — não esquecer

- **Bateria:** `nivelBateria` é gerado por uma variável do firmware (começa em
  92,5%); não há sensor de tensão de bateria no diagrama ou no circuito atual.
- **Luminária:** o LED RGB representa visualmente o comando. Não é uma luminária
  de campo e não deve ser ligado diretamente a uma carga real.
- **Escala do LDR:** o firmware converte a leitura ADC do ESP32 (0–4095) para
  0–1023, que é a escala esperada pela API.
- **Botão:** a lógica é ativa em nível baixo (`LOW`) por causa do pull-up interno.
- **PWM da resposta:** a API envia `intensidadePwm` de 0 a 255. O firmware atual
  lê esse campo, mas escolhe cores com valores fixos e não aplica a intensidade
  recebida aos canais. Para demonstrar controle de brilho proporcional, essa
  lógica precisa ser implementada no firmware.
- **Wi-Fi:** sem comunicação com o backend, só o fallback local de cerração é
  exercitado; a atualização do estado no SI Luma depende da API.

## 9. Checklist final para levar à escola

- [ ] ESP32 DevKit V1 e cabo USB de dados.
- [ ] Protoboard e jumpers.
- [ ] Módulo LDR analógico (ou LDR avulso + resistor de 10 kΩ).
- [ ] LED RGB de cátodo comum + **3 resistores de 220 Ω**.
- [ ] Botão normalmente aberto.
- [ ] Computador, Arduino IDE, biblioteca ArduinoJson 6 e porta USB funcionando.
- [ ] Nome/senha da rede Wi-Fi de teste.
- [ ] Endereço IP do computador que executa o backend.
- [ ] Backend iniciado e torre de teste (por exemplo, `TR-001`) cadastrada.
- [ ] Firmware compilado e gravado; Monitor Serial em 115200 baud.
- [ ] Testados LDR claro/escuro, botão, Wi-Fi e retorno do estado normal.
- [ ] Fotos da montagem e anotações dos valores ADC observados para documentar
  a apresentação.
