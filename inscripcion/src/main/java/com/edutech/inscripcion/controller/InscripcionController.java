package com.edutech.inscripcion.controller;

import com.edutech.inscripcion.dto.InscripcionRespuestaDTO;
import com.edutech.inscripcion.model.Inscripcion;
import com.edutech.inscripcion.repository.InscripcionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/inscripciones")
public class InscripcionController {

    @Autowired
    private InscripcionRepository inscripcionRepository;

    @Autowired
    private RestTemplate restTemplate;

    private final String USUARIO_SERVICE_URL = "http://localhost:9090/api/usuarios/";
    private final String CURSO_SERVICE_URL = "http://localhost:8082/api/cursos/";

    @PostMapping
    public ResponseEntity<?> inscribir(@RequestParam Long usuarioId, @RequestParam Long cursoId) {
        try {
            restTemplate.getForEntity(USUARIO_SERVICE_URL + usuarioId, Object.class);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Usuario no existe");
        }

        try {
            restTemplate.getForEntity(CURSO_SERVICE_URL + cursoId, Object.class);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Curso no existe");
        }

        Inscripcion inscripcion = new Inscripcion();
        inscripcion.setUsuarioId(usuarioId);
        inscripcion.setCursoId(cursoId);
        inscripcion.setFechaInscripcion(LocalDate.now());

        Inscripcion saved = inscripcionRepository.save(inscripcion);

        InscripcionRespuestaDTO respuesta = new InscripcionRespuestaDTO(
                saved.getId(),
                saved.getUsuarioId(),
                saved.getCursoId(),
                saved.getFechaInscripcion()
        );

        return ResponseEntity.ok(respuesta);
    }

    @GetMapping
    public List<InscripcionRespuestaDTO> listarTodas() {
        return inscripcionRepository.findAll()
                .stream()
                .map(inscripcion -> new InscripcionRespuestaDTO(
                        inscripcion.getId(),
                        inscripcion.getUsuarioId(),
                        inscripcion.getCursoId(),
                        inscripcion.getFechaInscripcion()
                ))
                .collect(Collectors.toList());
    }

    @GetMapping("/usuario/{usuarioId}")
    public List<InscripcionRespuestaDTO> listarPorUsuario(@PathVariable Long usuarioId) {
        return inscripcionRepository.findByUsuarioId(usuarioId)
                .stream()
                .map(inscripcion -> new InscripcionRespuestaDTO(
                        inscripcion.getId(),
                        inscripcion.getUsuarioId(),
                        inscripcion.getCursoId(),
                        inscripcion.getFechaInscripcion()
                ))
                .collect(Collectors.toList());
    }

    @GetMapping("/curso/{cursoId}")
    public List<InscripcionRespuestaDTO> listarPorCurso(@PathVariable Long cursoId) {
        return inscripcionRepository.findByCursoId(cursoId)
                .stream()
                .map(inscripcion -> new InscripcionRespuestaDTO(
                        inscripcion.getId(),
                        inscripcion.getUsuarioId(),
                        inscripcion.getCursoId(),
                        inscripcion.getFechaInscripcion()
                ))
                .collect(Collectors.toList());
    }
}
