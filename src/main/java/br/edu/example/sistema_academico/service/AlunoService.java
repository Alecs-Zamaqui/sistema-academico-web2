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

import java.util.List;

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

    @Transactional(readOnly = true)
    public List<Aluno> listar() {
        return alunoRepository.findAll();
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
