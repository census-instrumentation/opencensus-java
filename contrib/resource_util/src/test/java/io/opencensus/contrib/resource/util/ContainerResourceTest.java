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

package io.opencensus.contrib.resource.util;

import static com.google.common.truth.Truth.assertThat;

import io.opencensus.resource.Resource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link ContainerResource}. */
@RunWith(JUnit4.class)
public class ContainerResourceTest {
  private static final String NAME = "container";
  private static final String IMAGE_NAME = "image_name";
  private static final String IMAGE_TAG = "image_tag";

  @Test
  public void create_ContainerResourceTest() {
    Resource resource = ContainerResource.create(NAME, IMAGE_NAME, IMAGE_TAG);
    assertThat(resource.getType()).isEqualTo(ContainerResource.TYPE);
    assertThat(resource.getLabels())
        .containsExactly(
            ContainerResource.NAME_KEY,
            NAME,
            ContainerResource.IMAGE_NAME_KEY,
            IMAGE_NAME,
            ContainerResource.IMAGE_TAG_KEY,
            IMAGE_TAG);
  }
}
