package io.opencensus.contrib.spring.aop;

import io.opencensus.trace.SpanBuilder;
import io.opencensus.trace.Tracer;
import io.opencensus.trace.Tracing;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Configurable;

/**
 */
@Aspect
@Configurable
public class CensusSpringSQLAspect {
  private static final Tracer tracer = Tracing.getTracer();

  @Around("execute() || testing()")
  public Object trace(ProceedingJoinPoint call) throws Throwable {
    if (call.getArgs().length == 0 || call.getArgs()[0] == null) {
      return call.proceed();
    }

    String sql = (String) call.getArgs()[0];
    String spanName = makeSpanName(call, sql);
    SpanBuilder builder = tracer.spanBuilder(spanName);

    return Handler.proceed(call, builder, sql);
  }

  /**
   * execute creates spans around all invocations of Statement.execute*.  The raw SQL
   * will be stored in an annotation associated with the Span
   */
  @Pointcut("execution(public !void java.sql.Statement.execute*(java.lang.String))")
  protected void execute() {
  }

  @Pointcut("execution(public void io.opencensus.contrib.spring.aop.Sample.execute*(java.lang.String))")
  protected void testing() {
  }

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
