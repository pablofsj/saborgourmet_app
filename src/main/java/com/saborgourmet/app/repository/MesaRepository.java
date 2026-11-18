package com.saborgourmet.app.repository;

import com.saborgourmet.app.model.Mesa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MesaRepository extends JpaRepository<Mesa, Long> {

    List<Mesa> findAllByOrderByNumeroAsc();

    List<Mesa> findByActivaTrueOrderByNumeroAsc();

    List<Mesa> findByActivaTrueAndCapacidadGreaterThanEqualOrderByCapacidadAscNumeroAsc(Integer capacidad);

    long countByActivaTrue();

    boolean existsByNumero(Integer numero);

    boolean existsByNumeroAndIdNot(Integer numero, Long id);
}
