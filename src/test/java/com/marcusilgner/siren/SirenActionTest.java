package com.marcusilgner.siren;

import okhttp3.Request;
import okio.BufferedSink;
import okio.Okio;
import org.junit.Test;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class SirenActionTest {
  @Test
  public void testConstructor() {
    SirenAction action = new SirenAction(Fixtures.getAddItemAction().build());
    assertNotNull(action);
  }

  @Test
  public void testCopyConstructor() {
    SirenAction action = new SirenAction(Fixtures.getAddItemAction().build());
    SirenAction copyAction = new SirenAction(action);
    assertNotNull(copyAction);
    assertEquals(action.json, copyAction.json);
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

  @Test
  public void testBuildRequestWithoutFields() throws IOException {
    SirenAction action = new SirenAction(Fixtures.getAddItemAction().build());
    Request request = action.buildRequest();
    assertNotNull(request);
    assertEquals("http://api.x.io/orders/42/items", request.url().toString());
  }

  @Test
  public void testBuildRequestWithFields() throws IOException {
    SirenAction action = new SirenAction(Fixtures.getAddItemAction().build());
    action.getField("productCode").get().setValue("MEANING");
    action.getField("quantity").get().setValue("42");
    Request request = action.buildRequest();
    assertNotNull(request);
    assertEquals("application/json", request.body().contentType().toString());
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    BufferedSink sink = Okio.buffer(Okio.sink(outputStream));
    request.body().writeTo(sink);
    sink.flush();
    JsonReader jsonReader = Json.createReader(new StringReader(outputStream.toString()));
    JsonObject json = jsonReader.readObject();
    jsonReader.close();
    assertEquals("MEANING", json.getString("productCode"));
    assertEquals("42", json.getString("quantity"));
  }
}
