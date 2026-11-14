package br.edu.example.sistema_academico.dto.error;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.List;

public class ErroResponseDTO {

    private LocalDateTime timestamp;
    private Integer status;
    private String erro;
    private String mensagem;
    private String path;
    private List<CampoErroDTO> campos;

    // Desafio adicional (Parte 14): código estável para o Front-End; omitido do JSON quando nulo
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String codigo;

    public ErroResponseDTO(Integer status, String erro, String mensagem, String path,
                           List<CampoErroDTO> campos, String codigo) {
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.erro = erro;
        this.mensagem = mensagem;
        this.path = path;
        this.campos = campos == null ? List.of() : campos;
        this.codigo = codigo;
    }

    public ErroResponseDTO(Integer status, String erro, String mensagem, String path,
                           List<CampoErroDTO> campos) {
        this(status, erro, mensagem, path, campos, null);
    }

    public LocalDateTime getTimestamp() { return timestamp; }
    public Integer getStatus() { return status; }
    public String getErro() { return erro; }
    public String getMensagem() { return mensagem; }
    public String getPath() { return path; }
    public List<CampoErroDTO> getCampos() { return campos; }
    public String getCodigo() { return codigo; }
}
