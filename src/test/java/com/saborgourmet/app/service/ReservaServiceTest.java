package com.saborgourmet.app.service;

import com.saborgourmet.app.dto.ReservaForm;
import com.saborgourmet.app.model.Mesa;
import com.saborgourmet.app.model.Reserva;
import com.saborgourmet.app.model.ZonaMesa;
import com.saborgourmet.app.repository.ClienteRepository;
import com.saborgourmet.app.repository.MesaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:sabortest;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Transactional
class ReservaServiceTest {

    @Autowired
    private ReservaService reservaService;

    @Autowired
    private MesaRepository mesaRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Test
    void unaMesaReservadaNoSeOfreceDentroDelBloqueDeDosHoras() {
        Mesa mesa = crearMesa(101, 4);
        LocalDate fecha = LocalDate.now().plusDays(2);
        reservaService.crear(formulario(mesa, fecha, LocalTime.of(20, 0), 2));

        assertThat(idsDisponibles(fecha, LocalTime.of(20, 30), 2)).doesNotContain(mesa.getId());
        assertThat(idsDisponibles(fecha, LocalTime.of(18, 30), 2)).doesNotContain(mesa.getId());
        assertThat(idsDisponibles(fecha, LocalTime.of(22, 0), 2)).contains(mesa.getId());
        assertThat(idsDisponibles(fecha.plusDays(1), LocalTime.of(20, 0), 2)).contains(mesa.getId());
    }

    @Test
    void noSePuedeReservarFueraDeLosTurnosDeServicio() {
        Mesa mesa = crearMesa(102, 4);
        LocalDate fecha = LocalDate.now().plusDays(2);

        assertThatThrownBy(() -> reservaService.crear(formulario(mesa, fecha, LocalTime.of(17, 0), 2)))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("horario");
    }

    @Test
    void noSePuedeReservarUnaMesaMasPequenaQueElGrupo() {
        Mesa mesa = crearMesa(103, 2);
        LocalDate fecha = LocalDate.now().plusDays(2);

        assertThatThrownBy(() -> reservaService.crear(formulario(mesa, fecha, LocalTime.of(13, 0), 4)))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("capacidad");
    }

    @Test
    void noSePuedeReservarUnaFechaPasada() {
        Mesa mesa = crearMesa(104, 4);
        LocalDate ayer = LocalDate.now().minusDays(1);

        assertThatThrownBy(() -> reservaService.crear(formulario(mesa, ayer, LocalTime.of(13, 0), 2)))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("pasadas");
    }

    @Test
    void cancelarLiberaLaMesaYUnaReservaCanceladaNoSeVuelveACancelar() {
        Mesa mesa = crearMesa(105, 4);
        LocalDate fecha = LocalDate.now().plusDays(2);
        LocalTime hora = LocalTime.of(21, 0);

        Reserva reserva = reservaService.crear(formulario(mesa, fecha, hora, 2));
        assertThat(reserva.getCodigo()).startsWith("SG-");

        reservaService.cancelar(reserva.getCodigo());

        assertThat(idsDisponibles(fecha, hora, 2)).contains(mesa.getId());
        assertThatThrownBy(() -> reservaService.cancelar(reserva.getCodigo()))
                .isInstanceOf(ReglaNegocioException.class);
    }

    @Test
    void elMismoCorreoReutilizaElClienteEnVezDeDuplicarlo() {
        Mesa mesa = crearMesa(106, 4);
        LocalDate fecha = LocalDate.now().plusDays(3);

        reservaService.crear(formulario(mesa, fecha, LocalTime.of(13, 0), 2));
        reservaService.crear(formulario(mesa, fecha.plusDays(1), LocalTime.of(13, 0), 2));

        assertThat(clienteRepository.findAll())
                .filteredOn(cliente -> cliente.getEmail().equals("prueba@example.com"))
                .hasSize(1);
    }

    private Mesa crearMesa(int numero, int capacidad) {
        return mesaRepository.save(new Mesa(numero, capacidad, ZonaMesa.SALON, "Mesa de prueba"));
    }

    private ReservaForm formulario(Mesa mesa, LocalDate fecha, LocalTime hora, int personas) {
        ReservaForm form = new ReservaForm();
        form.setFecha(fecha);
        form.setHora(hora);
        form.setPersonas(personas);
        form.setMesaId(mesa.getId());
        form.setNombre("Cliente de Prueba");
        form.setEmail("prueba@example.com");
        form.setTelefono("+56 9 0000 0000");
        return form;
    }

    private List<Long> idsDisponibles(LocalDate fecha, LocalTime hora, int personas) {
        return reservaService.mesasDisponibles(fecha, hora, personas).stream()
                .map(Mesa::getId)
                .toList();
    }
}
