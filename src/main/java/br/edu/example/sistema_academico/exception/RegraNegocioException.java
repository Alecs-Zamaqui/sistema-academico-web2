package br.edu.example.sistema_academico.exception;

/** Violação de regra de negócio. Nesta atividade vira HTTP 400. Não verificada: gera rollback. */
public class RegraNegocioException extends RuntimeException {

    private final String codigo;

    public RegraNegocioException(String mensagem) {
        this(null, mensagem);
    }

    public RegraNegocioException(String codigo, String mensagem) {
        super(mensagem);
        this.codigo = codigo;
    }

    public String getCodigo() { return codigo; }

    public static RegraNegocioException matriculaAtivaJaExistente() {
        return new RegraNegocioException("MATRICULA_ATIVA_DUPLICADA",
                "Aluno já possui matrícula ativa neste curso.");
    }

    public static RegraNegocioException operacaoIncompativel(String operacao, String statusAtual) {
        return new RegraNegocioException("OPERACAO_INCOMPATIVEL_COM_STATUS",
                "Operação '" + operacao + "' incompatível com o status atual da matrícula: " + statusAtual + ".");
    }
}
