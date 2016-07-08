# Description:
#   Open source Census for cloud services.

# _DEP_MAP = {
#     '//java/com/google/common/annotations': '//external:guava-jar',
#     '//java/com/google/common/base': '//external:guava-jar',
#     '//java/com/google/common/collect': '//external:guava-jar',
#     '//java/com/google/common/flags': '//external:guava-jar',
#     '//java/com/google/common/io': '//external:guava-jar',
#     '//java/com/google/common/inject': '//external:guava-jar',
#     '//java/com/google/common/labs/collect': '//external:guava-jar',
#     '//java/com/google/common/primitives': '//external:guava-jar',
#     '//java/com/google/common/util/concurrent': '//external:guava-jar',
#     '//third_party/java/android/android_sdk_linux:dx': '//external:dx-jar',
#     '//third_party/java/asm': '//external:asm-jar',
#     '//third_party/java/asm:asm-util': '//external:asm-jar',
#     '//third_party/java/auto:auto_value': '//third_party:auto_value',
#     '//third_party/java/checker_framework:annotations': '//external:checkerframework-jar',
#     '//third_party/java/fastutil': '//external:fastutil-jar',
#     '//third_party/java/guice': '//external:guice-jar',
#     '//third_party/java/guice:guice-multibindings': '//external:guice-multibindings-jar',
#     '//third_party/java/jsr305_annotations': '//external:jsr305-jar',
#     '//third_party/java/jsr330_inject': '//external:javax_inject-jar',
# }

java_library(
    name = "census",
    srcs = glob(["core/java/com/google/census/*.java"]),
    deps = [
        "@guava//jar",
        "@jsr305//jar",
    ],
)

java_library(
    name = "census_native",
    srcs = glob(["core_native/java/com/google/census/*.java"]),
    deps = [
        ":census",
        "@guava//jar",
        "@jsr305//jar",
    ],
)

java_binary(
    name = "census-runner",
    srcs = ["CensusRunner.java"],
    main_class = "com.google.census.CensusRunner",
    deps = [
        ":census",
        ":census_native",
        "@guava//jar",
        "@jsr305//jar",
    ],
)

java_test(
    name = "CensusContextTest",
    srcs = ["core/javatests/com/google/census/CensusContextTest.java"],
    deps = [
        ":census",
        ":census_native",
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
        ":census",
        ":census_native",
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
        ":census",
        ":census_native",
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
        ":census",
        ":census_native",
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
        ":census",
        ":census_native",
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
        ":census",
        ":census_native",
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
        ":census",
        ":census_native",
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
        ":census",
        ":census_native",
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
        ":census",
        ":census_native",
        "@guava//jar",
        "@jsr305//jar",
        "@junit//jar",
        "@truth//jar",
    ],
)
