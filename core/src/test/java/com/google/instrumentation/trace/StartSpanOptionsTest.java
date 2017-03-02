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

package com.google.instrumentation.trace;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.testing.EqualsTester;
import com.google.instrumentation.common.Timestamp;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link StartSpanOptions}. */
@RunWith(JUnit4.class)
public class StartSpanOptionsTest {
  private final Span singleParent = BlankSpan.INSTANCE;
  private final List<Span> parentList = Arrays.asList(BlankSpan.INSTANCE, BlankSpan.INSTANCE);

  @Test
  public void defaultOptions() {
    assertThat(StartSpanOptions.getDefault().getStartTime()).isNull();
    assertThat(StartSpanOptions.getDefault().getSampler()).isNull();
    assertThat(StartSpanOptions.getDefault().getParentLinks().isEmpty()).isTrue();
    assertThat(StartSpanOptions.getDefault().getRecordEvents()).isNull();
  }

  @Test
  public void setStartTime() {
    StartSpanOptions options =
        StartSpanOptions.builder().setStartTime(Timestamp.fromMillis(1234567L)).build();
    assertThat(options.getStartTime()).isEqualTo(Timestamp.fromMillis(1234567L));
    assertThat(options.getSampler()).isNull();
    assertThat(options.getParentLinks().isEmpty()).isTrue();
    assertThat(options.getRecordEvents()).isNull();
  }

  @Test
  public void setSampler() {
    StartSpanOptions options =
        StartSpanOptions.builder().setSampler(Samplers.neverSample()).build();
    assertThat(options.getStartTime()).isNull();
    assertThat(options.getSampler()).isEqualTo(Samplers.neverSample());
    assertThat(options.getParentLinks().isEmpty()).isTrue();
    assertThat(options.getRecordEvents()).isNull();
  }

  @Test
  public void addParentLink() {
    StartSpanOptions options = StartSpanOptions.builder().addParentLink(singleParent).build();
    assertThat(options.getStartTime()).isNull();
    assertThat(options.getSampler()).isNull();
    assertThat(options.getParentLinks().size()).isEqualTo(1);
    assertThat(options.getRecordEvents()).isNull();
  }

  @Test(expected = NullPointerException.class)
  public void addParentLink_Null() {
    StartSpanOptions.builder().addParentLink(null).build();
  }

  @Test
  public void addParentLinks() {
    StartSpanOptions options = StartSpanOptions.builder().addParentLinks(parentList).build();
    assertThat(options.getStartTime()).isNull();
    assertThat(options.getSampler()).isNull();
    assertThat(options.getParentLinks().size()).isEqualTo(2);
    assertThat(options.getRecordEvents()).isNull();
  }

  @Test(expected = NullPointerException.class)
  public void addParentLinks_Null() {
    StartSpanOptions.builder().addParentLinks(null).build();
  }

  @Test
  public void setRecordEvents() {
    StartSpanOptions options = StartSpanOptions.builder().setRecordEvents(true).build();
    assertThat(options.getStartTime()).isNull();
    assertThat(options.getSampler()).isNull();
    assertThat(options.getParentLinks().isEmpty()).isTrue();
    assertThat(options.getRecordEvents()).isTrue();
  }

  @Test
  public void setAllProperties() {
    StartSpanOptions options =
        StartSpanOptions.builder()
            .setStartTime(Timestamp.fromMillis(1234567L))
            .setSampler(Samplers.neverSample())
            .setSampler(Samplers.alwaysSample()) // second SetSampler should apply
            .addParentLink(singleParent)
            .setRecordEvents(true)
            .addParentLinks(parentList)
            .build();
    assertThat(options.getStartTime()).isEqualTo(Timestamp.fromMillis(1234567L));
    assertThat(options.getSampler()).isEqualTo(Samplers.alwaysSample());
    assertThat(options.getParentLinks().size()).isEqualTo(3);
    assertThat(options.getRecordEvents()).isTrue();
  }

  @Test
  public void startSpanOptions_ToString() {
    StartSpanOptions options =
        StartSpanOptions.builder()
            .setStartTime(Timestamp.fromMillis(1234567L))
            .setSampler(Samplers.neverSample())
            .addParentLink(singleParent)
            .setRecordEvents(true)
            .build();
    assertThat(options.toString()).contains(Timestamp.fromMillis(1234567L).toString());
    assertThat(options.toString()).contains(Samplers.neverSample().toString());
    assertThat(options.toString()).contains(singleParent.toString());
    assertThat(options.toString()).contains("recordEvents=true");
  }

  @Test
  public void startSpanOptions_EqualsAndHashCode() {
    EqualsTester tester = new EqualsTester();
    tester.addEqualityGroup(
        StartSpanOptions.builder().setStartTime(Timestamp.fromMillis(1234567L)).build(),
        StartSpanOptions.builder().setStartTime(Timestamp.fromMillis(1234567L)).build());
    tester.addEqualityGroup(
        StartSpanOptions.builder().setRecordEvents(true).build(),
        StartSpanOptions.builder().setRecordEvents(true).build());
    tester.addEqualityGroup(StartSpanOptions.getDefault(), StartSpanOptions.builder().build());
    tester.testEquals();
  }
}
