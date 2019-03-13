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

package io.opencensus.metrics.data;

import static com.google.common.truth.Truth.assertThat;

import io.opencensus.common.Timestamp;
import io.opencensus.metrics.data.AttachmentValue.AttachmentValueString;
import java.util.Collections;
import java.util.Map;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link io.opencensus.metrics.data.Exemplar}. */
@RunWith(JUnit4.class)
public class ExemplarTest {

  private static final double TOLERANCE = 1e-6;
  private static final Timestamp TIMESTAMP_1 = Timestamp.create(1, 0);
  private static final AttachmentValue ATTACHMENT_VALUE = AttachmentValueString.create("value");
  private static final Map<String, AttachmentValue> ATTACHMENTS =
      Collections.singletonMap("key", ATTACHMENT_VALUE);

  @Rule public ExpectedException thrown = ExpectedException.none();

  @Test
  public void testExemplar() {
    Exemplar exemplar = Exemplar.create(15.0, TIMESTAMP_1, ATTACHMENTS);
    assertThat(exemplar.getValue()).isWithin(TOLERANCE).of(15.0);
    assertThat(exemplar.getTimestamp()).isEqualTo(TIMESTAMP_1);
    assertThat(exemplar.getAttachments()).isEqualTo(ATTACHMENTS);
  }

  @Test
  public void testExemplar_PreventNullTimestamp() {
    thrown.expect(NullPointerException.class);
    Exemplar.create(15, null, ATTACHMENTS);
  }

  @Test
  public void testExemplar_PreventNullAttachments() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("attachments");
    Exemplar.create(15, TIMESTAMP_1, null);
  }

  @Test
  public void testExemplar_PreventNullAttachmentKey() {
    Map<String, AttachmentValue> attachments = Collections.singletonMap(null, ATTACHMENT_VALUE);
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("key of attachment");
    Exemplar.create(15, TIMESTAMP_1, attachments);
  }

  @Test
  public void testExemplar_PreventNullAttachmentValue() {
    Map<String, AttachmentValue> attachments = Collections.singletonMap("key", null);
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("value of attachment");
    Exemplar.create(15, TIMESTAMP_1, attachments);
  }
}
