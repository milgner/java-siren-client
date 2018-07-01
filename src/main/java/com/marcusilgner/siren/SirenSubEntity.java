package com.marcusilgner.siren;

import javax.json.JsonObject;
import java.util.Optional;

public class SirenSubEntity extends SirenEntity implements SubItemMixin {
  public SirenSubEntity(JsonObject json) {
    super(json);
  }

  public Optional<String> getType() {
    return stringValue(json, "type");
  }
}
