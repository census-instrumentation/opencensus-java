/*
 * Copyright 2019, OpenCensus Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opencensus.examples.spring.servlet;

import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.AsyncRestTemplate;
import org.springframework.web.client.RestTemplate;

/* Controller for Web server. */
@RestController
public class HelloController {
  private static final Logger logger = Logger.getLogger(HelloController.class.getName());

  /**
   * Serves index page.
   *
   * @return String
   */
  @RequestMapping("/")
  public String index() {
    String str = "Hello from servlet instrumented with opencensus-spring";
    String resp = restTemplate.getForObject("http://localhost:8080/loopback", String.class);

    String asyncUrl = "http://localhost:8080/asyncloopback";
    ListenableFuture<ResponseEntity<String>> future1 =
        asyncRestTemplate.getForEntity(asyncUrl, String.class);
    ListenableFuture<ResponseEntity<String>> future2 =
        asyncRestTemplate.getForEntity(asyncUrl, String.class);
    ListenableFuture<ResponseEntity<String>> future3 =
        asyncRestTemplate.getForEntity(asyncUrl, String.class);

    String resp1 = null;
    String resp2 = null;
    String resp3 = null;
    try {
      resp1 = future1.get().toString();
      resp2 = future2.get().toString();
      resp3 = future3.get().toString();
    } catch (InterruptedException | ExecutionException e) {
      logger.log(Level.WARNING, "request failed", e);
    }
    return str + resp + "\n" + resp1 + "\n" + resp2 + "\n" + resp3;
  }

  /* Serves loopback endpoint. */
  @RequestMapping("/loopback")
  public String loopback() {
    return "Loopback. Hello from servlet!";
  }

  /* Serves asyncloopback endpoint. */
  @RequestMapping("/asyncloopback")
  public String asyncLoopback() {
    return "Async Loopback. Hello from servlet!";
  }

  @Autowired AsyncRestTemplate asyncRestTemplate;

  RestTemplate restTemplate = new RestTemplate(getClientHttpRequestFactory());

  private ClientHttpRequestFactory getClientHttpRequestFactory() {
    int timeout = 5000;
    HttpComponentsClientHttpRequestFactory clientHttpRequestFactory =
        new HttpComponentsClientHttpRequestFactory();
    clientHttpRequestFactory.setConnectTimeout(timeout);
    return clientHttpRequestFactory;
  }
}
