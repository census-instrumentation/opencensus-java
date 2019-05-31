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

package io.opencensus.contrib.spring.instrument.web;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.opencensus.contrib.http.servlet.OcHttpServletFilter;
import io.opencensus.contrib.spring.autoconfig.OpenCensusAutoConfiguration;
import io.opencensus.contrib.spring.autoconfig.OpenCensusProperties.Trace;
import io.opencensus.trace.Tracing;
import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(OpenCensusAutoConfiguration.TRACE_FILTER_ORDER)
@SuppressFBWarnings("RI_REDUNDANT_INTERFACES")
public class HttpServletFilter extends OcHttpServletFilter implements Filter {

  @Value("${opencensus.spring.trace.propagation:" + Trace.TRACE_PROPAGATION_TRACE_CONTEXT + "}")
  private String propagation;

  @Value("${opencensus.spring.trace.publicEndpoint:false}")
  private Boolean publicEndpoint;

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    ServletContext context = filterConfig.getServletContext();
    if (propagation.equals(Trace.TRACE_PROPAGATION_B3)) {
      context.setAttribute(OC_TRACE_PROPAGATOR, Tracing.getPropagationComponent().getB3Format());
    }
    if (publicEndpoint) {
      context.setInitParameter(OC_PUBLIC_ENDPOINT, publicEndpoint.toString());
    }
    super.init(filterConfig);
  }
}
