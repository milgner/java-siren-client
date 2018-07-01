package com.marcusilgner.siren;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class SirenObject {
  protected JsonObject json;

  protected SirenObject() {
  }

  public SirenObject(JsonObject json) {
    this.json = json;
  }

  public static final Optional<String> stringValue(JsonObject object, String key) {
    JsonString jsonString = object.getJsonString(key);
    return jsonString == null ? Optional.empty() : Optional.of(jsonString.getString());
  }

  public static final List<String> asStringList(JsonValue object) {
    if (object instanceof JsonArray) {
      return ((JsonArray) object).getValuesAs(JsonString.class)
                                 .stream()
                                 .map(JsonString::getString)
                                 .collect(Collectors.toList());
    } else if (object instanceof JsonString) {
      return Arrays.asList(((JsonString) object).getString());
    }
    return Collections.emptyList();
  }

  public JsonObject getJson() {
    return json;
  }

  public Optional<String> getTitle() {
    return stringValue(json, "title");
  }

}
