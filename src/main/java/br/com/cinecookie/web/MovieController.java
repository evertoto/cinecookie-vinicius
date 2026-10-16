package br.com.cinecookie.web;

import br.com.cinecookie.movie.MovieService;
import br.com.cinecookie.movie.MovieService.MovieView;
import br.com.cinecookie.review.ReviewForm;
import br.com.cinecookie.review.ReviewService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class MovieController {
    private final MovieService movies;
    private final ReviewService reviews;

    public MovieController(MovieService movies, ReviewService reviews) {
        this.movies = movies;
        this.reviews = reviews;
    }

    @GetMapping("/filmes/{slug}")
    public String details(@PathVariable String slug, Model model, HttpSession session) {
        MovieView movie = movies.find(slug).orElse(null);
        if (movie == null) {
            return "redirect:/filmes";
        }
        Long userId = (Long) session.getAttribute("userId");
        model.addAttribute("reviewForm", reviews.form(userId, slug));
        prepare(movie, model, userId);
        return "detalhes";
    }

    @PostMapping("/filmes/{slug}/avaliacoes")
    public String save(@PathVariable String slug,
            @Valid @ModelAttribute("reviewForm") ReviewForm form, BindingResult errors,
            Model model, HttpSession session, RedirectAttributes redirect) {
        Long userId = (Long) session.getAttribute("userId");
        if (!reviews.isRegistered(userId)) {
            session.removeAttribute("userId");
            redirect.addFlashAttribute("errorMessage", "Entre na sua conta para avaliar um filme.");
            return "redirect:/login";
        }
        MovieView movie = movies.find(slug).orElse(null);
        if (movie == null) {
            return "redirect:/filmes";
        }
        if (!errors.hasErrors()) {
            try {
                reviews.save(userId, slug, form);
                redirect.addFlashAttribute("reviewMessage", "Sua avaliação foi salva!");
                return "redirect:/filmes/" + movie.id() + "#avaliacoes";
            } catch (DataIntegrityViolationException exception) {
                errors.reject("review.conflict", "Sua avaliação foi alterada em outra aba. Recarregue e tente novamente.");
            }
        }
        prepare(movie, model, userId);
        return "detalhes";
    }

    private void prepare(MovieView movie, Model model, Long userId) {
        model.addAttribute("pageTitle", movie.title() + " — CineCookie");
        model.addAttribute("activePage", "filmes");
        model.addAttribute("movie", movie);
        model.addAttribute("reviews", reviews.list(movie.id()));
        model.addAttribute("canReview", reviews.isRegistered(userId));
    }
}
