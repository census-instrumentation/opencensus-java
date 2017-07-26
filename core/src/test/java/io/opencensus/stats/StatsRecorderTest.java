/*
 * Copyright 2017, Google Inc.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opencensus.stats;

import java.io.IOException;
import java.io.OutputStream;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link StatsRecorder}. */
@RunWith(JUnit4.class)
public final class StatsRecorderTest {

  private final StatsContext statsContext =
      new StatsContext() {

        @Override
        public void serialize(OutputStream output) throws IOException {
        }

        @Override
        public StatsContext record(MeasureMap measurements) {
          return null;
        }

        @Override
        public Builder builder() {
          return null;
        }
      };

  @Test
  public void defaultRecord() {
    StatsRecorder.getNoopStatsRecorder().record(statsContext, MeasureMap.builder().build());
  }

  @Test(expected = NullPointerException.class)
  public void defaultRecord_DisallowNullStatsContext() {
    StatsRecorder.getNoopStatsRecorder().record(null, MeasureMap.builder().build());
  }

  @Test(expected = NullPointerException.class)
  public void defaultRecord_DisallowNullMeasureMap() {
    StatsRecorder.getNoopStatsRecorder().record(statsContext, null);
  }
}
