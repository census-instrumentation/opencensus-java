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

/**
 * {@link ResourceType} represents the type of supported monitored resources that can be
 * automatically detected by OpenCensus.
 */
enum ResourceType {
  GkeContainer("gke_container"),
  GceInstance("gce_instance"),
  AwsEc2Instance("aws_ec2_instance"),
  Global("global");

  private final String type;

  ResourceType(String type) {
    this.type = type;
  }

  /** Returns the type of {@link ResourceType}. */
  String getType() {
    return type;
  }
}
