# CineCookie

Equipe -
    Luka de Souza Lima
    Everton Fontes Alencar

Clone acadêmico do Letterboxd com identidade visual baseada em cookies.

## Estrutura

- `pom.xml`: configuração do projeto Maven.
- `src/main/java`: aplicação Spring Boot e controllers MVC.
- `src/main/resources/templates`: páginas renderizadas com Thymeleaf.
- `src/main/resources/static`: CSS, JavaScript, fontes e imagens.
- `src/test/java`: testes automatizados.

O Thymeleaf é renderizado pelo Spring, por isso o front-end e o back-end fazem
parte do mesmo módulo Maven. Se futuramente existir um aplicativo móvel ou outro
cliente independente, ele pode ser adicionado como um novo módulo no monorepo.

## Dados simulados

Cadastro e login são processados pelo backend Spring Boot. As contas ficam em
uma lista em memória e são apagadas quando a aplicação é reiniciada, dispensando
banco de dados nesta etapa do trabalho.

## Requisitos

- Java 17 ou superior.
- Maven 3.9 ou superior.

## Executar localmente

```bash
mvn spring-boot:run
```

Acesse [http://localhost:8080](http://localhost:8080).

Se o Maven não estiver instalado globalmente, o Maven Wrapper incluído no
repositório continua disponível:

```bash
./mvnw spring-boot:run
```

## Verificar o projeto

```bash
mvn clean verify
```
