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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Helper class for Host {@code Resource}. A host is defined as a general computing instance.
 *
 * @since 0.20
 */
public final class HostResource {
  /**
   * The type of this {@code Resource}.
   *
   * @since 0.20
   */
  public static final String TYPE = "host";

  /**
   * Key for the hostname of the host.
   *
   * <p>It contains what the `hostname` command returns on the host machine.
   *
   * @since 0.20
   */
  public static final String HOSTNAME_KEY = "host.hostname";

  /**
   * Key for the name of the host.
   *
   * <p>It may contain what `hostname` returns on Unix systems, the fully qualified, or a name
   * specified by the user.
   *
   * @since 0.20
   */
  public static final String NAME_KEY = "host.name";

  /**
   * Key for the unique host id (instance id in Cloud).
   *
   * @since 0.20
   */
  public static final String ID_KEY = "host.id";

  /**
   * Key for the type of the host (machine type).
   *
   * @since 0.20
   */
  public static final String TYPE_KEY = "host.type";

  /**
   * Returns a {@link Resource} that describes a k8s container.
   *
   * @param hostname the hostname of the host.
   * @param name the name of the host.
   * @param id the unique host id (instance id in Cloud).
   * @param type the type of the host (machine type).
   * @return a {@link Resource} that describes a k8s container.
   * @since 0.20
   */
  public static Resource create(String hostname, String name, String id, String type) {
    Map<String, String> mutableLabels = new LinkedHashMap<String, String>();
    mutableLabels.put(HOSTNAME_KEY, checkNotNull(hostname, "hostname"));
    mutableLabels.put(NAME_KEY, checkNotNull(name, "name"));
    mutableLabels.put(ID_KEY, checkNotNull(id, "id"));
    mutableLabels.put(TYPE_KEY, checkNotNull(type, "type"));
    return Resource.create(TYPE, Collections.unmodifiableMap(mutableLabels));
  }

  static Resource detect() {
    if (AwsIdentityDocUtils.isRunningOnAws()) {
      return create(
          "", "", AwsIdentityDocUtils.getInstanceId(), AwsIdentityDocUtils.getMachineType());
    }
    if (GcpMetadataConfig.isRunningOnGcp()) {
      return create(
          GcpMetadataConfig.getInstanceHostname(),
          GcpMetadataConfig.getInstanceName(),
          GcpMetadataConfig.getInstanceId(),
          GcpMetadataConfig.getMachineType());
    }
    return EMPTY_RESOURCE;
  }

  private HostResource() {}
}
