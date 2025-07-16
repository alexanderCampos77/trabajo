package com.edutech.cl.EduTech.service;

import com.edutech.cl.EduTech.model.Cursos;
import com.edutech.cl.EduTech.model.Inscripcion;
import com.edutech.cl.EduTech.model.Usuario;
import com.edutech.cl.EduTech.repository.CursosRepository;
import com.edutech.cl.EduTech.repository.InscripcionRepository;
import com.edutech.cl.EduTech.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class InscripcionServiceTest {

    @Mock
    private InscripcionRepository inscripcionRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private CursosRepository cursosRepository;

    @InjectMocks
    private InscripcionService inscripcionService;

    private Usuario usuario;
    private Cursos curso;
    private Inscripcion inscripcion;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setIdUsuario(1);
        usuario.setCursos("");

        curso = new Cursos();
        curso.setIdCurso(1);
        curso.setNombreCurso("Matemáticas");
        curso.setCuposDisponibles(10);

        inscripcion = new Inscripcion();
        inscripcion.setId(1);
        inscripcion.setUsuario(usuario);
        inscripcion.setCurso(curso);
        inscripcion.setFechaInscripcion(LocalDate.now());
    }

    @Test
    void obtenerTodas_ShouldReturnAllInscripciones() {
        // Arrange
        when(inscripcionRepository.findAll()).thenReturn(Collections.singletonList(inscripcion));

        // Act
        List<Inscripcion> result = inscripcionService.obtenerTodas();

        // Assert
        assertEquals(1, result.size());
        assertEquals(inscripcion, result.get(0));
        verify(inscripcionRepository, times(1)).findAll();
    }

    @Test
    void obtenerPorUsuario_ShouldReturnUserInscripciones() {
        // Arrange
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuario));
        when(inscripcionRepository.findByUsuario(usuario)).thenReturn(Collections.singletonList(inscripcion));

        // Act
        List<Inscripcion> result = inscripcionService.obtenerPorUsuario(1);

        // Assert
        assertEquals(1, result.size());
        assertEquals(inscripcion, result.get(0));
        verify(usuarioRepository, times(1)).findById(1);
        verify(inscripcionRepository, times(1)).findByUsuario(usuario);
    }

    @Test
    void obtenerPorUsuario_ShouldThrowWhenUserNotFound() {
        // Arrange
        when(usuarioRepository.findById(1)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> inscripcionService.obtenerPorUsuario(1));
        verify(usuarioRepository, times(1)).findById(1);
    }

    @Test
    void obtenerPorId_ShouldReturnInscripcion() {
        // Arrange
        when(inscripcionRepository.findById(1)).thenReturn(Optional.of(inscripcion));

        // Act
        Inscripcion result = inscripcionService.obtenerPorId(1);

        // Assert
        assertEquals(inscripcion, result);
        verify(inscripcionRepository, times(1)).findById(1);
    }

    @Test
    void obtenerPorId_ShouldThrowWhenInscripcionNotFound() {
        // Arrange
        when(inscripcionRepository.findById(1)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> inscripcionService.obtenerPorId(1));
        verify(inscripcionRepository, times(1)).findById(1);
    }

    @Test
    void inscribirUsuario_ShouldCreateInscripcion() {
        // Arrange
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuario));
        when(cursosRepository.findById(1)).thenReturn(Optional.of(curso));
        when(inscripcionRepository.existsByUsuarioAndCurso(usuario, curso)).thenReturn(false);
        when(inscripcionRepository.save(any(Inscripcion.class))).thenReturn(inscripcion);

        // Act
        Inscripcion result = inscripcionService.inscribirUsuario(1, 1);

        // Assert
        assertNotNull(result);
        assertEquals(usuario, result.getUsuario());
        assertEquals(curso, result.getCurso());
        assertEquals(LocalDate.now(), result.getFechaInscripcion());
        verify(usuarioRepository, times(1)).findById(1);
        verify(cursosRepository, times(1)).findById(1);
        verify(inscripcionRepository, times(1)).existsByUsuarioAndCurso(usuario, curso);
        verify(inscripcionRepository, times(1)).save(any(Inscripcion.class));
        verify(cursosRepository, times(1)).save(curso);
    }

    @Test
    void inscribirUsuario_ShouldThrowWhenUserNotFound() {
        // Arrange
        when(usuarioRepository.findById(1)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> inscripcionService.inscribirUsuario(1, 1));
        verify(usuarioRepository, times(1)).findById(1);
    }

    @Test
    void inscribirUsuario_ShouldThrowWhenCourseNotFound() {
        // Arrange
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuario));
        when(cursosRepository.findById(1)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> inscripcionService.inscribirUsuario(1, 1));
        verify(usuarioRepository, times(1)).findById(1);
        verify(cursosRepository, times(1)).findById(1);
    }

    @Test
    void inscribirUsuario_ShouldThrowWhenUserAlreadyEnrolled() {
        // Arrange
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuario));
        when(cursosRepository.findById(1)).thenReturn(Optional.of(curso));
        when(inscripcionRepository.existsByUsuarioAndCurso(usuario, curso)).thenReturn(true);

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> inscripcionService.inscribirUsuario(1, 1));
        verify(usuarioRepository, times(1)).findById(1);
        verify(cursosRepository, times(1)).findById(1);
        verify(inscripcionRepository, times(1)).existsByUsuarioAndCurso(usuario, curso);
    }

    @Test
    void inscribirUsuario_ShouldThrowWhenNoAvailableSlots() {
        // Arrange
        curso.setCuposDisponibles(0);
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuario));
        when(cursosRepository.findById(1)).thenReturn(Optional.of(curso));
        when(inscripcionRepository.existsByUsuarioAndCurso(usuario, curso)).thenReturn(false);

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> inscripcionService.inscribirUsuario(1, 1));
        verify(usuarioRepository, times(1)).findById(1);
        verify(cursosRepository, times(1)).findById(1);
        verify(inscripcionRepository, times(1)).existsByUsuarioAndCurso(usuario, curso);
    }

    @Test
    void actualizarInscripcion_ShouldUpdateInscripcion() {
        // Arrange
        Inscripcion updatedInscripcion = new Inscripcion();
        updatedInscripcion.setFechaInscripcion(LocalDate.now().minusDays(1));

        when(inscripcionRepository.findById(1)).thenReturn(Optional.of(inscripcion));
        when(inscripcionRepository.save(any(Inscripcion.class))).thenReturn(inscripcion);

        // Act
        Inscripcion result = inscripcionService.actualizarInscripcion(1, updatedInscripcion);

        // Assert
        assertEquals(LocalDate.now().minusDays(1), result.getFechaInscripcion());
        verify(inscripcionRepository, times(1)).findById(1);
        verify(inscripcionRepository, times(1)).save(any(Inscripcion.class));
    }

    @Test
    void actualizarInscripcionPorIds_ShouldUpdateUser() {
        // Arrange
        Usuario newUser = new Usuario();
        newUser.setIdUsuario(2);
        newUser.setCursos("");

        when(inscripcionRepository.findById(1)).thenReturn(Optional.of(inscripcion));
        when(usuarioRepository.findById(2)).thenReturn(Optional.of(newUser));
        when(inscripcionRepository.existsByUsuarioAndCurso(newUser, curso)).thenReturn(false);
        when(inscripcionRepository.save(any(Inscripcion.class))).thenReturn(inscripcion);

        // Act
        Inscripcion result = inscripcionService.actualizarInscripcionPorIds(1, 2, null);

        // Assert
        assertEquals(newUser, result.getUsuario());
        verify(inscripcionRepository, times(1)).findById(1);
        verify(usuarioRepository, times(1)).findById(2);
        verify(inscripcionRepository, times(1)).existsByUsuarioAndCurso(newUser, curso);
        verify(inscripcionRepository, times(1)).save(any(Inscripcion.class));
    }

    @Test
    void actualizarInscripcionPorIds_ShouldUpdateCourse() {
        // Arrange
        Cursos newCourse = new Cursos();
        newCourse.setIdCurso(2);
        newCourse.setNombreCurso("Física");
        newCourse.setCuposDisponibles(5);

        when(inscripcionRepository.findById(1)).thenReturn(Optional.of(inscripcion));
        when(cursosRepository.findById(2)).thenReturn(Optional.of(newCourse));
        when(inscripcionRepository.existsByUsuarioAndCurso(usuario, newCourse)).thenReturn(false);
        when(inscripcionRepository.save(any(Inscripcion.class))).thenReturn(inscripcion);

        // Act
        Inscripcion result = inscripcionService.actualizarInscripcionPorIds(1, null, 2);

        // Assert
        assertEquals(newCourse, result.getCurso());
        verify(inscripcionRepository, times(1)).findById(1);
        verify(cursosRepository, times(1)).findById(2);
        verify(inscripcionRepository, times(1)).existsByUsuarioAndCurso(usuario, newCourse);
        verify(inscripcionRepository, times(1)).save(any(Inscripcion.class));
        verify(cursosRepository, times(1)).save(curso); // Para devolver cupo al curso anterior
        verify(cursosRepository, times(1)).save(newCourse); // Para descontar cupo del nuevo curso
    }

    @Test
    void eliminarInscripcion_ShouldDeleteInscripcion() {
        // Arrange
        when(inscripcionRepository.findById(1)).thenReturn(Optional.of(inscripcion));
        doNothing().when(inscripcionRepository).delete(inscripcion);

        // Act
        inscripcionService.eliminarInscripcion(1);

        // Assert
        verify(inscripcionRepository, times(1)).findById(1);
        verify(cursosRepository, times(1)).save(curso); // Para devolver el cupo
        verify(inscripcionRepository, times(1)).delete(inscripcion);
    }

    @Test
    void limpiarStringCursos_ShouldCleanStringProperly() {
        // Arrange
        String input = "  Matemáticas,, , Física, , Química  ";

        // Act
        String result = inscripcionService.limpiarStringCursos(input);

        // Assert
        assertEquals("Matemáticas, Física, Química", result);
    }
}