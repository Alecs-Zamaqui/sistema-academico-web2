package br.edu.example.sistema_academico.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AlunoRequestDTO(
        @NotBlank(message = "Informe o nome.") String nome,
        @NotBlank(message = "Informe o e-mail.")
        @Email(message = "Informe um e-mail válido.") String email) {
}
