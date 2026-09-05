package br.com.cinecookie.web;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import br.com.cinecookie.web.SimulatedUserStore.RegistrationResult;
import br.com.cinecookie.web.SimulatedUserStore.SimulatedUser;

@Controller
public class PageController {

    private final SimulatedUserStore userStore;

    public PageController(SimulatedUserStore userStore) {
        this.userStore = userStore;
    }

    private List<MovieView> getMovieList() {
        return List.of(
                new MovieView("a-odisseia", "A Odisseia", "Ação • Aventura", "2h52", "14", "4.7",
                       "/images/posters/a-odisseia.webp", "acao"),
                new MovieView("homem-aranha", "Homem-Aranha: Um Novo Dia", "Ação • Fantasia", "2h24", "12", "4.5",
                       "/images/posters/homem-aranha.webp", "acao"),
                new MovieView("shaolin", "O Shaolin do Sertão 2", "Comédia", "1h46", "12", "4.3",
                       "/images/posters/shaolin.webp", "comedia"),
                new MovieView("patrulha-canina", "Patrulha Canina: Uma Aventura Dino", "Animação • Infantil", "1h28", "L", "4.1",
                       "/images/posters/patrulha-canina.webp", "animacao"),
                new MovieView("authentic-games", "Authentic Games no Império Desconectado", "Ação • Animação", "1h11", "L", "3.9",
                       "/images/posters/authentic-games.webp", "animacao"),
                new MovieView("amigas-sem-filtro", "Amigas sem Filtro", "Comédia", "1h37", "16", "3.8",
                       "/images/posters/amigas-sem-filtro.webp", "comedia"));
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("pageTitle", "CineCookie — Seu cinema, sua história");
        model.addAttribute("activePage", "home");
        model.addAttribute("movies", getMovieList());
        return "home";
    }

    @GetMapping("/login")
    public String login(Model model) {
        prepareLoginPage(model);
        return "login";
    }

    @PostMapping("/login")
    public String login(
            @RequestParam String email,
            @RequestParam String password,
            Model model) {
        prepareLoginPage(model);
        model.addAttribute("email", email);

        if (email.isBlank() || password.length() < 8) {
            model.addAttribute("errorMessage", "Informe um e-mail válido e uma senha com pelo menos 8 caracteres.");
            return "login";
        }

        SimulatedUser user = userStore.authenticate(email, password).orElse(null);
        if (user == null) {
            model.addAttribute("errorMessage", "E-mail ou senha incorretos. Cadastre uma conta antes de entrar.");
            return "login";
        }

        model.addAttribute("email", "");
        model.addAttribute("successMessage", "Login realizado! Bem-vindo, " + user.name() + ".");
        return "login";
    }

    @GetMapping("/cadastro")
    public String cadastro(Model model) {
        prepareSignupPage(model);
        return "cadastro";
    }

    @PostMapping("/cadastro")
    public String cadastro(
            @RequestParam String name,
            @RequestParam String username,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam(defaultValue = "false") boolean terms,
            Model model,
            RedirectAttributes redirectAttributes) {
        prepareSignupPage(model);
        model.addAttribute("name", name);
        model.addAttribute("username", username);
        model.addAttribute("email", email);

        String validationError = validateSignup(name, username, email, password, terms);
        if (validationError != null) {
            model.addAttribute("errorMessage", validationError);
            return "cadastro";
        }

        RegistrationResult result = userStore.register(name, username, email, password);
        if (result == RegistrationResult.EMAIL_ALREADY_EXISTS) {
            model.addAttribute("errorMessage", "Já existe uma conta cadastrada com este e-mail.");
            return "cadastro";
        }
        if (result == RegistrationResult.USERNAME_ALREADY_EXISTS) {
            model.addAttribute("errorMessage", "Este nome de usuário já está em uso.");
            return "cadastro";
        }

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Conta criada com sucesso! Agora entre com seu e-mail e senha.");
        return "redirect:/login";
    }

    @GetMapping("/filmes")
    public String filmes(Model model) {
        model.addAttribute("pageTitle", "Catálogo de filmes — CineCookie");
        model.addAttribute("activePage", "filmes");
        model.addAttribute("movies", getMovieList());
        return "filmes";
    }

    @GetMapping("/filmes/{id}")
    public String detalhes(Model model, @PathVariable String id) {
        List<MovieView> movies = getMovieList();
        MovieView movie = movies.stream()
                .filter(m -> m.id().equalsIgnoreCase(id))
                .findFirst()
                .orElse(null);
        if (movie == null) {
            return "redirect:/filmes";
        }
        model.addAttribute("pageTitle", movie.title() + " — CineCookie");
        model.addAttribute("activePage", "filmes");
        model.addAttribute("movie", movie);
        return "detalhes";
    }

    public record MovieView(
            String id,
            String title,
            String genre,
            String duration,
            String ageRating,
            String score,
            String poster,
            String category) {
    }

    private void prepareLoginPage(Model model) {
        model.addAttribute("pageTitle", "Entrar — CineCookie");
        model.addAttribute("activePage", "login");
    }

    private void prepareSignupPage(Model model) {
        model.addAttribute("pageTitle", "Criar conta — CineCookie");
        model.addAttribute("activePage", "cadastro");
        model.addAttribute("registeredUserCount", userStore.count());
    }

    private String validateSignup(String name, String username, String email, String password, boolean terms) {
        if (name == null || name.trim().length() < 2) {
            return "Informe um nome com pelo menos 2 caracteres.";
        }
        if (username == null || !username.matches("[A-Za-z0-9._-]{3,20}")) {
            return "O usuário deve ter de 3 a 20 caracteres e usar apenas letras, números, ponto, hífen ou underline.";
        }
        if (email == null || !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            return "Informe um endereço de e-mail válido.";
        }
        if (password == null || password.length() < 8) {
            return "A senha deve ter pelo menos 8 caracteres.";
        }
        if (!terms) {
            return "Você precisa aceitar os Termos de Uso e a Política de Privacidade.";
        }
        return null;
    }
}
