/*
 * Copyright 2016, Google Inc.
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

package com.google.instrumentation.stats;

import static com.google.common.truth.Truth.assertThat;

import java.util.Map.Entry;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for ViewConstantsProtoBinaryParser.
 */
@RunWith(JUnit4.class)
public final class ViewConstantsProtoBinaryParserTest {

  private static final ViewConstantsProtoBinaryParser parser = new ViewConstantsProtoBinaryParser(
      "../proto/stats/rpc_constants.pb");
  private static final int GRPC_MEASUREMENT_DESCRIPTOR_COUNT = 22;
  private static final int GRPC_VIEW_DESCRIPTOR_COUNT = 36;

  @Test(expected = AssertionError.class)
  public void testConstructorFileNotFound() {
    ViewConstantsProtoBinaryParser invalidParser = new ViewConstantsProtoBinaryParser("NOT_EXIST");
  }

  @Test
  public void testGetMeasurementDescriptorMap() {
    assertThat(parser.getMeasurementDescriptorMap()).hasSize(GRPC_MEASUREMENT_DESCRIPTOR_COUNT);
    for (Entry<String, MeasurementDescriptor> entry :
        parser.getMeasurementDescriptorMap().entrySet()) {
      assertThat(entry.getValue().getName()).isEqualTo(entry.getKey());
      assertThat(entry.getValue().getDescription()).isNotEmpty();
      assertThat(entry.getValue().getUnit()).isNotNull();
    }
  }

  @Test
  public void testGetViewDescriptorMap() {
    assertThat(parser.getViewDescriptorMap()).hasSize(GRPC_VIEW_DESCRIPTOR_COUNT);
    for (Entry<String, ViewDescriptor> entry : parser.getViewDescriptorMap().entrySet()) {
      assertThat(entry.getValue().getName()).isEqualTo(entry.getKey());
      assertThat(entry.getValue().getDescription()).isNotEmpty();
      assertThat(entry.getValue().getMeasurementDescriptor()).isNotNull();
      assertThat(entry.getValue().getTagKeys()).isNotEmpty();
    }
  }
}
