package com.k_int.pushKb.services;

import com.k_int.pushKb.interactions.gokb.model.Gokb;
import com.k_int.pushKb.interactions.gokb.model.GokbSource;
import com.k_int.pushKb.interactions.gokb.model.GokbSourceType;
import com.k_int.pushKb.test.ServiceIntegrationTest;
import com.k_int.taskscheduler.model.DutyCycleTask;
import io.micronaut.core.type.Argument;
import io.micronaut.data.model.Page;
import io.micronaut.http.HttpRequest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SourceServiceTest extends ServiceIntegrationTest {

	@Inject
	SourceService sourceService;

	@BeforeEach
	void setup() {
		// We spy on the service to verify orchestration without replacing it
		sourceService = spy(sourceService);
	}

	private GokbSource setupGokbSource() {
		return GokbSource.builder()
			.name("SourceService API Test")
			.gokb(Gokb.builder()
				.name("GOKB Instance")
				.baseUrl("https://service.test.gokb.org")
				.build())
			.gokbSourceType(GokbSourceType.PACKAGE)
			.build();
	}

	@Test
	void testEnsureSourceAndVerifyViaApi() {
		GokbSource source = setupGokbSource();

		// 1. Use the service to ensure the source
		GokbSource saved = Mono.from(sourceService.ensureSource(source))
			.map(GokbSource::castFromSource)
			.block();

		assertNotNull(saved.getId());

		verify(sourceService, times(1)).registerIngestTask(eq(GokbSource.class), eq(saved));

		Page<DutyCycleTask> taskPage = httpClient.toBlocking().retrieve(
			HttpRequest.GET("/dutycycletasks?size=10&sort=id,asc"),
			Argument.of(Page.class, DutyCycleTask.class)
		);

		boolean taskExists = taskPage.getContent().stream()
			.anyMatch(task -> task.getReference().equals(saved.getId().toString()));

		assertTrue(taskExists, "The IngestScheduledTask should be visible via the DutyCycleTask API");
	}

	@Test
	void testDeleteSourceAndVerifyApiCleanup() {
		GokbSource source = setupGokbSource();
		GokbSource saved = Mono.from(sourceService.ensureSource(source))
			.map(GokbSource::castFromSource).block();

		Mono.from(sourceService.delete(GokbSource.class, saved)).block();

		Page<DutyCycleTask> taskPage = httpClient.toBlocking().retrieve(
			HttpRequest.GET("/dutycycletasks"),
			Argument.of(Page.class, DutyCycleTask.class)
		);

		boolean taskStillExists = taskPage.getContent().stream()
			.anyMatch(task -> task.getReference().equals(saved.getId().toString()));

		assertFalse(taskStillExists, "The DutyCycleTask should have been removed from the API/DB");
	}
}
