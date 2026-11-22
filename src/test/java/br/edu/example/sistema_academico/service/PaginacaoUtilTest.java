package br.edu.example.sistema_academico.service;

import br.edu.example.sistema_academico.exception.RegraNegocioException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PaginacaoUtilTest {

    private static final Set<String> CAMPOS = Set.of("id", "nome", "email");

    @Test
    @DisplayName("size=1000000 é limitado a 100 registros por página")
    void limitaTamanho() {
        Pageable resultado = PaginacaoUtil.limitar(PageRequest.of(2, 1_000_000), CAMPOS);

        assertEquals(100, resultado.getPageSize());
        assertEquals(2, resultado.getPageNumber());
    }

    @Test
    @DisplayName("Tamanho dentro do limite é preservado, junto com a ordenação")
    void preservaTamanhoEOrdenacao() {
        Pageable resultado = PaginacaoUtil.limitar(PageRequest.of(0, 5, Sort.by("nome").descending()), CAMPOS);

        assertEquals(5, resultado.getPageSize());
        assertEquals(Sort.Direction.DESC, resultado.getSort().getOrderFor("nome").getDirection());
    }

    @Test
    @DisplayName("Ordenar por campo não permitido -> exceção de negócio")
    void rejeitaCampoInvalido() {
        RegraNegocioException e = assertThrows(RegraNegocioException.class,
                () -> PaginacaoUtil.limitar(PageRequest.of(0, 10, Sort.by("senha")), CAMPOS));

        assertEquals("ORDENACAO_INVALIDA", e.getCodigo());
    }

    @Test
    @DisplayName("PageRequest montado manualmente (Atividade 25)")
    void criaPageRequestManual() {
        Pageable pageable = PaginacaoUtil.criar(1, 500, "nome", true);

        assertEquals(1, pageable.getPageNumber());
        assertEquals(100, pageable.getPageSize());
        assertEquals(Sort.Direction.ASC, pageable.getSort().getOrderFor("nome").getDirection());
    }
}
