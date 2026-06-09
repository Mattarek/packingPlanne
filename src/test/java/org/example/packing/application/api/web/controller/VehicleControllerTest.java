package org.example.packing.application.api.web.controller;

import org.example.packing.application.controller.VehicleController;
import org.example.packing.application.dto.VehicleRequest;
import org.example.packing.application.dto.VehicleResponse;
import org.example.packing.application.service.VehicleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VehicleController.class)
class VehicleControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private VehicleService vehicleService;

	@Test
	void shouldCreateSingleVehicle() throws Exception {
		final VehicleRequest request = new VehicleRequest(
				"VAN-001",
				"Mercedes Sprinter",
				420.0,
				180.0,
				200.0,
				1000.0
		);

		mockMvc.perform(post("/api/vehicles")
						.contentType(APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk());

		verify(vehicleService).createVehicle(request);
	}

	@Test
	void shouldCreateManyVehicles() throws Exception {
		final List<VehicleRequest> requests = List.of(
				new VehicleRequest("VAN-001", "Mercedes Sprinter", 420.0, 180.0, 200.0, 1000.0),
				new VehicleRequest("TRUCK-001", "Iveco Daily 7t", 620.0, 220.0, 230.0, 3500.0)
		);

		mockMvc.perform(post("/api/vehicles/create")
						.contentType(APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(requests)))
				.andExpect(status().isOk());

		verify(vehicleService).createVehicles(requests);
	}

	@Test
	void shouldReturnVehicles() throws Exception {
		final List<VehicleResponse> responses = List.of(
				new VehicleResponse("VAN-001", "Mercedes Sprinter", 420.0, 180.0, 200.0, 1000.0),
				new VehicleResponse("TRUCK-001", "Iveco Daily 7t", 620.0, 220.0, 230.0, 3500.0)
		);

		when(vehicleService.getVehicles()).thenReturn(responses);

		mockMvc.perform(get("/api/vehicles"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id").value("VAN-001"))
				.andExpect(jsonPath("$[0].name").value("Mercedes Sprinter"))
				.andExpect(jsonPath("$[0].length").value(420.0))
				.andExpect(jsonPath("$[0].width").value(180.0))
				.andExpect(jsonPath("$[0].height").value(200.0))
				.andExpect(jsonPath("$[0].maxPayload").value(1000.0))
				.andExpect(jsonPath("$[1].id").value("TRUCK-001"))
				.andExpect(jsonPath("$[1].maxPayload").value(3500.0));
	}

	@Test
	void shouldReturnVehicleById() throws Exception {
		final VehicleResponse response = new VehicleResponse(
				"VAN-001",
				"Mercedes Sprinter",
				420.0,
				180.0,
				200.0,
				1000.0
		);

		when(vehicleService.getVehicle("VAN-001")).thenReturn(response);

		mockMvc.perform(get("/api/vehicles/VAN-001"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value("VAN-001"))
				.andExpect(jsonPath("$.name").value("Mercedes Sprinter"))
				.andExpect(jsonPath("$.length").value(420.0))
				.andExpect(jsonPath("$.width").value(180.0))
				.andExpect(jsonPath("$.height").value(200.0))
				.andExpect(jsonPath("$.maxPayload").value(1000.0));
	}

	@Test
	void shouldDeleteVehicle() throws Exception {
		mockMvc.perform(delete("/api/vehicles/VAN-001"))
				.andExpect(status().isOk());

		verify(vehicleService).deleteVehicle("VAN-001");
	}

	@Test
	void shouldDeleteAllVehicles() throws Exception {
		mockMvc.perform(delete("/api/vehicles"))
				.andExpect(status().isOk());

		verify(vehicleService).deleteAllVehicles();
	}

	@Test
	void shouldReturnBadRequestWhenArrayIsSentToSingleVehicleEndpoint() throws Exception {
		final List<VehicleRequest> requests = List.of(
				new VehicleRequest("VAN-001", "Mercedes Sprinter", 420.0, 180.0, 200.0, 1000.0)
		);

		mockMvc.perform(post("/api/vehicles")
						.contentType(APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(requests)))
				.andExpect(status().isBadRequest());

		verify(vehicleService, never()).createVehicle(any());
	}
}