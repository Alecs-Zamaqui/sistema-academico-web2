package br.edu.example.sistema_academico.dto;

import br.edu.example.sistema_academico.model.StatusMatricula;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/** status é opcional; se informado, precisa ser um valor válido do enum e ATIVA para nova matrícula. */
public record MatriculaRequestDTO(
        @NotNull(message = "Informe o aluno.") @Positive(message = "Aluno inválido.") Long alunoId,
        @NotNull(message = "Informe o curso.") @Positive(message = "Curso inválido.") Long cursoId,
        StatusMatricula status) {
}
