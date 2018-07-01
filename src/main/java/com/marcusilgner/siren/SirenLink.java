package com.marcusilgner.siren;

import javax.json.JsonObject;

public class SirenLink extends SirenObject implements SubItemMixin {
  public SirenLink(JsonObject json) {
    super(json);
  }
}
