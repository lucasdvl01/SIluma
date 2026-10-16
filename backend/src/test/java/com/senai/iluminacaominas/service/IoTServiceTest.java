package com.senai.iluminacaominas.service;

import com.senai.iluminacaominas.dto.IoTTelemetryDTOs.*;
import com.senai.iluminacaominas.entity.*;
import com.senai.iluminacaominas.repository.IoTTelemetryRepository;
import com.senai.iluminacaominas.repository.LightingTowerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IoTServiceTest {

    @Mock
    private LightingTowerRepository towerRepository;

    @Mock
    private IoTTelemetryRepository telemetryRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private IoTService ioTService;

    private LightingTower mockTower;

    @BeforeEach
    void setUp() {
        mockTower = LightingTower.builder()
                .id(1L)
                .codigoIdentificacao("TR-001")
                .status(TowerStatus.OPERACIONAL)
                .ligada(true)
                .modoOperacao(LightingMode.NORMAL)
                .nivelBateria(85.0)
                .build();
    }

    @Test
    @DisplayName("Sensor LDR detecta queda de luminosidade (< 300) -> Dispara Alerta de Baixa Visibilidade e comanda LED no máximo (RF06)")
    void testQuedaLuminosidadeLDR() {
        when(towerRepository.findByCodigoIdentificacao("TR-001")).thenReturn(Optional.of(mockTower));

        // Simula LDR lendo 150 (escuridão/cerração intensa na mina)
        TelemetryRequestDTO telemetry = TelemetryRequestDTO.builder()
                .codigoTorre("TR-001")
                .luminosidadeLdr(150)
                .statusLed(false)
                .nivelBateria(80.0)
                .pressaoBotaoCerracao(false)
                .build();

        TelemetryResponseDTO response = ioTService.processTelemetry(telemetry, "192.168.1.50");

        assertTrue(response.isAlertaAtivado());
        assertTrue(response.isInstrucaoLed());
        assertEquals(255, response.getIntensidadePwm());
        assertEquals("ALTA_VISIBILIDADE_CERRACAO", response.getModoLuz());
        assertEquals(TowerStatus.ALERTA_BAIXA_VISIBILIDADE, mockTower.getStatus());
        assertTrue(mockTower.isAlertaBaixaVisibilidade());

        verify(telemetryRepository, times(1)).save(any(IoTTelemetry.class));
        verify(towerRepository, times(1)).save(mockTower);
    }

    @Test
    @DisplayName("Pressionar botão de cerração no Arduino ativa alerta e resposta de instrução do LED (RF06)")
    void testBotaoCerracaoArduino() {
        when(towerRepository.findByCodigoIdentificacao("TR-001")).thenReturn(Optional.of(mockTower));

        TelemetryRequestDTO telemetry = TelemetryRequestDTO.builder()
                .codigoTorre("TR-001")
                .luminosidadeLdr(800) // Luz normal, mas apertou botão de teste de cerração
                .statusLed(true)
                .nivelBateria(75.0)
                .pressaoBotaoCerracao(true)
                .build();

        TelemetryResponseDTO response = ioTService.processTelemetry(telemetry, "192.168.1.50");

        assertTrue(response.isAlertaAtivado());
        assertTrue(response.isInstrucaoLed());
        assertEquals(255, response.getIntensidadePwm());
        assertEquals(TowerStatus.ALERTA_BAIXA_VISIBILIDADE, mockTower.getStatus());
    }

    @Test
    @DisplayName("Polling do hardware retorna instrução atual do sistema (RF06)")
    void testHardwarePolling() {
        mockTower.setLigada(true);
        mockTower.setModoOperacao(LightingMode.ALTA_VISIBILIDADE_CERRACAO);
        when(towerRepository.findByCodigoIdentificacao("TR-001")).thenReturn(Optional.of(mockTower));

        HardwarePollingDTO poll = ioTService.getHardwarePolling("TR-001");

        assertTrue(poll.isLigada());
        assertTrue(poll.isModoCerracao());
        assertEquals("AMBAR", poll.getCorLedSugerida());
        assertEquals(255, poll.getIntensidadePwm());
    }
}
