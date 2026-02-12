package com.k_int.pushKb.api;

import com.k_int.pushKb.interactions.folio.model.FolioDestination;
import com.k_int.pushKb.interactions.folio.model.FolioTenant;
import com.k_int.pushKb.interactions.gokb.model.GokbSource;
import com.k_int.pushKb.model.PushTask;
import com.k_int.pushKb.test.ServiceIntegrationTest;
import com.k_int.pushKb.transform.model.ProteusTransform;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class PushTaskApiTest extends ServiceIntegrationTest {

	@Test
	void testPushTaskLifecycle() {
		UUID sourceId = UUID.randomUUID();
		UUID destId = UUID.randomUUID();
		UUID transformId = UUID.randomUUID();

		PushTask pt = PushTask.builder()
			.sourceId(sourceId)
			.sourceType(GokbSource.class)
			.destinationId(destId)
			.destinationType(FolioDestination.class)
			.transformId(transformId)
			.transformType(ProteusTransform.class)
			.build();

		HttpResponse<PushTask> postResponse = httpClient.toBlocking().exchange(
			HttpRequest.POST("/pushtasks", pt),
			PushTask.class
		);

		assertEquals(HttpStatus.OK, postResponse.getStatus());
		PushTask saved = postResponse.body();
		assertNotNull(saved);
		assertNotNull(saved.getId(), "Deterministic UUID5 should be generated");
		assertEquals(Instant.EPOCH, saved.getLastSentPointer(), "Pointers should default to EPOCH");

		PushTask fetched = httpClient.toBlocking().retrieve(
			HttpRequest.GET("/pushtasks/" + saved.getId()),
			PushTask.class
		);
		assertEquals(saved.getId(), fetched.getId());

		HttpResponse<PushTask> resetResponse = httpClient.toBlocking().exchange(
			HttpRequest.PUT("/pushtasks/" + saved.getId() + "/resetPointers", null),
			PushTask.class
		);
		assertEquals(HttpStatus.OK, resetResponse.getStatus());
		assertEquals(Instant.EPOCH, resetResponse.body().getLastSentPointer());

		HttpResponse<Long> deleteResponse = httpClient.toBlocking().exchange(
			HttpRequest.DELETE("/pushtasks/" + saved.getId()),
			Long.class
		);
		assertEquals(HttpStatus.OK, deleteResponse.getStatus());
		assertEquals(1L, deleteResponse.body());
	}
}
