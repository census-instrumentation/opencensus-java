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

/** Unit tests for {@link AwsEc2InstanceResource}. */
@RunWith(JUnit4.class)
public class AwsEc2InstanceResourceTest {
  private static final String AWS_ACCOUNT_ID = "aws-account";
  private static final String AWS_INSTANCE_ID = "instance";
  private static final String AWS_REGION = "us-west-2";

  @Test
  public void create_AwsEc2InstanceResource() {
    Resource resource = AwsEc2InstanceResource.create(AWS_ACCOUNT_ID, AWS_REGION, AWS_INSTANCE_ID);
    assertThat(resource.getType()).isEqualTo(AwsEc2InstanceResource.TYPE);
    assertThat(resource.getLabels())
        .containsExactly(
            AwsEc2InstanceResource.ACCOUNT_ID_KEY,
            AWS_ACCOUNT_ID,
            AwsEc2InstanceResource.REGION_KEY,
            AWS_REGION,
            AwsEc2InstanceResource.INSTANCE_ID_KEY,
            AWS_INSTANCE_ID);
  }
}
