package br.com.cinecookie.web;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class PageController {

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
        model.addAttribute("pageTitle", "Entrar — CineCookie");
        model.addAttribute("activePage", "login");
        return "login";
    }

    @GetMapping("/cadastro")
    public String cadastro(Model model) {
        model.addAttribute("pageTitle", "Criar conta — CineCookie");
        model.addAttribute("activePage", "cadastro");
        return "cadastro";
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
}
