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

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkNotNull;

import io.opencensus.resource.Resource;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Helper class for K8S container {@code Resource}.
 *
 * @since 0.20
 */
public class ContainerResource {
  /**
   * Kubernetes resources key that represents a type of the resource.
   *
   * @since 0.20
   */
  public static final String TYPE = "container";

  /**
   * Key for the container name.
   *
   * @since 0.20
   */
  public static final String NAME_KEY = "container.name";

  /**
   * Key for the container image name.
   *
   * @since 0.20
   */
  public static final String IMAGE_NAME_KEY = "container.image.name";

  /**
   * Key for the container image tag.
   *
   * @since 0.20
   */
  public static final String IMAGE_TAG_KEY = "container.image.tag";

  /**
   * Returns a {@link Resource} that describes a container.
   *
   * @param name the container name.
   * @param imageName the container image name.
   * @param imageTag the container image tag.
   * @return a {@link Resource} that describes a k8s container.
   * @since 0.20
   */
  public static Resource create(String name, String imageName, String imageTag) {
    Map<String, String> mutableLabels = new LinkedHashMap<String, String>();
    mutableLabels.put(NAME_KEY, checkNotNull(name, "name"));
    mutableLabels.put(IMAGE_NAME_KEY, checkNotNull(imageName, "imageName"));
    mutableLabels.put(IMAGE_TAG_KEY, checkNotNull(imageTag, "imageTag"));
    return Resource.create(TYPE, Collections.unmodifiableMap(mutableLabels));
  }

  static Resource detect() {
    return create(firstNonNull(System.getenv("CONTAINER_NAME"), ""), "", "");
  }

  private ContainerResource() {}
}
