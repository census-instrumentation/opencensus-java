/*
 * Copyright 2016, Google Inc.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.instrumentation.stats;

import com.google.common.io.Files;
import com.google.instrumentation.common.Duration;
import com.google.instrumentation.stats.MeasurementDescriptor.BasicUnit;
import com.google.instrumentation.stats.MeasurementDescriptor.MeasurementUnit;
import com.google.instrumentation.stats.ViewDescriptor.DistributionViewDescriptor;
import com.google.instrumentation.stats.ViewDescriptor.IntervalViewDescriptor;
import com.google.instrumentation.stats.proto.CensusProto;
import com.google.instrumentation.stats.proto.ViewDescriptorConstantsProto;
import com.google.instrumentation.stats.proto.ViewDescriptorConstantsProto.ViewDescriptorConstants;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A parser class used to translate view constants from proto binary format into POJOs.
 */
public class ViewConstantsProtoBinaryParser {

  private final ViewDescriptorConstantsProto.ViewDescriptorConstants viewDescriptorConstants;

  // Used to quickly look up specific measurement descriptor associated with some view descriptor.
  private Map<String, MeasurementDescriptor> measurementDescriptorLocalMap = null;

  /**
   * @param protoBinaryFilePath is the absolute path of the view constants proto binary file.
   */
  public ViewConstantsProtoBinaryParser(String protoBinaryFilePath) {
    final File protoBinaryFile = new File(protoBinaryFilePath);
    try {
      byte[] protoBinaryBytes = Files.toByteArray(protoBinaryFile);
      this.viewDescriptorConstants = ViewDescriptorConstants.parseFrom(protoBinaryBytes);
    } catch (IOException e) {
      throw new AssertionError(e);
    }
  }

  private static final MeasurementDescriptor toMeasurementDescriptor(
      CensusProto.MeasurementDescriptor protoMeasurementDescriptor) {
    List<BasicUnit> numerators = new ArrayList<BasicUnit>();
    List<BasicUnit> denominators = new ArrayList<BasicUnit>();
    for (CensusProto.MeasurementDescriptor.BasicUnit protoBasicUnit :
        protoMeasurementDescriptor.getUnit().getNumeratorsList()) {
      numerators.add(BasicUnit.valueOf(protoBasicUnit.toString()));
    }
    for (CensusProto.MeasurementDescriptor.BasicUnit protoBasicUnit :
        protoMeasurementDescriptor.getUnit().getDenominatorsList()) {
      denominators.add(BasicUnit.valueOf(protoBasicUnit.toString()));
    }

    return MeasurementDescriptor.create(
        protoMeasurementDescriptor.getName(),
        protoMeasurementDescriptor.getDescription(),
        MeasurementUnit.create(
            protoMeasurementDescriptor.getUnit().getPower10(), numerators, denominators));
  }

  private static final ViewDescriptor toViewDescriptor(
      CensusProto.ViewDescriptor protoViewDescriptor,
      Map<String, MeasurementDescriptor> measurementDescriptorMap) {
    final ViewDescriptor viewDescriptor;
    if (protoViewDescriptor.getName().endsWith("interval")) {
      viewDescriptor = IntervalViewDescriptor.create(
          protoViewDescriptor.getName(),
          protoViewDescriptor.getDescription(),
          measurementDescriptorMap.get(protoViewDescriptor.getMeasurementDescriptorName()),
          toIntervalAggregationDescriptor(protoViewDescriptor.getIntervalAggregation()),
          toTagKeys(protoViewDescriptor.getTagKeysList()));
    } else {
      viewDescriptor = DistributionViewDescriptor.create(
          protoViewDescriptor.getName(),
          protoViewDescriptor.getDescription(),
          measurementDescriptorMap.get(protoViewDescriptor.getMeasurementDescriptorName()),
          toDistributionViewDescriptor(protoViewDescriptor.getDistributionAggregation()),
          toTagKeys(protoViewDescriptor.getTagKeysList()));
    }
    return viewDescriptor;
  }

  private static final IntervalAggregationDescriptor toIntervalAggregationDescriptor(
      CensusProto.IntervalAggregationDescriptor protoIntervalAggregationDescriptor) {
    List<Duration> durations = new ArrayList<Duration>();
    for (CensusProto.Duration protoDuration :
        protoIntervalAggregationDescriptor.getIntervalSizesList()) {
      durations.add(Duration.create(protoDuration.getSeconds(), protoDuration.getNanos()));
    }

    int numSubIntervals = protoIntervalAggregationDescriptor.getNSubIntervals();
    if (numSubIntervals < 2 || numSubIntervals > 20) {
      return IntervalAggregationDescriptor.create(durations);
    } else {
      return IntervalAggregationDescriptor.create(
          protoIntervalAggregationDescriptor.getNSubIntervals(), durations);
    }
  }

  private static final DistributionAggregationDescriptor toDistributionViewDescriptor(
      CensusProto.DistributionAggregationDescriptor protoDistributionAggregationDescriptor) {
    return DistributionAggregationDescriptor.create(
        protoDistributionAggregationDescriptor.getBucketBoundsList());
  }

  private static final List<TagKey> toTagKeys(com.google.protobuf.ProtocolStringList protoTagKeys) {
    List<TagKey> tagKeys = new ArrayList<TagKey>();
    for (String tagKeyStr : protoTagKeys) {
      tagKeys.add(TagKey.create(tagKeyStr));
    }
    return tagKeys;
  }

  /**
   * @return a map, where key is measurement descriptor name, and value is the descriptor.
   */
  public final Map<String, MeasurementDescriptor> getMeasurementDescriptorMap() {
    if (this.measurementDescriptorLocalMap != null) {
      return this.measurementDescriptorLocalMap;
    }

    Map<String, MeasurementDescriptor> measurementDescriptorMap =
        new HashMap<String, MeasurementDescriptor>();
    for (CensusProto.MeasurementDescriptor protoMeasurementDescriptor :
        viewDescriptorConstants.getMeasurementDescriptorList()) {
      measurementDescriptorMap.put(protoMeasurementDescriptor.getName(),
          toMeasurementDescriptor(protoMeasurementDescriptor));
    }
    this.measurementDescriptorLocalMap = measurementDescriptorMap;
    return measurementDescriptorMap;
  }

  /**
   * @return a map, where key is view descriptor name, and value is the descriptor.
   */
  public final Map<String, ViewDescriptor> getViewDescriptorMap() {
    if (this.measurementDescriptorLocalMap == null) {
      // Initialize the local map for measurement descriptor if needed.
      getMeasurementDescriptorMap();
    }

    Map<String, ViewDescriptor> viewDescriptorMap = new HashMap<String, ViewDescriptor>();
    for (CensusProto.ViewDescriptor protoViewDescriptor :
        viewDescriptorConstants.getViewDescriptorList()) {
      viewDescriptorMap.put(protoViewDescriptor.getName(),
          toViewDescriptor(protoViewDescriptor, this.measurementDescriptorLocalMap));
    }
    return viewDescriptorMap;
  }
}
