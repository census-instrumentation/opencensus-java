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

import com.google.common.util.concurrent.MoreExecutors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/** A {@link ThreadFactory} implementation that starts all {@link Thread} as daemons. */
public final class DaemonThreadFactory implements ThreadFactory {
  // AppEngine runtimes have constraints on threading and socket handling
  // that need to be accommodated.
  public static final boolean IS_RESTRICTED_APPENGINE =
      System.getProperty("com.google.appengine.runtime.environment") != null
          && "1.7".equals(System.getProperty("java.specification.version"));
  private static final String DELIMITER = "-";
  private static final ThreadFactory threadFactory = MoreExecutors.platformThreadFactory();
  private final AtomicInteger threadIdGen = new AtomicInteger();
  private final String threadPrefix;

  /**
   * Constructs a new {@code DaemonThreadFactory}.
   *
   * @param threadPrefix used to prefix all thread names. (E.g. "CensusDisruptor").
   */
  public DaemonThreadFactory(String threadPrefix) {
    this.threadPrefix = threadPrefix + DELIMITER;
  }

  @Override
  public Thread newThread(Runnable r) {
    Thread thread = threadFactory.newThread(r);
    if (!IS_RESTRICTED_APPENGINE) {
      thread.setName(threadPrefix + threadIdGen.getAndIncrement());
      thread.setDaemon(true);
    }
    return thread;
  }
}
