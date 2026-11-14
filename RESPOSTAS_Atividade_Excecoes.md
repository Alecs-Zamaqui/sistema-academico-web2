# Atividade Avaliativa – Tratamento Global de Exceções
**Disciplina:** Programação Web II — **Projeto:** sistema-academico
**Aluno(s):** _ALECSSANDRO DA SILVA ZAMAQUIZOMARE_

## Estrutura criada (Parte 15)
```
dto
└── error
    ├── CampoErroDTO
    └── ErroResponseDTO
exception
├── GlobalExceptionHandler
├── RegraNegocioException
└── RecursoNaoEncontradoException
```

## Fluxo implementado
```
Cliente → Controller → Service ──X── Exception → GlobalExceptionHandler → ErroResponseDTO → Resposta HTTP padronizada
```

## Handlers do GlobalExceptionHandler
| Exceção | Status | `erro` | `mensagem` |
|---|---|---|---|
| RecursoNaoEncontradoException | 404 | Recurso não encontrado | a da exceção (ex.: "Aluno não encontrado.") |
| RegraNegocioException | 400 | Regra de negócio violada | a da exceção |
| MethodArgumentNotValidException | 400 | Dados inválidos | "Existem campos inválidos na requisição." + lista `campos` |
| HttpMessageNotReadableException | 400 | JSON inválido | "Não foi possível interpretar os dados enviados." |
| MethodArgumentTypeMismatchException | 400 | Parâmetro inválido | "O valor informado para o parâmetro 'id' é inválido." |
| Exception (genérico) | 500 | Erro interno | "Ocorreu um erro inesperado." |

---
## Parte 10 – Limpeza dos Controllers (Atividades 26–28)
Revise `AlunoController`, `CursoController`, `MatriculaController` e `UsuarioController`:
1. Apague qualquer `try { ... } catch (RecursoNaoEncontradoException ...)` ou `catch (RegraNegocioException ...)`.
2. Deixe o Controller apenas chamando o Service, como:
```java
@GetMapping("/{id}")
public ResponseEntity<AlunoResponseDTO> consultar(@PathVariable Long id) {
    return ResponseEntity.ok(toResponse(service.consultar(id)));
}
```
3. Nos Services, troque os "não encontrado" por `throw RecursoNaoEncontradoException.aluno(id)` (ou `curso`, `matricula`) e regras de negócio por `RegraNegocioException`.
O `MatriculaController` do projeto já está nesse formato.

**Mensagens de validação:** para obter a mensagem do exemplo da Atividade 16, declare nos seus DTOs, por exemplo:
```java
@NotBlank(message = "Informe o nome.") String nome,
@Email(message = "Informe um e-mail válido.") String email
```

---
## Parte 11 – Testes no Swagger (`http://localhost:8080/swagger-ui.html`)
Execute cada teste e confira. (Os mesmos cenários estão automatizados em `GlobalExceptionHandlerTest`.)

| Atv. | Requisição | Esperado |
|---|---|---|
| 29 | `GET /api/alunos/99999` | 404, `"mensagem":"Aluno não encontrado."`, `"codigo":"ALUNO_NAO_ENCONTRADO"` |
| 30 | `POST /api/matriculas` repetido para o mesmo aluno/curso | 400, `"mensagem":"Aluno já possui matrícula ativa neste curso."` |
| 31 | `POST /api/alunos` com `{"nome":"","email":"x"}` | 400 com 2 itens em `campos` |
| 32 | `GET /api/alunos/abc` | 400, `"erro":"Parâmetro inválido"` |
| 33 | `POST /api/matriculas` com `"status":"DESCONHECIDA"` | 400, `"mensagem":"Não foi possível interpretar os dados enviados."` |

_Anexe prints do Swagger se o professor solicitar._

Exemplo de resposta 404:
```json
{
  "timestamp": "2026-11-12T15:00:00",
  "status": 404,
  "erro": "Recurso não encontrado",
  "mensagem": "Aluno não encontrado.",
  "path": "/api/alunos/99999",
  "campos": [],
  "codigo": "ALUNO_NAO_ENCONTRADO"
}
```

---
## Parte 12 – Tabela de códigos HTTP (Atividade 34)
| Situação | Status |
|---|---|
| Aluno inexistente | **404** Not Found |
| DTO inválido | **400** Bad Request |
| Regra de negócio | **400** Bad Request (adotado nesta aula) |
| JSON inválido | **400** Bad Request |
| Parâmetro incompatível | **400** Bad Request |
| Erro inesperado | **500** Internal Server Error |

---
## Parte 13 – Análise conceitual

**1. `throw new RecursoNaoEncontradoException(...)` × `404 Not Found`**
O `throw` é um mecanismo da linguagem Java: interrompe o fluxo dentro da aplicação e sinaliza que, no domínio, um recurso não existe — o Service não conhece HTTP. O `404 Not Found` é um código do protocolo HTTP, enviado ao cliente na resposta. O `GlobalExceptionHandler` faz a tradução: captura a exceção e a converte em resposta 404.

**2. Por que não usar try/catch em todos os Controllers?**
Gera código duplicado e poluído, mistura responsabilidades (o Controller passa a decidir como tratar erros), produz respostas inconsistentes entre endpoints, é fácil esquecer um tratamento (ou engolir uma exceção) e dificulta a manutenção: mudar o formato de erro exigiria alterar todos os Controllers.

**3. Função do `@RestControllerAdvice`**
Marca uma classe que atua como "conselheiro" global dos Controllers REST (equivale a `@ControllerAdvice` + `@ResponseBody`). Centraliza o tratamento de exceções para múltiplos Controllers e devolve o resultado já serializado em JSON.

**4. Função do `@ExceptionHandler`**
Indica que o método trata um tipo específico de exceção. Quando ela é lançada, o Spring escolhe o handler cujo tipo seja o mais específico, executa o método e usa seu retorno como resposta HTTP.

**5. Por que padronizar a resposta de erro?**
Porque o cliente (Front-End, apps, outras APIs) consegue tratar erros de forma previsível, com um único formato; facilita documentação, depuração e testes; melhora a consistência e profissionalismo da API; e permite controlar o que é exposto.

**6. Vantagem do `CampoErroDTO` na validação**
Entrega uma lista estruturada (campo + mensagem) com todos os erros de uma vez. O Front-End pode destacar cada campo do formulário e exibir a mensagem correspondente, em vez de interpretar um texto único.

**7. `400 Bad Request` × `404 Not Found`**
- **400:** a requisição do cliente é inválida ou malformada (dados inválidos, JSON quebrado, parâmetro de tipo errado, regra violada).
- **404:** a requisição está bem formada, mas o recurso solicitado não existe.
Exemplo: `GET /api/alunos/abc` → 400 (id mal formado); `GET /api/alunos/99999` → 404 (id válido, mas inexistente).

**8. Cenários para `409 Conflict`**
Quando a requisição é válida, mas conflita com o estado atual do recurso: matrícula ativa duplicada; cadastro de e-mail/CPF já existente (violação de unicidade); exclusão de aluno/curso com vínculos; transição de status incompatível; conflito de edição concorrente (versionamento otimista).

**9. Por que não retornar stack trace ao cliente?**
Expõe detalhes internos (classes, pacotes, bibliotecas e versões, SQL, nomes de tabelas, caminhos do servidor), o que ajuda atacantes (vazamento de informação) e não ajuda o usuário final. O cliente deve receber só uma mensagem controlada.

**10. Log do servidor × resposta HTTP**
O **log** é registro interno para desenvolvedores/operação, com detalhes completos (stack trace, SQL, contexto), mantido no servidor. A **resposta HTTP** é o contrato com o cliente: curta, padronizada e sem detalhes sensíveis. No handler de erro 500, o erro completo vai para o log (`log.error`) e o cliente recebe apenas "Ocorreu um erro inesperado."

---
## Parte 14 – Desafio adicional: campo `codigo`
Implementado em `ErroResponseDTO` (omitido do JSON quando nulo) e preenchido pelas exceções, por exemplo:
```json
{
  "codigo": "ALUNO_NAO_ENCONTRADO",
  "status": 404,
  "mensagem": "Aluno não encontrado."
}
```
**Como ajuda o Front-End:** o código é estável e legível por máquina, diferentemente da mensagem, que pode mudar de texto ou idioma. O Front pode decidir o comportamento pelo `codigo` (ex.: `MATRICULA_ATIVA_DUPLICADA` → avisar o usuário e ir para a lista de matrículas; `ALUNO_NAO_ENCONTRADO` → redirecionar), traduzir mensagens (i18n) e testar fluxos sem depender do texto.

---
## Parte 16 – Checklist de entrega
- [x] GlobalExceptionHandler criado
- [x] @RestControllerAdvice utilizado
- [x] @ExceptionHandler utilizado
- [x] erro 404 tratado
- [x] regra de negócio tratada
- [x] Validation tratada
- [x] enum inválido tratado
- [x] parâmetro inválido tratado
- [x] erro 500 tratado
- [x] ErroResponseDTO criado
- [x] CampoErroDTO criado
- [ ] Controllers sem try/catch repetitivo — _revise AlunoController, CursoController e UsuarioController do seu projeto_
- [x] respostas JSON padronizadas
- [ ] Swagger testado — _faça os testes da Parte 11_
- [ ] aplicação continua funcionando com PostgreSQL e Flyway — _confirme ao subir a aplicação_
