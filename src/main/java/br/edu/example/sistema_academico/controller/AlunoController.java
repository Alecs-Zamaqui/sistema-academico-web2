package br.edu.example.sistema_academico.controller;

import br.edu.example.sistema_academico.model.Aluno;
import br.edu.example.sistema_academico.service.AlunoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/alunos")
@Tag(name = "Alunos", description = "API REST de gerenciamento de alunos")
public class AlunoController {

    private final AlunoService alunoService;

    public AlunoController(AlunoService alunoService) {
        this.alunoService = alunoService;
    }

    // GET /api/alunos - Listar todos (Status 200)
    @GetMapping
    @Operation(summary = "Listar todos os alunos")
    public ResponseEntity<List<Aluno>> listar() {
        List<Aluno> lista = alunoService.listarTodos();
        return ResponseEntity.ok(lista);
    }

    // GET /api/alunos/{id} - Consultar por ID (Status 200 ou 404)
    @GetMapping("/{id}")
    @Operation(summary = "Consultar aluno por ID")
    public ResponseEntity<Aluno> consultarPorId(@PathVariable Long id) {
        Optional<Aluno> aluno = alunoService.buscarPorId(id);
        return aluno.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // POST /api/alunos - Cadastrar novo aluno (Status 201)
    @PostMapping
    @Operation(summary = "Cadastrar novo aluno")
    public ResponseEntity<Aluno> cadastrar(@RequestBody Aluno aluno) {
        Aluno novoAluno = alunoService.cadastrar(aluno);
        return ResponseEntity.status(HttpStatus.CREATED).body(novoAluno);
    }

    // PUT /api/alunos/{id} - Atualizar aluno (Status 200 ou 404)
    @PutMapping("/{id}")
    @Operation(summary = "Atualizar aluno existente")
    public ResponseEntity<Aluno> atualizar(@PathVariable Long id, @RequestBody Aluno aluno) {
        Optional<Aluno> alunoAtualizado = alunoService.atualizar(id, aluno);
        return alunoAtualizado.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // DELETE /api/alunos/{id} - Excluir aluno (Status 204 ou 404)
    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir aluno por ID")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        boolean deletado = alunoService.excluir(id);
        if (deletado) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
