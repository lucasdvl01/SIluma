package com.senai.iluminacaominas.service;

import com.senai.iluminacaominas.dto.TowerDTOs.*;
import com.senai.iluminacaominas.entity.*;
import com.senai.iluminacaominas.repository.LightingTowerRepository;
import com.senai.iluminacaominas.repository.TowerActivityLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TowerServiceTest {

    @Mock
    private LightingTowerRepository towerRepository;

    @Mock
    private TowerActivityLogRepository activityLogRepository;

    @Mock
    private SectorService sectorService;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private TowerService towerService;

    private Sector mockSector;
    private LightingTower mockTower;

    @BeforeEach
    void setUp() {
        mockSector = Sector.builder()
                .id(1L)
                .codigo("SET-01")
                .nome("Frente de Lavra Norte")
                .build();

        mockTower = LightingTower.builder()
                .id(1L)
                .codigoIdentificacao("TR-001")
                .setor(mockSector)
                .latitude(-19.9725)
                .longitude(-44.1982)
                .fonteEnergia(PowerSource.PAINEL_SOLAR)
                .status(TowerStatus.OPERACIONAL)
                .ligada(true)
                .modoOperacao(LightingMode.NORMAL)
                .nivelBateria(90.0)
                .horasAtividadeAcumuladas(50.0)
                .limiteHorasPreventiva(250.0)
                .build();
    }

    @Test
    @DisplayName("Deve ligar torre desligada e criar registro de atividade (RF03, RF10)")
    void testLigarTorre() {
        mockTower.setLigada(false);
        mockTower.setStatus(TowerStatus.DESLIGADA);

        when(towerRepository.findById(1L)).thenReturn(Optional.of(mockTower));
        when(towerRepository.save(any(LightingTower.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TowerActionDTO action = new TowerActionDTO("LIGAR", null);
        TowerResponseDTO response = towerService.executeAction(1L, action, "admin@teste.com", "127.0.0.1");

        assertTrue(response.isLigada());
        assertEquals(TowerStatus.OPERACIONAL, response.getStatus());
        verify(activityLogRepository, times(1)).save(any(TowerActivityLog.class));
        verify(auditService, times(1)).registrarLog(eq("admin@teste.com"), eq("LIGAR_TORRE"), any(), any(), any(), eq("127.0.0.1"));
    }

    @Test
    @DisplayName("Deve desligar torre e fechar log de atividade consolidando horas (RF03, RF10)")
    void testDesligarTorre() {
        mockTower.setLigada(true);

        TowerActivityLog openLog = TowerActivityLog.builder()
                .id(10L)
                .torre(mockTower)
                .codigoTorre("TR-001")
                .dataHoraLigou(LocalDateTime.now().minusHours(2))
                .modo(LightingMode.NORMAL)
                .build();

        when(towerRepository.findById(1L)).thenReturn(Optional.of(mockTower));
        when(activityLogRepository.findTopByTorreIdAndDataHoraDesligouIsNullOrderByDataHoraLigouDesc(1L))
                .thenReturn(Optional.of(openLog));
        when(towerRepository.save(any(LightingTower.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TowerActionDTO action = new TowerActionDTO("DESLIGAR", null);
        TowerResponseDTO response = towerService.executeAction(1L, action, "admin@teste.com", "127.0.0.1");

        assertFalse(response.isLigada());
        assertEquals(TowerStatus.DESLIGADA, response.getStatus());
        assertNotNull(openLog.getDataHoraDesligou());
        assertTrue(openLog.getConsumoEstimadoKWh() > 0);
        assertTrue(response.getHorasAtividadeAcumuladas() > 50.0);
    }

    @Test
    @DisplayName("Deve alterar modo de operação para Alta Visibilidade / Cerração (RF03)")
    void testAlterarModoCerracao() {
        when(towerRepository.findById(1L)).thenReturn(Optional.of(mockTower));
        when(towerRepository.save(any(LightingTower.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TowerActionDTO action = new TowerActionDTO("ALTERAR_MODO", LightingMode.ALTA_VISIBILIDADE_CERRACAO);
        TowerResponseDTO response = towerService.executeAction(1L, action, "operador@teste.com", "127.0.0.1");

        assertEquals(LightingMode.ALTA_VISIBILIDADE_CERRACAO, response.getModoOperacao());
        assertEquals(TowerStatus.ALERTA_BAIXA_VISIBILIDADE, response.getStatus());
        assertTrue(response.isAlertaBaixaVisibilidade());
    }

    @Test
    @DisplayName("Deve executar comando em lote para todas as torres de um setor (RF09)")
    void testComandoEmLoteSetor() {
        LightingTower tower2 = LightingTower.builder()
                .id(2L)
                .codigoIdentificacao("TR-002")
                .setor(mockSector)
                .latitude(-19.9740)
                .longitude(-44.1950)
                .fonteEnergia(PowerSource.GERADOR_DIESEL)
                .status(TowerStatus.OPERACIONAL)
                .ligada(true)
                .modoOperacao(LightingMode.NORMAL)
                .horasAtividadeAcumuladas(20.0)
                .limiteHorasPreventiva(250.0)
                .build();

        when(sectorService.getSectorEntityById(1L)).thenReturn(mockSector);
        when(towerRepository.findBySetorId(1L)).thenReturn(List.of(mockTower, tower2));
        when(towerRepository.save(any(LightingTower.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BatchActionDTO batch = new BatchActionDTO(1L, "ALTERAR_MODO", LightingMode.ALTA_VISIBILIDADE_CERRACAO);
        List<TowerResponseDTO> result = towerService.executeBatchAction(batch, "admin@teste.com", "127.0.0.1");

        assertEquals(2, result.size());
        assertEquals(LightingMode.ALTA_VISIBILIDADE_CERRACAO, result.get(0).getModoOperacao());
        assertEquals(LightingMode.ALTA_VISIBILIDADE_CERRACAO, result.get(1).getModoOperacao());
        verify(auditService).registrarLog(eq("admin@teste.com"), eq("COMANDO_LOTE"), eq("Sector"), eq("1"), any(), eq("127.0.0.1"));
    }
}
