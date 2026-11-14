package br.edu.example.sistema_academico.controller;

import br.edu.example.sistema_academico.dto.CursoRequestDTO;
import br.edu.example.sistema_academico.dto.CursoResponseDTO;
import br.edu.example.sistema_academico.model.Curso;
import br.edu.example.sistema_academico.service.CursoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cursos")
public class CursoController {

    private final CursoService service;

    public CursoController(CursoService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<CursoResponseDTO> cadastrar(@Valid @RequestBody CursoRequestDTO dto) {
        Curso curso = service.cadastrar(dto.nome(), dto.cargaHoraria());
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(curso));
    }

    @GetMapping
    public ResponseEntity<List<CursoResponseDTO>> listar() {
        return ResponseEntity.ok(service.listar().stream().map(this::toResponse).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CursoResponseDTO> consultar(@PathVariable Long id) {
        return ResponseEntity.ok(toResponse(service.consultar(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CursoResponseDTO> atualizar(@PathVariable Long id,
                                                      @Valid @RequestBody CursoRequestDTO dto) {
        return ResponseEntity.ok(toResponse(service.atualizar(id, dto.nome(), dto.cargaHoraria())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }

    private CursoResponseDTO toResponse(Curso c) {
        return new CursoResponseDTO(c.getId(), c.getNome(), c.getCargaHoraria());
    }
}
