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
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Configurable;

/**
 * CensusSpringSqlAspect captures span from all SQL invocations that utilize
 * java.sql.Statement.execute*
 *
 * @since 0.16.0
 */
@Aspect
@Configurable
public final class CensusSpringSqlAspect {
  private final Tracer tracer;

  /**
   * Creates a {@code CensusSpringSqlAspect} with the given tracer.
   *
   * @param tracer the tracer responsible for building new spans
   * @since 0.16.0
   */
  public CensusSpringSqlAspect(Tracer tracer) {
    this.tracer = tracer;
  }

  /**
   * trace handles invocations of java.sql.Statement.execute*. A new span will be created whose name
   * is (execute|executeQuery|executeQuery)-(hash of sql).
   *
   * @since 0.16.0
   */
  @Around("execute() || testing()")
  public Object trace(ProceedingJoinPoint call) throws Throwable {
    if (call.getArgs().length == 0 || call.getArgs()[0] == null) {
      return call.proceed();
    }

    String sql = (String) call.getArgs()[0];
    String spanName = makeSpanName(call, sql);

    return Handler.proceed(call, tracer, spanName, sql);
  }

  /**
   * execute creates spans around all invocations of Statement.execute*. The raw SQL will be stored
   * in an annotation associated with the Span
   */
  @Pointcut("execution(public !void java.sql.Statement.execute*(java.lang.String))")
  protected void execute() {}

  @Pointcut("execution(public void Sample.execute*(java.lang.String))")
  protected void testing() {}

  private static String makeSpanName(ProceedingJoinPoint call, String sql) {
    String hash = Integer.toHexString(hashCode(sql.toCharArray()));
    return call.getSignature().getName() + "-" + hash;
  }

  private static int hashCode(char[] seq) {
    if (seq == null) {
      return 0;
    }

    int hash = 0;
    for (char c : seq) {
      hash = 31 * hash + c;
    }
    return hash;
  }
}
