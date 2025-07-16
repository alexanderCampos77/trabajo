package com.edutech.cl.EduTech.repository;

import com.edutech.cl.EduTech.model.Cursos;
import com.edutech.cl.EduTech.model.Inscripcion;
import com.edutech.cl.EduTech.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InscripcionRepository extends JpaRepository<Inscripcion, Integer> {

    // Método existente
    boolean existsByUsuarioAndCurso(Usuario usuario, Cursos curso);

    // Método existente
    List<Inscripcion> findByUsuario(Usuario usuario);

    // Métodos adicionales útiles
    List<Inscripcion> findByCurso(Cursos curso);

    // Contar inscripciones por curso
    long countByCurso(Cursos curso);

    // Contar inscripciones por usuario
    long countByUsuario(Usuario usuario);

    // Buscar inscripciones por ID de usuario (útil para el controlador)
    @Query("SELECT i FROM Inscripcion i WHERE i.usuario.idUsuario = :usuarioId")
    List<Inscripcion> findByUsuarioIdUsuario(@Param("usuarioId") Integer usuarioId);

    // Buscar inscripciones por ID de curso
    @Query("SELECT i FROM Inscripcion i WHERE i.curso.idCurso = :cursoId")
    List<Inscripcion> findByCursoId(@Param("cursoId") Integer cursoId);

    // Verificar existencia por IDs (más eficiente para validaciones)
    @Query("SELECT CASE WHEN COUNT(i) > 0 THEN true ELSE false END FROM Inscripcion i WHERE i.usuario.idUsuario = :usuarioId AND i.curso.idCurso = :cursoId")
    boolean existsByUsuarioIdUsuarioAndCursoIdCurso(@Param("usuarioId") Integer usuarioId, @Param("cursoId") Integer cursoId);

    // Obtener inscripción específica por usuario y curso
    Optional<Inscripcion> findByUsuarioAndCurso(Usuario usuario, Cursos curso);

    // Obtener inscripciones ordenadas por fecha
    List<Inscripcion> findAllByOrderByFechaInscripcionDesc();

    // Obtener inscripciones de un usuario ordenadas por fecha
    List<Inscripcion> findByUsuarioOrderByFechaInscripcionDesc(Usuario usuario);
}