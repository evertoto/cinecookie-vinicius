package br.com.cinecookie;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.ActiveProfiles;

import br.com.cinecookie.user.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CineCookieApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ApplicationContext applicationContext;

	@Autowired
	private UserRepository userRepository;

	@BeforeEach
	void clearUsers() {
		userRepository.deleteAll();
	}

	@Test
	void contextLoads() {
		assertThat(applicationContext.containsBean("flyway")).isTrue();
	}

	@Test
	void rendersMovieListFromController() throws Exception {
		mockMvc.perform(get("/filmes"))
				.andExpect(status().isOk())
				.andExpect(view().name("filmes"))
				.andExpect(model().attributeExists("movies"));
	}

	@Test
	void registersAndAuthenticatesPersistedUser() throws Exception {
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

		assertThat(userRepository.findByEmail(email)).isPresent();

		mockMvc.perform(post("/login")
					.param("email", email)
					.param("password", "cinema123"))
				.andExpect(status().isOk())
				.andExpect(view().name("login"))
				.andExpect(model().attribute("successMessage", containsString("Login realizado")))
				.andExpect(request().sessionAttribute("userId", notNullValue()));
	}

	@Test
	void rejectsInvalidSignupWithFieldErrorsAndPreservedValues() throws Exception {
		mockMvc.perform(post("/cadastro")
					.param("name", "A")
					.param("username", "x")
					.param("email", "email-invalido")
					.param("password", "123")
					.param("terms", "false"))
				.andExpect(status().isOk())
				.andExpect(view().name("cadastro"))
				.andExpect(model().attributeHasFieldErrors(
						"registrationForm", "name", "username", "email", "password", "terms"))
				.andExpect(model().attribute(
						"registrationForm",
						hasProperty("email", is("email-invalido"))));

		assertThat(userRepository.count()).isZero();
	}

	@Test
	void rejectsDuplicateEmailAsGlobalFormError() throws Exception {
		postRegistration("Primeiro", "primeiro", "duplicado@cinecookie.com", "cinema123");

		mockMvc.perform(post("/cadastro")
					.param("name", "Segundo")
					.param("username", "segundo")
					.param("email", "DUPLICADO@CINECOOKIE.COM")
					.param("password", "cinema456")
					.param("terms", "true"))
				.andExpect(status().isOk())
				.andExpect(view().name("cadastro"))
				.andExpect(model().attribute(
						"org.springframework.validation.BindingResult.registrationForm",
						hasProperty("globalErrorCount", is(1))));

		assertThat(userRepository.count()).isEqualTo(1);
	}

	@Test
	void rejectsInvalidLoginWithGenericMessage() throws Exception {
		mockMvc.perform(post("/login")
					.param("email", "desconhecido@cinecookie.com")
					.param("password", "cinema123"))
				.andExpect(status().isOk())
				.andExpect(view().name("login"))
				.andExpect(model().attribute("errorMessage", "E-mail ou senha incorretos."));
	}

	private void postRegistration(String name, String username, String email, String password) throws Exception {
		mockMvc.perform(post("/cadastro")
				.param("name", name)
				.param("username", username)
				.param("email", email)
				.param("password", password)
				.param("terms", "true"))
				.andExpect(status().is3xxRedirection());
	}

}
