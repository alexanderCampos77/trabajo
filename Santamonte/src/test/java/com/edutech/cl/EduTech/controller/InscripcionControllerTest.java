package com.edutech.cl.EduTech.controller;

import com.edutech.cl.EduTech.model.Inscripcion;
import com.edutech.cl.EduTech.model.Usuario;
import com.edutech.cl.EduTech.service.InscripcionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test") // Activa el perfil de test
class InscripcionControllerTest {

    @Mock
    private InscripcionService inscripcionService;

    @InjectMocks
    private InscripcionController inscripcionController;

    private Inscripcion inscripcion1;
    private Inscripcion inscripcion2;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setIdUsuario(1);

        inscripcion1 = new Inscripcion();
        inscripcion1.setId(1);
        inscripcion1.setUsuario(usuario);

        inscripcion2 = new Inscripcion();
        inscripcion2.setId(2);
        inscripcion2.setUsuario(usuario);
    }

    @Test
    void listarTodas_DeberiaRetornarListaDeInscripciones() {
        // Configuración para Java 21 con tipos más específicos
        var inscripciones = List.of(inscripcion1, inscripcion2);
        when(inscripcionService.obtenerTodas()).thenReturn(inscripciones);

        var response = inscripcionController.listarTodas();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().getContent().size());

        verify(inscripcionService).obtenerTodas();
    }

    @Test
    void listarPorUsuario_DeberiaRetornarInscripcionesDelUsuario() {
        when(inscripcionService.obtenerPorUsuario(anyInt()))
                .thenReturn(List.of(inscripcion1, inscripcion2));

        var response = inscripcionController.listarPorUsuario(1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().getContent().size());
    }

    @Test
    void inscribir_DeberiaCrearInscripcionExitosamente() {
        when(inscripcionService.inscribirUsuario(anyInt(), anyInt()))
                .thenReturn(inscripcion1);

        var response = inscripcionController.inscribir(1, 1);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertInstanceOf(EntityModel.class, response.getBody());
    }

    @Test
    void inscribir_DeberiaManejarConflictos() {
        when(inscripcionService.inscribirUsuario(anyInt(), anyInt()))
                .thenThrow(new IllegalStateException("No hay cupos"));

        var response = inscripcionController.inscribir(1, 1);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("No hay cupos"));
    }

    @Test
    void buscarInscripcion_DeberiaRetornarInscripcionExistente() {
        when(inscripcionService.obtenerPorId(anyInt())).thenReturn(inscripcion1);

        var response = inscripcionController.buscarInscripcion(1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getContent().getId());
    }

    @Test
    void actualizarInscripcion_DeberiaActualizarExitosamente() {
        when(inscripcionService.actualizarInscripcionPorIds(anyInt(), any(), any()))
                .thenReturn(inscripcion1);

        var response = inscripcionController.actualizarInscripcion(1, 2, 3);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertInstanceOf(EntityModel.class, response.getBody());
    }

    @Test
    void eliminarInscripcion_DeberiaEliminarExitosamente() {
        doNothing().when(inscripcionService).eliminarInscripcion(anyInt());

        var response = inscripcionController.eliminarInscripcion(1);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }
}