package com.marcusilgner.siren;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;
import okio.ByteString;

import javax.json.JsonValue;
import java.io.IOException;

class JsonRequestBody extends RequestBody {
  JsonValue json;

  JsonRequestBody(JsonValue value) {
    this.json = value;
  }

  @Override
  public MediaType contentType() {
    return MediaType.parse("application/json");
  }

  @Override
  public void writeTo(BufferedSink sink) throws IOException {
    sink.write(ByteString.encodeUtf8(json.toString()));
  }
}