package com.saborgourmet.app.model;

public enum ZonaMesa {
    SALON("Salón principal"),
    VENTANA("Junto a la ventana"),
    TERRAZA("Terraza"),
    PRIVADO("Sala privada");

    private final String etiqueta;

    ZonaMesa(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
