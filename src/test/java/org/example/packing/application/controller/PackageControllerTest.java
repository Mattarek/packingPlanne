package org.example.packing.application.controller;

import org.example.packing.application.dto.PackageRequest;
import org.example.packing.application.dto.PackageResponse;
import org.example.packing.application.service.PackageService;
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

@WebMvcTest(PackageController.class)
class PackageControllerTest {

	private static final String PACKAGES_URL = "/api/packages";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private PackageService packageService;

	private UUID packageId1;
	private UUID packageId2;

	private PackageRequest packageRequest1;
	private PackageRequest packageRequest2;
	private PackageRequest singlePackageRequest;

	private PackageResponse packageResponse1;
	private PackageResponse packageResponse2;
	private PackageResponse singlePackageResponse;

	private List<PackageRequest> packageRequests;
	private List<PackageResponse> packageResponses;

	private Page<PackageResponse> packageResponsePage;

	@BeforeEach
	void setUp() {
		packageId1 = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
		packageId2 = UUID.fromString("123e4567-e89b-12d3-a456-426614174001");

		packageRequest1 = new PackageRequest(
				90.0,
				60.0,
				50.0,
				200.0
		);

		packageRequest2 = new PackageRequest(
				40.0,
				20.0,
				10.0,
				10.0
		);

		singlePackageRequest = packageRequest1;

		packageResponse1 = new PackageResponse(
				packageId1,
				90.0,
				60.0,
				50.0,
				200.0
		);

		packageResponse2 = new PackageResponse(
				packageId2,
				40.0,
				20.0,
				10.0,
				10.0
		);

		singlePackageResponse = packageResponse1;

		packageRequests = List.of(
				packageRequest1,
				packageRequest2
		);

		packageResponses = List.of(
				packageResponse1,
				packageResponse2
		);

		packageResponsePage = new PageImpl<>(
				packageResponses,
				PageRequest.of(0, 20),
				packageResponses.size()
		);
	}

	@AfterEach
	void tearDown() {
		reset(packageService);
	}

	@Test
	void shouldCreatePackages() throws Exception {
		when(packageService.createPackages(packageRequests)).thenReturn(packageResponses);

		mockMvc.perform(post(PACKAGES_URL)
						.contentType(APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(packageRequests)))
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

		verify(packageService).createPackages(packageRequests);
	}

	@Test
	void shouldReturnPackages() throws Exception {
		when(packageService.getPackages(0, 20)).thenReturn(packageResponsePage);

		mockMvc.perform(get(PACKAGES_URL)
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
		when(packageService.getPackage(packageId1)).thenReturn(singlePackageResponse);

		mockMvc.perform(get(PACKAGES_URL + "/{id}", packageId1))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value("Package fetched successfully."))
				.andExpect(jsonPath("$.data.id").value(packageId1.toString()))
				.andExpect(jsonPath("$.data.length").value(90.0))
				.andExpect(jsonPath("$.data.width").value(60.0))
				.andExpect(jsonPath("$.data.height").value(50.0))
				.andExpect(jsonPath("$.data.weight").value(200.0));

		verify(packageService).getPackage(packageId1);
	}

	@Test
	void shouldDeletePackage() throws Exception {
		mockMvc.perform(delete(PACKAGES_URL + "/{id}", packageId1))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value("Package deleted successfully."))
				.andExpect(jsonPath("$.data.deletedId").value(packageId1.toString()));

		verify(packageService).deletePackage(packageId1);
	}

	@Test
	void shouldReturnBadRequestWhenObjectIsSentToPackagesEndpoint() throws Exception {
		mockMvc.perform(post(PACKAGES_URL)
						.contentType(APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(singlePackageRequest)))
				.andExpect(status().isBadRequest());

		verify(packageService, never()).createPackages(any());
	}

	@Test
	void shouldReturnBadRequestWhenInvalidUuidIsSent() throws Exception {
		mockMvc.perform(get(PACKAGES_URL + "/invalid-id"))
				.andExpect(status().isBadRequest());

		verify(packageService, never()).getPackage(any());
	}
}