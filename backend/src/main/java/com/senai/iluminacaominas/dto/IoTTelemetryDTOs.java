package com.senai.iluminacaominas.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

public class IoTTelemetryDTOs {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TelemetryRequestDTO {
        @NotBlank(message = "Código da torre é obrigatório")
        private String codigoTorre;

        @NotNull(message = "Leitura do sensor LDR é obrigatória")
        private Integer luminosidadeLdr; // 0-1023 (onde valores baixos indicam escuro/cerração)

        private Boolean statusLed;
        private Double nivelBateria;
        private Boolean pressaoBotaoCerracao;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TelemetryResponseDTO {
        private String codigoTorre;
        private boolean instrucaoLed; // true = acender / manter aceso
        private String modoLuz;
        private int intensidadePwm; // 0 a 255 para controle PWM no ESP32/Arduino
        private boolean alertaAtivado;
        private String mensagem;
        private LocalDateTime horarioServidor;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class HardwarePollingDTO {
        private String codigoTorre;
        private boolean ligada;
        private String modo;
        private int intensidadePwm; // 0 a 255
        private boolean modoCerracao;
        private String corLedSugerida; // "VERDE", "AMBAR", "AZUL", "APAGADO"
    }
}
