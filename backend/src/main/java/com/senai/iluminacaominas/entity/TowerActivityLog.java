package com.senai.iluminacaominas.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_historico_atividade")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TowerActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "torre_id", nullable = false)
    private LightingTower torre;

    @Column(nullable = false, length = 30)
    private String codigoTorre;

    @Column(nullable = false)
    private LocalDateTime dataHoraLigou;

    @Column
    private LocalDateTime dataHoraDesligou;

    @Column
    @Builder.Default
    private Long minutosAtivo = 0L;

    @Column
    @Builder.Default
    private Double consumoEstimadoKWh = 0.0;

    @Enumerated(EnumType.STRING)
    @Column(length = 35)
    private LightingMode modo;

    @Column(length = 120)
    private String usuarioAcionamento;

    @Column(length = 60)
    private String ipAcionamento;
}
