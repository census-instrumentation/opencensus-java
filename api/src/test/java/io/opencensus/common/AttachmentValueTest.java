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

package io.opencensus.common;

import static com.google.common.truth.Truth.assertThat;

import io.opencensus.common.AttachmentValue.AttachmentValueString;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link io.opencensus.common.AttachmentValue}. */
@RunWith(JUnit4.class)
public class AttachmentValueTest {

  @Rule public final ExpectedException thrown = ExpectedException.none();

  @Test
  public void getValue() {
    AttachmentValueString attachmentValue = AttachmentValueString.create("value");
    assertThat(attachmentValue.getValue()).isEqualTo("value");
  }

  @Test
  public void preventNullString() {
    thrown.expect(NullPointerException.class);
    AttachmentValueString.create(null);
  }
}
