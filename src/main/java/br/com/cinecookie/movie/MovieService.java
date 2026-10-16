package br.com.cinecookie.movie;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import br.com.cinecookie.review.ReviewRepository;

@Service
@Transactional(readOnly = true)
public class MovieService {
    private final MovieRepository movies;
    private final ReviewRepository reviews;

    public MovieService(MovieRepository movies, ReviewRepository reviews) {
        this.movies = movies;
        this.reviews = reviews;
    }

    public List<MovieView> list() {
        Map<Long, Double> scores = scores();
        return movies.findAllByOrderByIdAsc().stream().map(movie -> view(movie, scores)).toList();
    }

    public Optional<MovieView> find(String slug) {
        return movies.findBySlugIgnoreCase(slug).map(movie -> view(movie, scores()));
    }

    private Map<Long, Double> scores() {
        return reviews.averageScores().stream().collect(Collectors.toMap(
                ReviewRepository.MovieScore::getMovieId, ReviewRepository.MovieScore::getScore));
    }

    private MovieView view(Movie movie, Map<Long, Double> scores) {
        BigDecimal score = scores.containsKey(movie.getId())
                ? BigDecimal.valueOf(scores.get(movie.getId())) : movie.getInitialScore();
        return new MovieView(movie.getSlug(), movie.getTitle(),
                movie.getGenres().stream().map(Genre::getName).collect(Collectors.joining(" • ")),
                movie.getDuration(), movie.getAgeRating(), score.setScale(1, RoundingMode.HALF_UP).toPlainString(),
                movie.getPoster(), movie.getCategory());
    }

    public record MovieView(String id, String title, String genre, String duration,
            String ageRating, String score, String poster, String category) {}
}
