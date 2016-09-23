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
 * Census Tag keys.
 *
 * TagKey's are {@link String}s with enforced restrictions.
 */
public final class TagKey {
  public static final int MAX_LENGTH = StringSanitization.MAX_LENGTH;

  public TagKey(String key) {
    this.key = StringSanitization.sanitize(key);
  }

  @Override
  public boolean equals(Object obj) {
    return (obj instanceof TagKey) && key.equals(((TagKey) obj).key);
  }

  @Override
  public int hashCode() {
    return key.hashCode();
  }

  @Override
  public String toString() {
    return key;
  }

  private final String key;
}
