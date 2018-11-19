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

package io.opencensus.contrib.http;

import static com.google.common.truth.Truth.assertThat;

import javax.annotation.Nullable;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

/** Unit tests for {@link AbstractHttpStats}. */
@RunWith(JUnit4.class)
public class AbstractHttpStatsTest {

  static class HttpStatsTest extends AbstractHttpStats<Object, Object> {
    HttpStatsTest(HttpExtractor<Object, Object> extractor) {
      super(extractor);
    }

    @Override
    public void requestStart(HttpStatsCtx statsCtx, Object request) {}

    @Override
    public void requestEnd(
        HttpStatsCtx statsCtx, Object request, Object response, @Nullable Throwable error) {}
  }

  @Rule public final ExpectedException thrown = ExpectedException.none();
  private final Object request = new Object();
  @Mock private HttpExtractor<Object, Object> extractor;
  @Spy private final HttpStatsCtx statsCtx = new HttpStatsCtx();

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void constructorWithExtractor() {
    HttpStatsTest stats = new HttpStatsTest(extractor);
    assertThat(stats).isNotNull();
  }

  @Test
  public void constructorDisallowNullExtractor() {
    thrown.expect(NullPointerException.class);
    new HttpStatsTest(null);
  }

  @Test
  public void testRecordStartTime() {
    HttpStatsTest stats = new HttpStatsTest(extractor);
    stats.recordStartTime(statsCtx, request);
    assertThat(statsCtx.getStartTime()).isGreaterThan(0L);
  }
}
