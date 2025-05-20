package com.edutech.inscripcion.repository;

import com.edutech.inscripcion.model.Inscripcion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InscripcionRepository extends JpaRepository<Inscripcion, Long> {
    List<Inscripcion> findByUsuarioId(Long usuarioId);
    List<Inscripcion> findByCursoId(Long cursoId);
}
