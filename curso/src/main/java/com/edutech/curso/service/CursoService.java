package com.edutech.curso.service;

import com.edutech.curso.model.Curso;
import com.edutech.curso.repository.CursoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CursoService {

    private final CursoRepository repository;

    public CursoService(CursoRepository repository) {
        this.repository = repository;
    }
    public List<Curso> findAll() {
        return repository.findAll();
    }

    public Optional<Curso> findById(Long id) {
        return repository.findById(id);
    }

    public Boolean existsById(Long id) { return repository.existsById(id); }

    public Curso save(Curso curso) {
        return repository.save(curso);
    }

    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}
