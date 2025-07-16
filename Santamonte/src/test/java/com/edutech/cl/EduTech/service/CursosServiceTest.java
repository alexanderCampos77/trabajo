package com.edutech.cl.EduTech.service;

import com.edutech.cl.EduTech.model.Cursos;
import com.edutech.cl.EduTech.repository.CursosRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class CursosServiceTest {

    @Mock
    private CursosRepository cursosRepository;

    @InjectMocks
    private CursosService cursosService;

    private Cursos crearCursoEjemplo() {
        return new Cursos(
                1,
                "Programación en Java",
                199.90,
                "8 semanas",
                25
        );
    }

    @Test
    void findAll_DeberiaRetornarTodosLosCursos() {
        // Arrange
        Cursos curso1 = crearCursoEjemplo();
        Cursos curso2 = new Cursos(2, "Spring Boot Avanzado", 299.90, "6 semanas", 15);

        when(cursosRepository.findAll()).thenReturn(Arrays.asList(curso1, curso2));

        // Act
        List<Cursos> resultado = cursosService.findAll();

        // Assert
        assertEquals(2, resultado.size());
        assertEquals("Programación en Java", resultado.get(0).getNombreCurso());
        assertEquals(15, resultado.get(1).getCuposDisponibles());
        verify(cursosRepository, times(1)).findAll();
    }

    @Test
    void findById_CursoExistente_DeberiaRetornarCurso() {
        // Arrange
        Cursos curso = crearCursoEjemplo();
        when(cursosRepository.findById(1)).thenReturn(Optional.of(curso));

        // Act
        Cursos resultado = cursosService.findById(1);

        // Assert
        assertNotNull(resultado);
        assertEquals(199.90, resultado.getPrecioSub());
        assertEquals("8 semanas", resultado.getDuracionCurso());
        verify(cursosRepository, times(1)).findById(1);
    }

    @Test
    void findById_CursoNoExistente_DeberiaLanzarExcepcion() {
        // Arrange
        when(cursosRepository.findById(99)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> cursosService.findById(99));
        verify(cursosRepository, times(1)).findById(99);
    }

    @Test
    void save_DeberiaGuardarCursoCorrectamente() {
        // Arrange
        Cursos nuevoCurso = new Cursos(null, "Angular Básico", 149.90, "4 semanas", 30);
        Cursos cursoGuardado = new Cursos(3, "Angular Básico", 149.90, "4 semanas", 30);

        when(cursosRepository.save(nuevoCurso)).thenReturn(cursoGuardado);

        // Act
        Cursos resultado = cursosService.save(nuevoCurso);

        // Assert
        assertNotNull(resultado.getIdCurso());
        assertEquals(30, resultado.getCuposDisponibles());
        verify(cursosRepository, times(1)).save(nuevoCurso);
    }

    @Test
    void delete_DeberiaEliminarCurso() {
        // Act
        cursosService.delete(1);

        // Assert
        verify(cursosRepository, times(1)).deleteById(1);
    }

    // Pruebas adicionales recomendadas
    @Test
    void save_CursoSinNombre_DeberiaLanzarExcepcion() {
        Cursos cursoInvalido = new Cursos(null, null, 99.90, "2 semanas", 10);

        assertThrows(RuntimeException.class, () -> cursosService.save(cursoInvalido));
    }

    @Test
    void save_CursoConPrecioNegativo_DeberiaLanzarExcepcion() {
        Cursos cursoInvalido = new Cursos(null, "Curso Inválido", -50.0, "1 semana", 5);

        assertThrows(RuntimeException.class, () -> cursosService.save(cursoInvalido));
    }

    @Test
    void save_CursoConCuposNegativos_DeberiaLanzarExcepcion() {
        Cursos cursoInvalido = new Cursos(null, "Curso Inválido", 100.0, "1 semana", -5);

        assertThrows(RuntimeException.class, () -> cursosService.save(cursoInvalido));
    }
}