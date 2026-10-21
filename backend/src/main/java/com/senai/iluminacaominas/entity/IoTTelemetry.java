package com.senai.iluminacaominas.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_telemetria_iot")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IoTTelemetry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30)
    private String codigoTorre;

    @Column(nullable = false)
    private Integer luminosidadeLdr; // Valor analógico normalizado para 0-1023

    @Column(nullable = false)
    private Boolean statusLed; // se a luminária está ligada ou desligada

    @Column(nullable = false)
    private Double nivelBateria; // %

    @Column(nullable = false)
    @Builder.Default
    private Boolean pressaoBotaoCerracao = false;

    @Column(nullable = false)
    private LocalDateTime dataHora;

    @Column(nullable = false)
    @Builder.Default
    private Boolean alertaGerado = false;

    @Column(length = 150)
    private String comandoResposta; // Instrução enviada de volta ao circuito Arduino

    @PrePersist
    protected void onCreate() {
        if (this.dataHora == null) {
            this.dataHora = LocalDateTime.now();
        }
    }
}
