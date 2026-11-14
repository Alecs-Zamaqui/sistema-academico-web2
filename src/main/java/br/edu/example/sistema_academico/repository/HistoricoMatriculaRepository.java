package br.edu.example.sistema_academico.repository;

import br.edu.example.sistema_academico.model.HistoricoMatricula;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HistoricoMatriculaRepository extends JpaRepository<HistoricoMatricula, Long> {
    long countByMatriculaId(Long matriculaId);
}
