package com.edutech.cl.EduTech.service;


import com.edutech.cl.EduTech.model.Cursos;
import com.edutech.cl.EduTech.repository.CursosRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
@Transactional
public class CursosService {
    @Autowired
    private CursosRepository cursosRepository;


    public List<Cursos> findAll() {
        return cursosRepository.findAll();
    }


    public Cursos findById(Integer idCurso) {
        return cursosRepository.findById(idCurso).get();
    }

    public Cursos save(Cursos cursos) {
        if (cursos.getNombreCurso() == null || cursos.getNombreCurso().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del curso es obligatorio");
        }
        if (cursos.getPrecioSub() < 0) {
            throw new IllegalArgumentException("El precio no puede ser negativo");
        }
        if (cursos.getCuposDisponibles() < 0) {
            throw new IllegalArgumentException("Los cupos no pueden ser negativos");
        }
        return cursosRepository.save(cursos);
    }

    public void delete(Integer idCurso) {
        cursosRepository.deleteById(idCurso);
    }
}