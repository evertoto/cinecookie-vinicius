package br.com.cinecookie.user;

import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class UserServiceTests {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private Validator validator;

    @BeforeEach
    void clearUsers() {
        userRepository.deleteAll();
    }

    @Test
    void registersNormalizedUserWithoutPlainTextPassword() {
        RegistrationResult result = userService.register(registration(
                "  Luka Silva  ", "  Luka.VNS  ", "  LUKA@EXAMPLE.COM  ", "cinema123"));

        User saved = userRepository.findByEmail("luka@example.com").orElseThrow();

        assertThat(result).isEqualTo(RegistrationResult.SUCCESS);
        assertThat(saved.getName()).isEqualTo("Luka Silva");
        assertThat(saved.getUsername()).isEqualTo("Luka.VNS");
        assertThat(saved.getNormalizedUsername()).isEqualTo("luka.vns");
        assertThat(saved.getPasswordHash()).hasSize(64).isNotEqualTo("cinema123");
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(userService.countUsers()).isEqualTo(1);
    }

    @Test
    void authenticatesWithNormalizedEmail() {
        userService.register(registration("Luka", "lukavns", "luka@example.com", "cinema123"));

        assertThat(userService.authenticate(login("  LUKA@EXAMPLE.COM  ", "cinema123")))
                .get()
                .extracting(User::getUsername)
                .isEqualTo("lukavns");
    }

    @Test
    void rejectsDuplicateEmailIgnoringCaseAndWhitespace() {
        userService.register(registration("Luka", "primeiro", "luka@example.com", "cinema123"));

        RegistrationResult result = userService.register(registration(
                "Outro", "segundo", "  LUKA@EXAMPLE.COM ", "cinema456"));

        assertThat(result).isEqualTo(RegistrationResult.EMAIL_ALREADY_EXISTS);
        assertThat(userService.countUsers()).isEqualTo(1);
    }

    @Test
    void rejectsDuplicateUsernameIgnoringCaseAndWhitespace() {
        userService.register(registration("Luka", "Luka.VNS", "um@example.com", "cinema123"));

        RegistrationResult result = userService.register(registration(
                "Outro", "  luka.vns  ", "dois@example.com", "cinema456"));

        assertThat(result).isEqualTo(RegistrationResult.USERNAME_ALREADY_EXISTS);
        assertThat(userService.countUsers()).isEqualTo(1);
    }

    @Test
    void doesNotAuthenticateWrongPasswordOrUnknownEmail() {
        userService.register(registration("Luka", "lukavns", "luka@example.com", "cinema123"));

        assertThat(userService.authenticate(login("luka@example.com", "senha-errada"))).isEmpty();
        assertThat(userService.authenticate(login("ninguem@example.com", "cinema123"))).isEmpty();
    }

    @Test
    void validatesRegistrationFields() {
        RegistrationForm form = registration("A", "x!", "email-invalido", "123", false);

        Set<ConstraintViolation<RegistrationForm>> violations = validator.validate(form);

        assertThat(violations)
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("name", "username", "email", "password", "terms");
    }

    private RegistrationForm registration(String name, String username, String email, String password) {
        return registration(name, username, email, password, true);
    }

    private RegistrationForm registration(
            String name,
            String username,
            String email,
            String password,
            boolean terms) {
        RegistrationForm form = new RegistrationForm();
        form.setName(name);
        form.setUsername(username);
        form.setEmail(email);
        form.setPassword(password);
        form.setTerms(terms);
        return form;
    }

    private LoginForm login(String email, String password) {
        LoginForm form = new LoginForm();
        form.setEmail(email);
        form.setPassword(password);
        return form;
    }
}
