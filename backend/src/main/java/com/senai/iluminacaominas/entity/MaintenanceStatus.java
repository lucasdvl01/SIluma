package com.senai.iluminacaominas.entity;

public enum MaintenanceStatus {
    PENDENTE("Pendente / Aberta"),
    EM_ANDAMENTO("Em Andamento"),
    CONCLUIDA("Concluída"),
    CANCELADA("Cancelada");

    private final String label;

    MaintenanceStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
