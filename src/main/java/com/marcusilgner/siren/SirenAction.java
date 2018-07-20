package com.marcusilgner.siren;

import okhttp3.*;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

// Encapsulates Siren actions and provides helpers to submit them
public class SirenAction extends SirenObject implements SubItemMixin {

  private List<ActionField> fields = null;

  public SirenAction(JsonObject json) {
    super(json);
  }

  public Optional<String> getName() {
    return stringValue(json, "name");
  }

  public List<String> getClazz() {
    return asStringList(json.get("class"));
  }

  public String getMethod() {
    return stringValue(json, "method").orElse("GET");
  }

  public String getType() {
    return stringValue(json, "type").orElse("application/x-www-form-urlencoded");
  }

  public CompletableFuture<SirenEntity> submit(OkHttpClient httpClient) {
    String href = getHref().orElse(null);
    if (href == null) {
      return createFailedFuture("Missing action href");
    }
    RequestBody requestBody = getRequestBody();
    if (requestBody == null) {
      return createFailedFuture("Unable to build request body");
    }
    Request request = new Request.Builder().url(href).method(getMethod(), requestBody).build();
    return HttpClientFacade.loadSirenEntity(httpClient, request);
  }

  private CompletableFuture<SirenEntity> createFailedFuture(String errorMessage) {
    CompletableFuture<SirenEntity> future = new CompletableFuture<>();
    future.completeExceptionally(new IllegalArgumentException(errorMessage));
    return future;
  }

  private RequestBody getRequestBody() {
    switch (getType()) {
      case "application/x-www-form-urlencoded":
        return getFormEncodedRequestBody();
      case "application/json":
        return getJsonEncodedRequestBody();
      case "multipart/form-data":
        try {
          return getMultipartFormBody();
        } catch (IOException e) {
          return null;
        }
      default:
        return null;
    }
  }

  private RequestBody getMultipartFormBody() throws IOException {
    MultipartBody.Builder builder = new MultipartBody.Builder()
                                        .setType(MultipartBody.FORM);
    for (ActionField field : fields) {
      if (field.getValue() == null) {
        continue;
      }
      if (field.getType().equalsIgnoreCase("file")) {
        // interpret string value as path
        Path file = Paths.get(field.getValue());
        RequestBody fileBody = RequestBody.create(MediaType.parse(Files.probeContentType(file)), file.toFile());
        builder.addFormDataPart(field.getName(), file.getFileName().toString(), fileBody);
      } else {
        builder.addFormDataPart(field.getName(), field.getValue());
      }
    }
    ;
    return builder.build();
  }

  private RequestBody getJsonEncodedRequestBody() {
    JsonObjectBuilder builder = Json.createObjectBuilder();
    fields.forEach(field -> builder.add(field.getName(), field.getValue()));
    return new JsonRequestBody(builder.build());
  }

  private RequestBody getFormEncodedRequestBody() {
    FormBody.Builder builder = new FormBody.Builder();
    fields.forEach(field -> builder.add(field.getName(), field.getValue()));
    return builder.build();
  }

  private synchronized void populateFields() {
    fields = json.getJsonArray("fields").stream().map(ActionField::new).collect(Collectors.toList());
  }

  public Optional<ActionField> getField(String name) {
    if (fields == null) {
      populateFields();
    }
    return fields.stream().filter(f -> f.getName().equals(name)).findAny();
  }

  public List<ActionField> getFields() {
    if (fields != null) {
      return fields;
    }
    populateFields();
    return fields;
  }

  // TODO: support non-string values (probably using Generics, will need adapter to retrieve data from Json)
  public class ActionField extends SirenObject {
    private String value;

    private ActionField(JsonValue json) {
      super(json.asJsonObject());
      Optional<String> optValue = stringValue(json.asJsonObject(), "value");
      if (optValue.isPresent()) {
        this.value = optValue.get();
      } else {
        this.value = null;
      }
    }

    public String getName() {
      return stringValue(json, "name").orElse(null);
    }

    public String getType() {
      return stringValue(json, "type").orElse("text");
    }

    public String getValue() {
      return value;
    }

    public void setValue(String value) {
      this.value = value;
    }

    public List<String> getClazz() {
      return asStringList(json.get("class"));
    }
  }

}
