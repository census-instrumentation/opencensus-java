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

package io.opencensus.benchmarks.trace.propagation;

import io.opencensus.trace.SpanContext;
import io.opencensus.trace.propagation.SpanContextParseException;
import io.opencensus.trace.propagation.TextFormat;
import io.opencensus.trace.propagation.TextFormat.Getter;
import io.opencensus.trace.propagation.TextFormat.Setter;
import java.util.Map;
import javax.annotation.Nullable;

/** Generic benchmarks for {@link io.opencensus.trace.propagation.TextFormat}. */
final class TextFormatBenchmarkBase {
  private static final Setter<Map<String, String>> setter =
      new Setter<Map<String, String>>() {
        @Override
        public void put(Map<String, String> carrier, String key, String value) {
          carrier.put(key, value);
        }
      };

  private static final Getter<Map<String, String>> getter =
      new Getter<Map<String, String>>() {
        @Nullable
        @Override
        public String get(Map<String, String> carrier, String key) {
          return carrier.get(key);
        }
      };

  private final TextFormat textFormat;

  TextFormatBenchmarkBase(TextFormat textFormat) {
    this.textFormat = textFormat;
  }

  void inject(SpanContext spanContext, Map<String, String> carrier) {
    textFormat.inject(spanContext, carrier, setter);
  }

  SpanContext extract(Map<String, String> carrier) throws SpanContextParseException {
    return textFormat.extract(carrier, getter);
  }
}
