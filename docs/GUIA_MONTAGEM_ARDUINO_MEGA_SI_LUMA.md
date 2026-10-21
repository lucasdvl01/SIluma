# Guia do circuito Arduino Mega — SI Luma

Este guia descreve a montagem física usada no projeto: Arduino Mega 2560,
fotoresistor LDR, botão de cerração e três LEDs indicadores. O Mega comunica-se
com o backend por USB e por uma ponte PowerShell executada no computador.

> Os LEDs são apenas indicadores de estado. Este circuito não aciona uma
> luminária real, não mede bateria e não tem conexão de rede própria.

## Componentes

- Arduino Mega 2560 e cabo USB de dados.
- Protoboard e jumpers macho-macho.
- LDR avulso e resistor de 10 kΩ para o divisor de tensão.
- Botão normalmente aberto.
- LEDs vermelho, verde e amarelo, cada um com resistor de 220 Ω.

## Ligações

| Componente | Pino/conexão no Mega | Observação |
|---|---|---|
| LDR, terminal 1 | 5V | Alimentação do divisor |
| LDR, terminal 2 | A7 e terminal 1 do resistor de 10 kΩ | Nó de leitura analógica |
| Resistor de 10 kΩ, terminal 2 | GND | Referência do divisor |
| Botão, terminal 1 | Pino digital 3 | Entrada com `INPUT_PULLUP` |
| Botão, terminal 2 | GND | Pressionado = `LOW` |
| LED vermelho, ânodo | Pino digital 51 via resistor de 220 Ω | Falha técnica/manutenção |
| LED verde, ânodo | Pino digital 49 via resistor de 220 Ω | Operação normal |
| LED amarelo, ânodo | Pino digital 47 via resistor de 220 Ω | Baixa visibilidade/modo econômico |
| Cátodo dos LEDs | GND | Terra comum |

Confira a polaridade dos LEDs; cada LED precisa de seu próprio resistor. Desligue
o USB antes de alterar os fios. Com o LDR entre 5V e A7 e o resistor de 10 kΩ
entre A7 e GND, a leitura normalmente aumenta com mais luz. Observe os valores
reais e calibre o limiar de 300 se necessário.

## Como iniciar

1. Grave `InoSiluma/InoSiluma.ino` na placa **Arduino Mega 2560**.
2. Inicie o backend local; a ponte espera a API em `127.0.0.1:8080`.
3. Feche o Monitor Serial da Arduino IDE, pois ele ocupa a porta USB/COM.
4. Na raiz do repositório, execute no PowerShell, trocando `COM6` pela porta da
   placa:

   ```powershell
   powershell.exe -NoProfile -ExecutionPolicy Bypass -File ".\iot-simulator\serial_http_bridge.ps1" -Port COM6
   ```

5. Para parar a ponte e liberar a porta, pressione `Ctrl+C`.

O Mega envia uma telemetria JSON a cada três segundos pela USB. A ponte encaminha
os dados ao endpoint `/api/iot/telemetry` e devolve um único caractere por linha:
`V` (verde), `A` (amarelo), `R` (vermelho), `Z` (modo econômico, representado
por amarelo nesta montagem) ou `O` (apagado).

## Comportamento esperado

| Cenário | Resultado esperado |
|---|---|
| LDR acima do limiar, torre ligada | LED verde |
| LDR abaixo de 300 ou botão pressionado, torre ligada | LED amarelo e alerta no sistema |
| Torre desligada pela interface | LEDs apagados na próxima telemetria |
| Falha técnica ou manutenção | LED vermelho |
| LDR volta ao nível normal | O sistema restaura o estado de operação da torre |

Falha/manutenção tem prioridade. Fora desses estados, uma torre desligada
permanece apagada mesmo se o LDR detectar pouca luz. Os comandos feitos na
interface chegam à placa na próxima telemetria, em até aproximadamente três
segundos.

## Limitações do protótipo

- `nivelBateria` é um valor demonstrativo fixo; não existe sensor de bateria.
- Os LEDs indicam estados e não comutam uma luminária de campo.
- O Arduino Mega não envia dados diretamente pela internet: o computador e a
  ponte precisam permanecer ligados durante a integração.
- O botão usa nível baixo (`LOW`) quando pressionado.
- O LED amarelo representa também o modo econômico porque a montagem tem LEDs
  discretos e não possui canal azul.

## Segurança

- Faça alterações na protoboard com o USB desconectado.
- Não conecte cargas de potência ou rede elétrica aos pinos da placa.
- Não conecte bateria de 24 V ao Arduino, LDR ou LEDs deste protótipo.
- Se algum componente ou fio aquecer, desconecte o USB e revise a montagem.
