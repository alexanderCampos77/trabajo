package com.edutech.curso.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CrearCursoDTO {

    @NotBlank
    private String titulo;

    @NotBlank
    private String descripcion;

    @NotNull
    private Integer duracion;

    public String getTitulo() {
        return titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public Integer getDuracion() {
        return duracion;
    }
}
