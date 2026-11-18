package com.saborgourmet.app.repository;

import com.saborgourmet.app.model.EstadoReserva;
import com.saborgourmet.app.model.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    Optional<Reserva> findByCodigoIgnoreCase(String codigo);

    boolean existsByCodigo(String codigo);

    List<Reserva> findByClienteEmailIgnoreCaseOrderByFechaDescHoraDesc(String email);

    List<Reserva> findByFechaOrderByHoraAsc(LocalDate fecha);

    List<Reserva> findByFechaAndEstadoOrderByHoraAsc(LocalDate fecha, EstadoReserva estado);

    List<Reserva> findByFechaAndEstadoNotOrderByHoraAsc(LocalDate fecha, EstadoReserva estado);

    List<Reserva> findByMesaIdAndFechaAndEstadoNot(Long mesaId, LocalDate fecha, EstadoReserva estado);

    List<Reserva> findByFechaGreaterThanEqualAndEstadoNotOrderByFechaAscHoraAsc(
            LocalDate fecha, EstadoReserva estado);

    long countByFechaAndEstadoNot(LocalDate fecha, EstadoReserva estado);

    long countByEstado(EstadoReserva estado);
}
