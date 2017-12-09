# The following dependencies were calculated from:
#
# generate_workspace --artifact=io.opencensus:opencensus-api:0.10.1 --artifact=io.opencensus:opencensus-contrib-zpages:0.10.1 --artifact=io.opencensus:opencensus-exporter-trace-logging:0.10.1 --artifact=io.opencensus:opencensus-impl:0.10.1 --repositories=http://repo.maven.apache.org/maven2

def opencensus_maven_jars():
  # io.opencensus:opencensus-contrib-zpages:jar:0.10.1 got requested version
  # io.opencensus:opencensus-impl-core:jar:0.10.1 got requested version
  # io.opencensus:opencensus-exporter-trace-logging:jar:0.10.1 got requested version
  # io.opencensus:opencensus-impl:jar:0.10.1 got requested version
  # io.opencensus:opencensus-api:jar:0.10.1
  native.maven_jar(
      name = "com_google_code_findbugs_jsr305",
      artifact = "com.google.code.findbugs:jsr305:3.0.1",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "f7be08ec23c21485b9b5a1cf1654c2ec8c58168d",
  )


  # io.opencensus:opencensus-api:jar:0.10.1
  native.maven_jar(
      name = "io_grpc_grpc_context",
      artifact = "io.grpc:grpc-context:1.8.0",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "7fe8214b8d1141afadbe0bcad751df2b8fe2e078",
  )


  native.maven_jar(
      name = "io_opencensus_opencensus_exporter_trace_logging",
      artifact = "io.opencensus:opencensus-exporter-trace-logging:0.10.1",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "c9d934614bb0237a39eda79d52d245626ef32d71",
  )


  # io.opencensus:opencensus-contrib-zpages:jar:0.10.1 got requested version
  # io.opencensus:opencensus-impl-core:jar:0.10.1 got requested version
  # io.opencensus:opencensus-exporter-trace-logging:jar:0.10.1 got requested version
  # io.opencensus:opencensus-impl:jar:0.10.1 got requested version
  # io.opencensus:opencensus-api:jar:0.10.1
  native.maven_jar(
      name = "com_google_errorprone_error_prone_annotations",
      artifact = "com.google.errorprone:error_prone_annotations:2.1.2",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "6dcc08f90f678ac33e5ef78c3c752b6f59e63e0c",
  )


  native.maven_jar(
      name = "io_opencensus_opencensus_contrib_zpages",
      artifact = "io.opencensus:opencensus-contrib-zpages:0.10.1",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "4e2cf2a5f05bffb8dedbc8f183f127cd46cd4a3f",
  )


  # io.opencensus:opencensus-impl:jar:0.10.1
  native.maven_jar(
      name = "com_lmax_disruptor",
      artifact = "com.lmax:disruptor:3.3.6",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "09bfca4ee4f691f3737b3f4f006d0c4770f178eb",
  )


  # io.opencensus:opencensus-contrib-zpages:jar:0.10.1 got requested version
  # io.opencensus:opencensus-impl-core:jar:0.10.1 got requested version
  # io.opencensus:opencensus-exporter-trace-logging:jar:0.10.1 got requested version
  # io.opencensus:opencensus-api:jar:0.10.1
  native.maven_jar(
      name = "com_google_guava_guava",
      artifact = "com.google.guava:guava:19.0",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "6ce200f6b23222af3d8abb6b6459e6c44f4bb0e9",
  )


  # io.opencensus:opencensus-contrib-zpages:jar:0.10.1 got requested version
  # io.opencensus:opencensus-impl-core:jar:0.10.1 got requested version
  # io.opencensus:opencensus-exporter-trace-logging:jar:0.10.1 got requested version
  # io.opencensus:opencensus-impl:jar:0.10.1 got requested version
  native.maven_jar(
      name = "io_opencensus_opencensus_api",
      artifact = "io.opencensus:opencensus-api:0.10.1",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "1f5319c45aa3f4c879805f2a323d1ab6ba42e334",
  )


  # io.opencensus:opencensus-impl:jar:0.10.1
  native.maven_jar(
      name = "io_opencensus_opencensus_impl_core",
      artifact = "io.opencensus:opencensus-impl-core:0.10.1",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "66b221c4388e3208000422954b4ba0248f7746ca",
  )


  native.maven_jar(
      name = "io_opencensus_opencensus_impl",
      artifact = "io.opencensus:opencensus-impl:0.10.1",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "6a96e07455946d43608a9bde4ed0663cd7c0fe56",
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
