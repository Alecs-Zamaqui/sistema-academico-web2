package br.edu.example.sistema_academico.repository;

import br.edu.example.sistema_academico.model.Aluno;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlunoRepository extends JpaRepository<Aluno, Long> {
    boolean existsByEmail(String email);
}
