package br.edu.example.sistema_academico.exception;

import br.edu.example.sistema_academico.dto.error.CampoErroDTO;
import br.edu.example.sistema_academico.dto.error.ErroResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;

/**
 * @RestControllerAdvice: combina @ControllerAdvice + @ResponseBody. Permite CENTRALIZAR o
 * tratamento de exceções em uma única classe, valendo para MÚLTIPLOS Controllers da aplicação.
 * Assim os Controllers não precisam de try/catch repetitivo e todas as respostas de erro
 * seguem o mesmo formato JSON (ErroResponseDTO).
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ---- Recurso inexistente -> 404 ----
    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroResponseDTO> tratarRecursoNaoEncontrado(
            RecursoNaoEncontradoException ex, HttpServletRequest request) {
        return montar(HttpStatus.NOT_FOUND, "Recurso não encontrado",
                ex.getMessage(), request, List.of(), ex.getCodigo());
    }

    // ---- Regra de negócio -> 400 (decisão adotada nesta aula) ----
    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<ErroResponseDTO> tratarRegraNegocio(
            RegraNegocioException ex, HttpServletRequest request) {
        return montar(HttpStatus.BAD_REQUEST, "Regra de negócio violada",
                ex.getMessage(), request, List.of(), ex.getCodigo());
    }

    // ---- Jakarta Validation (@Valid no DTO) -> 400 com lista de campos ----
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponseDTO> tratarValidacao(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<CampoErroDTO> campos = ex.getBindingResult().getFieldErrors().stream()
                .map(erro -> new CampoErroDTO(erro.getField(), erro.getDefaultMessage()))
                .toList();
        return montar(HttpStatus.BAD_REQUEST, "Dados inválidos",
                "Existem campos inválidos na requisição.", request, campos, "VALIDACAO_INVALIDA");
    }

    // ---- JSON malformado / enum inexistente (ex.: status "ABERTA") -> 400 ----
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResponseDTO> tratarJsonInvalido(
            HttpMessageNotReadableException ex, HttpServletRequest request) {
        // O detalhe técnico (ex.getMessage()) vai só para o log, nunca para o cliente.
        log.warn("JSON ilegível em {} {}: {}", request.getMethod(), request.getRequestURI(),
                ex.getMostSpecificCause().getMessage());
        return montar(HttpStatus.BAD_REQUEST, "JSON inválido",
                "Não foi possível interpretar os dados enviados.", request, List.of(), "JSON_INVALIDO");
    }

    // ---- Parâmetro de tipo incompatível (ex.: /api/alunos/abc) -> 400 ----
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErroResponseDTO> tratarParametroInvalido(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        return montar(HttpStatus.BAD_REQUEST, "Parâmetro inválido",
                "O valor informado para o parâmetro '" + ex.getName() + "' é inválido.",
                request, List.of(), "PARAMETRO_INVALIDO");
    }

    // ---- Qualquer outra exceção -> 500 com mensagem controlada ----
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponseDTO> tratarErroInterno(Exception ex, HttpServletRequest request) {

        // Exceções do próprio Spring MVC (rota inexistente, método não suportado etc.)
        // já carregam o status correto; evitamos transformá-las em 500.
        if (ex instanceof ErrorResponse erroSpring) {
            HttpStatus status = HttpStatus.valueOf(erroSpring.getStatusCode().value());
            return montar(status, status.getReasonPhrase(),
                    "Não foi possível atender a requisição.", request, List.of(), null);
        }

        // Stack trace COMPLETA somente no log do servidor.
        log.error("Erro inesperado em {} {}", request.getMethod(), request.getRequestURI(), ex);

        // O cliente recebe apenas uma mensagem genérica: sem SQL, stack trace, tabela, senha ou caminhos.
        return montar(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno",
                "Ocorreu um erro inesperado.", request, List.of(), "ERRO_INTERNO");
    }

    private ResponseEntity<ErroResponseDTO> montar(HttpStatus status, String erro, String mensagem,
                                                   HttpServletRequest request,
                                                   List<CampoErroDTO> campos, String codigo) {
        ErroResponseDTO corpo = new ErroResponseDTO(
                status.value(), erro, mensagem, request.getRequestURI(), campos, codigo);
        return ResponseEntity.status(status).body(corpo);
    }
}
