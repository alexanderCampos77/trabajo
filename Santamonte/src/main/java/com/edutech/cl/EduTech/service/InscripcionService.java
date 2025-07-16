package com.edutech.cl.EduTech.service;

import com.edutech.cl.EduTech.model.Cursos;
import com.edutech.cl.EduTech.model.Inscripcion;
import com.edutech.cl.EduTech.model.Usuario;
import com.edutech.cl.EduTech.repository.CursosRepository;
import com.edutech.cl.EduTech.repository.InscripcionRepository;
import com.edutech.cl.EduTech.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class InscripcionService {

    @Autowired
    private InscripcionRepository inscripcionRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private CursosRepository cursosRepository;

    public List<Inscripcion> obtenerTodas() {
        return inscripcionRepository.findAll();
    }

    public List<Inscripcion> obtenerPorUsuario(Integer usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + usuarioId));
        return inscripcionRepository.findByUsuario(usuario);
    }

    public Inscripcion obtenerPorId(Integer inscripcionId) {
        return inscripcionRepository.findById(inscripcionId)
                .orElseThrow(() -> new IllegalArgumentException("Inscripción no encontrada con ID: " + inscripcionId));
    }

    @Transactional
    public Inscripcion inscribirUsuario(Integer usuarioId, Integer cursoId) {
        // Validar parámetros de entrada
        if (usuarioId == null || cursoId == null) {
            throw new IllegalArgumentException("El ID del usuario y del curso son obligatorios");
        }

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + usuarioId));

        Cursos curso = cursosRepository.findById(cursoId)
                .orElseThrow(() -> new IllegalArgumentException("Curso no encontrado con ID: " + cursoId));

        // Verificar si el usuario ya está inscrito en este curso
        if (inscripcionRepository.existsByUsuarioAndCurso(usuario, curso)) {
            throw new IllegalStateException("El usuario ya está inscrito en este curso");
        }

        // Verificar cupos disponibles
        if (curso.getCuposDisponibles() <= 0) {
            throw new IllegalStateException("No hay cupos disponibles para este curso");
        }

        // Descontar cupo
        curso.setCuposDisponibles(curso.getCuposDisponibles() - 1);
        cursosRepository.save(curso);

        // Actualizar campo cursos en Usuario (como texto)
        actualizarCursosUsuario(usuario, curso.getNombreCurso(), true);

        // Crear nueva inscripción
        Inscripcion inscripcion = new Inscripcion();
        inscripcion.setUsuario(usuario);
        inscripcion.setCurso(curso);
        inscripcion.setFechaInscripcion(LocalDate.now());

        return inscripcionRepository.save(inscripcion);
    }

    @Transactional
    public Inscripcion actualizarInscripcion(Integer inscripcionId, Inscripcion inscripcionActualizada) {
        Inscripcion inscripcionExistente = inscripcionRepository.findById(inscripcionId)
                .orElseThrow(() -> new IllegalArgumentException("Inscripción no encontrada con ID: " + inscripcionId));

        // Obtener el curso anterior
        Cursos cursoAnterior = inscripcionExistente.getCurso();

        // Si se proporciona un nuevo curso en la actualización
        if (inscripcionActualizada.getCurso() != null &&
                !cursoAnterior.getIdCurso().equals(inscripcionActualizada.getCurso().getIdCurso())) {

            // Buscar el nuevo curso
            Cursos cursoNuevo = cursosRepository.findById(inscripcionActualizada.getCurso().getIdCurso())
                    .orElseThrow(() -> new IllegalArgumentException("Curso nuevo no encontrado"));

            // Verificar cupos disponibles en el nuevo curso
            if (cursoNuevo.getCuposDisponibles() <= 0) {
                throw new IllegalStateException("No hay cupos disponibles en el nuevo curso");
            }

            // Verificar que el usuario no esté ya inscrito en el nuevo curso
            if (inscripcionRepository.existsByUsuarioAndCurso(inscripcionExistente.getUsuario(), cursoNuevo)) {
                throw new IllegalStateException("El usuario ya está inscrito en el nuevo curso");
            }

            // Devolver cupo al curso anterior
            cursoAnterior.setCuposDisponibles(cursoAnterior.getCuposDisponibles() + 1);
            cursosRepository.save(cursoAnterior);

            // Descontar cupo del nuevo curso
            cursoNuevo.setCuposDisponibles(cursoNuevo.getCuposDisponibles() - 1);
            cursosRepository.save(cursoNuevo);

            // Actualizar el campo cursos en Usuario
            Usuario usuario = inscripcionExistente.getUsuario();
            actualizarCursosUsuario(usuario, cursoAnterior.getNombreCurso(), false);
            actualizarCursosUsuario(usuario, cursoNuevo.getNombreCurso(), true);

            // Actualizar la inscripción
            inscripcionExistente.setCurso(cursoNuevo);
        }

        // Actualizar otros campos si se proporcionan
        if (inscripcionActualizada.getFechaInscripcion() != null) {
            inscripcionExistente.setFechaInscripcion(inscripcionActualizada.getFechaInscripcion());
        }

        return inscripcionRepository.save(inscripcionExistente);
    }

    // Nuevo método para actualizar por IDs
    @Transactional
    public Inscripcion actualizarInscripcionPorIds(Integer inscripcionId, Integer usuarioId, Integer cursoId) {
        Inscripcion inscripcionExistente = inscripcionRepository.findById(inscripcionId)
                .orElseThrow(() -> new IllegalArgumentException("Inscripción no encontrada con ID: " + inscripcionId));

        boolean cambioUsuario = false;
        boolean cambioCurso = false;

        // Actualizar usuario si se proporciona
        if (usuarioId != null && !inscripcionExistente.getUsuario().getIdUsuario().equals(usuarioId)) {
            Usuario nuevoUsuario = usuarioRepository.findById(usuarioId)
                    .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + usuarioId));

            // Verificar que el nuevo usuario no esté ya inscrito en este curso
            if (inscripcionRepository.existsByUsuarioAndCurso(nuevoUsuario, inscripcionExistente.getCurso())) {
                throw new IllegalStateException("El nuevo usuario ya está inscrito en este curso");
            }

            // Remover curso del usuario anterior
            actualizarCursosUsuario(inscripcionExistente.getUsuario(),
                    inscripcionExistente.getCurso().getNombreCurso(), false);

            // Agregar curso al nuevo usuario
            actualizarCursosUsuario(nuevoUsuario,
                    inscripcionExistente.getCurso().getNombreCurso(), true);

            inscripcionExistente.setUsuario(nuevoUsuario);
            cambioUsuario = true;
        }

        // Actualizar curso si se proporciona
        if (cursoId != null && !inscripcionExistente.getCurso().getIdCurso().equals(cursoId)) {
            Cursos cursoAnterior = inscripcionExistente.getCurso();
            Cursos cursoNuevo = cursosRepository.findById(cursoId)
                    .orElseThrow(() -> new IllegalArgumentException("Curso no encontrado con ID: " + cursoId));

            // Verificar cupos disponibles en el nuevo curso
            if (cursoNuevo.getCuposDisponibles() <= 0) {
                throw new IllegalStateException("No hay cupos disponibles en el nuevo curso");
            }

            // Verificar que el usuario no esté ya inscrito en el nuevo curso
            if (inscripcionRepository.existsByUsuarioAndCurso(inscripcionExistente.getUsuario(), cursoNuevo)) {
                throw new IllegalStateException("El usuario ya está inscrito en el nuevo curso");
            }

            // Devolver cupo al curso anterior
            cursoAnterior.setCuposDisponibles(cursoAnterior.getCuposDisponibles() + 1);
            cursosRepository.save(cursoAnterior);

            // Descontar cupo del nuevo curso
            cursoNuevo.setCuposDisponibles(cursoNuevo.getCuposDisponibles() - 1);
            cursosRepository.save(cursoNuevo);

            // Actualizar el campo cursos en Usuario (solo si no se cambió el usuario)
            if (!cambioUsuario) {
                actualizarCursosUsuario(inscripcionExistente.getUsuario(), cursoAnterior.getNombreCurso(), false);
                actualizarCursosUsuario(inscripcionExistente.getUsuario(), cursoNuevo.getNombreCurso(), true);
            }

            inscripcionExistente.setCurso(cursoNuevo);
            cambioCurso = true;
        }

        // Si no se hicieron cambios, lanzar excepción
        if (!cambioUsuario && !cambioCurso) {
            throw new IllegalArgumentException("Debe proporcionar al menos un ID (usuario o curso) para actualizar");
        }

        return inscripcionRepository.save(inscripcionExistente);
    }

    @Transactional
    public void eliminarInscripcion(Integer inscripcionId) {
        Inscripcion inscripcion = inscripcionRepository.findById(inscripcionId)
                .orElseThrow(() -> new IllegalArgumentException("Inscripción no encontrada con ID: " + inscripcionId));

        // Devolver cupo al curso
        Cursos curso = inscripcion.getCurso();
        curso.setCuposDisponibles(curso.getCuposDisponibles() + 1);
        cursosRepository.save(curso);

        // Actualizar el campo cursos en Usuario
        actualizarCursosUsuario(inscripcion.getUsuario(), curso.getNombreCurso(), false);

        // Eliminar la inscripción
        inscripcionRepository.delete(inscripcion);
    }

    // Método auxiliar para actualizar el campo cursos del usuario
    private void actualizarCursosUsuario(Usuario usuario, String nombreCurso, boolean agregar) {
        String cursosActuales = usuario.getCursos() != null ? usuario.getCursos() : "";

        if (agregar) {
            // Agregar curso
            if (cursosActuales.isEmpty()) {
                usuario.setCursos(nombreCurso);
            } else if (!cursosActuales.contains(nombreCurso)) {
                usuario.setCursos(cursosActuales + ", " + nombreCurso);
            }
        } else {
            // Remover curso
            cursosActuales = cursosActuales.replace(nombreCurso, "");
            cursosActuales = limpiarStringCursos(cursosActuales);
            usuario.setCursos(cursosActuales.isEmpty() ? null : cursosActuales);
        }

        usuarioRepository.save(usuario);
    }

    // Método auxiliar para limpiar el string de cursos
    public String limpiarStringCursos(String cursos) {
        if (cursos == null || cursos.trim().isEmpty()) {
            return "";
        }

        // 1. Eliminar espacios extras
        String resultado = cursos.trim()
                // 2. Reemplazar múltiples comas por una sola
                .replaceAll(",{2,}", ",")
                // 3. Eliminar comas con espacios alrededor
                .replaceAll("\\s*,\\s*", ", ")
                // 4. Eliminar comas al inicio/final
                .replaceAll("^,|,$", "")
                // 5. Eliminar espacios múltiples
                .replaceAll("\\s+", " ")
                .trim();

        // 6. Eliminar posibles ", " al inicio/final que hayan quedado
        if (resultado.startsWith(", ")) {
            resultado = resultado.substring(2);
        }
        if (resultado.endsWith(", ")) {
            resultado = resultado.substring(0, resultado.length() - 2);
        }

        // 7. Eliminar cualquier coma solitaria que haya quedado
        resultado = resultado.replaceAll(", ,", ",")
                .replaceAll(" ,", ",")
                .replaceAll(", ", ",")
                .replaceAll(",,", ",");

        // 8. Volver a formatear con ", " entre cursos
        if (!resultado.isEmpty()) {
            resultado = String.join(", ", resultado.split("\\s*,\\s*"));
        }

        return resultado;
    }
}