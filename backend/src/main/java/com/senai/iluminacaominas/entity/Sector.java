package com.senai.iluminacaominas.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tb_setores")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Sector {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String codigo;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(length = 150)
    private String areaMina; // ex: "Cava Principal", "Trecho de Acesso", "Plataforma Sul"

    @Column(length = 200)
    private String frentesDeLavra; // ex: "Frente 01, Frente 02"

    @Column(length = 255)
    private String descricao;

    @OneToMany(mappedBy = "setor", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<LightingTower> torres = new ArrayList<>();
}
