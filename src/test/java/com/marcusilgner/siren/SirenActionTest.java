package com.marcusilgner.siren;

import org.junit.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class SirenActionTest {
  @Test
  public void testConstructor() {
    SirenAction action = new SirenAction(Fixtures.getAddItemAction().build());
    assertNotNull(action);
  }

  @Test
  public void testGetField() {
    SirenAction action = new SirenAction(Fixtures.getAddItemAction().build());
    Optional<SirenAction.ActionField> orderNumberField = action.getField("orderNumber");
    assertTrue(orderNumberField.isPresent());
    assertEquals("hidden", orderNumberField.get().getType());
    assertEquals("42", orderNumberField.get().getValue());
  }

  @Test
  public void testSetFieldValue() {
    SirenAction action = new SirenAction(Fixtures.getAddItemAction().build());
    SirenAction.ActionField orderNumberField = action.getField("orderNumber").get();
    orderNumberField.setValue("84");
    assertEquals("84", action.getField("orderNumber").get().getValue());
  }
}
