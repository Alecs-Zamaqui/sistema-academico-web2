package br.edu.example.sistema_academico.repository;

import br.edu.example.sistema_academico.model.Curso;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CursoRepository extends JpaRepository<Curso, Long> {
}
