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

package io.opencensus.contrib.spring.aop;

import io.opencensus.trace.Tracer;
import java.lang.reflect.Method;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Configurable;

/**
 * CensusSpringAspect handles logic for the `@Traced` annotation.
 *
 * @since 0.16.0
 */
@Aspect
@Configurable
public final class CensusSpringAspect {
  private final Tracer tracer;

  /**
   * @param tracer the tracer responsible for building new spans
   * @since 0.16.0
   */
  public CensusSpringAspect(Tracer tracer) {
    this.tracer = tracer;
  }

  /**
   * trace handles methods executed with the `@Traced` annotation. A new span will be created with
   * an optionally customizable span name.
   *
   * @param call the join point to execute
   * @return the result of the invocation
   * @throws Throwable if the underlying target throws an exception
   * @since 0.16.0
   */
  @Around("@annotation(io.opencensus.contrib.spring.aop.Traced)")
  public Object trace(ProceedingJoinPoint call) throws Throwable {
    MethodSignature signature = (MethodSignature) call.getSignature();
    Method method = signature.getMethod();

    Traced annotation = method.getAnnotation(Traced.class);
    String spanName = annotation.name();
    if (spanName.isEmpty()) {
      spanName = method.getName();
    }

    return Handler.proceed(call, tracer, spanName);
  }
}
