package br.edu.example.sistema_academico.service;

import br.edu.example.sistema_academico.exception.RegraNegocioException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Set;
import java.util.TreeSet;

/** Regras de paginação do Sistema Acadêmico (tamanho máximo e campos de ordenação permitidos). */
public final class PaginacaoUtil {

    /** Atividade 46: nenhuma página pode ter mais de 100 registros. */
    public static final int TAMANHO_MAXIMO = 100;

    private PaginacaoUtil() {}

    /**
     * Recebe o Pageable vindo do Controller (page, size, sort), limita o tamanho a 100
     * e só aceita ordenar por campos permitidos (evita erro 500 com sort=campoInexistente).
     */
    public static Pageable limitar(Pageable pageable, Set<String> camposOrdenaveis) {
        if (pageable.isUnpaged()) {
            return PageRequest.of(0, TAMANHO_MAXIMO);
        }
        for (Sort.Order ordem : pageable.getSort()) {
            if (!camposOrdenaveis.contains(ordem.getProperty())) {
                throw new RegraNegocioException("ORDENACAO_INVALIDA",
                        "Campo de ordenação inválido. Campos permitidos: "
                                + String.join(", ", new TreeSet<>(camposOrdenaveis)) + ".");
            }
        }
        int tamanho = Math.min(pageable.getPageSize(), TAMANHO_MAXIMO);
        return PageRequest.of(pageable.getPageNumber(), tamanho, pageable.getSort());
    }

    /** Atividade 25: monta o PageRequest manualmente (página, tamanho e ordenação). */
    public static Pageable criar(int page, int size, String campoOrdenacao, boolean crescente) {
        int pagina = Math.max(page, 0);
        int tamanho = Math.min(Math.max(size, 1), TAMANHO_MAXIMO);
        Sort sort = crescente ? Sort.by(campoOrdenacao).ascending() : Sort.by(campoOrdenacao).descending();
        return PageRequest.of(pagina, tamanho, sort);
    }
}
