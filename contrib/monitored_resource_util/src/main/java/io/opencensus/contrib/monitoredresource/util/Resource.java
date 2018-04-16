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
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import javax.annotation.concurrent.Immutable;

/**
 * {@link Resource} represents an auto-detected monitored resource used by application for exporting
 * stats. It has a {@code ResourceType} associated with a mapping from resource labels to values.
 *
 * @since 0.13
 */
@AutoValue
@Immutable
public abstract class Resource {

  Resource() {}

  /**
   * Returns the string representation of {@link ResourceType} of this {@link Resource}.
   *
   * @return the {@code ResourceType}.
   * @since 0.13
   */
  public abstract String getResourceType();

  /**
   * Returns a mapping from {@code LabelKey}s to values associated with this {@link Resource}.
   *
   * @return the mapping from {@code LabelKey}s to their values.
   * @since 0.13
   */
  public abstract Map<String, String> getLabels();

  /**
   * Creates a {@link Resource} from the given {@link ResourceType} and labels.
   *
   * @param resourceType the {@code ResourceType}.
   * @param labels the labels.
   * @return a new {@code Resource}.
   * @since 0.13
   */
  public static Resource create(String resourceType, Map<String, String> labels) {
    return new AutoValue_Resource(resourceType, ImmutableMap.copyOf(labels));
  }
}
