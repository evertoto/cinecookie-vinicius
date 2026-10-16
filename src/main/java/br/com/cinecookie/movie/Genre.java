package br.com.cinecookie.movie;

import java.util.Set;
import java.util.LinkedHashSet;
import jakarta.persistence.*;

@Entity
@Table(name = "genres")
public class Genre {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 50)
    private String name;
    @ManyToMany(mappedBy = "genres")
    private Set<Movie> movies = new LinkedHashSet<>();

    protected Genre() {}
    public Long getId() { return id; }
    public String getName() { return name; }
    public Set<Movie> getMovies() { return java.util.Collections.unmodifiableSet(movies); }
}
