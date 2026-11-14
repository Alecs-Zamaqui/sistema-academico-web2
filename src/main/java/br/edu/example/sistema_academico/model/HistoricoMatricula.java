package br.edu.example.sistema_academico.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/** Segunda alteração no banco usada para demonstrar o @Transactional (Ex. 05). */
@Entity
@Table(name = "tb_historico_matricula")
public class HistoricoMatricula {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long matriculaId;

    @Enumerated(EnumType.STRING)
    private StatusMatricula statusAnterior;

    @Enumerated(EnumType.STRING)
    private StatusMatricula statusNovo;

    private LocalDateTime dataHora = LocalDateTime.now();

    protected HistoricoMatricula() {}

    public HistoricoMatricula(Long matriculaId, StatusMatricula anterior, StatusMatricula novo) {
        this.matriculaId = matriculaId;
        this.statusAnterior = anterior;
        this.statusNovo = novo;
    }

    public Long getId() { return id; }
    public Long getMatriculaId() { return matriculaId; }
    public StatusMatricula getStatusAnterior() { return statusAnterior; }
    public StatusMatricula getStatusNovo() { return statusNovo; }
    public LocalDateTime getDataHora() { return dataHora; }
}
