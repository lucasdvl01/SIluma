package com.senai.iluminacaominas.seeder;

import com.senai.iluminacaominas.entity.*;
import com.senai.iluminacaominas.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final SectorRepository sectorRepository;
    private final LightingTowerRepository towerRepository;
    private final MaintenanceOrderRepository maintenanceRepository;
    private final TowerActivityLogRepository activityLogRepository;
    private final AuditLogRepository auditLogRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Banco de dados já contém registros. Pulando seeder.");
            return;
        }

        log.info("Iniciando carga de dados padrão para mineração...");

        // 1. Usuários Padrão (ADMIN e OPERADOR - RF07, RF11)
        User admin = User.builder()
                .nome("Lucas Gestor (CCO)")
                .email("admin@mineracao.com.br")
                .senha(passwordEncoder.encode("admin123"))
                .perfil(Role.ADMIN)
                .ativo(true)
                .build();

        User operador = User.builder()
                .nome("Operador de Campo")
                .email("operador@mineracao.com.br")
                .senha(passwordEncoder.encode("operador123"))
                .perfil(Role.OPERADOR)
                .ativo(true)
                .build();

        userRepository.saveAll(List.of(admin, operador));
        log.info("Usuários criados com sucesso: admin@mineracao.com.br e operador@mineracao.com.br");

        // 2. Setores da Mina (RF01, RF08, RF09)
        Sector s1 = Sector.builder()
                .codigo("SET-01")
                .nome("Frente de Lavra Norte")
                .areaMina("Cava Principal")
                .frentesDeLavra("Frente 01, Frente 02")
                .descricao("Área de extração contínua de minério com escavadeiras de grande porte")
                .build();

        Sector s2 = Sector.builder()
                .codigo("SET-02")
                .nome("Estrada de Escoamento - Trecho 04")
                .areaMina("Via de Acesso Principal")
                .frentesDeLavra("Km 12 ao 18")
                .descricao("Trecho sinuoso em aclive sujeito a neblina intensa e poeira suspensa")
                .build();

        Sector s3 = Sector.builder()
                .codigo("SET-03")
                .nome("Britador Primário e Pátio")
                .areaMina("Plataforma de Moagem")
                .frentesDeLavra("Moega 01 e Pátio 03")
                .descricao("Região de descarga de caminhões fora-de-estrada e britagem")
                .build();

        Sector s4 = Sector.builder()
                .codigo("SET-04")
                .nome("Pátio de Estéril e Rejeito")
                .areaMina("Bacia de Disposição")
                .frentesDeLavra("Bancada Sul B")
                .descricao("Bancada de depósito de estéril e drenagem controlada")
                .build();

        sectorRepository.saveAll(List.of(s1, s2, s3, s4));

        // Coordenadas simuladas em região mineradora real (Betim / Quadrilátero Ferrífero - MG)
        // Lat: -19.97xxx, Lon: -44.19xxx
        LocalDateTime now = LocalDateTime.now();

        // 3. Torres de Iluminação (RF01, RF02, RF03)
        LightingTower t1 = LightingTower.builder()
                .codigoIdentificacao("TR-001")
                .setor(s1)
                .latitude(-19.9725)
                .longitude(-44.1982)
                .fonteEnergia(PowerSource.PAINEL_SOLAR)
                .status(TowerStatus.OPERACIONAL)
                .ligada(true)
                .modoOperacao(LightingMode.NORMAL)
                .nivelBateria(94.0)
                .horasAtividadeAcumuladas(142.5)
                .limiteHorasPreventiva(250.0)
                .alertaBaixaVisibilidade(false)
                .ultimoContatoIoT(now.minusSeconds(45))
                .dataUltimaAtivacao(now.minusHours(4))
                .build();

        LightingTower t2 = LightingTower.builder()
                .codigoIdentificacao("TR-002")
                .setor(s1)
                .latitude(-19.9741)
                .longitude(-44.1955)
                .fonteEnergia(PowerSource.BATERIA)
                .status(TowerStatus.ALERTA_BAIXA_VISIBILIDADE)
                .ligada(true)
                .modoOperacao(LightingMode.ALTA_VISIBILIDADE_CERRACAO)
                .nivelBateria(62.0)
                .horasAtividadeAcumuladas(189.0)
                .limiteHorasPreventiva(250.0)
                .alertaBaixaVisibilidade(true)
                .ultimoContatoIoT(now.minusSeconds(12))
                .dataUltimaAtivacao(now.minusHours(6))
                .build();

        LightingTower t3 = LightingTower.builder()
                .codigoIdentificacao("TR-003")
                .setor(s2)
                .latitude(-19.9780)
                .longitude(-44.1930)
                .fonteEnergia(PowerSource.HIBRIDO)
                .status(TowerStatus.OPERACIONAL)
                .ligada(true)
                .modoOperacao(LightingMode.ECONOMIA)
                .nivelBateria(78.0)
                .nivelCombustivel(85.0)
                .horasAtividadeAcumuladas(210.0)
                .limiteHorasPreventiva(250.0)
                .alertaBaixaVisibilidade(false)
                .ultimoContatoIoT(now.minusMinutes(2))
                .dataUltimaAtivacao(now.minusHours(8))
                .build();

        LightingTower t4 = LightingTower.builder()
                .codigoIdentificacao("TR-004")
                .setor(s2)
                .latitude(-19.9795)
                .longitude(-44.1915)
                .fonteEnergia(PowerSource.GERADOR_DIESEL)
                .status(TowerStatus.DESLIGADA)
                .ligada(false)
                .modoOperacao(LightingMode.NORMAL)
                .nivelBateria(88.0)
                .nivelCombustivel(52.0)
                .horasAtividadeAcumuladas(248.5)
                .limiteHorasPreventiva(250.0)
                .alertaBaixaVisibilidade(false)
                .dataUltimaAtivacao(now.minusDays(1))
                .build();

        LightingTower t5 = LightingTower.builder()
                .codigoIdentificacao("TR-005")
                .setor(s3)
                .latitude(-19.9705)
                .longitude(-44.1890)
                .fonteEnergia(PowerSource.PAINEL_SOLAR)
                .status(TowerStatus.OPERACIONAL)
                .ligada(true)
                .modoOperacao(LightingMode.NORMAL)
                .nivelBateria(97.0)
                .horasAtividadeAcumuladas(88.0)
                .limiteHorasPreventiva(250.0)
                .alertaBaixaVisibilidade(false)
                .ultimoContatoIoT(now.minusSeconds(20))
                .dataUltimaAtivacao(now.minusHours(3))
                .build();

        LightingTower t6 = LightingTower.builder()
                .codigoIdentificacao("TR-006")
                .setor(s3)
                .latitude(-19.9690)
                .longitude(-44.1870)
                .fonteEnergia(PowerSource.HIBRIDO)
                .status(TowerStatus.EM_MANUTENCAO)
                .ligada(false)
                .modoOperacao(LightingMode.NORMAL)
                .nivelBateria(32.0)
                .nivelCombustivel(20.0)
                .horasAtividadeAcumuladas(255.0) // Superou limite preventiva (250h)
                .limiteHorasPreventiva(250.0)
                .alertaBaixaVisibilidade(false)
                .dataUltimaAtivacao(now.minusDays(2))
                .build();

        LightingTower t7 = LightingTower.builder()
                .codigoIdentificacao("TR-007")
                .setor(s4)
                .latitude(-19.9830)
                .longitude(-44.2010)
                .fonteEnergia(PowerSource.PAINEL_SOLAR)
                .status(TowerStatus.OPERACIONAL)
                .ligada(true)
                .modoOperacao(LightingMode.NORMAL)
                .nivelBateria(82.0)
                .horasAtividadeAcumuladas(120.0)
                .limiteHorasPreventiva(250.0)
                .alertaBaixaVisibilidade(false)
                .ultimoContatoIoT(now.minusSeconds(10))
                .dataUltimaAtivacao(now.minusHours(5))
                .build();

        towerRepository.saveAll(List.of(t1, t2, t3, t4, t5, t6, t7));

        // 4. Ordens de Manutenção (RF04)
        MaintenanceOrder m1 = MaintenanceOrder.builder()
                .torre(t6)
                .codigoTorre(t6.getCodigoIdentificacao())
                .setorNome(s3.getNome())
                .tipoFalha("Revisão Preventiva de 250 Horas (Horímetro)")
                .gravidade(MaintenanceSeverity.ALTA)
                .descricao("Torre atingiu 255 horas de operação contínua. Necessária revisão do gerador auxiliar e teste das células de bateria.")
                .tecnicoResponsavel("Eng. Carlos Eduardo")
                .status(MaintenanceStatus.EM_ANDAMENTO)
                .dataAbertura(now.minusHours(12))
                .horasTorreNaAbertura(255.0)
                .usuarioAbertura(admin.getEmail())
                .build();

        MaintenanceOrder m2 = MaintenanceOrder.builder()
                .torre(t1)
                .codigoTorre(t1.getCodigoIdentificacao())
                .setorNome(s1.getNome())
                .tipoFalha("Limpeza e Calibração dos Sensores")
                .gravidade(MaintenanceSeverity.BAIXA)
                .descricao("Limpeza dos módulos fotovoltaicos e calibração periódica do sensor LDR de luminosidade.")
                .tecnicoResponsavel("Mariana Costa")
                .status(MaintenanceStatus.CONCLUIDA)
                .dataAbertura(now.minusDays(5))
                .dataConclusao(now.minusDays(4))
                .horasTorreNaAbertura(110.0)
                .observacoesResolucao("Painéis higienizados e sensor LDR calibrado com sucesso.")
                .usuarioAbertura(operador.getEmail())
                .build();

        maintenanceRepository.saveAll(List.of(m1, m2));

        // 5. Histórico de Atividade (RF10)
        TowerActivityLog a1 = TowerActivityLog.builder()
                .torre(t1)
                .codigoTorre("TR-001")
                .dataHoraLigou(now.minusHours(4))
                .dataHoraDesligou(null)
                .minutosAtivo(240L)
                .consumoEstimadoKWh(4.8)
                .modo(LightingMode.NORMAL)
                .usuarioAcionamento(admin.getEmail())
                .ipAcionamento("192.168.1.15")
                .build();

        TowerActivityLog a2 = TowerActivityLog.builder()
                .torre(t2)
                .codigoTorre("TR-002")
                .dataHoraLigou(now.minusHours(6))
                .dataHoraDesligou(null)
                .minutosAtivo(360L)
                .consumoEstimadoKWh(10.8) // Fator 1.5x devido ao modo cerração
                .modo(LightingMode.ALTA_VISIBILIDADE_CERRACAO)
                .usuarioAcionamento("IOT_AUTOMATION")
                .ipAcionamento("192.168.1.50")
                .build();

        activityLogRepository.saveAll(List.of(a1, a2));

        // 6. Logs de Auditoria Iniciais (RF12)
        AuditLog aud1 = AuditLog.builder()
                .usuarioEmail(admin.getEmail())
                .acao("INICIALIZACAO_SISTEMA")
                .entidade("System")
                .entidadeId("1")
                .detalhes("Carga inicial de dados da mina executada com sucesso")
                .ipOrigem("127.0.0.1")
                .dataHora(now.minusMinutes(30))
                .build();

        AuditLog aud2 = AuditLog.builder()
                .usuarioEmail("DISPOSITIVO_ESP32")
                .acao("ALERTA_IOT_LDR")
                .entidade("LightingTower")
                .entidadeId(t2.getId().toString())
                .detalhes("Queda de luminosidade detectada pelo sensor LDR (leitura: 120). Modo cerração ativado.")
                .ipOrigem("192.168.1.50")
                .dataHora(now.minusMinutes(15))
                .build();

        auditLogRepository.saveAll(List.of(aud1, aud2));

        log.info("Carga inicial do sistema de iluminação de minas concluída com êxito!");
    }
}
