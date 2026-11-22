package br.edu.example.sistema_academico.service;

import br.edu.example.sistema_academico.exception.RecursoNaoEncontradoException;
import br.edu.example.sistema_academico.exception.RegraNegocioException;
import br.edu.example.sistema_academico.model.*;
import br.edu.example.sistema_academico.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MatriculaServiceTest {

    @Mock MatriculaRepository repository;
    @Mock AlunoRepository alunoRepository;
    @Mock CursoRepository cursoRepository;
    @Mock HistoricoMatriculaRepository historicoRepository;
    @Mock Aluno aluno;
    @Mock Curso curso;

    @InjectMocks MatriculaService service;

    private Matricula matriculaComStatus(StatusMatricula status) {
        return new Matricula(null, aluno, curso, status);
    }

    @Test
    @DisplayName("Aluno e curso existentes -> matrícula criada com status ATIVA")
    void deveCriarMatricula() {
        when(alunoRepository.findById(1L)).thenReturn(Optional.of(aluno));
        when(cursoRepository.findById(2L)).thenReturn(Optional.of(curso));
        when(repository.existsByAluno_IdAndCurso_IdAndStatus(1L, 2L, StatusMatricula.ATIVA))
                .thenReturn(false);
        when(repository.save(any(Matricula.class))).thenAnswer(i -> i.getArgument(0));

        Matricula resultado = service.matricular(1L, 2L);

        assertEquals(StatusMatricula.ATIVA, resultado.getStatus());
        verify(repository).save(any(Matricula.class));
    }

    @Test
    @DisplayName("Aluno inexistente -> exceção")
    void alunoInexistente() {
        when(alunoRepository.findById(1L)).thenReturn(Optional.empty());

        RecursoNaoEncontradoException e = assertThrows(RecursoNaoEncontradoException.class,
                () -> service.matricular(1L, 2L));

        assertTrue(e.getMessage().contains("Aluno não encontrado"));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Curso inexistente -> exceção")
    void cursoInexistente() {
        when(alunoRepository.findById(1L)).thenReturn(Optional.of(aluno));
        when(cursoRepository.findById(2L)).thenReturn(Optional.empty());

        RecursoNaoEncontradoException e = assertThrows(RecursoNaoEncontradoException.class,
                () -> service.matricular(1L, 2L));

        assertTrue(e.getMessage().contains("Curso não encontrado"));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Matrícula ativa duplicada -> operação rejeitada")
    void matriculaDuplicada() {
        when(alunoRepository.findById(1L)).thenReturn(Optional.of(aluno));
        when(cursoRepository.findById(2L)).thenReturn(Optional.of(curso));
        when(repository.existsByAluno_IdAndCurso_IdAndStatus(1L, 2L, StatusMatricula.ATIVA))
                .thenReturn(true);

        RegraNegocioException e = assertThrows(RegraNegocioException.class,
                () -> service.matricular(1L, 2L));

        assertTrue(e.getMessage().contains("já possui matrícula ativa"));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Trancamento válido -> status TRANCADA")
    void trancamentoValido() {
        when(repository.findById(10L))
                .thenReturn(Optional.of(matriculaComStatus(StatusMatricula.ATIVA)));

        Matricula m = service.trancarMatricula(10L);

        assertEquals(StatusMatricula.TRANCADA, m.getStatus());
        verify(historicoRepository).save(any(HistoricoMatricula.class));
    }

    @Test
    @DisplayName("Conclusão inválida (matrícula TRANCADA) -> exceção")
    void conclusaoInvalida() {
        when(repository.findById(10L))
                .thenReturn(Optional.of(matriculaComStatus(StatusMatricula.TRANCADA)));

        RegraNegocioException e = assertThrows(RegraNegocioException.class,
                () -> service.concluirMatricula(10L));

        assertTrue(e.getMessage().contains("incompatível"));
        verify(historicoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Cancelamento válido -> status CANCELADA")
    void cancelamentoValido() {
        when(repository.findById(10L))
                .thenReturn(Optional.of(matriculaComStatus(StatusMatricula.ATIVA)));

        Matricula m = service.cancelarMatricula(10L);

        assertEquals(StatusMatricula.CANCELADA, m.getStatus());
    }

    @Test
    @DisplayName("Pesquisa paginada de matrículas repassa filtros e limita o tamanho a 100")
    void pesquisarLimitaTamanho() {
        when(repository.pesquisar(any(), any(), any(), any()))
                .thenReturn(org.springframework.data.domain.Page.empty());

        service.pesquisar(null, 1L, StatusMatricula.ATIVA,
                org.springframework.data.domain.PageRequest.of(0, 5000));

        org.mockito.ArgumentCaptor<org.springframework.data.domain.Pageable> captor =
                org.mockito.ArgumentCaptor.forClass(org.springframework.data.domain.Pageable.class);
        verify(repository).pesquisar(org.mockito.ArgumentMatchers.isNull(),
                org.mockito.ArgumentMatchers.eq(1L),
                org.mockito.ArgumentMatchers.eq(StatusMatricula.ATIVA), captor.capture());
        assertEquals(100, captor.getValue().getPageSize());
    }
}
