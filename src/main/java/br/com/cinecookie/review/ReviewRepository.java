package br.com.cinecookie.review;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    Optional<Review> findByUserIdAndMovieId(Long userId, Long movieId);
    @EntityGraph(attributePaths = "user")
    List<Review> findByMovieIdOrderByUpdatedAtDesc(Long movieId);

    @EntityGraph(attributePaths = {"user", "movie"})
    List<Review> findTop3ByOrderByUpdatedAtDesc();

    @Query("select r.movie.id as movieId, avg(r.rating) as score from Review r group by r.movie.id")
    List<MovieScore> averageScores();

    interface MovieScore {
        Long getMovieId();
        Double getScore();
    }
}
