package io.opencensus.contrib.spring.aop;

import java.sql.SQLException;

/**
 */
public class Sample {
  @Trace()
  void example1() {
    // do work
  }

  @Trace(name = "custom-label")
  void example2() {
    // do moar work
  }

  @Trace()
  void call(long delay) throws Exception {
    Thread.sleep(delay);
  }

  @Trace(name = "blah")
  void custom(long delay) throws Exception {
    Thread.sleep(delay);
  }

  public void execute(String sql) throws SQLException {
  }

  public void executeQuery(String sql) throws SQLException {
  }

  public void executeUpdate(String sql) throws SQLException {
  }

  public void executeLargeUpdate(String sql) throws SQLException {
  }
}
