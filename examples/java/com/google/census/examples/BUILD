load("//third_party/java/grpc:build_defs.bzl", "grpc_java_library")

package(default_visibility = ["//visibility:public"])

licenses(["notice"])  # BSD

exports_files(["LICENSE"])

proto_library(
    name = "gol_proto",
    srcs = ["gol.proto"],
    has_services = 1,
    java_api_version = 2,
)

grpc_java_library(
    name = "gol_proto_grpc",
    src = "gol.proto",
    proto_deps = [":gol_proto"],
)
