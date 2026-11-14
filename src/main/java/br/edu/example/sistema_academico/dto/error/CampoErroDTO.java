package br.edu.example.sistema_academico.dto.error;

public class CampoErroDTO {

    private String campo;
    private String mensagem;

    public CampoErroDTO(String campo, String mensagem) {
        this.campo = campo;
        this.mensagem = mensagem;
    }

    public String getCampo() { return campo; }
    public String getMensagem() { return mensagem; }
}
