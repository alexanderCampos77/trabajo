package com.edutech.cl.EduTech.service;

import com.edutech.cl.EduTech.model.Usuario;
import com.edutech.cl.EduTech.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private UsuarioService usuarioService;

    private Date crearFecha(String fecha) throws Exception {
        return new SimpleDateFormat("yyyy-MM-dd").parse(fecha);
    }

    private Usuario crearUsuarioEjemplo() throws Exception {
        return new Usuario(
                1,
                "12345678-9",
                "Juan",
                "Perez",
                crearFecha("1990-05-15"),
                "juan@mail.com",
                "MAT101,MAT102",
                "profesor"
        );
    }

    @Test
    void findAll_DeberiaRetornarTodosLosUsuarios() throws Exception {
        // Arrange
        Usuario usuario1 = crearUsuarioEjemplo();
        Usuario usuario2 = new Usuario(
                2,
                "98765432-1",
                "Maria",
                "Gomez",
                crearFecha("1985-10-20"),
                "maria@mail.com",
                null,
                "estudiante"
        );

        when(usuarioRepository.findAll()).thenReturn(Arrays.asList(usuario1, usuario2));

        // Act
        List<Usuario> resultado = usuarioService.findAll();

        // Assert
        assertEquals(2, resultado.size());
        assertEquals("Juan", resultado.get(0).getNombres());
        assertEquals("Gomez", resultado.get(1).getApellidos());
        verify(usuarioRepository, times(1)).findAll();
    }

    @Test
    void findById_UsuarioExistente_DeberiaRetornarUsuario() throws Exception {
        // Arrange
        Usuario usuario = crearUsuarioEjemplo();
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(usuario));

        // Act
        Usuario resultado = usuarioService.findById(1);

        // Assert
        assertNotNull(resultado);
        assertEquals("12345678-9", resultado.getRun());
        assertEquals("profesor", resultado.getRol());
        verify(usuarioRepository, times(1)).findById(1);
    }

    @Test
    void findById_UsuarioNoExistente_DeberiaLanzarExcepcion() {
        // Arrange
        when(usuarioRepository.findById(99)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> usuarioService.findById(99));
        verify(usuarioRepository, times(1)).findById(99);
    }

    @Test
    void save_DeberiaGuardarUsuarioCorrectamente() throws Exception {
        // Arrange
        Usuario nuevoUsuario = new Usuario(
                null,
                "11222333-4",
                "Ana",
                "Silva",
                crearFecha("1995-03-25"),
                "ana@mail.com",
                null,
                "estudiante"
        );

        Usuario usuarioGuardado = new Usuario(
                3,
                "11222333-4",
                "Ana",
                "Silva",
                crearFecha("1995-03-25"),
                "ana@mail.com",
                null,
                "estudiante"
        );

        when(usuarioRepository.save(nuevoUsuario)).thenReturn(usuarioGuardado);

        // Act
        Usuario resultado = usuarioService.save(nuevoUsuario);

        // Assert
        assertNotNull(resultado.getIdUsuario());
        assertEquals("Silva", resultado.getApellidos());
        verify(usuarioRepository, times(1)).save(nuevoUsuario);
    }

    @Test
    void delete_DeberiaEliminarUsuario() {
        // Act
        usuarioService.delete(1);

        // Assert
        verify(usuarioRepository, times(1)).deleteById(1);
    }

    @Test
    void findByRol_DeberiaRetornarUsuariosDelRol() throws Exception {
        // Arrange
        Usuario profesor1 = crearUsuarioEjemplo();
        Usuario profesor2 = new Usuario(
                2,
                "22222222-2",
                "Carlos",
                "Lopez",
                crearFecha("1980-07-10"),
                "carlos@mail.com",
                "FIS101",
                "profesor"
        );

        when(usuarioRepository.findByRol("profesor")).thenReturn(Arrays.asList(profesor1, profesor2));

        // Act
        List<Usuario> resultado = usuarioService.findByRol("profesor");

        // Assert
        assertEquals(2, resultado.size());
        assertTrue(resultado.stream().allMatch(u -> u.getRol().equals("profesor")));
        verify(usuarioRepository, times(1)).findByRol("profesor");
    }

    @Test
    void findByRun_UsuarioExistente_DeberiaRetornarUsuario() throws Exception {
        // Arrange
        Usuario usuario = crearUsuarioEjemplo();
        when(usuarioRepository.findByRun("12345678-9")).thenReturn(usuario);

        // Act
        Usuario resultado = usuarioService.findByRun("12345678-9");

        // Assert
        assertNotNull(resultado);
        assertEquals("Perez", resultado.getApellidos());
        assertEquals("MAT101,MAT102", resultado.getCursos());
        verify(usuarioRepository, times(1)).findByRun("12345678-9");
    }

    @Test
    void findByRun_UsuarioNoExistente_DeberiaRetornarNull() {
        // Arrange
        when(usuarioRepository.findByRun("99999999-9")).thenReturn(null);

        // Act
        Usuario resultado = usuarioService.findByRun("99999999-9");

        // Assert
        assertNull(resultado);
        verify(usuarioRepository, times(1)).findByRun("99999999-9");
    }
}