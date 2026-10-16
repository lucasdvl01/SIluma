package com.senai.iluminacaominas.service;

import com.senai.iluminacaominas.dto.TowerDTOs.*;
import com.senai.iluminacaominas.entity.*;
import com.senai.iluminacaominas.repository.LightingTowerRepository;
import com.senai.iluminacaominas.repository.TowerActivityLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TowerService {

    private final LightingTowerRepository towerRepository;
    private final TowerActivityLogRepository activityLogRepository;
    private final SectorService sectorService;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public List<TowerResponseDTO> getAllTowers() {
        return towerRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TowerResponseDTO getTowerById(Long id) {
        LightingTower tower = findEntityById(id);
        return toDTO(tower);
    }

    @Transactional(readOnly = true)
    public LightingTower findEntityById(Long id) {
        return towerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Torre de iluminação não encontrada com ID: " + id));
    }

    @Transactional(readOnly = true)
    public LightingTower findEntityByCodigo(String codigo) {
        return towerRepository.findByCodigoIdentificacao(codigo)
                .orElseThrow(() -> new IllegalArgumentException("Torre não encontrada com código: " + codigo));
    }

    @Transactional
    public TowerResponseDTO createTower(TowerCreateDTO dto, String userEmail, String ip) {
        if (towerRepository.existsByCodigoIdentificacao(dto.getCodigoIdentificacao())) {
            throw new IllegalArgumentException("Já existe uma torre com o código: " + dto.getCodigoIdentificacao());
        }

        Sector sector = sectorService.getSectorEntityById(dto.getSetorId());

        LightingTower tower = LightingTower.builder()
                .codigoIdentificacao(dto.getCodigoIdentificacao().toUpperCase().trim())
                .setor(sector)
                .latitude(dto.getLatitude())
                .longitude(dto.getLongitude())
                .fonteEnergia(dto.getFonteEnergia())
                .status(TowerStatus.OPERACIONAL)
                .ligada(true)
                .modoOperacao(LightingMode.NORMAL)
                .nivelBateria(dto.getNivelBateria() != null ? dto.getNivelBateria() : 100.0)
                .nivelCombustivel(dto.getNivelCombustivel())
                .horasAtividadeAcumuladas(0.0)
                .limiteHorasPreventiva(dto.getLimiteHorasPreventiva() != null ? dto.getLimiteHorasPreventiva() : 250.0)
                .alertaBaixaVisibilidade(false)
                .dataUltimaAtivacao(LocalDateTime.now())
                .build();

        tower = towerRepository.save(tower);

        // Abre histórico de atividade inicial
        TowerActivityLog activityLog = TowerActivityLog.builder()
                .torre(tower)
                .codigoTorre(tower.getCodigoIdentificacao())
                .dataHoraLigou(LocalDateTime.now())
                .modo(tower.getModoOperacao())
                .usuarioAcionamento(userEmail)
                .ipAcionamento(ip)
                .build();
        activityLogRepository.save(activityLog);

        auditService.registrarLog(
                userEmail,
                "CADASTRAR_TORRE",
                "LightingTower",
                tower.getId().toString(),
                "Torre criada: " + tower.getCodigoIdentificacao() + " no setor " + sector.getNome() + " com fonte " + tower.getFonteEnergia(),
                ip
        );

        return toDTO(tower);
    }

    @Transactional
    public TowerResponseDTO updateTower(Long id, TowerUpdateDTO dto, String userEmail, String ip) {
        LightingTower tower = findEntityById(id);

        if (dto.getSetorId() != null) {
            Sector sector = sectorService.getSectorEntityById(dto.getSetorId());
            tower.setSetor(sector);
        }
        if (dto.getLatitude() != null) tower.setLatitude(dto.getLatitude());
        if (dto.getLongitude() != null) tower.setLongitude(dto.getLongitude());
        if (dto.getFonteEnergia() != null) tower.setFonteEnergia(dto.getFonteEnergia());
        if (dto.getLimiteHorasPreventiva() != null) tower.setLimiteHorasPreventiva(dto.getLimiteHorasPreventiva());

        tower = towerRepository.save(tower);

        auditService.registrarLog(
                userEmail,
                "ATUALIZAR_TORRE",
                "LightingTower",
                tower.getId().toString(),
                "Dados da torre " + tower.getCodigoIdentificacao() + " atualizados",
                ip
        );

        return toDTO(tower);
    }

    @Transactional
    public void deleteTower(Long id, String userEmail, String ip) {
        LightingTower tower = findEntityById(id);
        String codigo = tower.getCodigoIdentificacao();
        towerRepository.delete(tower);

        auditService.registrarLog(
                userEmail,
                "EXCLUIR_TORRE",
                "LightingTower",
                id.toString(),
                "Torre excluída: " + codigo,
                ip
        );
    }

    @Transactional
    public TowerResponseDTO executeAction(Long id, TowerActionDTO actionDTO, String userEmail, String ip) {
        LightingTower tower = findEntityById(id);
        String acao = actionDTO.getAcao().toUpperCase().trim();

        switch (acao) {
            case "LIGAR":
                ligarTorre(tower, userEmail, ip);
                break;
            case "DESLIGAR":
                desligarTorre(tower, userEmail, ip);
                break;
            case "ALTERAR_MODO":
            case "MODO":
                if (actionDTO.getModo() != null) {
                    alterarModoTorre(tower, actionDTO.getModo(), userEmail, ip);
                }
                break;
            case "MODO_CERRACAO":
                alterarModoTorre(tower, LightingMode.ALTA_VISIBILIDADE_CERRACAO, userEmail, ip);
                break;
            default:
                throw new IllegalArgumentException("Ação não reconhecida: " + acao);
        }

        tower = towerRepository.save(tower);
        return toDTO(tower);
    }

    private void ligarTorre(LightingTower tower, String userEmail, String ip) {
        if (!tower.isLigada()) {
            tower.setLigada(true);
            tower.setDataUltimaAtivacao(LocalDateTime.now());
            if (tower.getStatus() == TowerStatus.DESLIGADA) {
                tower.setStatus(TowerStatus.OPERACIONAL);
            }

            TowerActivityLog activityLog = TowerActivityLog.builder()
                    .torre(tower)
                    .codigoTorre(tower.getCodigoIdentificacao())
                    .dataHoraLigou(LocalDateTime.now())
                    .modo(tower.getModoOperacao())
                    .usuarioAcionamento(userEmail)
                    .ipAcionamento(ip)
                    .build();
            activityLogRepository.save(activityLog);

            auditService.registrarLog(
                    userEmail,
                    "LIGAR_TORRE",
                    "LightingTower",
                    tower.getId().toString(),
                    "Torre " + tower.getCodigoIdentificacao() + " acionada (LIGADA)",
                    ip
            );
        }
    }

    private void desligarTorre(LightingTower tower, String userEmail, String ip) {
        if (tower.isLigada()) {
            tower.setLigada(false);
            if (tower.getStatus() == TowerStatus.OPERACIONAL || tower.getStatus() == TowerStatus.ALERTA_BAIXA_VISIBILIDADE) {
                tower.setStatus(TowerStatus.DESLIGADA);
            }

            // Fecha log de atividade aberto e consolida horas (RF10)
            Optional<TowerActivityLog> openLogOpt = activityLogRepository
                    .findTopByTorreIdAndDataHoraDesligouIsNullOrderByDataHoraLigouDesc(tower.getId());

            LocalDateTime now = LocalDateTime.now();
            long minutos = 15; // padrão mínimo de amostragem
            if (openLogOpt.isPresent()) {
                TowerActivityLog openLog = openLogOpt.get();
                openLog.setDataHoraDesligou(now);
                Duration dur = Duration.between(openLog.getDataHoraLigou(), now);
                minutos = Math.max(1, dur.toMinutes());
                openLog.setMinutosAtivo(minutos);

                // Cálculo de consumo: lâmpadas LED alta potência em torre móvel consomem aprox. 1.2 kW na média
                double horas = minutos / 60.0;
                double fatorModo = openLog.getModo() != null ? openLog.getModo().getFatorConsumo() : 1.0;
                double consumoKWh = horas * 1.2 * fatorModo;
                openLog.setConsumoEstimadoKWh(consumoKWh);
                activityLogRepository.save(openLog);

                // Incrementa horímetro acumulado para preventiva (RF04, RF10)
                tower.setHorasAtividadeAcumuladas(tower.getHorasAtividadeAcumuladas() + horas);
            }

            auditService.registrarLog(
                    userEmail,
                    "DESLIGAR_TORRE",
                    "LightingTower",
                    tower.getId().toString(),
                    "Torre " + tower.getCodigoIdentificacao() + " desligada. Horas acumuladas: "
                            + String.format("%.2f", tower.getHorasAtividadeAcumuladas()) + "h",
                    ip
            );
        }
    }

    private void alterarModoTorre(LightingTower tower, LightingMode novoModo, String userEmail, String ip) {
        LightingMode antigo = tower.getModoOperacao();
        tower.setModoOperacao(novoModo);
        if (!tower.isLigada()) {
            tower.setLigada(true);
            tower.setDataUltimaAtivacao(LocalDateTime.now());
        }

        if (novoModo == LightingMode.ALTA_VISIBILIDADE_CERRACAO) {
            tower.setStatus(TowerStatus.ALERTA_BAIXA_VISIBILIDADE);
            tower.setAlertaBaixaVisibilidade(true);
        } else if (tower.getStatus() == TowerStatus.ALERTA_BAIXA_VISIBILIDADE) {
            tower.setStatus(TowerStatus.OPERACIONAL);
            tower.setAlertaBaixaVisibilidade(false);
        }

        auditService.registrarLog(
                userEmail,
                "ALTERAR_MODO",
                "LightingTower",
                tower.getId().toString(),
                "Torre " + tower.getCodigoIdentificacao() + " modo alterado de " + antigo + " para " + novoModo,
                ip
        );
    }

    // RF09: Ações e Comandos em Lote por Setor
    @Transactional
    public List<TowerResponseDTO> executeBatchAction(BatchActionDTO batchDTO, String userEmail, String ip) {
        Sector sector = sectorService.getSectorEntityById(batchDTO.getSetorId());
        List<LightingTower> towers = towerRepository.findBySetorId(sector.getId());

        String acao = batchDTO.getAcao().toUpperCase().trim();
        for (LightingTower tower : towers) {
            if (tower.getStatus() == TowerStatus.EM_MANUTENCAO) {
                continue; // Não altera torres que estejam sob intervenção de manutenção
            }
            switch (acao) {
                case "LIGAR_TODAS":
                case "LIGAR":
                    ligarTorre(tower, userEmail, ip);
                    break;
                case "DESLIGAR_TODAS":
                case "DESLIGAR":
                    desligarTorre(tower, userEmail, ip);
                    break;
                case "ALTERAR_MODO":
                    if (batchDTO.getModo() != null) {
                        alterarModoTorre(tower, batchDTO.getModo(), userEmail, ip);
                    }
                    break;
                case "MODO_CERRACAO":
                    alterarModoTorre(tower, LightingMode.ALTA_VISIBILIDADE_CERRACAO, userEmail, ip);
                    break;
            }
            towerRepository.save(tower);
        }

        auditService.registrarLog(
                userEmail,
                "COMANDO_LOTE",
                "Sector",
                sector.getId().toString(),
                "Comando em lote '" + acao + "' executado para " + towers.size() + " torres do setor: " + sector.getNome(),
                ip
        );

        return towers.stream().map(this::toDTO).collect(Collectors.toList());
    }

    public TowerResponseDTO toDTO(LightingTower tower) {
        boolean preventiva = tower.getHorasAtividadeAcumuladas() != null 
                && tower.getLimiteHorasPreventiva() != null
                && tower.getHorasAtividadeAcumuladas() >= tower.getLimiteHorasPreventiva();

        return TowerResponseDTO.builder()
                .id(tower.getId())
                .codigoIdentificacao(tower.getCodigoIdentificacao())
                .setorId(tower.getSetor() != null ? tower.getSetor().getId() : null)
                .setorCodigo(tower.getSetor() != null ? tower.getSetor().getCodigo() : null)
                .setorNome(tower.getSetor() != null ? tower.getSetor().getNome() : null)
                .setorAreaMina(tower.getSetor() != null ? tower.getSetor().getAreaMina() : null)
                .latitude(tower.getLatitude())
                .longitude(tower.getLongitude())
                .fonteEnergia(tower.getFonteEnergia())
                .fonteEnergiaDescricao(tower.getFonteEnergia() != null ? tower.getFonteEnergia().getDescricao() : "Não Definida")
                .status(tower.getStatus())
                .statusLabel(tower.getStatus() != null ? tower.getStatus().getLabel() : "Desconhecido")
                .statusCor(tower.getStatus() != null ? tower.getStatus().getHexColor() : "#6B7280")
                .ligada(tower.isLigada())
                .modoOperacao(tower.getModoOperacao())
                .modoOperacaoLabel(tower.getModoOperacao() != null ? tower.getModoOperacao().getLabel() : "Normal")
                .nivelBateria(tower.getNivelBateria())
                .nivelCombustivel(tower.getNivelCombustivel())
                .horasAtividadeAcumuladas(tower.getHorasAtividadeAcumuladas() != null ? tower.getHorasAtividadeAcumuladas() : 0.0)
                .limiteHorasPreventiva(tower.getLimiteHorasPreventiva() != null ? tower.getLimiteHorasPreventiva() : 250.0)
                .necessitaManutencaoPreventiva(preventiva)
                .alertaBaixaVisibilidade(tower.isAlertaBaixaVisibilidade())
                .ultimoContatoIoT(tower.getUltimoContatoIoT())
                .dataUltimaAtivacao(tower.getDataUltimaAtivacao())
                .dataCadastro(tower.getDataCadastro())
                .build();
    }
}
