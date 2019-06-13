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

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.AsyncClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsAsyncClientHttpRequestFactory;
import org.springframework.web.client.AsyncRestTemplate;

@Configuration
public class ApplicationAutoConfiguration {

  /* Instance of AsyncRestTemplate. */
  @Bean
  public AsyncRestTemplate getAsyncRestTemplate(AsyncClientHttpRequestFactory factory) {
    return new AsyncRestTemplate(factory);
  }

  /**
   * Factory for AsyncClientHttpRequest.
   *
   * @return AsyncClientHttpRequestFactory
   */
  @Bean
  public AsyncClientHttpRequestFactory getAsyncClientHttpRequestFactory() {
    int timeout = 5000;
    HttpComponentsAsyncClientHttpRequestFactory asyncClientHttpRequestFactory =
        new HttpComponentsAsyncClientHttpRequestFactory();
    asyncClientHttpRequestFactory.setConnectTimeout(timeout);
    return asyncClientHttpRequestFactory;
  }
}
