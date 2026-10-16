package br.com.cinecookie.user;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class RegistrationForm {

    @NotBlank(message = "Informe seu nome.")
    @Size(min = 2, max = 100, message = "O nome deve ter entre 2 e 100 caracteres.")
    private String name;

    @NotBlank(message = "Informe um nome de usuário.")
    @Size(min = 3, max = 20, message = "O usuário deve ter entre 3 e 20 caracteres.")
    @Pattern(
            regexp = "[A-Za-z0-9._-]+",
            message = "Use apenas letras, números, ponto, hífen ou underline.")
    private String username;

    @NotBlank(message = "Informe seu e-mail.")
    @Email(message = "Informe um endereço de e-mail válido.")
    @Size(max = 150, message = "O e-mail deve ter no máximo 150 caracteres.")
    private String email;

    @NotBlank(message = "Informe uma senha.")
    @Size(min = 8, max = 100, message = "A senha deve ter entre 8 e 100 caracteres.")
    private String password;

    @AssertTrue(message = "Você precisa aceitar os Termos de Uso e a Política de Privacidade.")
    private boolean terms;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isTerms() {
        return terms;
    }

    public void setTerms(boolean terms) {
        this.terms = terms;
    }
}
