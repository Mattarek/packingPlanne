package org.example.packing.infrastructure.kafka.handler;

import org.example.packing.application.command.CreatePackageCommand;
import org.example.packing.application.port.in.PackageUseCase;
import org.example.packing.domain.model.Package;
import org.example.packing.infrastructure.kafka.event.EventEnumMapper;
import org.example.packing.infrastructure.kafka.event.PackageCreateRequestEvent;
import org.example.packing.infrastructure.kafka.event.PackageCreateRequestItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PackageCreateRequestV1Handler implements PackageCreateRequestHandler {

	private static final Logger log = LoggerFactory.getLogger(PackageCreateRequestV1Handler.class);

	private final PackageUseCase packageUseCase;

	public PackageCreateRequestV1Handler(final PackageUseCase packageUseCase) {
		this.packageUseCase = packageUseCase;
	}

	@Override
	public String eventType() {
		return "PACKAGE_CREATE_REQUESTED";
	}

	@Override
	public int version() {
		return 1;
	}

	@Override
	public void handle(final PackageCreateRequestEvent event) {
		final List<CreatePackageCommand> commands = event.packages().stream()
				.map(this::toCommand)
				.toList();

		final List<Package> created = packageUseCase.createPackages(commands);

		log.info(
				"Package create event processed: eventId={}, createdPackages={}",
				event.eventId(),
				created.size()
		);
	}

	private CreatePackageCommand toCommand(final PackageCreateRequestItem item) {
		return new CreatePackageCommand(
				item.length(),
				item.width(),
				item.height(),
				item.weight(),
				EventEnumMapper.categoryFromWire(item.category()),
				EventEnumMapper.fragilityFromWire(item.fragility())
		);
	}
}
