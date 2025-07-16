package com.edutech.cl.EduTech.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name ="Cursos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cursos {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idCurso;

    @Column(nullable = false)
    private String nombreCurso;

    @Column(nullable = false)
    private double precioSub;

    @Column(nullable = false)
    private String duracionCurso;

    @Column(nullable=false)
    private Integer cuposDisponibles;



}
