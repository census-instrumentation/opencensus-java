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

package io.opencensus.trace.base;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.testing.EqualsTester;
import io.opencensus.trace.BlankSpan;
import io.opencensus.trace.Span;
import io.opencensus.trace.samplers.Samplers;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link StartSpanOptions}. */
@RunWith(JUnit4.class)
public class StartSpanOptionsTest {
  private final List<Span> singleParentList = Arrays.<Span>asList(BlankSpan.INSTANCE);

  @Test
  public void defaultOptions() {
    StartSpanOptions defaultOptions = StartSpanOptions.builder().build();
    assertThat(defaultOptions.getSampler()).isNull();
    assertThat(defaultOptions.getParentLinks().isEmpty()).isTrue();
    assertThat(defaultOptions.getRecordEvents()).isNull();
  }

  @Test
  public void setSampler() {
    StartSpanOptions options =
        StartSpanOptions.builder().setSampler(Samplers.neverSample()).build();
    assertThat(options.getSampler()).isEqualTo(Samplers.neverSample());
    assertThat(options.getParentLinks().isEmpty()).isTrue();
    assertThat(options.getRecordEvents()).isNull();
  }

  @Test
  public void setParentLinks() {
    StartSpanOptions options = StartSpanOptions.builder().setParentLinks(singleParentList).build();
    assertThat(options.getSampler()).isNull();
    assertThat(options.getParentLinks()).isEqualTo(singleParentList);
    assertThat(options.getRecordEvents()).isNull();
  }

  @Test
  public void setParentLinks_EmptyList() {
    StartSpanOptions options =
        StartSpanOptions.builder().setParentLinks(new LinkedList<Span>()).build();
    assertThat(options.getSampler()).isNull();
    assertThat(options.getParentLinks().size()).isEqualTo(0);
    assertThat(options.getRecordEvents()).isNull();
  }

  @Test
  public void setParentLinks_MultipleParents() {
    StartSpanOptions options =
        StartSpanOptions.builder()
            .setParentLinks(Arrays.<Span>asList(BlankSpan.INSTANCE, BlankSpan.INSTANCE))
            .build();
    assertThat(options.getSampler()).isNull();
    assertThat(options.getParentLinks().size()).isEqualTo(2);
    assertThat(options.getRecordEvents()).isNull();
  }

  @Test
  public void setRecordEvents() {
    StartSpanOptions options = StartSpanOptions.builder().setRecordEvents(true).build();
    assertThat(options.getSampler()).isNull();
    assertThat(options.getParentLinks().isEmpty()).isTrue();
    assertThat(options.getRecordEvents()).isTrue();
  }

  @Test
  public void setAllProperties() {
    StartSpanOptions options =
        StartSpanOptions.builder()
            .setSampler(Samplers.alwaysSample())
            .setRecordEvents(true)
            .setParentLinks(singleParentList)
            .build();
    assertThat(options.getSampler()).isEqualTo(Samplers.alwaysSample());
    assertThat(options.getParentLinks()).isEqualTo(singleParentList);
    assertThat(options.getRecordEvents()).isTrue();
  }

  @Test
  public void startSpanOptions_EqualsAndHashCode() {
    EqualsTester tester = new EqualsTester();
    StartSpanOptions optionsWithAlwaysSampler1 =
        StartSpanOptions.builder().setSampler(Samplers.alwaysSample()).build();
    StartSpanOptions optionsWithAlwaysSampler2 =
        StartSpanOptions.builder().setSampler(Samplers.alwaysSample()).build();
    tester.addEqualityGroup(optionsWithAlwaysSampler1, optionsWithAlwaysSampler2);
    StartSpanOptions optionsWithNeverSampler =
        StartSpanOptions.builder().setSampler(Samplers.neverSample()).build();
    tester.addEqualityGroup(optionsWithNeverSampler);
    tester.addEqualityGroup(StartSpanOptions.DEFAULT);
    tester.testEquals();
  }
}
