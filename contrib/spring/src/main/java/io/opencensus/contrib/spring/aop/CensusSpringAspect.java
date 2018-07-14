package io.opencensus.contrib.spring.aop;

import io.opencensus.trace.SpanBuilder;
import io.opencensus.trace.Tracing;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Configurable;

import java.lang.reflect.Method;

/**
 * CensusSpringAspect handles logic for the @Trace annotation
 */
@Aspect
@Configurable
public class CensusSpringAspect {
  @Around("@annotation(Trace)")
  public Object trace(ProceedingJoinPoint call) throws Throwable {
    MethodSignature signature = (MethodSignature) call.getSignature();
    Method method = signature.getMethod();

    Trace annotation = method.getAnnotation(Trace.class);
    String spanName = annotation.name();
    if ("".equals(spanName)) {
      spanName = method.getName();
    }

    SpanBuilder builder = Tracing.getTracer().spanBuilder(spanName);

    return Handler.proceed(call, builder);
  }
}
