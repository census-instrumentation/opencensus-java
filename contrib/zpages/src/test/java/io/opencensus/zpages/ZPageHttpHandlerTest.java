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

package io.opencensus.zpages;

import static com.google.common.truth.Truth.assertThat;

import java.net.URI;
import java.net.URISyntaxException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link ZPageHttpHandler}. */
@RunWith(JUnit4.class)
public class ZPageHttpHandlerTest {
  @Test
  public void parseUndefinedQuery() throws URISyntaxException {
    URI uri = new URI("http://localhost:8000/tracez");
    assertThat(ZPageHttpHandler.uriQueryToMap(uri)).isEmpty();
  }

  @Test
  public void parseQuery() throws URISyntaxException {
    URI uri = new URI("http://localhost:8000/tracez?ztype=1&zsubtype&zname=Test");
    assertThat(ZPageHttpHandler.uriQueryToMap(uri))
        .containsExactly("ztype", "1", "zsubtype", "", "zname", "Test");
  }
}
