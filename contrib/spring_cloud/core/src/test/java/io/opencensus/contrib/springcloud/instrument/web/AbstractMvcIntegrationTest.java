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

package io.opencensus.contrib.springcloud.instrument.web;

import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

@WebAppConfiguration
public abstract class AbstractMvcIntegrationTest {

  @Autowired protected WebApplicationContext webApplicationContext;

  protected MockMvc mockMvc;

  @Before
  public void setup() {
    InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
    viewResolver.setPrefix("/WEB-INF/jsp/view/");
    viewResolver.setSuffix(".jsp");
    DefaultMockMvcBuilder mockMvcBuilder =
        MockMvcBuilders.webAppContextSetup(this.webApplicationContext);
    configureMockMvcBuilder(mockMvcBuilder);
    this.mockMvc = mockMvcBuilder.build();
  }

  /**
   * Override in a subclass to modify mockMvcBuilder configuration (e.g. add filter).
   *
   * <p>The method from super class should be called.
   */
  protected void configureMockMvcBuilder(DefaultMockMvcBuilder mockMvcBuilder) {}
}
