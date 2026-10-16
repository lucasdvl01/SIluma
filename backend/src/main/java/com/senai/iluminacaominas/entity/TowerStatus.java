package com.senai.iluminacaominas.entity;

public enum TowerStatus {
    OPERACIONAL("Operacional", "#10B981", "green"),
    ALERTA_BAIXA_VISIBILIDADE("Alerta Baixa Visibilidade", "#F59E0B", "yellow"),
    EM_MANUTENCAO("Em Manutenção", "#EF4444", "red"),
    FALHA_TECNICA("Falha Técnica", "#DC2626", "red"),
    DESLIGADA("Desligada", "#6B7280", "gray");

    private final String label;
    private final String hexColor;
    private final String colorName;

    TowerStatus(String label, String hexColor, String colorName) {
        this.label = label;
        this.hexColor = hexColor;
        this.colorName = colorName;
    }

    public String getLabel() {
        return label;
    }

    public String getHexColor() {
        return hexColor;
    }

    public String getColorName() {
        return colorName;
    }
}
