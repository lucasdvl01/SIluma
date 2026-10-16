package com.senai.iluminacaominas.dto;

import lombok.*;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardStatsDTO {
    // Contadores de frota (RF05)
    private long totalTorres;
    private long torresOperacionais;
    private long torresDesligadas;
    private long torresAlertaVisibilidade;
    private long torresEmManutencao;

    // Métricas energéticas e sustentabilidade
    private double consumoTotalKWh;
    private double economiaGeradaKWh;
    private double litrosDieselEconomizados;
    private double reducaoCO2Kg;
    private double totalHorasAtividade;

    // Gráficos e Distribuições
    private Map<String, Long> distribuicaoStatus;
    private Map<String, Long> distribuicaoFonteEnergia;
    private List<SectorChartDTO> distribuicaoSetores;
    private List<RecentAlertDTO> alertasRecentes;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SectorChartDTO {
        private String setorNome;
        private int totalTorres;
        private int torresAtivas;
        private double consumoKWh;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RecentAlertDTO {
        private String codigoTorre;
        private String tipoAlerta;
        private String nivelGravidade;
        private String dataHora;
    }
}
