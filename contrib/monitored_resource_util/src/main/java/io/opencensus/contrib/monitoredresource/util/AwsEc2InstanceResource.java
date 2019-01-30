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

package io.opencensus.contrib.monitoredresource.util;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkNotNull;

import io.opencensus.resource.Resource;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Helper class for AWS EC2 instances {@code Resource}.
 *
 * @since 0.20
 */
public final class AwsEc2InstanceResource {
  private static final String ACCOUNT_ID =
      firstNonNull(AwsIdentityDocUtils.getValueFromAwsIdentityDocument("accountId"), "");
  private static final String INSTANCE_ID =
      firstNonNull(AwsIdentityDocUtils.getValueFromAwsIdentityDocument("instanceId"), "");
  private static final String REGION =
      firstNonNull(AwsIdentityDocUtils.getValueFromAwsIdentityDocument("region"), "");

  /**
   * AWS key that represents a type of the resource.
   *
   * @since 0.20
   */
  public static final String TYPE = "aws.com/ec2/instance";

  /**
   * AWS key that represents the AWS account number for the VM.
   *
   * @since 0.20
   */
  public static final String ACCOUNT_ID_KEY = "aws.com/ec2/account_id";

  /**
   * AWS key that represents the VM instance identifier assigned by AWS.
   *
   * @since 0.20
   */
  public static final String INSTANCE_ID_KEY = "aws.com/ec2/instance_id";

  /**
   * AWS key that represents a region for the VM.
   *
   * @since 0.20
   */
  public static final String REGION_KEY = "aws.com/ec2/region";

  /**
   * Returns a {@link Resource} that describes a aws ec2 instance.
   *
   * @param accountId the AWS account ID.
   * @param region the AWS region.
   * @param instanceId the AWS EC2 instance ID.
   * @return a {@link Resource} that describes a aws ec2 instance.
   * @since 0.20
   */
  public static Resource create(String accountId, String region, String instanceId) {
    Map<String, String> mutableLabels = new LinkedHashMap<String, String>();
    mutableLabels.put(ACCOUNT_ID_KEY, checkNotNull(accountId, "accountId"));
    mutableLabels.put(REGION_KEY, checkNotNull(region, "region"));
    mutableLabels.put(INSTANCE_ID_KEY, checkNotNull(instanceId, "instanceId"));
    return Resource.create(TYPE, Collections.unmodifiableMap(mutableLabels));
  }

  static Resource detect() {
    return create(ACCOUNT_ID, REGION, INSTANCE_ID);
  }

  private AwsEc2InstanceResource() {}
}
