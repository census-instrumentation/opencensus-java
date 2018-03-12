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

package io.opencensus.implcore.internal;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Objects;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

/**
 * A utility class to help with registering shutdown {@link Hook}s to the runtime, influenced by
 * Apache Hadoop's <a
 * href="https://github.com/apache/hadoop/blob/trunk/hadoop-common-project/hadoop-common/src/main/java/org/apache/hadoop/util/ShutdownHookManager.java">ShutdownHookManager</a>.
 * It is used for OpenCensus to clean things up in a deterministic order. Possible usages are: 1.
 * close running spans. 2. Dump unexported spans for stat/tracing. 3. Allow users to have their own
 * shutdown hooks (not supported yet, still in discussion).
 *
 * <p>A {@link Hook} can have its own name and priority. The execution order among registered hooks
 * will be determined by their priorities first (descending order) and then registration time
 * (ascending order). That is to say, higher priority {@code Hook} will be executed first, and
 * earlier registered {@code Hook} will be executed first given the same priority.
 *
 * <p>Caveat: 1. GAE Java 7 environment is not supported for now. 2. This manager only manages
 * OpenCensus related shutdown hooks. User can still use other methods to register a shutdown hook,
 * e.g. the native {@link Runtime#addShutdownHook}, or the Hadoop's {@code ShutdownHookManager}. The
 * execution order of registered {@link Hook}s and hooks registered by other systems depends on the
 * JVM implementation and thus can not be guaranteed, which might sometimes leads to unwanted
 * behaviors. An example is that {@code java.util.logging.Logger} has its own shutdown hook to reset
 * all handlers, and once invoked, it will prevent all future logging in other shutdown hooks.
 */
@ThreadSafe
public final class ShutdownHookManager {

  private static final Logger logger = Logger.getLogger(ShutdownHookManager.class.getName());

  // The singlelon instance.
  private static final ShutdownHookManager instance = new ShutdownHookManager();

  private final Object monitor = new Object();

  // A list to store registered hooks.
  @GuardedBy("monitor")
  private final List<Hook> hooks = new ArrayList<Hook>();

  @GuardedBy("monitor")
  private boolean isShuttingDown = false;

  /** A shutdown hook with name and priority. */
  public abstract static class Hook implements Comparable<Hook> {

    // Default priority. It is public for user to know the default priority.
    public static final int DEFAULT_PRIORITY = 100;

    private final String name;
    private final int priority;

    /**
     * Creates a hook with given name and default priority.
     *
     * @param name the name of the hook
     */
    // Suppress false postive warning, see https://github.com/google/error-prone/issues/655 and
    // https://github.com/google/error-prone/pull/789
    @SuppressWarnings("ConstructorLeaksThis")
    public Hook(String name) {
      this(name, DEFAULT_PRIORITY);
    }

    /**
     * Creates a hook with given name and priority.
     *
     * @param name the name of the hook.
     * @param priority the priority of the hook. Use a priority higher than {@link DEFAULT_PRIORITY}
     *     if the hook needs to be executed earlier than default ones. Use a priority lower than
     *     that if the hook needs to be executed later.
     */
    public Hook(String name, int priority) {
      this.name = name;
      this.priority = priority;
    }

    /**
     * Returns the name of this hook.
     *
     * @return the name of this hook.
     */
    public final String getName() {
      return this.name;
    }

    /**
     * Returns the priority of this hook.
     *
     * @return the priority of this hook.
     */
    public final int getPriority() {
      return this.priority;
    }

    @Override
    public int compareTo(@Nullable Hook hook) {
      return hook == null ? -1 : hook.priority - this.priority;
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(name, priority);
    }

    @Override
    public boolean equals(@Nullable Object other) {
      if (other == this) {
        return true;
      }
      if (other instanceof Hook) {
        Hook that = (Hook) other;
        return priority == that.priority && Objects.equal(name, that.name);
      }
      return false;
    }

    /**
     * Things that will be done before shutdown.
     *
     * <p>There is no guarentee of the completion of this method.
     */
    public abstract void run();
  }

  private static final class ShutdownHandler implements Runnable {
    @Override
    public void run() {
      ShutdownHookManager.getInstance().invokeShutdownHooks();
    }
  }

  /**
   * Returns the singleton instance of this class.
   *
   * @return the singleton instance of this class.
   */
  public static ShutdownHookManager getInstance() {
    return instance;
  }

  @VisibleForTesting
  void sortHooks(List<Hook> hooks) {
    // This is a stable sort and will maintain the original order when priorities are equal.
    Collections.sort(hooks);
  }

  @VisibleForTesting
  void invokeShutdownHooks() {
    synchronized (monitor) {
      isShuttingDown = true;
      sortHooks(hooks);
      for (Hook hook : hooks) {
        try {
          hook.run();
        } catch (Throwable e) {
          String message = e.getMessage();
          if (message == null) {
            message = e.getClass().getSimpleName();
          }
          logger.log(
              Level.WARNING,
              String.format(
                  "Error occurred during execution of shutdown hook %s, message is `%s`.",
                  hook.getName(), message));
        }
      }
    }
  }

  /**
   * Adds a {@link Hook} to the manager.
   *
   * @param hook the {@code Hook}
   */
  public void addShutdownHook(Hook hook) {
    checkNotNull(hook, "hook");
    synchronized (monitor) {
      if (!isShuttingDown) {
        hooks.add(hook);
      }
    }
  }

  private ShutdownHookManager() {
    // TODO(hailongwen@): Consider using `LifecycleManager.setShutdownHook` to support GAE Java 7
    // environment.
    if (!DaemonThreadFactory.IS_RESTRICTED_APPENGINE) {
      Runtime.getRuntime()
          .addShutdownHook(
              new DaemonThreadFactory("OpenCensus.ShutdownHookManager")
                  .newThread(new ShutdownHandler()));
    }
  }
}
