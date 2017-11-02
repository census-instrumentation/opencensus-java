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

package io.opencensus.contrib.agent;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.verify;

import com.google.common.base.Charsets;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/** Unit tests for {@link Resources}. */
@RunWith(MockitoJUnitRunner.class)
public class ResourcesTest {

  @Rule public final ExpectedException exception = ExpectedException.none();

  @Mock private File mockFile;

  @Test
  public void getResourceAsTempFile_deleteOnExit() throws IOException {
    Resources.getResourceAsTempFile("some_resource.txt", mockFile, new ByteArrayOutputStream());

    verify(mockFile).deleteOnExit();
  }

  @Test
  public void getResourceAsTempFile_contents() throws IOException {
    File file = Resources.getResourceAsTempFile("some_resource.txt");

    assertThat(new String(java.nio.file.Files.readAllBytes(file.toPath()), Charsets.UTF_8))
        .isEqualTo("A resource!");
  }

  @Test
  public void getResourceAsTempFile_empty() throws IOException {
    exception.expect(IllegalArgumentException.class);

    Resources.getResourceAsTempFile("");
  }

  @Test
  public void getResourceAsTempFile_Missing() throws IOException {
    exception.expect(FileNotFoundException.class);

    Resources.getResourceAsTempFile("missing_resource.txt");
  }

  @Test
  public void getResourceAsTempFile_WriteFailure() throws IOException {
    OutputStream badOutputStream =
        new OutputStream() {
          @Override
          public void write(int b) throws IOException {
            throw new IOException("denied");
          }
        };

    exception.expect(IOException.class);
    exception.expectMessage("denied");

    Resources.getResourceAsTempFile("some_resource.txt", mockFile, badOutputStream);
  }
}
