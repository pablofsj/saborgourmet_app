package com.saborgourmet.app.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class ReservaForm {

    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");

    @NotNull(message = "Selecciona la fecha de la reserva")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fecha;

    @NotNull(message = "Selecciona la hora de la reserva")
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime hora;

    @NotNull(message = "Indica cuántas personas asistirán")
    @Min(value = 1, message = "La reserva es para al menos 1 persona")
    @Max(value = 12, message = "Para más de 12 personas contáctanos por teléfono")
    private Integer personas;

    @NotNull(message = "Selecciona una mesa disponible")
    private Long mesaId;

    @NotBlank(message = "Ingresa tu nombre")
    @Size(max = 120, message = "El nombre no puede superar los 120 caracteres")
    private String nombre;

    @NotBlank(message = "Ingresa tu correo electrónico")
    @Email(message = "Ingresa un correo electrónico válido")
    @Size(max = 160, message = "El correo no puede superar los 160 caracteres")
    private String email;

    @Size(max = 30, message = "El teléfono no puede superar los 30 caracteres")
    private String telefono;

    @Size(max = 300, message = "Las notas no pueden superar los 300 caracteres")
    private String notas;

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public LocalTime getHora() {
        return hora;
    }

    public void setHora(LocalTime hora) {
        this.hora = hora;
    }

    public Integer getPersonas() {
        return personas;
    }

    public void setPersonas(Integer personas) {
        this.personas = personas;
    }

    public Long getMesaId() {
        return mesaId;
    }

    public void setMesaId(Long mesaId) {
        this.mesaId = mesaId;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getNotas() {
        return notas;
    }

    public void setNotas(String notas) {
        this.notas = notas;
    }

    public String getHoraTexto() {
        return hora == null ? "" : hora.format(HORA);
    }
}
