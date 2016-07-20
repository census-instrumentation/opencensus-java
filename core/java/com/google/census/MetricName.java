/*
 * Copyright 2016, Google Inc.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.census;

/**
 * Census Metric names.
 *
 * MetricName's are {@link String}s with enforced restrictions.
 */
public final class MetricName {
  public static final int MAX_LENGTH = Tag.MAX_LENGTH;

  public MetricName(String name) {
    this.name = Tag.sanitize(name);
  }

  @Override
  public boolean equals(Object obj) {
    return (obj instanceof MetricName) && name.equals(((MetricName) obj).name);
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }

  @Override
  public String toString() {
    return name;
  }

  private final String name;
}
