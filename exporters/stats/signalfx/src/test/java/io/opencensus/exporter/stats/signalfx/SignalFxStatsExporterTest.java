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

package io.opencensus.exporter.stats.signalfx;

import static org.junit.Assert.assertEquals;

import io.opencensus.common.Duration;
import java.net.URI;
import java.net.URISyntaxException;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link SignalFxStatsExporter}. */
@RunWith(JUnit4.class)
public class SignalFxStatsExporterTest {

  private static final String TEST_TOKEN = "token";
  private static final String TEST_ENDPOINT = "https://example.com";
  private static final Duration ONE_SECOND = Duration.create(1, 0);

  @Rule public final ExpectedException thrown = ExpectedException.none();

  @After
  public void tearDown() {
    SignalFxStatsExporter.unsafeResetExporter();
  }

  @Test
  public void createWithNullConfiguration() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("configuration");
    SignalFxStatsExporter.create(null);
  }

  @Test
  public void createWithNullHostUsesDefault() {
    SignalFxStatsExporter.create(SignalFxStatsConfiguration.builder().setToken(TEST_TOKEN).build());
    assertEquals(
        SignalFxStatsConfiguration.DEFAULT_SIGNALFX_ENDPOINT,
        SignalFxStatsExporter.unsafeGetConfig().getIngestEndpoint());
  }

  @Test
  public void createWithNullIntervalUsesDefault() {
    SignalFxStatsExporter.create(SignalFxStatsConfiguration.builder().setToken(TEST_TOKEN).build());
    assertEquals(
        SignalFxStatsConfiguration.DEFAULT_EXPORT_INTERVAL,
        SignalFxStatsExporter.unsafeGetConfig().getExportInterval());
  }

  @Test
  public void createExporterTwice() {
    SignalFxStatsConfiguration config =
        SignalFxStatsConfiguration.builder()
            .setToken(TEST_TOKEN)
            .setExportInterval(ONE_SECOND)
            .build();
    SignalFxStatsExporter.create(config);
    thrown.expect(IllegalStateException.class);
    thrown.expectMessage("SignalFx stats exporter is already created.");
    SignalFxStatsExporter.create(config);
  }

  @Test
  public void createWithConfiguration() throws URISyntaxException {
    SignalFxStatsConfiguration config =
        SignalFxStatsConfiguration.builder()
            .setToken(TEST_TOKEN)
            .setIngestEndpoint(new URI(TEST_ENDPOINT))
            .setExportInterval(ONE_SECOND)
            .build();
    SignalFxStatsExporter.create(config);
    assertEquals(config, SignalFxStatsExporter.unsafeGetConfig());
  }
}
