# Description:
#   Open source Census for cloud services.

java_library(
    name = "census-core",
    srcs = glob(["core/java/com/google/census/*.java"]),
    deps = [
        "@guava//jar",
        "@jsr305//jar",
    ],
)

java_library(
    name = "census-core_native",
    srcs = glob(["core_native/java/com/google/census/*.java"]),
    deps = [
        ":census-core",
        "@guava//jar",
        "@jsr305//jar",
    ],
)

java_binary(
    name = "CensusRunner",
    srcs = ["examples/java/com/google/census/CensusRunner.java"],
    main_class = "com.google.census.CensusRunner",
    deps = [
        ":census-core",
        ":census-core_native",
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
    name = "CensusScopeTest",
    srcs = ["core/javatests/com/google/census/CensusScopeTest.java"],
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
        "@jsr305//jar",
        "@junit//jar",
        "@truth//jar",
    ],
)

java_test(
    name = "TagMapTest",
    srcs = ["core/javatests/com/google/census/TagMapTest.java"],
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
    name = "TagTest",
    srcs = ["core/javatests/com/google/census/TagTest.java"],
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
    name = "TagValueTest",
    srcs = ["core/javatests/com/google/census/TagValueTest.java"],
    deps = [
        ":census-core",
        ":census-core_native",
        "@guava//jar",
        "@jsr305//jar",
        "@junit//jar",
        "@truth//jar",
    ],
)
