package com.saborgourmet.app.util;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/** Horario de servicio del restaurante: dos turnos de 30 en 30 minutos. */
public final class Agenda {

    public static final List<String> HORAS = construirHoras();

    private Agenda() {
    }

    public static boolean esHoraValida(LocalTime hora) {
        if (hora == null || hora.getSecond() != 0) {
            return false;
        }
        return HORAS.contains(hora.toString());
    }

    public static String turnoDe(LocalTime hora) {
        return hora.isBefore(LocalTime.of(17, 0)) ? "Almuerzo" : "Cena";
    }

    public static String horaTexto(LocalTime hora) {
        return hora == null ? "" : hora.toString();
    }

    private static List<String> construirHoras() {
        List<String> horas = new ArrayList<>();
        agregarTurno(horas, LocalTime.of(12, 0), LocalTime.of(15, 30));
        agregarTurno(horas, LocalTime.of(19, 0), LocalTime.of(22, 30));
        return List.copyOf(horas);
    }

    private static void agregarTurno(List<String> horas, LocalTime inicio, LocalTime fin) {
        for (LocalTime h = inicio; !h.isAfter(fin); h = h.plusMinutes(30)) {
            horas.add(h.toString());
        }
    }
}
