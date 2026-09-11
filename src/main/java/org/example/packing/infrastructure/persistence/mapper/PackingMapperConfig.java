package org.example.packing.infrastructure.persistence.mapper;

import org.mapstruct.MapperConfig;
import org.mapstruct.ReportingPolicy;

/**
 * Shared MapStruct settings for every mapper in the application.
 * <p>
 * {@code unmappedTargetPolicy = ERROR} turns a silent risk into a build
 * failure: without it, a target field that no longer matches any source
 * field by name (a rename on one side, a new field added to only one of
 * the DTO/entity/domain records) is just a compile-time warning, so the
 * value is silently dropped at runtime and nobody notices until data goes
 * missing in production. With this config, the same situation fails the
 * build instead, with MapStruct pointing at the exact unmapped property.
 */
@MapperConfig(
		componentModel = "spring",
		unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface PackingMapperConfig {
}
