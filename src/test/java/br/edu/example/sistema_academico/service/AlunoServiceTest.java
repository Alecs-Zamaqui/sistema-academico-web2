package br.edu.example.sistema_academico.service;

import br.edu.example.sistema_academico.model.Aluno;
import br.edu.example.sistema_academico.repository.AlunoRepository;
import br.edu.example.sistema_academico.repository.MatriculaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlunoServiceTest {

    @Mock AlunoRepository alunoRepository;
    @Mock MatriculaRepository matriculaRepository;

    @InjectMocks AlunoService service;

    @Test
    @DisplayName("Pesquisa: filtro em branco vira null e tamanho da página é limitado a 100")
    void pesquisaNormalizaFiltrosELimitaTamanho() {
        when(alunoRepository.pesquisar(any(), any(), any())).thenReturn(Page.empty());

        service.pesquisar("   ", "gmail", PageRequest.of(0, 1000, Sort.by("nome")));

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(alunoRepository).pesquisar(isNull(), eq("gmail"), captor.capture());
        assertEquals(100, captor.getValue().getPageSize());
    }

    @Test
    @DisplayName("Busca por nome delega ao repository")
    void buscarPorNome() {
        List<Aluno> alunos = List.of(new Aluno(1L, "Ana", "ana@gmail.com"));
        when(alunoRepository.buscarPorNome("ana")).thenReturn(alunos);

        assertEquals(1, service.buscarPorNome("ana").size());
    }

    @Test
    @DisplayName("Listagem paginada usa findAll(pageable)")
    void listarPaginado() {
        Page<Aluno> pagina = new PageImpl<>(List.of(new Aluno(1L, "Ana", "ana@gmail.com")));
        when(alunoRepository.findAll(any(Pageable.class))).thenReturn(pagina);

        Page<Aluno> resultado = service.listar(PageRequest.of(0, 5));

        assertEquals(1, resultado.getTotalElements());
    }
}
