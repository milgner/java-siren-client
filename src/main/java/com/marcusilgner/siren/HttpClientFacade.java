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
  private static HttpRequestFactory httpRequestFactory = null;

  private HttpClientFacade() {
  }

  public static void setHttpRequestFactory(HttpRequestFactory factory) {
    httpRequestFactory = factory;
  }

  public static CompletableFuture<SirenEntity> loadSirenEntity(OkHttpClient client, Request request) {
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

  protected static Request.Builder createRequestBuilder() {
    if (httpRequestFactory != null) {
      return httpRequestFactory.createRequestBuilder();
    }
    return new Request.Builder();
  }
}
