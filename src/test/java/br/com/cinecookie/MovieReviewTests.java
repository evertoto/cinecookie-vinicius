package br.com.cinecookie;

import java.math.BigDecimal;
import java.time.Instant;
import br.com.cinecookie.movie.*;
import br.com.cinecookie.review.*;
import br.com.cinecookie.user.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import static org.assertj.core.api.Assertions.*;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class MovieReviewTests {
    @Autowired MockMvc mvc;
    @Autowired MovieRepository movies;
    @Autowired MovieService catalog;
    @Autowired GenreRepository genres;
    @Autowired ReviewRepository reviews;
    @Autowired ReviewService reviewService;
    @Autowired UserRepository users;
    @Autowired EntityManager entityManager;
    @Autowired JdbcTemplate jdbc;

    @Test
    void loadsSeededCatalogAndManyToManyGenres() {
        assertThat(movies.count()).isEqualTo(6);
        assertThat(genres.count()).isEqualTo(6);
        Movie movie = movies.findBySlugIgnoreCase("A-ODISSEIA").orElseThrow();
        assertThat(movie.getGenres()).extracting(Genre::getName).containsExactly("Ação", "Aventura");
        assertThat(movie.getGenres().iterator().next().getMovies())
                .extracting(Movie::getSlug).contains("a-odisseia", "homem-aranha");
    }

    @Test
    void rendersCatalogFromDatabase() throws Exception {
        jdbc.update("UPDATE movies SET title = ? WHERE slug = ?", "Título persistido", "a-odisseia");
        entityManager.clear();
        mvc.perform(get("/filmes")).andExpect(status().isOk())
                .andExpect(content().string(containsString("Título persistido")));
    }

    @Test
    void rendersDetailsAndHandlesUnknownMovie() throws Exception {
        mvc.perform(get("/filmes/a-odisseia")).andExpect(status().isOk())
                .andExpect(model().attribute("canReview", false));
        mvc.perform(get("/filmes/inexistente")).andExpect(redirectedUrl("/filmes"));
    }

    @Test
    void persistsReviewRelationshipsAndUpdatesOwnReview() {
        User user = user("reviewer");
        reviewService.save(user.getId(), "a-odisseia", form("4.5", "Bom filme"));
        entityManager.flush();
        entityManager.clear();
        Review saved = reviews.findAll().get(0);
        Long reviewId = saved.getId();
        assertThat(saved.getUser().getId()).isEqualTo(user.getId());
        assertThat(saved.getMovie().getSlug()).isEqualTo("a-odisseia");
        reviewService.save(user.getId(), "a-odisseia", form("3.5", "Nova opinião"));
        entityManager.flush();
        entityManager.clear();
        assertThat(reviews.count()).isEqualTo(1);
        Review updated = reviews.findById(reviewId).orElseThrow();
        assertThat(updated.getRating()).isEqualByComparingTo("3.5");
        assertThat(updated.getComment()).isEqualTo("Nova opinião");
    }

    @Test
    void computesCommunityAverageFromPersistedReviews() {
        reviewService.save(user("first").getId(), "a-odisseia", form("4.0", ""));
        reviewService.save(user("second").getId(), "a-odisseia", form("5.0", ""));
        assertThat(catalog.find("a-odisseia").orElseThrow().score()).isEqualTo("4.5");
    }

    @Test
    void rejectsAnonymousReviews() throws Exception {
        mvc.perform(post("/filmes/a-odisseia/avaliacoes").param("rating", "4.5"))
                .andExpect(redirectedUrl("/login"));
        assertThat(reviews.count()).isZero();
    }

    @Test
    void preventsDuplicateUserMovieAtDatabaseLevel() {
        User user = user("unique");
        Movie movie = movies.findBySlugIgnoreCase("a-odisseia").orElseThrow();
        reviews.saveAndFlush(new Review(user, movie, new BigDecimal("4.0"), "Primeira"));
        assertThatThrownBy(() -> reviews.saveAndFlush(
                new Review(user, movie, new BigDecimal("5.0"), "Duplicada")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void databaseRejectsReviewWithoutExistingUser() {
        Long movieId = movies.findBySlugIgnoreCase("a-odisseia").orElseThrow().getId();
        assertThatThrownBy(() -> jdbc.update("""
                INSERT INTO reviews(user_id, movie_id, rating, comment, created_at, updated_at)
                VALUES (?, ?, 4.5, '', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """, -1L, movieId)).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void usesSessionUserInsteadOfSubmittedUserId() throws Exception {
        User owner = user("owner");
        User other = user("other");
        mvc.perform(post("/filmes/a-odisseia/avaliacoes").session(session(owner))
                .param("userId", other.getId().toString()).param("rating", "5.0"))
                .andExpect(status().is3xxRedirection());
        assertThat(reviews.findAll()).singleElement()
                .extracting(review -> review.getUser().getId()).isEqualTo(owner.getId());
    }

    @Test
    void validatesRatingAndCommentOnBackend() throws Exception {
        MockHttpSession session = session(user("invalid"));
        for (String rating : new String[]{"0", "5.5", "4.2", "abc", ""}) {
            mvc.perform(post("/filmes/a-odisseia/avaliacoes").session(session)
                    .param("rating", rating).param("comment", "x".repeat(1001)))
                    .andExpect(status().isOk()).andExpect(view().name("detalhes"))
                    .andExpect(model().attributeHasErrors("reviewForm"));
        }
        assertThat(reviews.count()).isZero();
    }

    @Test
    void savesAndRendersAuthenticatedReviewWithEscapedComment() throws Exception {
        MockHttpSession session = session(user("author"));
        mvc.perform(post("/filmes/a-odisseia/avaliacoes").session(session)
                .param("rating", "4.5").param("comment", "<script>alert(1)</script>"))
                .andExpect(redirectedUrl("/filmes/a-odisseia#avaliacoes"));
        mvc.perform(get("/filmes/a-odisseia").session(session))
                .andExpect(status().isOk()).andExpect(model().attribute("canReview", true))
                .andExpect(content().string(containsString("&lt;script&gt;")));
        mvc.perform(get("/")).andExpect(status().isOk())
                .andExpect(content().string(containsString("Usuário author")));
        assertThat(reviewService.form((Long) session.getAttribute("userId"), "a-odisseia").getRating())
                .isEqualByComparingTo("4.5");
    }

    private User user(String username) {
        return users.saveAndFlush(new User("Usuário " + username, username, username,
                username + "@example.com", "a".repeat(64), Instant.now()));
    }
    private MockHttpSession session(User user) {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", user.getId());
        return session;
    }
    private ReviewForm form(String rating, String comment) {
        ReviewForm form = new ReviewForm();
        form.setRating(new BigDecimal(rating));
        form.setComment(comment);
        return form;
    }
}
