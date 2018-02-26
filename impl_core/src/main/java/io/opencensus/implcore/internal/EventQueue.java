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

package io.opencensus.implcore.internal;

/** A queue that processes events. See {@code DisruptorEventQueue} for an example. */
public interface EventQueue {
  void enqueue(Entry entry);

  /**
   * Base interface to be used for all entries in {@link EventQueue}. For example usage, see {@code
   * DisruptorEventQueue}.
   */
  interface Entry {
    /**
     * Process the event associated with this entry. This will be called for every event in the
     * associated {@link EventQueue}.
     */
    void process();
  }
}
