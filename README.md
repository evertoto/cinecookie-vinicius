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
Enquanto um filme não tem avaliações, sua nota inicial é exibida; após a primeira
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

## Como executar

Para executar tudo em containers, instale e inicie o Docker Desktop (ou Docker
Engine com Compose no Linux). Não é necessário instalar Java, Maven ou PostgreSQL
separadamente. Na raiz do projeto, execute:

```bash
docker compose up --build
```

Quando a aplicação terminar de iniciar, acesse
[http://localhost:8080](http://localhost:8080). O comando funciona também no
PowerShell do Windows. Na primeira execução, é necessária conexão com a internet
para baixar as imagens e dependências.

O build compila o projeto e executa os testes. A aplicação aguarda o PostgreSQL
ficar saudável antes de iniciar. Os usuários ficam preservados em um volume,
inclusive após encerrar os containers.

Se as portas padrão estiverem ocupadas, crie um arquivo `.env` na raiz com:

```dotenv
APP_PORT=8081
DB_PORT=5433
```

Nesse exemplo, abra [http://localhost:8081](http://localhost:8081).

### Desenvolvimento com Java local

Para executar a aplicação fora do container, é necessário Java 17 ou superior.
Inicie somente o banco:

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

Para encerrar a aplicação e o banco, preservando os dados:

```bash
docker compose down
```

Use `docker compose down -v` somente quando quiser remover também todos os
dados cadastrados no volume local.
