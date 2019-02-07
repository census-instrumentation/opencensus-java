/*
 * Copyright 2018, OpenCensus Authors
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

/** Tests for {@link ResourceUtils}. */
@RunWith(JUnit4.class)
public class ResourceUtilsTest {

  @Test
  public void testDetectResource() {
    Resource resource = ResourceUtils.detectResource();
    if (System.getenv("KUBERNETES_SERVICE_HOST") != null) {
      assertThat(resource.getType()).isEqualTo(K8sContainerResource.TYPE);
    } else if (GcpMetadataConfig.getInstanceId() != null) {
      assertThat(resource.getType()).isEqualTo(GcpGceInstanceResource.TYPE);
    } else if (AwsIdentityDocUtils.isRunningOnAwsEc2()) {
      assertThat(resource.getType()).isEqualTo(AwsEc2InstanceResource.TYPE);
    } else {
      assertThat(resource).isNotNull();
      assertThat(resource.getType()).isNull();
      assertThat(resource.getLabels()).isEmpty();
    }
  }
}
