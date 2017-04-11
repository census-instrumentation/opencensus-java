/*
 * Copyright 2017, Google Inc.
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

package com.google.instrumentation.common;

/**
 * Base interface to be used for all entries in {@link DisruptorEventQueue}. For example usage, see
 * {@link DisruptorEventQueue}.
 */
public interface EventQueueEntry {
  /**
   * Process the event associated with this entry. This will be called for every event in the
   * associated {@link DisruptorEventQueue}.
   */
  void process();
}
