package com.marcusilgner.siren;

import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;
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

  public CompletableFuture<SirenEntity> submit() {
    String href = getHref().orElse(null);
    if (href == null) {
      CompletableFuture<SirenEntity> future = new CompletableFuture();
      future.completeExceptionally(new IllegalArgumentException("Missing action href"));
      return future;
    }
    Request request = new Request.Builder().url(href).method(getMethod(), getRequestBody()).build();
    return HttpClientFacade.loadSirenEntity(request);
  }

  private RequestBody getRequestBody() {
    switch (getType()) {
      case "application/x-www-form-urlencoded":
        return getFormEncodedRequestBody();
      case "application/json":
        return getJsonEncodedRequestBody();
      default:
        return null;
    }
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
  class ActionField extends SirenObject {
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
