package br.com.cinecookie.user;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserServiceConflictTests {

    @Test
    void returnsConflictWhenDatabaseRejectsRegistration() {
        UserRepository repository = mock(UserRepository.class);
        when(repository.saveAndFlush(any(User.class)))
                .thenThrow(new DataIntegrityViolationException("unique constraint"));
        UserService service = new UserService(repository);

        RegistrationResult result = service.register(validRegistration());

        assertThat(result.name()).isEqualTo("CONFLICT");
    }

    private RegistrationForm validRegistration() {
        RegistrationForm form = new RegistrationForm();
        form.setName("Luka Silva");
        form.setUsername("lukavns");
        form.setEmail("luka@example.com");
        form.setPassword("cinema123");
        form.setTerms(true);
        return form;
    }
}
