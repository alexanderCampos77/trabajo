package com.edutech.cl.EduTech.repository;

import com.edutech.cl.EduTech.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer>{

    Usuario findByRun(String run);

    List<Usuario> findByRol(String rol);
}
