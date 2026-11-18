package com.saborgourmet.app.util;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/** Horario de servicio del restaurante: reservas cada hora de 12:00 a 22:00. */
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

    private static List<String> construirHoras() {
        List<String> horas = new ArrayList<>();
        for (LocalTime h = LocalTime.of(12, 0); !h.isAfter(LocalTime.of(22, 0)); h = h.plusHours(1)) {
            horas.add(h.toString());
        }
        return List.copyOf(horas);
    }
}
