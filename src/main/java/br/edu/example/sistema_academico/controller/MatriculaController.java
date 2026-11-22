package br.edu.example.sistema_academico.controller;

import br.edu.example.sistema_academico.dto.MatriculaRequestDTO;
import br.edu.example.sistema_academico.dto.MatriculaResponseDTO;
import br.edu.example.sistema_academico.model.Matricula;
import br.edu.example.sistema_academico.service.MatriculaService;
import br.edu.example.sistema_academico.model.StatusMatricula;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/matriculas")
public class MatriculaController {

    private final MatriculaService service;

    public MatriculaController(MatriculaService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<MatriculaResponseDTO> matricular(@Valid @RequestBody MatriculaRequestDTO dto) {
        Matricula m = service.matricular(dto.alunoId(), dto.cursoId(), dto.status());
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(m));
    }

    @GetMapping
    public ResponseEntity<List<MatriculaResponseDTO>> listar() {
        return ResponseEntity.ok(service.listar().stream().map(this::toResponse).toList());
    }

    // Atividades 34 a 36 e 47/48:
    // GET /api/matriculas/pesquisa?cursoId=1&status=ATIVA&page=0&size=5&sort=id,desc
    @GetMapping("/pesquisa")
    public ResponseEntity<Page<MatriculaResponseDTO>> pesquisar(
            @RequestParam(required = false) Long alunoId,
            @RequestParam(required = false) Long cursoId,
            @RequestParam(required = false) StatusMatricula status,
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        return ResponseEntity.ok(service.pesquisar(alunoId, cursoId, status, pageable).map(this::toResponse));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MatriculaResponseDTO> consultar(@PathVariable Long id) {
        return ResponseEntity.ok(toResponse(service.consultar(id)));
    }

    @PatchMapping("/{id}/trancar")
    public ResponseEntity<MatriculaResponseDTO> trancar(@PathVariable Long id) {
        return ResponseEntity.ok(toResponse(service.trancarMatricula(id)));
    }

    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<MatriculaResponseDTO> cancelar(@PathVariable Long id) {
        return ResponseEntity.ok(toResponse(service.cancelarMatricula(id)));
    }

    @PatchMapping("/{id}/concluir")
    public ResponseEntity<MatriculaResponseDTO> concluir(@PathVariable Long id) {
        return ResponseEntity.ok(toResponse(service.concluirMatricula(id)));
    }

    // Apenas para demonstrar o rollback (Exercício 05)
    @PatchMapping("/{id}/cancelar-com-falha")
    public void cancelarComFalha(@PathVariable Long id) {
        service.cancelarMatriculaComFalhaSimulada(id);
    }

    private MatriculaResponseDTO toResponse(Matricula m) {
        return new MatriculaResponseDTO(m.getId(), m.getAluno().getId(),
                m.getCurso().getId(), m.getStatus().name());
    }
}
