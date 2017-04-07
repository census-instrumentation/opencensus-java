package com.google.instrumentation.stats;

import static com.google.common.truth.Truth.assertThat;

import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link com.google.instrumentation.stats.Distribution}. */
@RunWith(JUnit4.class)
public class DistributionTest {

  @Before
  public void setUp() {}

  @Test
  public void testEmptyDistribution() {
    Distribution empty = Distribution.create();
    assertThat(empty.getCount()).isEqualTo(0);
    assertThat(empty.getSum()).isEqualTo(0.0);
    assertThat(empty.getMean()).isEqualTo(Double.NaN);
    assertThat(empty.getBucketCounts()).isNull();
  }

  @Test
  public void testEmptyDistributionWithBuckets() {
    List<Double> buckets = Arrays.asList(0.0, 1.0, 2.0);
    Distribution empty = Distribution.create(buckets);
    assertThat(empty.getCount()).isEqualTo(0);
    assertThat(empty.getSum()).isEqualTo(0.0);
    assertThat(empty.getMean()).isEqualTo(Double.NaN);
    assertThat(empty.getBucketCounts().size()).isEqualTo(4);
    for (int i = 0; i < empty.getBucketCounts().size(); i++) {
      assertThat(empty.getBucketCounts().get(i)).isEqualTo(0);
    }
  }

  @Test
  public void testDistribution() {
    Distribution distribution = Distribution.create();
    distribution.put(1.0);
    assertThat(distribution.getCount()).isEqualTo(1);
    assertThat(distribution.getSum()).isEqualTo(1.0);
    assertThat(distribution.getMean()).isEqualTo(1.0);
    assertThat(distribution.getRange().getMin()).isEqualTo(1.0);
    assertThat(distribution.getRange().getMax()).isEqualTo(1.0);
    distribution.put(1.0);
    assertThat(distribution.getCount()).isEqualTo(2);
    assertThat(distribution.getSum()).isEqualTo(2.0);
    assertThat(distribution.getMean()).isEqualTo(1.0);
    assertThat(distribution.getRange().getMin()).isEqualTo(1.0);
    assertThat(distribution.getRange().getMax()).isEqualTo(1.0);
    distribution.put(7.0);
    assertThat(distribution.getCount()).isEqualTo(3);
    assertThat(distribution.getSum()).isEqualTo(9.0);
    assertThat(distribution.getMean()).isEqualTo(3.0);
    assertThat(distribution.getRange().getMin()).isEqualTo(1.0);
    assertThat(distribution.getRange().getMax()).isEqualTo(7.0);
    distribution.put(-4.6);
    assertThat(distribution.getCount()).isEqualTo(4);
    assertThat(distribution.getSum()).isEqualTo(4.4);
    assertThat(distribution.getMean()).isEqualTo(1.1);
    assertThat(distribution.getRange().getMin()).isEqualTo(-4.6);
    assertThat(distribution.getRange().getMax()).isEqualTo(7.0);
    assertThat(distribution.getBucketCounts()).isNull();
  }

  @Test
  public void testDistributionWithOneBoundary() {
    List<Double> buckets = Arrays.asList(5.0);
    Distribution distribution = Distribution.create(buckets);
    assertThat(distribution.getBucketCounts().size()).isEqualTo(2);
    for (int i = 0; i < distribution.getBucketCounts().size(); i++) {
      assertThat(distribution.getBucketCounts().get(i)).isEqualTo(0);
    }
    distribution.put(1.4);
    distribution.put(5.0);
    distribution.put(-17.0);
    distribution.put(123.45);
    assertThat(distribution.getCount()).isEqualTo(4);
    assertThat(distribution.getBucketCounts().get(0)).isEqualTo(2);
    assertThat(distribution.getBucketCounts().get(1)).isEqualTo(2);
  }

  @Test
  public void testDistributionWithBoundaries() {
    List<Double> buckets = Arrays.asList(-1.0, 2.0, 5.0, 20.0);
    Distribution distribution = Distribution.create(buckets);
    assertThat(distribution.getBucketCounts().size()).isEqualTo(5);
    for (int i = 0; i < distribution.getBucketCounts().size(); i++) {
      assertThat(distribution.getBucketCounts().get(i)).isEqualTo(0);
    }
    distribution.put(-50.0);
    distribution.put(-20.0);
    distribution.put(4.0);
    distribution.put(4.0);
    distribution.put(4.999999);
    distribution.put(5.0);
    distribution.put(5.000001);
    distribution.put(123.45);
    assertThat(distribution.getCount()).isEqualTo(8);
    assertThat(distribution.getBucketCounts().get(0)).isEqualTo(2);
    assertThat(distribution.getBucketCounts().get(1)).isEqualTo(0);
    assertThat(distribution.getBucketCounts().get(2)).isEqualTo(3);
    assertThat(distribution.getBucketCounts().get(3)).isEqualTo(2);
    assertThat(distribution.getBucketCounts().get(4)).isEqualTo(1);
  }
}
