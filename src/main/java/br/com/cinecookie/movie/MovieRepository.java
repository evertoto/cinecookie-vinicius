package br.com.cinecookie.movie;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovieRepository extends JpaRepository<Movie, Long> {
    @EntityGraph(attributePaths = "genres")
    List<Movie> findAllByOrderByIdAsc();

    @EntityGraph(attributePaths = "genres")
    Optional<Movie> findBySlugIgnoreCase(String slug);
}
