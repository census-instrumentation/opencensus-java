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

package io.opencensus.trace;

import io.opencensus.internal.Utils;
import java.util.EnumSet;
import java.util.Map;
import javax.annotation.Nullable;

/**
 * Class to be used in tests where an implementation for the Span is needed.
 *
 * <p>Not final to allow Mockito to "spy" this class.
 */
public class NoopSpan extends Span {

  /** Creates a new {@code NoopSpan}. */
  public NoopSpan(SpanContext context, @Nullable EnumSet<Options> options) {
    super(Utils.checkNotNull(context, "context"), options);
  }

  @Override
  public void putAttributes(Map<String, AttributeValue> attributes) {
    Utils.checkNotNull(attributes, "attributes");
  }

  @Override
  public void addAnnotation(String description, Map<String, AttributeValue> attributes) {
    Utils.checkNotNull(description, "description");
    Utils.checkNotNull(attributes, "attributes");
  }

  @Override
  public void addAnnotation(Annotation annotation) {
    Utils.checkNotNull(annotation, "annotation");
  }

  @Override
  public void addNetworkEvent(NetworkEvent networkEvent) {}

  @Override
  public void addMessageEvent(MessageEvent messageEvent) {
    Utils.checkNotNull(messageEvent, "messageEvent");
  }

  @Override
  public void addLink(Link link) {
    Utils.checkNotNull(link, "link");
  }

  @Override
  public void end(EndSpanOptions options) {
    Utils.checkNotNull(options, "options");
  }
}
