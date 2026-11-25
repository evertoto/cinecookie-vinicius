# CineCookie

Clone acadêmico do Letterboxd para descobrir filmes, consultar detalhes e
cadastrar usuários com persistência em banco de dados.

## Integrantes

- Luka de Souza Lima
- Everton Fontes Alencar

## Tecnologias

- Java 17
- Spring Boot e Spring Web MVC
- Spring Data JPA e Bean Validation
- Flyway e PostgreSQL (Neon)
- Spring Boot Actuator (health check)
- Docker e Docker Compose
- Render (deploy)
- Thymeleaf
- HTML, CSS e JavaScript
- Maven

## Funcionalidades

- Home e catálogo de filmes persistidos no PostgreSQL.
- Busca e filtros por gênero.
- Página de detalhes de cada filme.
- Cadastro persistente com validações no backend.
- Login validado pelo banco e usuário autenticado mantido na sessão.
- Login redireciona para a home; o cabeçalho exibe nome, usuário, avatar padrão e botão de sair.
- Avaliações com nota de 0,5 a 5 e comentário opcional de até 1000 caracteres.
- Uma avaliação por usuário e filme; novos envios atualizam a própria avaliação.
- Nota média calculada pelas avaliações e atividade recente na home.

## Relacionamentos JPA

`Movie` e `Genre` usam `@ManyToMany` com a tabela `movie_genres`.
`Review` possui dois relacionamentos `@ManyToOne`: um com `User` e outro com
`Movie`. As chaves estrangeiras e a unicidade de usuário/filme são garantidas
pelas migrations. Não há cascade de remoção entre essas entidades.

O catálogo inicial e seus gêneros são inseridos pelo Flyway uma única vez.
Enquanto um filme não tem avaliações, sua nota aparece como `-`; após a primeira
avaliação, a nota passa a ser a média dos usuários.

## Principais rotas

| Método | Rota | Função |
| --- | --- | --- |
| GET | `/` | Página inicial |
| GET | `/filmes` | Catálogo de filmes |
| GET | `/filmes/{id}` | Detalhes de um filme |
| GET | `/cadastro` | Formulário de cadastro |
| POST | `/cadastro` | Processa e armazena o cadastro |
| GET | `/login` | Formulário de login |
| POST | `/login` | Valida os dados de acesso |
| POST | `/logout` | Encerra a sessão e retorna à home |
| POST | `/filmes/{id}/avaliacoes` | Cria ou atualiza a avaliação do usuário da sessão |

## Variáveis de ambiente

A aplicação não guarda segredos no código: o `application.properties` apenas
lê valores do ambiente.

| Variável | Obrigatória | Descrição |
| --- | --- | --- |
| `DB_URL` | Sim | URL JDBC do PostgreSQL, ex.: `jdbc:postgresql://HOST/BANCO?sslmode=require` |
| `DB_USER` | Sim | Usuário do banco |
| `DB_PASSWORD` | Sim | Senha do banco |
| `SPRING_PROFILES_ACTIVE` | Não | `dev` (logs `DEBUG`, cache de templates desligado) ou `prod` |
| `PORT` | Não | Porta da aplicação; o Render define sozinho. Padrão: `8080` |
| `APP_PORT` | Não | Porta publicada no computador pelo Compose. Padrão: `8080` |

Os arquivos `.env`, `.env.dev` e `.env.prod` com credenciais reais ficam fora
do Git (estão no `.gitignore`).

## Como executar

### Com Docker Compose (banco no Neon)

Pré-requisito: Docker Desktop (ou Docker Engine com Compose) e um banco
PostgreSQL criado no [Neon](https://neon.tech). Não é necessário instalar Java
nem Maven.

1. Crie um arquivo `.env.dev` na raiz do projeto e preencha com os dados do seu banco:

   ```dotenv
   DB_URL=jdbc:postgresql://HOST/BANCO?sslmode=require
   DB_USER=usuario
   DB_PASSWORD=senha
   ```

   Use o host direto do Neon, sem `-pooler`, para executar as migrations do Flyway.

2. Suba a aplicação:

   ```bash
   docker compose --env-file .env.dev up --build
   ```

3. Acesse [http://localhost:8080](http://localhost:8080). O Flyway cria as
   tabelas e o catálogo inicial no banco na primeira execução.

Comandos úteis:

```bash
docker compose --env-file .env.dev up -d      # segundo plano
docker compose --env-file .env.dev ps         # serviço, status e porta
docker compose --env-file .env.dev logs -f app # logs
docker compose --env-file .env.dev down       # encerra (os dados ficam no Neon)
```

Se a porta 8080 estiver ocupada, defina `APP_PORT=8081` no `.env.dev`.

### Sem Docker (Java 17+)

Defina as variáveis no terminal e execute com o Maven Wrapper:

```bash
export DB_URL="jdbc:postgresql://HOST/BANCO?sslmode=require"
export DB_USER="usuario"
export DB_PASSWORD="senha"
export SPRING_PROFILES_ACTIVE=dev
./mvnw spring-boot:run
```

No Windows (PowerShell), troque `export X=...` por `$env:X="..."` e use
`mvnw.cmd spring-boot:run`.

### Build e execução do JAR

```bash
./mvnw clean package          # Windows: mvnw.cmd clean package
java -jar target/app.jar
```

O JAR (`target/app.jar`) roda sem IDE; as variáveis acima precisam estar definidas.

### Testes

```bash
./mvnw test
```

Os testes usam um banco H2 em memória (profile `test`) e não tocam no Neon.
O GitHub Actions (`.github/workflows/ci.yml`) roda o build e os testes a cada
push e pull request.

## Banco e migrations

O Flyway executa as migrations de `src/main/resources/db/migration` ao iniciar,
inclusive em um banco vazio (V1 → V4). O Hibernate usa `ddl-auto=validate`:
confere se as entidades correspondem às tabelas, mas não altera o banco.
**Nunca edite uma migration já aplicada**; crie uma nova versão (V5, V6...).
Os dados de V3 são o catálogo de filmes da aplicação; não há usuários fictícios.

## Profiles, logs e health check

- `application.properties`: configuração comum.
- `application-dev.properties`: logs `DEBUG` da aplicação e cache de templates desligado.
- `application-prod.properties`: logs `INFO` e cache de templates ligado.
- Erros não expõem stack trace, SQL ou caminhos internos; o usuário vê a página `error.html`.
- Health check: `GET /actuator/health` retorna `{"status":"UP"}` (somente esse endpoint é exposto).

## Deploy no Render

Arquitetura: o **Render** executa a aplicação (Dockerfile) e o **Neon** guarda
o banco. O `compose.yaml` é só para uso local; o Render usa o `Dockerfile`.

1. Envie o projeto para o GitHub (sem `.env*` com credenciais).
2. No Render: **New → Web Service**, conecte o repositório e escolha a branch `main`.
3. **Runtime/Language: Docker** (o Render encontra o `Dockerfile`).
4. Em **Environment Variables**, cadastre:
   - `DB_URL` (JDBC do Neon de produção, com `?sslmode=require`)
   - `DB_USER`
   - `DB_PASSWORD`
   - `SPRING_PROFILES_ACTIVE=prod`
5. Em **Advanced → Health Check Path**, informe `/actuator/health`.
6. **Create Web Service** e acompanhe o build em **Logs**.
7. A aplicação fica em `https://<nome-do-servico>.onrender.com`.

A porta vem da variável `PORT` do Render (`server.port=${PORT:8080}`). Idealmente,
use bancos Neon diferentes para desenvolvimento e produção.

Auto-deploy: cada push na `main` dispara um novo build e deploy no Render.
No plano gratuito, o serviço é suspenso após cerca de 15 minutos sem tráfego e a
primeira requisição seguinte demora mais para responder; isso é esperado.
