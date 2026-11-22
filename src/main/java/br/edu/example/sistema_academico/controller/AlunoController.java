package br.edu.example.sistema_academico.controller;

import br.edu.example.sistema_academico.dto.AlunoRequestDTO;
import br.edu.example.sistema_academico.dto.AlunoResponseDTO;
import br.edu.example.sistema_academico.model.Aluno;
import br.edu.example.sistema_academico.service.AlunoService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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

    // Atividades 16 a 24: GET /api/alunos?page=0&size=5&sort=nome,asc
    @GetMapping
    public ResponseEntity<Page<AlunoResponseDTO>> listar(
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(service.listar(pageable).map(this::toResponse));
    }

    // Atividade 5: GET /api/alunos/buscar?nome=ana (retorna DTOs, não entidades)
    @GetMapping("/buscar")
    public ResponseEntity<List<AlunoResponseDTO>> buscar(@RequestParam String nome) {
        return ResponseEntity.ok(service.buscarPorNome(nome).stream().map(this::toResponse).toList());
    }

    // Atividades 13, 14 e 27 a 30: filtros opcionais + paginação + ordenação
    @GetMapping("/pesquisa")
    public ResponseEntity<Page<AlunoResponseDTO>> pesquisar(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) String email,
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(service.pesquisar(nome, email, pageable).map(this::toResponse));
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
