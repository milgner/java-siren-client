package com.marcusilgner.siren;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
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

  private String siren = "{\n" +
                         "  \"class\": [ \"order\" ],\n" +
                         "  \"title\": \"Order 42\",\n" +
                         "  \"properties\": { \n" +
                         "      \"orderNumber\": 42, \n" +
                         "      \"itemCount\": 3,\n" +
                         "      \"status\": \"pending\"\n" +
                         "  },\n" +
                         "  \"entities\": [\n" +
                         "    { \n" +
                         "      \"class\": [ \"items\", \"collection\" ], \n" +
                         "      \"rel\": [ \"http://x.io/rels/order-items\" ], \n" +
                         "      \"href\": \"http://api.x.io/orders/42/items\"\n" +
                         "    },\n" +
                         "    {\n" +
                         "      \"class\": [ \"info\", \"customer\" ],\n" +
                         "      \"rel\": [ \"http://x.io/rels/customer\" ], \n" +
                         "      \"properties\": { \n" +
                         "        \"customerId\": \"pj123\",\n" +
                         "        \"name\": \"Peter Joseph\"\n" +
                         "      },\n" +
                         "      \"links\": [\n" +
                         "        { \"rel\": [ \"self\" ], \"href\": \"http://api.x.io/customers/pj123\" }\n" +
                         "      ]\n" +
                         "    }\n" +
                         "  ],\n" +
                         "  \"actions\": [\n" +
                         "    {\n" +
                         "      \"name\": \"add-item\",\n" +
                         "      \"title\": \"Add Item\",\n" +
                         "      \"method\": \"POST\",\n" +
                         "      \"href\": \"http://api.x.io/orders/42/items\",\n" +
                         "      \"type\": \"application/x-www-form-urlencoded\",\n" +
                         "      \"fields\": [\n" +
                         "        { \"name\": \"orderNumber\", \"type\": \"hidden\", \"value\": \"42\" },\n" +
                         "        { \"name\": \"productCode\", \"type\": \"text\" },\n" +
                         "        { \"name\": \"quantity\", \"type\": \"number\" }\n" +
                         "      ]\n" +
                         "    }\n" +
                         "  ],\n" +
                         "  \"links\": [\n" +
                         "    { \"rel\": [ \"self\" ], \"href\": \"http://api.x.io/orders/42\" },\n" +
                         "    { \"rel\": [ \"previous\" ], \"href\": \"http://api.x.io/orders/41\" },\n" +
                         "    { \"rel\": [ \"next\" ], \"href\": \"http://api.x.io/orders/43\" }\n" +
                         "  ]\n" +
                         "}";

  @BeforeEach
  private void setUp() {
    mockWebServer = new MockWebServer();
  }

  @AfterEach
  private void tearDown() throws IOException {
    mockWebServer.shutdown();
  }

  @Test
  public void testLoadSirenEntity() throws ExecutionException, InterruptedException {
    mockWebServer.enqueue(new MockResponse().setBody(siren));
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

    mockWebServer.enqueue(new MockResponse().setBody(siren));
    final HttpUrl testUrl =  mockWebServer.url("/protectedOrder");
    final Request request = new Request.Builder().get().url(testUrl).build();
    final SirenEntity entity = HttpClientFacade.loadSirenEntity(request).get();
    verify(interceptor, times(1)).intercept(any(Interceptor.Chain.class));
    assertNotNull(entity);

  }
}
