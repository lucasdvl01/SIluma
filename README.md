# Sistema de Gestão e Monitoramento de Iluminação Inteligente para Minas (Com Extensão IoT)

**Instituição:** SENAI Betim (CETEM Maria Madalena Nogueira)  
**Área:** Tecnologia da Informação  
**Stack Tecnológica:** Java 17 (Spring Boot 3.x) + Vue.js 3 (Composition API + TailwindCSS) + MySQL 8.x + Docker + Arduino/ESP32 (Wokwi ou Físico)

---

## ⛏️ 1. Sobre o Projeto

As vias e frentes de lavra em operações de mineração apresentam desafios extremos: extensas áreas geográficas, estradas não pavimentadas com traçado dinâmico, alta poeira em suspensão e períodos críticos de neblina/cerração densa.

Este sistema foi desenvolvido para realizar a **gestão centralizada e o monitoramento em tempo real de torres de iluminação móveis e autônomas**, viabilizando:
1. **Cadastro e Monitoramento Georreferenciado:** Localização por mapa de satélite, indicativos de cores semafóricas (Verde, Amarelo, Vermelho, Cinza), carga de bateria e nível de combustível.
2. **Controle Operacional Remoto e Comandos em Lote:** Acionamento individual ou em lote para setores inteiros da mina (ex: ativar **Modo Cerração** para toda uma estrada de escoamento simultaneamente).
3. **Gestão de Manutenções com Horímetro Preventivo:** Registro de panes em campo, abertura de Ordens de Serviço (OS) e cálculo contínuo de horas de operação com disparo automático de alerta de revisão preventiva.
4. **Dashboard Executivo de Sustentabilidade:** Monitoramento do consumo elétrico (kWh), estimativa de litros de diesel economizados e toneladas de CO₂ não emitidas.
5. **Extensão IoT (Arduino/ESP32):** Automação em tempo real através de sensor de luminosidade LDR (< 300) e botão manual de cerração, enviando telemetria HTTP REST para a API e acionando o LED RGB da luminária.
6. **Segurança e Auditoria (RBAC & JWT):** Perfis `ADMIN` e `OPERADOR`, senhas criptografadas com BCrypt (fator 10) e rastreamento rigoroso de IP e data/hora em cada operação.
7. **Aplicativo do Operador com Operação Offline:** Fila local com sincronização automática assim que o dispositivo restabelece sinal de rede celular/Wi-Fi.

---

## 🚀 2. Como Executar o Projeto

Você pode iniciar o projeto de duas maneiras:

### Opção A: Execução com Docker Compose (1 Único Comando - RNF-C03)
Certifique-se de que o Docker esteja instalado e em execução:
```bash
# Na raiz do projeto:
docker compose up -d --build
```
Após a inicialização:
- **Frontend Web & App Operador:** [http://localhost:80](http://localhost:80) ou [http://localhost:3000](http://localhost:3000)
- **Backend Spring Boot API:** [http://localhost:8080](http://localhost:8080)
- **Documentação Swagger / OpenAPI 3:** [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **Banco de Dados MySQL 8.x:** Porta `3306` (Database: `iluminacao_minas`)

---

### Opção B: Execução Local Direta (Windows / Sem Docker)
O backend possui um perfil inteligente `dev` com banco H2 integrado em arquivo persistente, permitindo rodar tudo imediatamente sem depender de container:

#### 1. Iniciar o Backend (Spring Boot 3 / Java 17):
```bash
cd backend
mvn spring-boot:run
```
*(Ou execute o atalho `start-local.bat` na raiz).*

#### 2. Iniciar o Frontend (Vue 3 / Vite):
```bash
cd frontend
npm install
npm run dev
```
Acesse no seu navegador: **[http://localhost:5173](http://localhost:5173)**.

---

## 🔑 3. Credenciais de Acesso Pré-Cadastradas

| Perfil | E-mail | Senha | Nível de Acesso |
| :--- | :--- | :--- | :--- |
| **Gestor CCO (`ADMIN`)** | `admin@mineracao.com.br` | `admin123` | Acesso total: Dashboard executivo, Mapa, Cadastro de Torres, Comandos em Lote, Auditoria e Manutenções |
| **Operador de Campo (`OPERADOR`)** | `operador@mineracao.com.br` | `operador123` | Interface ágil simplificada para smartphone/tablet, acionamento rápido, modo offline e reporte de falhas |

*(A tela de login possui botões de 1 clique para preenchimento imediato das credenciais).*

---

## 💡 4. Extensão IoT e Simuladores (Arduino / ESP32)

O subsistema IoT atende integralmente ao **RF06**, **RNF-C10** e ao **Item 9 (Adicional de Nota)** da especificação SENAI:

### 1. Bancada de Testes Virtual Integrada na Web:
- Acesse o sistema e clique em **"Simulador IoT (ESP32)"** no menu lateral.
- Ajuste o slider do **Sensor LDR** (movendo para baixo de 300 para simular cerração/escuridão) ou pressione o **Botão Cerração**.
- Acompanhe a requisição HTTP POST e veja a luminária virtual (LED RGB) acender imediatamente na cor âmbar de alta visibilidade!

### 2. Simulador Standalone via Linha de Comando (Node.js):
Para gerar telemetria simultânea contínua de várias torres:
```bash
node iot-simulator/simulator_runner.js
```

### 3. Código Firmware Pronto para ESP32 Físico ou Wokwi:
- Arquivo de código: `iot-simulator/arduino_esp32_firmware.ino`
- Esquemático Wokwi: `iot-simulator/wokwi_diagram.json`
- Manual de pinagem: `iot-simulator/README.md`

---

## 📚 5. Documentação Técnica Exigida pelo SENAI

A pasta [`docs/`](file:///c:/Users/lucas/Downloads/MelhorPerfilDETODOS/docs/) contém todos os documentos formais exigidos no Item 5 da especificação:
1. **[1_Documento_de_Requisitos.md](file:///c:/Users/lucas/Downloads/MelhorPerfilDETODOS/docs/1_Documento_de_Requisitos.md):** 13 Requisitos Funcionais (RF01-RF13) e 52 Requisitos Não Funcionais (RNF-C, RNF-W, RNF-A).
2. **[2_Casos_de_Uso.md](file:///c:/Users/lucas/Downloads/MelhorPerfilDETODOS/docs/2_Casos_de_Uso.md):** Descrição textual de todos os fluxos principais e alternativos.
3. **[3_DER_Modelagem_Banco_Dados.md](file:///c:/Users/lucas/Downloads/MelhorPerfilDETODOS/docs/3_DER_Modelagem_Banco_Dados.md):** Modelagem relacional DER em Mermaid e script SQL DDL completo.
4. **[4_Diagramas_de_Sequencia.md](file:///c:/Users/lucas/Downloads/MelhorPerfilDETODOS/docs/4_Diagramas_de_Sequencia.md):** Fluxo de comando web, automação por sensor LDR e sincronização offline.
5. **[5_Diagrama_de_Classes.md](file:///c:/Users/lucas/Downloads/MelhorPerfilDETODOS/docs/5_Diagrama_de_Classes.md):** Visão estrutural das principais classes de backend.

---

## 🌿 6. Versionamento Git e Branches (Item 4 SENAI)

O projeto está organizado no repositório Git com as branches exigidas:
- `main`: Versão estável e oficial do projeto.
- `develop`: Branch de integração da equipe.
- `feature/backend-api`: Desenvolvimento da API Spring Boot 3, entidades, segurança e persistência.
- `feature/frontend-vue`: Desenvolvimento da SPA em Vue 3, Pinia, Tailwind e rotas.
- `feature/iot-simulator`: Simulador de telemetria, firmware Arduino C++ e esquemático Wokwi.
- `test/system-validation`: Suíte de testes unitários com JUnit 5 e Mockito.

---

## 🧪 7. Execução dos Testes Automatizados (RNF-C12)

Para rodar a suíte de testes de unidade e integração:
```bash
cd backend
mvn test
```
Resultados: **8 testes executados com 100% de sucesso (0 falhas, 0 erros)**.
