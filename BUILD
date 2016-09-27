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

# Description:
#   Open source Census for cloud services.

java_library(
    name = "census-core",
    srcs = glob(["core/java/com/google/census/*.java"]),
    deps = ["@jsr305//jar"],
)

java_library(
    name = "census-core_native",
    srcs = glob(["core_native/java/com/google/census/*.java"]),
    deps = [
        ":census-core",
        "//proto:census_context-proto-java",
        "@jsr305//jar",
        "@protobuf//jar",
    ],
)

java_library(
    name = "census-grpc",
    srcs = glob(["grpc/java/com/google/census/*.java"]),
    deps = [
        ":census-core",
        "@grpc_context//jar",
        "@jsr305//jar",
    ],
)

java_library(
    name = "census-grpc_native",
    srcs = glob(["grpc_native/java/com/google/census/*.java"]),
    deps = [
        ":census-core",
        ":census-grpc",
        "@grpc_context//jar",
        "@jsr305//jar",
    ],
)

java_binary(
    name = "CensusRunner",
    srcs = ["examples/java/com/google/census/CensusRunner.java"],
    main_class = "com.google.census.examples.CensusRunner",
    deps = [
        ":census-core",
        ":census-core_native",
        ":census-grpc",
        ":census-grpc_native",
        "@grpc_context//jar",
        "@guava//jar",
        "@jsr305//jar",
    ],
)

java_test(
    name = "CensusContextTest",
    srcs = ["core/javatests/com/google/census/CensusContextTest.java"],
    deps = [
        ":census-core",
        ":census-core_native",
        "@guava//jar",
        "@guava_testlib//jar",
        "@jsr305//jar",
        "@junit//jar",
        "@truth//jar",
    ],
)

java_test(
    name = "CensusContextFactoryTest",
    srcs = ["core/javatests/com/google/census/CensusContextFactoryTest.java"],
    deps = [
        ":census-core",
        ":census-core_native",
        "@guava//jar",
        "@jsr305//jar",
        "@junit//jar",
        "@truth//jar",
    ],
)

java_test(
    name = "CensusGrpcContextTest",
    srcs = ["grpc/javatests/com/google/census/CensusGrpcContextTest.java"],
    deps = [
        ":census-core",
        ":census-core_native",
        ":census-grpc",
        ":census-grpc_native",
        "@grpc_context//jar",
        "@guava//jar",
        "@jsr305//jar",
        "@junit//jar",
        "@truth//jar",
    ],
)

java_test(
    name = "MetricMapTest",
    srcs = ["core/javatests/com/google/census/MetricMapTest.java"],
    deps = [
        ":census-core",
        ":census-core_native",
        "@guava//jar",
        "@jsr305//jar",
        "@junit//jar",
        "@truth//jar",
    ],
)

java_test(
    name = "MetricNameTest",
    srcs = ["core/javatests/com/google/census/MetricNameTest.java"],
    deps = [
        ":census-core",
        ":census-core_native",
        "@guava//jar",
        "@guava_testlib//jar",
        "@jsr305//jar",
        "@junit//jar",
        "@truth//jar",
    ],
)

java_test(
    name = "ProviderTest",
    srcs = ["core/javatests/com/google/census/ProviderTest.java"],
    deps = [
        ":census-core",
        ":census-core_native",
        "@guava//jar",
        "@jsr305//jar",
        "@junit//jar",
        "@truth//jar",
    ],
)

java_test(
    name = "RpcConstantsTest",
    srcs = ["core/javatests/com/google/census/RpcConstantsTest.java"],
    deps = [
        ":census-core",
        ":census-core_native",
        "@guava//jar",
        "@jsr305//jar",
        "@junit//jar",
        "@truth//jar",
    ],
)

java_test(
    name = "TagKeyTest",
    srcs = ["core/javatests/com/google/census/TagKeyTest.java"],
    deps = [
        ":census-core",
        ":census-core_native",
        "@guava//jar",
        "@guava_testlib//jar",
        "@jsr305//jar",
        "@junit//jar",
        "@truth//jar",
    ],
)

java_test(
    name = "TagTest",
    srcs = ["core/javatests/com/google/census/TagTest.java"],
    deps = [
        ":census-core",
        ":census-core_native",
        "@guava//jar",
        "@guava_testlib//jar",
        "@jsr305//jar",
        "@junit//jar",
        "@truth//jar",
    ],
)

java_test(
    name = "TagValueTest",
    srcs = ["core/javatests/com/google/census/TagValueTest.java"],
    deps = [
        ":census-core",
        ":census-core_native",
        "@guava//jar",
        "@guava_testlib//jar",
        "@jsr305//jar",
        "@junit//jar",
        "@truth//jar",
    ],
)
