package io.opencensus.implcore.metrics;

import static com.google.common.truth.Truth.assertThat;

import io.opencensus.common.Timestamp;
import io.opencensus.common.ToDoubleFunction;
import io.opencensus.metrics.LabelKey;
import io.opencensus.metrics.LabelValue;
import io.opencensus.metrics.export.Metric;
import io.opencensus.metrics.export.MetricDescriptor;
import io.opencensus.metrics.export.MetricDescriptor.Type;
import io.opencensus.metrics.export.TimeSeries;
import io.opencensus.metrics.export.Value;
import io.opencensus.testing.common.TestClock;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link DoubleGaugeImpl}. */
@RunWith(JUnit4.class)
public class DerivedDoubleGaugeImplTest {
  @Rule public ExpectedException thrown = ExpectedException.none();

  private static final String METRIC_NAME = "name";
  private static final String METRIC_DESCRIPTION = "description";
  private static final String METRIC_UNIT = "1";
  private static final LabelKey LABEL_KEY = LabelKey.create("key", "key description");
  private static final LabelValue LABEL_VALUES = LabelValue.create("value");
  private static final LabelValue LABEL_VALUES_1 = LabelValue.create("value1");
  private final List<LabelKey> labelKeys = new ArrayList<LabelKey>();
  private final List<LabelValue> labelValues = new ArrayList<LabelValue>();
  private final List<LabelValue> labelValues1 = new ArrayList<LabelValue>();

  private static final Timestamp TEST_TIME = Timestamp.create(1234, 123);
  private final TestClock testClock = TestClock.create(TEST_TIME);

  private DerivedDoubleGaugeImpl derivedDoubleGauge;

  @Before
  public void setUp() {
    labelKeys.add(LABEL_KEY);
    labelValues.add(LABEL_VALUES);
    labelValues1.add(LABEL_VALUES_1);

    derivedDoubleGauge =
        new DerivedDoubleGaugeImpl(METRIC_NAME, METRIC_DESCRIPTION, METRIC_UNIT, labelKeys);
  }

  // helper class
  public static class TotalMemory {
    public double getValue() {
      return 2.13;
    }
  }

  @Test
  public void addTimeSeries_WithObjFunction() {
    derivedDoubleGauge.createTimeSeries(
        labelValues,
        new TotalMemory(),
        new ToDoubleFunction<TotalMemory>() {
          @Override
          public double applyAsDouble(TotalMemory memory) {
            return memory.getValue();
          }
        });

    assertThat(derivedDoubleGauge.getMetric(testClock))
        .isEqualTo(
            Metric.create(
                MetricDescriptor.create(
                    METRIC_NAME,
                    METRIC_DESCRIPTION,
                    METRIC_UNIT,
                    Type.GAUGE_DOUBLE,
                    Collections.singletonList(LABEL_KEY)),
                Collections.singletonList(
                    TimeSeries.create(
                        Collections.singletonList(LABEL_VALUES),
                        Collections.singletonList(
                            io.opencensus.metrics.export.Point.create(
                                Value.doubleValue(2.13), TEST_TIME)),
                        null))));
  }

  @Test
  public void addTimeSeries_IncorrectLabelsWithObjFunction() {
    thrown.expect(IllegalArgumentException.class);
    derivedDoubleGauge.createTimeSeries(
        new ArrayList<LabelValue>(),
        null,
        new ToDoubleFunction<TotalMemory>() {
          @Override
          public double applyAsDouble(TotalMemory memory) {
            return memory.getValue();
          }
        });
  }
}
