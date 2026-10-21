# Documento de Requisitos Consolidados
**Projeto:** Sistema de Gestão e Monitoramento de Iluminação Inteligente para Minas  
**Instituição:** SENAI Betim (CETEM Maria Madalena Nogueira)  
**Área:** Tecnologia da Informação  
**Stack Tecnológica:** Java 17 (Spring Boot 3.x) + Vue.js 3 + MySQL 8.x + Docker + Arduino Mega 2560 físico com ponte USB/HTTP

---

## 1. Requisitos Funcionais (RF)

| ID | Requisito | Descrição Detalhada | Atores | Prioridade |
| :--- | :--- | :--- | :--- | :--- |
| **RF01** | **Cadastro de Torres de Iluminação** | O sistema deve permitir o cadastro de novas torres de iluminação móveis no banco de dados. Cada registro deve obrigatoriamente conter: número de identificação único (ID/Código, ex: `TR-001`), localização atual (latitude/longitude e setor/trecho da mina) e tipo de fonte de energia (Painel Solar, Gerador a Diesel, Bateria ou Híbrido). | Administrador (CCO) | Alta |
| **RF02** | **Monitoramento das Torres em Tempo Real** | O sistema deve exibir em tempo real o estado de cada torre no painel principal e no mapa. Deve ser exibido se está ligada ou desligada, quantidade de carga de bateria (%), nível de combustível, se está em manutenção ou se atingiu o horímetro preventivo, com indicativos de cores claras: **Verde** (Operacional), **Amarelo** (Alerta / Cerração), **Vermelho** (Manutenção / Falha) e **Cinza** (Desligada). | Gestores e Operadores | Alta |
| **RF03** | **Controle das Torres** | O sistema deve conseguir interagir diretamente com as torres. Deve ser possível ligar e desligar as torres remotamente diretamente pelo sistema, além de ativar diferentes modos de energia: **Modo Normal** (100% intensidade), **Modo Economia** (50% intensidade) e **Modo Alta Visibilidade para Cerração** (150% foco para neblina e poeira). | Gestores e Operadores | Alta |
| **RF04** | **Gestão de Manutenções das Torres** | O sistema deve permitir o registro detalhado de panes reportadas em campo, agendar ordens de serviço (OS) e atribuí-las automaticamente aos técnicos responsáveis. Deve calcular e consolidar as horas de uso contínuo (horímetro) emitindo alertas automáticos aos técnicos nas horas programadas para manutenção preventiva. | Gestores, Técnicos e Operadores | Alta |
| **RF05** | **Dashboard de Indicadores** | O sistema deve possuir um dashboard executivo exibindo a quantidade de torres em funcionamento versus total, resumo do consumo energético global acumulado (kWh), estimativa de litros de diesel economizados, redução de emissão de CO₂ evitada e gráficos de eficiência por setor e fonte energética. | Administrador (CCO) | Alta |
| **RF06** | **Transmissão e Leitura de Dados IoT (Arduino Mega)** | O sistema deve integrar-se ao circuito demonstrativo por uma ponte USB/HTTP, receber telemetria JSON com leitura do LDR (< 300) ou botão manual e responder com a instrução para os LEDs indicadores do circuito. | Arduino Mega, ponte local, Backend | Alta |
| **RF07** | **Autenticação de Usuários (Login)** | O sistema deve possuir tela de login para restringir o acesso a usuários autorizados via e-mail e senha criptografada, gerando um token JWT seguro com 24 horas de expiração. | Todos os Usuários | Alta |
| **RF08** | **Visualização da Frota em Mapa ou Lista** | O sistema deve apresentar a distribuição geográfica das torres ativas no campo em um mapa interativo com imagens de satélite da mina ou em lista tabular detalhada com filtros por setor, trecho e fonte energética. | Administrador e Operador | Média |
| **RF09** | **Ações e Comandos em Lote (Áreas Remotas)** | O sistema deve permitir o agrupamento de torres por setor para comandos operacionais simultâneos. O operador deve conseguir selecionar um trecho inteiro (ex: "Estrada de Escoamento - Trecho 04") e alterar o modo de todas as torres de uma só vez (ex: ativar modo cerração para toda a via). | Administrador (CCO) | Média |
| **RF10** | **Consolidação do Tempo de Atividade (Histórico)** | O sistema deve registrar no banco de dados a data/hora exata de acionamento e desligamento de cada torre, calculando o tempo de uso contínuo e usando essa métrica para calcular o consumo energético real e a economia gerada. | Sistema (Backend) | Alta |
| **RF11** | **Autenticação e Controle de Acesso (RBAC)** | O sistema deve restringir funcionalidades com base no perfil de acesso: Perfil `ADMIN` possui acesso total (dashboard executivo, mapa, gestão de usuários, cadastro de torres e auditoria). Perfil `OPERADOR` possui visão focada com acionamento rápido, visualização de status e reporte de falhas. | Sistema (Segurança) | Alta |
| **RF12** | **Registro de Auditoria / Histórico** | O sistema deve salvar automaticamente um registro de auditoria para cada ação operacional e de gestão (ligar, desligar, alterar modo, abrir chamado, cadastrar ou excluir), gravando usuário, timestamp, detalhes da ação e endereço IP de origem. | Sistema (Segurança) | Alta |
| **RF13** | **Sincronização e Operação Offline** | O aplicativo móvel do operador deve permitir o acionamento de comandos básicos mesmo sem conexão com a internet (áreas de sombra na mina). Os comandos emitidos offline devem ser salvos na fila local do dispositivo e sincronizados automaticamente com o backend assim que o sinal de rede for restabelecido. | Operador de Campo | Média |

---

## 2. Requisitos Não Funcionais (RNF)

### 2.1 RNF Comuns (Web + App)
- **RNF-C01 [Backend]:** API REST desenvolvida em Spring Boot 3.x com Java 17+.
- **RNF-C02 [Banco de Dados]:** Banco relacional MySQL 8.x com volume persistente no Docker (suporte a perfil H2 para desenvolvimento instantâneo).
- **RNF-C03 [DevOps]:** Backend + MySQL sobem com um único comando via `docker-compose.yml`.
- **RNF-C04 [Git]:** Versionamento de código padronizado com branches `main`, `develop`, `feature/*` e `test/*`.
- **RNF-C05 [Segurança]:** Todas as comunicações via HTTPS em produção com CORS configurado.
- **RNF-C06 [Segurança]:** Controle de acesso baseado em papéis (RBAC com perfis `ADMIN` e `OPERADOR`).
- **RNF-C07 [Desempenho]:** Respostas das requisições de API em menos de 2 segundos sob condições normais.
- **RNF-C08 [Confiabilidade]:** Logs estruturados via SLF4J + Logback para auditoria e debug.
- **RNF-C09 [Confiabilidade]:** Rotina de backup do banco de dados estruturada.
- **RNF-C10 [Escalabilidade]:** Endpoints de API preparados para suportar telemetria contínua de dispositivos IoT.
- **RNF-C11 [Documentação]:** Documentação viva e interativa dos endpoints através de Swagger / OpenAPI 3.
- **RNF-C12 [Testes]:** Cobertura de testes unitários e de integração utilizando JUnit 5 e Mockito.

### 2.2 RNF Sistema Web (Gestores CCO)
- **RNF-W01 [Tecnologia]:** SPA desenvolvida em Vue.js 3 com Composition API.
- **RNF-W02 [Integração]:** Comunicação REST via Axios consumindo dados em JSON com interceptors.
- **RNF-W03 [Segurança]:** Acesso restrito a gestores no painel administrativo via guards de rota no frontend e RBAC no backend.
- **RNF-W04 [Segurança]:** Autenticação JWT com expiração de 24 horas armazenado no `localStorage`.
- **RNF-W05 [Segurança]:** Senhas com hash criptográfico BCrypt (fator de custo 10) e tamanho mínimo de 8 caracteres.
- **RNF-W06 [Segurança]:** Validação rigorosa de entradas de formulários e proteção contra ataques.
- **RNF-W07 [Usabilidade]:** Layout responsivo adaptável para monitores desktop (1920px), notebooks (1366px) e tablets (768px).
- **RNF-W08 [Usabilidade]:** Menu lateral fixo, breadcrumbs de navegação e máximo de 3 cliques para qualquer função.
- **RNF-W09 [Usabilidade]:** Feedback visual imediato com loaders, alertas e confirmações para ações críticas.
- **RNF-W10 [Desempenho]:** Carregamento de páginas em menos de 2 segundos.
- **RNF-W11 [Desempenho]:** Atualização do painel em tempo real via polling automático a cada 10 segundos.
- **RNF-W12 [Confiabilidade]:** Disponibilidade contínua 24/7 com auditoria das ações operacionais.
- **RNF-W13 [Confiabilidade]:** Mensagens amigáveis para o usuário em caso de indisponibilidade da API.
- **RNF-W14 [Portabilidade]:** Aplicação empacotada em container Docker com Nginx.
- **RNF-W15 [Portabilidade]:** Compatibilidade total com navegadores Chrome, Firefox e Edge.
- **RNF-W16 [Manutenibilidade]:** Código modular estruturado em componentes, services e Pinia stores.
- **RNF-W17 [Manutenibilidade]:** Versionamento de novas telas e melhorias em branches do Git.
- **RNF-W18 [Auditoria]:** Toda alteração de torre, modo ou manutenção salva usuário, timestamp e IP.
- **RNF-W19 [Escalabilidade]:** Suporte simultâneo a mais de 20 gestores sem perda de performance.
- **RNF-W20 [Acessibilidade]:** Contraste elevado no tema industrial escuro, ícones semafóricos e tipografia legível.

### 2.3 RNF Aplicativo Operador de Campo
- **RNF-A01 [Tecnologia]:** Interface minimalista e ágil desenvolvida como Progressive Web App (PWA) responsiva.
- **RNF-A02 [Integração]:** Consumo da mesma API REST do sistema web via HTTPS/JSON.
- **RNF-A03 [Armazenamento]:** Armazenamento em cache local da lista de torres para operação sem internet.
- **RNF-A04 [Segurança]:** Autenticação JWT consistente com expiração de 24h.
- **RNF-A05 [Permissões]:** Perfil `OPERADOR` restrito à operação das torres sem acesso a métricas sensíveis.
- **RNF-A06 [Segurança]:** Armazenamento seguro de credenciais e tokens.
- **RNF-A07 [Segurança]:** Logout automático por inatividade após 15 minutos sem uso.
- **RNF-A08 [Usabilidade]:** Qualquer acionamento efetuado em até 3 toques na tela.
- **RNF-A09 [Usabilidade]:** Elementos visuais grandes, alto contraste e botões de toque fácil com luvas.
- **RNF-A10 [Usabilidade]:** Feedback imediato tátil (vibração via Web Vibration API) e visual ao acionar uma torre.
- **RNF-A11 [Usabilidade]:** Modo offline parcial exibindo a última lista carregada sem sinal de rede.
- **RNF-A12 [Desempenho]:** Carregamento de telas em < 1.5s e resposta de acionamento em < 1s.
- **RNF-A13 [Desempenho]:** Baixo consumo de dados na rede móvel da mina (polling otimizado).
- **RNF-A14 [Confiabilidade]:** Sincronização automática em segundo plano ao detectar reconexão com a rede.
- **RNF-A15 [Confiabilidade]:** Mensagens informativas claras sobre status de conexão e tentativas automáticas.
- **RNF-A16 [Portabilidade]:** Compatibilidade com Android 8+ e iOS 12+ através de navegadores móveis modernos.
- **RNF-A17 [Portabilidade]:** Adaptação dinâmica para smartphones e tablets industriais reforçados.
- **RNF-A18 [Manutenibilidade]:** Arquitetura desacoplada e modular.
- **RNF-A19 [Manutenibilidade]:** Gestão de branches de código no Git.
- **RNF-A20 [Bateria]:** Otimização para baixo consumo energético de bateria durante o turno de trabalho.
