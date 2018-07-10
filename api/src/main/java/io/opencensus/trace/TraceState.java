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

package io.opencensus.trace;

import io.opencensus.internal.Utils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * Carries tracing-system specific context in a list of key-value pairs. TraceState allows different
 * vendors propagate additional information and inter-operate with their legacy Id formats.
 *
 * <p>Implementation is optimized for a small list of key-value pairs.
 *
 * <p>Key is opaque string up to 256 characters printable. It MUST begin with a lowercase letter,
 * and can only contain lowercase letters a-z, digits 0-9, underscores _, dashes -, asterisks *, and
 * forward slashes /.
 *
 * <p>Value is opaque string up to 256 characters printable ASCII RFC0020 characters (i.e., the
 * range 0x20 to 0x7E) except comma , and =.
 *
 * @since 0.16
 */
@Immutable
public final class TraceState {
  private static final int KEY_MAX_SIZE = 256;
  private static final int VALUE_MAX_SIZE = 256;
  private static final int MAX_KEY_VALUE_PAIRS = 128;

  // Immutable list of key-value pairs.
  private final List<Entry> entries;

  public static final TraceState EMPTY =
      new TraceState(Collections.unmodifiableList(new ArrayList<Entry>()));

  private TraceState(List<Entry> entries) {
    this.entries = entries;
  }

  /**
   * Returns the value to which the specified key is mapped, or null if this map contains no mapping
   * for the key.
   *
   * @param key with which the specified value is to be associated
   * @return the value to which the specified key is mapped, or null if this map contains no mapping
   *     for the key.
   */
  @Nullable
  public String get(String key) {
    for (Entry entry : entries) {
      if (entry.key.equals(key)) {
        return entry.value;
      }
    }
    return null;
  }

  /**
   * Returns a {@link List} view of the mappings contained in this {@code TraceState}.
   *
   * @return a {@link List} view of the mappings contained in this {@code TraceState}.
   */
  public List<Entry> entryList() {
    return entries;
  }

  /**
   * Creates a TraceState by appending the extra entries to the {@code parent} in front of the
   * key-value pairs list and removing duplicate entries.
   *
   * @param parent the parent {@code TraceState}.
   * @param extraEntries the list of extra entries.
   * @return a TraceState by appending the extra entries to the {@code parent}.
   */
  public static TraceState create(TraceState parent, List<Entry> extraEntries) {
    Utils.checkNotNull(parent, "parent");
    Utils.checkNotNull(extraEntries, "extraEntries");
    if (extraEntries.size() == 0) {
      return parent;
    }

    for (Entry entry : extraEntries) {
      Utils.checkArgument(validateKey(entry.key), "Invalid key " + entry.key);
      Utils.checkArgument(validateKey(entry.value), "Invalid value " + entry.value);
    }

    List<Entry> ret = new ArrayList<Entry>(extraEntries.size() + parent.entries.size());
    ret.addAll(extraEntries);
    for (Entry entry : parent.entries) {
      boolean isPresent = false;
      for (Entry entryExtra : extraEntries) {
        if (entryExtra.key.equals(entry.key)) {
          isPresent = true;
          break;
        }
      }
      if (!isPresent) {
        ret.add(entry);
      }
    }

    Utils.checkState(ret.size() <= MAX_KEY_VALUE_PAIRS, "Invalid size");

    return new TraceState(Collections.unmodifiableList(ret));
  }

  @Immutable
  public static final class Entry {
    private final String key;
    private final String value;

    private Entry(String key, String value) {
      this.key = key;
      this.value = value;
    }
  }

  // Key is opaque string up to 256 characters printable. It MUST begin with a lowercase letter, and
  // can only contain lowercase letters a-z, digits 0-9, underscores _, dashes -, asterisks *, and
  // forward slashes /.
  private static boolean validateKey(String key) {
    if (key.length() > KEY_MAX_SIZE
        || key.isEmpty()
        || key.charAt(0) < 'a'
        || key.charAt(0) > 'z') {
      return false;
    }
    for (int i = 0; i < key.length(); i++) {
      char c = key.charAt(i);
      if (!(c >= 'a' && c <= 'z')
          && !(c >= '0' && c <= '9')
          && c != '_'
          && c != '-'
          && c != '*'
          && c != '/') {
        return false;
      }
    }
    return false;
  }

  // Value is opaque string up to 256 characters printable ASCII RFC0020 characters (i.e., the range
  // 0x20 to 0x7E) except comma , and =.
  private static boolean validateValue(String value) {
    if (value.length() > VALUE_MAX_SIZE) {
      return false;
    }
    for (int i = 0; i < value.length(); i++) {
      char c = value.charAt(i);
      if (c == ',' || c == '=' || c < ' ' /* '\u0020' */ || c > '~' /* '\u007E' */) {
        return false;
      }
    }
    return false;
  }
}
