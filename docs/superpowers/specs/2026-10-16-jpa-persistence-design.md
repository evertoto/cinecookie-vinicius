# CineCookie JPA Persistence Design

## Objective

Evolve the current Spring Boot and Thymeleaf prototype into a database-backed application, following the sequence taught in classes 12 through 16: JPA entities and Flyway migrations, repositories, backend validation, services and JPA relationships.

## Chosen approach

The implementation will be incremental and domain-oriented. The existing visual design and public routes will remain available while hard-coded and in-memory data are replaced by PostgreSQL persistence. Each classroom topic will produce a working, testable checkpoint before the next topic is introduced.

PostgreSQL is the production database. Flyway is the only mechanism allowed to create or evolve the schema, and Hibernate will run with `ddl-auto=validate`. Tests use an isolated H2 database in PostgreSQL compatibility mode so the Maven test suite does not depend on a developer's local database.

## Architecture

The application will use the flow demonstrated in class:

```text
HTTP request -> Controller -> Service -> Repository -> PostgreSQL
                     |             |
                 Thymeleaf     business rules
```

- Controllers handle routes, form binding, `BindingResult`, redirects and template models.
- Services contain normalization, duplicate checks, authentication and review rules.
- Repositories extend `JpaRepository` and contain only persistence queries.
- Entities map the database schema explicitly with JPA annotations.
- Form DTOs carry input and Bean Validation annotations; entities are not used directly as web forms.

The current monolithic `PageController` will be separated only where necessary into catalog and authentication responsibilities. Spring Security is outside this iteration; a successful login stores the persisted user ID in `HttpSession` solely to support the academic review flow.

## Domain model

### User

`User` represents a registered CineCookie account.

- `id: Long`, generated with `IDENTITY`.
- `name`, required, 2 to 100 characters.
- `username`, required, unique and normalized for comparisons.
- `email`, required, unique and normalized for comparisons.
- `passwordHash`, required; plain-text passwords are never persisted.
- `createdAt`, required.

### Movie

`Movie` replaces the hard-coded `MovieView` list.

- `id: Long`, generated with `IDENTITY`.
- `slug`, required and unique, used by `/filmes/{slug}`.
- `title`, duration, age rating, poster path and initial community score.
- Initial catalog records are inserted once by a versioned Flyway seed migration.

### Genre

`Genre` stores reusable catalog categories. Movies and genres form a bidirectional many-to-many relationship through `movie_genres`. Cascade remove is not used because a genre can belong to several movies.

### Review

`Review` connects one user to one movie through two required many-to-one relationships.

- `rating`, from 0.5 to 5.0.
- Optional `comment`, at most 1000 characters.
- `createdAt` and `updatedAt`.
- A database unique constraint on `(user_id, movie_id)` ensures one review per user and movie; submitting again updates the existing review through the service.

## Migrations

Migrations live in `src/main/resources/db/migration` and are immutable after application:

1. `V1__create_users.sql`
2. `V2__create_movies_and_genres.sql`
3. `V3__seed_catalog.sql`
4. `V4__create_reviews.sql`

Column lengths, nullability and unique constraints match the JPA and Bean Validation rules. Future schema changes require a new migration rather than editing an applied file.

## Web behavior

- `/`, `/filmes` and `/filmes/{slug}` read movies through `MovieService`.
- `/cadastro` binds a validated `RegistrationForm`; invalid fields render beside their inputs.
- Successful registration persists a user and redirects to `/login`.
- `/login` binds a validated `LoginForm`; successful authentication stores `userId` in the session.
- The movie details page shows persisted genres and reviews.
- A logged-in user can submit a validated review form on the details page.
- Duplicate email/username and duplicate review behavior are expressed as service rules and protected again by database constraints.

## Error handling

- Field-level format errors return the same form with Thymeleaf error messages.
- Business conflicts such as duplicate email or username become form-level messages.
- An unknown movie slug redirects to `/filmes` as it does today.
- Database constraint violations are not exposed as stack traces to users.

## Configuration and local execution

Database connection values come from environment variables with development defaults. A `compose.yaml` provides a local PostgreSQL service, while `mvn spring-boot:run` starts the Spring Boot application. The README will document both commands and explain that Flyway applies pending migrations automatically at startup.

## Verification

The implementation will include:

- repository tests for persistence and relationship mappings;
- service tests for duplicate registration, authentication and review upsert rules;
- MockMvc tests for valid and invalid forms, redirects and persisted catalog pages;
- a full `./mvnw test` run;
- a PostgreSQL startup check that proves Flyway migrations and Hibernate validation succeed together.

## Out of scope

- Spring Security and production-grade authentication;
- password recovery, authorization roles and social features;
- external movie APIs;
- administration screens for editing the catalog.
