package br.edu.example.sistema_academico.repository;

import br.edu.example.sistema_academico.model.Curso;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CursoRepository extends JpaRepository<Curso, Long> {

    // Parte 20 (desafio): filtros opcionais nome e cargaHorariaMinima, com paginação
    @Query("""
            SELECT c
            FROM Curso c
            WHERE (:nome IS NULL
                   OR LOWER(c.nome) LIKE LOWER(CONCAT('%', CAST(:nome AS String), '%')))
              AND (:cargaHorariaMinima IS NULL OR c.cargaHoraria >= :cargaHorariaMinima)
            """)
    Page<Curso> pesquisar(@Param("nome") String nome,
                          @Param("cargaHorariaMinima") Integer cargaHorariaMinima,
                          Pageable pageable);
}
