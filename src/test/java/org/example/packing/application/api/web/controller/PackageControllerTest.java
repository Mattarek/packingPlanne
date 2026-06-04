package org.example.packing.application.api.web.controller;

import org.example.packing.api.web.controller.PackageController;
import org.example.packing.application.dto.PackageRequest;
import org.example.packing.application.dto.PackageResponse;
import org.example.packing.application.service.PackageService;
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

@WebMvcTest(PackageController.class)
class PackageControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private PackageService packageService;

	@Test
	void shouldCreateSinglePackage() throws Exception {
		final PackageRequest request = new PackageRequest(
				"PKG-001",
				90.0,
				60.0,
				50.0,
				200.0
		);

		mockMvc.perform(post("/api/packages")
						.contentType(APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk());

		verify(packageService).createPackage(request);
	}

	@Test
	void shouldCreateManyPackages() throws Exception {
		final List<PackageRequest> requests = List.of(
				new PackageRequest("PKG-001", 90.0, 60.0, 50.0, 200.0),
				new PackageRequest("PKG-002", 40.0, 20.0, 10.0, 10.0)
		);

		mockMvc.perform(post("/api/packages/create")
						.contentType(APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(requests)))
				.andExpect(status().isOk());

		verify(packageService).createPackages(requests);
	}

	@Test
	void shouldReturnPackages() throws Exception {
		final List<PackageResponse> responses = List.of(
				new PackageResponse("PKG-001", 90.0, 60.0, 50.0, 200.0),
				new PackageResponse("PKG-002", 40.0, 20.0, 10.0, 10.0)
		);

		when(packageService.getPackages()).thenReturn(responses);

		mockMvc.perform(get("/api/packages"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id").value("PKG-001"))
				.andExpect(jsonPath("$[0].length").value(90.0))
				.andExpect(jsonPath("$[0].width").value(60.0))
				.andExpect(jsonPath("$[0].height").value(50.0))
				.andExpect(jsonPath("$[0].weight").value(200.0))
				.andExpect(jsonPath("$[1].id").value("PKG-002"))
				.andExpect(jsonPath("$[1].length").value(40.0))
				.andExpect(jsonPath("$[1].weight").value(10.0));
	}

	@Test
	void shouldReturnPackageById() throws Exception {
		final PackageResponse response = new PackageResponse(
				"PKG-001",
				90.0,
				60.0,
				50.0,
				200.0
		);

		when(packageService.getPackage("PKG-001")).thenReturn(response);

		mockMvc.perform(get("/api/packages/PKG-001"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value("PKG-001"))
				.andExpect(jsonPath("$.length").value(90.0))
				.andExpect(jsonPath("$.width").value(60.0))
				.andExpect(jsonPath("$.height").value(50.0))
				.andExpect(jsonPath("$.weight").value(200.0));
	}

	@Test
	void shouldDeletePackage() throws Exception {
		mockMvc.perform(delete("/api/packages/PKG-001"))
				.andExpect(status().isOk());

		verify(packageService).deletePackage("PKG-001");
	}

	@Test
	void shouldDeleteAllPackages() throws Exception {
		mockMvc.perform(delete("/api/packages"))
				.andExpect(status().isOk());

		verify(packageService).deleteAllPackages();
	}

	@Test
	void shouldReturnBadRequestWhenArrayIsSentToSinglePackageEndpoint() throws Exception {
		final List<PackageRequest> requests = List.of(
				new PackageRequest("PKG-001", 90.0, 60.0, 50.0, 200.0)
		);

		mockMvc.perform(post("/api/packages")
						.contentType(APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(requests)))
				.andExpect(status().isBadRequest());

		verify(packageService, never()).createPackage(any());
	}
}