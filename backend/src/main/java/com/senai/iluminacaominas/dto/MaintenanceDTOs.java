package com.senai.iluminacaominas.dto;

import com.senai.iluminacaominas.entity.MaintenanceSeverity;
import com.senai.iluminacaominas.entity.MaintenanceStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

public class MaintenanceDTOs {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MaintenanceCreateDTO {
        @NotNull(message = "ID da torre é obrigatório")
        private Long torreId;

        @NotBlank(message = "Tipo da falha é obrigatório")
        private String tipoFalha;

        private MaintenanceSeverity gravidade;

        @NotBlank(message = "Descrição do problema é obrigatória")
        private String descricao;

        @NotBlank(message = "Técnico responsável é obrigatório")
        private String tecnicoResponsavel;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MaintenanceUpdateDTO {
        @NotNull(message = "Status é obrigatório")
        private MaintenanceStatus status;
        private String observacoesResolucao;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MaintenanceResponseDTO {
        private Long id;
        private Long torreId;
        private String codigoTorre;
        private String setorNome;
        private String tipoFalha;
        private MaintenanceSeverity gravidade;
        private String descricao;
        private String tecnicoResponsavel;
        private MaintenanceStatus status;
        private String statusLabel;
        private LocalDateTime dataAbertura;
        private LocalDateTime dataConclusao;
        private Double horasTorreNaAbertura;
        private String observacoesResolucao;
        private String usuarioAbertura;
    }
}
