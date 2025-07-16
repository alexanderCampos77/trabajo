package com.edutech.cl.EduTech.controller;

import com.edutech.cl.EduTech.model.Usuario;
import com.edutech.cl.EduTech.service.UsuarioService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/v2/usuarios")
@Tag(name = "Usuarios", description = "API para gestión de usuarios del sistema")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @Operation(summary = "Listar todos los usuarios",
            description = "Obtiene una lista completa de todos los usuarios registrados")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de usuarios obtenida exitosamente"),
            @ApiResponse(responseCode = "204", description = "No hay usuarios registrados")
    })
    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<Usuario>>> listar() {
        List<Usuario> usuarios = usuarioService.findAll();

        if (usuarios.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        List<EntityModel<Usuario>> usuarioModels = usuarios.stream()
                .map(usuario -> EntityModel.of(usuario,
                        linkTo(methodOn(UsuarioController.class).buscar(usuario.getIdUsuario())).withSelfRel(),
                        linkTo(methodOn(UsuarioController.class).listar()).withRel("lISTADO DE TODOS LOS USUARIOS")))
                .collect(Collectors.toList());

        CollectionModel<EntityModel<Usuario>> collectionModel = CollectionModel.of(usuarioModels,
                linkTo(methodOn(UsuarioController.class).listar()).withSelfRel());

        return ResponseEntity.ok(collectionModel);
    }

    @Operation(summary = "Crear nuevo usuario",
            description = "Registra un nuevo usuario en el sistema")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuario creado exitosamente",
                    content = @Content(schema = @Schema(implementation = Usuario.class))),
            @ApiResponse(responseCode = "400", description = "Datos del usuario inválidos")
    })
    @PostMapping
    public ResponseEntity<EntityModel<Usuario>> guardar(@RequestBody Usuario usuario) {
        Usuario nuevoUsuario = usuarioService.save(usuario);

        EntityModel<Usuario> model = EntityModel.of(nuevoUsuario,
                linkTo(methodOn(UsuarioController.class).buscar(nuevoUsuario.getIdUsuario())).withSelfRel(),
                linkTo(methodOn(UsuarioController.class).listar()).withRel("usuarios"));

        return ResponseEntity
                .created(model.getRequiredLink(IanaLinkRelations.SELF).toUri())
                .body(model);
    }

    @Operation(summary = "Buscar usuario por ID",
            description = "Obtiene los detalles de un usuario específico por su ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario encontrado",
                    content = @Content(schema = @Schema(implementation = Usuario.class))),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<Usuario>> buscar(
            @Parameter(description = "ID del usuario", example = "1", required = true)
            @PathVariable Integer id) {
        try {
            Usuario usuario = usuarioService.findById(id);

            EntityModel<Usuario> model = EntityModel.of(usuario,
                    linkTo(methodOn(UsuarioController.class).buscar(id)).withSelfRel(),
                    linkTo(methodOn(UsuarioController.class).listar()).withRel("usuarios"),
                    linkTo(methodOn(UsuarioController.class).Actualizar(id, null)).withRel("actualizar"),
                    linkTo(methodOn(UsuarioController.class).Eliminar(id)).withRel("eliminar"));

            return ResponseEntity.ok(model);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Actualizar usuario",
            description = "Actualiza la información de un usuario existente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario actualizado exitosamente",
                    content = @Content(schema = @Schema(implementation = Usuario.class))),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
            @ApiResponse(responseCode = "400", description = "Datos del usuario inválidos")
    })
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<Usuario>> Actualizar(
            @Parameter(description = "ID del usuario a actualizar", example = "1", required = true)
            @PathVariable Integer id,
            @RequestBody Usuario usuario) {
        try {
            Usuario user = usuarioService.findById(id);
            user.setRun(usuario.getRun());
            user.setNombres(usuario.getNombres());
            user.setApellidos(usuario.getApellidos());
            user.setFechaNacimiento(usuario.getFechaNacimiento());
            user.setCorreo(usuario.getCorreo());

            Usuario usuarioActualizado = usuarioService.save(user);

            EntityModel<Usuario> model = EntityModel.of(usuarioActualizado,
                    linkTo(methodOn(UsuarioController.class).buscar(id)).withSelfRel(),
                    linkTo(methodOn(UsuarioController.class).listar()).withRel("usuarios"));

            return ResponseEntity.ok(model);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Eliminar usuario",
            description = "Elimina un usuario específico del sistema")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Usuario eliminado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> Eliminar(
            @Parameter(description = "ID del usuario a eliminar", example = "1", required = true)
            @PathVariable Integer id) {
        try {
            usuarioService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Buscar usuarios por rol",
            description = "Obtiene una lista de usuarios filtrados por su rol")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuarios encontrados"),
            @ApiResponse(responseCode = "204", description = "No hay usuarios con ese rol")
    })
    @GetMapping("/rol/{rol}")
    public ResponseEntity<CollectionModel<EntityModel<Usuario>>> buscarPorRol(
            @Parameter(description = "Rol de los usuarios a buscar", example = "ADMIN", required = true)
            @PathVariable String rol) {
        List<Usuario> usuarios = usuarioService.findByRol(rol);

        if (usuarios.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        List<EntityModel<Usuario>> usuarioModels = usuarios.stream()
                .map(usuario -> EntityModel.of(usuario,
                        linkTo(methodOn(UsuarioController.class).buscar(usuario.getIdUsuario())).withSelfRel(),
                        linkTo(methodOn(UsuarioController.class).listar()).withRel("usuarios")))
                .collect(Collectors.toList());

        CollectionModel<EntityModel<Usuario>> collectionModel = CollectionModel.of(usuarioModels,
                linkTo(methodOn(UsuarioController.class).buscarPorRol(rol)).withSelfRel());

        return ResponseEntity.ok(collectionModel);
    }

    @Operation(summary = "Buscar usuario por RUN",
            description = "Obtiene los detalles de un usuario específico por su RUN")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario encontrado",
                    content = @Content(schema = @Schema(implementation = Usuario.class))),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    @GetMapping("/run/{run}")
    public ResponseEntity<EntityModel<Usuario>> buscarPorRun(
            @Parameter(description = "RUN del usuario", example = "12345678-9", required = true)
            @PathVariable String run) {
        try {
            Usuario usuario = usuarioService.findByRun(run);

            EntityModel<Usuario> model = EntityModel.of(usuario,
                    linkTo(methodOn(UsuarioController.class).buscarPorRun(run)).withSelfRel(),
                    linkTo(methodOn(UsuarioController.class).buscar(usuario.getIdUsuario())).withRel("usuario-by-id"),
                    linkTo(methodOn(UsuarioController.class).listar()).withRel("usuarios"));

            return ResponseEntity.ok(model);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}