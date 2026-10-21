package com.senai.iluminacaominas.service;

import com.senai.iluminacaominas.dto.IoTTelemetryDTOs.*;
import com.senai.iluminacaominas.entity.*;
import com.senai.iluminacaominas.repository.IoTTelemetryRepository;
import com.senai.iluminacaominas.repository.LightingTowerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class IoTService {

    private final LightingTowerRepository towerRepository;
    private final IoTTelemetryRepository telemetryRepository;
    private final AuditService auditService;

    // Limiar analógico do LDR: < 300 significa baixa luz / cerração intensa na mina (escuridão)
    private static final int LIMIAR_LDR_BAIXA_VISIBILIDADE = 300;

    @Transactional
    public TelemetryResponseDTO processTelemetry(TelemetryRequestDTO request, String ip) {
        String codigo = request.getCodigoTorre().toUpperCase().trim();
        Optional<LightingTower> towerOpt = towerRepository.findByCodigoIdentificacao(codigo);

        boolean alertaVisibilidade = false;
        boolean instrucaoLed = false;
        int intensidadePwm = 0;
        String modoLuz = "NORMAL";
        String mensagem = "Telemetria processada com sucesso.";

        // Verifica condições do sensor LDR ou botão de cerração (RF06)
        boolean quedaLuminosidade = request.getLuminosidadeLdr() != null && request.getLuminosidadeLdr() < LIMIAR_LDR_BAIXA_VISIBILIDADE;
        boolean botaoCerracao = Boolean.TRUE.equals(request.getPressaoBotaoCerracao());

        if (quedaLuminosidade || botaoCerracao) {
            alertaVisibilidade = true;
            instrucaoLed = true;
            intensidadePwm = 255;
            modoLuz = LightingMode.ALTA_VISIBILIDADE_CERRACAO.name();
            mensagem = "Alerta de Baixa Visibilidade! Instrução IoT: Ligar LED em potência máxima (Cerração).";
        }

        if (towerOpt.isPresent()) {
            LightingTower tower = towerOpt.get();
            tower.setUltimoContatoIoT(LocalDateTime.now());

            if (request.getNivelBateria() != null) {
                tower.setNivelBateria(request.getNivelBateria());
            }

            if (tower.getStatus() == TowerStatus.FALHA_TECNICA
                    || tower.getStatus() == TowerStatus.EM_MANUTENCAO) {
                modoLuz = tower.getModoOperacao().name();
            } else if (alertaVisibilidade) {
                tower.setStatus(TowerStatus.ALERTA_BAIXA_VISIBILIDADE);
                tower.setAlertaBaixaVisibilidade(true);
                tower.setModoOperacao(LightingMode.ALTA_VISIBILIDADE_CERRACAO);
                instrucaoLed = tower.isLigada();
                intensidadePwm = tower.isLigada() ? 255 : 0;

                auditService.registrarLog(
                        "DISPOSITIVO_IOT",
                        "ALERTA_IOT_LDR",
                        "LightingTower",
                        tower.getId().toString(),
                        "LDR: " + request.getLuminosidadeLdr() + " - Torre " + codigo + " alterada para Alerta de Baixa Visibilidade",
                        ip
                );
            } else if (tower.getStatus() == TowerStatus.ALERTA_BAIXA_VISIBILIDADE) {
                // Clareou o ambiente: restaura status normal
                tower.setStatus(tower.isLigada() ? TowerStatus.OPERACIONAL : TowerStatus.DESLIGADA);
                tower.setAlertaBaixaVisibilidade(false);
                tower.setModoOperacao(LightingMode.NORMAL);
                modoLuz = LightingMode.NORMAL.name();
                instrucaoLed = tower.isLigada();
            } else {
                instrucaoLed = tower.isLigada();
                modoLuz = tower.getModoOperacao().name();
                intensidadePwm = tower.isLigada() ? (tower.getModoOperacao() == LightingMode.ECONOMIA ? 100 : 200) : 0;
            }

            towerRepository.save(tower);
        }

        String corLedSugerida = towerOpt.map(this::getSuggestedLedColor).orElse("APAGADO");
        instrucaoLed = !"APAGADO".equals(corLedSugerida);
        intensidadePwm = getSuggestedLedIntensity(corLedSugerida);

        // Grava histórico de telemetria IoT no banco
        IoTTelemetry telemetry = IoTTelemetry.builder()
                .codigoTorre(codigo)
                .luminosidadeLdr(request.getLuminosidadeLdr())
                .statusLed(instrucaoLed)
                .nivelBateria(request.getNivelBateria() != null ? request.getNivelBateria() : 100.0)
                .pressaoBotaoCerracao(botaoCerracao)
                .dataHora(LocalDateTime.now())
                .alertaGerado(alertaVisibilidade)
                .comandoResposta(mensagem)
                .build();
        telemetryRepository.save(telemetry);

        return TelemetryResponseDTO.builder()
                .codigoTorre(codigo)
                .instrucaoLed(instrucaoLed)
                .modoLuz(modoLuz)
                .intensidadePwm(intensidadePwm)
                .corLedSugerida(corLedSugerida)
                .alertaAtivado(alertaVisibilidade)
                .mensagem(mensagem)
                .horarioServidor(LocalDateTime.now())
                .build();
    }

    @Transactional(readOnly = true)
    public HardwarePollingDTO getHardwarePolling(String codigoTorre) {
        String codigo = codigoTorre.toUpperCase().trim();
        Optional<LightingTower> towerOpt = towerRepository.findByCodigoIdentificacao(codigo);

        if (towerOpt.isEmpty()) {
            return HardwarePollingDTO.builder()
                    .codigoTorre(codigo)
                    .ligada(false)
                    .modo("DESCONHECIDO")
                    .intensidadePwm(0)
                    .modoCerracao(false)
                    .corLedSugerida("APAGADO")
                    .build();
        }

        LightingTower tower = towerOpt.get();
        boolean ligada = tower.isLigada();
        boolean cerracao = tower.getModoOperacao() == LightingMode.ALTA_VISIBILIDADE_CERRACAO
                || tower.getStatus() == TowerStatus.ALERTA_BAIXA_VISIBILIDADE;

        String cor = getSuggestedLedColor(tower);
        int pwm = getSuggestedLedIntensity(cor);

        return HardwarePollingDTO.builder()
                .codigoTorre(codigo)
                .ligada(ligada)
                .modo(tower.getModoOperacao().name())
                .intensidadePwm(pwm)
                .modoCerracao(cerracao)
                .corLedSugerida(cor)
                .build();
    }

    private String getSuggestedLedColor(LightingTower tower) {
        if (tower.getStatus() == TowerStatus.FALHA_TECNICA
                || tower.getStatus() == TowerStatus.EM_MANUTENCAO) {
            return "VERMELHO";
        }
        if (!tower.isLigada()) {
            return "APAGADO";
        }
        if (tower.getStatus() == TowerStatus.ALERTA_BAIXA_VISIBILIDADE
                || tower.getModoOperacao() == LightingMode.ALTA_VISIBILIDADE_CERRACAO) {
            return "AMBAR";
        }
        if (tower.getModoOperacao() == LightingMode.ECONOMIA) {
            return "AZUL";
        }
        return "VERDE";
    }

    private int getSuggestedLedIntensity(String color) {
        return switch (color) {
            case "VERMELHO", "AMBAR" -> 255;
            case "AZUL" -> 100;
            case "VERDE" -> 200;
            default -> 0;
        };
    }
}
