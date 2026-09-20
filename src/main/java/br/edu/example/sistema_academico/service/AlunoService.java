package br.edu.example.sistema_academico.service;

import br.edu.example.sistema_academico.model.Aluno;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class AlunoService {

    private final List<Aluno> alunos = new ArrayList<>();
    private Long proximoId = 1L;

    // Listar todos os alunos
    public List<Aluno> listarTodos() {
        return alunos;
    }

    // Consultar por ID
    public Optional<Aluno> buscarPorId(Long id) {
        return alunos.stream()
                .filter(aluno -> aluno.getId().equals(id))
                .findFirst();
    }

    // Cadastrar novo aluno
    public Aluno cadastrar(Aluno aluno) {
        aluno.setId(proximoId++);
        alunos.add(aluno);
        return aluno;
    }

    // Atualizar aluno existente
    public Optional<Aluno> atualizar(Long id, Aluno alunoAtualizado) {
        Optional<Aluno> alunoExistente = buscarPorId(id);
        if (alunoExistente.isPresent()) {
            Aluno aluno = alunoExistente.get();
            aluno.setNome(alunoAtualizado.getNome());
            aluno.setEmail(alunoAtualizado.getEmail());
            return Optional.of(aluno);
        }
        return Optional.empty();
    }

    // Excluir aluno
    public boolean excluir(Long id) {
        return alunos.removeIf(aluno -> aluno.getId().equals(id));
    }
}
