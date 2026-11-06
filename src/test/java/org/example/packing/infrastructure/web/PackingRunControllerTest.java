package org.example.packing.infrastructure.web;

import org.example.packing.application.dto.PackingReport;
import org.example.packing.application.port.in.RunPackingUseCase;
import org.example.packing.domain.exception.NoVehiclesAvailableException;
import org.example.packing.domain.model.PackingStatistics;
import org.example.packing.infrastructure.web.mapper.RestResponseMapperImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PackingRunController.class)
@Import(RestResponseMapperImpl.class)
class PackingRunControllerTest {

	private static final String PACKING_RUNS_URL = "/api/packing-runs";

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private RunPackingUseCase runPackingUseCase;

	@Test
	void shouldReturnPackingRunReport() throws Exception {
		when(runPackingUseCase.runPacking()).thenReturn(new PackingReport(
				"TestStrategy",
				List.of(),
				List.of(),
				0,
				3L,
				new PackingStatistics(0, 0, true, 0.0, 0.0)
		));

		mockMvc.perform(post(PACKING_RUNS_URL))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.strategyName").value("TestStrategy"))
				.andExpect(jsonPath("$.data.fullyPacked").value(true))
				.andExpect(jsonPath("$.data.elapsedMillis").value(3));
	}

	@Test
	void shouldReturnUnprocessableEntityWhenThereAreNoVehicles() throws Exception {
		when(runPackingUseCase.runPacking()).thenThrow(new NoVehiclesAvailableException());

		mockMvc.perform(post(PACKING_RUNS_URL))
				.andExpect(status().isUnprocessableContent())
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.message").value("At least one vehicle must be provided"));
	}
}
