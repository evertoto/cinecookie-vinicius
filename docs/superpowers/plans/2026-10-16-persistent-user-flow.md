# Persistent User Flow Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the simulated in-memory user store with a validated JPA user flow backed by Flyway-managed PostgreSQL.

**Architecture:** The web layer binds dedicated form DTOs and delegates registration and authentication to `UserService`. The service owns normalization, duplicate checks and password hashing, while `UserRepository` owns persistence. Flyway creates the schema and Hibernate only validates it.

**Tech Stack:** Java 17, Spring Boot 4.1, Spring MVC, Thymeleaf, Bean Validation, Spring Data JPA, Flyway, PostgreSQL, H2 tests, Maven.

**Spec:** `docs/superpowers/specs/2026-10-16-jpa-persistence-design.md`

## Global Constraints

- PostgreSQL is the runtime database and Flyway is the only schema mutation mechanism.
- `spring.jpa.hibernate.ddl-auto=validate` must remain enabled outside tests.
- Existing public routes and visual design must remain compatible.
- Controllers handle HTTP concerns, services handle rules, repositories handle persistence.
- Plain-text passwords must never be persisted.
- Spring Security is outside this phase.
- Existing applied migrations must never be edited; future changes require new versions.

## Review Focus

- Whitespace and letter-case variants of an email must resolve to the same persisted account; covered by Task 3 service tests.
- Whitespace and letter-case variants of a username must be rejected as duplicates; covered by Task 3 service tests.
- Invalid form submissions must preserve entered non-password fields and expose field errors; covered by Task 4 MockMvc tests.
- Wrong passwords and unknown emails must produce the same generic login error; covered by Task 3 and Task 4 tests.
- Repeated test runs must not leak registered users between methods; covered by Task 4 transactional/database cleanup setup.

---

### Task 1: Database and migration foundation

**Files:**
- Modify: `pom.xml`
- Modify: `src/main/resources/application.properties`
- Create: `src/test/resources/application-test.properties`
- Create: `src/main/resources/db/migration/V1__create_users.sql`
- Create: `compose.yaml`
- Modify: `src/test/java/br/com/cinecookie/CineCookieApplicationTests.java`

**Interfaces:**
- Consumes: the current Spring Boot Maven project.
- Produces: a Flyway-managed `users` table and runtime/test datasource profiles used by all later tasks.

- [ ] **Step 1: Add a failing context test using the `test` profile**

Annotate `CineCookieApplicationTests` with `@ActiveProfiles("test")` and keep `contextLoads()` as the schema bootstrap assertion.

- [ ] **Step 2: Run the context test to verify missing datasource/JPA support fails**

Run: `./mvnw -Dtest=CineCookieApplicationTests#contextLoads test`

Expected: FAIL because the project does not yet provide the JPA datasource and migration setup.

- [ ] **Step 3: Add persistence dependencies and datasource configuration**

Add `spring-boot-starter-data-jpa`, PostgreSQL runtime driver, `flyway-core`, `flyway-database-postgresql` and test-scoped H2. Configure environment-backed PostgreSQL defaults, Flyway enabled and `ddl-auto=validate`; configure H2 PostgreSQL compatibility in the test profile.

- [ ] **Step 4: Add the initial users migration**

Create `V1__create_users.sql` with `id`, `name`, `username`, `normalized_username`, `email`, `password_hash` and `created_at`. Use explicit nullability, lengths and unique constraints on normalized username and email.

- [ ] **Step 5: Add local PostgreSQL Compose service**

Create a PostgreSQL 17 service named `database`, a health check, a named volume and defaults matching `application.properties`.

- [ ] **Step 6: Run the context test**

Run: `./mvnw -Dtest=CineCookieApplicationTests#contextLoads test`

Expected: PASS with Flyway applying V1 and Hibernate startup succeeding.

- [ ] **Step 7: Commit**

```bash
git add pom.xml compose.yaml src/main/resources/application.properties src/main/resources/db/migration/V1__create_users.sql src/test/resources/application-test.properties src/test/java/br/com/cinecookie/CineCookieApplicationTests.java
git commit -m "feat: add Flyway persistence foundation"
```

### Task 2: User entity and repository

**Files:**
- Create: `src/main/java/br/com/cinecookie/user/User.java`
- Create: `src/main/java/br/com/cinecookie/user/UserRepository.java`
- Create: `src/test/java/br/com/cinecookie/user/UserRepositoryTests.java`

**Interfaces:**
- Consumes: the `users` table from Task 1.
- Produces: `UserRepository.findByEmail(String)`, `existsByEmail(String)`, `existsByNormalizedUsername(String)` and the persisted `User` aggregate.

- [ ] **Step 1: Write failing repository tests**

Add tests named `savesAndFindsUserByEmail`, `rejectsDuplicateEmail` and `rejectsDuplicateNormalizedUsername`. Assert generated IDs, round-trip field values and database uniqueness exceptions.

- [ ] **Step 2: Run repository tests to verify they fail**

Run: `./mvnw -Dtest=UserRepositoryTests test`

Expected: FAIL because `User` and `UserRepository` do not exist.

- [ ] **Step 3: Implement the JPA entity**

Create `User` mapped to `users` with an `IDENTITY` ID, explicit `@Column` definitions matching V1, a protected no-argument constructor and a public constructor accepting `name`, `username`, `normalizedUsername`, `email`, `passwordHash`, and `createdAt`.

- [ ] **Step 4: Implement the repository**

Create `UserRepository extends JpaRepository<User, Long>` with:

```java
Optional<User> findByEmail(String email);
boolean existsByEmail(String email);
boolean existsByNormalizedUsername(String normalizedUsername);
```

- [ ] **Step 5: Run repository tests**

Run: `./mvnw -Dtest=UserRepositoryTests test`

Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git add src/main/java/br/com/cinecookie/user src/test/java/br/com/cinecookie/user/UserRepositoryTests.java
git commit -m "feat: persist CineCookie users with JPA"
```

### Task 3: Validated forms and user service

**Files:**
- Create: `src/main/java/br/com/cinecookie/user/RegistrationForm.java`
- Create: `src/main/java/br/com/cinecookie/user/LoginForm.java`
- Create: `src/main/java/br/com/cinecookie/user/RegistrationResult.java`
- Create: `src/main/java/br/com/cinecookie/user/UserService.java`
- Create: `src/test/java/br/com/cinecookie/user/UserServiceTests.java`

**Interfaces:**
- Consumes: `UserRepository` from Task 2.
- Produces: `RegistrationResult register(RegistrationForm form)`, `Optional<User> authenticate(LoginForm form)` and `long countUsers()`.

- [ ] **Step 1: Write failing service tests**

Cover successful registration, normalized email authentication, duplicate email, duplicate normalized username, wrong password and unknown email. Assert the stored password differs from the submitted password.

- [ ] **Step 2: Run service tests to verify they fail**

Run: `./mvnw -Dtest=UserServiceTests test`

Expected: FAIL because forms, result type and service do not exist.

- [ ] **Step 3: Implement Bean Validation form DTOs**

`RegistrationForm` exposes `name`, `username`, `email`, `password` and `terms`, using `@NotBlank`, `@Size`, `@Pattern`, `@Email` and `@AssertTrue` with Portuguese messages matching database limits. `LoginForm` exposes validated `email` and `password`.

- [ ] **Step 4: Implement registration result semantics**

Create `RegistrationResult` with `SUCCESS`, `EMAIL_ALREADY_EXISTS` and `USERNAME_ALREADY_EXISTS`.

- [ ] **Step 5: Implement `UserService`**

Normalize email and username with trim plus `Locale.ROOT` lowercase. Check duplicates before saving. Preserve the current SHA-256 hashing behavior as an explicitly academic implementation and use constant-value comparison semantics available in `MessageDigest.isEqual` during authentication.

- [ ] **Step 6: Run service tests**

Run: `./mvnw -Dtest=UserServiceTests test`

Expected: PASS.

- [ ] **Step 7: Commit**

```bash
git add src/main/java/br/com/cinecookie/user src/test/java/br/com/cinecookie/user/UserServiceTests.java
git commit -m "feat: add validated user business service"
```

### Task 4: Integrate persistent forms with MVC and Thymeleaf

**Files:**
- Modify: `src/main/java/br/com/cinecookie/web/PageController.java`
- Delete: `src/main/java/br/com/cinecookie/web/SimulatedUserStore.java`
- Modify: `src/main/resources/templates/cadastro.html`
- Modify: `src/main/resources/templates/login.html`
- Modify: `src/main/resources/static/css/styles.css`
- Modify: `src/test/java/br/com/cinecookie/CineCookieApplicationTests.java`

**Interfaces:**
- Consumes: `UserService`, `RegistrationForm`, `LoginForm` and `RegistrationResult` from Task 3.
- Produces: persistent `/cadastro` and `/login` GET/POST flows with field-level errors and a session `userId` after authentication.

- [ ] **Step 1: Replace simulated-flow tests with failing persistent MVC tests**

Test successful registration and login, invalid registration field errors, duplicate email form error, generic invalid login error, preserved non-password fields and authenticated `userId` session state. Clean `UserRepository` before each test.

- [ ] **Step 2: Run MVC tests to verify they fail**

Run: `./mvnw -Dtest=CineCookieApplicationTests test`

Expected: FAIL because the controller still accepts request parameters and uses `SimulatedUserStore`.

- [ ] **Step 3: Bind forms in `PageController`**

Use `@ModelAttribute`, `@Valid` and adjacent `BindingResult`. Delegate all user rules to `UserService`, map duplicate results to global form errors, redirect after successful registration and store `userId` in `HttpSession` after successful authentication.

- [ ] **Step 4: Render field errors in Thymeleaf**

Bind each form with `th:object` and each input with `th:field`. Render `th:errors` beside fields and render global errors in the existing status area. Change the simulated account copy to persisted-account copy.

- [ ] **Step 5: Add minimal error styling**

Extend existing form styles for field-level messages and invalid controls without changing the page layout.

- [ ] **Step 6: Delete the simulated store**

Remove `SimulatedUserStore` after all callers have moved to `UserService`.

- [ ] **Step 7: Run MVC and full tests**

Run: `./mvnw test`

Expected: all repository, service and MVC tests PASS.

- [ ] **Step 8: Commit**

```bash
git add src/main/java/br/com/cinecookie src/main/resources/templates src/main/resources/static/css/styles.css src/test/java/br/com/cinecookie
git commit -m "feat: connect authentication forms to persistence"
```

### Task 5: Documentation and PostgreSQL smoke verification

**Files:**
- Modify: `README.md`

**Interfaces:**
- Consumes: the completed persistent user flow.
- Produces: reproducible setup instructions and evidence that PostgreSQL, Flyway and Hibernate validation agree.

- [ ] **Step 1: Document local database startup**

Document `docker compose up -d database`, `./mvnw spring-boot:run`, database environment overrides, Flyway behavior and `docker compose down`.

- [ ] **Step 2: Run full automated verification**

Run: `./mvnw clean test`

Expected: BUILD SUCCESS.

- [ ] **Step 3: Run PostgreSQL smoke verification**

Start the Compose database, run the packaged application long enough to observe Flyway V1 success and Hibernate schema validation, then request `/cadastro` and expect HTTP 200.

- [ ] **Step 4: Inspect schema history**

Query `flyway_schema_history` and verify version `1` is successful; query `users` after one browser or HTTP registration and verify the password column is not plain text.

- [ ] **Step 5: Commit**

```bash
git add README.md
git commit -m "docs: explain PostgreSQL development workflow"
```

- [ ] **Step 6: Record the next phase boundary**

The next plan begins only after this phase passes: persist movies, add genre many-to-many, add review many-to-one relationships and move catalog reads into `MovieService`.
