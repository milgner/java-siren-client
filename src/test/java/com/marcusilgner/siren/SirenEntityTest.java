package com.marcusilgner.siren;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.marcusilgner.siren.Fixtures.getEntityJsonObject;
import static org.junit.jupiter.api.Assertions.*;

public class SirenEntityTest {
  @Test
  public void testInstantiateFromString() {
    SirenEntity entity = new SirenEntity(Fixtures.getEntityJsonObject().toString());
    assertEquals("Order 12345", entity.getTitle().get());
  }

  @Test
  public void testGetClasses() {
    String[] expected = {"order"};
    SirenEntity entity = new SirenEntity(Fixtures.getEntityJsonObject());
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
}
