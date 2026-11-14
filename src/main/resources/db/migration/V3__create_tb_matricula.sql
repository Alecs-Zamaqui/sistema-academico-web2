CREATE TABLE tb_matricula (
    id             BIGSERIAL   PRIMARY KEY,
    aluno_id       BIGINT      NOT NULL,
    curso_id       BIGINT      NOT NULL,
    status         VARCHAR(30) NOT NULL,
    data_matricula DATE        NOT NULL,
    CONSTRAINT fk_matricula_aluno FOREIGN KEY (aluno_id) REFERENCES tb_aluno (id),
    CONSTRAINT fk_matricula_curso FOREIGN KEY (curso_id) REFERENCES tb_curso (id)
);

-- Garante no banco que não existam duas matrículas ATIVAS do mesmo aluno no mesmo curso
CREATE UNIQUE INDEX uk_matricula_ativa
    ON tb_matricula (aluno_id, curso_id)
    WHERE status = 'ATIVA';
