package br.com.cinecookie.web;

import java.util.List;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import br.com.cinecookie.user.LoginForm;
import br.com.cinecookie.user.RegistrationForm;
import br.com.cinecookie.user.RegistrationResult;
import br.com.cinecookie.user.User;
import br.com.cinecookie.user.UserService;
import br.com.cinecookie.movie.MovieService;
import br.com.cinecookie.movie.MovieService.MovieView;
import br.com.cinecookie.review.ReviewService;

@Controller
public class PageController {

    private final UserService userService;
    private final MovieService movieService;
    private final ReviewService reviewService;

    public PageController(UserService userService, MovieService movieService, ReviewService reviewService) {
        this.userService = userService;
        this.movieService = movieService;
        this.reviewService = reviewService;
    }

    private List<MovieView> getMovieList() {
        return movieService.list();
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("pageTitle", "CineCookie — Seu cinema, sua história");
        model.addAttribute("activePage", "home");
        model.addAttribute("movies", getMovieList());
        model.addAttribute("recentReviews", reviewService.recent());
        return "home";
    }

    @GetMapping("/login")
    public String login(Model model) {
        prepareLoginPage(model);
        if (!model.containsAttribute("loginForm")) {
            model.addAttribute("loginForm", new LoginForm());
        }
        return "login";
    }

    @PostMapping("/login")
    public String login(
            @Valid @ModelAttribute("loginForm") LoginForm form,
            BindingResult bindingResult,
            Model model,
            HttpSession session) {
        prepareLoginPage(model);

        if (bindingResult.hasErrors()) {
            return "login";
        }

        User user = userService.authenticate(form).orElse(null);
        if (user == null) {
            model.addAttribute("errorMessage", "E-mail ou senha incorretos.");
            return "login";
        }

        session.setAttribute("userId", user.getId());
        form.setPassword("");
        model.addAttribute("successMessage", "Login realizado! Bem-vindo, " + user.getName() + ".");
        return "login";
    }

    @GetMapping("/cadastro")
    public String cadastro(Model model) {
        prepareSignupPage(model);
        if (!model.containsAttribute("registrationForm")) {
            model.addAttribute("registrationForm", new RegistrationForm());
        }
        return "cadastro";
    }

    @PostMapping("/cadastro")
    public String cadastro(
            @Valid @ModelAttribute("registrationForm") RegistrationForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        prepareSignupPage(model);

        if (bindingResult.hasErrors()) {
            return "cadastro";
        }

        RegistrationResult result = userService.register(form);
        if (result == RegistrationResult.EMAIL_ALREADY_EXISTS) {
            bindingResult.reject("registration.emailAlreadyExists", "Já existe uma conta cadastrada com este e-mail.");
            return "cadastro";
        }
        if (result == RegistrationResult.USERNAME_ALREADY_EXISTS) {
            bindingResult.reject("registration.usernameAlreadyExists", "Este nome de usuário já está em uso.");
            return "cadastro";
        }
        if (result == RegistrationResult.CONFLICT) {
            bindingResult.reject(
                    "registration.conflict",
                    "Não foi possível criar a conta porque o e-mail ou usuário acabou de ser cadastrado.");
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

    private void prepareLoginPage(Model model) {
        model.addAttribute("pageTitle", "Entrar — CineCookie");
        model.addAttribute("activePage", "login");
    }

    private void prepareSignupPage(Model model) {
        model.addAttribute("pageTitle", "Criar conta — CineCookie");
        model.addAttribute("activePage", "cadastro");
        model.addAttribute("registeredUserCount", userService.countUsers());
    }
}
