package com.senai.iluminacaominas.entity;

public enum PowerSource {
    PAINEL_SOLAR("Painel Solar"),
    GERADOR_DIESEL("Gerador a Diesel"),
    BATERIA("Bateria"),
    HIBRIDO("Híbrido (Solar + Diesel)");

    private final String descricao;

    PowerSource(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
