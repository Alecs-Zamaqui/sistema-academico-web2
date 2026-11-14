package br.edu.example.sistema_academico.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "tb_matricula")
public class Matricula {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "aluno_id", nullable = false)
    private Aluno aluno;

    @ManyToOne(optional = false)
    @JoinColumn(name = "curso_id", nullable = false)
    private Curso curso;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatusMatricula status;

    @Column(nullable = false)
    private LocalDate dataMatricula;

    protected Matricula() {}

    public Matricula(Long id, Aluno aluno, Curso curso, StatusMatricula status) {
        this.id = id;
        this.aluno = aluno;
        this.curso = curso;
        this.status = status;
        this.dataMatricula = LocalDate.now();
    }

    public Long getId() { return id; }
    public Aluno getAluno() { return aluno; }
    public Curso getCurso() { return curso; }
    public StatusMatricula getStatus() { return status; }
    public void setStatus(StatusMatricula status) { this.status = status; }
    public LocalDate getDataMatricula() { return dataMatricula; }
}
