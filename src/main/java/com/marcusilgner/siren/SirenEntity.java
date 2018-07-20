package com.marcusilgner.siren;

import okhttp3.OkHttpClient;
import okhttp3.Request;

import javax.json.*;
import java.io.StringReader;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// easy access to a Siren entity
public class SirenEntity extends SirenObject {

  public SirenEntity(JsonObject json) {
    super(json);
  }

  public SirenEntity(String json) {
    JsonReader jsonReader = Json.createReader(new StringReader(json));
    this.json = jsonReader.readObject();
    jsonReader.close();
  }

  public List<String> getClasses() {
    JsonValue object = json.get("class");
    return asStringList(object);
  }

  public CompletableFuture<SirenEntity> reload(OkHttpClient httpClient) {
    Optional<SirenLink> link = getLinkByRel("self");
    Optional<String> href = link.isPresent() ? link.get().getHref() : Optional.empty();
    if (!href.isPresent()) {
      href = SirenObject.stringValue(getJson(), "href");
    }
    if (!href.isPresent()) {
      return CompletableFuture.completedFuture(null);
    }

    Request request = HttpClientFacade.createRequestBuilder().get().url(href.get()).build();
    return HttpClientFacade.loadSirenEntity(httpClient, request);
  }

  public List<SirenSubEntity> getSubEntities() {
    JsonArray entities = json.getJsonArray("entities");
    if (entities == null) {
      return Collections.emptyList();
    }
    return buildStream(entities, SirenSubEntity.class).collect(Collectors.toList());
  }

  public List<SirenLink> getLinks() {
    JsonArray links = json.getJsonArray("links");
    if (links == null) {
      return Collections.emptyList();
    }
    return buildStream(links, SirenLink.class).collect(Collectors.toList());
  }

  public Optional<SirenAction> getActionByName(String name) {
    JsonArray actions = json.getJsonArray("actions");
    if (actions == null) {
      return Optional.empty();
    }
    return actions.stream()
                  .map(json -> new SirenAction(json.asJsonObject()))
                  .filter(element -> name.equalsIgnoreCase(element.getName().orElse(null)))
                  .findAny();
  }

  public List<SirenAction> getActions() {
    JsonArray actions = json.getJsonArray("actions");
    if (actions == null) {
      return Collections.emptyList();
    }
    return buildStream(actions, SirenAction.class).collect(Collectors.toList());
  }

  public Optional<SirenLink> getLinkByRel(String rel) {
    JsonArray links = json.getJsonArray("links");
    if (links == null) {
      return Optional.empty();
    }
    return findByRel(buildStream(links, SirenLink.class), rel);
  }

  public Map<String, JsonValue> getProperties() {
    Map<String, JsonValue> properties = json.getJsonObject("properties");
    return properties == null ? Collections.emptyMap() : properties;
  }

  public Optional<JsonValue> getProperty(String name) {
    Map<String, JsonValue> properties = json.getJsonObject("properties");
    JsonValue value = properties.get(name);
    return value == null ? Optional.empty() : Optional.of(value);
  }

  private <T extends SirenObject> Stream<T> buildStream(JsonArray array, Class<T> clazz) {
    return array.stream().map(value -> {
      try {
        return clazz.getDeclaredConstructor(JsonObject.class).newInstance(value.asJsonObject());
      } catch (Exception e) { // gotta catch them all :P
        return null;
      }
    });
  }

  private <T extends SubItemMixin> Optional<T> findByRel(Stream<T> stream, String rel) {
    return stream.filter(element -> {
      Optional<String> any = element.getRelList().stream().filter(foundRel -> foundRel.equals(rel)).findAny();
      return any.isPresent();
    }).findAny();
  }
}
