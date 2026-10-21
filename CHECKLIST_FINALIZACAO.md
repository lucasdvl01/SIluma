# Checklist de finalização do projeto

Este documento registra o estado conhecido do projeto em **21/10/2026** e organiza o trabalho restante para concluir, testar e entregar o Sistema de Gestão e Monitoramento de Iluminação Inteligente para Minas.

> **Importante:** “build passou” e “testes passaram” não significam que todos os requisitos foram certificados. A montagem física com Arduino Mega e a integração serial com o backend foram demonstradas; o teste do botão, a revisão visual final do mapa e outros requisitos ainda precisam ser confirmados.

## Como retomar

1. Abra este arquivo e continue pelo próximo item pendente.
2. Use a branch `feature/frontend-vue` enquanto termina o circuito e as correções atuais.
3. Não apague `backend/h2_data` nem o volume Docker `minas_mysql_volume` sem fazer backup e confirmar que os dados podem ser removidos.
4. Antes de começar outra tarefa, confira `git status` para preservar alterações locais.

## Estado atual confirmado

- [x] Repositório GitHub: `https://github.com/lucasdvl01/SIluma`.
- [x] Branches publicadas: `main`, `develop`, `feature/backend-api`, `feature/frontend-vue`, `feature/iot-simulator` e `test/system-validation`.
- [x] O commit `1af6591` corrige a limpeza do build antes da inicialização local; `a3370cb` corrige o H2 e compacta os cartões de manutenção.
- [x] Os 8 testes existentes do backend passaram em uma execução limpa (`mvnw clean test`).
- [x] O build de produção do frontend passou (`npm run build`).
- [x] Melhorias locais de UX/UI aplicadas: navegação móvel acessível, estados de carregamento/falha, estimativas ambientais identificadas, atualizações periódicas no dashboard, telas carregadas sob demanda, desconexão simulada distinguida da rede real e redesign visual consistente de navegação, cabeçalhos, painéis, formulários, tabelas e páginas de operação.
- [x] O usuário validou visualmente as alterações de UX/UI; identificou como pendência final o painel de torre cortado no mapa.
- [x] Corrigido o layout do Mapa para dar mais altura à área geográfica e manter o painel da torre fora do recorte do canvas; painel limitado à área disponível e rolável em viewports menores.
- [ ] Conferir visualmente o painel selecionado no mapa em desktop e celular após a correção. A tentativa desta sessão não conectou ao navegador integrado; o build passou, mas a apresentação visual final ainda precisa de confirmação.
- [x] Backend local validado com login ADMIN e consultas autenticadas somente de leitura via IPv4: torres (8), setores (4), estatísticas do dashboard, manutenções (4) e auditoria (50). OpenAPI respondeu HTTP 200. Nenhuma operação de escrita foi feita.
- [x] Em 21/10/2026, Arduino Mega 2560 em COM6 foi compilado e gravado; o LDR variou de ~830 (claro) para ~22 (coberto), a ponte serial enviou telemetria de `TR-001` ao backend, o alerta foi confirmado e o usuário observou o LED laranja no escuro e verde ao clarear. O backend registrou evento `ALERTA_IOT_LDR`; nível de bateria continua simulado.
- [x] O botão físico de cerração também foi testado pelo usuário: LED laranja pressionado e recuperação para verde após soltar.
- [x] Corrigida a comunicação serial de retorno: a ponte envia ao Mega somente os booleanos `alertaAtivado` e `instrucaoLed`; sketch recompilado/gravado e o usuário confirmou que o teste voltou a funcionar. O firmware também registra o comando recebido para diagnóstico.
- [ ] O smoke test visual anterior usou respostas HTTP simuladas; a validação direta do backend não substitui percorrer os fluxos da interface ligados à API real. Repetir o smoke test integrado com backend antes da entrega.
- [x] O terminal de operador foi ajustado para acompanhar o tema claro; o build do frontend passou após essa alteração.
- [x] Redesign e documentação revisados, commitados e publicados em `feature/frontend-vue` no commit `909d035`.
- [x] Criado `docs/GUIA_MONTAGEM_CIRCUITO_ESP32_SI_LUMA.md` com materiais, pinagem, ligações, segurança e roteiro de testes do protótipo físico; o README do simulador IoT aponta para o guia.
- [x] Docker Compose foi validado e os serviços MySQL 8, backend Spring Boot e frontend Nginx foram construídos e iniciados.
- [x] No teste Docker, MySQL ficou saudável, o backend conectou ao MySQL, criou as tabelas e carregou os dados iniciais. Foram observados 2 usuários, 4 setores, 7 torres, 2 ordens de manutenção e nenhuma telemetria.
- [x] Login Admin e Operador, endpoints principais, proxy do frontend, mapa, manutenção, dashboard e polling IoT foram exercitados no ambiente Docker.
- [x] O MySQL foi reiniciado durante a validação; os dados permaneceram no volume e o backend voltou a responder.
- [x] Não foram encontradas referências de torre/setor inválidas nem ordens sem torre no banco Docker testado.
- [x] Os containers do teste foram encerrados com `docker compose down`; o volume `minas_mysql_volume` foi preservado. O banco Docker não é o mesmo que o H2 local.
- [x] O H2 local iniciou e carregou o contexto de Spring durante os testes de backend. A falha posterior de inicialização foi identificada como uma classe compilada inválida em `backend/target`; limpar e reconstruir o backend resolveu o erro.
- [x] `start-local.bat` executa a limpeza de build antes da inicialização do backend; essa alteração já está no commit publicado `1af6591`.
- [ ] O PDF `proposta 1 web + arduino.docx.pdf` aparece como arquivo não rastreado no diretório. Ele foi compartilhado para consulta, não deve ser incluído por acidente nos commits.
- [ ] Não há Pull Request aberto de `feature/frontend-vue` para `develop`.
- [x] O protótipo físico Arduino Mega + LDR + LEDs foi testado com o backend pela ponte USB/HTTP `iot-simulator/serial_http_bridge.ps1`.

## Fase 1 — Consolidar as correções locais e o Git

- [ ] Na branch `feature/frontend-vue`, revisar e testar o `start-local.bat` atualizado:
  - [ ] Executar o arquivo e confirmar que o backend inicia na porta 8080 antes do frontend.
  - [ ] Confirmar que o script não anuncia sucesso nem inicia o frontend se a API não ficar disponível no tempo limite.
  - [ ] Confirmar que a limpeza de `backend/target` não apaga `backend/h2_data`.
- [x] Rodar testes do backend e build do frontend após as alterações: 8 testes passaram usando H2 em memória para não disputar o arquivo H2 da instância local ativa; `npm run build` passou. Repetir a execução limpa antes da entrega final, quando não houver backend local usando o H2.
- [x] Conferir `git status` e `git diff --check`; os PDFs, os arquivos `.traineddata` e o SVG original foram mantidos fora do commit.
- [x] Commitar apenas os arquivos pretendidos e enviar para `origin/feature/frontend-vue` (`909d035`).
- [ ] Abrir um Pull Request de `feature/frontend-vue` para `develop` e registrar os resultados. Não há PR aberto; a criação deve ser concluída em [comparar branches no GitHub](https://github.com/lucasdvl01/SIluma/compare/develop...feature/frontend-vue?expand=1).
- [ ] Depois da integração em `develop` e da validação final, abrir um Pull Request de `develop` para `main`.
- [ ] Manter `main` como versão estável; configurar proteção da branch para exigir Pull Request/revisão, se disponível no GitHub.

Comandos úteis para revisar os arquivos staged antes do commit:

```powershell
git status --short --branch
git diff --check
git diff
git diff --cached
```

Adicione arquivos explicitamente com `git add -- caminho\do\arquivo`; evite `git add .` enquanto o PDF estiver não rastreado.

## Fase 2 — Validar o protótipo e decidir sobre ESP32

A montagem já realizada usa Arduino Mega 2560 sem Wi-Fi e comunica-se com o backend pela ponte USB/HTTP. LDR e botão foram testados ponta a ponta; alerta acende o LED laranja e a recuperação retorna ao verde. A ponte ocupa a porta COM enquanto está ativa; o Monitor Serial deve permanecer fechado.

Um ESP32 é uma expansão opcional se o objetivo passar a ser enviar dados por Wi-Fi sem depender do computador. O firmware está em `iot-simulator/arduino_esp32_firmware.ino`; a pinagem e os testes estão em `iot-simulator/README.md` e no [Guia de montagem IoT SI Luma](docs/GUIA_MONTAGEM_CIRCUITO_ESP32_SI_LUMA.md).

- [ ] Separar e confirmar os componentes do guia: ESP32 DevKit V1, cabo USB de dados, protoboard, jumpers, módulo LDR analógico (ou LDR + resistor de 10 kΩ), botão normalmente aberto, LED RGB de cátodo comum e três resistores de 220 Ω.
- [ ] Montar primeiro um circuito de **baixa tensão**, com alimentação e ligação dos GPIOs conferidas no datasheet dos componentes. Não ligar GPIO do ESP32 diretamente à rede elétrica nem exceder 3,3 V nas entradas.
- [ ] Conferir a pinagem proposta e validar a orientação do divisor de tensão caso use LDR avulso:
  - LDR/saída analógica: GPIO 34 (ADC1).
  - Botão de cerração: GPIO 14, com `INPUT_PULLUP`.
  - LED RGB de cátodo comum: GPIOs 25, 26 e 27, cada canal em série com resistor de 220 Ω; verificar a ordem física dos terminais do LED.
- [ ] Configurar no firmware o SSID/senha reais e o endereço IP da máquina que executa o backend. O endereço atual `10.0.2.2` é apenas um valor voltado a certos ambientes de simulação; não presumir que seja o IP correto da rede física.
- [ ] Confirmar que computador e ESP32 estão na mesma rede e que o firewall permite acesso ao backend na porta 8080.
- [ ] Verificar a versão do core ESP32 e da biblioteca ArduinoJson na IDE escolhida. O firmware usa chamadas PWM `ledcSetup`/`ledcAttachPin`; confirmar se a versão instalada as suporta ou adaptar à API dessa versão antes de gravar a placa.
- [ ] Compilar e gravar o firmware; registrar placa, versões, pinagem final e configurações necessárias sem publicar credenciais reais de Wi-Fi.
- [ ] Testar sequencialmente:
  - [ ] Inicialização e conexão Wi-Fi.
  - [ ] Leitura do LDR em condição clara e escura; confirmar limiar `< 300`.
  - [ ] Acionamento do botão e debounce.
  - [ ] POST para `/api/iot/telemetry` e resposta do backend.
  - [ ] Polling de `/api/iot/command/{codigoTorre}`.
  - [ ] Cores/intensidade do LED para modo normal, cerração e desligado.
  - [ ] Comportamento sem Wi-Fi (fallback local) e reconexão.
  - [ ] Atualização coerente do status da torre no sistema web.
- [ ] Observar que o nível de bateria do firmware é simulado (não há sensor no circuito atual) e que o firmware lê `intensidadePwm` da resposta, mas ainda não aplica proporcionalmente esse valor aos canais do LED.
- [ ] Guardar evidências da demonstração (fotos do circuito, saída Serial Monitor e telas/eventos do sistema).
- [ ] Manter o simulador Web, Wokwi e runner durante a fase de montagem; remover somente depois da validação e se realmente não forem mais necessários.

## Fase 3 — Validar os requisitos e fechar lacunas

Use `docs/1_Documento_de_Requisitos.md` (RF01–RF13 e RNFs), `docs/2_Casos_de_Uso.md` e a proposta PDF como referência. Para cada requisito, anotar **evidência**: teste automatizado, execução observada, captura de tela ou demonstração.

- [ ] Percorrer os fluxos com perfil ADMIN e OPERADOR, inclusive permissões de tela e de API.
- [ ] Validar criação/edição/exclusão de torres, comandos individuais e em lote, ordens de manutenção, horímetro, alertas e auditoria.
- [ ] Validar operação offline do terminal do operador: cache, fila de comandos, reconexão e sincronização. A tela existir não comprova a sincronização completa.
- [ ] Validar polling de 10 segundos e experiência em resoluções desktop, tablet e celular.
- [ ] Testar APIs de sucesso, entrada inválida, ausência de autenticação e papel sem permissão. Foi observado `403` em casos de login inválido/acesso anônimo; avaliar a resposta HTTP apropriada (`401` para não autenticado e `403` para autenticado sem permissão) e corrigir/testar se necessário.
- [ ] Exercitar os fluxos mutáveis de manutenção e IoT em um banco de teste, verificar efeitos no banco/auditoria e restaurar dados de demonstração depois. Não usar o banco de demonstração sem backup.
- [ ] Verificar feedback e tratamento de erro quando backend ou internet ficam indisponíveis.
- [ ] Verificar Swagger/OpenAPI, rotas Vue diretas via Nginx, logs, validações e comportamento de reinício dos serviços.
- [ ] Executar a suíte de testes e o build a partir de uma cópia limpa da branch que será entregue.
- [ ] Rever documentação e remover afirmações como “100% funcional” se não houver evidência para cada requisito.

## Fase 4 — Entrega reproduzível

- [ ] Testar o fluxo local em uma cópia nova do repositório: Java 17, Node.js/npm, instalação de dependências frontend e `start-local.bat`.
- [ ] Testar Docker em uma máquina/cópia limpa: `docker compose up --build -d`, aguardar MySQL saudável e confirmar login/API/frontend.
- [ ] Conferir persistência depois de reiniciar containers e documentar backup/restauração do MySQL.
- [ ] Atualizar README/manual com requisitos de instalação, comandos exatos, URLs, credenciais demonstrativas, configuração de Wi-Fi/firmware e limitações.
- [ ] Confirmar que segredos reais (Wi-Fi, tokens, senhas pessoais) não estão versionados; manter apenas credenciais claramente identificadas como demonstração.
- [ ] Fazer revisão final do Pull Request e mesclar primeiro em `develop`, depois em `main`.
- [ ] Criar uma tag/versão de entrega e registrar evidências/testes executados.

## Avisos já observados na validação

- Docker Compose avisa que a chave superior `version` está obsoleta; não impediu a execução, mas pode ser removida em uma alteração futura.
- O build Vite passou, mas emite um aviso de bundle JavaScript acima de 500 kB.
- Os testes existentes cobrem o carregamento do contexto e alguns serviços (IoT e torres); não são uma certificação automatizada de todos os RF/RNFs.
- Dados e estado do H2 local (`backend/h2_data`) e do volume MySQL Docker (`minas_mysql_volume`) são independentes.
