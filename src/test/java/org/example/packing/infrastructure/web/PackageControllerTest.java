package org.example.packing.infrastructure.web;

import org.example.packing.application.command.CreatePackageCommand;
import org.example.packing.application.port.PageResult;
import org.example.packing.application.port.in.PackageUseCase;
import org.example.packing.domain.exception.PackageNotAcceptedException;
import org.example.packing.domain.exception.PackageNotFoundException;
import org.example.packing.domain.model.Dimensions;
import org.example.packing.domain.model.FragilityLevel;
import org.example.packing.domain.model.Package;
import org.example.packing.domain.model.ProductCategory;
import org.example.packing.domain.model.Weight;
import org.example.packing.infrastructure.web.dto.PackageRequest;
import org.example.packing.infrastructure.web.mapper.RestRequestMapperImpl;
import org.example.packing.infrastructure.web.mapper.RestResponseMapperImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
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
@Import({RestRequestMapperImpl.class, RestResponseMapperImpl.class})
class PackageControllerTest {

	private static final String PACKAGES_URL = "/api/packages";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private PackageUseCase packageUseCase;

	private UUID packageId1;
	private UUID packageId2;

	private PackageRequest packageRequest1;
	private PackageRequest packageRequest2;
	private PackageRequest singlePackageRequest;

	private Package package1;
	private Package package2;
	private Package singlePackage;

	private List<PackageRequest> packageRequests;
	private List<CreatePackageCommand> packageCommands;
	private List<Package> packages;

	private PageResult<Package> packagePage;

	@BeforeEach
	void setUp() {
		packageId1 = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
		packageId2 = UUID.fromString("123e4567-e89b-12d3-a456-426614174001");

		packageRequest1 = new PackageRequest(
				90.0,
				60.0,
				50.0,
				200.0,
				ProductCategory.STANDARD,
				FragilityLevel.STANDARD
		);

		packageRequest2 = new PackageRequest(
				40.0,
				20.0,
				10.0,
				10.0,
				ProductCategory.ELECTRONICS,
				FragilityLevel.FRAGILE
		);

		singlePackageRequest = packageRequest1;

		package1 = new Package(
				packageId1,
				new Dimensions(90.0, 60.0, 50.0),
				new Weight(200.0),
				ProductCategory.STANDARD,
				FragilityLevel.STANDARD
		);

		package2 = new Package(
				packageId2,
				new Dimensions(40.0, 20.0, 10.0),
				new Weight(10.0),
				ProductCategory.ELECTRONICS,
				FragilityLevel.FRAGILE
		);

		singlePackage = package1;

		packageRequests = List.of(
				packageRequest1,
				packageRequest2
		);

		packageCommands = List.of(
				new CreatePackageCommand(90.0, 60.0, 50.0, 200.0, ProductCategory.STANDARD, FragilityLevel.STANDARD),
				new CreatePackageCommand(40.0, 20.0, 10.0, 10.0, ProductCategory.ELECTRONICS, FragilityLevel.FRAGILE)
		);

		packages = List.of(
				package1,
				package2
		);

		packagePage = new PageResult<>(packages, 0, 20, packages.size());
	}

	@AfterEach
	void tearDown() {
		reset(packageUseCase);
	}

	@Test
	void shouldCreatePackages() throws Exception {
		when(packageUseCase.createPackages(packageCommands)).thenReturn(packages);

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

		verify(packageUseCase).createPackages(packageCommands);
	}

	@Test
	void shouldReturnPackages() throws Exception {
		when(packageUseCase.getPackages(0, 20)).thenReturn(packagePage);

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

		verify(packageUseCase).getPackages(0, 20);
	}

	@Test
	void shouldReturnPackageById() throws Exception {
		when(packageUseCase.getPackage(packageId1)).thenReturn(singlePackage);

		mockMvc.perform(get(PACKAGES_URL + "/{id}", packageId1))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value("Package fetched successfully."))
				.andExpect(jsonPath("$.data.id").value(packageId1.toString()))
				.andExpect(jsonPath("$.data.length").value(90.0))
				.andExpect(jsonPath("$.data.width").value(60.0))
				.andExpect(jsonPath("$.data.height").value(50.0))
				.andExpect(jsonPath("$.data.weight").value(200.0));

		verify(packageUseCase).getPackage(packageId1);
	}

	@Test
	void shouldDeletePackage() throws Exception {
		mockMvc.perform(delete(PACKAGES_URL + "/{id}", packageId1))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value("Package deleted successfully."))
				.andExpect(jsonPath("$.data.deletedId").value(packageId1.toString()));

		verify(packageUseCase).deletePackage(packageId1);
	}

	@Test
	void shouldReturnBadRequestWhenObjectIsSentToPackagesEndpoint() throws Exception {
		mockMvc.perform(post(PACKAGES_URL)
						.contentType(APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(singlePackageRequest)))
				.andExpect(status().isBadRequest());

		verify(packageUseCase, never()).createPackages(any());
	}

	@Test
	void shouldReturnBadRequestWhenInvalidUuidIsSent() throws Exception {
		mockMvc.perform(get(PACKAGES_URL + "/invalid-id"))
				.andExpect(status().isBadRequest());

		verify(packageUseCase, never()).getPackage(any());
	}

	@Test
	void shouldReturnBadRequestWhenSizeExceedsMaximum() throws Exception {
		mockMvc.perform(get(PACKAGES_URL + "?size=99999"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void shouldReturnNotFoundForAnyNotFoundDomainException() throws Exception {
		when(packageUseCase.getPackage(packageId1)).thenThrow(new PackageNotFoundException(packageId1));

		mockMvc.perform(get(PACKAGES_URL + "/{id}", packageId1))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.message").value("Package not found: " + packageId1));
	}

	@Test
	void shouldReturnUnprocessableEntityForAnyBusinessRuleViolation() throws Exception {
		when(packageUseCase.createPackages(any())).thenThrow(new PackageNotAcceptedException("too heavy"));

		mockMvc.perform(post(PACKAGES_URL)
						.contentType(APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(packageRequests)))
				.andExpect(status().isUnprocessableContent())
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.message").value("Package not accepted: too heavy"));
	}

	@Test
	void shouldReturnBadRequestWhenSizeIsZero() throws Exception {
		mockMvc.perform(get(PACKAGES_URL + "?size=0"))
				.andExpect(status().isBadRequest());

		verify(packageUseCase, never()).getPackages(anyInt(), anyInt());
	}

	@Test
	void shouldReturnBadRequestWithReasonWhenListElementIsInvalid() throws Exception {
		final PackageRequest invalid = new PackageRequest(
				-1.0, 60.0, 50.0, 20.0, ProductCategory.STANDARD, FragilityLevel.STANDARD
		);

		mockMvc.perform(post(PACKAGES_URL)
						.contentType(APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(List.of(invalid))))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value(containsString("Length must be positive.")));

		verify(packageUseCase, never()).createPackages(any());
	}
}
