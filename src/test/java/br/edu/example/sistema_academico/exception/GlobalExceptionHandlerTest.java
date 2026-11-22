package br.edu.example.sistema_academico.exception;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.*;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Testa o handler isolado, com um controller falso que lança cada tipo de erro. */
class GlobalExceptionHandlerTest {

    MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(new ControllerDeTeste())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @RestController
    @RequestMapping("/api/teste")
    public static class ControllerDeTeste {

        public record AlunoDto(@NotBlank(message = "Informe o nome.") String nome,
                               @Email(message = "Informe um e-mail válido.") String email) {}

        public enum Status { ATIVA, TRANCADA }

        public record MatriculaDto(Long alunoId, Long cursoId, Status status) {}

        @GetMapping("/nao-encontrado")
        public void naoEncontrado() { throw RecursoNaoEncontradoException.aluno(999L); }

        @GetMapping("/regra")
        public void regra() { throw RegraNegocioException.matriculaAtivaJaExistente(); }

        @PostMapping("/alunos")
        public void validar(@Valid @RequestBody AlunoDto dto) { }

        @PostMapping("/matriculas")
        public void matricula(@RequestBody MatriculaDto dto) { }

        @GetMapping("/alunos/{id}")
        public void consultar(@PathVariable("id") Long id) { }

        @GetMapping("/matriculas/pesquisa")
        public void pesquisar(@RequestParam(value = "status", required = false) Status status) { }

        @GetMapping("/erro")
        public void erro() { throw new RuntimeException("SELECT * FROM tb_aluno WHERE senha='123' /home/app"); }
    }

    @Test
    @DisplayName("Recurso inexistente -> 404 com JSON padronizado")
    void recursoNaoEncontrado() throws Exception {
        mvc.perform(get("/api/teste/nao-encontrado"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.erro").value("Recurso não encontrado"))
                .andExpect(jsonPath("$.mensagem").value("Aluno não encontrado."))
                .andExpect(jsonPath("$.path").value("/api/teste/nao-encontrado"))
                .andExpect(jsonPath("$.codigo").value("ALUNO_NAO_ENCONTRADO"))
                .andExpect(jsonPath("$.campos").isEmpty());
    }

    @Test
    @DisplayName("Regra de negócio -> 400")
    void regraNegocio() throws Exception {
        mvc.perform(get("/api/teste/regra"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.mensagem").value("Aluno já possui matrícula ativa neste curso."));
    }

    @Test
    @DisplayName("DTO inválido -> 400 com os dois campos")
    void validacao() throws Exception {
        mvc.perform(post("/api/teste/alunos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"\",\"email\":\"email-invalido\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.length()").value(2))
                .andExpect(jsonPath("$.campos[*].campo", containsInAnyOrder("nome", "email")))
                .andExpect(jsonPath("$.campos[*].mensagem",
                        containsInAnyOrder("Informe o nome.", "Informe um e-mail válido.")));
    }

    @Test
    @DisplayName("Enum inexistente (status ABERTA) -> 400 com mensagem controlada")
    void enumInvalido() throws Exception {
        mvc.perform(post("/api/teste/matriculas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"alunoId\":1,\"cursoId\":1,\"status\":\"ABERTA\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("Não foi possível interpretar os dados enviados."))
                .andExpect(content().string(not(containsString("Exception"))));
    }

    @Test
    @DisplayName("Parâmetro incompatível (/alunos/abc) -> 400")
    void parametroInvalido() throws Exception {
        mvc.perform(get("/api/teste/alunos/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Parâmetro inválido"));
    }

    @Test
    @DisplayName("status=INVALIDO na pesquisa -> 400 com o padrão de erro")
    void statusInvalidoNaPesquisa() throws Exception {
        mvc.perform(get("/api/teste/matriculas/pesquisa").param("status", "INVALIDO"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.erro").value("Parâmetro inválido"))
                .andExpect(jsonPath("$.path").value("/api/teste/matriculas/pesquisa"));
    }

    @Test
    @DisplayName("Erro inesperado -> 500 sem vazar SQL, tabela, senha ou caminho")
    void erroInterno() throws Exception {
        mvc.perform(get("/api/teste/erro"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.erro").value("Erro interno"))
                .andExpect(jsonPath("$.mensagem").value("Ocorreu um erro inesperado."))
                .andExpect(content().string(not(containsString("SELECT"))))
                .andExpect(content().string(not(containsString("tb_aluno"))))
                .andExpect(content().string(not(containsString("senha"))))
                .andExpect(content().string(not(containsString("/home"))));
    }
}
