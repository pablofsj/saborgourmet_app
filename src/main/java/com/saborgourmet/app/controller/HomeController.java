package com.saborgourmet.app.controller;

import com.saborgourmet.app.service.ReservaService;
import com.saborgourmet.app.util.Agenda;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Controller
public class HomeController {

    private final ReservaService reservaService;

    public HomeController(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

    @GetMapping("/")
    public String inicio(Model model) {
        LocalDate fecha = LocalDate.now();
        LocalTime hora = LocalTime.of(20, 0);
        if (LocalTime.now().isAfter(LocalTime.of(22, 0))) {
            fecha = fecha.plusDays(1);
            hora = LocalTime.of(13, 0);
        }

        model.addAttribute("horas", Agenda.HORAS);
        model.addAttribute("fechaSugerida", fecha);
        model.addAttribute("horaSugerida", hora.toString());
        model.addAttribute("personasSugeridas", 2);
        model.addAttribute("hoy", LocalDate.now());
        model.addAttribute("limite", LocalDate.now().plusDays(60));
        model.addAttribute("mesasDisponiblesHoy", reservaService.mesasDisponibles(fecha, hora, 2).size());
        return "index";
    }
}
