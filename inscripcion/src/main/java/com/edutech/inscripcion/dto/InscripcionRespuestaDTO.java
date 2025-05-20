package com.edutech.inscripcion.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class InscripcionRespuestaDTO {
    private Long id;
    private Long usuarioId;
    private Long cursoId;
    private LocalDate fechaInscripcion;
}
