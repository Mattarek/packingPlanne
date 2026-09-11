package org.example.packing.application.controller;

import org.example.packing.application.dto.VehicleRequest;
import org.example.packing.application.dto.VehicleResponse;
import org.example.packing.application.service.VehicleService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
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

	private static final String VEHICLES_URL = "/api/vehicles";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private VehicleService vehicleService;

	private UUID vehicleId1;
	private UUID vehicleId2;

	private VehicleRequest vehicleRequest1;
	private VehicleRequest vehicleRequest2;
	private VehicleRequest singleVehicleRequest;

	private VehicleResponse vehicleResponse1;
	private VehicleResponse vehicleResponse2;
	private VehicleResponse singleVehicleResponse;

	private List<VehicleRequest> vehicleRequests;
	private List<VehicleResponse> vehicleResponses;

	private Page<VehicleResponse> vehicleResponsePage;

	@BeforeEach
	void setUp() {
		vehicleId1 = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
		vehicleId2 = UUID.fromString("123e4567-e89b-12d3-a456-426614174001");

		vehicleRequest1 = new VehicleRequest(
				"Mercedes Sprinter",
				420.0,
				180.0,
				200.0,
				1000.0
		);

		vehicleRequest2 = new VehicleRequest(
				"Iveco Daily 7t",
				620.0,
				220.0,
				230.0,
				3500.0
		);

		singleVehicleRequest = vehicleRequest1;

		vehicleResponse1 = new VehicleResponse(
				vehicleId1,
				"Mercedes Sprinter",
				420.0,
				180.0,
				200.0,
				1000.0
		);

		vehicleResponse2 = new VehicleResponse(
				vehicleId2,
				"Iveco Daily 7t",
				620.0,
				220.0,
				230.0,
				3500.0
		);

		singleVehicleResponse = vehicleResponse1;

		vehicleRequests = List.of(
				vehicleRequest1,
				vehicleRequest2
		);

		vehicleResponses = List.of(
				vehicleResponse1,
				vehicleResponse2
		);

		vehicleResponsePage = new PageImpl<>(
				vehicleResponses,
				PageRequest.of(0, 20),
				vehicleResponses.size()
		);
	}

	@AfterEach
	void tearDown() {
		reset(vehicleService);
	}

	@Test
	void shouldCreateVehicles() throws Exception {
		when(vehicleService.createVehicles(vehicleRequests)).thenReturn(vehicleResponses);

		mockMvc.perform(post(VEHICLES_URL)
						.contentType(APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(vehicleRequests)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value("Vehicles created successfully."))
				.andExpect(jsonPath("$.data[0].id").value(vehicleId1.toString()))
				.andExpect(jsonPath("$.data[0].name").value("Mercedes Sprinter"))
				.andExpect(jsonPath("$.data[0].length").value(420.0))
				.andExpect(jsonPath("$.data[0].width").value(180.0))
				.andExpect(jsonPath("$.data[0].height").value(200.0))
				.andExpect(jsonPath("$.data[0].maxPayload").value(1000.0))
				.andExpect(jsonPath("$.data[1].id").value(vehicleId2.toString()))
				.andExpect(jsonPath("$.data[1].name").value("Iveco Daily 7t"))
				.andExpect(jsonPath("$.data[1].length").value(620.0))
				.andExpect(jsonPath("$.data[1].width").value(220.0))
				.andExpect(jsonPath("$.data[1].height").value(230.0))
				.andExpect(jsonPath("$.data[1].maxPayload").value(3500.0));

		verify(vehicleService).createVehicles(vehicleRequests);
	}

	@Test
	void shouldReturnVehicles() throws Exception {
		when(vehicleService.getVehicles(0, 20)).thenReturn(vehicleResponsePage);

		mockMvc.perform(get(VEHICLES_URL)
						.param("page", "0")
						.param("size", "20"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value("Vehicles fetched successfully."))
				.andExpect(jsonPath("$.data.content[0].id").value(vehicleId1.toString()))
				.andExpect(jsonPath("$.data.content[0].name").value("Mercedes Sprinter"))
				.andExpect(jsonPath("$.data.content[0].length").value(420.0))
				.andExpect(jsonPath("$.data.content[0].width").value(180.0))
				.andExpect(jsonPath("$.data.content[0].height").value(200.0))
				.andExpect(jsonPath("$.data.content[0].maxPayload").value(1000.0))
				.andExpect(jsonPath("$.data.content[1].id").value(vehicleId2.toString()))
				.andExpect(jsonPath("$.data.content[1].name").value("Iveco Daily 7t"))
				.andExpect(jsonPath("$.data.content[1].length").value(620.0))
				.andExpect(jsonPath("$.data.content[1].width").value(220.0))
				.andExpect(jsonPath("$.data.content[1].height").value(230.0))
				.andExpect(jsonPath("$.data.content[1].maxPayload").value(3500.0));

		verify(vehicleService).getVehicles(0, 20);
	}

	@Test
	void shouldReturnVehicleById() throws Exception {
		when(vehicleService.getVehicle(vehicleId1)).thenReturn(singleVehicleResponse);

		mockMvc.perform(get(VEHICLES_URL + "/{id}", vehicleId1))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value("Vehicle fetched successfully."))
				.andExpect(jsonPath("$.data.id").value(vehicleId1.toString()))
				.andExpect(jsonPath("$.data.name").value("Mercedes Sprinter"))
				.andExpect(jsonPath("$.data.length").value(420.0))
				.andExpect(jsonPath("$.data.width").value(180.0))
				.andExpect(jsonPath("$.data.height").value(200.0))
				.andExpect(jsonPath("$.data.maxPayload").value(1000.0));

		verify(vehicleService).getVehicle(vehicleId1);
	}

	@Test
	void shouldDeleteVehicle() throws Exception {
		mockMvc.perform(delete(VEHICLES_URL + "/{id}", vehicleId1))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value("Vehicle deleted successfully."))
				.andExpect(jsonPath("$.data.deletedId").value(vehicleId1.toString()));

		verify(vehicleService).deleteVehicle(vehicleId1);
	}

	@Test
	void shouldReturnBadRequestWhenObjectIsSentToVehiclesEndpoint() throws Exception {
		mockMvc.perform(post(VEHICLES_URL)
						.contentType(APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(singleVehicleRequest)))
				.andExpect(status().isBadRequest());

		verify(vehicleService, never()).createVehicles(any());
	}

	@Test
	void shouldReturnBadRequestWhenInvalidUuidIsSent() throws Exception {
		mockMvc.perform(get(VEHICLES_URL + "/invalid-id"))
				.andExpect(status().isBadRequest());

		verify(vehicleService, never()).getVehicle(any());
	}
}