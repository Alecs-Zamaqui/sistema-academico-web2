package br.edu.example.sistema_academico.repository;

import br.edu.example.sistema_academico.model.Aluno;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AlunoRepository extends JpaRepository<Aluno, Long> {

    boolean existsByEmail(String email);

    // Atividade 3: Derived Query Method (o Spring Data deriva o SQL a partir do nome do método)
    List<Aluno> findByNomeContainingIgnoreCase(String nome);

    /*
     * Atividade 6: consulta JPQL explícita.
     *
     * Atividade 7: em JPQL escrevemos "FROM Aluno a" (e não "FROM tb_aluno") porque o JPQL
     * é uma linguagem orientada a OBJETOS: consultamos entidades Java e seus atributos
     * (a.nome), e não tabelas e colunas. O provedor JPA (Hibernate) traduz a consulta para
     * o SQL do banco usado, e é ele quem sabe que Aluno corresponde à tabela tb_aluno.
     *
     * Atividade 8: nome da ENTIDADE é o nome da classe Java anotada com @Entity (Aluno),
     * usado no JPQL. Nome da TABELA é o nome físico no banco (tb_aluno), definido em
     * @Table(name = "tb_aluno") e usado apenas em SQL nativo.
     */
    @Query("""
            SELECT a
            FROM Aluno a
            WHERE LOWER(a.nome) LIKE LOWER(CONCAT('%', :nome, '%'))
            """)
    List<Aluno> buscarPorNome(@Param("nome") String nome);

    /*
     * Atividades 13 a 15 e 27 a 28: filtros opcionais + paginação.
     * Estratégia ":param IS NULL": se o filtro não for informado (null), a condição é ignorada.
     * CAST(:param AS String) ajuda o PostgreSQL a descobrir o tipo do parâmetro quando ele é null.
     * O Spring Data gera sozinho o COUNT necessário para o Page.
     */
    @Query("""
            SELECT a
            FROM Aluno a
            WHERE (:nome IS NULL
                   OR LOWER(a.nome) LIKE LOWER(CONCAT('%', CAST(:nome AS String), '%')))
              AND (:email IS NULL
                   OR LOWER(a.email) LIKE LOWER(CONCAT('%', CAST(:email AS String), '%')))
            """)
    Page<Aluno> pesquisar(@Param("nome") String nome,
                          @Param("email") String email,
                          Pageable pageable);
}
