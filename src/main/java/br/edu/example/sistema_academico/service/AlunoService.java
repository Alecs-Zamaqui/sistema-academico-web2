package br.edu.example.sistema_academico.service;

import br.edu.example.sistema_academico.exception.RecursoNaoEncontradoException;
import br.edu.example.sistema_academico.exception.RegraNegocioException;
import br.edu.example.sistema_academico.model.Aluno;
import br.edu.example.sistema_academico.model.StatusMatricula;
import br.edu.example.sistema_academico.repository.AlunoRepository;
import br.edu.example.sistema_academico.repository.MatriculaRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Set;

@Service
public class AlunoService {

    private final AlunoRepository alunoRepository;
    private final MatriculaRepository matriculaRepository;

    public AlunoService(AlunoRepository alunoRepository, MatriculaRepository matriculaRepository) {
        this.alunoRepository = alunoRepository;
        this.matriculaRepository = matriculaRepository;
    }

    @Transactional
    public Aluno cadastrar(String nome, String email) {
        if (alunoRepository.existsByEmail(email)) {
            throw new RegraNegocioException("EMAIL_JA_CADASTRADO", "Já existe um aluno com este e-mail.");
        }
        return alunoRepository.save(new Aluno(null, nome, email));
    }

    private static final Set<String> CAMPOS_ORDENAVEIS = Set.of("id", "nome", "email");

    // Atividades 16 e 17: listagem paginada
    @Transactional(readOnly = true)
    public Page<Aluno> listar(Pageable pageable) {
        return alunoRepository.findAll(PaginacaoUtil.limitar(pageable, CAMPOS_ORDENAVEIS));
    }

    // Atividade 4: busca por nome (somente leitura)
    @Transactional(readOnly = true)
    public List<Aluno> buscarPorNome(String nome) {
        return alunoRepository.buscarPorNome(nome);
    }

    // Atividades 27 a 30: pesquisa com filtros opcionais, paginação e ordenação
    @Transactional(readOnly = true)
    public Page<Aluno> pesquisar(String nome, String email, Pageable pageable) {
        return alunoRepository.pesquisar(normalizar(nome), normalizar(email),
                PaginacaoUtil.limitar(pageable, CAMPOS_ORDENAVEIS));
    }

    /** Filtro em branco vira null, e o JPQL (":param IS NULL") ignora a condição. */
    private String normalizar(String filtro) {
        return (filtro == null || filtro.isBlank()) ? null : filtro.trim();
    }

    @Transactional(readOnly = true)
    public Aluno consultar(Long id) {
        return alunoRepository.findById(id)
                .orElseThrow(() -> RecursoNaoEncontradoException.aluno(id));
    }

    /** Dirty checking: a entidade gerenciada é alterada e o UPDATE sai no commit, sem save(). */
    @Transactional
    public Aluno atualizar(Long id, String nome, String email) {
        Aluno aluno = alunoRepository.findById(id)
                .orElseThrow(() -> RecursoNaoEncontradoException.aluno(id));
        if (!aluno.getEmail().equals(email) && alunoRepository.existsByEmail(email)) {
            throw new RegraNegocioException("EMAIL_JA_CADASTRADO", "Já existe um aluno com este e-mail.");
        }
        aluno.setNome(nome);
        aluno.setEmail(email);
        return aluno;
    }

    // Exercício 07
    @Transactional
    public void excluir(Long id) {
        Aluno aluno = alunoRepository.findById(id)
                .orElseThrow(() -> RecursoNaoEncontradoException.aluno(id));

        if (matriculaRepository.existsByAluno_IdAndStatus(id, StatusMatricula.ATIVA)) {
            throw new RegraNegocioException("ALUNO_COM_MATRICULA_ATIVA",
                    "Não é possível excluir o aluno: existe matrícula ativa.");
        }

        try {
            alunoRepository.delete(aluno);
            alunoRepository.flush(); // força o SQL agora, para capturar violação de FK aqui
        } catch (DataIntegrityViolationException e) {
            throw new RegraNegocioException("ALUNO_COM_REGISTROS_VINCULADOS",
                    "Não é possível excluir o aluno: há registros acadêmicos vinculados.");
        }
    }
}
