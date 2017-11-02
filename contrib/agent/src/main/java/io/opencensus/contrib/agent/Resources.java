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

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.google.common.io.ByteStreams;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/** Helper methods for working with resources. */
final class Resources {

  /**
   * Returns a resource of the given name as a temporary file.
   *
   * @param resourceName name of the resource
   * @return a temporary {@link File} containing a copy of the resource
   * @throws FileNotFoundException if no resource of the given name is found
   * @throws IOException if an I/O error occurs
   */
  static File getResourceAsTempFile(String resourceName) throws IOException {
    checkArgument(!Strings.isNullOrEmpty(resourceName), "resourceName");

    File file = File.createTempFile(resourceName, ".tmp");
    try (OutputStream os = new FileOutputStream(file)) {
      getResourceAsTempFile(resourceName, file, os);
      return file;
    }
  }

  @VisibleForTesting
  static void getResourceAsTempFile(String resourceName, File file, OutputStream outputStream)
      throws IOException {
    file.deleteOnExit();

    InputStream is = getResourceAsStream(resourceName);
    try {
      ByteStreams.copy(is, outputStream);
    } finally {
      is.close();
    }
  }

  private static InputStream getResourceAsStream(String resourceName) throws FileNotFoundException {
    InputStream is = Resources.class.getResourceAsStream(resourceName);
    if (is == null) {
      throw new FileNotFoundException(
          "Cannot find resource '" + resourceName + "' on the class path.");
    }
    return is;
  }
}
