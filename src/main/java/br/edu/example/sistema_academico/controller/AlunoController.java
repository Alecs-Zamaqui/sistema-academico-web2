package br.edu.example.sistema_academico.controller;

import br.edu.example.sistema_academico.dto.AlunoRequestDTO;
import br.edu.example.sistema_academico.dto.AlunoResponseDTO;
import br.edu.example.sistema_academico.model.Aluno;
import br.edu.example.sistema_academico.service.AlunoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alunos")
public class AlunoController {

    private final AlunoService service;

    public AlunoController(AlunoService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<AlunoResponseDTO> cadastrar(@Valid @RequestBody AlunoRequestDTO dto) {
        Aluno aluno = service.cadastrar(dto.nome(), dto.email());
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(aluno));
    }

    @GetMapping
    public ResponseEntity<List<AlunoResponseDTO>> listar() {
        return ResponseEntity.ok(service.listar().stream().map(this::toResponse).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AlunoResponseDTO> consultar(@PathVariable Long id) {
        return ResponseEntity.ok(toResponse(service.consultar(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AlunoResponseDTO> atualizar(@PathVariable Long id,
                                                      @Valid @RequestBody AlunoRequestDTO dto) {
        return ResponseEntity.ok(toResponse(service.atualizar(id, dto.nome(), dto.email())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }

    private AlunoResponseDTO toResponse(Aluno a) {
        return new AlunoResponseDTO(a.getId(), a.getNome(), a.getEmail());
    }
}
