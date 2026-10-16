package br.com.cinecookie.user;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class UserRepositoryTests {

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void clearUsers() {
        userRepository.deleteAll();
    }

    @Test
    void savesAndFindsUserByEmail() {
        User saved = userRepository.saveAndFlush(user("luka", "luka", "luka@cinecookie.com"));

        User found = userRepository.findByEmail("luka@cinecookie.com").orElseThrow();

        assertThat(saved.getId()).isNotNull();
        assertThat(found.getName()).isEqualTo("Luka");
        assertThat(found.getUsername()).isEqualTo("luka");
        assertThat(found.getNormalizedUsername()).isEqualTo("luka");
        assertThat(found.getEmail()).isEqualTo("luka@cinecookie.com");
        assertThat(found.getPasswordHash()).isEqualTo("a".repeat(64));
    }

    @Test
    void rejectsDuplicateEmail() {
        userRepository.saveAndFlush(user("luka", "luka", "duplicado@cinecookie.com"));

        assertThatThrownBy(() -> userRepository.saveAndFlush(
                user("outro", "outro", "duplicado@cinecookie.com")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void rejectsDuplicateNormalizedUsername() {
        userRepository.saveAndFlush(user("luka", "mesmo.usuario", "um@cinecookie.com"));

        assertThatThrownBy(() -> userRepository.saveAndFlush(
                user("outro", "mesmo.usuario", "dois@cinecookie.com")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private User user(String username, String normalizedUsername, String email) {
        return new User(
                "Luka",
                username,
                normalizedUsername,
                email,
                "a".repeat(64),
                Instant.parse("2026-10-16T12:00:00Z"));
    }
}
