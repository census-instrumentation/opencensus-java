package(default_visibility = ["//visibility:public"])

licenses(["notice"])  # BSD

exports_files(["LICENSE"])

java_binary(
    name = "CensusClient",
    srcs = [
        "CensusApplication.java",
        "CensusClient.java",
        "CensusClientz.java",
    ],
    main_class = "com.google.census.examples.gol.CensusClient",
    deps = [
        "//java/com/google/common/base",
        "//java/com/google/common/flags",
        "//java/com/google/monitoring/runtime:hooks",
        "//third_party/java/grpc:core",
        "//third_party/java_src/census",
        "//third_party/java_src/census:census_google3",
        "//third_party/java_src/census:census_grpc",
        "//third_party/java_src/census/examples/java/com/google/census/examples:gol_proto",
        "//third_party/java_src/census/examples/java/com/google/census/examples:gol_proto_grpc",
    ],
)

java_binary(
    name = "CensusServer",
    srcs = [
        "CensusServer.java",
        "GameOfLife.java",
    ],
    main_class = "com.google.census.examples.gol.CensusServer",
    deps = [
        "//java/com/google/common/flags",
        "//third_party/java/grpc:core",
        "//third_party/java/grpc:stub",
        "//third_party/java_src/census",
        "//third_party/java_src/census:census_google3",
        "//third_party/java_src/census:census_grpc",
        "//third_party/java_src/census/examples/java/com/google/census/examples:gol_proto",
        "//third_party/java_src/census/examples/java/com/google/census/examples:gol_proto_grpc",
    ],
)
