/*
 * Copyright 2018, OpenCensus Authors
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

package io.opencensus.contrib.spring.sleuth;

import io.opencensus.common.ExperimentalApi;
import java.util.Random;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.sleuth.Sampler;
import org.springframework.cloud.sleuth.SpanNamer;
import org.springframework.cloud.sleuth.SpanReporter;
import org.springframework.cloud.sleuth.TraceKeys;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.cloud.sleuth.autoconfig.TraceAutoConfiguration;
import org.springframework.cloud.sleuth.log.SpanLogger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Role;

/**
 * {@link org.springframework.boot.autoconfigure.EnableAutoConfiguration Auto-configuration} that
 * allows inter-operation between Sleuth(Brave) and OpenCensus.
 */
@ExperimentalApi
@Configuration
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
@ConditionalOnProperty(name = "spring.opencensus.sleuth.enabled", matchIfMissing = true)
@AutoConfigureBefore(TraceAutoConfiguration.class)
@EnableConfigurationProperties(OpenCensusSleuthProperties.class)
public class OpenCensusSleuthAutoConfiguration {

  @Bean
  @Primary
  Tracer openCensusSleuthTracer(
      Sampler sampler,
      Random random,
      SpanNamer spanNamer,
      SpanLogger spanLogger,
      SpanReporter spanReporter,
      TraceKeys traceKeys) {
    return new OpenCensusSleuthTracer(
        sampler, random, spanNamer, spanLogger, spanReporter, traceKeys, /* traceId128= */ true);
  }
}
