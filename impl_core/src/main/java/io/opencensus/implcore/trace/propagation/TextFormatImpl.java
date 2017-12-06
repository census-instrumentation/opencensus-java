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

package io.opencensus.implcore.trace.propagation;

import io.opencensus.trace.SpanContext;
import io.opencensus.trace.propagation.SpanContextParseException;
import io.opencensus.trace.propagation.TextFormat;
import java.util.List;

public class TextFormatImpl extends TextFormat {
  private final B3Format b3Format = new B3Format();

  @Override
  public List<String> fields() {
    return null;
  }

  @Override
  public <C> void inject(SpanContext spanContext, C carrier, Setter<C> setter) {
    // TODO(bdrutu): Decide how to implement this.
  }

  @Override
  public <C> SpanContext extract(C carrier, Getter<C> getter) throws SpanContextParseException {
    return b3Format.extract(carrier, getter);
  }
}
