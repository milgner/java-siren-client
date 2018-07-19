package com.marcusilgner.siren;

import okhttp3.*;

public interface HttpRequestFactory {
  Request.Builder createRequestBuilder();
}
