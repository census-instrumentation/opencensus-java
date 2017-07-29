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

/**
 * API for associating tags with scoped operations.
 *
 * <p>This package manages a set of tags in the {@code io.grpc.Context}. The tags can be used to
 * label anything that is associated with a specific operation. For example, the {@code
 * io.opencensus.stats} package labels all measurements with the current tags.
 *
 * <p>{@link io.opencensus.tags.Tag Tags} are key-value pairs. The {@link io.opencensus.tags.TagKey
 * keys} are wrapped {@code String}s, but the values can have multiple types, such as {@code
 * String}, {@code long}, and {@code boolean}. They are stored as a map in a {@link
 * io.opencensus.tags.TagContext}.
 */
// TODO(sebright): Add code examples after the API is updated to use a TagContext factory.
package io.opencensus.tags;
