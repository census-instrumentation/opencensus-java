# Copyright 2016, Google Inc.
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#     http://www.apache.org/licenses/LICENSE-2.0
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

maven_jar(
    name = "grpc_context",
    artifact = "io.grpc:grpc-context:1.0.1",
)

maven_jar(
    name = "guava",
    artifact = "com.google.guava:guava:19.0",
)

maven_jar(
    name = "jsr305",
    artifact = "com.google.code.findbugs:jsr305:3.0.0",
)

# proto_library rules implicitly depend on @com_google_protobuf//:protoc,
# which is the proto-compiler.
# This statement defines the @com_google_protobuf repo.
http_archive(
    name = "com_google_protobuf",
    sha256 = "ff771a662fb6bd4d3cc209bcccedef3e93980a49f71df1e987f6afa3bcdcba3a",
    strip_prefix = "protobuf-b4b0e304be5a68de3d0ee1af9b286f958750f5e4",
    urls = ["https://github.com/google/protobuf/archive/b4b0e304be5a68de3d0ee1af9b286f958750f5e4.zip"],
)

# java_proto_library rules implicitly depend on @com_google_protobuf_java//:java_toolchain,
# which is the Java proto runtime (base classes and common utilities).
http_archive(
    name = "com_google_protobuf_java",
    sha256 = "ff771a662fb6bd4d3cc209bcccedef3e93980a49f71df1e987f6afa3bcdcba3a",
    strip_prefix = "protobuf-b4b0e304be5a68de3d0ee1af9b286f958750f5e4",
    urls = ["https://github.com/google/protobuf/archive/b4b0e304be5a68de3d0ee1af9b286f958750f5e4.zip"],
)

maven_jar(
    name = "protobuf",
    artifact = "com.google.protobuf:protobuf-java:3.2.0",
)

# Test dependencies

maven_jar(
    name = "guava_testlib",
    artifact = "com.google.guava:guava-testlib:19.0",
)

maven_jar(
    name = "truth",
    artifact = "com.google.truth:truth:0.30",
)

maven_jar(
    name = "mockito",
    artifact = "org.mockito:mockito-all:1.9.5",
)

maven_jar(
    name = "junit",
    artifact = "junit:junit:4.11",
)

maven_jar(
    name = "jmh",
    artifact = "org.openjdk.jmh:jmh-core:1.18",
)

git_repository(
    name = "io_bazel",
    remote = "https://github.com/bazelbuild/bazel",
    tag = "0.4.5",
)
