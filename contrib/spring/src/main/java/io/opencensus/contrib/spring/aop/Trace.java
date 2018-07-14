package io.opencensus.contrib.spring.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Trace specifies the annotated method should be included in the Trace.
 *
 * <p>
 * By default, the name of the method will be used for the span name. However, the
 * span name can be explicitly set via the name interface.
 * </p>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Trace {
  /**
   * @return the optional custom span name; if not specified the method name will be
   * used as the span name
   */
  String name() default "";
}
