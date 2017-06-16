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

package io.opencensus.trace.export;

import io.opencensus.trace.SpanImpl;
import io.opencensus.trace.internal.ConcurrentIntrusiveList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.concurrent.ThreadSafe;

/** Implementation of the {@link ActiveSpansExporter}. */
@ThreadSafe
public final class ActiveSpansExporterImpl extends ActiveSpansExporter {
  private final ConcurrentIntrusiveList<SpanImpl> activeSpans;

  public ActiveSpansExporterImpl() {
    activeSpans = new ConcurrentIntrusiveList<SpanImpl>();
  }

  /**
   * Adds the {@code Span} into the active spans list when the {@code Span} starts.
   *
   * @param span the {@code Span} that started.
   */
  public void onStart(SpanImpl span) {
    activeSpans.addElement(span);
  }

  /**
   * Removes the {@code Span} from the active spans list when the {@code Span} ends.
   *
   * @param span the {@code Span} that ended.
   */
  public void onEnd(SpanImpl span) {
    activeSpans.removeElement(span);
  }

  @Override
  public Summary getSummary() {
    Collection<SpanImpl> allActiveSpans = activeSpans.getAll();
    Map<String, Integer> numSpansPerName = new HashMap<String, Integer>();
    for (SpanImpl span : allActiveSpans) {
      Integer prevValue = numSpansPerName.get(span.getName());
      numSpansPerName.put(span.getName(), prevValue != null ? prevValue + 1 : 1);
    }
    Map<String, PerSpanNameSummary> perSpanNameSummary = new HashMap<String, PerSpanNameSummary>();
    for (Map.Entry<String, Integer> it : numSpansPerName.entrySet()) {
      perSpanNameSummary.put(it.getKey(), PerSpanNameSummary.create(it.getValue()));
    }
    Summary summary = Summary.create(perSpanNameSummary);
    return summary;
  }

  @Override
  public Collection<SpanData> getActiveSpans(Filter filter) {
    Collection<SpanImpl> allActiveSpans = activeSpans.getAll();
    int maxSpansToReturn =
        filter.getMaxSpansToReturn() == 0 ? allActiveSpans.size() : filter.getMaxSpansToReturn();
    List<SpanData> ret = new ArrayList<SpanData>(maxSpansToReturn);
    for (SpanImpl span : allActiveSpans) {
      if (ret.size() == maxSpansToReturn) {
        break;
      }
      if (span.getName().equals(filter.getSpanName())) {
        ret.add(span.toSpanData());
      }
    }
    return ret;
  }
}
