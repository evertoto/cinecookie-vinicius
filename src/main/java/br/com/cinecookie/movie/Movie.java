package br.com.cinecookie.movie;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Set;
import jakarta.persistence.*;

@Entity
@Table(name = "movies")
public class Movie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 100)
    private String slug;
    @Column(nullable = false, length = 200)
    private String title;
    @Column(nullable = false, length = 10)
    private String duration;
    @Column(name = "age_rating", nullable = false, length = 3)
    private String ageRating;
    @Column(name = "initial_score", nullable = false, precision = 2, scale = 1)
    private BigDecimal initialScore;
    @Column(nullable = false, length = 200)
    private String poster;
    @Column(nullable = false, length = 30)
    private String category;

    @ManyToMany
    @JoinTable(name = "movie_genres",
            joinColumns = @JoinColumn(name = "movie_id"),
            inverseJoinColumns = @JoinColumn(name = "genre_id"))
    @OrderBy("id ASC")
    private Set<Genre> genres = new LinkedHashSet<>();

    protected Movie() {}

    public Long getId() { return id; }
    public String getSlug() { return slug; }
    public String getTitle() { return title; }
    public String getDuration() { return duration; }
    public String getAgeRating() { return ageRating; }
    public BigDecimal getInitialScore() { return initialScore; }
    public String getPoster() { return poster; }
    public String getCategory() { return category; }
    public Set<Genre> getGenres() { return java.util.Collections.unmodifiableSet(genres); }
}
