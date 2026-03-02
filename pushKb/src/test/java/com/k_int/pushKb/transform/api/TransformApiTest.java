package com.k_int.pushKb.transform.api;

import com.k_int.pushKb.test.ServiceIntegrationTest;
import io.micronaut.core.type.Argument;
import io.micronaut.data.model.Page;
import io.micronaut.http.HttpRequest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class TransformApiTest extends ServiceIntegrationTest {

  @Test
  void testGetImplementersPagination() {
    var request = HttpRequest.GET("/transforms/implementers?size=1");
    Page<String> page = httpClient.toBlocking().retrieve(
      request,
      Argument.of(Page.class, String.class)
    );

    assertNotNull(page);
    assertFalse(page.getContent().isEmpty(), "Should have at least one implementer (ProteusTransform)");
    assertTrue(page.getContent().get(0).contains("ProteusTransform"));
    
    assertEquals(1, page.getContent().size());
    assertTrue(page.getTotalSize() >= 1);
  }
}
