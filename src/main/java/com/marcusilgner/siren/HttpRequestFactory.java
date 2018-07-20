package com.marcusilgner.siren;

import okhttp3.Request;

public interface HttpRequestFactory {
  Request.Builder createRequestBuilder();
}
