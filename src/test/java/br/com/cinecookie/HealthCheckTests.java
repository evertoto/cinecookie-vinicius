package br.com.cinecookie;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class HealthCheckTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void healthEndpointReportsUpWithoutDetails() throws Exception {
		mockMvc.perform(get("/actuator/health"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("\"status\":\"UP\"")))
				.andExpect(content().string(not(containsString("jdbc"))));
	}

	@Test
	void otherActuatorEndpointsAreNotExposed() throws Exception {
		mockMvc.perform(get("/actuator/env"))
				.andExpect(status().isNotFound());
	}
}
