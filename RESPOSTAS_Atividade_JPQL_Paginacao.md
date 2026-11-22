# Atividade – JPQL, Filtros e Paginação
**Disciplina:** Programação Web II — **Projeto:** sistema-academico
**Aluno(s):** _Alecssandro da Silva Zamaquizomare_

## O que foi implementado
| Item | Onde |
|---|---|
| Derived Query `findByNomeContainingIgnoreCase` (At. 3) | `AlunoRepository` |
| Busca por nome com JPQL, `@Query` e `@Param` (At. 6) + comentários (At. 7 e 8) | `AlunoRepository` |
| `GET /api/alunos/buscar?nome=` retornando `AlunoResponseDTO` (At. 4 e 5) | `AlunoService.buscarPorNome` (`readOnly`), `AlunoController` |
| Consultas por relacionamento, múltiplos critérios e JOIN (At. 9 a 12, 37, 38) | `MatriculaRepository` |
| Filtros opcionais com `:param IS NULL` + paginação (At. 13 a 15, 27 a 30) | `AlunoRepository.pesquisar`, `GET /api/alunos/pesquisa` |
| Listagem paginada e ordenada (At. 16 a 24) | `GET /api/alunos` com `page`, `size`, `sort`; retorna `Page<AlunoResponseDTO>` via `page.map(...)` |
| `PageRequest` manual (At. 25) | `PaginacaoUtil.criar(...)` |
| Limite de 100 registros por página (At. 45 e 46) | `PaginacaoUtil.limitar(...)` |
| Pesquisa paginada de matrículas (At. 31 a 36) | `MatriculaRepository.pesquisar`, `GET /api/matriculas/pesquisa` |
| Tratamento global da Semana 09 preservado (At. 47 e 48) | `GlobalExceptionHandler` (+ teste `status=INVALIDO`) |
| Desafio: `GET /api/cursos/pesquisa` (nome, cargaHorariaMinima) | `CursoRepository`, `CursoService`, `CursoController` |

**Observações de projeto**
- A ordenação só aceita campos permitidos (aluno: `id`, `nome`, `email`; curso: `id`, `nome`, `cargaHoraria`; matrícula: `id`, `status`, `dataMatricula`, `aluno.nome`, `curso.nome`). Campo inválido retorna 400 com o padrão de erro, e não erro 500.
- Nas consultas com filtro opcional de texto foi usado `CAST(:param AS String)` para o PostgreSQL identificar o tipo do parâmetro quando ele é `null`.
- Filtros em branco (`nome=`) são tratados como "não informado".

---
## Atividade 20 – Campos da resposta paginada
| Campo | Função |
|---|---|
| `content` | Lista com os registros da página atual |
| `totalElements` | Total de registros que atendem à consulta, somando todas as páginas |
| `totalPages` | Quantidade total de páginas (`totalElements / size`, arredondado para cima) |
| `size` | Tamanho da página solicitado (máximo de itens por página) |
| `number` | Índice da página atual (começa em 0) |
| `first` | `true` quando é a primeira página |
| `last` | `true` quando é a última página |

_Obs.: o Spring Data pode registrar no log um aviso sobre serializar `PageImpl` diretamente; o JSON continua com esses campos._

## Atividade 19 – Páginas 0 e 1
`page=0&size=5` retorna os 5 primeiros alunos; `page=1&size=5` retorna os alunos 6 a 10. Os `content` são diferentes, `number` muda de 0 para 1 e `first` passa a `false`. _(Anexe o resultado do seu teste.)_

## Atividade 23 – `sort=nome,asc` × `sort=nome,desc`
`asc` ordena do menor para o maior (A → Z); `desc` ordena do maior para o menor (Z → A). A segunda lista é a primeira invertida.

## Atividade 24 – Vários critérios
`sort=nome,asc&sort=email,asc` ordena primeiro por nome; quando há nomes iguais, o e-mail desempata.

## Atividade 26 – `Pageable` no Controller × `PageRequest` manual
- **`Pageable` direto no Controller** é adequado quando o cliente controla `page`, `size` e `sort` pela URL: o Spring monta o objeto sozinho, com pouco código e padrão uniforme entre endpoints.
- **`PageRequest` manual** é útil quando a regra define os valores (ordenação fixa, tamanho máximo, página padrão), quando os parâmetros têm nomes próprios, e em Services, jobs e testes que não recebem uma requisição HTTP.

## Atividade 39 – Por que não escrever `ON m.aluno_id = a.id`
Porque o relacionamento já está mapeado na entidade (`@ManyToOne` com `@JoinColumn(name = "aluno_id")`). Ao escrever `JOIN m.aluno a`, o JPA usa esse mapeamento e gera o `ON` correto no SQL. Isso evita erros e deixa a consulta independente dos nomes das colunas.

## Atividade 40 – JPQL × SQL nativo
`SELECT a FROM Aluno a` é JPQL: consulta a **entidade** `Aluno` e é traduzida para o SQL do banco em uso, retornando objetos gerenciados pelo JPA. `SELECT * FROM tb_aluno` com `nativeQuery = true` é **SQL puro** sobre a tabela `tb_aluno`, executado como está e dependente do banco.

## Atividade 41 – Duas vantagens do JPQL
1. **Portabilidade:** não depende do dialeto do banco; o Hibernate gera o SQL correto.
2. **Orientação a objetos:** navega por relacionamentos (`m.aluno.nome`) sem escrever JOIN manual, é validado na inicialização da aplicação e acompanha refatorações das entidades. Integra-se naturalmente com `Pageable` e `Sort`.

## Atividade 42 – Cenário para SQL nativo
Recursos específicos do PostgreSQL ou consultas muito complexas: busca textual (full text search), colunas JSONB, funções de janela (window functions), CTEs recursivas ou relatórios analíticos otimizados com hints.

## Atividade 43 – List × Page × Slice
| Tipo | Característica |
|---|---|
| `List<T>` | Apenas os registros, sem informação de paginação. Pode ser usada com resultado limitado, mas não sabe se há mais dados. |
| `Page<T>` | Registros da página + metadados completos (`totalElements`, `totalPages`). Executa uma consulta `COUNT` extra. |
| `Slice<T>` | Registros da página + se existe próxima página (`hasNext`). Não faz `COUNT`: busca `size + 1` registros para saber se há mais. |

## Atividade 44 – Por que `Page` exige `COUNT`
Para informar `totalElements` e `totalPages`, o `Page` precisa saber quantos registros existem no total que atendem ao filtro. Isso só é possível com uma consulta adicional `SELECT COUNT(...)` com os mesmos critérios. Em tabelas grandes, esse `COUNT` pode ser custoso.

## Atividade 45 – `GET /api/alunos?size=1000000`
A requisição obriga o servidor a carregar um volume enorme de registros em memória, convertê-los em DTOs e enviá-los pela rede. Isso causa lentidão, consumo excessivo de memória e CPU, risco de `OutOfMemoryError` e pode ser usado como ataque de negação de serviço. A paginação perde o sentido. Por isso a API limita o tamanho a 100 (`Math.min(size, 100)`).

---
## Parte 18 – Testes práticos (Swagger: `http://localhost:8080/swagger-ui.html`)
| # | Requisição | Esperado |
|---|---|---|
| 1 | `GET /api/alunos?page=0&size=5` | 200, até 5 alunos, `number: 0`, `first: true` |
| 2 | `GET /api/alunos?page=1&size=5` | 200, alunos 6 a 10, `number: 1` |
| 3 | `GET /api/alunos?sort=nome,asc` | 200, ordenado por nome crescente |
| 4 | `GET /api/alunos/pesquisa?nome=ana` | 200, alunos cujo nome contém "ana" |
| 5 | `GET /api/alunos/pesquisa?email=gmail` | 200, alunos com "gmail" no e-mail |
| 6 | `GET /api/alunos/pesquisa?nome=ana&email=gmail&page=0&size=5` | 200, os dois filtros combinados, página de 5 |
| 7 | `GET /api/matriculas/pesquisa?status=ATIVA` | 200, apenas matrículas ATIVA |
| 8 | `GET /api/matriculas/pesquisa?cursoId=1&status=ATIVA` | 200, matrículas ativas do curso 1 |
| 47/48 | `GET /api/matriculas/pesquisa?status=INVALIDO` | 400 com o padrão de erro (`"erro": "Parâmetro inválido"`) |

_Anexe prints do Swagger se o professor solicitar._

---
## Parte 19 – Questões conceituais

**1. Principal diferença entre SQL e JPQL?**
SQL opera sobre **tabelas e colunas** do banco e depende do dialeto. JPQL opera sobre **entidades e atributos** Java e é traduzido para o SQL do banco pelo provedor JPA.

**2. Função de `@Query`?**
Define explicitamente a consulta (JPQL ou SQL nativo) que o método do repository executa, em vez de derivá-la do nome do método.

**3. Função de `@Param`?**
Liga um parâmetro do método (`String nome`) ao parâmetro nomeado da consulta (`:nome`).

**4. Quando um método derivado é suficiente?**
Para consultas simples, com poucos critérios, como `findByNomeContainingIgnoreCase` ou `existsByEmail`, em que o nome do método continua legível.

**5. Quando JPQL é mais adequado?**
Quando há vários critérios, filtros opcionais (`:param IS NULL`), JOINs explícitos ou regras que deixariam o nome do método longo e ilegível.

**6. O que representa `Pageable`?**
A solicitação de uma página: número da página, tamanho e ordenação (`Sort`). O Repository usa essas informações para buscar só aquele trecho dos dados.

**7. `List<Aluno>` × `Page<Aluno>`?**
`List` devolve apenas os registros. `Page` devolve os registros da página mais metadados (`totalElements`, `totalPages`, `number`, `first`, `last`), à custa de um `COUNT` extra.

**8. Por que a primeira página é 0?**
Porque o índice da página funciona como deslocamento (offset = `page × size`) e segue a convenção de índices iniciando em zero do Java e do SQL: a página 0 começa no registro 0.

**9. Função de `Sort`?**
Define a ordenação do resultado (campo e direção asc/desc), podendo combinar vários critérios.

**10. Vantagem de `Page.map(...)`?**
Converte cada elemento (entidade → DTO) mantendo os metadados da paginação, sem montar uma nova página manualmente e sem expor entidades.

**11. Por que `findAll()` pode ser inadequado em tabelas grandes?**
Carrega todos os registros na memória de uma vez, causando lentidão, alto consumo de memória e tráfego de rede, e risco de falhas.

**12. Por que limitar `size`?**
Para impedir que o cliente peça páginas gigantes, protegendo memória, CPU, banco e rede (inclusive contra abuso), e manter o tempo de resposta previsível.

**13. O que significa uma consulta `readOnly`?**
Que a transação é apenas de leitura (`@Transactional(readOnly = true)`). Documenta a intenção, evita alterações acidentais e permite otimizações (por exemplo, o Hibernate não precisa fazer dirty checking).

**14. `Page<T>` × `Slice<T>`?**
`Page` conhece o total de registros e de páginas (executa `COUNT`). `Slice` só sabe se existe uma próxima página, sem `COUNT`, o que é mais leve; serve para "carregar mais" e rolagem infinita.

**15. Por que as regras de negócio continuam no Service?**
O JPQL e o Repository cuidam apenas de **acesso a dados**. Validar existência, transições de status, limites de página e demais regras no Service garante reuso, testabilidade, uma única fronteira transacional e Repositories sem decisões de negócio.

---
## Parte 20 – Desafio adicional
Implementado: `GET /api/cursos/pesquisa?nome=java&cargaHorariaMinima=40&page=0&size=10&sort=nome,asc`. Os dois filtros são opcionais, com paginação e ordenação (campos: `id`, `nome`, `cargaHoraria`).

---
## Parte 21 – Desafio de performance (1.000.000 de alunos)
1. **`findAll()` seria adequado?** Não. Carregaria um milhão de objetos na memória, com lentidão, alto consumo e risco de falha.
2. **Vantagem da paginação?** Traz apenas o necessário (por exemplo 20 registros), reduzindo memória, tempo de resposta e tráfego, com resultado previsível mesmo com a tabela crescendo.
3. **`Page` pode gerar `COUNT`?** Sim. Para calcular `totalElements` e `totalPages` ele executa `SELECT COUNT(...)` além da consulta dos dados, o que pode ser caro em tabelas enormes.
4. **`Slice` poderia ser interessante?** Sim. Como não executa o `COUNT`, é mais leve quando a interface só precisa saber se há próxima página (rolagem infinita, "carregar mais").
5. **Índices ainda seriam importantes?** Sim. Colunas usadas em filtros e ordenação (`nome`, `email`, chaves estrangeiras) precisam de índice para evitar leitura da tabela inteira. Observação: um índice comum não ajuda em `LIKE '%texto%'` (busca "contém"); para isso seriam necessários recursos como `pg_trgm`.

---
## Parte 22 – Checklist de entrega
- [x] pesquisa de aluno por nome implementada
- [x] JPQL utilizado
- [x] @Query utilizado
- [x] @Param utilizado
- [x] filtro opcional por nome
- [x] filtro opcional por e-mail
- [x] paginação implementada
- [x] Pageable utilizado
- [x] Page utilizado
- [x] ordenação implementada
- [x] PageRequest compreendido ou utilizado
- [x] consulta paginada de Matrícula implementada
- [x] filtros alunoId, cursoId e status
- [x] retorno utilizando DTO
- [x] tratamento global da Semana 09 preservado
- [ ] aplicação continua funcionando com PostgreSQL e Flyway — _confirme ao subir a aplicação_
- [ ] testes realizados via Swagger — _faça os testes da Parte 18_
