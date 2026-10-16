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
- Flyway e PostgreSQL
- Thymeleaf
- HTML, CSS e JavaScript
- Maven

## Funcionalidades

- Home e catálogo de filmes.
- Busca e filtros por gênero.
- Página de detalhes de cada filme.
- Cadastro persistente com validações no backend.
- Login validado pelo banco e usuário autenticado mantido na sessão.

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

## Como executar

É necessário ter Java 17 ou superior e Docker instalados. Na raiz do projeto,
inicie o PostgreSQL:

```bash
docker compose up -d database
```

Em seguida, execute a aplicação:

```bash
./mvnw spring-boot:run
```

No Windows, utilize:

```powershell
mvnw.cmd spring-boot:run
```

Depois, acesse [http://localhost:8080](http://localhost:8080).

O Flyway executa automaticamente as migrations de
`src/main/resources/db/migration`. O Hibernate utiliza
`ddl-auto=validate`: ele confere se as entidades correspondem às tabelas, mas
não modifica o banco.

As configurações padrão podem ser substituídas por variáveis de ambiente:

```bash
export DB_URL=jdbc:postgresql://localhost:5432/cinecookie
export DB_USERNAME=cinecookie
export DB_PASSWORD=cinecookie
```

Para executar os testes:

```bash
./mvnw test
```

Os testes utilizam um banco H2 isolado e não alteram os dados do PostgreSQL.

Para encerrar o banco local:

```bash
docker compose down
```

Use `docker compose down -v` somente quando quiser remover também todos os
dados cadastrados no volume local.
