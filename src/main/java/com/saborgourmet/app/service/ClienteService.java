package com.saborgourmet.app.service;

import com.saborgourmet.app.model.Cliente;
import com.saborgourmet.app.repository.ClienteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    /**
     * Reutiliza el cliente ya registrado con ese correo y actualiza sus datos de
     * contacto, para no duplicar personas en cada reserva.
     */
    @Transactional
    public Cliente obtenerOCrear(String nombre, String email, String telefono) {
        String emailNormalizado = email.trim().toLowerCase();
        Cliente cliente = clienteRepository.findByEmailIgnoreCase(emailNormalizado)
                .orElseGet(() -> new Cliente(nombre.trim(), emailNormalizado, telefono));
        cliente.setNombre(nombre.trim());
        cliente.setTelefono(telefono);
        return clienteRepository.save(cliente);
    }
}
