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

package io.opencensus.stats;

import static org.mockito.Matchers.same;
import static org.mockito.Mockito.verify;

import io.grpc.Context;
import io.opencensus.tags.Tag;
import io.opencensus.tags.Tag.TagString;
import io.opencensus.tags.TagContext;
import io.opencensus.tags.TagKey.TagKeyString;
import io.opencensus.tags.TagValue.TagValueString;
import io.opencensus.tags.unsafe.ContextUtils;
import java.util.Collections;
import java.util.Iterator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/** Tests for {@link StatsRecorder}. */
@RunWith(JUnit4.class)
public final class StatsRecorderTest {
  private static final TagString TAG =
      TagString.create(TagKeyString.create("key"), TagValueString.create("value"));

  private final TagContext tagContext =
      new TagContext() {

        @Override
        protected Iterator<Tag> getIterator() {
          return Collections.<Tag>singleton(TAG).iterator();
        }
      };

  @Mock private StatsRecorder statsRecorder;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void record_CurrentContextNotSet() {
    statsRecorder.builder();
    verify(statsRecorder).builder(same(ContextUtils.TAG_CONTEXT_KEY.get()));
  }

  @Test
  public void record_CurrentContextSet() {
    Context orig = Context.current().withValue(ContextUtils.TAG_CONTEXT_KEY, tagContext).attach();
    try {
      statsRecorder.builder();
      verify(statsRecorder).builder(same(tagContext));
    } finally {
      Context.current().detach(orig);
    }
  }
}
