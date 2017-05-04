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

package com.google.instrumentation.tags;

import com.google.instrumentation.internal.logging.Logger;
import com.google.instrumentation.internal.logging.LoggerFactory;

/**
 * Factory for {@link TagSetImpl}s.
 */
public final class TagSetFactoryImpl extends TagSetFactory {

  private final TagSetImpl emptyTagSet;
  private final Logger tagSetLogger;

  /**
   * Creates a {@code TagSetFactoryImpl} which creates {@code TagSet}s that log using the given
   * {@code LoggerFactory}.
   *
   * @param loggerFactory a {@code LoggerFactory} used for {@code TagSet}-related logging.
   * @return a new {@code TagSetFactoryImpl}.
   */
  public static TagSetFactoryImpl create(LoggerFactory loggerFactory) {
    return new TagSetFactoryImpl(loggerFactory);
  }

  private TagSetFactoryImpl(LoggerFactory loggerFactory) {
    this.tagSetLogger = loggerFactory.getLogger(TagSetImpl.class.getName());
    this.emptyTagSet = new TagSetImpl.Builder(tagSetLogger).build();
  }

  @Override
  public TagSetImpl.Builder builder() {
    return new TagSetImpl.Builder(tagSetLogger);
  }

  @Override
  public TagSetImpl empty() {
    return emptyTagSet;
  }
}
