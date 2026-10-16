package com.senai.iluminacaominas.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

public class SectorDTOs {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SectorCreateDTO {
        @NotBlank(message = "Código do setor é obrigatório")
        private String codigo;
        @NotBlank(message = "Nome do setor é obrigatório")
        private String nome;
        private String areaMina;
        private String frentesDeLavra;
        private String descricao;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SectorResponseDTO {
        private Long id;
        private String codigo;
        private String nome;
        private String areaMina;
        private String frentesDeLavra;
        private String descricao;
        private int totalTorres;
        private int torresLigadas;
    }
}
