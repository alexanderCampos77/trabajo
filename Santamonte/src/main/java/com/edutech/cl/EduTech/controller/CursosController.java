package com.edutech.cl.EduTech.controller;

import com.edutech.cl.EduTech.model.Cursos;
import com.edutech.cl.EduTech.service.CursosService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/v2/cursos")
@Tag(name = "Cursos", description = "API para gestión de cursos educativos")
public class CursosController {

    @Autowired
    private CursosService cursosService;

    @Operation(summary = "Listar todos los cursos",
            description = "Obtiene una lista completa de todos los cursos disponibles")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de cursos obtenida exitosamente"),
            @ApiResponse(responseCode = "204", description = "No hay cursos disponibles")
    })
    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<Cursos>>> listar() {
        List<Cursos> cursos = cursosService.findAll();

        if (cursos.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        List<EntityModel<Cursos>> cursosModels = cursos.stream()
                .map(curso -> EntityModel.of(curso,
                        linkTo(methodOn(CursosController.class).buscar(curso.getIdCurso())).withSelfRel(),
                        linkTo(methodOn(CursosController.class).listar()).withRel("cursos")))
                .collect(Collectors.toList());

        CollectionModel<EntityModel<Cursos>> collectionModel = CollectionModel.of(cursosModels,
                linkTo(methodOn(CursosController.class).listar()).withSelfRel());

        return ResponseEntity.ok(collectionModel);
    }

    @Operation(summary = "Crear un nuevo curso",
            description = "Registra un nuevo curso en el sistema")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Curso creado exitosamente",
                    content = @Content(schema = @Schema(implementation = Cursos.class))),
            @ApiResponse(responseCode = "400", description = "Datos del curso inválidos")
    })
    @PostMapping
    public ResponseEntity<EntityModel<Cursos>> guardar(@RequestBody Cursos cursos) {
        Cursos nuevoCurso = cursosService.save(cursos);

        EntityModel<Cursos> model = EntityModel.of(nuevoCurso,
                linkTo(methodOn(CursosController.class).buscar(nuevoCurso.getIdCurso())).withSelfRel(),
                linkTo(methodOn(CursosController.class).listar()).withRel("cursos"));

        return ResponseEntity
                .created(model.getRequiredLink(IanaLinkRelations.SELF).toUri())
                .body(model);
    }

    @Operation(summary = "Buscar curso por ID",
            description = "Obtiene los detalles de un curso específico por su ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Curso encontrado",
                    content = @Content(schema = @Schema(implementation = Cursos.class))),
            @ApiResponse(responseCode = "404", description = "Curso no encontrado")
    })
    @GetMapping("/{idCurso}")
    public ResponseEntity<EntityModel<Cursos>> buscar(
            @Parameter(description = "ID del curso", example = "1", required = true)
            @PathVariable Integer idCurso) {
        try {
            Cursos curso = cursosService.findById(idCurso);

            EntityModel<Cursos> model = EntityModel.of(curso,
                    linkTo(methodOn(CursosController.class).buscar(idCurso)).withSelfRel(),
                    linkTo(methodOn(CursosController.class).listar()).withRel("listar total de cursos"),
                    linkTo(methodOn(CursosController.class).Actualizar(idCurso, null)).withRel("actualizar informacion del curso"),
                    linkTo(methodOn(CursosController.class).Eliminar(idCurso)).withRel("eliminar curso seleccionado"));

            return ResponseEntity.ok(model);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Actualizar curso",
            description = "Actualiza la información de un curso existente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Curso actualizado exitosamente",
                    content = @Content(schema = @Schema(implementation = Cursos.class))),
            @ApiResponse(responseCode = "404", description = "Curso no encontrado"),
            @ApiResponse(responseCode = "400", description = "Datos del curso inválidos")
    })
    @PutMapping("/{idCurso}")
    public ResponseEntity<EntityModel<Cursos>> Actualizar(
            @Parameter(description = "ID del curso a actualizar", example = "1", required = true)
            @PathVariable Integer idCurso,
            @RequestBody Cursos cursos) {
        try {
            Cursos cursoExistente = cursosService.findById(idCurso);
            cursoExistente.setNombreCurso(cursos.getNombreCurso());
            cursoExistente.setDuracionCurso(cursos.getDuracionCurso());
            cursoExistente.setPrecioSub(cursos.getPrecioSub());
            cursoExistente.setCuposDisponibles(cursos.getCuposDisponibles());

            Cursos cursoActualizado = cursosService.save(cursoExistente);

            EntityModel<Cursos> model = EntityModel.of(cursoActualizado,
                    linkTo(methodOn(CursosController.class).buscar(idCurso)).withSelfRel(),
                    linkTo(methodOn(CursosController.class).listar()).withRel("cursos"));

            return ResponseEntity.ok(model);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Eliminar curso",
            description = "Elimina un curso específico del sistema")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Curso eliminado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Curso no encontrado")
    })
    @DeleteMapping("/{idCurso}")
    public ResponseEntity<?> Eliminar(
            @Parameter(description = "ID del curso a eliminar", example = "1", required = true)
            @PathVariable Integer idCurso) {
        try {
            cursosService.delete(idCurso);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}