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

/** Unit tests for {@link HostResource}. */
@RunWith(JUnit4.class)
public class HostResourceTest {
  private static final String HOSTNAME = "hostname";
  private static final String NAME = "name";
  private static final String ID = "id";
  private static final String TYPE = "type";

  @Test
  public void create_ContainerResourceTest() {
    Resource resource = HostResource.create(HOSTNAME, NAME, ID, TYPE);
    assertThat(resource.getType()).isEqualTo(HostResource.TYPE);
    assertThat(resource.getLabels())
        .containsExactly(
            HostResource.HOSTNAME_KEY,
            HOSTNAME,
            HostResource.NAME_KEY,
            NAME,
            HostResource.ID_KEY,
            ID,
            HostResource.TYPE_KEY,
            TYPE);
  }
}
