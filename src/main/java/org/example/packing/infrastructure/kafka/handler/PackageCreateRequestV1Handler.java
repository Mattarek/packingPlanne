package org.example.packing.infrastructure.kafka.handler;

import org.example.packing.application.command.CreatePackageCommand;
import org.example.packing.application.port.in.PackageUseCase;
import org.example.packing.domain.model.Package;
import org.example.packing.infrastructure.kafka.event.EventContract;
import org.example.packing.infrastructure.kafka.event.EventType;
import org.example.packing.infrastructure.kafka.event.FragilityLevelWire;
import org.example.packing.infrastructure.kafka.event.PackageCreateRequestEvent;
import org.example.packing.infrastructure.kafka.event.PackageCreateRequestItem;
import org.example.packing.infrastructure.kafka.event.ProductCategoryWire;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PackageCreateRequestV1Handler implements PackageCreateRequestHandler {

	private static final Logger log = LoggerFactory.getLogger(PackageCreateRequestV1Handler.class);

	private static final EventContract CONTRACT = EventContract.v1(EventType.PACKAGE_CREATE_REQUESTED);

	private final PackageUseCase packageUseCase;

	public PackageCreateRequestV1Handler(final PackageUseCase packageUseCase) {
		this.packageUseCase = packageUseCase;
	}

	@Override
	public EventContract contract() {
		return CONTRACT;
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
				ProductCategoryWire.fromWire(item.category()).domain(),
				FragilityLevelWire.fromWire(item.fragility()).domain()
		);
	}
}
