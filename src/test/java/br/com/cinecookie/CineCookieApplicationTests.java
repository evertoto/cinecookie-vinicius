package br.com.cinecookie;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.mock.web.MockHttpSession;

import br.com.cinecookie.user.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.hamcrest.Matchers.not;
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
	void servesVersionedStylesheetToAvoidStaleBrowserCache() throws Exception {
		String html = mockMvc.perform(get("/")).andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();
		java.util.regex.Matcher stylesheet = java.util.regex.Pattern
				.compile("/css/styles-[a-f0-9]+\\.css").matcher(html);
		assertThat(stylesheet.find()).isTrue();
		mockMvc.perform(get(stylesheet.group())).andExpect(status().isOk())
				.andExpect(content().string(containsString(".account-identity")));
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
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/"))
				.andExpect(request().sessionAttribute("userId", notNullValue()));
	}

	@Test
	void showsLoggedInUserAcrossPublicPagesAndRedirectsLogin() throws Exception {
		MockHttpSession session = authenticatedSession();
		for (String route : new String[]{"/", "/filmes", "/filmes/a-odisseia"}) {
			mockMvc.perform(get(route).session(session))
					.andExpect(status().isOk())
					.andExpect(model().attribute("currentUser",
							new br.com.cinecookie.user.UserService.CurrentUser("Usuário Teste", "usuario.teste")))
					.andExpect(content().string(containsString("avatar-default.svg")))
					.andExpect(content().string(containsString("Sair da sua conta")))
					.andExpect(content().string(not(containsString(">Entrar</a>"))))
					.andExpect(content().string(not(containsString("passwordHash"))));
		}
		mockMvc.perform(get("/login").session(session)).andExpect(redirectedUrl("/"));
	}

	@Test
	void logoutInvalidatesSessionAndRestoresGuestNavigation() throws Exception {
		MockHttpSession session = authenticatedSession();
		mockMvc.perform(post("/logout").session(session))
				.andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/"));
		assertThat(session.isInvalid()).isTrue();
		mockMvc.perform(get("/"))
				.andExpect(content().string(containsString(">Entrar</a>")))
				.andExpect(content().string(not(containsString("Sair da sua conta"))));
		mockMvc.perform(post("/filmes/a-odisseia/avaliacoes").param("rating", "5.0"))
				.andExpect(redirectedUrl("/login"));
	}

	@Test
	void clearsSessionForDeletedAccount() throws Exception {
		MockHttpSession session = authenticatedSession();
		userRepository.deleteAll();
		mockMvc.perform(get("/").session(session)).andExpect(status().isOk())
				.andExpect(content().string(containsString(">Entrar</a>")));
		assertThat(session.isInvalid()).isTrue();
	}

	@Test
	void rotatesSessionIdentifierWhenLoggingIn() throws Exception {
		postRegistration("Usuário Teste", "usuario.teste", "session@cinecookie.com", "cinema123");
		MockHttpSession session = new MockHttpSession();
		String previousId = session.getId();
		mockMvc.perform(post("/login").session(session)
				.param("email", "session@cinecookie.com").param("password", "cinema123"))
				.andExpect(redirectedUrl("/"));
		assertThat(session.getId()).isNotEqualTo(previousId);
	}

	private MockHttpSession authenticatedSession() throws Exception {
		postRegistration("Usuário Teste", "usuario.teste", "header@cinecookie.com", "cinema123");
		return (MockHttpSession) mockMvc.perform(post("/login")
				.param("email", "header@cinecookie.com").param("password", "cinema123"))
				.andExpect(redirectedUrl("/"))
				.andReturn().getRequest().getSession(false);
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
