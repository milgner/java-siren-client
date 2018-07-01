package com.marcusilgner.siren;

import okhttp3.*;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

// Facilitates working with HTTP. Since it's supposed to be a singleton, make things here static
public class HttpClientFacade {
  private static OkHttpClient httpClient = null;
  private static List<Interceptor> httpInterceptors = new ArrayList<>();

  private HttpClientFacade() {
  }

  public static OkHttpClient getHttpClient() {
    if (httpClient != null) {
      return httpClient;
    }
    initializeHttpClient();
    return httpClient;
  }

  // These can then add authorization information, hide away things like refreshing OAuth tokens etc
  public static void addInterceptor(Interceptor interceptor) {
    httpInterceptors.add(interceptor);
    shutdownExistingClient();
  }

  public static CompletableFuture<SirenEntity> loadSirenEntity(Request request) {
    OkHttpClient client = getHttpClient();
    Call call = client.newCall(request);
    CompletableFuture<SirenEntity> future = new CompletableFuture<>();

    call.enqueue(new Callback() {
      @Override
      public void onFailure(Call call, IOException e) {
        future.completeExceptionally(e);
      }

      @Override
      public void onResponse(Call call, Response response) {
        try {
          InputStream responseStream = response.body().byteStream();
          JsonReader jsonReader = Json.createReader(responseStream);
          JsonObject jsonObject = jsonReader.readObject();
          jsonReader.close();
          SirenEntity entity = new SirenEntity(jsonObject);
          future.complete(entity);
        } finally {
          response.close();
        }
      }
    });

    return future;
  }

  private static synchronized void initializeHttpClient() {
    OkHttpClient.Builder builder = new OkHttpClient.Builder();
    httpInterceptors.forEach(builder::addInterceptor);
    httpClient = builder.build();
  }

  private static synchronized void shutdownExistingClient() {
    if (httpClient == null) {
      return;
    }
    httpClient.dispatcher().executorService().shutdown();
    httpClient.connectionPool().evictAll();
    httpClient = null;
  }
}
