package com.senai.iluminacaominas.service;

import com.senai.iluminacaominas.dto.MaintenanceDTOs.*;
import com.senai.iluminacaominas.entity.*;
import com.senai.iluminacaominas.repository.LightingTowerRepository;
import com.senai.iluminacaominas.repository.MaintenanceOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MaintenanceService {

    private final MaintenanceOrderRepository maintenanceRepository;
    private final LightingTowerRepository towerRepository;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public List<MaintenanceResponseDTO> getAll(MaintenanceStatus status) {
        List<MaintenanceOrder> list;
        if (status != null) {
            list = maintenanceRepository.findByStatusOrderByDataAberturaDesc(status);
        } else {
            list = maintenanceRepository.findAllByOrderByDataAberturaDesc();
        }
        return list.stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public MaintenanceResponseDTO getById(Long id) {
        MaintenanceOrder order = maintenanceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ordem de manutenção não encontrada com ID: " + id));
        return toDTO(order);
    }

    @Transactional
    public MaintenanceResponseDTO createMaintenance(MaintenanceCreateDTO dto, String userEmail, String ip) {
        LightingTower tower = towerRepository.findById(dto.getTorreId())
                .orElseThrow(() -> new IllegalArgumentException("Torre não encontrada com ID: " + dto.getTorreId()));

        // Altera status da torre para EM_MANUTENCAO (RF04)
        tower.setStatus(TowerStatus.EM_MANUTENCAO);
        towerRepository.save(tower);

        MaintenanceOrder order = MaintenanceOrder.builder()
                .torre(tower)
                .codigoTorre(tower.getCodigoIdentificacao())
                .setorNome(tower.getSetor().getNome())
                .tipoFalha(dto.getTipoFalha())
                .gravidade(dto.getGravidade() != null ? dto.getGravidade() : MaintenanceSeverity.MEDIA)
                .descricao(dto.getDescricao())
                .tecnicoResponsavel(dto.getTecnicoResponsavel())
                .status(MaintenanceStatus.PENDENTE)
                .dataAbertura(LocalDateTime.now())
                .horasTorreNaAbertura(tower.getHorasAtividadeAcumuladas())
                .usuarioAbertura(userEmail)
                .build();

        order = maintenanceRepository.save(order);

        auditService.registrarLog(
                userEmail,
                "ABRIR_MANUTENCAO",
                "MaintenanceOrder",
                order.getId().toString(),
                "OS aberta para torre " + tower.getCodigoIdentificacao() + " - Falha: " + order.getTipoFalha()
                        + " - Técnico: " + order.getTecnicoResponsavel()
                        + " - Horímetro: " + String.format("%.2f", tower.getHorasAtividadeAcumuladas()) + "h",
                ip
        );

        return toDTO(order);
    }

    @Transactional
    public MaintenanceResponseDTO updateMaintenance(Long id, MaintenanceUpdateDTO dto, String userEmail, String ip) {
        MaintenanceOrder order = maintenanceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ordem de manutenção não encontrada com ID: " + id));

        order.setStatus(dto.getStatus());
        if (dto.getObservacoesResolucao() != null) {
            order.setObservacoesResolucao(dto.getObservacoesResolucao());
        }

        LightingTower tower = order.getTorre();

        if (dto.getStatus() == MaintenanceStatus.CONCLUIDA) {
            order.setDataConclusao(LocalDateTime.now());
            // Libera a torre para operacional
            tower.setStatus(TowerStatus.OPERACIONAL);
            // Se foi manutenção preventiva por horas de uso, podemos redefinir novo ciclo preventivo
            if (order.getTipoFalha().toLowerCase().contains("preventiva")) {
                tower.setLimiteHorasPreventiva(tower.getHorasAtividadeAcumuladas() + 250.0);
            }
            towerRepository.save(tower);
        } else if (dto.getStatus() == MaintenanceStatus.CANCELADA) {
            tower.setStatus(TowerStatus.OPERACIONAL);
            towerRepository.save(tower);
        }

        order = maintenanceRepository.save(order);

        auditService.registrarLog(
                userEmail,
                "ATUALIZAR_MANUTENCAO",
                "MaintenanceOrder",
                order.getId().toString(),
                "OS atualizada para status: " + order.getStatus() + " - Torre " + tower.getCodigoIdentificacao(),
                ip
        );

        return toDTO(order);
    }

    @Transactional
    public void deleteMaintenance(Long id) {
        maintenanceRepository.deleteById(id);
    }

    private MaintenanceResponseDTO toDTO(MaintenanceOrder order) {
        return MaintenanceResponseDTO.builder()
                .id(order.getId())
                .torreId(order.getTorre().getId())
                .codigoTorre(order.getCodigoTorre())
                .setorNome(order.getSetorNome())
                .tipoFalha(order.getTipoFalha())
                .gravidade(order.getGravidade())
                .descricao(order.getDescricao())
                .tecnicoResponsavel(order.getTecnicoResponsavel())
                .status(order.getStatus())
                .statusLabel(order.getStatus().getLabel())
                .dataAbertura(order.getDataAbertura())
                .dataConclusao(order.getDataConclusao())
                .horasTorreNaAbertura(order.getHorasTorreNaAbertura())
                .observacoesResolucao(order.getObservacoesResolucao())
                .usuarioAbertura(order.getUsuarioAbertura())
                .build();
    }
}
