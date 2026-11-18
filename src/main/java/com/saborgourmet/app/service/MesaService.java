package com.saborgourmet.app.service;

import com.saborgourmet.app.dto.MesaForm;
import com.saborgourmet.app.model.Mesa;
import com.saborgourmet.app.repository.MesaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MesaService {

    private final MesaRepository mesaRepository;

    public MesaService(MesaRepository mesaRepository) {
        this.mesaRepository = mesaRepository;
    }

    @Transactional(readOnly = true)
    public List<Mesa> listarTodas() {
        return mesaRepository.findAllByOrderByNumeroAsc();
    }

    @Transactional(readOnly = true)
    public List<Mesa> listarActivas() {
        return mesaRepository.findByActivaTrueOrderByNumeroAsc();
    }

    @Transactional(readOnly = true)
    public Mesa obtener(Long id) {
        return mesaRepository.findById(id)
                .orElseThrow(() -> new ReglaNegocioException("La mesa indicada no existe"));
    }

    @Transactional
    public Mesa crear(MesaForm form) {
        if (mesaRepository.existsByNumero(form.getNumero())) {
            throw new ReglaNegocioException("Ya existe una mesa con el número " + form.getNumero());
        }
        Mesa mesa = new Mesa(form.getNumero(), form.getCapacidad(), form.getZona(), form.getDescripcion());
        mesa.setActiva(form.isActiva());
        return mesaRepository.save(mesa);
    }

    @Transactional
    public Mesa actualizar(Long id, MesaForm form) {
        Mesa mesa = obtener(id);
        if (mesaRepository.existsByNumeroAndIdNot(form.getNumero(), id)) {
            throw new ReglaNegocioException("Ya existe otra mesa con el número " + form.getNumero());
        }
        mesa.setNumero(form.getNumero());
        mesa.setCapacidad(form.getCapacidad());
        mesa.setZona(form.getZona());
        mesa.setDescripcion(form.getDescripcion());
        mesa.setActiva(form.isActiva());
        return mesa;
    }

    /** No se eliminan mesas con historial de reservas: se desactivan. */
    @Transactional
    public Mesa alternarActiva(Long id) {
        Mesa mesa = obtener(id);
        mesa.setActiva(!mesa.isActiva());
        return mesa;
    }

    @Transactional(readOnly = true)
    public long contarActivas() {
        return mesaRepository.countByActivaTrue();
    }

    public MesaForm aFormulario(Mesa mesa) {
        MesaForm form = new MesaForm();
        form.setNumero(mesa.getNumero());
        form.setCapacidad(mesa.getCapacidad());
        form.setZona(mesa.getZona());
        form.setDescripcion(mesa.getDescripcion());
        form.setActiva(mesa.isActiva());
        return form;
    }
}
