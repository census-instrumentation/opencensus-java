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

package io.opencensus.contrib.agent.bootstrap;

/**
 * {@link ContextProxy} proxies method calls for {@link io.grpc.Context}, thereby avoiding tight
 * coupling between the agent and the concrete implementation version of {@link io.grpc.Context}.
 *
 * <p>The agent loads the {@link ContextProxy} interface from the bootstrap classloader so that it
 * can be used from code that the agents injects into classes loaded by the bootstrap classloader.
 * To avoid having to also load {@link io.grpc.Context} from the bootstrap classloader,
 * {@link ContextProxy} delegates to its implementation
 * {@link io.opencensus.contrib.agent.ContextProxyImpl}, which is loaded by the system
 * classloader. {@link ContextProxy} cannot directly reference {@link io.grpc.Context}, thus Object
 * is used in the interface, whereas its implementation
 * ({@link io.opencensus.contrib.agent.ContextProxyImpl}) can downcast to
 * {@link io.grpc.Context} whenever required.
 */
public abstract class ContextProxy {

  /** Reference to the implementation of {@link ContextProxy}, loaded by the system classloader. */
  private static ContextProxy contextProxy;

  public static void init(ContextProxy contextProxy) {
    ContextProxy.contextProxy = contextProxy;
  }

  public static Object attach(Object context) {
    return contextProxy.attachInternal(context);
  }

  public static Object current() {
    return contextProxy.currentInternal();
  }

  public static Runnable wrap(Object context, Runnable r) {
    return contextProxy.wrapInternal(context, r);
  }

  public static Runnable wrapInCurrent(Runnable r) {
    return wrap(ContextProxy.current(), r);
  }

  protected abstract Object attachInternal(Object context);

  protected abstract Object currentInternal();

  protected abstract Runnable wrapInternal(Object context, Runnable r);
}
