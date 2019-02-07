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

/** Unit tests for {@link K8sContainerResource}. */
@RunWith(JUnit4.class)
public class K8sContainerResourceTest {
  private static final String K8S_CLUSTER_NAME = "cluster";
  private static final String K8S_NAMESPACE_NAME = "namespace";
  private static final String K8S_POD_NAME = "pod-id";
  private static final String K8S_CONTAINER_NAME = "container";

  @Test
  public void create_K8sContainerResourceTest() {
    Resource resource =
        K8sContainerResource.create(
            K8S_CLUSTER_NAME, K8S_NAMESPACE_NAME, K8S_POD_NAME, K8S_CONTAINER_NAME);
    assertThat(resource.getType()).isEqualTo(K8sContainerResource.TYPE);
    assertThat(resource.getLabels())
        .containsExactly(
            K8sContainerResource.CLUSTER_NAME_KEY,
            K8S_CLUSTER_NAME,
            K8sContainerResource.NAMESPACE_NAME_KEY,
            K8S_NAMESPACE_NAME,
            K8sContainerResource.POD_NAME_KEY,
            K8S_POD_NAME,
            K8sContainerResource.CONTAINER_NAME_KEY,
            K8S_CONTAINER_NAME);
  }
}
