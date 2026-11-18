package com.saborgourmet.app.dto;

import com.saborgourmet.app.model.ZonaMesa;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class MesaForm {

    @NotNull(message = "Indica el número de mesa")
    @Min(value = 1, message = "El número de mesa debe ser mayor que 0")
    @Max(value = 999, message = "El número de mesa no puede superar 999")
    private Integer numero;

    @NotNull(message = "Indica la capacidad de la mesa")
    @Min(value = 1, message = "La capacidad mínima es 1 persona")
    @Max(value = 12, message = "La capacidad máxima es 12 personas")
    private Integer capacidad;

    @NotNull(message = "Selecciona una zona")
    private ZonaMesa zona;

    @Size(max = 160, message = "La descripción no puede superar los 160 caracteres")
    private String descripcion;

    private boolean activa = true;

    public Integer getNumero() {
        return numero;
    }

    public void setNumero(Integer numero) {
        this.numero = numero;
    }

    public Integer getCapacidad() {
        return capacidad;
    }

    public void setCapacidad(Integer capacidad) {
        this.capacidad = capacidad;
    }

    public ZonaMesa getZona() {
        return zona;
    }

    public void setZona(ZonaMesa zona) {
        this.zona = zona;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public boolean isActiva() {
        return activa;
    }

    public void setActiva(boolean activa) {
        this.activa = activa;
    }
}
