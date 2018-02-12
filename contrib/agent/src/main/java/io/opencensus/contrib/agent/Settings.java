/*
 * Copyright 2017, OpenCensus Authors
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

package io.opencensus.contrib.agent;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

/**
 * The {@code Settings} class provides access to user-configurable settings.
 *
 * @since 0.10
 */
public class Settings {

  private static final String CONFIG_ROOT = "opencensus.contrib.agent";

  private final Config config;

  /**
   * Creates agent settings.
   *
   * @since 0.10
   */
  @VisibleForTesting
  public Settings(Config config) {
    this.config = checkNotNull(config);
  }

  static Settings load() {
    return new Settings(readConfig());
  }

  private static Config readConfig() {
    Config config = ConfigFactory.load();
    config.checkValid(ConfigFactory.defaultReference(), CONFIG_ROOT);

    return config.getConfig(CONFIG_ROOT);
  }

  /**
   * Checks whether a feature is enabled in the effective configuration.
   *
   * <p>A feature is identified by a path expression relative to {@link #CONFIG_ROOT}, such as
   * {@code context-propagation.executor}. The feature is enabled iff the config element at the
   * requested path has a child element {@code enabled} with a value of {@code true}, {@code on}, or
   * {@code yes}.
   *
   * @param featurePath the feature's path expression
   * @return true, if enabled, otherwise false
   * @since 0.10
   */
  public boolean isEnabled(String featurePath) {
    checkArgument(!Strings.isNullOrEmpty(featurePath));

    return config.getConfig(featurePath).getBoolean("enabled");
  }
}
