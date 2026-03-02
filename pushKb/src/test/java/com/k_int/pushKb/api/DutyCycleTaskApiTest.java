package com.k_int.pushKb.api;

import com.k_int.pushKb.interactions.gokb.model.Gokb;
import com.k_int.pushKb.interactions.gokb.model.GokbSource;
import com.k_int.pushKb.interactions.gokb.model.GokbSourceType;
import com.k_int.pushKb.model.responses.TaskResetDTO;
import com.k_int.pushKb.services.SourceService;
import com.k_int.pushKb.test.ServiceIntegrationTest;
import com.k_int.taskscheduler.model.DutyCycleTask;
import io.micronaut.core.type.Argument;
import io.micronaut.data.model.Page;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpStatus;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class DutyCycleTaskApiTest extends ServiceIntegrationTest {

	@Inject
	SourceService sourceService;

	private GokbSource createSource(String name) {
		return GokbSource.builder()
			.name(name)
			.gokb(Gokb.builder().name("GOKB").baseUrl("https://" + name + ".gokb.org").build())
			.gokbSourceType(GokbSourceType.PACKAGE)
			.build();
	}

	@Test
	void testListDutyCycleTasksWithPagination() {
		Mono.from(sourceService.ensureSource(createSource("task-list-1"))).block();
		Mono.from(sourceService.ensureSource(createSource("task-list-2"))).block();

		var request = HttpRequest.GET("/dutycycletasks?size=1");
		Page<DutyCycleTask> page = httpClient.toBlocking().retrieve(
			request,
			Argument.of(Page.class, DutyCycleTask.class)
		);

		assertNotNull(page);
		assertEquals(1, page.getContent().size(), "Page size should respect the query parameter");
		assertTrue(page.getTotalSize() >= 2, "Total size should reflect all tasks in the system");
	}

	@Test
	void testManualResetTaskLifecycle() {
		GokbSource saved = (GokbSource) Mono.from(sourceService.ensureSource(createSource("reset-test"))).block();

		Page<DutyCycleTask> taskPage = httpClient.toBlocking().retrieve(
			HttpRequest.GET("/dutycycletasks"),
			Argument.of(Page.class, DutyCycleTask.class)
		);

		UUID taskId = taskPage.getContent().stream()
			.filter(t -> t.getReference().equals(saved.getId().toString()))
			.map(DutyCycleTask::getId)
			.findFirst()
			.orElseThrow(() -> new RuntimeException("Task not found for source"));

		var resetRequest = HttpRequest.PUT("/dutycycletasks/" + taskId + "/reset", "");
		var response = httpClient.toBlocking().exchange(resetRequest, TaskResetDTO.class);

		assertEquals(HttpStatus.OK, response.getStatus());
		TaskResetDTO body = response.body();
		assertNotNull(body);
		assertEquals("success", body.getStatus());
		assertEquals(taskId, body.getId());
		assertEquals("Task status has been reset to IDLE", body.getMessage());
	}

	@Test
	void testResetNonExistentTaskReturns404() {
		UUID fakeId = UUID.randomUUID();

		try {
			httpClient.toBlocking().exchange(
				HttpRequest.PUT("/dutycycletasks/" + fakeId + "/reset", ""),
				Argument.of(TaskResetDTO.class),
				Argument.of(com.k_int.pushKb.api.errors.PushkbAPIError.class)
			);
			fail("Should have returned 404");
		} catch (io.micronaut.http.client.exceptions.HttpClientResponseException e) {
			assertEquals(HttpStatus.NOT_FOUND, e.getStatus());
		}
	}
}
