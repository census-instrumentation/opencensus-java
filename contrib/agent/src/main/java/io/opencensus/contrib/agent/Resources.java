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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

import com.google.common.base.Strings;
import com.google.common.io.ByteStreams;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Helper methods for working with resources on the classpath.
 */
final class Resources {

  static File toTempFile(String resourceName) throws IOException {
    checkArgument(!Strings.isNullOrEmpty(resourceName));

    try (InputStream is = Resources.class.getResourceAsStream(resourceName)) {
      checkState(
              is != null,
              "Could not find resource '%s' on the class path. "
              + " Should it be bundled with the agent's JAR? Check the build process!",
              resourceName);
      File file = File.createTempFile(resourceName, ".tmp");
      file.deleteOnExit();
      try (OutputStream os = new FileOutputStream(file)) {
        ByteStreams.copy(is, os);
      }
      return file;
    }
  }
}
