package br.edu.example.sistema_academico.exception;

/** Lançada quando um recurso (aluno, curso, matrícula...) não existe. Vira HTTP 404. */
public class RecursoNaoEncontradoException extends RuntimeException {

    private final String codigo;

    public RecursoNaoEncontradoException(String mensagem) {
        this(null, mensagem);
    }

    public RecursoNaoEncontradoException(String codigo, String mensagem) {
        super(mensagem);
        this.codigo = codigo;
    }

    public String getCodigo() { return codigo; }

    public static RecursoNaoEncontradoException aluno(Long id) {
        return new RecursoNaoEncontradoException("ALUNO_NAO_ENCONTRADO", "Aluno não encontrado.");
    }

    public static RecursoNaoEncontradoException curso(Long id) {
        return new RecursoNaoEncontradoException("CURSO_NAO_ENCONTRADO", "Curso não encontrado.");
    }

    public static RecursoNaoEncontradoException matricula(Long id) {
        return new RecursoNaoEncontradoException("MATRICULA_NAO_ENCONTRADA", "Matrícula não encontrada.");
    }
}
