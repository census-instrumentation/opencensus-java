/*
 * Copyright 2017, OpenCensus Authors
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

package io.opencensus.contrib.agent.instrumentation;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import io.opencensus.testing.export.TestHandler;
import io.opencensus.trace.Tracing;
import io.opencensus.trace.export.SpanData;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Integration tests for {@link UrlInstrumentation}.
 *
 * <p>The integration tests are executed in a separate JVM that has the OpenCensus agent enabled via
 * the {@code -javaagent} command line option.
 */
@RunWith(JUnit4.class)
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
public class UrlInstrumentationIT {

  private static final TestHandler testHandler = new TestHandler();

  @BeforeClass
  public static void beforeClass() {
    Tracing.getExportComponent().getSpanExporter().registerHandler("test", testHandler);
  }

  @AfterClass
  public static void afterClass() {
    Tracing.getExportComponent().getSpanExporter().unregisterHandler("test");
  }

  @Test(timeout = 60000)
  public void getContent() throws Exception {
    URL url = getClass().getResource("some_resource.txt").toURI().toURL();
    Object content = url.getContent();

    assertThat(content).isInstanceOf(InputStream.class);
    assertThat(CharStreams.toString(new InputStreamReader((InputStream) content, Charsets.UTF_8)))
        .isEqualTo("Some resource.");

    SpanData span = testHandler.waitForExport(1).get(0);
    assertThat(span.getName()).isEqualTo("java.net.URL#getContent");
    assertThat(span.getStatus().isOk()).isTrue();
  }

  @Test(timeout = 60000)
  public void getContent_fails() throws MalformedURLException {
    URL url = new URL("file:///nonexistent");

    try {
      url.getContent();
      fail();
    } catch (IOException e) {
      SpanData span = testHandler.waitForExport(1).get(0);
      assertThat(span.getName()).isEqualTo("java.net.URL#getContent");
      assertThat(span.getStatus().isOk()).isFalse();
    }
  }
}
