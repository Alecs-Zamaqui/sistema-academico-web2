package br.edu.example.sistema_academico.repository;

import br.edu.example.sistema_academico.model.Matricula;
import br.edu.example.sistema_academico.model.StatusMatricula;
import org.springframework.data.jpa.repository.JpaRepository;

// Se você já tem este repository, acrescente apenas os métodos que faltarem.
public interface MatriculaRepository extends JpaRepository<Matricula, Long> {

    boolean existsByAluno_IdAndCurso_IdAndStatus(Long alunoId, Long cursoId, StatusMatricula status);

    boolean existsByAluno_IdAndStatus(Long alunoId, StatusMatricula status);

    boolean existsByCurso_IdAndStatus(Long cursoId, StatusMatricula status);
}
