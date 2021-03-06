package com.marcusilgner.siren;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class HttpClientFacadeTest {

  private MockWebServer mockWebServer;

  private String loadEntity() throws IOException {
    return IOUtils.toString(
        this.getClass().getResourceAsStream("entity.json"),
        "UTF-8"
    );
  }

  @BeforeEach
  private void setUp() {
    mockWebServer = new MockWebServer();
  }

  @AfterEach
  private void tearDown() throws IOException {
    mockWebServer.shutdown();
  }

  @Test
  public void testLoadSirenEntity() throws ExecutionException, InterruptedException, IOException {
    mockWebServer.enqueue(new MockResponse().setBody(loadEntity()));
    final HttpUrl testUrl = mockWebServer.url("/orders");
    final Request request = new Request.Builder().get().url(testUrl).build();
    final OkHttpClient client = new OkHttpClient.Builder().build();
    final Optional<SirenEntity> entity = HttpClientFacade.loadSirenEntity(client, request);
    assertTrue(entity.isPresent());
    assertEquals("Order 42", entity.get().getTitle().get());
  }

  private SirenLink fakeSirenLink() {
    return new SirenLink(Fixtures.buildSirenLink("self", mockWebServer.url("/orders/42").toString()).build());
  }

  @Test
  public void testSetHttpRequestFactory() throws IOException {
    HttpRequestFactory factory = mock(HttpRequestFactory.class);
    when(factory.createRequestBuilder()).then(i -> new Request.Builder());
    HttpClientFacade.setHttpRequestFactory(factory);

    mockWebServer.enqueue(new MockResponse().setBody(loadEntity()));
    final SirenEntity entity = new SirenEntity(loadEntity());
    final OkHttpClient client = new OkHttpClient.Builder().build();
    when(entity.getLinkByRel("self")).thenReturn(Optional.of(fakeSirenLink()));
    final Optional<SirenEntity> reloadedEntity = entity.reload(client);
    verify(factory, times(1)).createRequestBuilder();
    assertTrue(reloadedEntity.isPresent());
  }
}
