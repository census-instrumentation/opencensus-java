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

package io.opencensus.trace.propagation;

import io.opencensus.trace.SpanContext;
import io.opencensus.trace.propagation.TextFormat.Setter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Unit tests for {@link TextFormat}.
 */
@RunWith(JUnit4.class)
public class TextFormatTest {

  private static final TextFormat textFormat = TextFormat.getNoopTextFormat();
  private static final SpanContext spanContext = SpanContext.INVALID;

  @Test
  public void howThisWorks() throws Exception {
    HttpURLConnection connection = (HttpURLConnection) new URL("http://myserver").openConnection();

    // same as the method reference:
    //    textFormat.putContext(spanContext, connection, URLConnection::setRequestProperty);
    textFormat.putContext(spanContext, connection, new Setter<HttpURLConnection>() {
      @Override
      public void put(HttpURLConnection carrier, String field, String value) {
        carrier.setRequestProperty(field, value);
      }
    });
  }
}
