package br.com.cinecookie.user;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public RegistrationResult register(RegistrationForm form) {
        String normalizedEmail = normalize(form.getEmail());
        String normalizedUsername = normalize(form.getUsername());

        if (userRepository.existsByEmail(normalizedEmail)) {
            return RegistrationResult.EMAIL_ALREADY_EXISTS;
        }
        if (userRepository.existsByNormalizedUsername(normalizedUsername)) {
            return RegistrationResult.USERNAME_ALREADY_EXISTS;
        }

        User user = new User(
                form.getName().trim(),
                form.getUsername().trim(),
                normalizedUsername,
                normalizedEmail,
                hash(form.getPassword()),
                Instant.now());
        try {
            userRepository.saveAndFlush(user);
            return RegistrationResult.SUCCESS;
        } catch (DataIntegrityViolationException exception) {
            return RegistrationResult.CONFLICT;
        }
    }

    @Transactional(readOnly = true)
    public Optional<User> authenticate(LoginForm form) {
        String normalizedEmail = normalize(form.getEmail());
        byte[] submittedHash = hash(form.getPassword()).getBytes(StandardCharsets.UTF_8);

        return userRepository.findByEmail(normalizedEmail)
                .filter(user -> MessageDigest.isEqual(
                        user.getPasswordHash().getBytes(StandardCharsets.UTF_8),
                        submittedHash));
    }

    @Transactional(readOnly = true)
    public long countUsers() {
        return userRepository.count();
    }

    @Transactional(readOnly = true)
    public Optional<CurrentUser> findCurrentUser(Long id) {
        return userRepository.findById(id)
                .map(user -> new CurrentUser(user.getName(), user.getUsername()));
    }

    public record CurrentUser(String name, String username) {}

    private String normalize(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private String hash(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 não está disponível", exception);
        }
    }
}
