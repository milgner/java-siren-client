package com.marcusilgner.siren;

import org.junit.jupiter.api.Test;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class SirenEntityTest {
  @Test
  public void testInstantiateFromString() {
    SirenEntity entity = new SirenEntity(getEntityJsonObject().toString());
    assertEquals("Order 12345", entity.getTitle().get());
  }

  @Test
  public void testGetClasses() {
    String[] expected = {"order"};
    SirenEntity entity = new SirenEntity(getEntityJsonObject());
    assertArrayEquals(expected, entity.getClasses().toArray());
  }

  @Test
  public void testGetLinkHrefByRel() {
    SirenEntity entity = new SirenEntity(getEntityJsonObject());

    Optional<SirenLink> selfLink = entity.getLinkByRel("self");
    assertEquals("http://api.x.io/orders/42", selfLink.get().getHref().get());

    Optional<SirenLink> nonexistingLink = entity.getLinkByRel("foobar");
    assertFalse(nonexistingLink.isPresent());
  }

  @Test
  public void testGetSubEntities() {
    SirenEntity entity = new SirenEntity(getEntityJsonObject());
    List<SirenSubEntity> subEntities = entity.getSubEntities();
    List<String> hrefs = subEntities.stream()
                                    .map(SirenSubEntity::getHref)
                                    .map(Optional::get)
                                    .collect(Collectors.toList());
    String[] expectedHrefs = {"http://api.x.io/orders/42/items"};
    assertArrayEquals(expectedHrefs, hrefs.toArray());
  }

  @Test
  public void testGetLinks() {
    SirenEntity entity = new SirenEntity(getEntityJsonObject());
    List<SirenLink> links = entity.getLinks();
    assertEquals(3, links.size());
    SirenLink lastLink = links.get(2);
    final String[] rels = {"next"};
    assertArrayEquals(rels, lastLink.getRelList().toArray());
    assertEquals("http://api.x.io/orders/43", lastLink.getHref().get());
  }

  @Test
  public void testGetActions() {
    SirenEntity entity = new SirenEntity(getEntityJsonObject());
    List<SirenAction> actions = entity.getActions();
    assertEquals(1, actions.size());
    SirenAction action = actions.get(0);
    assertEquals("add-item", action.getName().get());
    assertEquals("Add Item", action.getTitle().get());
    assertEquals("POST", action.getMethod());
    assertEquals("http://api.x.io/orders/42/items", action.getHref().get());
    assertEquals("application/x-www-form-urlencoded", action.getType());
    List<SirenAction.ActionField> fields = action.getFields();
    assertEquals(3, fields.size());
  }

  private JsonObjectBuilder buildSirenLink(String rel, String href) {
    return Json.createObjectBuilder()
               .add("rel", Json.createArrayBuilder().add(rel))
               .add("href", href);
  }

  private JsonObject getEntityJsonObject() {
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

  private JsonObjectBuilder getAddItemAction() {
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
}
