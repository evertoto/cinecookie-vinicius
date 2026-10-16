INSERT INTO movies (slug, title, duration, age_rating, initial_score, poster, category) VALUES
('a-odisseia', 'A Odisseia', '2h52', '14', 4.7, '/images/posters/a-odisseia.webp', 'acao'),
('homem-aranha', 'Homem-Aranha: Um Novo Dia', '2h24', '12', 4.5, '/images/posters/homem-aranha.webp', 'acao'),
('shaolin', 'O Shaolin do Sertão 2', '1h46', '12', 4.3, '/images/posters/shaolin.webp', 'comedia'),
('patrulha-canina', 'Patrulha Canina: Uma Aventura Dino', '1h28', 'L', 4.1, '/images/posters/patrulha-canina.webp', 'animacao'),
('authentic-games', 'Authentic Games no Império Desconectado', '1h11', 'L', 3.9, '/images/posters/authentic-games.webp', 'animacao'),
('amigas-sem-filtro', 'Amigas sem Filtro', '1h37', '16', 3.8, '/images/posters/amigas-sem-filtro.webp', 'comedia');

INSERT INTO genres (name) VALUES ('Ação'), ('Aventura'), ('Fantasia'), ('Comédia'), ('Animação'), ('Infantil');

INSERT INTO movie_genres (movie_id, genre_id)
SELECT m.id, g.id FROM movies m CROSS JOIN genres g
WHERE (m.slug = 'a-odisseia' AND g.name IN ('Ação', 'Aventura'))
   OR (m.slug = 'homem-aranha' AND g.name IN ('Ação', 'Fantasia'))
   OR (m.slug IN ('shaolin', 'amigas-sem-filtro') AND g.name = 'Comédia')
   OR (m.slug = 'patrulha-canina' AND g.name IN ('Animação', 'Infantil'))
   OR (m.slug = 'authentic-games' AND g.name IN ('Ação', 'Animação'));
