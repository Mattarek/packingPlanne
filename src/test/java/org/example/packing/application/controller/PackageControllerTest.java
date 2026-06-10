package org.example.packing.application.controller;

import org.example.packing.application.dto.PackageRequest;
import org.example.packing.application.dto.PackageResponse;
import org.example.packing.application.service.PackageService;
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
	void shouldCreatePackages() throws Exception {
		final UUID packageId1 = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
		final UUID packageId2 = UUID.fromString("123e4567-e89b-12d3-a456-426614174001");

		final List<PackageRequest> requests = List.of(
				new PackageRequest(90.0, 60.0, 50.0, 200.0),
				new PackageRequest(40.0, 20.0, 10.0, 10.0)
		);

		final List<PackageResponse> responses = List.of(
				new PackageResponse(packageId1, 90.0, 60.0, 50.0, 200.0),
				new PackageResponse(packageId2, 40.0, 20.0, 10.0, 10.0)
		);

		when(packageService.createPackages(requests)).thenReturn(responses);

		mockMvc.perform(post("/api/packages")
						.contentType(APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(requests)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value("Packages created successfully."))
				.andExpect(jsonPath("$.data[0].id").value(packageId1.toString()))
				.andExpect(jsonPath("$.data[0].length").value(90.0))
				.andExpect(jsonPath("$.data[0].width").value(60.0))
				.andExpect(jsonPath("$.data[0].height").value(50.0))
				.andExpect(jsonPath("$.data[0].weight").value(200.0))
				.andExpect(jsonPath("$.data[1].id").value(packageId2.toString()))
				.andExpect(jsonPath("$.data[1].length").value(40.0))
				.andExpect(jsonPath("$.data[1].width").value(20.0))
				.andExpect(jsonPath("$.data[1].height").value(10.0))
				.andExpect(jsonPath("$.data[1].weight").value(10.0));

		verify(packageService).createPackages(requests);
	}

	@Test
	void shouldReturnPackages() throws Exception {
		final UUID packageId1 = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
		final UUID packageId2 = UUID.fromString("123e4567-e89b-12d3-a456-426614174001");

		final List<PackageResponse> responses = List.of(
				new PackageResponse(packageId1, 90.0, 60.0, 50.0, 200.0),
				new PackageResponse(packageId2, 40.0, 20.0, 10.0, 10.0)
		);

		final Page<PackageResponse> page = new PageImpl<>(
				responses,
				PageRequest.of(0, 20),
				responses.size()
		);

		when(packageService.getPackages(0, 20)).thenReturn(page);

		mockMvc.perform(get("/api/packages")
						.param("page", "0")
						.param("size", "20"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value("Packages fetched successfully."))
				.andExpect(jsonPath("$.data.content[0].id").value(packageId1.toString()))
				.andExpect(jsonPath("$.data.content[0].length").value(90.0))
				.andExpect(jsonPath("$.data.content[0].width").value(60.0))
				.andExpect(jsonPath("$.data.content[0].height").value(50.0))
				.andExpect(jsonPath("$.data.content[0].weight").value(200.0))
				.andExpect(jsonPath("$.data.content[1].id").value(packageId2.toString()))
				.andExpect(jsonPath("$.data.content[1].length").value(40.0))
				.andExpect(jsonPath("$.data.content[1].width").value(20.0))
				.andExpect(jsonPath("$.data.content[1].height").value(10.0))
				.andExpect(jsonPath("$.data.content[1].weight").value(10.0));

		verify(packageService).getPackages(0, 20);
	}

	@Test
	void shouldReturnPackageById() throws Exception {
		final UUID packageId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

		final PackageResponse response = new PackageResponse(
				packageId,
				90.0,
				60.0,
				50.0,
				200.0
		);

		when(packageService.getPackage(packageId)).thenReturn(response);

		mockMvc.perform(get("/api/packages/{id}", packageId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value("Package fetched successfully."))
				.andExpect(jsonPath("$.data.id").value(packageId.toString()))
				.andExpect(jsonPath("$.data.length").value(90.0))
				.andExpect(jsonPath("$.data.width").value(60.0))
				.andExpect(jsonPath("$.data.height").value(50.0))
				.andExpect(jsonPath("$.data.weight").value(200.0));

		verify(packageService).getPackage(packageId);
	}

	@Test
	void shouldDeletePackage() throws Exception {
		final UUID packageId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

		mockMvc.perform(delete("/api/packages/{id}", packageId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value("Package deleted successfully."))
				.andExpect(jsonPath("$.data.id").value(packageId.toString()));

		verify(packageService).deletePackage(packageId);
	}

	@Test
	void shouldReturnBadRequestWhenObjectIsSentToPackagesEndpoint() throws Exception {
		final PackageRequest request = new PackageRequest(
				90.0,
				60.0,
				50.0,
				200.0
		);

		mockMvc.perform(post("/api/packages")
						.contentType(APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest());

		verify(packageService, never()).createPackages(any());
	}

	@Test
	void shouldReturnBadRequestWhenInvalidUuidIsSent() throws Exception {
		mockMvc.perform(get("/api/packages/invalid-id"))
				.andExpect(status().isBadRequest());

		verify(packageService, never()).getPackage(any());
	}
}