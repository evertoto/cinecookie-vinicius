package br.com.cinecookie.web;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.springframework.stereotype.Component;

@Component
public class SimulatedUserStore {

    private final List<SimulatedUser> users = new ArrayList<>();

    public synchronized RegistrationResult register(String name, String username, String email, String password) {
        String normalizedEmail = normalize(email);
        String normalizedUsername = normalize(username);

        if (users.stream().anyMatch(user -> user.email().equals(normalizedEmail))) {
            return RegistrationResult.EMAIL_ALREADY_EXISTS;
        }
        if (users.stream().anyMatch(user -> user.normalizedUsername().equals(normalizedUsername))) {
            return RegistrationResult.USERNAME_ALREADY_EXISTS;
        }

        users.add(new SimulatedUser(
                name.trim(),
                username.trim(),
                normalizedUsername,
                normalizedEmail,
                hash(password)));
        return RegistrationResult.SUCCESS;
    }

    public synchronized Optional<SimulatedUser> authenticate(String email, String password) {
        String normalizedEmail = normalize(email);
        String passwordHash = hash(password);
        return users.stream()
                .filter(user -> user.email().equals(normalizedEmail) && user.passwordHash().equals(passwordHash))
                .findFirst();
    }

    public synchronized int count() {
        return users.size();
    }

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

    public enum RegistrationResult {
        SUCCESS,
        EMAIL_ALREADY_EXISTS,
        USERNAME_ALREADY_EXISTS
    }

    public record SimulatedUser(
            String name,
            String username,
            String normalizedUsername,
            String email,
            String passwordHash) {
    }
}
