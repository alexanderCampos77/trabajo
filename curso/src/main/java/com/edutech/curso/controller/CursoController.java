package com.edutech.curso.controller;

import com.edutech.curso.dto.ActualizarCursoDTO;
import com.edutech.curso.dto.CrearCursoDTO;
import com.edutech.curso.model.Curso;
import com.edutech.curso.service.CursoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/cursos")
public class CursoController {

    private final CursoService service;

    public CursoController(CursoService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Curso>> listar() {
        List<Curso> cursoList = service.findAll();
        if (cursoList.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(cursoList);
    }

    @GetMapping("/{id}")
    public Optional<Curso> buscarPorId(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    public ResponseEntity<Curso> guardar(@RequestBody @Valid CrearCursoDTO crearCursoDTO) {
        Curso curso = Curso.builder()
                .titulo(crearCursoDTO.getTitulo())
                .descripcion(crearCursoDTO.getDescripcion())
                .duracion(crearCursoDTO.getDuracion())
                .build();

        Curso guardado = service.save(curso);
        return ResponseEntity.ok(guardado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Curso> actualizar(@PathVariable Long id, @RequestBody @Valid ActualizarCursoDTO actualizarCursoDTO) {
        if (!service.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        Curso cursoActualizado = service.findById(id).get();
        cursoActualizado.setTitulo(actualizarCursoDTO.getTitulo());
        cursoActualizado.setDescripcion(actualizarCursoDTO.getDescripcion());
        cursoActualizado.setDuracion(actualizarCursoDTO.getDuracion());
        service.save(cursoActualizado);
        return ResponseEntity.ok(cursoActualizado);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        service.deleteById(id);
    }
}
