CREATE TABLE tb_aluno (
    id    BIGSERIAL    PRIMARY KEY,
    nome  VARCHAR(120) NOT NULL,
    email VARCHAR(160) NOT NULL,
    CONSTRAINT uk_aluno_email UNIQUE (email)
);
