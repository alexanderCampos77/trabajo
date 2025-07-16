package com.edutech.cl.EduTech.controller;

import com.edutech.cl.EduTech.model.Inscripcion;
import com.edutech.cl.EduTech.service.InscripcionService;
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
@RequestMapping("/api/v2/inscripciones")
@Tag(name = "Inscripciones", description = "API para gestión de inscripciones de usuarios a cursos")
public class InscripcionController {

    @Autowired
    private InscripcionService inscripcionService;

    @Operation(summary = "Listar todas las inscripciones",
            description = "Obtiene una lista completa de todas las inscripciones registradas en el sistema")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de inscripciones obtenida exitosamente"),
            @ApiResponse(responseCode = "204", description = "No hay inscripciones disponibles")
    })
    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<Inscripcion>>> listarTodas() {
        List<Inscripcion> inscripciones = inscripcionService.obtenerTodas();

        if (inscripciones.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        List<EntityModel<Inscripcion>> inscripcionesModels = inscripciones.stream()
                .map(inscripcion -> EntityModel.of(inscripcion,
                        linkTo(methodOn(InscripcionController.class).listarTodas()).withSelfRel(),
                        linkTo(methodOn(InscripcionController.class).listarPorUsuario(inscripcion.getUsuario().getIdUsuario())).withRel("inscripciones del usuario")))
                .collect(Collectors.toList());

        CollectionModel<EntityModel<Inscripcion>> collectionModel = CollectionModel.of(inscripcionesModels,
                linkTo(methodOn(InscripcionController.class).listarTodas()).withSelfRel());

        return ResponseEntity.ok(collectionModel);
    }

    @Operation(summary = "Listar inscripciones por usuario",
            description = "Obtiene todas las inscripciones de un usuario específico")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Inscripciones del usuario obtenidas exitosamente"),
            @ApiResponse(responseCode = "204", description = "El usuario no tiene inscripciones"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<CollectionModel<EntityModel<Inscripcion>>> listarPorUsuario(
            @Parameter(description = "ID del usuario", example = "1", required = true)
            @PathVariable Integer usuarioId) {
        List<Inscripcion> inscripciones = inscripcionService.obtenerPorUsuario(usuarioId);

        if (inscripciones.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        List<EntityModel<Inscripcion>> inscripcionesModels = inscripciones.stream()
                .map(inscripcion -> EntityModel.of(inscripcion,
                        linkTo(methodOn(InscripcionController.class).listarPorUsuario(usuarioId)).withSelfRel(),
                        linkTo(methodOn(InscripcionController.class).listarTodas()).withRel("todas las inscripciones")))
                .collect(Collectors.toList());

        CollectionModel<EntityModel<Inscripcion>> collectionModel = CollectionModel.of(inscripcionesModels,
                linkTo(methodOn(InscripcionController.class).listarPorUsuario(usuarioId)).withSelfRel(),
                linkTo(methodOn(InscripcionController.class).listarTodas()).withRel("todas las inscripciones"),
                linkTo(methodOn(InscripcionController.class).inscribir(usuarioId, null)).withRel("crear nueva inscripcion"));

        return ResponseEntity.ok(collectionModel);
    }

    @Operation(summary = "Inscribir usuario a un curso",
            description = "Registra la inscripción de un usuario a un curso específico y descuenta un cupo disponible del curso")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Inscripción creada exitosamente",
                    content = @Content(schema = @Schema(implementation = Inscripcion.class))),
            @ApiResponse(responseCode = "400", description = "Datos de inscripción inválidos"),
            @ApiResponse(responseCode = "409", description = "El usuario ya está inscrito en este curso o no hay cupos disponibles"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PostMapping
    public ResponseEntity<?> inscribir(
            @Parameter(description = "ID del usuario a inscribir", example = "1", required = true)
            @RequestParam Integer usuarioId,
            @Parameter(description = "ID del curso al que se inscribe", example = "1", required = true)
            @RequestParam Integer cursoId) {
        try {
            Inscripcion inscripcion = inscripcionService.inscribirUsuario(usuarioId, cursoId);

            EntityModel<Inscripcion> model = EntityModel.of(inscripcion,
                    linkTo(methodOn(InscripcionController.class).buscarInscripcion(inscripcion.getId())).withSelfRel(),
                    linkTo(methodOn(InscripcionController.class).listarPorUsuario(inscripcion.getUsuario().getIdUsuario())).withRel("inscripciones del usuario"),
                    linkTo(methodOn(InscripcionController.class).listarTodas()).withRel("todas las inscripciones"),
                    linkTo(methodOn(InscripcionController.class).actualizarInscripcion(inscripcion.getId(), null, null)).withRel("actualizar inscripcion"),
                    linkTo(methodOn(InscripcionController.class).eliminarInscripcion(inscripcion.getId())).withRel("eliminar inscripcion"));

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(model);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error en los datos proporcionados: " + e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Conflicto en la inscripción: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al inscribir: " + e.getMessage());
        }
    }

    @Operation(summary = "Buscar inscripción por ID",
            description = "Obtiene los detalles de una inscripción específica por su ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Inscripción encontrada",
                    content = @Content(schema = @Schema(implementation = Inscripcion.class))),
            @ApiResponse(responseCode = "404", description = "Inscripción no encontrada")
    })
    @GetMapping("/{inscripcionId}")
    public ResponseEntity<EntityModel<Inscripcion>> buscarInscripcion(
            @Parameter(description = "ID de la inscripción", example = "1", required = true)
            @PathVariable Integer inscripcionId) {
        try {
            Inscripcion inscripcion = inscripcionService.obtenerPorId(inscripcionId);

            EntityModel<Inscripcion> model = EntityModel.of(inscripcion,
                    linkTo(methodOn(InscripcionController.class).buscarInscripcion(inscripcionId)).withSelfRel(),
                    linkTo(methodOn(InscripcionController.class).listarTodas()).withRel("todas las inscripciones"),
                    linkTo(methodOn(InscripcionController.class).listarPorUsuario(inscripcion.getUsuario().getIdUsuario())).withRel("inscripciones del usuario"),
                    linkTo(methodOn(InscripcionController.class).actualizarInscripcion(inscripcionId, null, null)).withRel("actualizar inscripcion"),
                    linkTo(methodOn(InscripcionController.class).eliminarInscripcion(inscripcionId)).withRel("eliminar inscripcion"));

            return ResponseEntity.ok(model);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Actualizar inscripción",
            description = "Actualiza la inscripción cambiando el usuario y/o curso usando sus IDs. Si se cambia de curso, devuelve el cupo al curso anterior y descuenta del nuevo curso")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Inscripción actualizada exitosamente",
                    content = @Content(schema = @Schema(implementation = Inscripcion.class))),
            @ApiResponse(responseCode = "404", description = "Inscripción no encontrada"),
            @ApiResponse(responseCode = "400", description = "Datos de inscripción inválidos"),
            @ApiResponse(responseCode = "409", description = "No hay cupos disponibles en el nuevo curso")
    })
    @PutMapping("/{inscripcionId}")
    public ResponseEntity<?> actualizarInscripcion(
            @Parameter(description = "ID de la inscripción a actualizar", example = "1", required = true)
            @PathVariable Integer inscripcionId,
            @Parameter(description = "ID del nuevo usuario", example = "2")
            @RequestParam(required = false) Integer usuarioId,
            @Parameter(description = "ID del nuevo curso", example = "3")
            @RequestParam(required = false) Integer cursoId) {
        try {
            Inscripcion inscripcion = inscripcionService.actualizarInscripcionPorIds(inscripcionId, usuarioId, cursoId);

            EntityModel<Inscripcion> model = EntityModel.of(inscripcion,
                    linkTo(methodOn(InscripcionController.class).buscarInscripcion(inscripcionId)).withSelfRel(),
                    linkTo(methodOn(InscripcionController.class).listarTodas()).withRel("todas las inscripciones"),
                    linkTo(methodOn(InscripcionController.class).listarPorUsuario(inscripcion.getUsuario().getIdUsuario())).withRel("inscripciones del usuario"),
                    linkTo(methodOn(InscripcionController.class).eliminarInscripcion(inscripcionId)).withRel("eliminar inscripcion"));

            return ResponseEntity.ok(model);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error en los datos proporcionados: " + e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("No hay cupos disponibles: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Inscripción no encontrada: " + e.getMessage());
        }
    }

    @Operation(summary = "Eliminar inscripción",
            description = "Elimina una inscripción específica del sistema y devuelve el cupo al curso correspondiente")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Inscripción eliminada exitosamente"),
            @ApiResponse(responseCode = "404", description = "Inscripción no encontrada")
    })
    @DeleteMapping("/{inscripcionId}")
    public ResponseEntity<?> eliminarInscripcion(
            @Parameter(description = "ID de la inscripción a eliminar", example = "1", required = true)
            @PathVariable Integer inscripcionId) {
        try {
            inscripcionService.eliminarInscripcion(inscripcionId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Inscripción no encontrada: " + e.getMessage());
        }
    }
}