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

package io.opencensus.contrib.monitoredresource.util;

import static com.google.common.truth.Truth.assertThat;

import io.opencensus.contrib.monitoredresource.util.MonitoredResource.AwsEc2InstanceMonitoredResource;
import io.opencensus.contrib.monitoredresource.util.MonitoredResource.GcpGceInstanceMonitoredResource;
import io.opencensus.contrib.monitoredresource.util.MonitoredResource.GcpGkeContainerMonitoredResource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link MonitoredResource}. */
@RunWith(JUnit4.class)
public class MonitoredResourceTest {

  private static final String AWS_ACCOUNT = "aws-account";
  private static final String AWS_INSTANCE = "instance";
  private static final String AWS_REGION = "us-west-2";
  private static final String GCP_PROJECT = "gcp-project";
  private static final String GCP_INSTANCE = "instance";
  private static final String GCP_ZONE = "projects/my-project/zone/us-east1";
  private static final String GCP_ZONE_SANITIZED = "us-east1";
  private static final String GCP_GKE_NAMESPACE = "namespace";
  private static final String GCP_GKE_POD_ID = "pod-id";
  private static final String GCP_GKE_CONTAINER_NAME = "container";
  private static final String GCP_GKE_CLUSTER_NAME = "cluster";

  @Test
  public void testAwsEc2InstanceMonitoredResource() {
    AwsEc2InstanceMonitoredResource resource =
        AwsEc2InstanceMonitoredResource.create(AWS_ACCOUNT, AWS_INSTANCE, AWS_REGION);
    assertThat(resource.getResourceType()).isEqualTo(ResourceType.AWS_EC2_INSTANCE);
    assertThat(resource.getAccount()).isEqualTo(AWS_ACCOUNT);
    assertThat(resource.getInstanceId()).isEqualTo(AWS_INSTANCE);
    assertThat(resource.getRegion()).isEqualTo(AWS_REGION);
  }

  @Test
  public void testGcpGceInstanceMonitoredResource() {
    GcpGceInstanceMonitoredResource resource =
        GcpGceInstanceMonitoredResource.create(GCP_PROJECT, GCP_INSTANCE, GCP_ZONE_SANITIZED);
    assertThat(resource.getResourceType()).isEqualTo(ResourceType.GCP_GCE_INSTANCE);
    assertThat(resource.getAccount()).isEqualTo(GCP_PROJECT);
    assertThat(resource.getInstanceId()).isEqualTo(GCP_INSTANCE);
    assertThat(resource.getZone()).isEqualTo(GCP_ZONE_SANITIZED);
  }

  @Test
  public void testGcpGkeContainerMonitoredResource() {
    GcpGkeContainerMonitoredResource resource =
        GcpGkeContainerMonitoredResource.create(
            GCP_PROJECT,
            GCP_GKE_CLUSTER_NAME,
            GCP_GKE_CONTAINER_NAME,
            GCP_GKE_NAMESPACE,
            GCP_INSTANCE,
            GCP_GKE_POD_ID,
            GCP_ZONE_SANITIZED);
    assertThat(resource.getResourceType()).isEqualTo(ResourceType.GCP_GKE_CONTAINER);
    assertThat(resource.getAccount()).isEqualTo(GCP_PROJECT);
    assertThat(resource.getClusterName()).isEqualTo(GCP_GKE_CLUSTER_NAME);
    assertThat(resource.getContainerName()).isEqualTo(GCP_GKE_CONTAINER_NAME);
    assertThat(resource.getNamespaceId()).isEqualTo(GCP_GKE_NAMESPACE);
    assertThat(resource.getInstanceId()).isEqualTo(GCP_INSTANCE);
    assertThat(resource.getPodId()).isEqualTo(GCP_GKE_POD_ID);
    assertThat(resource.getZone()).isEqualTo(GCP_ZONE_SANITIZED);
  }

  @Test
  public void sanitizeLocationLabel() {
    assertThat(MonitoredResource.sanitizeLocationLabel(GCP_ZONE)).isEqualTo(GCP_ZONE_SANITIZED);
  }
}
