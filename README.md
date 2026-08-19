# CineCookie

Clone acadêmico do Letterboxd com identidade visual baseada no aplicativo
CineCookie original.

## Estrutura

- `backend/`: aplicação Spring Boot, páginas Thymeleaf e arquivos estáticos.

O Thymeleaf é renderizado pelo Spring, por isso os templates e o CSS permanecem
no mesmo módulo. Se futuramente existir um aplicativo móvel ou outro cliente
independente, ele pode ser adicionado como um novo pacote no monorepo.

## Executar localmente

```bash
cd backend
./mvnw spring-boot:run
```

Acesse `http://localhost:8080`.
