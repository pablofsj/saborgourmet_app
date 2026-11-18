package com.saborgourmet.app.service;

import com.saborgourmet.app.dto.ReservaForm;
import com.saborgourmet.app.model.Cliente;
import com.saborgourmet.app.model.EstadoReserva;
import com.saborgourmet.app.model.Mesa;
import com.saborgourmet.app.model.Reserva;
import com.saborgourmet.app.repository.MesaRepository;
import com.saborgourmet.app.repository.ReservaRepository;
import com.saborgourmet.app.util.Agenda;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Locale;

@Service
public class ReservaService {

    private static final int DIAS_MAXIMOS_ANTICIPACION = 60;
    private static final String ALFABETO_CODIGO = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final ReservaRepository reservaRepository;
    private final MesaRepository mesaRepository;
    private final ClienteService clienteService;

    public ReservaService(ReservaRepository reservaRepository,
                          MesaRepository mesaRepository,
                          ClienteService clienteService) {
        this.reservaRepository = reservaRepository;
        this.mesaRepository = mesaRepository;
        this.clienteService = clienteService;
    }

    /**
     * Mesas activas con capacidad suficiente y sin otra reserva que se solape en
     * las dos horas siguientes a la hora solicitada.
     */
    @Transactional(readOnly = true)
    public List<Mesa> mesasDisponibles(LocalDate fecha, LocalTime hora, int personas) {
        if (fecha == null || hora == null || personas < 1) {
            return List.of();
        }
        return mesaRepository
                .findByActivaTrueAndCapacidadGreaterThanEqualOrderByCapacidadAscNumeroAsc(personas)
                .stream()
                .filter(mesa -> sinSolapamiento(mesa, fecha, hora, null))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Mesa> mesasDisponiblesParaEdicion(Reserva reserva, LocalDate fecha, LocalTime hora, int personas) {
        if (fecha == null || hora == null || personas < 1) {
            return List.of();
        }
        return mesaRepository
                .findByActivaTrueAndCapacidadGreaterThanEqualOrderByCapacidadAscNumeroAsc(personas)
                .stream()
                .filter(mesa -> sinSolapamiento(mesa, fecha, hora, reserva.getId()))
                .toList();
    }

    @Transactional(readOnly = true)
    public void validarSolicitud(LocalDate fecha, LocalTime hora, int personas) {
        validarDatos(fecha, hora, personas);
    }

    @Transactional
    public Reserva crear(ReservaForm form) {
        validarDatos(form.getFecha(), form.getHora(), form.getPersonas());
        Mesa mesa = mesaActiva(form.getMesaId());
        validarDisponibilidad(mesa, form.getFecha(), form.getHora(), form.getPersonas(), null);

        Cliente cliente = clienteService.obtenerOCrear(form.getNombre(), form.getEmail(), form.getTelefono());
        Reserva reserva = new Reserva(generarCodigo(), cliente, mesa, form.getFecha(), form.getHora(), form.getPersonas());
        reserva.setNotas(form.getNotas());
        return reservaRepository.save(reserva);
    }

    @Transactional
    public Reserva actualizar(String codigo, ReservaForm form) {
        Reserva reserva = obtener(codigo);
        if (!reserva.isModificable()) {
            throw new ReglaNegocioException("Esta reserva ya no se puede modificar");
        }
        validarDatos(form.getFecha(), form.getHora(), form.getPersonas());
        Mesa mesa = mesaActiva(form.getMesaId());
        validarDisponibilidad(mesa, form.getFecha(), form.getHora(), form.getPersonas(), reserva.getId());

        Cliente cliente = clienteService.obtenerOCrear(form.getNombre(), form.getEmail(), form.getTelefono());
        reserva.setCliente(cliente);
        reserva.setMesa(mesa);
        reserva.setFecha(form.getFecha());
        reserva.setHora(form.getHora());
        reserva.setPersonas(form.getPersonas());
        reserva.setNotas(form.getNotas());
        return reserva;
    }

    @Transactional
    public Reserva cancelar(String codigo) {
        Reserva reserva = obtener(codigo);
        if (!reserva.isModificable()) {
            throw new ReglaNegocioException("Esta reserva ya no se puede cancelar");
        }
        reserva.setEstado(EstadoReserva.CANCELADA);
        return reserva;
    }

    @Transactional
    public Reserva cambiarEstado(Long id, EstadoReserva estado) {
        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new ReglaNegocioException("La reserva indicada no existe"));
        reserva.setEstado(estado);
        return reserva;
    }

    @Transactional(readOnly = true)
    public Reserva obtener(String codigo) {
        return reservaRepository.findByCodigoIgnoreCase(codigo)
                .orElseThrow(() -> new ReglaNegocioException("No encontramos una reserva con el código " + codigo));
    }

    /** Busca por código de reserva o por correo electrónico del cliente. */
    @Transactional(readOnly = true)
    public List<Reserva> buscar(String consulta) {
        if (consulta == null || consulta.isBlank()) {
            return List.of();
        }
        String texto = consulta.trim();
        if (texto.contains("@")) {
            return reservaRepository.findByClienteEmailIgnoreCaseOrderByFechaDescHoraDesc(texto);
        }
        return reservaRepository.findByCodigoIgnoreCase(texto)
                .map(List::of)
                .orElse(List.of());
    }

    @Transactional(readOnly = true)
    public List<Reserva> reservasDelDia(LocalDate fecha) {
        return reservaRepository.findByFechaOrderByHoraAsc(fecha);
    }

    @Transactional(readOnly = true)
    public List<Reserva> reservasDelDia(LocalDate fecha, EstadoReserva estado) {
        return reservaRepository.findByFechaAndEstadoOrderByHoraAsc(fecha, estado);
    }

    @Transactional(readOnly = true)
    public List<Reserva> proximasReservas() {
        return reservaRepository
                .findByFechaGreaterThanEqualAndEstadoNotOrderByFechaAscHoraAsc(LocalDate.now(), EstadoReserva.CANCELADA);
    }

    @Transactional(readOnly = true)
    public long reservasActivasDelDia(LocalDate fecha) {
        return reservaRepository.countByFechaAndEstadoNot(fecha, EstadoReserva.CANCELADA);
    }

    @Transactional(readOnly = true)
    public long comensalesDelDia(LocalDate fecha) {
        return reservaRepository.findByFechaAndEstadoNotOrderByHoraAsc(fecha, EstadoReserva.CANCELADA)
                .stream()
                .mapToLong(Reserva::getPersonas)
                .sum();
    }

    @Transactional(readOnly = true)
    public long totalConfirmadas() {
        return reservaRepository.countByEstado(EstadoReserva.CONFIRMADA);
    }

    @Transactional(readOnly = true)
    public long totalCanceladas() {
        return reservaRepository.countByEstado(EstadoReserva.CANCELADA);
    }

    private boolean sinSolapamiento(Mesa mesa, LocalDate fecha, LocalTime hora, Long reservaExcluidaId) {
        return reservaRepository
                .findByMesaIdAndFechaAndEstadoNot(mesa.getId(), fecha, EstadoReserva.CANCELADA)
                .stream()
                .filter(reserva -> reservaExcluidaId == null || !reserva.getId().equals(reservaExcluidaId))
                .noneMatch(reserva -> reserva.solapaCon(fecha, hora));
    }

    private Mesa mesaActiva(Long mesaId) {
        Mesa mesa = mesaRepository.findById(mesaId)
                .orElseThrow(() -> new ReglaNegocioException("Selecciona una mesa disponible"));
        if (!mesa.isActiva()) {
            throw new ReglaNegocioException("La mesa seleccionada ya no está disponible");
        }
        return mesa;
    }

    private void validarDisponibilidad(Mesa mesa, LocalDate fecha, LocalTime hora, int personas, Long reservaExcluidaId) {
        if (mesa.getCapacidad() < personas) {
            throw new ReglaNegocioException("La mesa " + mesa.getNumero() + " tiene capacidad para "
                    + mesa.getCapacidad() + " personas");
        }
        if (!sinSolapamiento(mesa, fecha, hora, reservaExcluidaId)) {
            throw new ReglaNegocioException("La mesa " + mesa.getNumero()
                    + " ya está reservada cerca de esa hora. Elige otra mesa u horario.");
        }
    }

    private void validarDatos(LocalDate fecha, LocalTime hora, int personas) {
        LocalDate hoy = LocalDate.now();
        if (fecha == null || hora == null) {
            throw new ReglaNegocioException("Completa la fecha y la hora de la reserva");
        }
        if (fecha.isBefore(hoy)) {
            throw new ReglaNegocioException("No se pueden reservar fechas pasadas");
        }
        if (fecha.isAfter(hoy.plusDays(DIAS_MAXIMOS_ANTICIPACION))) {
            throw new ReglaNegocioException("Solo aceptamos reservas con hasta "
                    + DIAS_MAXIMOS_ANTICIPACION + " días de anticipación");
        }
        if (!Agenda.esHoraValida(hora)) {
            throw new ReglaNegocioException("Elige un horario dentro de nuestros turnos de servicio");
        }
        if (fecha.equals(hoy) && hora.isBefore(LocalTime.now())) {
            throw new ReglaNegocioException("Ese horario de hoy ya pasó. Elige uno posterior.");
        }
        if (personas < 1 || personas > 12) {
            throw new ReglaNegocioException("Las reservas en línea son de 1 a 12 personas");
        }
    }

    private String generarCodigo() {
        for (int intento = 0; intento < 20; intento++) {
            StringBuilder codigo = new StringBuilder("SG-");
            for (int i = 0; i < 6; i++) {
                codigo.append(ALFABETO_CODIGO.charAt(RANDOM.nextInt(ALFABETO_CODIGO.length())));
            }
            String valor = codigo.toString().toUpperCase(Locale.ROOT);
            if (!reservaRepository.existsByCodigo(valor)) {
                return valor;
            }
        }
        throw new ReglaNegocioException("No pudimos generar tu código de reserva, intenta nuevamente");
    }
}
