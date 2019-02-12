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
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * {@link org.springframework.boot.autoconfigure.EnableAutoConfiguration Auto-configuration} to
 * enable tracing using OpenCensus.
 *
 * @since 0.19.0
 */
@Configuration
@ComponentScan(basePackages = "io.opencensus")
@ConditionalOnProperty(value = "spring.opencensus.enabled", matchIfMissing = true)
@EnableConfigurationProperties(OpenCensusProperties.class)
@ExperimentalApi
public class OpenCensusAutoConfiguration {

  /**
   * TRACE_FILTER_ORDER determines the order in which {@link
   * io.opencensus.contrib.spring.instrument.web.HttpServletFilter} is invoked. In order to capture
   * accurate request processing latency it is desirable that the filter is invoked as early as
   * possible. However, there may be some security related filters that my need to execute before,
   * hence +5 is added.
   */
  public static final int TRACE_FILTER_ORDER = Ordered.HIGHEST_PRECEDENCE + 5;
}
