package com.marcusilgner.siren;

import javax.json.JsonObject;
import java.util.List;
import java.util.Optional;

public interface SubItemMixin {
  JsonObject getJson();

  default List<String> getRelList() {
    return SirenObject.asStringList(getJson().get("rel"));
  }

  default Optional<String> getHref() {
    return SirenObject.stringValue(getJson(), "href");
  }
}
