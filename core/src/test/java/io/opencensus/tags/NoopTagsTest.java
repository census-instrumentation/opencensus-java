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

package io.opencensus.tags;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.collect.Lists;
import io.opencensus.tags.Tag.TagString;
import io.opencensus.tags.TagKey.TagKeyString;
import io.opencensus.tags.TagValue.TagValueString;
import java.util.Arrays;
import java.util.Iterator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link NoopTags}. */
@RunWith(JUnit4.class)
public final class NoopTagsTest {
  private static final TagContext TAG_CONTEXT =
      new TagContext() {

        @Override
        public Iterator<Tag> unsafeGetIterator() {
          return Arrays.<Tag>asList(
                  TagString.create(TagKeyString.create("key"), TagValueString.create("value")))
              .iterator();
        }
      };

  @Test
  public void noopTagsComponent() {
    assertThat(NoopTags.getNoopTagsComponent().getTagger()).isSameAs(NoopTags.getNoopTagger());
    assertThat(NoopTags.getNoopTagsComponent().getTagPropagationComponent())
        .isSameAs(NoopTags.getNoopTagPropagationComponent());
  }

  @Test
  public void noopTagger() {
    assertThat(NoopTags.getNoopTagger().empty()).isSameAs(NoopTags.getNoopTagContext());
    assertThat(NoopTags.getNoopTagger().toBuilder(TAG_CONTEXT))
        .isSameAs(NoopTags.getNoopTagContextBuilder());
  }

  @Test
  public void noopTagContextBuilder() {
    assertThat(NoopTags.getNoopTagContextBuilder().build()).isSameAs(NoopTags.getNoopTagContext());
    assertThat(
            NoopTags.getNoopTagContextBuilder()
                .put(TagKeyString.create("key"), TagValueString.create("value"))
                .build())
        .isSameAs(NoopTags.getNoopTagContext());
  }

  @Test
  public void noopTagContext() {
    assertThat(Lists.newArrayList(NoopTags.getNoopTagContext().unsafeGetIterator())).isEmpty();
  }

  @Test
  public void noopTagPropagationComponent() {
    assertThat(NoopTags.getNoopTagPropagationComponent().getBinarySerializer())
        .isSameAs(NoopTags.getNoopTagContextBinarySerializer());
  }

  @Test
  public void noopTagContextBinarySerializer() throws TagContextParseException {
    assertThat(NoopTags.getNoopTagContextBinarySerializer().toByteArray(TAG_CONTEXT))
        .isEqualTo(new byte[0]);
    assertThat(NoopTags.getNoopTagContextBinarySerializer().fromByteArray(new byte[5]))
        .isEqualTo(NoopTags.getNoopTagContext());
  }
}
