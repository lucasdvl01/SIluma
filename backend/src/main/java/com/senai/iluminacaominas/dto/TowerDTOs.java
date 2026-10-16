package com.senai.iluminacaominas.dto;

import com.senai.iluminacaominas.entity.LightingMode;
import com.senai.iluminacaominas.entity.PowerSource;
import com.senai.iluminacaominas.entity.TowerStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

public class TowerDTOs {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TowerCreateDTO {
        @NotBlank(message = "O código de identificação é obrigatório")
        private String codigoIdentificacao;

        @NotNull(message = "O ID do setor é obrigatório")
        private Long setorId;

        @NotNull(message = "A latitude é obrigatória")
        private Double latitude;

        @NotNull(message = "A longitude é obrigatória")
        private Double longitude;

        @NotNull(message = "A fonte de energia é obrigatória")
        private PowerSource fonteEnergia;

        private Double nivelBateria;
        private Double nivelCombustivel;
        private Double limiteHorasPreventiva;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TowerUpdateDTO {
        private Long setorId;
        private Double latitude;
        private Double longitude;
        private PowerSource fonteEnergia;
        private Double limiteHorasPreventiva;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TowerResponseDTO {
        private Long id;
        private String codigoIdentificacao;
        private Long setorId;
        private String setorCodigo;
        private String setorNome;
        private String setorAreaMina;
        private Double latitude;
        private Double longitude;
        private PowerSource fonteEnergia;
        private String fonteEnergiaDescricao;
        private TowerStatus status;
        private String statusLabel;
        private String statusCor;
        private boolean ligada;
        private LightingMode modoOperacao;
        private String modoOperacaoLabel;
        private Double nivelBateria;
        private Double nivelCombustivel;
        private Double horasAtividadeAcumuladas;
        private Double limiteHorasPreventiva;
        private boolean necessitaManutencaoPreventiva;
        private boolean alertaBaixaVisibilidade;
        private LocalDateTime ultimoContatoIoT;
        private LocalDateTime dataUltimaAtivacao;
        private LocalDateTime dataCadastro;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TowerActionDTO {
        @NotBlank(message = "Ação é obrigatória (ex: LIGAR, DESLIGAR, ALTERAR_MODO)")
        private String acao;
        private LightingMode modo;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BatchActionDTO {
        @NotNull(message = "ID do setor é obrigatório")
        private Long setorId;
        @NotBlank(message = "Ação em lote é obrigatória (LIGAR_TODAS, DESLIGAR_TODAS, ALTERAR_MODO)")
        private String acao;
        private LightingMode modo;
    }
}
