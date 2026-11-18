package com.saborgourmet.app.config;

import com.saborgourmet.app.model.Cliente;
import com.saborgourmet.app.model.EstadoReserva;
import com.saborgourmet.app.model.Mesa;
import com.saborgourmet.app.model.Reserva;
import com.saborgourmet.app.model.ZonaMesa;
import com.saborgourmet.app.repository.ClienteRepository;
import com.saborgourmet.app.repository.MesaRepository;
import com.saborgourmet.app.repository.ReservaRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Datos de demostración: solo se cargan cuando la base de datos está vacía, para
 * que la aplicación se pueda probar y evaluar sin carga manual.
 */
@Configuration
public class DataInitializer {

    @Bean
    @Transactional
    CommandLineRunner cargarDatosDemo(MesaRepository mesas,
                                      ClienteRepository clientes,
                                      ReservaRepository reservas) {
        return args -> {
            if (mesas.count() > 0) {
                return;
            }

            List<Mesa> mesasDemo = mesas.saveAll(List.of(
                    new Mesa(1, 2, ZonaMesa.SALON),
                    new Mesa(2, 4, ZonaMesa.SALON),
                    new Mesa(3, 5, ZonaMesa.TERRAZA)
            ));

            List<Cliente> clientesDemo = clientes.saveAll(List.of(
                    new Cliente("María Fernández", "maria.fernandez@example.com", "+56 9 6123 4567"),
                    new Cliente("Diego Rojas", "diego.rojas@example.com", "+56 9 7345 8890"),
                    new Cliente("Camila Soto", "camila.soto@example.com", "+56 9 8456 1122")
            ));

            LocalDate hoy = LocalDate.now();
            LocalDate manana = hoy.plusDays(1);

            reservas.saveAll(List.of(
                    reserva("SG-A1B2C3", clientesDemo.get(0), mesasDemo.get(1), hoy, LocalTime.of(20, 0), 2,
                            EstadoReserva.CONFIRMADA, "Aniversario: preparar mesa con vela."),
                    reserva("SG-D4E5F6", clientesDemo.get(1), mesasDemo.get(2), hoy, LocalTime.of(21, 0), 5,
                            EstadoReserva.CONFIRMADA, null),
                    reserva("SG-G7H8J9", clientesDemo.get(2), mesasDemo.get(0), hoy, LocalTime.of(13, 0), 2,
                            EstadoReserva.COMPLETADA, null),
                    reserva("SG-K2L3M4", clientesDemo.get(0), mesasDemo.get(0), manana, LocalTime.of(13, 0), 2,
                            EstadoReserva.CONFIRMADA, null),
                    reserva("SG-N5P6Q7", clientesDemo.get(1), mesasDemo.get(2), manana, LocalTime.of(19, 0), 4,
                            EstadoReserva.CONFIRMADA, "Celebración de cumpleaños, llevar torta."),
                    reserva("SG-R8S9T2", clientesDemo.get(2), mesasDemo.get(1), manana, LocalTime.of(21, 0), 2,
                            EstadoReserva.CANCELADA, "Cancelada por el cliente.")
            ));
        };
    }

    private Reserva reserva(String codigo, Cliente cliente, Mesa mesa, LocalDate fecha, LocalTime hora,
                            int personas, EstadoReserva estado, String notas) {
        Reserva reserva = new Reserva(codigo, cliente, mesa, fecha, hora, personas);
        reserva.setEstado(estado);
        reserva.setNotas(notas);
        return reserva;
    }
}
