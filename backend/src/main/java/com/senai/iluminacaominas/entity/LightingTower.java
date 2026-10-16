package com.senai.iluminacaominas.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_torres_iluminacao")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LightingTower {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String codigoIdentificacao; // ex: "TR-001", "TR-002"

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "setor_id", nullable = false)
    @JsonIgnoreProperties("torres")
    private Sector setor;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PowerSource fonteEnergia;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 35)
    @Builder.Default
    private TowerStatus status = TowerStatus.OPERACIONAL;

    @Column(nullable = false)
    @Builder.Default
    private boolean ligada = true;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 35)
    @Builder.Default
    private LightingMode modoOperacao = LightingMode.NORMAL;

    @Column(nullable = false)
    @Builder.Default
    private Double nivelBateria = 85.0; // % 0 a 100

    @Column
    private Double nivelCombustivel; // % 0 a 100 (para gerador a diesel ou híbrido)

    @Column(nullable = false)
    @Builder.Default
    private Double horasAtividadeAcumuladas = 0.0; // Horímetro total

    @Column(nullable = false)
    @Builder.Default
    private Double limiteHorasPreventiva = 250.0; // Horas para próxima manutenção preventiva

    @Column(nullable = false)
    @Builder.Default
    private boolean alertaBaixaVisibilidade = false; // disparado pelo sensor LDR ou cerração

    @Column
    private LocalDateTime ultimoContatoIoT;

    @Column
    private LocalDateTime dataUltimaAtivacao;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dataCadastro;

    @PrePersist
    protected void onCreate() {
        this.dataCadastro = LocalDateTime.now();
        if (this.ligada && this.dataUltimaAtivacao == null) {
            this.dataUltimaAtivacao = LocalDateTime.now();
        }
    }
}
