# CineCookie

Clone acadêmico do Letterboxd para descobrir filmes, consultar detalhes e
simular o cadastro e login de usuários.

## Integrantes

- Luka de Souza Lima
- Everton Fontes Alencar

## Tecnologias

- Java 17
- Spring Boot e Spring Web MVC
- Thymeleaf
- HTML, CSS e JavaScript
- Maven

## Funcionalidades

- Home e catálogo de filmes.
- Busca e filtros por gênero.
- Página de detalhes de cada filme.
- Cadastro e login integrados ao backend.
- Usuários armazenados em memória durante a execução.

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

É necessário ter o Java 17 ou superior instalado. Na raiz do projeto, execute:

```bash
./mvnw spring-boot:run
```

No Windows, utilize:

```powershell
mvnw.cmd spring-boot:run
```

Depois, acesse [http://localhost:8080](http://localhost:8080).

Para executar os testes:

```bash
./mvnw test
```

Os usuários são mantidos somente em memória e apagados quando a aplicação é
reiniciada. Banco de dados não é necessário nesta etapa do trabalho.
