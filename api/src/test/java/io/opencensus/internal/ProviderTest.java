/*
 * Copyright 2016-17, OpenCensus Authors
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

package io.opencensus.internal;

import static com.google.common.truth.Truth.assertThat;

import java.util.ServiceConfigurationError;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link Provider}. */
@RunWith(JUnit4.class)
public class ProviderTest {
  static class GoodClass {
    public GoodClass() {}
  }

  static class PrivateConstructorClass {
    private PrivateConstructorClass() {}
  }

  static class NoDefaultConstructorClass {
    public NoDefaultConstructorClass(int arg) {}
  }

  private static class PrivateClass {}

  static interface MyInterface {}

  static class MyInterfaceImpl implements MyInterface {
    public MyInterfaceImpl() {}
  }

  @Test(expected = ServiceConfigurationError.class)
  public void createInstance_ThrowsErrorWhenClassIsPrivate() throws ClassNotFoundException {
    Provider.createInstance(
        Class.forName(
            "io.opencensus.internal.ProviderTest$PrivateClass",
            true,
            ProviderTest.class.getClassLoader()),
        PrivateClass.class);
  }

  @Test(expected = ServiceConfigurationError.class)
  public void createInstance_ThrowsErrorWhenClassHasPrivateConstructor()
      throws ClassNotFoundException {
    Provider.createInstance(
        Class.forName(
            "io.opencensus.internal.ProviderTest$PrivateConstructorClass",
            true,
            ProviderTest.class.getClassLoader()),
        PrivateConstructorClass.class);
  }

  @Test(expected = ServiceConfigurationError.class)
  public void createInstance_ThrowsErrorWhenClassDoesNotHaveDefaultConstructor()
      throws ClassNotFoundException {
    Provider.createInstance(
        Class.forName(
            "io.opencensus.internal.ProviderTest$NoDefaultConstructorClass",
            true,
            ProviderTest.class.getClassLoader()),
        NoDefaultConstructorClass.class);
  }

  @Test(expected = ServiceConfigurationError.class)
  public void createInstance_ThrowsErrorWhenClassIsNotASubclass() throws ClassNotFoundException {
    Provider.createInstance(
        Class.forName(
            "io.opencensus.internal.ProviderTest$GoodClass",
            true,
            ProviderTest.class.getClassLoader()),
        MyInterface.class);
  }

  @Test
  public void createInstance_GoodClass() throws ClassNotFoundException {
    assertThat(
            Provider.createInstance(
                Class.forName(
                    "io.opencensus.internal.ProviderTest$GoodClass",
                    true,
                    ProviderTest.class.getClassLoader()),
                GoodClass.class))
        .isNotNull();
  }

  @Test
  public void createInstance_GoodSubclass() throws ClassNotFoundException {
    assertThat(
            Provider.createInstance(
                Class.forName(
                    "io.opencensus.internal.ProviderTest$MyInterfaceImpl",
                    true,
                    ProviderTest.class.getClassLoader()),
                MyInterface.class))
        .isNotNull();
  }
}
