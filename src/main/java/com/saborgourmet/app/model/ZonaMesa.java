package com.saborgourmet.app.model;

public enum ZonaMesa {
    SALON("Salón"),
    TERRAZA("Terraza");

    private final String etiqueta;

    ZonaMesa(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
