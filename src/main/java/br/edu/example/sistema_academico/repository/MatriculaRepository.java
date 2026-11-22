package br.edu.example.sistema_academico.repository;

import br.edu.example.sistema_academico.model.Matricula;
import br.edu.example.sistema_academico.model.StatusMatricula;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MatriculaRepository extends JpaRepository<Matricula, Long> {

    boolean existsByAluno_IdAndCurso_IdAndStatus(Long alunoId, Long cursoId, StatusMatricula status);

    boolean existsByAluno_IdAndStatus(Long alunoId, StatusMatricula status);

    boolean existsByCurso_IdAndStatus(Long cursoId, StatusMatricula status);

    // Atividade 9: consulta por relacionamento (navega de Matricula até Aluno)
    @Query("""
            SELECT m
            FROM Matricula m
            WHERE LOWER(m.aluno.nome) LIKE LOWER(CONCAT('%', :nome, '%'))
            """)
    List<Matricula> buscarPorNomeAluno(@Param("nome") String nome);

    // Atividade 10: consulta equivalente pelo nome do curso
    @Query("""
            SELECT m
            FROM Matricula m
            WHERE LOWER(m.curso.nome) LIKE LOWER(CONCAT('%', :nome, '%'))
            """)
    List<Matricula> buscarPorNomeCurso(@Param("nome") String nome);

    // Atividades 11 e 12: múltiplos critérios
    @Query("""
            SELECT m
            FROM Matricula m
            WHERE m.curso.id = :cursoId
              AND m.status = :status
            """)
    List<Matricula> buscarPorCursoEStatus(@Param("cursoId") Long cursoId,
                                          @Param("status") StatusMatricula status);

    // Atividades 37 e 38: JOIN explícito entre Matricula, Aluno e Curso
    @Query("""
            SELECT m
            FROM Matricula m
            JOIN m.aluno a
            JOIN m.curso c
            WHERE LOWER(a.nome) LIKE LOWER(CONCAT('%', :nome, '%'))
            """)
    List<Matricula> buscarComJoinPorNomeAluno(@Param("nome") String nome);

    // Atividades 31 e 32: pesquisa paginada com filtros opcionais
    @Query("""
            SELECT m
            FROM Matricula m
            WHERE (:alunoId IS NULL OR m.aluno.id = :alunoId)
              AND (:cursoId IS NULL OR m.curso.id = :cursoId)
              AND (:status IS NULL OR m.status = :status)
            """)
    Page<Matricula> pesquisar(@Param("alunoId") Long alunoId,
                              @Param("cursoId") Long cursoId,
                              @Param("status") StatusMatricula status,
                              Pageable pageable);
}
