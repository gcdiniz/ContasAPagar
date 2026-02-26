# API Contas a Pagar

API REST para gestão de contas a pagar. 

* CRUD de contas: criar, listar (paginado com filtros por data e descrição), buscar, atualizar e excluir.
* CRUD de fornecedores: criar, listar, buscar, atualizar e excluir.
* Controle de situação: transições de estado (PENDENTE -> PAGO/CANCELADO) com regras de domínio que impedem retorno.
* Relatório: total pago por período (soma dos valores de contas pagas entre duas datas).
* Importação de CSV assíncrona: upload do arquivo que retorna 202 e um protocolo imediatamente.
* Autenticação JWT — login com usuário/senha, todas as rotas protegidas por token Bearer.

## Stacks

* Java
* Spring Boot
* Spring Batch
* Spring Security 
* PostgreSQL
* Flyway
* RabbitMQ
* Swagger/OpenAPI 
* Docker

## Arquitetura

O projeto segue princípios de **DDD (Domain-Driven Design)** com separação entre camadas:

```
src/main/java/com/desafio/contaspagar/
├── domain/                     # Camada de Domínio
│   ├── entity/                 # Entidades (Conta, Fornecedor, Usuario)
│   ├── enums/                  # Enumerações (SituacaoConta)
│   └── exception/              # Exceções de domínio
├── application/                # Camada de Aplicação
│   ├── dto/                    # DTOs (Records)
│   └── service/                # Serviços de aplicação
├── infrastructure/             # Camada de Infraestrutura
│   ├── batch/                  # Spring Batch (Job, Reader, Processor, Writer, Listeners)
│   ├── config/                 # Configurações (RabbitMQ, OpenAPI, Exception Handler)
│   ├── messaging/              # Publisher e Consumer RabbitMQ
│   ├── repository/             # Repositórios JPA
│   └── security/               # Segurança JWT
└── interfaces/                 # Camada de Interface
    └── rest/                   # Controllers REST
```


## Como Executar

### Pré-requisitos
- Docker e Docker Compose instalados

### Configuração do `.env`

O Docker Compose utiliza um arquivo `.env` na raiz do projeto para parametrizar toda a infraestrutura. 

As variáveis disponíveis são:

| Variável | Descrição | Valor padrão |
|----------|-----------|--------------|
| `DB_HOST` | Host do PostgreSQL | `postgres` |
| `DB_PORT` | Porta do PostgreSQL | `5432` |
| `DB_NAME` | Nome do banco de dados | `contas_pagar` |
| `DB_USER` | Usuário do banco | `postgres` |
| `DB_PASSWORD` | Senha do banco | `postgres` |
| `RABBITMQ_HOST` | Host do RabbitMQ | `rabbitmq` |
| `RABBITMQ_PORT` | Porta do RabbitMQ | `5672` |
| `RABBITMQ_USER` | Usuário do RabbitMQ | `guest` |
| `RABBITMQ_PASSWORD` | Senha do RabbitMQ | `guest` |
| `JWT_SECRET` | Chave para assinatura HMAC-SHA dos tokens JWT (mínimo 256 bits) | *(definido no .env)* |
| `APP_PORT` | Porta exposta pela aplicação | `8080` |

>  O `JWT_SECRET` é utilizado para assinar e validar os tokens JWT. Deve ter no mínimo 32 caracteres (256 bits).

Crie um arquivo `.env` na raiz do projeto e preencha com seus dados. Exemplo:

```env
# =============================================
# Contas a Pagar - Variáveis de Ambiente
# =============================================

# --- PostgreSQL ---
DB_HOST=postgres
DB_PORT=5432
DB_NAME=contas_pagar
DB_USER=postgres
DB_PASSWORD=postgres

# --- RabbitMQ ---
RABBITMQ_HOST=rabbitmq
RABBITMQ_PORT=5672
RABBITMQ_USER=guest
RABBITMQ_PASSWORD=guest

# --- JWT ---
JWT_SECRET=minha-chave-secreta-super-segura-com-pelo-menos-256-bits-para-hmac-sha

# --- Aplicação ---
APP_PORT=8080
```

### Subir toda a infraestrutura + aplicação 

```bash
docker-compose up --build
```

Isso irá iniciar:
- **PostgreSQL** na porta `5432`
- **RabbitMQ** na porta `5672` (Management UI na `15672`)
- **Aplicação** na porta `8080`

### Acessos

| Recurso             | URL                                      |
|----------------------|------------------------------------------|
| Swagger UI           | http://localhost:8080/swagger-ui.html     |
| API Docs (JSON)      | http://localhost:8080/api-docs            |
| RabbitMQ Management  | http://localhost:15672 (guest/guest)      |

## Segurança

A aplicação utiliza **Spring Security** com autenticação stateless via JWT:

1. **Login** — O cliente envia usuario e senha para `POST /api/auth/login`. O Spring Security autentica via AuthenticationManager com senha verificada por **BCrypt**.
2. **Token JWT** — Após autenticação, um token JWT é gerado com o subject sendo o usuário, assinado com HMAC-SHA e com expiração configurável.
3. **Filtro** — Todas as requisições passam pelo JwtAuthenticationFilter, que extrai o token do header `Authorization: Bearer <token>`, valida a assinatura e expiração, e popula o SecurityContext.
4. **Endpoints públicos** — Apenas `/api/auth/**` e o Swagger (`/swagger-ui/**`, `/api-docs/**`) são acessíveis sem token. Todas as demais rotas exigem autenticação.
5. **Sem sessão** — `SessionCreationPolicy.STATELESS` garante que o servidor não mantém estado de sessão, cada request é autenticada individualmente pelo token.

### Obtendo um token

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"usuario": "admin", "senha": "admin123"}'
```

Use o token retornado no header `Authorization`:
```
Authorization: Bearer <token>
```

## Endpoints Principais

### Autenticação
| Método | Endpoint             | Descrição                |
|--------|----------------------|--------------------------|
| POST   | `/api/auth/login`    | Autenticar e obter token |

### Contas
| Método | Endpoint                         | Descrição                          |
|--------|----------------------------------|------------------------------------|
| POST   | `/api/contas`                    | Criar conta                        |
| GET    | `/api/contas`                    | Listar contas (paginado + filtros) |
| GET    | `/api/contas/{id}`               | Buscar conta por ID                |
| PUT    | `/api/contas/{id}`               | Atualizar conta                    |
| PATCH  | `/api/contas/{id}/situacao`      | Alterar situação da conta          |
| DELETE | `/api/contas/{id}`               | Excluir conta                      |
| GET    | `/api/contas/relatorio/total-pago` | Total pago por período           |
| POST   | `/api/contas/importar-csv`       | Importar contas via CSV            |

### Fornecedores
| Método | Endpoint                  | Descrição                  |
|--------|---------------------------|----------------------------|
| POST   | `/api/fornecedores`       | Criar fornecedor           |
| GET    | `/api/fornecedores`       | Listar fornecedores        |
| GET    | `/api/fornecedores/{id}`  | Buscar fornecedor por ID   |
| PUT    | `/api/fornecedores/{id}`  | Atualizar fornecedor       |
| DELETE | `/api/fornecedores/{id}`  | Excluir fornecedor         |

## Importação CSV com Spring Batch

O arquivo CSV deve seguir o formato (separador `;`):

```csv
data_vencimento;valor;descricao;fornecedor_id
2026-03-15;1500.00;Aluguel escritório;1
2026-03-20;350.50;Material de escritório;2
```

### Fluxo Assíncrono

O processamento é **assíncrono** via **RabbitMQ + Spring Batch**:

1. A API recebe o arquivo CSV e retorna um protocolo imediatamente (**HTTP 202 Accepted**)
2. O conteúdo CSV é publicado como mensagem no **RabbitMQ** (exchange `csv-import-exchange`)
3. Um **Consumer** escuta a fila e delega o processamento ao **Spring Batch**
4. O Spring Batch processa o CSV em chunks, validando e persistindo cada lote
5. Linhas com erro são **ignoradas (skip)** e registradas em log, sem impedir as demais

### Por que Spring Batch?

O **Spring Batch** é o framework padrão do ecossistema Spring para processamento em lote (batch processing) e se encaixa na importação de CSV por diversas razões:


**Chunk Processing**: Processa registros em lotes de N (configurado em 100), evitando carregar todo o arquivo em memória de uma vez. Cada chunk é commitado em sua própria transação.

**Fault Tolerance (Skip Policy)**: Linhas inválidas são automaticamente **puladas** sem abortar o job inteiro. Até 1000 erros são tolerados por importação (configurável).

**Separação de Responsabilidades**: A arquitetura **Reader → Processor → Writer** isola a leitura do CSV, a validação/transformação e a persistência em componentes distintos e testáveis.

**Transações por Chunk**: Se um chunk falha, apenas aquele lote de registros é revertido — os chunks anteriores já foram commitados com sucesso.

**Observabilidade**: O `JobExecution` fornece métricas automáticas: registros lidos, escritos, pulados e tempo de execução. Listeners customizados logam detalhes de cada erro.

**Escalabilidade**: Para volumes maiores, o Spring Batch suporta execução paralela de chunks, particionamento de arquivos e remote chunking sem alterar a lógica de negócio.

### Componentes do Batch

```
infrastructure/batch/
├── CsvImportBatchConfig.java    # Configuração do Job, Step, Reader e Writer
├── CsvImportJobLauncher.java    # Cria e executa Jobs dinamicamente por importação
├── ContaCsvRow.java             # DTO que mapeia cada linha do CSV
├── ContaCsvProcessor.java       # ItemProcessor: valida e converte CSV → Conta
├── CsvSkipListener.java         # Listener: loga linhas ignoradas por erro
└── CsvJobCompletionListener.java # Listener: loga resultado final do Job
```

### Configuração

| Parâmetro | Valor | Descrição |
|-----------|-------|-----------|
| `app.batch.chunk-size` | 100 | Registros processados por transação |
| `app.batch.skip-limit` | 1000 | Máximo de erros tolerados antes de abortar o job |
| `spring.batch.job.enabled` | false | Jobs não executam automaticamente ao iniciar a aplicação |
| `spring.batch.jdbc.initialize-schema` | always | Tabelas de metadados do Batch criadas automaticamente |


### Falhas parciais na importação CSV

O Spring Batch processa o CSV em **chunks de 100 registros**, cada chunk em sua própria transação. Quando uma linha é inválida:

1. O `ContaCsvProcessor` retorna `null` -> a linha é descartada do chunk
2. Se a exceção é do tipo _skippable_ (`Exception.class`), o **Skip Policy** a contabiliza e segue
3. O `CsvSkipListener` registra em log cada linha ignorada (fase de leitura, processamento ou escrita)
4. **As demais linhas do chunk — e os chunks seguintes — continuam normalmente**
5. Se o total de erros ultrapassar o `skip-limit` (padrão: 1000), o job é abortado

Isso significa que um CSV com 10.000 linhas e 50 inválidas resultará em ~9.950 contas importadas, sem perda das válidas.

## Testes

Executar os testes unitários:

```bash
./mvnw test
```
