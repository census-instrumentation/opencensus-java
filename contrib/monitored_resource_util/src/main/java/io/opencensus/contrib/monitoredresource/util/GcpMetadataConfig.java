/*
 * Copyright 2018, OpenCensus Authors
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

package io.opencensus.contrib.monitoredresource.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;

/**
 * Retrieves Google Cloud project-id and a limited set of instance attributes from Metadata server.
 *
 * @see <a href="https://cloud.google.com/compute/docs/storing-retrieving-metadata">
 *     https://cloud.google.com/compute/docs/storing-retrieving-metadata</a>
 */
final class GcpMetadataConfig {

  private static final String METADATA_URL = "http://metadata/computeMetadata/v1/";

  private GcpMetadataConfig() {}

  static String getProjectId() {
    return getAttribute("project/project-id");
  }

  static String getZone() {
    String zoneId = getAttribute("instance/zone");
    if (zoneId.contains("/")) {
      return zoneId.substring(zoneId.lastIndexOf('/') + 1);
    }
    return zoneId;
  }

  static String getInstanceId() {
    return getAttribute("instance/id");
  }

  static String getClusterName() {
    return getAttribute("instance/attributes/cluster-name");
  }

  private static String getAttribute(String attributeName) {
    try {
      URL url = new URL(METADATA_URL + attributeName);
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      connection.setRequestProperty("Metadata-Flavor", "Google");
      InputStream input = connection.getInputStream();
      if (connection.getResponseCode() == 200) {
        BufferedReader reader = null;
        try {
          reader = new BufferedReader(new InputStreamReader(input, Charset.forName("UTF-8")));
          return reader.readLine();
        } finally {
          if (reader != null) {
            reader.close();
          }
        }
      }
    } catch (IOException ignore) {
      // ignore
    }
    return null;
  }
}
