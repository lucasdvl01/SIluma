package com.senai.iluminacaominas.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_logs_auditoria")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String usuarioEmail;

    @Column(nullable = false, length = 80)
    private String acao; // ex: "LIGAR_TORRE", "DESLIGAR_TORRE", "MODO_CERRACAO", "COMANDO_LOTE", "CADASTRAR_TORRE", "ABRIR_MANUTENCAO"

    @Column(nullable = false, length = 60)
    private String entidade; // ex: "LightingTower", "MaintenanceOrder"

    @Column(length = 60)
    private String entidadeId;

    @Column(columnDefinition = "TEXT")
    private String detalhes;

    @Column(length = 60)
    private String ipOrigem;

    @Column(nullable = false)
    private LocalDateTime dataHora;

    @PrePersist
    protected void onCreate() {
        if (this.dataHora == null) {
            this.dataHora = LocalDateTime.now();
        }
    }
}
