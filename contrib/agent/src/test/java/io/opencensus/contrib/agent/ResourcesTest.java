/*
 * Copyright 2017, Google Inc.
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

package io.opencensus.contrib.agent;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.google.common.base.Charsets;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Unit tests for {@link Resources}.
 */
@RunWith(JUnit4.class)
public class ResourcesTest {

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @Test
  public void getResourceAsTempFile() throws IOException {
    File mockFile = mock(File.class);
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();

    Resources.getResourceAsTempFile("some_resource.txt", mockFile, bytes);

    verify(mockFile).deleteOnExit();
    assertThat(bytes.toString(Charsets.UTF_8.name())).isEqualTo("A resource!");
  }

  @Test
  public void getResourceAsTempFile_Missing() throws IOException {
    exception.expect(FileNotFoundException.class);

    Resources.getResourceAsTempFile("missing_resource.txt",
            mock(File.class), new ByteArrayOutputStream());
  }
}
