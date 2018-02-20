# The following dependencies were calculated from:
#
# generate_workspace --artifact=io.opencensus:opencensus-api:0.12.0 --artifact=io.opencensus:opencensus-contrib-zpages:0.12.0 --artifact=io.opencensus:opencensus-exporter-trace-logging:0.12.0 --artifact=io.opencensus:opencensus-impl:0.12.0 --repositories=http://repo.maven.apache.org/maven2


def opencensus_maven_jars():
  # io.opencensus:opencensus-impl-core:jar:0.12.0 got requested version
  # io.opencensus:opencensus-contrib-zpages:jar:0.12.0 got requested version
  # io.opencensus:opencensus-contrib-grpc-metrics:jar:0.12.0 got requested version
  # io.opencensus:opencensus-impl:jar:0.12.0 got requested version
  # io.opencensus:opencensus-exporter-trace-logging:jar:0.12.0 got requested version
  # io.opencensus:opencensus-api:jar:0.12.0
  native.maven_jar(
      name = "com_google_code_findbugs_jsr305",
      artifact = "com.google.code.findbugs:jsr305:3.0.1",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "f7be08ec23c21485b9b5a1cf1654c2ec8c58168d",
  )


  # io.opencensus:opencensus-api:jar:0.12.0
  native.maven_jar(
      name = "io_grpc_grpc_context",
      artifact = "io.grpc:grpc-context:1.9.0",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "28b0836f48c9705abf73829bbc536dba29a1329a",
  )


  native.maven_jar(
      name = "io_opencensus_opencensus_exporter_trace_logging",
      artifact = "io.opencensus:opencensus-exporter-trace-logging:0.12.0",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "593a6b4211d616aec55748bcf08811cea8193bc2",
  )


  # io.opencensus:opencensus-impl-core:jar:0.12.0 got requested version
  # io.opencensus:opencensus-contrib-zpages:jar:0.12.0 got requested version
  # io.opencensus:opencensus-contrib-grpc-metrics:jar:0.12.0 got requested version
  # io.opencensus:opencensus-impl:jar:0.12.0 got requested version
  # io.opencensus:opencensus-exporter-trace-logging:jar:0.12.0 got requested version
  # io.opencensus:opencensus-api:jar:0.12.0
  native.maven_jar(
      name = "com_google_errorprone_error_prone_annotations",
      artifact = "com.google.errorprone:error_prone_annotations:2.2.0",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "88e3c593e9b3586e1c6177f89267da6fc6986f0c",
  )


  native.maven_jar(
      name = "io_opencensus_opencensus_contrib_zpages",
      artifact = "io.opencensus:opencensus-contrib-zpages:0.12.0",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "fc87ede80a6d2a9a64a63c4d2825a515a6583d8b",
  )


  # io.opencensus:opencensus-impl:jar:0.12.0
  native.maven_jar(
      name = "com_lmax_disruptor",
      artifact = "com.lmax:disruptor:3.3.6",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "09bfca4ee4f691f3737b3f4f006d0c4770f178eb",
  )


  # io.opencensus:opencensus-impl-core:jar:0.12.0 got requested version
  # io.opencensus:opencensus-contrib-zpages:jar:0.12.0 got requested version
  # io.opencensus:opencensus-exporter-trace-logging:jar:0.12.0 got requested version
  # io.opencensus:opencensus-api:jar:0.12.0
  native.maven_jar(
      name = "com_google_guava_guava",
      artifact = "com.google.guava:guava:19.0",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "6ce200f6b23222af3d8abb6b6459e6c44f4bb0e9",
  )


  # io.opencensus:opencensus-contrib-zpages:jar:0.12.0
  native.maven_jar(
      name = "io_opencensus_opencensus_contrib_grpc_metrics",
      artifact = "io.opencensus:opencensus-contrib-grpc-metrics:0.12.0",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "35064e8624f9abce15846eeb071979191d0c9062",
  )


  # io.opencensus:opencensus-impl-core:jar:0.12.0 got requested version
  # io.opencensus:opencensus-contrib-zpages:jar:0.12.0 got requested version
  # io.opencensus:opencensus-contrib-grpc-metrics:jar:0.12.0 got requested version
  # io.opencensus:opencensus-impl:jar:0.12.0 got requested version
  # io.opencensus:opencensus-exporter-trace-logging:jar:0.12.0 got requested version
  native.maven_jar(
      name = "io_opencensus_opencensus_api",
      artifact = "io.opencensus:opencensus-api:0.12.0",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "047de3fd42920c8114b3516c0d5a498e4c503ff4",
  )


  # io.opencensus:opencensus-impl:jar:0.12.0
  native.maven_jar(
      name = "io_opencensus_opencensus_impl_core",
      artifact = "io.opencensus:opencensus-impl-core:0.12.0",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "e1c83051de4331a689a541920803f0de3af59870",
  )


  native.maven_jar(
      name = "io_opencensus_opencensus_impl",
      artifact = "io.opencensus:opencensus-impl:0.12.0",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "ae3f50ab9b1ae9cdbd29ebb1449d1190b62916b4",
  )




def opencensus_java_libraries():
  native.java_library(
      name = "com_google_code_findbugs_jsr305",
      visibility = ["//visibility:public"],
      exports = ["@com_google_code_findbugs_jsr305//jar"],
  )


  native.java_library(
      name = "io_grpc_grpc_context",
      visibility = ["//visibility:public"],
      exports = ["@io_grpc_grpc_context//jar"],
  )


  native.java_library(
      name = "io_opencensus_opencensus_exporter_trace_logging",
      visibility = ["//visibility:public"],
      exports = ["@io_opencensus_opencensus_exporter_trace_logging//jar"],
      runtime_deps = [
          ":com_google_code_findbugs_jsr305",
          ":com_google_errorprone_error_prone_annotations",
          ":com_google_guava_guava",
          ":io_opencensus_opencensus_api",
      ],
  )


  native.java_library(
      name = "com_google_errorprone_error_prone_annotations",
      visibility = ["//visibility:public"],
      exports = ["@com_google_errorprone_error_prone_annotations//jar"],
  )


  native.java_library(
      name = "io_opencensus_opencensus_contrib_zpages",
      visibility = ["//visibility:public"],
      exports = ["@io_opencensus_opencensus_contrib_zpages//jar"],
      runtime_deps = [
          ":com_google_code_findbugs_jsr305",
          ":com_google_errorprone_error_prone_annotations",
          ":com_google_guava_guava",
          ":io_opencensus_opencensus_api",
          ":io_opencensus_opencensus_contrib_grpc_metrics",
      ],
  )


  native.java_library(
      name = "com_lmax_disruptor",
      visibility = ["//visibility:public"],
      exports = ["@com_lmax_disruptor//jar"],
  )


  native.java_library(
      name = "com_google_guava_guava",
      visibility = ["//visibility:public"],
      exports = ["@com_google_guava_guava//jar"],
  )


  native.java_library(
      name = "io_opencensus_opencensus_contrib_grpc_metrics",
      visibility = ["//visibility:public"],
      exports = ["@io_opencensus_opencensus_contrib_grpc_metrics//jar"],
      runtime_deps = [
          ":com_google_code_findbugs_jsr305",
          ":com_google_errorprone_error_prone_annotations",
          ":io_opencensus_opencensus_api",
      ],
  )


  native.java_library(
      name = "io_opencensus_opencensus_api",
      visibility = ["//visibility:public"],
      exports = ["@io_opencensus_opencensus_api//jar"],
      runtime_deps = [
          ":com_google_code_findbugs_jsr305",
          ":com_google_errorprone_error_prone_annotations",
          ":com_google_guava_guava",
          ":io_grpc_grpc_context",
      ],
  )


  native.java_library(
      name = "io_opencensus_opencensus_impl_core",
      visibility = ["//visibility:public"],
      exports = ["@io_opencensus_opencensus_impl_core//jar"],
      runtime_deps = [
          ":com_google_code_findbugs_jsr305",
          ":com_google_errorprone_error_prone_annotations",
          ":com_google_guava_guava",
          ":io_opencensus_opencensus_api",
      ],
  )


  native.java_library(
      name = "io_opencensus_opencensus_impl",
      visibility = ["//visibility:public"],
      exports = ["@io_opencensus_opencensus_impl//jar"],
      runtime_deps = [
          ":com_google_code_findbugs_jsr305",
          ":com_google_errorprone_error_prone_annotations",
          ":com_google_guava_guava",
          ":com_lmax_disruptor",
          ":io_opencensus_opencensus_api",
          ":io_opencensus_opencensus_impl_core",
      ],
  )


