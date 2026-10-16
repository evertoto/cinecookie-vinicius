package br.com.cinecookie.review;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;
import br.com.cinecookie.movie.Movie;
import br.com.cinecookie.movie.MovieRepository;
import br.com.cinecookie.user.User;
import br.com.cinecookie.user.UserRepository;

@Service
@Validated
@Transactional(readOnly = true)
public class ReviewService {
    private final ReviewRepository reviews;
    private final MovieRepository movies;
    private final UserRepository users;

    public ReviewService(ReviewRepository reviews, MovieRepository movies, UserRepository users) {
        this.reviews = reviews;
        this.movies = movies;
        this.users = users;
    }

    @Transactional
    public void save(Long userId, String slug, @Valid ReviewForm form) {
        User user = users.findById(userId).orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));
        Movie movie = movies.findBySlugIgnoreCase(slug)
                .orElseThrow(() -> new IllegalArgumentException("Filme não encontrado."));
        Review review = reviews.findByUserIdAndMovieId(userId, movie.getId())
                .orElseGet(() -> new Review(user, movie, form.getRating(), form.getComment()));
        review.update(form.getRating(), form.getComment());
        reviews.saveAndFlush(review);
    }

    public List<ReviewView> list(String slug) {
        return movies.findBySlugIgnoreCase(slug)
                .map(movie -> reviews.findByMovieIdOrderByUpdatedAtDesc(movie.getId()).stream()
                        .map(review -> new ReviewView(review.getUser().getName(),
                                review.getRating(), review.getComment())).toList())
                .orElseGet(List::of);
    }

    public List<ActivityView> recent() {
        return reviews.findTop3ByOrderByUpdatedAtDesc().stream().map(review -> new ActivityView(
                review.getUser().getName(), review.getRating(), review.getComment(),
                review.getMovie().getSlug(), review.getMovie().getTitle(), review.getMovie().getPoster())).toList();
    }

    public ReviewForm form(Long userId, String slug) {
        ReviewForm form = new ReviewForm();
        if (userId != null) {
            movies.findBySlugIgnoreCase(slug)
                    .flatMap(movie -> reviews.findByUserIdAndMovieId(userId, movie.getId()))
                    .ifPresent(review -> {
                        form.setRating(review.getRating());
                        form.setComment(review.getComment());
                    });
        }
        return form;
    }

    public boolean isRegistered(Long userId) {
        return userId != null && users.existsById(userId);
    }

    public record ReviewView(String author, BigDecimal rating, String comment) {}
    public record ActivityView(String author, BigDecimal rating, String comment,
            String slug, String title, String poster) {}
}
