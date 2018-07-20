package com.marcusilgner.siren;

import okhttp3.*;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

// Facilitates working with HTTP. Since it's supposed to be a singleton, make things here static
public class HttpClientFacade {
  private static HttpRequestFactory httpRequestFactory = null;

  private HttpClientFacade() {
  }

  public static void setHttpRequestFactory(HttpRequestFactory factory) {
    httpRequestFactory = factory;
  }

  public static Optional<SirenEntity> loadSirenEntity(OkHttpClient client, Request request) {
    Call call = client.newCall(request);
    try {
      Response response = call.execute();
      if (!response.isSuccessful()) {
        return Optional.empty();
      }
      InputStream responseStream = response.body().byteStream();
      JsonReader jsonReader = Json.createReader(responseStream);
      JsonObject jsonObject = jsonReader.readObject();
      jsonReader.close();
      return Optional.of(new SirenEntity(jsonObject));
    } catch (IOException e) {
      return Optional.empty();
    }
  }

  protected static Request.Builder createRequestBuilder() {
    if (httpRequestFactory != null) {
      return httpRequestFactory.createRequestBuilder();
    }
    return new Request.Builder();
  }
}
