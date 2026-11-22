package br.edu.example.sistema_academico.service;

import br.edu.example.sistema_academico.exception.RecursoNaoEncontradoException;
import br.edu.example.sistema_academico.exception.RegraNegocioException;
import br.edu.example.sistema_academico.model.*;
import br.edu.example.sistema_academico.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Set;

import static br.edu.example.sistema_academico.model.StatusMatricula.*;

@Service
public class MatriculaService {

    private final MatriculaRepository repository;
    private final AlunoRepository alunoRepository;
    private final CursoRepository cursoRepository;
    private final HistoricoMatriculaRepository historicoRepository;

    public MatriculaService(MatriculaRepository repository,
                            AlunoRepository alunoRepository,
                            CursoRepository cursoRepository,
                            HistoricoMatriculaRepository historicoRepository) {
        this.repository = repository;
        this.alunoRepository = alunoRepository;
        this.cursoRepository = cursoRepository;
        this.historicoRepository = historicoRepository;
    }

    // ---------- Exercício 02 ----------
    @Transactional
    public Matricula matricular(Long alunoId, Long cursoId) {
        return matricular(alunoId, cursoId, null);
    }

    /** O DTO pode trazer status; uma nova matrícula só pode nascer ATIVA (null = ATIVA). */
    @Transactional
    public Matricula matricular(Long alunoId, Long cursoId, StatusMatricula status) {
        if (status != null && status != ATIVA) {
            throw new RegraNegocioException("NOVA_MATRICULA_DEVE_SER_ATIVA",
                    "Uma nova matrícula deve iniciar com status ATIVA.");
        }
        Aluno aluno = alunoRepository.findById(alunoId)
                .orElseThrow(() -> RecursoNaoEncontradoException.aluno(alunoId));
        Curso curso = cursoRepository.findById(cursoId)
                .orElseThrow(() -> RecursoNaoEncontradoException.curso(cursoId));

        if (repository.existsByAluno_IdAndCurso_IdAndStatus(alunoId, cursoId, ATIVA)) {
            throw RegraNegocioException.matriculaAtivaJaExistente();
        }

        return repository.save(new Matricula(null, aluno, curso, ATIVA));
    }

    @Transactional(readOnly = true)
    public List<Matricula> listar() {
        return repository.findAll();
    }

    private static final Set<String> CAMPOS_ORDENAVEIS =
            Set.of("id", "status", "dataMatricula", "aluno.nome", "curso.nome");

    // Atividades 31 a 33: pesquisa paginada com filtros opcionais
    @Transactional(readOnly = true)
    public Page<Matricula> pesquisar(Long alunoId, Long cursoId, StatusMatricula status, Pageable pageable) {
        return repository.pesquisar(alunoId, cursoId, status,
                PaginacaoUtil.limitar(pageable, CAMPOS_ORDENAVEIS));
    }

    // Atividades 9 a 12 e 37/38: consultas por relacionamento (somente leitura)
    @Transactional(readOnly = true)
    public List<Matricula> buscarPorNomeAluno(String nome) {
        return repository.buscarPorNomeAluno(nome);
    }

    @Transactional(readOnly = true)
    public List<Matricula> buscarPorNomeCurso(String nome) {
        return repository.buscarPorNomeCurso(nome);
    }

    @Transactional(readOnly = true)
    public List<Matricula> buscarPorCursoEStatus(Long cursoId, StatusMatricula status) {
        return repository.buscarPorCursoEStatus(cursoId, status);
    }

    @Transactional(readOnly = true)
    public Matricula consultar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> RecursoNaoEncontradoException.matricula(id));
    }

    // ---------- Exercício 04 ----------
    @Transactional
    public Matricula trancarMatricula(Long id) {
        return transicionar(id, "trancar", TRANCADA);
    }

    @Transactional
    public Matricula cancelarMatricula(Long id) {
        return transicionar(id, "cancelar", CANCELADA);
    }

    @Transactional
    public Matricula concluirMatricula(Long id) {
        return transicionar(id, "concluir", CONCLUIDA);
    }

    // ---------- Exercícios 04, 05 e 06 ----------
    /**
     * Dirty checking (Ex. 06): dentro da transação a matrícula está gerenciada pelo JPA.
     * Ao alterar o status, o Hibernate detecta a mudança no flush/commit e gera o UPDATE
     * sozinho — sem chamar save().
     *
     * O histórico é a segunda alteração no banco: as duas fazem parte da mesma
     * transação (Ex. 05) — ou as duas são confirmadas, ou nenhuma.
     */
    private Matricula transicionar(Long id, String operacao, StatusMatricula novoStatus) {
        Matricula matricula = repository.findById(id)
                .orElseThrow(() -> RecursoNaoEncontradoException.matricula(id));

        validarTransicao(matricula, operacao);

        StatusMatricula anterior = matricula.getStatus();
        matricula.setStatus(novoStatus);                       // dirty checking
        historicoRepository.save(new HistoricoMatricula(id, anterior, novoStatus));
        return matricula;
    }

    /** Todas as transições previstas na atividade partem de ATIVA. */
    private void validarTransicao(Matricula matricula, String operacao) {
        if (matricula.getStatus() != ATIVA) {
            throw RegraNegocioException.operacaoIncompativel(operacao, matricula.getStatus().name());
        }
    }

    // ---------- Exercício 05: simulação de falha ----------
    /**
     * Altera a matrícula, grava o histórico e DEPOIS lança exceção não verificada.
     * Esperado: rollback — status continua ATIVA e o histórico não é gravado.
     */
    @Transactional
    public void cancelarMatriculaComFalhaSimulada(Long id) {
        transicionar(id, "cancelar", CANCELADA);
        throw new IllegalStateException("Falha simulada para demonstrar o rollback");
    }
}
