package com.senai.iluminacaominas.dto;

import lombok.*;

import java.time.LocalDateTime;

public class AuditDTOs {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AuditResponseDTO {
        private Long id;
        private String usuarioEmail;
        private String acao;
        private String entidade;
        private String entidadeId;
        private String detalhes;
        private String ipOrigem;
        private LocalDateTime dataHora;
    }
}
