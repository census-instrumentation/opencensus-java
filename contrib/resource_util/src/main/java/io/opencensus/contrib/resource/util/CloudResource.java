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

import static com.google.common.base.Preconditions.checkNotNull;
import static io.opencensus.contrib.resource.util.ResourceUtils.EMPTY_RESOURCE;

import io.opencensus.resource.Resource;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Helper class for Cloud {@code Resource} environment.
 *
 * @since 0.20
 */
public final class CloudResource {
  /**
   * The type of this {@code Resource}.
   *
   * @since 0.20
   */
  public static final String TYPE = "cloud";

  /**
   * Key for the name of the cloud provider. Example values are aws, azure, gcp.
   *
   * @since 0.20
   */
  public static final String PROVIDER_KEY = "cloud.provider";

  /**
   * The value of the provider when running in AWS.
   *
   * @since 0.20
   */
  public static final String PROVIDER_AWS = "aws";

  /**
   * The value of the provider when running in AZURE.
   *
   * @since 0.20
   */
  public static final String PROVIDER_AZURE = "azure";

  /**
   * The value of the provider when running in GCP.
   *
   * @since 0.20
   */
  public static final String PROVIDER_GCP = "gcp";

  /**
   * Key for the cloud account id used to identify different entities.
   *
   * @since 0.20
   */
  public static final String ACCOUNT_ID_KEY = "cloud.account.id";

  /**
   * Key for the region in which entities are running.
   *
   * @since 0.20
   */
  public static final String REGION_KEY = "cloud.region";

  /**
   * Key for the zone in which entities are running.
   *
   * @since 0.20
   */
  public static final String ZONE_KEY = "cloud.zone";

  /**
   * Returns a {@link Resource} that describes a cloud environment.
   *
   * @param provider the name of the cloud provider.
   * @param accountId the cloud account id used to identify different entities.
   * @param region the region in which entities are running.
   * @param zone the zone in which entities are running.
   * @return a {@link Resource} that describes a aws ec2 instance.
   * @since 0.20
   */
  public static Resource create(String provider, String accountId, String region, String zone) {
    Map<String, String> labels = new LinkedHashMap<String, String>();
    labels.put(PROVIDER_KEY, checkNotNull(provider, "provider"));
    labels.put(ACCOUNT_ID_KEY, checkNotNull(accountId, "accountId"));
    labels.put(REGION_KEY, checkNotNull(region, "availabilityZone"));
    labels.put(ZONE_KEY, checkNotNull(zone, "zone"));
    return Resource.create(TYPE, labels);
  }

  static Resource detect() {
    if (AwsIdentityDocUtils.isRunningOnAws()) {
      return create(
          PROVIDER_AWS,
          AwsIdentityDocUtils.getAccountId(),
          AwsIdentityDocUtils.getRegion(),
          AwsIdentityDocUtils.getAvailabilityZone());
    }
    if (GcpMetadataConfig.isRunningOnGcp()) {
      return create(
          PROVIDER_GCP, GcpMetadataConfig.getProjectId(), "", GcpMetadataConfig.getZone());
    }
    return EMPTY_RESOURCE;
  }

  private CloudResource() {}
}
