package com.saborgourmet.app.controller;

import com.saborgourmet.app.dto.ReservaForm;
import com.saborgourmet.app.model.EstadoReserva;
import com.saborgourmet.app.model.Mesa;
import com.saborgourmet.app.model.Reserva;
import com.saborgourmet.app.service.ReglaNegocioException;
import com.saborgourmet.app.service.ReservaService;
import com.saborgourmet.app.util.Agenda;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Controller
@RequestMapping("/reservas")
public class ReservaController {

    private final ReservaService reservaService;

    public ReservaController(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

    @ModelAttribute("horas")
    public List<String> horas() {
        return Agenda.HORAS;
    }

    @GetMapping("/nueva")
    public String nueva(@RequestParam(required = false) LocalDate fecha,
                        @RequestParam(required = false) LocalTime hora,
                        @RequestParam(defaultValue = "2") Integer personas,
                        Model model,
                        RedirectAttributes redirectAttributes) {
        if (fecha == null || hora == null) {
            return "redirect:/";
        }
        List<Mesa> disponibles;
        try {
            reservaService.validarSolicitud(fecha, hora, personas);
            disponibles = reservaService.mesasDisponibles(fecha, hora, personas);
        } catch (ReglaNegocioException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/";
        }
        if (disponibles.isEmpty()) {
            redirectAttributes.addFlashAttribute("error",
                    "No quedan mesas para " + personas + " personas el " + fecha + " a las " + hora
                            + ". Prueba con otro horario o fecha.");
            return "redirect:/";
        }

        ReservaForm form = new ReservaForm();
        form.setFecha(fecha);
        form.setHora(hora);
        form.setPersonas(personas);

        model.addAttribute("form", form);
        model.addAttribute("mesas", disponibles);
        model.addAttribute("fecha", fecha);
        model.addAttribute("hora", hora);
        model.addAttribute("personas", personas);
        return "reservas/nueva";
    }

    @PostMapping
    public String crear(@Valid @ModelAttribute("form") ReservaForm form,
                        BindingResult bindingResult,
                        Model model,
                        RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return mostrarFormularioNueva(form, model);
        }
        try {
            Reserva reserva = reservaService.crear(form);
            redirectAttributes.addFlashAttribute("exito", "¡Reserva confirmada! Te enviamos los detalles por correo.");
            return "redirect:/reservas/" + reserva.getCodigo();
        } catch (ReglaNegocioException ex) {
            model.addAttribute("error", ex.getMessage());
            return mostrarFormularioNueva(form, model);
        }
    }

    @GetMapping("/{codigo}")
    public String detalle(@PathVariable String codigo, Model model) {
        Reserva reserva = reservaService.obtener(codigo);
        model.addAttribute("reserva", reserva);
        return "reservas/detalle";
    }

    @GetMapping("/buscar")
    public String buscar(@RequestParam(required = false) String q, Model model) {
        if (q != null && !q.isBlank()) {
            model.addAttribute("reservas", reservaService.buscar(q));
        }
        model.addAttribute("q", q);
        return "reservas/buscar";
    }

    @PostMapping("/{codigo}/cancelar")
    public String cancelar(@PathVariable String codigo, RedirectAttributes redirectAttributes) {
        try {
            Reserva reserva = reservaService.cancelar(codigo);
            redirectAttributes.addFlashAttribute("exito", "Tu reserva " + reserva.getCodigo() + " fue cancelada.");
        } catch (ReglaNegocioException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/reservas/" + codigo;
    }

    @GetMapping("/{codigo}/editar")
    public String editar(@PathVariable String codigo,
                         @RequestParam(required = false) LocalDate fecha,
                         @RequestParam(required = false) LocalTime hora,
                         @RequestParam(required = false) Integer personas,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        Reserva reserva = reservaService.obtener(codigo);
        if (!reserva.isModificable()) {
            redirectAttributes.addFlashAttribute("error", "Esta reserva ya no se puede modificar");
            return "redirect:/reservas/" + codigo;
        }

        ReservaForm form = new ReservaForm();
        form.setFecha(fecha != null ? fecha : reserva.getFecha());
        form.setHora(hora != null ? hora : reserva.getHora());
        form.setPersonas(personas != null ? personas : reserva.getPersonas());
        form.setMesaId(reserva.getMesa().getId());
        form.setNombre(reserva.getCliente().getNombre());
        form.setEmail(reserva.getCliente().getEmail());
        form.setTelefono(reserva.getCliente().getTelefono());
        form.setNotas(reserva.getNotas());

        return mostrarFormularioEdicion(reserva, form, model);
    }

    @PostMapping("/{codigo}/editar")
    public String actualizar(@PathVariable String codigo,
                             @Valid @ModelAttribute("form") ReservaForm form,
                             BindingResult bindingResult,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        Reserva reserva = reservaService.obtener(codigo);
        if (!reserva.isModificable()) {
            redirectAttributes.addFlashAttribute("error", "Esta reserva ya no se puede modificar");
            return "redirect:/reservas/" + codigo;
        }
        if (bindingResult.hasErrors()) {
            return mostrarFormularioEdicion(reserva, form, model);
        }
        try {
            reservaService.actualizar(codigo, form);
            redirectAttributes.addFlashAttribute("exito", "Actualizamos tu reserva correctamente.");
            return "redirect:/reservas/" + codigo;
        } catch (ReglaNegocioException ex) {
            model.addAttribute("error", ex.getMessage());
            return mostrarFormularioEdicion(reserva, form, model);
        }
    }

    private String mostrarFormularioNueva(ReservaForm form, Model model) {
        model.addAttribute("form", form);
        model.addAttribute("mesas", reservaService.mesasDisponibles(
                form.getFecha(), form.getHora(), form.getPersonas() == null ? 1 : form.getPersonas()));
        model.addAttribute("modo", "crear");
        return "reservas/nueva";
    }

    private String mostrarFormularioEdicion(Reserva reserva, ReservaForm form, Model model) {
        model.addAttribute("form", form);
        model.addAttribute("reserva", reserva);
        model.addAttribute("mesas", reservaService.mesasDisponiblesParaEdicion(
                reserva, form.getFecha(), form.getHora(),
                form.getPersonas() == null ? 1 : form.getPersonas()));
        model.addAttribute("estados", EstadoReserva.values());
        model.addAttribute("modo", "editar");
        return "reservas/editar";
    }
}
