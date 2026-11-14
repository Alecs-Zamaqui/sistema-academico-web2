package br.edu.example.sistema_academico.service;

import br.edu.example.sistema_academico.exception.RecursoNaoEncontradoException;
import br.edu.example.sistema_academico.exception.RegraNegocioException;
import br.edu.example.sistema_academico.model.Curso;
import br.edu.example.sistema_academico.model.StatusMatricula;
import br.edu.example.sistema_academico.repository.CursoRepository;
import br.edu.example.sistema_academico.repository.MatriculaRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CursoService {

    private final CursoRepository cursoRepository;
    private final MatriculaRepository matriculaRepository;

    public CursoService(CursoRepository cursoRepository, MatriculaRepository matriculaRepository) {
        this.cursoRepository = cursoRepository;
        this.matriculaRepository = matriculaRepository;
    }

    @Transactional
    public Curso cadastrar(String nome, Integer cargaHoraria) {
        return cursoRepository.save(new Curso(null, nome, cargaHoraria));
    }

    @Transactional(readOnly = true)
    public List<Curso> listar() {
        return cursoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Curso consultar(Long id) {
        return cursoRepository.findById(id)
                .orElseThrow(() -> RecursoNaoEncontradoException.curso(id));
    }

    @Transactional
    public Curso atualizar(Long id, String nome, Integer cargaHoraria) {
        Curso curso = cursoRepository.findById(id)
                .orElseThrow(() -> RecursoNaoEncontradoException.curso(id));
        curso.setNome(nome);
        curso.setCargaHoraria(cargaHoraria);
        return curso;
    }

    // Exercício 07
    @Transactional
    public void excluir(Long id) {
        Curso curso = cursoRepository.findById(id)
                .orElseThrow(() -> RecursoNaoEncontradoException.curso(id));

        if (matriculaRepository.existsByCurso_IdAndStatus(id, StatusMatricula.ATIVA)) {
            throw new RegraNegocioException("CURSO_COM_MATRICULA_ATIVA",
                    "Não é possível excluir o curso: existe matrícula ativa.");
        }

        try {
            cursoRepository.delete(curso);
            cursoRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new RegraNegocioException("CURSO_COM_REGISTROS_VINCULADOS",
                    "Não é possível excluir o curso: há registros acadêmicos vinculados.");
        }
    }
}
