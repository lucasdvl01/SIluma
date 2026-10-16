package com.senai.iluminacaominas.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_ordens_manutencao")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaintenanceOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "torre_id", nullable = false)
    private LightingTower torre;

    @Column(nullable = false, length = 30)
    private String codigoTorre;

    @Column(length = 100)
    private String setorNome;

    @Column(nullable = false, length = 100)
    private String tipoFalha; // ex: "Lâmpada Queimada", "Bateria não carrega", "Revisão Preventiva Horímetro", "Falha Sensor LDR"

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private MaintenanceSeverity gravidade = MaintenanceSeverity.MEDIA;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descricao;

    @Column(nullable = false, length = 100)
    private String tecnicoResponsavel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    @Builder.Default
    private MaintenanceStatus status = MaintenanceStatus.PENDENTE;

    @Column(nullable = false)
    private LocalDateTime dataAbertura;

    @Column
    private LocalDateTime dataConclusao;

    @Column
    private Double horasTorreNaAbertura;

    @Column(columnDefinition = "TEXT")
    private String observacoesResolucao;

    @Column(length = 120)
    private String usuarioAbertura;

    @PrePersist
    protected void onCreate() {
        if (this.dataAbertura == null) {
            this.dataAbertura = LocalDateTime.now();
        }
    }
}
