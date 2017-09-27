/*
 * Copyright 2017, OpenCensus Authors
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

package io.opencensus.implcore.stats.export;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import io.opencensus.common.Duration;
import io.opencensus.common.Timestamp;
import io.opencensus.implcore.internal.SimpleEventQueue;
import io.opencensus.implcore.stats.StatsComponentImplBase;
import io.opencensus.implcore.stats.StatsRecorderImpl;
import io.opencensus.implcore.stats.ViewManagerImpl;
import io.opencensus.implcore.tags.TagsComponentImplBase;
import io.opencensus.stats.Aggregation.Distribution;
import io.opencensus.stats.BucketBoundaries;
import io.opencensus.stats.Measure.MeasureDouble;
import io.opencensus.stats.MeasureMap;
import io.opencensus.stats.View;
import io.opencensus.stats.View.AggregationWindow.Cumulative;
import io.opencensus.stats.View.Name;
import io.opencensus.stats.ViewData;
import io.opencensus.stats.export.StatsExporter.Handler;
import io.opencensus.tags.TagKey.TagKeyString;
import io.opencensus.tags.TagValue.TagValueString;
import io.opencensus.tags.Tagger;
import io.opencensus.testing.common.TestClock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.annotation.concurrent.GuardedBy;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/** Unit tests for {@link StatsExporterImpl}. */
@RunWith(JUnit4.class)
public class StatsExporterImplTest {

  private static final TagKeyString TAG_KEY = TagKeyString.create("key");
  private static final TagValueString TAG_VALUE_1 = TagValueString.create("value1");
  private static final TagValueString TAG_VALUE_2 = TagValueString.create("value2");
  private static final MeasureDouble MEASURE = MeasureDouble.create("measure", "description", "1");
  private static final BucketBoundaries BUCKET_BOUNDARIES = BucketBoundaries.create(
      Arrays.asList(-10.0, 0.0, 10.0));
  private static final Distribution DISTRIBUTION = Distribution.create(BUCKET_BOUNDARIES);
  private static final Cumulative CUMULATIVE = Cumulative.create();
  private static final Name VIEW_NAME = Name.create("view");
  private static final View VIEW = View.create(
      VIEW_NAME, "description", MEASURE, DISTRIBUTION, Arrays.asList(TAG_KEY), CUMULATIVE);
  private static final Timestamp START = Timestamp.fromMillis(1000);
  private static final Duration ONE_SECOND = Duration.create(1, 0);
  private static final String SERVICE_NAME = "test.service";
  private static final String MOCK_SERVICE_NAME = "mock.service";

  private final StatsExporterImpl statsExporter = StatsExporterImpl.create(4, 1000);
  private final FakeServiceHandler serviceHandler = new FakeServiceHandler();
  @Mock private Handler mockServiceHandler;
  @Captor private ArgumentCaptor<Collection<ViewData>> argumentCaptor;

  private final TestClock clock = TestClock.create();
  private final StatsComponentImplBase statsComponent = new StatsComponentImplBase(
      new SimpleEventQueue(), clock, new ExportComponentImpl(statsExporter));
  private final Tagger tagger = new TagsComponentImplBase().getTagger();
  private final ViewManagerImpl viewManager = statsComponent.getViewManager();
  private final StatsRecorderImpl statsRecorder = statsComponent.getStatsRecorder();

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    clock.setTime(START);
    statsExporter.registerHandler(SERVICE_NAME, serviceHandler);
    viewManager.registerView(VIEW, Arrays.<Handler>asList(serviceHandler));
  }

  @Test
  public void registerAndUnregisterService() {
    doNothing().when(mockServiceHandler).export(anyListOf(ViewData.class));

    // Not registered yet
    clock.advanceTime(ONE_SECOND);
    statsRecorder.record(
        tagger.emptyBuilder().put(TAG_KEY, TAG_VALUE_1).build(),
        MeasureMap.builder().put(MEASURE, 4.4).build());
    final ViewData viewData1 = viewManager.getView(VIEW);
    verify(mockServiceHandler, timeout(1000).never()).export(anyListOf(ViewData.class));

    // Register
    statsExporter.registerHandler(MOCK_SERVICE_NAME, mockServiceHandler);
    viewManager.registerView(VIEW, Arrays.asList(mockServiceHandler));
    verify(mockServiceHandler, only()).registerView(same(VIEW));
    clock.advanceTime(ONE_SECOND);
    statsRecorder.record(
        tagger.emptyBuilder().put(TAG_KEY, TAG_VALUE_1).build(),
        MeasureMap.builder().put(MEASURE, 5.5).build());
    final ViewData viewData2 = viewManager.getView(VIEW);
    verify(mockServiceHandler, timeout(1000).times(1)).export(argumentCaptor.capture());
    // Note that viewData1 hasn't been consumed yet
    assertThat(argumentCaptor.getValue()).containsExactly(viewData1, viewData2).inOrder();

    // Unregister
    statsExporter.unregisterHandler(MOCK_SERVICE_NAME);
    clock.advanceTime(ONE_SECOND);
    statsRecorder.record(
        tagger.emptyBuilder().put(TAG_KEY, TAG_VALUE_1).build(),
        MeasureMap.builder().put(MEASURE, 6.6).build());
    verify(mockServiceHandler, timeout(1000).times(1)).export(anyListOf(ViewData.class));
  }

  @Test
  public void exportDifferentViewData() {
    clock.advanceTime(ONE_SECOND);
    statsRecorder.record(
        tagger.emptyBuilder().put(TAG_KEY, TAG_VALUE_1).build(),
        MeasureMap.builder().put(MEASURE, 1.1).build());
    final ViewData viewData1 = viewManager.getView(VIEW);

    clock.advanceTime(ONE_SECOND);
    statsRecorder.record(
        tagger.emptyBuilder().put(TAG_KEY, TAG_VALUE_2).build(),
        MeasureMap.builder().put(MEASURE, 2.2).build());
    final ViewData viewData2 = viewManager.getView(VIEW);

    clock.advanceTime(ONE_SECOND);
    statsRecorder.record(
        tagger.emptyBuilder().put(TAG_KEY, TAG_VALUE_2).build(),
        MeasureMap.builder().put(MEASURE, 33.3).build());
    final ViewData viewData3 = viewManager.getView(VIEW);

    List<ViewData> exported = serviceHandler.waitForExport(3);
    assertThat(exported).hasSize(3);
    assertThat(exported).containsExactly(viewData1, viewData2, viewData3).inOrder();
  }

  @Test
  public void exportMoreViewDataThanTheBufferSize() {
    List<ViewData> expected = Lists.newArrayList();
    for (int i = 0; i < 6; i++) {
      clock.advanceTime(ONE_SECOND);
      statsRecorder.record(
          tagger.emptyBuilder().put(TAG_KEY, TAG_VALUE_1).build(),
          MeasureMap.builder().put(MEASURE, i).build());
      expected.add(viewManager.getView(VIEW));
    }
    List<ViewData> exported = serviceHandler.waitForExport(6);
    assertThat(exported).hasSize(6);
    assertThat(exported).isEqualTo(expected);
  }

  @Test
  public void interruptWorkerThreadStops() throws InterruptedException {
    Thread serviceExporterThread = statsExporter.getServiceExporterThread();
    serviceExporterThread.interrupt();
    // Test that the worker thread will stop.
    serviceExporterThread.join();
  }

  @Test
  public void serviceHandlerThrowsException() {
    doThrow(new IllegalArgumentException("No export for you."))
        .when(mockServiceHandler)
        .export(anyListOf(ViewData.class));
    statsExporter.registerHandler(MOCK_SERVICE_NAME, mockServiceHandler);
    viewManager.registerView(VIEW, Arrays.asList(mockServiceHandler));
    clock.advanceTime(ONE_SECOND);
    statsRecorder.record(
        tagger.emptyBuilder().put(TAG_KEY, TAG_VALUE_1).build(),
        MeasureMap.builder().put(MEASURE, 8.8).build());
    ViewData viewData1 = viewManager.getView(VIEW);
    List<ViewData> exported1 = serviceHandler.waitForExport(1);
    assertThat(exported1).containsExactly(viewData1);
    // Continue to export after the exception was received.
    clock.advanceTime(ONE_SECOND);
    statsRecorder.record(
        tagger.emptyBuilder().put(TAG_KEY, TAG_VALUE_1).build(),
        MeasureMap.builder().put(MEASURE, 9.9).build());
    ViewData viewData2 = viewManager.getView(VIEW);
    List<ViewData> exported2 = serviceHandler.waitForExport(1);
    assertThat(exported2).containsExactly(viewData2);
  }

  @Test
  public void exportToMultipleServices() {
    FakeServiceHandler serviceHandler2 = new FakeServiceHandler();
    statsExporter.registerHandler("test.service2", serviceHandler2);
    viewManager.registerView(VIEW, Arrays.<Handler>asList(serviceHandler2));

    List<ViewData> expected = Lists.newArrayList();
    clock.advanceTime(ONE_SECOND);
    statsRecorder.record(
        tagger.emptyBuilder().put(TAG_KEY, TAG_VALUE_1).build(),
        MeasureMap.builder().put(MEASURE, 66.6).build());
    expected.add(viewManager.getView(VIEW));
    clock.advanceTime(ONE_SECOND);
    statsRecorder.record(
        tagger.emptyBuilder().put(TAG_KEY, TAG_VALUE_1).build(),
        MeasureMap.builder().put(MEASURE, 77.7).build());
    expected.add(viewManager.getView(VIEW));

    assertThat(serviceHandler.waitForExport(2)).isEqualTo(expected);
    assertThat(serviceHandler2.waitForExport(2)).isEqualTo(expected);
  }

  /** Fake {@link Handler} for testing only. */
  private static final class FakeServiceHandler extends Handler {
    private final Object monitor = new Object();

    @GuardedBy("monitor")
    private final Set<View> registeredViews = Sets.newHashSet();

    @GuardedBy("monitor")
    private final List<ViewData> viewDataList = new LinkedList<ViewData>();

    @Override
    public void registerView(View view) {
      synchronized (monitor) {
        registeredViews.add(view);
      }
    }

    @Override
    public void export(Collection<ViewData> viewDataList) {
      synchronized (monitor) {
        for (ViewData viewData : viewDataList) {
          if (registeredViews.contains(viewData.getView())) {
            this.viewDataList.add(viewData);
          }
        }
        monitor.notifyAll();
      }
    }

    // Waits until we received specified number ViewDatas to export. Returns the list of exported
    // ViewData objects, otherwise null if the current thread is interrupted
    private List<ViewData> waitForExport(int number) {
      List<ViewData> ret;
      synchronized (monitor) {
        while (viewDataList.size() < number) {
          try {
            monitor.wait();
          } catch (InterruptedException e) {
            // Preserve the interruption status as per guidance.
            Thread.currentThread().interrupt();
            return null;
          }
        }
        ret = new ArrayList<ViewData>(viewDataList);
        viewDataList.clear();
      }
      return ret;
    }
  }
}
