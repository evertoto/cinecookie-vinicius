package br.com.cinecookie.review;

import java.math.BigDecimal;
import java.time.Instant;
import br.com.cinecookie.movie.Movie;
import br.com.cinecookie.user.User;
import jakarta.persistence.*;

@Entity
@Table(name = "reviews", uniqueConstraints = @UniqueConstraint(
        name = "uk_reviews_user_movie", columnNames = {"user_id", "movie_id"}))
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;
    @Column(nullable = false, precision = 2, scale = 1)
    private BigDecimal rating;
    @Column(nullable = false, length = 1000)
    private String comment;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Review() {}

    public Review(User user, Movie movie, BigDecimal rating, String comment) {
        this.user = user;
        this.movie = movie;
        this.createdAt = Instant.now();
        update(rating, comment);
    }

    public void update(BigDecimal rating, String comment) {
        this.rating = rating;
        this.comment = comment == null ? "" : comment.trim();
        this.updatedAt = Instant.now();
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public Movie getMovie() { return movie; }
    public BigDecimal getRating() { return rating; }
    public String getComment() { return comment; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
