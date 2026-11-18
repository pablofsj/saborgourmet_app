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
                    new Mesa(1, 2, ZonaMesa.VENTANA, "Mesa íntima con vista a la plaza"),
                    new Mesa(2, 2, ZonaMesa.SALON, "Mesa para dos junto al mural"),
                    new Mesa(3, 4, ZonaMesa.SALON, "Mesa familiar en el centro del salón"),
                    new Mesa(4, 4, ZonaMesa.VENTANA, "Mesa con luz natural de tarde"),
                    new Mesa(5, 4, ZonaMesa.TERRAZA, "Terraza cubierta, ideal para sobremesa"),
                    new Mesa(6, 6, ZonaMesa.SALON, "Mesa larga bajo las lámparas de papel"),
                    new Mesa(7, 6, ZonaMesa.TERRAZA, "Terraza con vista al jardín"),
                    new Mesa(8, 8, ZonaMesa.PRIVADO, "Sala privada con biombo geométrico"),
                    new Mesa(9, 10, ZonaMesa.PRIVADO, "Sala privada para celebraciones"),
                    new Mesa(10, 12, ZonaMesa.SALON, "Mesa principal para grupos grandes")
            ));

            List<Cliente> clientesDemo = clientes.saveAll(List.of(
                    new Cliente("María Fernández", "maria.fernandez@example.com", "+56 9 6123 4567"),
                    new Cliente("Diego Rojas", "diego.rojas@example.com", "+56 9 7345 8890"),
                    new Cliente("Camila Soto", "camila.soto@example.com", "+56 9 8456 1122"),
                    new Cliente("Andrés Muñoz", "andres.munoz@example.com", "+56 9 5567 3344")
            ));

            LocalDate hoy = LocalDate.now();
            LocalDate manana = hoy.plusDays(1);

            reservas.saveAll(List.of(
                    reserva("SG-A1B2C3", clientesDemo.get(0), mesasDemo.get(0), hoy, LocalTime.of(20, 0), 2,
                            EstadoReserva.CONFIRMADA, "Aniversario: preparar mesa con vela."),
                    reserva("SG-D4E5F6", clientesDemo.get(1), mesasDemo.get(2), hoy, LocalTime.of(20, 30), 4,
                            EstadoReserva.CONFIRMADA, null),
                    reserva("SG-G7H8J9", clientesDemo.get(2), mesasDemo.get(5), hoy, LocalTime.of(21, 0), 5,
                            EstadoReserva.CONFIRMADA, "Una persona con dieta vegetariana."),
                    reserva("SG-K2L3M4", clientesDemo.get(3), mesasDemo.get(7), hoy, LocalTime.of(13, 0), 6,
                            EstadoReserva.COMPLETADA, null),
                    reserva("SG-N5P6Q7", clientesDemo.get(0), mesasDemo.get(1), hoy, LocalTime.of(13, 30), 2,
                            EstadoReserva.CANCELADA, "Cancelada por el cliente."),
                    reserva("SG-R8S9T2", clientesDemo.get(1), mesasDemo.get(8), manana, LocalTime.of(20, 0), 9,
                            EstadoReserva.CONFIRMADA, "Celebración de cumpleaños, llevar torta."),
                    reserva("SG-U3V4W5", clientesDemo.get(2), mesasDemo.get(4), manana, LocalTime.of(13, 0), 4,
                            EstadoReserva.CONFIRMADA, null),
                    reserva("SG-X6Y7Z8", clientesDemo.get(3), mesasDemo.get(3), manana, LocalTime.of(21, 0), 3,
                            EstadoReserva.CONFIRMADA, null)
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
