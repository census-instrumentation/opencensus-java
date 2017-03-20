package com.google.instrumentation.stats;

import static com.google.common.truth.Truth.assertThat;

import com.google.instrumentation.stats.proto.CensusProto;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for {@link RpcConstants}
 */
@RunWith(JUnit4.class)
public class RpcConstantsFromProtoBinaryTest {
  private List<CensusProto.MeasurementDescriptor> measurementDescriptorList =
      RpcConstantsFromProtoBinary.getMeasurementDescriptorList();
  private List<CensusProto.ViewDescriptor> viewDescriptorList =
      RpcConstantsFromProtoBinary.getViewDescriptorList();

  @Test
  public void testConstants() {
    assertThat(measurementDescriptorList).isNotEmpty();
    assertThat(viewDescriptorList).isNotEmpty();
  }
}
