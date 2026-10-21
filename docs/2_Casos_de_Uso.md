# Especificação de Casos de Uso
**Projeto:** Sistema de Gestão e Monitoramento de Iluminação Inteligente para Minas  
**Instituição:** SENAI Betim (CETEM Maria Madalena Nogueira)  
**Item Exigido:** Documentação Técnica Exigida - Item 5.2

---

## Sumário dos Casos de Uso
- **UC01:** Cadastrar Torre de Iluminação
- **UC02:** Monitorar Estado da Frota em Tempo Real
- **UC03:** Acionar Torre Remotamente e Alterar Modo de Iluminação
- **UC04:** Executar Comandos Operacionais em Lote por Setor (Áreas Remotas)
- **UC05:** Registrar Pane e Agendar Intervenção de Manutenção (Horímetro Preventivo)
- **UC06:** Transmissão e Automação IoT via Sensor LDR / Botão de Cerração
- **UC07:** Operação Offline e Sincronização Automática no Aplicativo Móvel

---

### UC01: Cadastrar Torre de Iluminação
- **Ator Principal:** Gestor CCO (Perfil `ADMIN`)
- **Pré-condições:** O gestor deve estar autenticado com credenciais de administrador.
- **Pós-condições:** A nova torre é gravada no banco de dados, fica visível no mapa/tabela e sua primeira sessão de atividade é iniciada caso esteja ligada.
- **Fluxo Principal:**
  1. O gestor acessa o menu "Frota de Torres".
  2. O sistema exibe o inventário atual e o botão "Cadastrar Nova Torre".
  3. O gestor clica no botão e o sistema abre o modal de cadastro.
  4. O gestor preenche: Código único (ex: `TR-008`), Setor da Mina, Coordenadas (Latitude/Longitude), Fonte de Energia (Solar, Diesel, Bateria, Híbrido), Nível de Bateria inicial e Limite de Horas para Manutenção Preventiva.
  5. O gestor confirma a operação.
  6. O sistema valida os campos, garante a unicidade do código, persiste no banco e grava o registro na trilha de auditoria com o IP do usuário.
  7. O sistema exibe mensagem de sucesso e atualiza a listagem.
- **Fluxos Alternativos:**
  - *4a. Código de identificação já existente:* O sistema exibe mensagem de erro ("Já existe uma torre com o código informado") e mantém os dados no modal para correção.

---

### UC02: Monitorar Estado da Frota em Tempo Real
- **Atores:** Gestor CCO (`ADMIN`) e Operador de Campo (`OPERADOR`)
- **Pré-condições:** Usuário autenticado.
- **Fluxo Principal:**
  1. O usuário acessa o Painel de Gestão ou o Mapa Georreferenciado.
  2. O sistema consulta as torres ativas e renderiza os marcadores no mapa de satélite com indicativos de cores semafóricas:
     - **Verde:** Operacional e ligada normalmente.
     - **Amarelo pulsante:** Alerta de Baixa Visibilidade / Cerração ativado.
     - **Vermelho:** Torre em manutenção corretiva ou falha.
     - **Cinza:** Torre desligada.
  3. A cada 10 segundos, o sistema realiza polling automático silencioso e atualiza os parâmetros na tela sem necessidade de recarregar a página.

---

### UC03: Acionar Torre Remotamente e Alterar Modo de Iluminação
- **Atores:** Gestor CCO (`ADMIN`) e Operador de Campo (`OPERADOR`)
- **Pré-condições:** A torre deve existir e não pode estar em status `EM_MANUTENCAO`.
- **Fluxo Principal:**
  1. O operador localiza a torre na listagem, mapa ou aplicativo de campo.
  2. O operador clica no botão de comando desejado: "Ligar", "Desligar" ou seleciona o modo:
     - **Normal:** 100% de intensidade.
     - **Economia (Eco):** 50% de intensidade para prolongar autonomia da bateria.
     - **Cerração / Alta Visibilidade:** Máxima potência e foco para neblina e poeira intensa.
  3. O backend processa o comando:
     - Se o comando for **Desligar**, encerra a sessão de atividade contínua aberta, calcula os minutos ativos, o consumo elétrico consumido (kWh) e incrementa o horímetro acumulado.
     - Se o comando for **Ligar**, inicia nova sessão de atividade.
     - Se o horímetro acumulado atingir ou superar o limite estipulado, sinaliza necessidade de manutenção preventiva.
  4. O sistema grava o log de auditoria com usuário, data, hora e IP.
  5. O comando é persistido e fica disponível imediatamente para a leitura do hardware IoT via polling/resposta.

---

### UC04: Executar Comandos Operacionais em Lote por Setor (Áreas Remotas)
- **Ator Principal:** Gestor CCO (`ADMIN`)
- **Pré-condições:** Existência de setores cadastrados contendo torres vinculadas.
- **Fluxo Principal:**
  1. O gestor acessa a aba "Comandos em Lote".
  2. O sistema apresenta os setores e trechos da mina (ex: "Estrada de Escoamento - Trecho 04", "Frente de Lavra Norte").
  3. O gestor seleciona o setor desejado.
  4. O sistema exibe o número de torres vinculadas àquele setor e as opções de disparo em lote.
  5. O gestor aciona "Ativar Modo Cerração para Todo o Trecho" (ou "Ligar Todas" / "Modo Eco").
  6. O sistema executa a ação simultaneamente para todas as torres do setor (exceto as que estejam em manutenção), salva o estado no banco de dados e registra a ação de lote na auditoria.
  7. O sistema exibe feedback de confirmação e atualiza as cores dos pinos daquele trecho no mapa.

---

### UC05: Registrar Pane e Agendar Intervenção de Manutenção (Horímetro)
- **Atores:** Operador de Campo (`OPERADOR`) e Gestor CCO (`ADMIN`)
- **Pré-condições:** Ocorrência de pane em campo ou alerta de horímetro preventivo atingido.
- **Fluxo Principal:**
  1. O encarregado acessa a seção de Manutenções ou clica em "Reportar Falha".
  2. O sistema exibe o formulário de abertura de Ordem de Serviço (OS).
  3. O usuário seleciona a torre, o tipo da falha (ex: "Quebra de luminária", "Revisão Preventiva de 250h"), a gravidade (Baixa, Média, Alta, Crítica) e detalha a ocorrência.
  4. O usuário informa o técnico responsável pela manutenção.
  5. O sistema salva a OS, altera o status da torre para **"Em Manutenção"** (cor Vermelha) e congela o horímetro da torre no momento do reporte.
  6. Quando o técnico conclui o serviço no campo, ele acessa o sistema e clica em "Concluir OS", informando as observações de reparo.
  7. O sistema marca a OS como concluída, restabelece o status da torre para **"Operacional"** e recalcula o próximo ciclo preventivo.

---

### UC06: Transmissão e Automação IoT via Arduino Mega
- **Atores:** Arduino Mega 2560, ponte USB/HTTP e simulador IoT
- **Pré-condições:** Backend ativo na porta 8080; para o circuito físico, ponte serial ativa no computador.
- **Fluxo Principal:**
  1. O Arduino Mega lê o LDR em A7 e o botão de cerração no pino digital 3.
  2. O Arduino envia uma linha JSON pela USB; a ponte serial encaminha a telemetria via `POST /api/iot/telemetry`.
  3. O backend recebe a requisição:
     - Se o valor do LDR for menor que 300 (queda de luminosidade por cerração ou noite) OU o botão tiver sido pressionado:
       - O backend altera o status da torre no sistema para **"Alerta de Baixa Visibilidade"** (cor Amarela no mapa).
       - Altera o modo da torre para `ALTA_VISIBILIDADE_CERRACAO`.
       - Responde a requisição HTTP com status 200 e payload contendo instrução: `{ "instrucaoLed": true, "modoLuz": "ALTA_VISIBILIDADE_CERRACAO", "intensidadePwm": 255 }`.
     - Caso a luminosidade esteja normal (> 300) e o alerta estava ativo, normaliza a torre para **"Operacional"**.
  4. A ponte converte a cor sugerida pelo backend em um caractere serial, e o Mega atualiza os LEDs discretos (amarelo para alerta, verde normal, vermelho falha/manutenção, apagado desligada).

---

### UC07: Operação Offline e Sincronização Automática no Aplicativo Móvel
- **Ator Principal:** Operador de Campo (`OPERADOR`)
- **Pré-condições:** O operador está autenticado no aplicativo móvel / PWA.
- **Fluxo Principal:**
  1. O operador transita por uma galeria profunda ou trecho remoto da mina sem sinal de rede celular/Wi-Fi.
  2. O aplicativo detecta a desconexão e exibe o indicador "Sem Sinal (Offline)".
  3. O operador visualiza a lista de torres armazenada no cache local do dispositivo (RNF-A03, RNF-A11).
  4. O operador aciona um comando (ex: "Ligar Torre TR-003").
  5. O aplicativo emite feedback tátil de vibração (RNF-A10), armazena a instrução na fila persistente local (`localStorage`) e informa que a ação foi enfileirada.
  6. Ao retornar para uma área com cobertura de rede, o aplicativo detecta o evento de reconexão.
  7. A rotina de sincronização em segundo plano despacha automaticamente todos os comandos da fila para a API do backend, esvazia a fila e emite confirmação de sucesso ao operador.
