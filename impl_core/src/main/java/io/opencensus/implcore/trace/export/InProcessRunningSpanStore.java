/*
 * Copyright 2019, OpenCensus Authors
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

package io.opencensus.implcore.trace.export;

import static com.google.common.base.Preconditions.checkArgument;

import io.opencensus.implcore.trace.RecordEventsSpanImpl;
import io.opencensus.implcore.trace.internal.ConcurrentIntrusiveList;
import io.opencensus.trace.export.RunningSpanStore;
import io.opencensus.trace.export.SpanData;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

/** In-process implementation of the {@link RunningSpanStore}. */
@ThreadSafe
public final class InProcessRunningSpanStore extends RunningSpanStore {
  private static final Summary EMPTY_SUMMARY =
      RunningSpanStore.Summary.create(Collections.<String, PerSpanNameSummary>emptyMap());

  @Nullable private volatile InProcessRunningSpanStoreImpl impl = null;

  static InProcessRunningSpanStore create() {
    return new InProcessRunningSpanStore();
  }

  /**
   * Adds the {@code Span} into the running spans list when the {@code Span} starts.
   *
   * @param span the {@code Span} that started.
   */
  public void onStart(RecordEventsSpanImpl span) {
    InProcessRunningSpanStoreImpl impl = this.impl;
    if (impl != null) {
      impl.onStart(span);
    }
  }

  /**
   * Removes the {@code Span} from the running spans list when the {@code Span} ends.
   *
   * @param span the {@code Span} that ended.
   * @return If span was not found, returns false.
   */
  public boolean onEnd(RecordEventsSpanImpl span) {
    InProcessRunningSpanStoreImpl impl = this.impl;
    if (impl != null) {
      return impl.onEnd(span);
    }
    return false;
  }

  /**
   * Returns {@code true} if the RunningSpanStore is enabled.
   *
   * @return {@code true} if the RunningSpanStore is enabled.
   */
  public boolean getEnabled() {
    return this.impl != null;
  }

  @Override
  public Summary getSummary() {
    InProcessRunningSpanStoreImpl impl = this.impl;
    if (impl != null) {
      return impl.getSummary();
    }
    return EMPTY_SUMMARY;
  }

  @Override
  public Collection<SpanData> getRunningSpans(Filter filter) {
    InProcessRunningSpanStoreImpl impl = this.impl;
    if (impl != null) {
      return impl.getRunningSpans(filter);
    }
    return Collections.emptyList();
  }

  @Override
  public void setMaxNumberOfSpans(int maxNumberOfSpans) {
    checkArgument(maxNumberOfSpans >= 0, "Invalid negative maxNumberOfElements");
    synchronized (this) {
      InProcessRunningSpanStoreImpl currentImpl = this.impl;
      if (currentImpl != null) {
        currentImpl.clear();
      }
      this.impl = null;
      if (maxNumberOfSpans > 0) {
        impl = new InProcessRunningSpanStoreImpl(maxNumberOfSpans);
      }
    }
  }

  private static final class InProcessRunningSpanStoreImpl {
    private final ConcurrentIntrusiveList<RecordEventsSpanImpl> runningSpans;

    private InProcessRunningSpanStoreImpl(int maxNumberOfElements) {
      runningSpans = new ConcurrentIntrusiveList<>(maxNumberOfElements);
    }

    private void onStart(RecordEventsSpanImpl span) {
      runningSpans.addElement(span);
    }

    private boolean onEnd(RecordEventsSpanImpl span) {
      // TODO: Count and display when try to remove span that was not present.
      return runningSpans.removeElement(span);
    }

    private Summary getSummary() {
      Collection<RecordEventsSpanImpl> allRunningSpans = runningSpans.getAll();
      Map<String, Integer> numSpansPerName = new HashMap<String, Integer>();
      for (RecordEventsSpanImpl span : allRunningSpans) {
        Integer prevValue = numSpansPerName.get(span.getName());
        numSpansPerName.put(span.getName(), prevValue != null ? prevValue + 1 : 1);
      }
      Map<String, PerSpanNameSummary> perSpanNameSummary =
          new HashMap<String, PerSpanNameSummary>();
      for (Map.Entry<String, Integer> it : numSpansPerName.entrySet()) {
        perSpanNameSummary.put(it.getKey(), PerSpanNameSummary.create(it.getValue()));
      }
      return Summary.create(perSpanNameSummary);
    }

    private Collection<SpanData> getRunningSpans(Filter filter) {
      Collection<RecordEventsSpanImpl> allRunningSpans = runningSpans.getAll();
      int maxSpansToReturn =
          filter.getMaxSpansToReturn() == 0 ? allRunningSpans.size() : filter.getMaxSpansToReturn();
      List<SpanData> ret = new ArrayList<SpanData>(maxSpansToReturn);
      for (RecordEventsSpanImpl span : allRunningSpans) {
        if (ret.size() == maxSpansToReturn) {
          break;
        }
        if (span.getName().equals(filter.getSpanName())) {
          ret.add(span.toSpanData());
        }
      }
      return ret;
    }

    private void clear() {
      runningSpans.clear();
    }
  }
}
