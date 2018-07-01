package com.marcusilgner.siren;

import okhttp3.Request;

import javax.json.*;
import java.io.StringReader;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

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

  public CompletableFuture<SirenEntity> reload() {
    Optional<String> url = getLinkHrefByRel("self");
    if (!url.isPresent()) {
      url = SirenObject.stringValue(getJson(), "href");
      if (!url.isPresent()) {
        return CompletableFuture.completedFuture(null);
      }
    }

    Request request = new Request.Builder().get().url(url.get()).build();
    return HttpClientFacade.loadSirenEntity(request);
  }

  public List<SirenSubEntity> getSubEntities() {
    JsonArray entities = json.getJsonArray("entities");
    if (entities == null) {
      return Collections.emptyList();
    }
    return entities.stream()
                   .map(jsonValue -> new SirenSubEntity(jsonValue.asJsonObject()))
                   .collect(Collectors.toList());
  }

  public List<SirenLink> getLinks() {
    JsonArray links = json.getJsonArray("links");
    if (links == null) {
      return Collections.emptyList();
    }
    return links.stream()
                .map(value -> new SirenLink(value.asJsonObject()))
                .collect(Collectors.toList());
  }

  public List<SirenAction> getActions() {
    JsonArray actions = json.getJsonArray("actions");
    if (actions == null) {
      return Collections.emptyList();
    }
    return actions.stream()
                  .map(value -> new SirenAction(value.asJsonObject()))
                  .collect(Collectors.toList());
  }

  public Optional<String> getLinkHrefByRel(String linkRel) {
    Optional<SirenLink> link = getLinks().stream().filter(element -> {
      Optional<String> any = element.getRelList().stream().filter(rel -> rel.equals(linkRel)).findAny();
      return any.isPresent();
    }).findAny();
    if (link.isPresent()) {
      return link.get().getHref();
    } else {
      return Optional.empty();
    }
  }

  public Map<String, JsonValue> getProperties() {
    Map<String, JsonValue> properties = json.getJsonObject("properties");
    return properties == null ? Collections.emptyMap() : properties;
  }
}
