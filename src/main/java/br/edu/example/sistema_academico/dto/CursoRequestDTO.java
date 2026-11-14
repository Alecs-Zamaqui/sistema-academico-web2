package br.edu.example.sistema_academico.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CursoRequestDTO(
        @NotBlank(message = "Informe o nome do curso.") String nome,
        @NotNull(message = "Informe a carga horária.")
        @Positive(message = "A carga horária deve ser maior que zero.") Integer cargaHoraria) {
}
