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
import com.google.cloud.MetadataConfig;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import java.util.List;
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
   * Returns the list of {@link LabelKey}s associated with this {@code MonitoredResource}.
   *
   * @return {@code LabelKey}s.
   * @since 0.13
   */
  public abstract List<LabelKey> getLabelKeys();

  /**
   * Returns the list of {@code LabelValue}s associated with this {@code MonitoredResource}. The
   * order of {@code LabelValue}s is the same as {@code LabelKey}s.
   *
   * @return {@code LabelValue}s.
   * @since 0.13
   */
  public abstract List<String> getLabelValues();

  @Immutable
  @AutoValue
  abstract static class AwsEc2MonitoredResource extends MonitoredResource {

    private static final ImmutableList<LabelKey> AWS_EC2_LABEL_KEYS =
        ImmutableList.of(LabelKey.AwsAccount, LabelKey.AwsInstanceId, LabelKey.AwsRegion);

    private static final String AWS_ACCOUNT =
        MoreObjects.firstNonNull(
            AwsIdentityDocUtils.getValueFromAwsIdentityDocument("accountId"), "");
    private static final String AWS_INSTANCE_ID =
        MoreObjects.firstNonNull(
            AwsIdentityDocUtils.getValueFromAwsIdentityDocument("instanceId"), "");
    private static final String AWS_REGION =
        MoreObjects.firstNonNull(AwsIdentityDocUtils.getValueFromAwsIdentityDocument("region"), "");

    private static final ImmutableList<String> AWS_EC2_LABEL_VALUES =
        ImmutableList.of(AWS_ACCOUNT, AWS_INSTANCE_ID, AWS_REGION);

    @Override
    public ResourceType getResourceType() {
      return ResourceType.AwsEc2Instance;
    }

    @Override
    public List<LabelKey> getLabelKeys() {
      return AWS_EC2_LABEL_KEYS;
    }

    @Override
    public List<String> getLabelValues() {
      return AWS_EC2_LABEL_VALUES;
    }

    static AwsEc2MonitoredResource create() {
      return new AutoValue_MonitoredResource_AwsEc2MonitoredResource();
    }
  }

  @Immutable
  @AutoValue
  abstract static class GcpGceInstanceMonitoredResource extends MonitoredResource {

    private static final ImmutableList<LabelKey> GCP_GCE_LABEL_KEYS =
        ImmutableList.of(LabelKey.GcpInstanceId, LabelKey.GcpZone);

    private static final String GCP_INSTANCE_ID =
        MoreObjects.firstNonNull(MetadataConfig.getInstanceId(), "");
    private static final String GCP_ZONE = MoreObjects.firstNonNull(MetadataConfig.getZone(), "");

    private static final ImmutableList<String> GCP_GCE_LABEL_VALUES =
        ImmutableList.of(GCP_INSTANCE_ID, GCP_ZONE);

    @Override
    public ResourceType getResourceType() {
      return ResourceType.GceInstance;
    }

    @Override
    public List<LabelKey> getLabelKeys() {
      return GCP_GCE_LABEL_KEYS;
    }

    @Override
    public List<String> getLabelValues() {
      return GCP_GCE_LABEL_VALUES;
    }

    static GcpGceInstanceMonitoredResource create() {
      return new AutoValue_MonitoredResource_GcpGceInstanceMonitoredResource();
    }
  }

  @Immutable
  @AutoValue
  abstract static class GcpGkeContainerMonitoredResource extends MonitoredResource {

    private static final ImmutableList<LabelKey> GCP_GKE_LABEL_KEYS =
        ImmutableList.of(
            LabelKey.GcpClusterName,
            LabelKey.GcpContainerName,
            LabelKey.GcpNamespaceId,
            LabelKey.GcpInstanceId,
            LabelKey.GcpGkePodId,
            LabelKey.GcpZone);

    private static final String GCP_CLUSTER_NAME =
        MoreObjects.firstNonNull(MetadataConfig.getClusterName(), "");
    private static final String GCP_CONTAINER_NAME =
        MoreObjects.firstNonNull(System.getenv("CONTAINER_NAME"), "");
    private static final String GCP_NAMESPACE_ID =
        MoreObjects.firstNonNull(System.getenv("NAMESPACE"), "");
    private static final String GCP_INSTANCE_ID =
        MoreObjects.firstNonNull(MetadataConfig.getInstanceId(), "");
    private static final String GCP_POD_ID =
        MoreObjects.firstNonNull(System.getenv("HOSTNAME"), "");
    private static final String GCP_ZONE = MoreObjects.firstNonNull(MetadataConfig.getZone(), "");

    private static final ImmutableList<String> GCP_GKE_LABEL_VALUES =
        ImmutableList.of(
            GCP_CLUSTER_NAME,
            GCP_CONTAINER_NAME,
            GCP_NAMESPACE_ID,
            GCP_INSTANCE_ID,
            GCP_POD_ID,
            GCP_ZONE);

    @Override
    public ResourceType getResourceType() {
      return ResourceType.GkeContainer;
    }

    @Override
    public List<LabelKey> getLabelKeys() {
      return GCP_GKE_LABEL_KEYS;
    }

    @Override
    public List<String> getLabelValues() {
      return GCP_GKE_LABEL_VALUES;
    }

    static GcpGkeContainerMonitoredResource create() {
      return new AutoValue_MonitoredResource_GcpGkeContainerMonitoredResource();
    }
  }

  @Immutable
  @AutoValue
  abstract static class GlobalMonitoredResource extends MonitoredResource {
    @Override
    public ResourceType getResourceType() {
      return ResourceType.Global;
    }

    @Override
    public List<LabelKey> getLabelKeys() {
      return ImmutableList.of();
    }

    @Override
    public List<String> getLabelValues() {
      return ImmutableList.of();
    }

    static GlobalMonitoredResource create() {
      return new AutoValue_MonitoredResource_GlobalMonitoredResource();
    }
  }
}
