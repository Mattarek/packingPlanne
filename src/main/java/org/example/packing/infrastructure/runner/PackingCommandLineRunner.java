package org.example.packing.infrastructure.runner;

import org.example.packing.application.dto.PackingReport;
import org.example.packing.application.service.PackingRunnerService;
import org.example.packing.application.service.ReportPrinter;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2)
public class PackingCommandLineRunner implements CommandLineRunner {

	private final PackingRunnerService packingRunnerService;
	private final ReportPrinter reportPrinter;

	public PackingCommandLineRunner(
			final PackingRunnerService packingRunnerService,
			final ReportPrinter reportPrinter
	) {
		this.packingRunnerService = packingRunnerService;
		this.reportPrinter = reportPrinter;
	}

	@Override
	public void run(final String... args) {
		final PackingReport report = packingRunnerService.runPacking();
		System.out.println(reportPrinter.print(report));
	}
}