package com.marcusilgner.siren;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
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
    final Future<SirenEntity> future = HttpClientFacade.loadSirenEntity(request);
    final SirenEntity entity = future.get();
    assertEquals("Order 42", entity.getTitle().get());
  }

  @Test
  public void testAddInterceptor() throws ExecutionException, InterruptedException, IOException {
    Interceptor interceptor = mock(Interceptor.class);
    when(interceptor.intercept(any(Interceptor.Chain.class))).then(i -> {
      Interceptor.Chain chain = ((Interceptor.Chain) i.getArgument(0));
      return chain.proceed(chain.request());
    });
    HttpClientFacade.addInterceptor(interceptor);

    mockWebServer.enqueue(new MockResponse().setBody(loadEntity()));
    final HttpUrl testUrl = mockWebServer.url("/protectedOrder");
    final Request request = new Request.Builder().get().url(testUrl).build();
    final SirenEntity entity = HttpClientFacade.loadSirenEntity(request).get();
    verify(interceptor, times(1)).intercept(any(Interceptor.Chain.class));
    assertNotNull(entity);

  }
}
