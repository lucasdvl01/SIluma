package com.senai.iluminacaominas.entity;

public enum LightingMode {
    NORMAL("Modo Normal", 1.0, 100),
    ECONOMIA("Modo Economia", 0.5, 50),
    ALTA_VISIBILIDADE_CERRACAO("Alta Visibilidade / Cerração", 1.5, 150);

    private final String label;
    private final double fatorConsumo; // Multiplicador de consumo em relação ao padrão (ex: 1.0 kWh/h)
    private final int intensidadeLuz; // % intensidade do LED / luminária

    LightingMode(String label, double fatorConsumo, int intensidadeLuz) {
        this.label = label;
        this.fatorConsumo = fatorConsumo;
        this.intensidadeLuz = intensidadeLuz;
    }

    public String getLabel() {
        return label;
    }

    public double getFatorConsumo() {
        return fatorConsumo;
    }

    public int getIntensidadeLuz() {
        return intensidadeLuz;
    }
}
