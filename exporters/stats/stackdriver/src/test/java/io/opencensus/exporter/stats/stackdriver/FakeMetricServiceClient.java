/*
 * Copyright 2019, OpenCensus Authors
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

package io.opencensus.exporter.stats.stackdriver;

import com.google.cloud.monitoring.v3.MetricServiceClient;
import com.google.cloud.monitoring.v3.stub.MetricServiceStub;

/**
 * MetricServiceClient.createMetricDescriptor() and MetricServiceClient.createTimeSeries() are final
 * methods and cannot be mocked. We have to use a mock MetricServiceStub in order to verify the
 * output.
 */
final class FakeMetricServiceClient extends MetricServiceClient {

  FakeMetricServiceClient(MetricServiceStub stub) {
    super(stub);
  }
}
