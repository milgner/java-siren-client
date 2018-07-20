package com.marcusilgner.siren;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.util.Arrays;

public class Fixtures {
  public static JsonObject getEntityJsonObject() {
    JsonObject subEntity = Json.createObjectBuilder()
                               .add("class", Json.createArrayBuilder().add("items").add("collection"))
                               .add("rel", Json.createArrayBuilder().add("http://x.io/rels/order-items"))
                               .add("href", "http://api.x.io/orders/42/items")
                               .build();
    JsonObjectBuilder builder = Json.createObjectBuilder();
    return builder.add("title", "Order 12345")
                  .add("class", Json.createArrayBuilder(Arrays.asList("order")))
                  .add("links", Json.createArrayBuilder()
                                    .add(buildSirenLink("self", "http://api.x.io/orders/42"))
                                    .add(buildSirenLink("previous", "http://api.x.io/orders/41"))
                                    .add(buildSirenLink("next", "http://api.x.io/orders/43")))
                  .add("entities", Json.createArrayBuilder().add(subEntity))
                  .add("actions", Json.createArrayBuilder().add(getAddItemAction()))
                  .build();
  }

  public static JsonObjectBuilder getAddItemAction() {
    return Json.createObjectBuilder()
               .add("name", "add-item")
               .add("title", "Add Item")
               .add("method", "POST")
               .add("href", "http://api.x.io/orders/42/items")
               .add("type", "application/x-www-form-urlencoded")
               .add("fields", Json.createArrayBuilder()
                                  .add(Json.createObjectBuilder().add("name", "orderNumber")
                                           .add("type", "hidden")
                                           .add("value", "42"))
                                  .add(Json.createObjectBuilder().add("name", "productCode")
                                           .add("type", "text"))
                                  .add(Json.createObjectBuilder().add("name", "quantity")
                                           .add("type", "number")));
  }

  public static JsonObjectBuilder buildSirenLink(String rel, String href) {
    return Json.createObjectBuilder()
               .add("rel", Json.createArrayBuilder().add(rel))
               .add("href", href);
  }
}
