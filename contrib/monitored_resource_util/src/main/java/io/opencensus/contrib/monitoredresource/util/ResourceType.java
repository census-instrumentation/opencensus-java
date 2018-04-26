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
 *
 * @since 0.13
 */
public enum ResourceType {

  /**
   * Resource for GCP GKE container.
   *
   * @since 0.13
   */
  GCP_GKE_CONTAINER,

  /**
   * Resource for GCP GCE instance.
   *
   * @since 0.13
   */
  GCP_GCE_INSTANCE,

  /**
   * Resource for AWS EC2 instance.
   *
   * @since 0.13
   */
  AWS_EC2_INSTANCE
}
