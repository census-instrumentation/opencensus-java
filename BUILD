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
    name = "common-core",
    srcs = glob(["core/java/com/google/instrumentation/common/*.java"]),
    deps = ["@jsr305//jar"],
)

java_library(
    name = "stats-core",
    srcs = glob(["core/java/com/google/instrumentation/stats/*.java"]),
    deps = [
        ":common-core",
        "@jsr305//jar",
    ],
)

java_library(
    name = "stats-core_native",
    srcs = glob(["core_native/java/com/google/instrumentation/stats/*.java"]),
    deps = [
        ":stats-core",
        "//proto:census_context-proto-java",
        "@jsr305//jar",
        "@protobuf//jar",
    ],
)

java_binary(
    name = "CensusRunner",
    srcs = ["examples/java/com/google/instrumentation/stats/CensusRunner.java"],
    main_class = "com.google.instrumentation.stats.CensusRunner",
    deps = [
        ":stats-core",
        ":stats-core_native",
        "@grpc_context//jar",
        "@guava//jar",
        "@jsr305//jar",
    ],
)

java_test(
    name = "AggregationDescriptorTest",
    srcs = ["core/javatests/com/google/instrumentation/stats/AggregationDescriptorTest.java"],
    deps = [
        ":common-core",
        ":stats-core",
        "@guava//jar",
        "@guava_testlib//jar",
        "@jsr305//jar",
        "@junit//jar",
        "@truth//jar",
    ],
)

java_test(
    name = "CensusContextTest",
    srcs = ["core/javatests/com/google/instrumentation/stats/CensusContextTest.java"],
    deps = [
        ":stats-core",
        ":stats-core_native",
        "@guava//jar",
        "@guava_testlib//jar",
        "@jsr305//jar",
        "@junit//jar",
        "@truth//jar",
    ],
)

java_test(
    name = "CensusContextFactoryTest",
    srcs = ["core/javatests/com/google/instrumentation/stats/CensusContextFactoryTest.java"],
    deps = [
        ":stats-core",
        ":stats-core_native",
        "@guava//jar",
        "@jsr305//jar",
        "@junit//jar",
        "@truth//jar",
    ],
)

java_test(
    name = "MeasurementMapTest",
    srcs = ["core/javatests/com/google/instrumentation/stats/MeasurementMapTest.java"],
    deps = [
        ":stats-core",
        "@guava//jar",
        "@jsr305//jar",
        "@junit//jar",
        "@truth//jar",
    ],
)

java_test(
    name = "MeasurementDescriptorTest",
    srcs = ["core/javatests/com/google/instrumentation/stats/MeasurementDescriptorTest.java"],
    deps = [
        ":stats-core",
        "@guava//jar",
        "@guava_testlib//jar",
        "@jsr305//jar",
        "@junit//jar",
        "@truth//jar",
    ],
)

java_test(
    name = "RpcConstantsTest",
    srcs = ["core/javatests/com/google/instrumentation/stats/RpcConstantsTest.java"],
    deps = [
        ":stats-core",
        ":stats-core_native",
        "@guava//jar",
        "@jsr305//jar",
        "@junit//jar",
        "@truth//jar",
    ],
)

java_test(
    name = "StringUtilTest",
    srcs = ["core/javatests/com/google/instrumentation/stats/StringUtilTest.java"],
    deps = [
        ":stats-core",
        ":stats-core_native",
        "@guava//jar",
        "@guava_testlib//jar",
        "@jsr305//jar",
        "@junit//jar",
        "@truth//jar",
    ],
)

java_test(
    name = "TagKeyTest",
    srcs = ["core/javatests/com/google/instrumentation/stats/TagKeyTest.java"],
    deps = [
        ":stats-core",
        ":stats-core_native",
        "@guava//jar",
        "@guava_testlib//jar",
        "@jsr305//jar",
        "@junit//jar",
        "@truth//jar",
    ],
)

java_test(
    name = "TagValueTest",
    srcs = ["core/javatests/com/google/instrumentation/stats/TagValueTest.java"],
    deps = [
        ":stats-core",
        ":stats-core_native",
        "@guava//jar",
        "@guava_testlib//jar",
        "@jsr305//jar",
        "@junit//jar",
        "@truth//jar",
    ],
)

java_test(
    name = "DurationTest",
    srcs = ["core/javatests/com/google/instrumentation/common/DurationTest.java"],
    deps = [
        ":common-core",
        "@guava//jar",
        "@guava_testlib//jar",
        "@jsr305//jar",
        "@junit//jar",
        "@truth//jar",
    ],
)

java_test(
    name = "ProviderTest",
    srcs = ["core/javatests/com/google/instrumentation/common/ProviderTest.java"],
    deps = [
        ":common-core",
        ":stats-core",
        "@guava//jar",
        "@jsr305//jar",
        "@junit//jar",
        "@truth//jar",
    ],
)

java_test(
    name = "TimestampTest",
    srcs = ["core/javatests/com/google/instrumentation/common/TimestampTest.java"],
    deps = [
        ":common-core",
        "@guava//jar",
        "@guava_testlib//jar",
        "@jsr305//jar",
        "@junit//jar",
        "@truth//jar",
    ],
)

java_test(
    name = "ViewDescriptorTest",
    srcs = ["core/javatests/com/google/instrumentation/stats/ViewDescriptorTest.java"],
    deps = [
        ":common-core",
        ":stats-core",
        "@guava//jar",
        "@guava_testlib//jar",
        "@jsr305//jar",
        "@junit//jar",
        "@truth//jar",
    ],
)
