# The following dependencies were calculated from:
#
# generate_workspace --artifact=io.opencensus:opencensus-api:0.12.2 --artifact=io.opencensus:opencensus-contrib-zpages:0.12.2 --artifact=io.opencensus:opencensus-exporter-trace-logging:0.12.2 --artifact=io.opencensus:opencensus-impl:0.12.2 --artifact=io.opencensus:opencensus-contrib-grpc-metrics:0.12.2 --artifact=io.opencensus:opencensus-exporter-trace-stackdriver:0.12.2 --artifact=io.opencensus:opencensus-exporter-stats-stackdriver:0.12.2 --artifact=io.opencensus:opencensus-exporter-stats-prometheus:0.12.2 --artifact=io.prometheus:simpleclient_httpserver:0.3.0 --artifact=io.grpc:grpc-all:1.9.0 --artifact=com.google.guava:guava:23.0 --repositories=http://repo.maven.apache.org/maven2


def opencensus_maven_jars():
  # io.opencensus:opencensus-impl-core:jar:0.12.2 got requested version
  # io.opencensus:opencensus-contrib-zpages:jar:0.12.2 got requested version
  # io.opencensus:opencensus-exporter-stats-stackdriver:jar:0.12.2 got requested version
  # io.opencensus:opencensus-exporter-stats-prometheus:jar:0.12.2 got requested version
  # io.opencensus:opencensus-contrib-grpc-metrics:jar:0.12.2 got requested version
  # io.opencensus:opencensus-api:jar:0.12.2
  # io.opencensus:opencensus-exporter-trace-logging:jar:0.12.2 got requested version
  # io.opencensus:opencensus-impl:jar:0.12.2 got requested version
  # io.opencensus:opencensus-exporter-trace-stackdriver:jar:0.12.2 got requested version
  native.maven_jar(
      name = "com_google_code_findbugs_jsr305",
      artifact = "com.google.code.findbugs:jsr305:3.0.1",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "f7be08ec23c21485b9b5a1cf1654c2ec8c58168d",
  )


  # com.google.api:gax-grpc:jar:1.17.0 got requested version
  # com.google.api:gax:jar:1.17.0
  native.maven_jar(
      name = "com_google_auth_google_auth_library_oauth2_http",
      artifact = "com.google.auth:google-auth-library-oauth2-http:0.9.0",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "04e6152c3aead24148627e84f5651e79698c00d9",
  )


  # io.grpc:grpc-protobuf:jar:1.9.0
  native.maven_jar(
      name = "io_grpc_grpc_protobuf_lite",
      artifact = "io.grpc:grpc-protobuf-lite:1.9.0",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "9dc9c6531ae0b304581adff0e9b7cff21a4073ac",
  )


  native.maven_jar(
      name = "io_opencensus_opencensus_exporter_stats_prometheus",
      artifact = "io.opencensus:opencensus-exporter-stats-prometheus:0.12.2",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "e7a2711b186ae7ca22f934b67c6d8a3d10bab5fa",
  )


  # io.grpc:grpc-netty:jar:1.9.0
  native.maven_jar(
      name = "io_netty_netty_handler_proxy",
      artifact = "io.netty:netty-handler-proxy:4.1.17.Final",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "9330ee60c4e48ca60aac89b7bc5ec2567e84f28e",
  )


  # io.grpc:grpc-all:jar:1.9.0
  native.maven_jar(
      name = "io_grpc_grpc_protobuf_nano",
      artifact = "io.grpc:grpc-protobuf-nano:1.9.0",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "561b03d3fd5178117a51f9f7ef9d9e5442ed2348",
  )


  # io.opencensus:opencensus-exporter-trace-stackdriver:jar:0.12.2
  native.maven_jar(
      name = "com_google_cloud_google_cloud_trace",
      artifact = "com.google.cloud:google-cloud-trace:0.34.0-beta",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "5ae1f92161d99ca57dd9d355c4fc9fa750ed95b7",
  )


  # org.apache.httpcomponents:httpclient:jar:4.0.1
  native.maven_jar(
      name = "commons_codec_commons_codec",
      artifact = "commons-codec:commons-codec:1.3",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "fd32786786e2adb664d5ecc965da47629dca14ba",
  )


  # io.opencensus:opencensus-impl:jar:0.12.2
  native.maven_jar(
      name = "io_opencensus_opencensus_impl_core",
      artifact = "io.opencensus:opencensus-impl-core:0.12.2",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "9e059704131a4455b3bd6d84cfa8e6875551d647",
  )


  # io.prometheus:simpleclient_httpserver:bundle:0.2.0
  native.maven_jar(
      name = "io_prometheus_simpleclient_common",
      artifact = "io.prometheus:simpleclient_common:0.3.0",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "c9656d515d3a7647407f2c221d56be13177b82a0",
  )


  # com.google.api:gax-grpc:jar:1.17.0 got requested version
  # com.google.api:gax:jar:1.17.0
  native.maven_jar(
      name = "org_threeten_threetenbp",
      artifact = "org.threeten:threetenbp:1.3.3",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "3ea31c96676ff12ab56be0b1af6fff61d1a4f1f2",
  )


  # io.opencensus:opencensus-impl-core:jar:0.12.2 got requested version
  # io.opencensus:opencensus-contrib-zpages:jar:0.12.2 got requested version
  # io.opencensus:opencensus-exporter-stats-stackdriver:jar:0.12.2 got requested version
  # io.opencensus:opencensus-exporter-stats-prometheus:jar:0.12.2 got requested version
  # io.opencensus:opencensus-contrib-grpc-metrics:jar:0.12.2 got requested version
  # io.grpc:grpc-core:jar:1.9.0 wanted version 2.1.2
  # io.opencensus:opencensus-api:jar:0.12.2
  # io.opencensus:opencensus-exporter-trace-logging:jar:0.12.2 got requested version
  # io.opencensus:opencensus-impl:jar:0.12.2 got requested version
  # io.opencensus:opencensus-exporter-trace-stackdriver:jar:0.12.2 got requested version
  native.maven_jar(
      name = "com_google_errorprone_error_prone_annotations",
      artifact = "com.google.errorprone:error_prone_annotations:2.2.0",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "88e3c593e9b3586e1c6177f89267da6fc6986f0c",
  )


  # io.grpc:grpc-protobuf:jar:1.9.0 got requested version
  # com.google.cloud:google-cloud-core-grpc:jar:1.16.0 got requested version
  # com.google.cloud:google-cloud-core:jar:1.16.0
  native.maven_jar(
      name = "com_google_protobuf_protobuf_java_util",
      artifact = "com.google.protobuf:protobuf-java-util:3.5.1",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "6e40a6a3f52455bd633aa2a0dba1a416e62b4575",
  )


  # com.squareup.okhttp:okhttp:jar:2.5.0
  # io.grpc:grpc-okhttp:jar:1.9.0 wanted version 1.13.0
  native.maven_jar(
      name = "com_squareup_okio_okio",
      artifact = "com.squareup.okio:okio:1.6.0",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "98476622f10715998eacf9240d6b479f12c66143",
  )


  # io.opencensus:opencensus-exporter-stats-stackdriver:jar:0.12.2 got requested version
  # com.google.cloud:google-cloud-core-grpc:jar:1.16.0 got requested version
  # com.google.api:gax-grpc:jar:1.17.0 got requested version
  # io.opencensus:opencensus-exporter-trace-stackdriver:jar:0.12.2
  # com.google.auth:google-auth-library-oauth2-http:jar:0.9.0 got requested version
  # io.grpc:grpc-auth:jar:1.9.0 got requested version
  native.maven_jar(
      name = "com_google_auth_google_auth_library_credentials",
      artifact = "com.google.auth:google-auth-library-credentials:0.9.0",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "8e2b181feff6005c9cbc6f5c1c1e2d3ec9138d46",
  )


  # com.google.api.grpc:proto-google-cloud-monitoring-v3:jar:0.1.29 got requested version
  # com.google.cloud:google-cloud-core:jar:1.16.0
  # com.google.api:gax-grpc:jar:1.17.0 got requested version
  # com.google.api.grpc:proto-google-cloud-trace-v1:jar:0.1.29 got requested version
  # com.google.api.grpc:proto-google-cloud-trace-v2:jar:0.1.29 got requested version
  # com.google.api:gax:jar:1.17.0 got requested version
  # com.google.api.grpc:proto-google-iam-v1:jar:0.1.29 got requested version
  native.maven_jar(
      name = "com_google_api_api_common",
      artifact = "com.google.api:api-common:1.2.0",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "ac251a3623e19c4eb0a7dbb503ca32a0515f9713",
  )


  # io.grpc:grpc-core:jar:1.9.0 wanted version 0.10.0
  native.maven_jar(
      name = "io_opencensus_opencensus_contrib_grpc_metrics",
      artifact = "io.opencensus:opencensus-contrib-grpc-metrics:0.12.2",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "20dd982bd8942fc6d612fedd4466cda0461267ec",
  )


  # org.mockito:mockito-core:jar:1.9.5
  native.maven_jar(
      name = "org_objenesis_objenesis",
      artifact = "org.objenesis:objenesis:1.0",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "9b473564e792c2bdf1449da1f0b1b5bff9805704",
  )


  # com.google.cloud:google-cloud-trace:jar:0.34.0-beta
  native.maven_jar(
      name = "com_google_api_grpc_proto_google_cloud_trace_v2",
      artifact = "com.google.api.grpc:proto-google-cloud-trace-v2:0.1.29",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "14fcdcb89c76a1d864e7f7428cc2f243375a72d0",
  )


  # com.google.cloud:google-cloud-trace:jar:0.34.0-beta
  native.maven_jar(
      name = "com_google_api_grpc_proto_google_cloud_trace_v1",
      artifact = "com.google.api.grpc:proto-google-cloud-trace-v1:0.1.29",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "0bf7839ccfc44f52a395838cf188f2f506865206",
  )


  # io.grpc:grpc-all:jar:1.9.0
  native.maven_jar(
      name = "io_grpc_grpc_okhttp",
      artifact = "io.grpc:grpc-okhttp:1.9.0",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "4e7fbb9d3cd65848f42494de165b1c5839f69a8a",
  )


  # junit:junit:jar:4.12
  native.maven_jar(
      name = "org_hamcrest_hamcrest_core",
      artifact = "org.hamcrest:hamcrest-core:1.3",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "42a25dc3219429f0e5d060061f71acb49bf010a0",
  )


  # com.google.cloud:google-cloud-monitoring:jar:0.34.0-beta
  native.maven_jar(
      name = "com_google_api_grpc_proto_google_cloud_monitoring_v3",
      artifact = "com.google.api.grpc:proto-google-cloud-monitoring-v3:0.1.29",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "b8db8ecb8bab78de1b63db58fa604f0eb8834092",
  )


  # com.google.cloud:google-cloud-core:jar:1.16.0
  # com.google.http-client:google-http-client-jackson2:jar:1.23.0 got requested version
  # com.google.auth:google-auth-library-oauth2-http:jar:0.9.0 wanted version 1.19.0
  native.maven_jar(
      name = "com_google_http_client_google_http_client",
      artifact = "com.google.http-client:google-http-client:1.23.0",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "8e86c84ff3c98eca6423e97780325b299133d858",
  )


  native.maven_jar(
      name = "io_prometheus_simpleclient_httpserver",
      artifact = "io.prometheus:simpleclient_httpserver:0.3.0",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "a2c1aeecac28f5bfa9a92a67b071d246ac00bbec",
  )


  # io.grpc:grpc-core:jar:1.9.0
  native.maven_jar(
      name = "com_google_instrumentation_instrumentation_api",
      artifact = "com.google.instrumentation:instrumentation-api:0.4.3",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "41614af3429573dc02645d541638929d877945a2",
  )


  native.maven_jar(
      name = "io_opencensus_opencensus_exporter_trace_logging",
      artifact = "io.opencensus:opencensus-exporter-trace-logging:0.12.2",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "15b8b3d2c9b3ffd2d8e242d252ee056a1c30d203",
  )


  # com.google.auth:google-auth-library-oauth2-http:jar:0.9.0
  native.maven_jar(
      name = "com_google_http_client_google_http_client_jackson2",
      artifact = "com.google.http-client:google-http-client-jackson2:1.19.0",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "81dbf9795d387d5e80e55346582d5f2fb81a42eb",
  )


  # com.google.cloud:google-cloud-core-grpc:jar:1.16.0
  # com.google.api:gax-grpc:jar:1.17.0 wanted version 1.7.0
  # io.grpc:grpc-all:jar:1.9.0 got requested version
  # com.google.cloud:google-cloud-monitoring:jar:0.34.0-beta got requested version
  # com.google.cloud:google-cloud-trace:jar:0.34.0-beta got requested version
  native.maven_jar(
      name = "io_grpc_grpc_auth",
      artifact = "io.grpc:grpc-auth:1.9.0",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "d2eadc6d28ebee8ec0cef74f882255e4069972ad",
  )


  # com.google.cloud:google-cloud-core:jar:1.16.0
  # com.google.api:gax-grpc:jar:1.17.0 got requested version
  native.maven_jar(
      name = "com_google_api_gax",
      artifact = "com.google.api:gax:1.17.0",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "91bf882c7789cf0df6bda8ed95bd528753126070",
  )


  # com.google.http-client:google-http-client-jackson2:jar:1.23.0
  native.maven_jar(
      name = "com_fasterxml_jackson_core_jackson_core",
      artifact = "com.fasterxml.jackson.core:jackson-core:2.1.3",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "f6c3aed1cdfa21b5c1737c915186ea93a95a58bd",
  )


  native.maven_jar(
      name = "io_opencensus_opencensus_exporter_trace_stackdriver",
      artifact = "io.opencensus:opencensus-exporter-trace-stackdriver:0.12.2",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "d19f86b61dadfe0f101fff0d002ffae3142c3519",
  )


  # io.grpc:grpc-stub:jar:1.9.0 got requested version
  # io.grpc:grpc-okhttp:jar:1.9.0 got requested version
  # io.grpc:grpc-protobuf-lite:jar:1.9.0 got requested version
  # io.grpc:grpc-all:jar:1.9.0 got requested version
  # io.grpc:grpc-protobuf-nano:jar:1.9.0 got requested version
  # io.grpc:grpc-testing:jar:1.9.0 got requested version
  # io.grpc:grpc-netty:jar:1.9.0 got requested version
  # io.grpc:grpc-protobuf:jar:1.9.0
  # io.grpc:grpc-auth:jar:1.9.0 got requested version
  native.maven_jar(
      name = "io_grpc_grpc_core",
      artifact = "io.grpc:grpc-core:1.9.0",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "cf76ab13d35e8bd5d0ffad6d82bb1ef1770f050c",
  )


  # com.google.cloud:google-cloud-core:jar:1.16.0
  native.maven_jar(
      name = "joda_time_joda_time",
      artifact = "joda-time:joda-time:2.9.2",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "36d6e77a419cb455e6fd5909f6f96b168e21e9d0",
  )


  # org.apache.httpcomponents:httpclient:jar:4.0.1
  native.maven_jar(
      name = "org_apache_httpcomponents_httpcore",
      artifact = "org.apache.httpcomponents:httpcore:4.0.1",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "e813b8722c387b22e1adccf7914729db09bcb4a9",
  )


  # io.grpc:grpc-testing:jar:1.9.0
  native.maven_jar(
      name = "org_mockito_mockito_core",
      artifact = "org.mockito:mockito-core:1.9.5",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "c3264abeea62c4d2f367e21484fbb40c7e256393",
  )


  # io.opencensus:opencensus-impl:jar:0.12.2
  native.maven_jar(
      name = "com_lmax_disruptor",
      artifact = "com.lmax:disruptor:3.3.9",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "7898f8e8dc2d908d4ae5240fbb17eb1a9c213b9b",
  )


  # com.google.cloud:google-cloud-core-grpc:jar:1.16.0 got requested version
  # com.google.cloud:google-cloud-monitoring:jar:0.34.0-beta got requested version
  # com.google.cloud:google-cloud-trace:jar:0.34.0-beta
  native.maven_jar(
      name = "io_netty_netty_tcnative_boringssl_static",
      artifact = "io.netty:netty-tcnative-boringssl-static:2.0.7.Final",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "a8ec0f0ee612fa89c709bdd3881c3f79fa00431d",
  )


  # io.grpc:grpc-protobuf:jar:1.9.0 got requested version
  # com.google.api.grpc:proto-google-cloud-monitoring-v3:jar:0.1.29 wanted version 3.4.0
  # com.google.cloud:google-cloud-core-grpc:jar:1.16.0 got requested version
  # com.google.api.grpc:proto-google-cloud-trace-v1:jar:0.1.29 wanted version 3.4.0
  # com.google.api.grpc:proto-google-common-protos:jar:1.0.5 wanted version 3.4.0
  # com.google.api.grpc:proto-google-iam-v1:jar:0.1.29 wanted version 3.4.0
  # com.google.api.grpc:proto-google-cloud-trace-v2:jar:0.1.29 wanted version 3.4.0
  # com.google.protobuf:protobuf-java-util:bundle:3.5.1
  native.maven_jar(
      name = "com_google_protobuf_protobuf_java",
      artifact = "com.google.protobuf:protobuf-java:3.5.1",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "8c3492f7662fa1cbf8ca76a0f5eb1146f7725acd",
  )


  # com.google.cloud:google-cloud-core-grpc:jar:1.16.0
  # com.google.api:gax-grpc:jar:1.17.0 wanted version 1.7.0
  # io.grpc:grpc-all:jar:1.9.0 got requested version
  # io.grpc:grpc-testing:jar:1.9.0 got requested version
  # com.google.cloud:google-cloud-monitoring:jar:0.34.0-beta got requested version
  # com.google.cloud:google-cloud-trace:jar:0.34.0-beta got requested version
  native.maven_jar(
      name = "io_grpc_grpc_stub",
      artifact = "io.grpc:grpc-stub:1.9.0",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "20e310f888860a27dfa509a69eebb236417ee93f",
  )


  # io.grpc:grpc-okhttp:jar:1.9.0
  native.maven_jar(
      name = "com_squareup_okhttp_okhttp",
      artifact = "com.squareup.okhttp:okhttp:2.5.0",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "4de2b4ed3445c37ec1720a7d214712e845a24636",
  )


  native.maven_jar(
      name = "io_opencensus_opencensus_impl",
      artifact = "io.opencensus:opencensus-impl:0.12.2",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "4e5cd57bddbd9b47cd16cc8b0b608b43355b223f",
  )


  # com.google.cloud:google-cloud-core-grpc:jar:1.16.0
  # com.google.api:gax-grpc:jar:1.17.0 wanted version 1.7.0
  # io.grpc:grpc-all:jar:1.9.0 got requested version
  native.maven_jar(
      name = "io_grpc_grpc_protobuf",
      artifact = "io.grpc:grpc-protobuf:1.9.0",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "94ca247577e4cf1a38d5ac9d536ac1d426a1ccc5",
  )


  # com.google.cloud:google-cloud-monitoring:jar:0.34.0-beta got requested version
  # com.google.cloud:google-cloud-trace:jar:0.34.0-beta
  native.maven_jar(
      name = "com_google_cloud_google_cloud_core_grpc",
      artifact = "com.google.cloud:google-cloud-core-grpc:1.16.0",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "7a5d5f9cfcbb422943b9fa73d4ff8426db4d5fdd",
  )


  # com.google.cloud:google-cloud-core-grpc:jar:1.16.0
  # com.google.api:gax-grpc:jar:1.17.0 wanted version 1.7.0
  # io.grpc:grpc-all:jar:1.9.0 got requested version
  # com.google.cloud:google-cloud-monitoring:jar:0.34.0-beta got requested version
  # com.google.cloud:google-cloud-trace:jar:0.34.0-beta got requested version
  native.maven_jar(
      name = "io_grpc_grpc_netty",
      artifact = "io.grpc:grpc-netty:1.9.0",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "8157384d87497dc18604a5ba3760763fe643f16e",
  )


  # io.grpc:grpc-all:jar:1.9.0
  native.maven_jar(
      name = "io_grpc_grpc_testing",
      artifact = "io.grpc:grpc-testing:1.9.0",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "3d20675f0e64825f565a7d21456e7dbdd5886c6b",
  )


  # io.opencensus:opencensus-impl-core:jar:0.12.2 got requested version
  # io.opencensus:opencensus-contrib-zpages:jar:0.12.2 got requested version
  # io.opencensus:opencensus-exporter-stats-stackdriver:jar:0.12.2 got requested version
  # io.opencensus:opencensus-exporter-stats-prometheus:jar:0.12.2 got requested version
  # io.opencensus:opencensus-contrib-grpc-metrics:jar:0.12.2 got requested version
  # io.opencensus:opencensus-exporter-trace-logging:jar:0.12.2 got requested version
  # io.opencensus:opencensus-impl:jar:0.12.2 got requested version
  # io.opencensus:opencensus-exporter-trace-stackdriver:jar:0.12.2 got requested version
  native.maven_jar(
      name = "io_opencensus_opencensus_api",
      artifact = "io.opencensus:opencensus-api:0.12.2",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "a2d524b62869350942106ab8f9a1f5adb1212775",
  )


  # io.prometheus:simpleclient_common:bundle:0.2.0 wanted version 0.3.0
  # io.prometheus:simpleclient_httpserver:bundle:0.2.0 wanted version 0.3.0
  # io.opencensus:opencensus-exporter-stats-prometheus:jar:0.12.2
  native.maven_jar(
      name = "io_prometheus_simpleclient",
      artifact = "io.prometheus:simpleclient:0.2.0",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "be8de6a5a01f25074be3b27a8db4448c9cce0168",
  )


  # io.grpc:grpc-testing:jar:1.9.0
  native.maven_jar(
      name = "junit_junit",
      artifact = "junit:junit:4.12",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "2973d150c0dc1fefe998f834810d68f278ea58ec",
  )


  native.maven_jar(
      name = "io_opencensus_opencensus_exporter_stats_stackdriver",
      artifact = "io.opencensus:opencensus-exporter-stats-stackdriver:0.12.2",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "266ce009bdf082b829fb18cbd21cd308f03a301b",
  )


  # org.apache.httpcomponents:httpclient:jar:4.0.1
  native.maven_jar(
      name = "commons_logging_commons_logging",
      artifact = "commons-logging:commons-logging:1.1.1",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "5043bfebc3db072ed80fbd362e7caf00e885d8ae",
  )


  # io.grpc:grpc-netty:jar:1.9.0
  native.maven_jar(
      name = "io_netty_netty_codec_http2",
      artifact = "io.netty:netty-codec-http2:4.1.17.Final",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "f9844005869c6d9049f4b677228a89fee4c6eab3",
  )


  # com.google.protobuf:protobuf-java-util:bundle:3.5.1
  native.maven_jar(
      name = "com_google_code_gson_gson",
      artifact = "com.google.code.gson:gson:2.7",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "751f548c85fa49f330cecbb1875893f971b33c4e",
  )


  # io.grpc:grpc-protobuf-nano:jar:1.9.0
  native.maven_jar(
      name = "com_google_protobuf_nano_protobuf_javanano",
      artifact = "com.google.protobuf.nano:protobuf-javanano:3.0.0-alpha-5",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "357e60f95cebb87c72151e49ba1f570d899734f8",
  )


  # com.google.http-client:google-http-client:jar:1.23.0
  native.maven_jar(
      name = "org_apache_httpcomponents_httpclient",
      artifact = "org.apache.httpcomponents:httpclient:4.0.1",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "1d7d28fa738bdbfe4fbd895d9486308999bdf440",
  )


  # com.google.cloud:google-cloud-core:jar:1.16.0
  native.maven_jar(
      name = "com_google_api_grpc_proto_google_iam_v1",
      artifact = "com.google.api.grpc:proto-google-iam-v1:0.1.29",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "31cab899fdcdecacc1f52b8196dd56f5e2bae393",
  )


  # com.google.cloud:google-cloud-core-grpc:jar:1.16.0 got requested version
  # io.grpc:grpc-all:jar:1.9.0 got requested version
  # io.opencensus:opencensus-api:jar:0.12.2
  # io.grpc:grpc-core:jar:1.9.0 got requested version
  native.maven_jar(
      name = "io_grpc_grpc_context",
      artifact = "io.grpc:grpc-context:1.9.0",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "28b0836f48c9705abf73829bbc536dba29a1329a",
  )


  # com.google.cloud:google-cloud-core-grpc:jar:1.16.0
  native.maven_jar(
      name = "com_google_api_gax_grpc",
      artifact = "com.google.api:gax-grpc:1.17.0",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "acb0eeddc113dbb21e9681c421a298ebbc3e4211",
  )


  native.maven_jar(
      name = "io_opencensus_opencensus_contrib_zpages",
      artifact = "io.opencensus:opencensus-contrib-zpages:0.12.2",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "44f8d5b81b20f9f0d34091baecffd67c2ce0c952",
  )


  # com.google.api.grpc:proto-google-cloud-monitoring-v3:jar:0.1.29 got requested version
  # com.google.cloud:google-cloud-core:jar:1.16.0
  # com.google.api.grpc:proto-google-cloud-trace-v1:jar:0.1.29 got requested version
  # com.google.api.grpc:proto-google-cloud-trace-v2:jar:0.1.29 got requested version
  # com.google.api.grpc:proto-google-iam-v1:jar:0.1.29 got requested version
  # io.grpc:grpc-protobuf:jar:1.9.0 wanted version 1.0.0
  # com.google.api:gax-grpc:jar:1.17.0 wanted version 1.0.0
  native.maven_jar(
      name = "com_google_api_grpc_proto_google_common_protos",
      artifact = "com.google.api.grpc:proto-google-common-protos:1.0.5",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "3a5e2e2849a918acbba69154c957c36679441fcf",
  )


  native.maven_jar(
      name = "com_google_guava_guava",
      artifact = "com.google.guava:guava:23.0",
      sha1 = "c947004bb13d18182be60077ade044099e4f26f1",
  )


  native.maven_jar(
      name = "io_grpc_grpc_all",
      artifact = "io.grpc:grpc-all:1.9.0",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "442dfac27fd072e15b7134ab02c2b38136036090",
  )


  # com.google.cloud:google-cloud-core-grpc:jar:1.16.0 got requested version
  # com.google.cloud:google-cloud-monitoring:jar:0.34.0-beta got requested version
  # com.google.cloud:google-cloud-trace:jar:0.34.0-beta
  native.maven_jar(
      name = "com_google_cloud_google_cloud_core",
      artifact = "com.google.cloud:google-cloud-core:1.16.0",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "96e795e6faaeb0a11fea5022f7ff455441fb03e2",
  )


  # io.opencensus:opencensus-exporter-stats-stackdriver:jar:0.12.2
  native.maven_jar(
      name = "com_google_cloud_google_cloud_monitoring",
      artifact = "com.google.cloud:google-cloud-monitoring:0.34.0-beta",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "ace606a24a5a35553411a741dc00a054a5864c03",
  )




def opencensus_java_libraries():
  native.java_library(
      name = "com_google_code_findbugs_jsr305",
      visibility = ["//visibility:public"],
      exports = ["@com_google_code_findbugs_jsr305//jar"],
  )


  native.java_library(
      name = "com_google_auth_google_auth_library_oauth2_http",
      visibility = ["//visibility:public"],
      exports = ["@com_google_auth_google_auth_library_oauth2_http//jar"],
      runtime_deps = [
          ":com_fasterxml_jackson_core_jackson_core",
          ":com_google_auth_google_auth_library_credentials",
          ":com_google_http_client_google_http_client",
          ":com_google_http_client_google_http_client_jackson2",
      ],
  )


  native.java_library(
      name = "io_grpc_grpc_protobuf_lite",
      visibility = ["//visibility:public"],
      exports = ["@io_grpc_grpc_protobuf_lite//jar"],
      runtime_deps = [
          ":io_grpc_grpc_core",
      ],
  )


  native.java_library(
      name = "io_opencensus_opencensus_exporter_stats_prometheus",
      visibility = ["//visibility:public"],
      exports = ["@io_opencensus_opencensus_exporter_stats_prometheus//jar"],
      runtime_deps = [
          ":com_google_code_findbugs_jsr305",
          ":com_google_errorprone_error_prone_annotations",
          ":io_opencensus_opencensus_api",
          ":io_prometheus_simpleclient",
      ],
  )


  native.java_library(
      name = "io_netty_netty_handler_proxy",
      visibility = ["//visibility:public"],
      exports = ["@io_netty_netty_handler_proxy//jar"],
  )


  native.java_library(
      name = "io_grpc_grpc_protobuf_nano",
      visibility = ["//visibility:public"],
      exports = ["@io_grpc_grpc_protobuf_nano//jar"],
      runtime_deps = [
          ":com_google_guava_guava",
          ":com_google_protobuf_nano_protobuf_javanano",
          ":io_grpc_grpc_core",
      ],
  )


  native.java_library(
      name = "com_google_cloud_google_cloud_trace",
      visibility = ["//visibility:public"],
      exports = ["@com_google_cloud_google_cloud_trace//jar"],
      runtime_deps = [
          ":com_fasterxml_jackson_core_jackson_core",
          ":com_google_api_api_common",
          ":com_google_api_gax",
          ":com_google_api_gax_grpc",
          ":com_google_api_grpc_proto_google_cloud_trace_v1",
          ":com_google_api_grpc_proto_google_cloud_trace_v2",
          ":com_google_api_grpc_proto_google_common_protos",
          ":com_google_api_grpc_proto_google_iam_v1",
          ":com_google_auth_google_auth_library_credentials",
          ":com_google_auth_google_auth_library_oauth2_http",
          ":com_google_cloud_google_cloud_core",
          ":com_google_cloud_google_cloud_core_grpc",
          ":com_google_code_gson_gson",
          ":com_google_errorprone_error_prone_annotations",
          ":com_google_http_client_google_http_client",
          ":com_google_http_client_google_http_client_jackson2",
          ":com_google_instrumentation_instrumentation_api",
          ":com_google_protobuf_protobuf_java",
          ":com_google_protobuf_protobuf_java_util",
          ":commons_codec_commons_codec",
          ":commons_logging_commons_logging",
          ":io_grpc_grpc_auth",
          ":io_grpc_grpc_context",
          ":io_grpc_grpc_core",
          ":io_grpc_grpc_netty",
          ":io_grpc_grpc_protobuf",
          ":io_grpc_grpc_protobuf_lite",
          ":io_grpc_grpc_stub",
          ":io_netty_netty_codec_http2",
          ":io_netty_netty_handler_proxy",
          ":io_netty_netty_tcnative_boringssl_static",
          ":io_opencensus_opencensus_contrib_grpc_metrics",
          ":joda_time_joda_time",
          ":org_apache_httpcomponents_httpclient",
          ":org_apache_httpcomponents_httpcore",
          ":org_threeten_threetenbp",
      ],
  )


  native.java_library(
      name = "commons_codec_commons_codec",
      visibility = ["//visibility:public"],
      exports = ["@commons_codec_commons_codec//jar"],
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
      name = "io_prometheus_simpleclient_common",
      visibility = ["//visibility:public"],
      exports = ["@io_prometheus_simpleclient_common//jar"],
      runtime_deps = [
          ":io_prometheus_simpleclient",
      ],
  )


  native.java_library(
      name = "org_threeten_threetenbp",
      visibility = ["//visibility:public"],
      exports = ["@org_threeten_threetenbp//jar"],
  )


  native.java_library(
      name = "com_google_errorprone_error_prone_annotations",
      visibility = ["//visibility:public"],
      exports = ["@com_google_errorprone_error_prone_annotations//jar"],
  )


  native.java_library(
      name = "com_google_protobuf_protobuf_java_util",
      visibility = ["//visibility:public"],
      exports = ["@com_google_protobuf_protobuf_java_util//jar"],
      runtime_deps = [
          ":com_google_code_gson_gson",
          ":com_google_protobuf_protobuf_java",
      ],
  )


  native.java_library(
      name = "com_squareup_okio_okio",
      visibility = ["//visibility:public"],
      exports = ["@com_squareup_okio_okio//jar"],
  )


  native.java_library(
      name = "com_google_auth_google_auth_library_credentials",
      visibility = ["//visibility:public"],
      exports = ["@com_google_auth_google_auth_library_credentials//jar"],
  )


  native.java_library(
      name = "com_google_api_api_common",
      visibility = ["//visibility:public"],
      exports = ["@com_google_api_api_common//jar"],
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
      name = "org_objenesis_objenesis",
      visibility = ["//visibility:public"],
      exports = ["@org_objenesis_objenesis//jar"],
  )


  native.java_library(
      name = "com_google_api_grpc_proto_google_cloud_trace_v2",
      visibility = ["//visibility:public"],
      exports = ["@com_google_api_grpc_proto_google_cloud_trace_v2//jar"],
      runtime_deps = [
          ":com_google_api_api_common",
          ":com_google_api_grpc_proto_google_common_protos",
          ":com_google_protobuf_protobuf_java",
      ],
  )


  native.java_library(
      name = "com_google_api_grpc_proto_google_cloud_trace_v1",
      visibility = ["//visibility:public"],
      exports = ["@com_google_api_grpc_proto_google_cloud_trace_v1//jar"],
      runtime_deps = [
          ":com_google_api_api_common",
          ":com_google_api_grpc_proto_google_common_protos",
          ":com_google_protobuf_protobuf_java",
      ],
  )


  native.java_library(
      name = "io_grpc_grpc_okhttp",
      visibility = ["//visibility:public"],
      exports = ["@io_grpc_grpc_okhttp//jar"],
      runtime_deps = [
          ":com_squareup_okhttp_okhttp",
          ":com_squareup_okio_okio",
          ":io_grpc_grpc_core",
      ],
  )


  native.java_library(
      name = "org_hamcrest_hamcrest_core",
      visibility = ["//visibility:public"],
      exports = ["@org_hamcrest_hamcrest_core//jar"],
  )


  native.java_library(
      name = "com_google_api_grpc_proto_google_cloud_monitoring_v3",
      visibility = ["//visibility:public"],
      exports = ["@com_google_api_grpc_proto_google_cloud_monitoring_v3//jar"],
      runtime_deps = [
          ":com_google_api_api_common",
          ":com_google_api_grpc_proto_google_common_protos",
          ":com_google_protobuf_protobuf_java",
      ],
  )


  native.java_library(
      name = "com_google_http_client_google_http_client",
      visibility = ["//visibility:public"],
      exports = ["@com_google_http_client_google_http_client//jar"],
      runtime_deps = [
          ":commons_codec_commons_codec",
          ":commons_logging_commons_logging",
          ":org_apache_httpcomponents_httpclient",
          ":org_apache_httpcomponents_httpcore",
      ],
  )


  native.java_library(
      name = "io_prometheus_simpleclient_httpserver",
      visibility = ["//visibility:public"],
      exports = ["@io_prometheus_simpleclient_httpserver//jar"],
      runtime_deps = [
          ":io_prometheus_simpleclient",
          ":io_prometheus_simpleclient_common",
      ],
  )


  native.java_library(
      name = "com_google_instrumentation_instrumentation_api",
      visibility = ["//visibility:public"],
      exports = ["@com_google_instrumentation_instrumentation_api//jar"],
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
      name = "com_google_http_client_google_http_client_jackson2",
      visibility = ["//visibility:public"],
      exports = ["@com_google_http_client_google_http_client_jackson2//jar"],
      runtime_deps = [
          ":com_fasterxml_jackson_core_jackson_core",
          ":com_google_http_client_google_http_client",
      ],
  )


  native.java_library(
      name = "io_grpc_grpc_auth",
      visibility = ["//visibility:public"],
      exports = ["@io_grpc_grpc_auth//jar"],
      runtime_deps = [
          ":com_google_auth_google_auth_library_credentials",
          ":io_grpc_grpc_core",
      ],
  )


  native.java_library(
      name = "com_google_api_gax",
      visibility = ["//visibility:public"],
      exports = ["@com_google_api_gax//jar"],
      runtime_deps = [
          ":com_fasterxml_jackson_core_jackson_core",
          ":com_google_api_api_common",
          ":com_google_auth_google_auth_library_credentials",
          ":com_google_auth_google_auth_library_oauth2_http",
          ":com_google_http_client_google_http_client",
          ":com_google_http_client_google_http_client_jackson2",
          ":org_threeten_threetenbp",
      ],
  )


  native.java_library(
      name = "com_fasterxml_jackson_core_jackson_core",
      visibility = ["//visibility:public"],
      exports = ["@com_fasterxml_jackson_core_jackson_core//jar"],
  )


  native.java_library(
      name = "io_opencensus_opencensus_exporter_trace_stackdriver",
      visibility = ["//visibility:public"],
      exports = ["@io_opencensus_opencensus_exporter_trace_stackdriver//jar"],
      runtime_deps = [
          ":com_fasterxml_jackson_core_jackson_core",
          ":com_google_api_api_common",
          ":com_google_api_gax",
          ":com_google_api_gax_grpc",
          ":com_google_api_grpc_proto_google_cloud_trace_v1",
          ":com_google_api_grpc_proto_google_cloud_trace_v2",
          ":com_google_api_grpc_proto_google_common_protos",
          ":com_google_api_grpc_proto_google_iam_v1",
          ":com_google_auth_google_auth_library_credentials",
          ":com_google_auth_google_auth_library_oauth2_http",
          ":com_google_cloud_google_cloud_core",
          ":com_google_cloud_google_cloud_core_grpc",
          ":com_google_cloud_google_cloud_trace",
          ":com_google_code_findbugs_jsr305",
          ":com_google_code_gson_gson",
          ":com_google_errorprone_error_prone_annotations",
          ":com_google_http_client_google_http_client",
          ":com_google_http_client_google_http_client_jackson2",
          ":com_google_instrumentation_instrumentation_api",
          ":com_google_protobuf_protobuf_java",
          ":com_google_protobuf_protobuf_java_util",
          ":commons_codec_commons_codec",
          ":commons_logging_commons_logging",
          ":io_grpc_grpc_auth",
          ":io_grpc_grpc_context",
          ":io_grpc_grpc_core",
          ":io_grpc_grpc_netty",
          ":io_grpc_grpc_protobuf",
          ":io_grpc_grpc_protobuf_lite",
          ":io_grpc_grpc_stub",
          ":io_netty_netty_codec_http2",
          ":io_netty_netty_handler_proxy",
          ":io_netty_netty_tcnative_boringssl_static",
          ":io_opencensus_opencensus_api",
          ":io_opencensus_opencensus_contrib_grpc_metrics",
          ":joda_time_joda_time",
          ":org_apache_httpcomponents_httpclient",
          ":org_apache_httpcomponents_httpcore",
          ":org_threeten_threetenbp",
      ],
  )


  native.java_library(
      name = "io_grpc_grpc_core",
      visibility = ["//visibility:public"],
      exports = ["@io_grpc_grpc_core//jar"],
      runtime_deps = [
          ":com_google_errorprone_error_prone_annotations",
          ":com_google_instrumentation_instrumentation_api",
          ":io_grpc_grpc_context",
          ":io_opencensus_opencensus_contrib_grpc_metrics",
      ],
  )


  native.java_library(
      name = "joda_time_joda_time",
      visibility = ["//visibility:public"],
      exports = ["@joda_time_joda_time//jar"],
  )


  native.java_library(
      name = "org_apache_httpcomponents_httpcore",
      visibility = ["//visibility:public"],
      exports = ["@org_apache_httpcomponents_httpcore//jar"],
  )


  native.java_library(
      name = "org_mockito_mockito_core",
      visibility = ["//visibility:public"],
      exports = ["@org_mockito_mockito_core//jar"],
      runtime_deps = [
          ":org_objenesis_objenesis",
      ],
  )


  native.java_library(
      name = "com_lmax_disruptor",
      visibility = ["//visibility:public"],
      exports = ["@com_lmax_disruptor//jar"],
  )


  native.java_library(
      name = "io_netty_netty_tcnative_boringssl_static",
      visibility = ["//visibility:public"],
      exports = ["@io_netty_netty_tcnative_boringssl_static//jar"],
  )


  native.java_library(
      name = "com_google_protobuf_protobuf_java",
      visibility = ["//visibility:public"],
      exports = ["@com_google_protobuf_protobuf_java//jar"],
  )


  native.java_library(
      name = "io_grpc_grpc_stub",
      visibility = ["//visibility:public"],
      exports = ["@io_grpc_grpc_stub//jar"],
      runtime_deps = [
          ":io_grpc_grpc_core",
      ],
  )


  native.java_library(
      name = "com_squareup_okhttp_okhttp",
      visibility = ["//visibility:public"],
      exports = ["@com_squareup_okhttp_okhttp//jar"],
      runtime_deps = [
          ":com_squareup_okio_okio",
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


  native.java_library(
      name = "io_grpc_grpc_protobuf",
      visibility = ["//visibility:public"],
      exports = ["@io_grpc_grpc_protobuf//jar"],
      runtime_deps = [
          ":com_google_api_grpc_proto_google_common_protos",
          ":com_google_errorprone_error_prone_annotations",
          ":com_google_instrumentation_instrumentation_api",
          ":com_google_protobuf_protobuf_java",
          ":com_google_protobuf_protobuf_java_util",
          ":io_grpc_grpc_context",
          ":io_grpc_grpc_core",
          ":io_grpc_grpc_protobuf_lite",
          ":io_opencensus_opencensus_contrib_grpc_metrics",
      ],
  )


  native.java_library(
      name = "com_google_cloud_google_cloud_core_grpc",
      visibility = ["//visibility:public"],
      exports = ["@com_google_cloud_google_cloud_core_grpc//jar"],
      runtime_deps = [
          ":com_google_api_api_common",
          ":com_google_api_gax",
          ":com_google_api_gax_grpc",
          ":com_google_api_grpc_proto_google_common_protos",
          ":com_google_auth_google_auth_library_credentials",
          ":com_google_auth_google_auth_library_oauth2_http",
          ":com_google_cloud_google_cloud_core",
          ":com_google_errorprone_error_prone_annotations",
          ":com_google_instrumentation_instrumentation_api",
          ":com_google_protobuf_protobuf_java",
          ":com_google_protobuf_protobuf_java_util",
          ":io_grpc_grpc_auth",
          ":io_grpc_grpc_context",
          ":io_grpc_grpc_core",
          ":io_grpc_grpc_netty",
          ":io_grpc_grpc_protobuf",
          ":io_grpc_grpc_protobuf_lite",
          ":io_grpc_grpc_stub",
          ":io_netty_netty_codec_http2",
          ":io_netty_netty_handler_proxy",
          ":io_netty_netty_tcnative_boringssl_static",
          ":io_opencensus_opencensus_contrib_grpc_metrics",
          ":org_threeten_threetenbp",
      ],
  )


  native.java_library(
      name = "io_grpc_grpc_netty",
      visibility = ["//visibility:public"],
      exports = ["@io_grpc_grpc_netty//jar"],
      runtime_deps = [
          ":io_grpc_grpc_core",
          ":io_netty_netty_codec_http2",
          ":io_netty_netty_handler_proxy",
      ],
  )


  native.java_library(
      name = "io_grpc_grpc_testing",
      visibility = ["//visibility:public"],
      exports = ["@io_grpc_grpc_testing//jar"],
      runtime_deps = [
          ":io_grpc_grpc_core",
          ":io_grpc_grpc_stub",
          ":junit_junit",
          ":org_hamcrest_hamcrest_core",
          ":org_mockito_mockito_core",
          ":org_objenesis_objenesis",
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
      name = "io_prometheus_simpleclient",
      visibility = ["//visibility:public"],
      exports = ["@io_prometheus_simpleclient//jar"],
  )


  native.java_library(
      name = "junit_junit",
      visibility = ["//visibility:public"],
      exports = ["@junit_junit//jar"],
      runtime_deps = [
          ":org_hamcrest_hamcrest_core",
      ],
  )


  native.java_library(
      name = "io_opencensus_opencensus_exporter_stats_stackdriver",
      visibility = ["//visibility:public"],
      exports = ["@io_opencensus_opencensus_exporter_stats_stackdriver//jar"],
      runtime_deps = [
          ":com_google_api_api_common",
          ":com_google_api_grpc_proto_google_cloud_monitoring_v3",
          ":com_google_api_grpc_proto_google_common_protos",
          ":com_google_auth_google_auth_library_credentials",
          ":com_google_cloud_google_cloud_core",
          ":com_google_cloud_google_cloud_core_grpc",
          ":com_google_cloud_google_cloud_monitoring",
          ":com_google_code_findbugs_jsr305",
          ":com_google_errorprone_error_prone_annotations",
          ":com_google_protobuf_protobuf_java",
          ":io_grpc_grpc_auth",
          ":io_grpc_grpc_netty",
          ":io_grpc_grpc_stub",
          ":io_netty_netty_tcnative_boringssl_static",
          ":io_opencensus_opencensus_api",
      ],
  )


  native.java_library(
      name = "commons_logging_commons_logging",
      visibility = ["//visibility:public"],
      exports = ["@commons_logging_commons_logging//jar"],
  )


  native.java_library(
      name = "io_netty_netty_codec_http2",
      visibility = ["//visibility:public"],
      exports = ["@io_netty_netty_codec_http2//jar"],
  )


  native.java_library(
      name = "com_google_code_gson_gson",
      visibility = ["//visibility:public"],
      exports = ["@com_google_code_gson_gson//jar"],
  )


  native.java_library(
      name = "com_google_protobuf_nano_protobuf_javanano",
      visibility = ["//visibility:public"],
      exports = ["@com_google_protobuf_nano_protobuf_javanano//jar"],
  )


  native.java_library(
      name = "org_apache_httpcomponents_httpclient",
      visibility = ["//visibility:public"],
      exports = ["@org_apache_httpcomponents_httpclient//jar"],
      runtime_deps = [
          ":commons_codec_commons_codec",
          ":commons_logging_commons_logging",
          ":org_apache_httpcomponents_httpcore",
      ],
  )


  native.java_library(
      name = "com_google_api_grpc_proto_google_iam_v1",
      visibility = ["//visibility:public"],
      exports = ["@com_google_api_grpc_proto_google_iam_v1//jar"],
      runtime_deps = [
          ":com_google_api_api_common",
          ":com_google_api_grpc_proto_google_common_protos",
          ":com_google_protobuf_protobuf_java",
      ],
  )


  native.java_library(
      name = "io_grpc_grpc_context",
      visibility = ["//visibility:public"],
      exports = ["@io_grpc_grpc_context//jar"],
  )


  native.java_library(
      name = "com_google_api_gax_grpc",
      visibility = ["//visibility:public"],
      exports = ["@com_google_api_gax_grpc//jar"],
      runtime_deps = [
          ":com_google_api_api_common",
          ":com_google_api_gax",
          ":com_google_api_grpc_proto_google_common_protos",
          ":com_google_auth_google_auth_library_credentials",
          ":com_google_auth_google_auth_library_oauth2_http",
          ":io_grpc_grpc_auth",
          ":io_grpc_grpc_netty",
          ":io_grpc_grpc_protobuf",
          ":io_grpc_grpc_stub",
          ":org_threeten_threetenbp",
      ],
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
      name = "com_google_api_grpc_proto_google_common_protos",
      visibility = ["//visibility:public"],
      exports = ["@com_google_api_grpc_proto_google_common_protos//jar"],
      runtime_deps = [
          ":com_google_protobuf_protobuf_java",
      ],
  )


  native.java_library(
      name = "com_google_guava_guava",
      visibility = ["//visibility:public"],
      exports = ["@com_google_guava_guava//jar"],
  )


  native.java_library(
      name = "io_grpc_grpc_all",
      visibility = ["//visibility:public"],
      exports = ["@io_grpc_grpc_all//jar"],
      runtime_deps = [
          ":com_google_guava_guava",
          ":com_google_protobuf_nano_protobuf_javanano",
          ":com_squareup_okhttp_okhttp",
          ":com_squareup_okio_okio",
          ":io_grpc_grpc_auth",
          ":io_grpc_grpc_context",
          ":io_grpc_grpc_core",
          ":io_grpc_grpc_netty",
          ":io_grpc_grpc_okhttp",
          ":io_grpc_grpc_protobuf",
          ":io_grpc_grpc_protobuf_nano",
          ":io_grpc_grpc_stub",
          ":io_grpc_grpc_testing",
          ":junit_junit",
          ":org_hamcrest_hamcrest_core",
          ":org_mockito_mockito_core",
          ":org_objenesis_objenesis",
      ],
  )


  native.java_library(
      name = "com_google_cloud_google_cloud_core",
      visibility = ["//visibility:public"],
      exports = ["@com_google_cloud_google_cloud_core//jar"],
      runtime_deps = [
          ":com_fasterxml_jackson_core_jackson_core",
          ":com_google_api_api_common",
          ":com_google_api_gax",
          ":com_google_api_grpc_proto_google_common_protos",
          ":com_google_api_grpc_proto_google_iam_v1",
          ":com_google_auth_google_auth_library_credentials",
          ":com_google_auth_google_auth_library_oauth2_http",
          ":com_google_code_gson_gson",
          ":com_google_http_client_google_http_client",
          ":com_google_http_client_google_http_client_jackson2",
          ":com_google_protobuf_protobuf_java",
          ":com_google_protobuf_protobuf_java_util",
          ":commons_codec_commons_codec",
          ":commons_logging_commons_logging",
          ":joda_time_joda_time",
          ":org_apache_httpcomponents_httpclient",
          ":org_apache_httpcomponents_httpcore",
          ":org_threeten_threetenbp",
      ],
  )


  native.java_library(
      name = "com_google_cloud_google_cloud_monitoring",
      visibility = ["//visibility:public"],
      exports = ["@com_google_cloud_google_cloud_monitoring//jar"],
      runtime_deps = [
          ":com_google_api_api_common",
          ":com_google_api_grpc_proto_google_cloud_monitoring_v3",
          ":com_google_api_grpc_proto_google_common_protos",
          ":com_google_cloud_google_cloud_core",
          ":com_google_cloud_google_cloud_core_grpc",
          ":com_google_protobuf_protobuf_java",
          ":io_grpc_grpc_auth",
          ":io_grpc_grpc_netty",
          ":io_grpc_grpc_stub",
          ":io_netty_netty_tcnative_boringssl_static",
      ],
  )


