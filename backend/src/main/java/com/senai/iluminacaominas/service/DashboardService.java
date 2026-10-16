package com.senai.iluminacaominas.service;

import com.senai.iluminacaominas.dto.DashboardStatsDTO;
import com.senai.iluminacaominas.dto.DashboardStatsDTO.RecentAlertDTO;
import com.senai.iluminacaominas.dto.DashboardStatsDTO.SectorChartDTO;
import com.senai.iluminacaominas.entity.LightingTower;
import com.senai.iluminacaominas.entity.PowerSource;
import com.senai.iluminacaominas.entity.Sector;
import com.senai.iluminacaominas.entity.TowerStatus;
import com.senai.iluminacaominas.repository.LightingTowerRepository;
import com.senai.iluminacaominas.repository.MaintenanceOrderRepository;
import com.senai.iluminacaominas.repository.SectorRepository;
import com.senai.iluminacaominas.repository.TowerActivityLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final LightingTowerRepository towerRepository;
    private final SectorRepository sectorRepository;
    private final TowerActivityLogRepository activityLogRepository;
    private final MaintenanceOrderRepository maintenanceRepository;

    @Transactional(readOnly = true)
    public DashboardStatsDTO getStats() {
        List<LightingTower> allTowers = towerRepository.findAll();
        List<Sector> allSectors = sectorRepository.findAll();

        long total = allTowers.size();
        long operacionais = allTowers.stream().filter(t -> t.isLigada() && t.getStatus() == TowerStatus.OPERACIONAL).count();
        long desligadas = allTowers.stream().filter(t -> !t.isLigada()).count();
        long alertasVisibilidade = allTowers.stream().filter(t -> t.getStatus() == TowerStatus.ALERTA_BAIXA_VISIBILIDADE).count();
        long emManutencao = allTowers.stream().filter(t -> t.getStatus() == TowerStatus.EM_MANUTENCAO || t.getStatus() == TowerStatus.FALHA_TECNICA).count();

        // Horas totais acumuladas somando horímetros
        double totalHoras = allTowers.stream().mapToDouble(LightingTower::getHorasAtividadeAcumuladas).sum();

        // Consumo calculado: 1.2 kWh por hora de torre ligada
        double consumoTotalKWh = activityLogRepository.sumTotalConsumoKWh();
        if (consumoTotalKWh == 0.0 && totalHoras > 0) {
            consumoTotalKWh = totalHoras * 1.2;
        }

        // Economia gerada: Torres solares e híbridas evitam geradores convencionais 100% diesel
        // 1 litro de diesel gera aprox. 3.0 kWh elétricos e emite 2.68 kg de CO2
        long torresSustentaveis = allTowers.stream()
                .filter(t -> t.getFonteEnergia() == PowerSource.PAINEL_SOLAR || t.getFonteEnergia() == PowerSource.HIBRIDO || t.getFonteEnergia() == PowerSource.BATERIA)
                .count();

        double fatorSustentabilidade = total > 0 ? ((double) torresSustentaveis / total) : 0.6;
        double economiaGeradaKWh = consumoTotalKWh * fatorSustentabilidade * 0.75;
        double litrosDieselEconomizados = economiaGeradaKWh / 3.0;
        double reducaoCO2Kg = litrosDieselEconomizados * 2.68;

        // Distribuições
        Map<String, Long> distStatus = new LinkedHashMap<>();
        distStatus.put("Operacional", operacionais);
        distStatus.put("Alerta Cerração/Visibilidade", alertasVisibilidade);
        distStatus.put("Desligada", desligadas);
        distStatus.put("Em Manutenção / Falha", emManutencao);

        Map<String, Long> distFonte = new LinkedHashMap<>();
        for (PowerSource source : PowerSource.values()) {
            long count = allTowers.stream().filter(t -> t.getFonteEnergia() == source).count();
            distFonte.put(source.getDescricao(), count);
        }

        List<SectorChartDTO> distSetores = new ArrayList<>();
        for (Sector s : allSectors) {
            long totalSetor = allTowers.stream().filter(t -> t.getSetor().getId().equals(s.getId())).count();
            long ativasSetor = allTowers.stream().filter(t -> t.getSetor().getId().equals(s.getId()) && t.isLigada()).count();
            double horasSetor = allTowers.stream().filter(t -> t.getSetor().getId().equals(s.getId())).mapToDouble(LightingTower::getHorasAtividadeAcumuladas).sum();
            double consumoSetor = horasSetor * 1.2;

            distSetores.add(SectorChartDTO.builder()
                    .setorNome(s.getNome())
                    .totalTorres((int) totalSetor)
                    .torresAtivas((int) ativasSetor)
                    .consumoKWh(Math.round(consumoSetor * 10.0) / 10.0)
                    .build());
        }

        // Alertas recentes
        List<RecentAlertDTO> alertas = new ArrayList<>();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM HH:mm");

        allTowers.stream()
                .filter(t -> t.getStatus() == TowerStatus.ALERTA_BAIXA_VISIBILIDADE)
                .forEach(t -> alertas.add(RecentAlertDTO.builder()
                        .codigoTorre(t.getCodigoIdentificacao())
                        .tipoAlerta("Cerração Detectada (Sensor LDR)")
                        .nivelGravidade("MODERADO")
                        .dataHora(t.getUltimoContatoIoT() != null ? t.getUltimoContatoIoT().format(dtf) : "Agora")
                        .build()));

        allTowers.stream()
                .filter(t -> t.getHorasAtividadeAcumuladas() >= t.getLimiteHorasPreventiva())
                .forEach(t -> alertas.add(RecentAlertDTO.builder()
                        .codigoTorre(t.getCodigoIdentificacao())
                        .tipoAlerta("Limite de Horímetro Preventiva Atingido (" + String.format("%.0f", t.getHorasAtividadeAcumuladas()) + "h)")
                        .nivelGravidade("ALERTA")
                        .dataHora("Pendente")
                        .build()));

        allTowers.stream()
                .filter(t -> t.getNivelBateria() != null && t.getNivelBateria() < 20.0)
                .forEach(t -> alertas.add(RecentAlertDTO.builder()
                        .codigoTorre(t.getCodigoIdentificacao())
                        .tipoAlerta("Nível Crítico de Bateria (" + String.format("%.0f", t.getNivelBateria()) + "%)")
                        .nivelGravidade("CRÍTICO")
                        .dataHora("Agora")
                        .build()));

        return DashboardStatsDTO.builder()
                .totalTorres(total)
                .torresOperacionais(operacionais)
                .torresDesligadas(desligadas)
                .torresAlertaVisibilidade(alertasVisibilidade)
                .torresEmManutencao(emManutencao)
                .consumoTotalKWh(Math.round(consumoTotalKWh * 10.0) / 10.0)
                .economiaGeradaKWh(Math.round(economiaGeradaKWh * 10.0) / 10.0)
                .litrosDieselEconomizados(Math.round(litrosDieselEconomizados * 10.0) / 10.0)
                .reducaoCO2Kg(Math.round(reducaoCO2Kg * 10.0) / 10.0)
                .totalHorasAtividade(Math.round(totalHoras * 10.0) / 10.0)
                .distribuicaoStatus(distStatus)
                .distribuicaoFonteEnergia(distFonte)
                .distribuicaoSetores(distSetores)
                .alertasRecentes(alertas)
                .build();
    }
}
