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

import com.google.auto.value.AutoValue;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * {@link MonitoredResource} represents an auto-detected monitored resource used by application for
 * exporting stats. It has a {@code ResourceType} associated with a mapping from resource labels to
 * values.
 *
 * @since 0.13
 */
@Immutable
public abstract class MonitoredResource {

  MonitoredResource() {}

  /**
   * Returns the {@link ResourceType} of this {@link MonitoredResource}.
   *
   * @return the {@code ResourceType}.
   * @since 0.13
   */
  public abstract ResourceType getResourceType();

  /**
   * Returns the map of {@link LabelKey}s to {@code LabelValue}s, associated with this {@code
   * MonitoredResource}.
   *
   * @return {@code Label}s.
   * @since 0.13
   */
  public abstract Map<LabelKey, String> getLabels();

  /*
   * Returns the first of two given parameters that is not null, if either is, or otherwise
   * throws a NullPointerException.
   */
  private static <T> T firstNonNull(@Nullable T first, @Nullable T second) {
    if (first != null) {
      return first;
    }
    if (second != null) {
      return second;
    }
    throw new NullPointerException("Both parameters are null");
  }

  @Immutable
  @AutoValue
  abstract static class AwsEc2MonitoredResource extends MonitoredResource {

    private static final String AWS_ACCOUNT =
        firstNonNull(AwsIdentityDocUtils.getValueFromAwsIdentityDocument("accountId"), "");
    private static final String AWS_INSTANCE_ID =
        firstNonNull(AwsIdentityDocUtils.getValueFromAwsIdentityDocument("instanceId"), "");
    private static final String AWS_REGION =
        firstNonNull(AwsIdentityDocUtils.getValueFromAwsIdentityDocument("region"), "");

    private static final Map<LabelKey, String> AWS_EC2_LABELS;

    static {
      Map<LabelKey, String> awsEc2Labels = new HashMap<LabelKey, String>();
      awsEc2Labels.put(LabelKey.AwsAccount, AWS_ACCOUNT);
      awsEc2Labels.put(LabelKey.AwsInstanceId, AWS_INSTANCE_ID);
      awsEc2Labels.put(LabelKey.AwsRegion, AWS_REGION);
      AWS_EC2_LABELS = Collections.unmodifiableMap(awsEc2Labels);
    }

    @Override
    public ResourceType getResourceType() {
      return ResourceType.AwsEc2Instance;
    }

    @Override
    public Map<LabelKey, String> getLabels() {
      return AWS_EC2_LABELS;
    }

    static AwsEc2MonitoredResource create() {
      return new AutoValue_MonitoredResource_AwsEc2MonitoredResource();
    }
  }

  @Immutable
  @AutoValue
  abstract static class GcpGceInstanceMonitoredResource extends MonitoredResource {

    private static final String GCP_INSTANCE_ID =
        firstNonNull(GcpMetadataConfig.getInstanceId(), "");
    private static final String GCP_ZONE = firstNonNull(GcpMetadataConfig.getZone(), "");

    private static final Map<LabelKey, String> GCP_GCE_LABELS;

    static {
      Map<LabelKey, String> gcpGceLabels = new HashMap<LabelKey, String>();
      gcpGceLabels.put(LabelKey.GcpInstanceId, GCP_INSTANCE_ID);
      gcpGceLabels.put(LabelKey.GcpZone, GCP_ZONE);
      GCP_GCE_LABELS = Collections.unmodifiableMap(gcpGceLabels);
    }

    @Override
    public ResourceType getResourceType() {
      return ResourceType.GceInstance;
    }

    @Override
    public Map<LabelKey, String> getLabels() {
      return GCP_GCE_LABELS;
    }

    static GcpGceInstanceMonitoredResource create() {
      return new AutoValue_MonitoredResource_GcpGceInstanceMonitoredResource();
    }
  }

  @Immutable
  @AutoValue
  abstract static class GcpGkeContainerMonitoredResource extends MonitoredResource {

    private static final String GCP_CLUSTER_NAME =
        firstNonNull(GcpMetadataConfig.getClusterName(), "");
    private static final String GCP_CONTAINER_NAME =
        firstNonNull(System.getenv("CONTAINER_NAME"), "");
    private static final String GCP_NAMESPACE_ID = firstNonNull(System.getenv("NAMESPACE"), "");
    private static final String GCP_INSTANCE_ID =
        firstNonNull(GcpMetadataConfig.getInstanceId(), "");
    private static final String GCP_POD_ID = firstNonNull(System.getenv("HOSTNAME"), "");
    private static final String GCP_ZONE = firstNonNull(GcpMetadataConfig.getZone(), "");

    private static final Map<LabelKey, String> GCP_GKE_LABELS;

    static {
      Map<LabelKey, String> gcpGkeLabels = new HashMap<LabelKey, String>();
      gcpGkeLabels.put(LabelKey.GcpClusterName, GCP_CLUSTER_NAME);
      gcpGkeLabels.put(LabelKey.GcpContainerName, GCP_CONTAINER_NAME);
      gcpGkeLabels.put(LabelKey.GcpNamespaceId, GCP_NAMESPACE_ID);
      gcpGkeLabels.put(LabelKey.GcpInstanceId, GCP_INSTANCE_ID);
      gcpGkeLabels.put(LabelKey.GcpGkePodId, GCP_POD_ID);
      gcpGkeLabels.put(LabelKey.GcpZone, GCP_ZONE);
      GCP_GKE_LABELS = Collections.unmodifiableMap(gcpGkeLabels);
    }

    @Override
    public ResourceType getResourceType() {
      return ResourceType.GkeContainer;
    }

    @Override
    public Map<LabelKey, String> getLabels() {
      return GCP_GKE_LABELS;
    }

    static GcpGkeContainerMonitoredResource create() {
      return new AutoValue_MonitoredResource_GcpGkeContainerMonitoredResource();
    }
  }
}
