CREATE TABLE tb_historico_matricula (
    id              BIGSERIAL   PRIMARY KEY,
    matricula_id    BIGINT      NOT NULL,
    status_anterior VARCHAR(30) NOT NULL,
    status_novo     VARCHAR(30) NOT NULL,
    data_hora       TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_historico_matricula FOREIGN KEY (matricula_id) REFERENCES tb_matricula (id)
);
