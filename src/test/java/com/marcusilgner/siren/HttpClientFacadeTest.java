package com.marcusilgner.siren;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
    final Future<SirenEntity> future = HttpClientFacade.loadSirenEntity(client, request);
    assertNotNull(future);
    final SirenEntity entity = future.get();
    assertEquals("Order 42", entity.getTitle().get());
  }

  @Test
  public void testSetHttpRequestFactory() throws ExecutionException, InterruptedException, IOException {
    HttpRequestFactory factory = mock(HttpRequestFactory.class);
    when(factory.createRequestBuilder()).then(i -> new Request.Builder());
    HttpClientFacade.setHttpRequestFactory(factory);

    mockWebServer.enqueue(new MockResponse().setBody(loadEntity()));
    final SirenEntity entity = new SirenEntity(loadEntity());
    final OkHttpClient client = new OkHttpClient.Builder().build();
    final SirenEntity reloadedEntity = entity.reload(client).get();
    verify(factory, times(1)).createRequestBuilder();
    assertNotNull(entity);

  }
}
