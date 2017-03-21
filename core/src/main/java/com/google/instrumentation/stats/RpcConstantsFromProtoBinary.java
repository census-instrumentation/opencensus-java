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
import com.google.instrumentation.stats.proto.CensusProto;
import com.google.instrumentation.stats.proto.ViewDescriptorConstantsProto;
import com.google.instrumentation.stats.proto.ViewDescriptorConstantsProto.ViewDescriptorConstants;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Constants for collecting rpc stats.
 */
public class RpcConstantsFromProtoBinary {
  private static ViewDescriptorConstantsProto.ViewDescriptorConstants viewDescriptorConstants;

  // The proto binary will be read during run-time. File location of the binary could be different
  // depending on how we finally decide to place the proto binary files.
  private static final String rpcConstantsProtoBinaryFilePath =
      "REPLACE THIS WITH THE PATH TO RPC CONSTANTS PROTO BINARY FILE.";

  static {
    final File rpcConstantsProtoBinary = new File(rpcConstantsProtoBinaryFilePath);
    try {
      byte[] rpcConstantsProtoBinaryBytes = Files.toByteArray(rpcConstantsProtoBinary);
      viewDescriptorConstants = ViewDescriptorConstants.parseFrom(rpcConstantsProtoBinaryBytes);
    } catch (IOException e) {
      throw new AssertionError(e);
    }
  }

  public static final List<CensusProto.MeasurementDescriptor> getMeasurementDescriptorList() {
    return viewDescriptorConstants.getMeasurementDescriptorList();
  }

  public static final List<CensusProto.ViewDescriptor> getViewDescriptorList() {
    return viewDescriptorConstants.getViewDescriptorList();
  }

  private RpcConstantsFromProtoBinary() {
    throw new AssertionError();
  }
}
