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

package io.opencensus.contrib.spring.autoconfig;

import io.opencensus.common.ExperimentalApi;
import io.opencensus.contrib.spring.instrument.web.client.TracingAsyncClientHttpRequestInterceptor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * {@link org.springframework.boot.autoconfigure.EnableAutoConfiguration Auto-configuration} enables
 * span information propagation for {@link org.springframework.web.client.AsyncRestTemplate}.
 *
 * @since 0.23.0
 */
@Configuration
@ComponentScan(basePackages = "io.opencensus")
@ConditionalOnProperty(value = "opencensus.spring.enabled", matchIfMissing = true)
@ConditionalOnClass(org.springframework.web.client.AsyncRestTemplate.class)
@EnableConfigurationProperties(OpenCensusProperties.class)
@AutoConfigureAfter(OpenCensusAutoConfiguration.class)
@ExperimentalApi
@SuppressWarnings("deprecation")
public class TraceWebAsyncClientAutoConfiguration {
  @Configuration
  @ConditionalOnBean(org.springframework.web.client.AsyncRestTemplate.class)
  static class AsyncRestTemplateCfg {

    @Bean
    public TracingAsyncClientHttpRequestInterceptor asyncTracingClientHttpRequestInterceptor() {
      return (TracingAsyncClientHttpRequestInterceptor)
          TracingAsyncClientHttpRequestInterceptor.create();
    }
  }

  @Configuration
  protected static class TraceInterceptorConfiguration {

    @Autowired(required = false)
    @SuppressWarnings("initialization.fields.uninitialized")
    private Collection<org.springframework.web.client.AsyncRestTemplate> restTemplates;

    @Autowired
    @SuppressWarnings("initialization.fields.uninitialized")
    private TracingAsyncClientHttpRequestInterceptor clientInterceptor;

    @PostConstruct
    public void init() {
      if (this.restTemplates != null) {
        for (org.springframework.web.client.AsyncRestTemplate restTemplate : this.restTemplates) {
          List<org.springframework.http.client.AsyncClientHttpRequestInterceptor> interceptors =
              new ArrayList<org.springframework.http.client.AsyncClientHttpRequestInterceptor>(
                  restTemplate.getInterceptors());
          interceptors.add(this.clientInterceptor);
          restTemplate.setInterceptors(interceptors);
        }
      }
    }
  }
}
