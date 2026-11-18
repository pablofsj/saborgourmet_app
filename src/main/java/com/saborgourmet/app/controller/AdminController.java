package com.saborgourmet.app.controller;

import com.saborgourmet.app.dto.MesaForm;
import com.saborgourmet.app.model.EstadoReserva;
import com.saborgourmet.app.model.Mesa;
import com.saborgourmet.app.model.Reserva;
import com.saborgourmet.app.model.ZonaMesa;
import com.saborgourmet.app.service.MesaService;
import com.saborgourmet.app.service.ReglaNegocioException;
import com.saborgourmet.app.service.ReservaService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
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

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final ReservaService reservaService;
    private final MesaService mesaService;

    public AdminController(ReservaService reservaService, MesaService mesaService) {
        this.reservaService = reservaService;
        this.mesaService = mesaService;
    }

    @ModelAttribute("estados")
    public EstadoReserva[] estados() {
        return EstadoReserva.values();
    }

    @ModelAttribute("zonas")
    public ZonaMesa[] zonas() {
        return ZonaMesa.values();
    }

    @GetMapping
    public String panel(Model model) {
        LocalDate hoy = LocalDate.now();
        model.addAttribute("hoy", hoy);
        model.addAttribute("reservasHoy", reservaService.reservasDelDia(hoy));
        model.addAttribute("totalReservasHoy", reservaService.reservasActivasDelDia(hoy));
        model.addAttribute("comensalesHoy", reservaService.comensalesDelDia(hoy));
        model.addAttribute("mesasActivas", mesaService.contarActivas());
        model.addAttribute("confirmadas", reservaService.totalConfirmadas());
        model.addAttribute("canceladas", reservaService.totalCanceladas());
        model.addAttribute("proximas", reservaService.proximasReservas().stream().limit(6).toList());
        return "admin/panel";
    }

    @GetMapping("/reservas")
    public String reservas(@RequestParam(required = false)
                           @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
                           @RequestParam(required = false) EstadoReserva estado,
                           Model model) {
        LocalDate dia = fecha != null ? fecha : LocalDate.now();
        model.addAttribute("fecha", dia);
        model.addAttribute("estadoSeleccionado", estado);
        model.addAttribute("reservas",
                estado == null ? reservaService.reservasDelDia(dia) : reservaService.reservasDelDia(dia, estado));
        return "admin/reservas";
    }

    @PostMapping("/reservas/{id}/estado")
    public String cambiarEstado(@PathVariable Long id,
                                @RequestParam EstadoReserva estado,
                                @RequestParam(required = false)
                                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
                                RedirectAttributes redirectAttributes) {
        try {
            Reserva reserva = reservaService.cambiarEstado(id, estado);
            redirectAttributes.addFlashAttribute("exito",
                    "Reserva " + reserva.getCodigo() + " marcada como " + estado.getEtiqueta().toLowerCase());
        } catch (ReglaNegocioException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/reservas" + (fecha != null ? "?fecha=" + fecha : "");
    }

    @GetMapping("/mesas")
    public String mesas(Model model) {
        model.addAttribute("mesas", mesaService.listarTodas());
        return "admin/mesas";
    }

    @GetMapping("/mesas/nueva")
    public String nuevaMesa(Model model) {
        model.addAttribute("form", new MesaForm());
        model.addAttribute("editando", false);
        return "admin/mesa-form";
    }

    @PostMapping("/mesas")
    public String crearMesa(@Valid @ModelAttribute("form") MesaForm form,
                            BindingResult bindingResult,
                            Model model,
                            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("editando", false);
            return "admin/mesa-form";
        }
        try {
            Mesa mesa = mesaService.crear(form);
            redirectAttributes.addFlashAttribute("exito", "Mesa " + mesa.getNumero() + " creada correctamente");
            return "redirect:/admin/mesas";
        } catch (ReglaNegocioException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("editando", false);
            return "admin/mesa-form";
        }
    }

    @GetMapping("/mesas/{id}/editar")
    public String editarMesa(@PathVariable Long id, Model model) {
        Mesa mesa = mesaService.obtener(id);
        model.addAttribute("form", mesaService.aFormulario(mesa));
        model.addAttribute("mesa", mesa);
        model.addAttribute("editando", true);
        return "admin/mesa-form";
    }

    @PostMapping("/mesas/{id}")
    public String actualizarMesa(@PathVariable Long id,
                                 @Valid @ModelAttribute("form") MesaForm form,
                                 BindingResult bindingResult,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("mesa", mesaService.obtener(id));
            model.addAttribute("editando", true);
            return "admin/mesa-form";
        }
        try {
            Mesa mesa = mesaService.actualizar(id, form);
            redirectAttributes.addFlashAttribute("exito", "Mesa " + mesa.getNumero() + " actualizada");
            return "redirect:/admin/mesas";
        } catch (ReglaNegocioException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("mesa", mesaService.obtener(id));
            model.addAttribute("editando", true);
            return "admin/mesa-form";
        }
    }

    @PostMapping("/mesas/{id}/estado")
    public String alternarMesa(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Mesa mesa = mesaService.alternarActiva(id);
            redirectAttributes.addFlashAttribute("exito",
                    "Mesa " + mesa.getNumero() + (mesa.isActiva() ? " activada" : " desactivada"));
        } catch (ReglaNegocioException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/mesas";
    }
}
