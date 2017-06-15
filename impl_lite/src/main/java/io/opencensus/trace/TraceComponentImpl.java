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

package io.opencensus.trace;

import io.opencensus.common.MillisClock;
import io.opencensus.internal.SimpleEventQueue;
import io.opencensus.trace.internal.RandomHandler.SecureRandomHandler;

/** Android-compatible implementation of the {@link TraceComponent}. */
public final class TraceComponentImpl extends TraceComponentImplBase {

  public TraceComponentImpl() {
    super(MillisClock.getInstance(), new SecureRandomHandler(), new SimpleEventQueue());
  }
}
