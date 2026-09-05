package br.com.cinecookie;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
class CineCookieApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void contextLoads() {
	}

	@Test
	void rendersMovieListFromController() throws Exception {
		mockMvc.perform(get("/filmes"))
				.andExpect(status().isOk())
				.andExpect(view().name("filmes"))
				.andExpect(model().attributeExists("movies"));
	}

	@Test
	void registersAndAuthenticatesSimulatedUser() throws Exception {
		String email = "teste-integracao@cinecookie.com";

		mockMvc.perform(post("/cadastro")
					.param("name", "Usuário Teste")
					.param("username", "usuario.teste")
					.param("email", email)
					.param("password", "cinema123")
					.param("terms", "true"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/login"))
				.andExpect(flash().attribute("successMessage", containsString("Conta criada")));

		mockMvc.perform(post("/login")
					.param("email", email)
					.param("password", "cinema123"))
				.andExpect(status().isOk())
				.andExpect(view().name("login"))
				.andExpect(model().attribute("successMessage", containsString("Login realizado")));
	}

	@Test
	void rejectsInvalidSimulatedSignup() throws Exception {
		mockMvc.perform(post("/cadastro")
					.param("name", "A")
					.param("username", "x")
					.param("email", "email-invalido")
					.param("password", "123")
					.param("terms", "false"))
				.andExpect(status().isOk())
				.andExpect(view().name("cadastro"))
				.andExpect(model().attributeExists("errorMessage"));
	}

}
