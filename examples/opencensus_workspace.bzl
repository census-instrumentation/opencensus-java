# The following dependencies were calculated from:
#
# generate_workspace --artifact=com.google.guava:guava-jdk5:23.0 --artifact=com.google.guava:guava:23.0 --artifact=io.grpc:grpc-all:1.9.0 --artifact=io.opencensus:opencensus-api:0.18.0 --artifact=io.opencensus:opencensus-contrib-grpc-metrics:0.18.0 --artifact=io.opencensus:opencensus-contrib-zpages:0.18.0 --artifact=io.opencensus:opencensus-exporter-stats-prometheus:0.18.0 --artifact=io.opencensus:opencensus-exporter-stats-stackdriver:0.18.0 --artifact=io.opencensus:opencensus-exporter-trace-logging:0.18.0 --artifact=io.opencensus:opencensus-exporter-trace-stackdriver:0.18.0 --artifact=io.opencensus:opencensus-impl:0.18.0 --artifact=io.prometheus:simpleclient_httpserver:0.3.0 --repositories=http://repo.maven.apache.org/maven2


def opencensus_maven_jars():
  # io.opencensus:opencensus-api:jar:0.10.0 wanted version 3.0.1
  # io.grpc:grpc-core:jar:1.9.0 wanted version 3.0.0
  # com.google.guava:guava:bundle:23.0
  # com.google.instrumentation:instrumentation-api:jar:0.4.3 wanted version 3.0.0
  # io.opencensus:opencensus-contrib-grpc-metrics:jar:0.10.0 wanted version 3.0.1
  native.maven_jar(
      name = "com_google_code_findbugs_jsr305",
      artifact = "com.google.code.findbugs:jsr305:2.0.2",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "516c03b21d50a644d538de0f0369c620989cd8f0",
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
      artifact = "io.opencensus:opencensus-exporter-stats-prometheus:0.18.0",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "57bd0e92225669be1b3f69f6aba5291c79da8f3b",
  )


  # com.google.api:gax:jar:1.34.0
  # com.google.api:gax-grpc:jar:1.34.0 got requested version
  native.maven_jar(
      name = "com_google_auth_google_auth_library_oauth2_http",
      artifact = "com.google.auth:google-auth-library-oauth2-http:0.11.0",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "576d6ae74c10e6a0d3f30a97ef59499473b02175",
  )


  # io.netty:netty-handler-proxy:jar:4.1.17.Final got requested version
  # io.netty:netty-codec:jar:4.1.17.Final
  # io.netty:netty-handler:jar:4.1.17.Final got requested version
  native.maven_jar(
      name = "io_netty_netty_transport",
      artifact = "io.netty:netty-transport:4.1.17.Final",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "9585776b0a8153182412b5d5366061ff486914c1",
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


  # io.opencensus:opencensus-exporter-trace-stackdriver:jar:0.18.0
  native.maven_jar(
      name = "com_google_cloud_google_cloud_trace",
      artifact = "com.google.cloud:google-cloud-trace:0.70.0-beta",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "ef5c68476a90dfe5b5d51542c78ce59dae20cb97",
  )


  # org.apache.httpcomponents:httpclient:jar:4.5.3
  native.maven_jar(
      name = "commons_codec_commons_codec",
      artifact = "commons-codec:commons-codec:1.9",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "9ce04e34240f674bc72680f8b843b1457383161a",
  )


  # io.opencensus:opencensus-impl:jar:0.18.0
  native.maven_jar(
      name = "io_opencensus_opencensus_impl_core",
      artifact = "io.opencensus:opencensus-impl-core:0.18.0",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "185b615a1a0cd42ef6d538f766bec7171f1c04c4",
  )


  # io.prometheus:simpleclient_httpserver:bundle:0.4.0
  native.maven_jar(
      name = "io_prometheus_simpleclient_common",
      artifact = "io.prometheus:simpleclient_common:0.3.0",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "c9656d515d3a7647407f2c221d56be13177b82a0",
  )


  # com.google.api:gax:jar:1.34.0
  # com.google.api:gax-grpc:jar:1.34.0 got requested version
  native.maven_jar(
      name = "org_threeten_threetenbp",
      artifact = "org.threeten:threetenbp:1.3.3",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "3ea31c96676ff12ab56be0b1af6fff61d1a4f1f2",
  )


  # io.grpc:grpc-core:jar:1.9.0 wanted version 2.1.2
  # io.opencensus:opencensus-contrib-grpc-metrics:jar:0.10.0 wanted version 2.1.2
  # com.google.guava:guava:bundle:23.0
  # io.opencensus:opencensus-api:jar:0.10.0 wanted version 2.1.2
  native.maven_jar(
      name = "com_google_errorprone_error_prone_annotations",
      artifact = "com.google.errorprone:error_prone_annotations:2.0.18",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "5f65affce1684999e2f4024983835efc3504012e",
  )


  # io.netty:netty-transport:jar:4.1.17.Final
  native.maven_jar(
      name = "io_netty_netty_resolver",
      artifact = "io.netty:netty-resolver:4.1.17.Final",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "8f386c80821e200f542da282ae1d3cde5cad8368",
  )


  # com.squareup.okhttp:okhttp:jar:2.5.0
  # io.grpc:grpc-okhttp:jar:1.9.0 wanted version 1.13.0
  native.maven_jar(
      name = "com_squareup_okio_okio",
      artifact = "com.squareup.okio:okio:1.6.0",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "98476622f10715998eacf9240d6b479f12c66143",
  )


  # com.google.cloud:google-cloud-core-grpc:jar:1.52.0 wanted version 3.6.1
  # com.google.cloud:google-cloud-core:jar:1.52.0 wanted version 3.6.1
  # io.grpc:grpc-protobuf:jar:1.9.0
  native.maven_jar(
      name = "com_google_protobuf_protobuf_java_util",
      artifact = "com.google.protobuf:protobuf-java-util:3.5.1",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "6e40a6a3f52455bd633aa2a0dba1a416e62b4575",
  )


  # io.grpc:grpc-auth:jar:1.9.0
  # io.opencensus:opencensus-exporter-trace-stackdriver:jar:0.18.0 wanted version 0.11.0
  # com.google.api:gax-grpc:jar:1.34.0 wanted version 0.11.0
  # com.google.auth:google-auth-library-oauth2-http:jar:0.9.0 got requested version
  # io.opencensus:opencensus-exporter-stats-stackdriver:jar:0.18.0 wanted version 0.11.0
  # com.google.cloud:google-cloud-core-grpc:jar:1.52.0 wanted version 0.11.0
  native.maven_jar(
      name = "com_google_auth_google_auth_library_credentials",
      artifact = "com.google.auth:google-auth-library-credentials:0.9.0",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "8e2b181feff6005c9cbc6f5c1c1e2d3ec9138d46",
  )


  # com.google.api.grpc:proto-google-cloud-monitoring-v3:jar:1.34.0 got requested version
  # com.google.api:gax:jar:1.34.0 got requested version
  # com.google.api:gax-grpc:jar:1.34.0 got requested version
  # com.google.api.grpc:proto-google-iam-v1:jar:0.12.0 wanted version 1.5.0
  # com.google.api.grpc:proto-google-cloud-trace-v1:jar:0.35.0 got requested version
  # com.google.cloud:google-cloud-core:jar:1.52.0
  # com.google.api.grpc:proto-google-cloud-trace-v2:jar:0.35.0 got requested version
  native.maven_jar(
      name = "com_google_api_api_common",
      artifact = "com.google.api:api-common:1.7.0",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "ea59fb8b2450999345035dec8a6f472543391766",
  )


  # io.opencensus:opencensus-contrib-zpages:jar:0.18.0 got requested version
  native.maven_jar(
      name = "io_opencensus_opencensus_contrib_grpc_metrics",
      artifact = "io.opencensus:opencensus-contrib-grpc-metrics:0.18.0",
      sha1 = "8e90fab2930b6a0e67dab48911b9c936470d43dd",
  )


  # org.mockito:mockito-core:jar:1.9.5
  native.maven_jar(
      name = "org_objenesis_objenesis",
      artifact = "org.objenesis:objenesis:1.0",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "9b473564e792c2bdf1449da1f0b1b5bff9805704",
  )


  # io.netty:netty-buffer:jar:4.1.17.Final
  # io.netty:netty-resolver:jar:4.1.17.Final got requested version
  native.maven_jar(
      name = "io_netty_netty_common",
      artifact = "io.netty:netty-common:4.1.17.Final",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "581c8ee239e4dc0976c2405d155f475538325098",
  )


  # com.google.cloud:google-cloud-trace:jar:0.70.0-beta
  native.maven_jar(
      name = "com_google_api_grpc_proto_google_cloud_trace_v2",
      artifact = "com.google.api.grpc:proto-google-cloud-trace-v2:0.35.0",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "4bada2490cdb000d8530bb38e58f4582ef22ff9b",
  )


  # com.google.cloud:google-cloud-trace:jar:0.70.0-beta got requested version
  # com.google.api:gax-grpc:jar:1.34.0 got requested version
  # com.google.cloud:google-cloud-monitoring:jar:1.52.0 got requested version
  # com.google.cloud:google-cloud-core-grpc:jar:1.52.0
  native.maven_jar(
      name = "io_grpc_grpc_netty_shaded",
      artifact = "io.grpc:grpc-netty-shaded:1.15.0",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "c1b4c7204cf3628e0e1aa52bd724393dbc647e33",
  )


  # com.google.cloud:google-cloud-trace:jar:0.70.0-beta
  native.maven_jar(
      name = "com_google_api_grpc_proto_google_cloud_trace_v1",
      artifact = "com.google.api.grpc:proto-google-cloud-trace-v1:0.35.0",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "36b23dfe97a91760fed2462773c0454dde7a7c55",
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


  # io.netty:netty-codec-http2:jar:4.1.17.Final
  native.maven_jar(
      name = "io_netty_netty_handler",
      artifact = "io.netty:netty-handler:4.1.17.Final",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "18c40ffb61a1d1979eca024087070762fdc4664a",
  )


  # com.google.cloud:google-cloud-monitoring:jar:1.52.0
  native.maven_jar(
      name = "com_google_api_grpc_proto_google_cloud_monitoring_v3",
      artifact = "com.google.api.grpc:proto-google-cloud-monitoring-v3:1.34.0",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "fa16c030a1ff00f825b44bcf6c2d036ce521ccdd",
  )


  # com.google.cloud:google-cloud-core:jar:1.52.0
  # com.google.auth:google-auth-library-oauth2-http:jar:0.9.0 wanted version 1.19.0
  native.maven_jar(
      name = "com_google_http_client_google_http_client",
      artifact = "com.google.http-client:google-http-client:1.24.1",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "396eac8d3fb1332675f82b208f48a469d64f3b4a",
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


  # com.google.auth:google-auth-library-oauth2-http:jar:0.9.0
  native.maven_jar(
      name = "com_google_http_client_google_http_client_jackson2",
      artifact = "com.google.http-client:google-http-client-jackson2:1.19.0",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "81dbf9795d387d5e80e55346582d5f2fb81a42eb",
  )


  native.maven_jar(
      name = "io_opencensus_opencensus_exporter_trace_logging",
      artifact = "io.opencensus:opencensus-exporter-trace-logging:0.18.0",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "53d3362516e49413cdab38cd6081dbaed6b3df03",
  )


  # com.google.cloud:google-cloud-core-grpc:jar:1.52.0 wanted version 1.15.0
  # com.google.api:gax-grpc:jar:1.34.0 wanted version 1.15.0
  # com.google.cloud:google-cloud-monitoring:jar:1.52.0 wanted version 1.15.0
  # io.grpc:grpc-all:jar:1.9.0
  # com.google.cloud:google-cloud-trace:jar:0.70.0-beta wanted version 1.15.0
  native.maven_jar(
      name = "io_grpc_grpc_auth",
      artifact = "io.grpc:grpc-auth:1.9.0",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "d2eadc6d28ebee8ec0cef74f882255e4069972ad",
  )


  # com.google.api:gax-grpc:jar:1.34.0 got requested version
  # com.google.cloud:google-cloud-core:jar:1.52.0
  native.maven_jar(
      name = "com_google_api_gax",
      artifact = "com.google.api:gax:1.34.0",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "72de4557377d7ec56f609bd00f1b2ead55632df0",
  )


  native.maven_jar(
      name = "io_opencensus_opencensus_exporter_trace_stackdriver",
      artifact = "io.opencensus:opencensus-exporter-trace-stackdriver:0.18.0",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "ad974124f46ebf1e101fed2dac90c55b2617ed78",
  )


  # com.google.guava:guava:bundle:23.0
  native.maven_jar(
      name = "com_google_j2objc_j2objc_annotations",
      artifact = "com.google.j2objc:j2objc-annotations:1.1",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "ed28ded51a8b1c6b112568def5f4b455e6809019",
  )


  # io.grpc:grpc-auth:jar:1.9.0
  # io.grpc:grpc-protobuf:jar:1.9.0 got requested version
  # io.grpc:grpc-okhttp:jar:1.9.0 got requested version
  # io.grpc:grpc-stub:jar:1.9.0 got requested version
  # io.grpc:grpc-protobuf-lite:jar:1.9.0 got requested version
  # io.grpc:grpc-all:jar:1.9.0 got requested version
  # io.grpc:grpc-protobuf-nano:jar:1.9.0 got requested version
  # io.grpc:grpc-netty-shaded:jar:1.15.0 wanted version 1.15.0
  # io.grpc:grpc-testing:jar:1.9.0 got requested version
  # io.grpc:grpc-netty:jar:1.9.0 got requested version
  native.maven_jar(
      name = "io_grpc_grpc_core",
      artifact = "io.grpc:grpc-core:1.9.0",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "cf76ab13d35e8bd5d0ffad6d82bb1ef1770f050c",
  )


  # io.opencensus:opencensus-exporter-trace-stackdriver:jar:0.18.0 got requested version
  # io.opencensus:opencensus-exporter-stats-stackdriver:jar:0.18.0
  native.maven_jar(
      name = "io_opencensus_opencensus_contrib_monitored_resource_util",
      artifact = "io.opencensus:opencensus-contrib-monitored-resource-util:0.18.0",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "a26b076c710d4d0871bc6917476a1ed3d024fd67",
  )


  # com.google.cloud:google-cloud-core:jar:1.52.0
  native.maven_jar(
      name = "joda_time_joda_time",
      artifact = "joda-time:joda-time:2.9.2",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "36d6e77a419cb455e6fd5909f6f96b168e21e9d0",
  )


  # io.grpc:grpc-testing:jar:1.9.0
  native.maven_jar(
      name = "org_mockito_mockito_core",
      artifact = "org.mockito:mockito-core:1.9.5",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "c3264abeea62c4d2f367e21484fbb40c7e256393",
  )


  # org.apache.httpcomponents:httpclient:jar:4.5.3
  native.maven_jar(
      name = "org_apache_httpcomponents_httpcore",
      artifact = "org.apache.httpcomponents:httpcore:4.4.6",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "e3fd8ced1f52c7574af952e2e6da0df8df08eb82",
  )


  # io.opencensus:opencensus-impl:jar:0.18.0
  native.maven_jar(
      name = "com_lmax_disruptor",
      artifact = "com.lmax:disruptor:3.4.2",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "e2543a63086b4189fbe418d05d56633bc1a815f7",
  )


  # com.google.api.grpc:proto-google-cloud-trace-v1:jar:0.35.0 wanted version 3.6.1
  # com.google.cloud:google-cloud-core-grpc:jar:1.52.0 wanted version 3.6.1
  # com.google.api.grpc:proto-google-cloud-monitoring-v3:jar:1.34.0 wanted version 3.6.1
  # io.grpc:grpc-protobuf:jar:1.9.0
  # com.google.api.grpc:proto-google-iam-v1:jar:0.12.0 got requested version
  # com.google.api.grpc:proto-google-cloud-trace-v2:jar:0.35.0 wanted version 3.6.1
  # com.google.protobuf:protobuf-java-util:bundle:3.5.1 got requested version
  native.maven_jar(
      name = "com_google_protobuf_protobuf_java",
      artifact = "com.google.protobuf:protobuf-java:3.5.1",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "8c3492f7662fa1cbf8ca76a0f5eb1146f7725acd",
  )


  # io.grpc:grpc-okhttp:jar:1.9.0
  native.maven_jar(
      name = "com_squareup_okhttp_okhttp",
      artifact = "com.squareup.okhttp:okhttp:2.5.0",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "4de2b4ed3445c37ec1720a7d214712e845a24636",
  )


  # com.google.cloud:google-cloud-core-grpc:jar:1.52.0 wanted version 1.15.0
  # com.google.api:gax-grpc:jar:1.34.0 wanted version 1.15.0
  # com.google.cloud:google-cloud-monitoring:jar:1.52.0 wanted version 1.15.0
  # io.grpc:grpc-testing:jar:1.9.0 got requested version
  # io.grpc:grpc-all:jar:1.9.0
  # com.google.cloud:google-cloud-trace:jar:0.70.0-beta wanted version 1.15.0
  native.maven_jar(
      name = "io_grpc_grpc_stub",
      artifact = "io.grpc:grpc-stub:1.9.0",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "20e310f888860a27dfa509a69eebb236417ee93f",
  )


  native.maven_jar(
      name = "io_opencensus_opencensus_impl",
      artifact = "io.opencensus:opencensus-impl:0.18.0",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "1ff6fc2fce24e5c870d3e2c9cd424057d390b44f",
  )


  # com.google.cloud:google-cloud-core-grpc:jar:1.52.0 wanted version 1.15.0
  # com.google.api:gax-grpc:jar:1.34.0 wanted version 1.15.0
  # io.grpc:grpc-all:jar:1.9.0
  native.maven_jar(
      name = "io_grpc_grpc_protobuf",
      artifact = "io.grpc:grpc-protobuf:1.9.0",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "94ca247577e4cf1a38d5ac9d536ac1d426a1ccc5",
  )


  # io.netty:netty-handler-proxy:jar:4.1.17.Final
  native.maven_jar(
      name = "io_netty_netty_codec_socks",
      artifact = "io.netty:netty-codec-socks:4.1.17.Final",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "a159bf1f3d5019e0d561c92fbbec8400967471fa",
  )


  # io.netty:netty-codec-http:jar:4.1.17.Final
  # io.netty:netty-codec-socks:jar:4.1.17.Final got requested version
  # io.netty:netty-handler:jar:4.1.17.Final got requested version
  native.maven_jar(
      name = "io_netty_netty_codec",
      artifact = "io.netty:netty-codec:4.1.17.Final",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "1d00f56dc9e55203a4bde5aae3d0828fdeb818e7",
  )


  # io.netty:netty-transport:jar:4.1.17.Final
  # io.netty:netty-handler:jar:4.1.17.Final got requested version
  native.maven_jar(
      name = "io_netty_netty_buffer",
      artifact = "io.netty:netty-buffer:4.1.17.Final",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "fdd68fb3defd7059a7392b9395ee941ef9bacc25",
  )


  # com.google.cloud:google-cloud-trace:jar:0.70.0-beta got requested version
  # com.google.cloud:google-cloud-monitoring:jar:1.52.0
  native.maven_jar(
      name = "com_google_cloud_google_cloud_core_grpc",
      artifact = "com.google.cloud:google-cloud-core-grpc:1.52.0",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "42db3e83fe4a2b207d00759f20b22e09e1bd19e8",
  )


  # io.grpc:grpc-all:jar:1.9.0
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


  # io.opencensus:opencensus-contrib-monitored-resource-util:jar:0.18.0 got requested version
  # io.opencensus:opencensus-exporter-trace-stackdriver:jar:0.18.0 got requested version
  # io.opencensus:opencensus-impl:jar:0.18.0 got requested version
  # io.opencensus:opencensus-contrib-grpc-metrics:jar:0.10.0 wanted version 0.10.0
  # io.opencensus:opencensus-exporter-stats-prometheus:jar:0.18.0 got requested version
  # io.opencensus:opencensus-contrib-zpages:jar:0.18.0 got requested version
  # io.opencensus:opencensus-exporter-stats-stackdriver:jar:0.18.0 got requested version
  # io.opencensus:opencensus-impl-core:jar:0.18.0 got requested version
  # io.opencensus:opencensus-exporter-trace-logging:jar:0.18.0 got requested version
  native.maven_jar(
      name = "io_opencensus_opencensus_api",
      artifact = "io.opencensus:opencensus-api:0.18.0",
      sha1 = "b89a8f8dfd1e1e0d68d83c82a855624814b19a6e",
  )


  # io.grpc:grpc-testing:jar:1.9.0
  native.maven_jar(
      name = "junit_junit",
      artifact = "junit:junit:4.12",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "2973d150c0dc1fefe998f834810d68f278ea58ec",
  )


  # io.prometheus:simpleclient_httpserver:bundle:0.4.0 wanted version 0.3.0
  # io.prometheus:simpleclient_common:bundle:0.4.0 wanted version 0.3.0
  # io.opencensus:opencensus-exporter-stats-prometheus:jar:0.18.0
  native.maven_jar(
      name = "io_prometheus_simpleclient",
      artifact = "io.prometheus:simpleclient:0.4.0",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "99c293bbf9461587b2179273b6fdc349582a1021",
  )


  # com.google.guava:guava:bundle:23.0
  native.maven_jar(
      name = "org_codehaus_mojo_animal_sniffer_annotations",
      artifact = "org.codehaus.mojo:animal-sniffer-annotations:1.14",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "775b7e22fb10026eed3f86e8dc556dfafe35f2d5",
  )


  native.maven_jar(
      name = "io_opencensus_opencensus_exporter_stats_stackdriver",
      artifact = "io.opencensus:opencensus-exporter-stats-stackdriver:0.18.0",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "55a833a0923e25c9f8ee0f3f79e261c7490758f7",
  )


  # io.netty:netty-handler-proxy:jar:4.1.17.Final got requested version
  # io.netty:netty-codec-http2:jar:4.1.17.Final
  native.maven_jar(
      name = "io_netty_netty_codec_http",
      artifact = "io.netty:netty-codec-http:4.1.17.Final",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "251d7edcb897122b9b23f24ff793cd0739056b9e",
  )


  # org.apache.httpcomponents:httpclient:jar:4.5.3
  native.maven_jar(
      name = "commons_logging_commons_logging",
      artifact = "commons-logging:commons-logging:1.2",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "4bfc12adfe4842bf07b657f0369c4cb522955686",
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


  # com.google.http-client:google-http-client:jar:1.24.1
  native.maven_jar(
      name = "org_apache_httpcomponents_httpclient",
      artifact = "org.apache.httpcomponents:httpclient:4.5.3",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "d1577ae15f01ef5438c5afc62162457c00a34713",
  )


  # com.google.cloud:google-cloud-core:jar:1.52.0
  native.maven_jar(
      name = "com_google_api_grpc_proto_google_iam_v1",
      artifact = "com.google.api.grpc:proto-google-iam-v1:0.12.0",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "ea312c0250a5d0a7cdd1b20bc2c3259938b79855",
  )


  # com.google.cloud:google-cloud-core-grpc:jar:1.52.0 wanted version 1.15.0
  # io.opencensus:opencensus-api:jar:0.10.0 wanted version 1.8.0
  # io.grpc:grpc-all:jar:1.9.0 got requested version
  # io.grpc:grpc-core:jar:1.9.0
  native.maven_jar(
      name = "io_grpc_grpc_context",
      artifact = "io.grpc:grpc-context:1.9.0",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "28b0836f48c9705abf73829bbc536dba29a1329a",
  )


  # com.google.cloud:google-cloud-core-grpc:jar:1.52.0
  native.maven_jar(
      name = "com_google_api_gax_grpc",
      artifact = "com.google.api:gax-grpc:1.34.0",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "af0663ea3d8549a1b9515125eea4707c19b75d2f",
  )


  # com.google.api.grpc:proto-google-cloud-trace-v1:jar:0.35.0 wanted version 1.12.0
  # com.google.api.grpc:proto-google-iam-v1:jar:0.12.0 wanted version 1.11.0
  # com.google.api.grpc:proto-google-cloud-trace-v2:jar:0.35.0 wanted version 1.12.0
  # com.google.api.grpc:proto-google-cloud-monitoring-v3:jar:1.34.0 wanted version 1.12.0
  # io.grpc:grpc-protobuf:jar:1.9.0
  # com.google.api:gax-grpc:jar:1.34.0 wanted version 1.12.0
  # com.google.cloud:google-cloud-core:jar:1.52.0 wanted version 1.12.0
  native.maven_jar(
      name = "com_google_api_grpc_proto_google_common_protos",
      artifact = "com.google.api.grpc:proto-google-common-protos:1.0.0",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "86f070507e28b930e50d218ee5b6788ef0dd05e6",
  )


  native.maven_jar(
      name = "io_opencensus_opencensus_contrib_zpages",
      artifact = "io.opencensus:opencensus-contrib-zpages:0.18.0",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "bc77c27de8abc0f47da5c4562c73d8600f94d2c1",
  )


  # io.grpc:grpc-protobuf-lite:jar:1.9.0 wanted version 19.0
  # io.opencensus:opencensus-exporter-stats-prometheus:jar:0.18.0 wanted version 20.0
  # io.opencensus:opencensus-exporter-stats-stackdriver:jar:0.18.0 wanted version 20.0
  # io.opencensus:opencensus-exporter-trace-stackdriver:jar:0.18.0 wanted version 20.0
  # com.google.instrumentation:instrumentation-api:jar:0.4.3 wanted version 19.0
  # io.grpc:grpc-protobuf:jar:1.9.0 wanted version 19.0
  # io.grpc:grpc-protobuf-nano:jar:1.9.0 wanted version 19.0
  # io.grpc:grpc-core:jar:1.9.0 wanted version 19.0
  # com.google.protobuf:protobuf-java-util:bundle:3.5.1 wanted version 19.0
  # io.opencensus:opencensus-api:jar:0.10.0 wanted version 19.0
  # io.opencensus:opencensus-contrib-zpages:jar:0.18.0 wanted version 20.0
  # io.opencensus:opencensus-exporter-trace-logging:jar:0.18.0 wanted version 20.0
  # io.opencensus:opencensus-impl-core:jar:0.18.0 wanted version 20.0
  native.maven_jar(
      name = "com_google_guava_guava",
      artifact = "com.google.guava:guava:23.0",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "c947004bb13d18182be60077ade044099e4f26f1",
  )


  native.maven_jar(
      name = "io_grpc_grpc_all",
      artifact = "io.grpc:grpc-all:1.9.0",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "442dfac27fd072e15b7134ab02c2b38136036090",
  )


  # com.google.cloud:google-cloud-trace:jar:0.70.0-beta got requested version
  # com.google.cloud:google-cloud-monitoring:jar:1.52.0
  # com.google.cloud:google-cloud-core-grpc:jar:1.52.0 got requested version
  native.maven_jar(
      name = "com_google_cloud_google_cloud_core",
      artifact = "com.google.cloud:google-cloud-core:1.52.0",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "5105a96c24a7eddffe372985814cfdf0748352d6",
  )


  # io.opencensus:opencensus-exporter-stats-stackdriver:jar:0.18.0
  native.maven_jar(
      name = "com_google_cloud_google_cloud_monitoring",
      artifact = "com.google.cloud:google-cloud-monitoring:1.52.0",
      repository = "http://repo.maven.apache.org/maven2/",
      sha1 = "674488b1f73d305c24b78ca6238c81c7282f6944",
  )




def opencensus_java_libraries():
  native.java_library(
      name = "com_google_code_findbugs_jsr305",
      visibility = ["//visibility:public"],
      exports = ["@com_google_code_findbugs_jsr305//jar"],
  )


  native.java_library(
      name = "io_grpc_grpc_protobuf_lite",
      visibility = ["//visibility:public"],
      exports = ["@io_grpc_grpc_protobuf_lite//jar"],
      runtime_deps = [
          ":com_google_guava_guava",
          ":io_grpc_grpc_core",
      ],
  )


  native.java_library(
      name = "io_opencensus_opencensus_exporter_stats_prometheus",
      visibility = ["//visibility:public"],
      exports = ["@io_opencensus_opencensus_exporter_stats_prometheus//jar"],
      runtime_deps = [
          ":com_google_guava_guava",
          ":io_opencensus_opencensus_api",
          ":io_prometheus_simpleclient",
      ],
  )


  native.java_library(
      name = "com_google_auth_google_auth_library_oauth2_http",
      visibility = ["//visibility:public"],
      exports = ["@com_google_auth_google_auth_library_oauth2_http//jar"],
      runtime_deps = [
          ":com_google_auth_google_auth_library_credentials",
          ":com_google_http_client_google_http_client",
          ":com_google_http_client_google_http_client_jackson2",
      ],
  )


  native.java_library(
      name = "io_netty_netty_transport",
      visibility = ["//visibility:public"],
      exports = ["@io_netty_netty_transport//jar"],
      runtime_deps = [
          ":io_netty_netty_buffer",
          ":io_netty_netty_common",
          ":io_netty_netty_resolver",
      ],
  )


  native.java_library(
      name = "io_netty_netty_handler_proxy",
      visibility = ["//visibility:public"],
      exports = ["@io_netty_netty_handler_proxy//jar"],
      runtime_deps = [
          ":io_netty_netty_codec",
          ":io_netty_netty_codec_http",
          ":io_netty_netty_codec_socks",
          ":io_netty_netty_transport",
      ],
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
          ":com_google_api_api_common",
          ":com_google_api_grpc_proto_google_cloud_trace_v1",
          ":com_google_api_grpc_proto_google_cloud_trace_v2",
          ":com_google_api_grpc_proto_google_common_protos",
          ":com_google_cloud_google_cloud_core",
          ":com_google_cloud_google_cloud_core_grpc",
          ":com_google_protobuf_protobuf_java",
          ":io_grpc_grpc_auth",
          ":io_grpc_grpc_netty_shaded",
          ":io_grpc_grpc_stub",
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
      name = "io_netty_netty_resolver",
      visibility = ["//visibility:public"],
      exports = ["@io_netty_netty_resolver//jar"],
      runtime_deps = [
          ":io_netty_netty_common",
      ],
  )


  native.java_library(
      name = "com_squareup_okio_okio",
      visibility = ["//visibility:public"],
      exports = ["@com_squareup_okio_okio//jar"],
  )


  native.java_library(
      name = "com_google_protobuf_protobuf_java_util",
      visibility = ["//visibility:public"],
      exports = ["@com_google_protobuf_protobuf_java_util//jar"],
      runtime_deps = [
          ":com_google_code_gson_gson",
          ":com_google_guava_guava",
          ":com_google_protobuf_protobuf_java",
      ],
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
      name = "io_netty_netty_common",
      visibility = ["//visibility:public"],
      exports = ["@io_netty_netty_common//jar"],
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
      name = "io_grpc_grpc_netty_shaded",
      visibility = ["//visibility:public"],
      exports = ["@io_grpc_grpc_netty_shaded//jar"],
      runtime_deps = [
          ":io_grpc_grpc_core",
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
      name = "io_netty_netty_handler",
      visibility = ["//visibility:public"],
      exports = ["@io_netty_netty_handler//jar"],
      runtime_deps = [
          ":io_netty_netty_buffer",
          ":io_netty_netty_codec",
          ":io_netty_netty_transport",
      ],
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
      runtime_deps = [
          ":com_google_code_findbugs_jsr305",
          ":com_google_guava_guava",
      ],
  )


  native.java_library(
      name = "com_google_http_client_google_http_client_jackson2",
      visibility = ["//visibility:public"],
      exports = ["@com_google_http_client_google_http_client_jackson2//jar"],
  )


  native.java_library(
      name = "io_opencensus_opencensus_exporter_trace_logging",
      visibility = ["//visibility:public"],
      exports = ["@io_opencensus_opencensus_exporter_trace_logging//jar"],
      runtime_deps = [
          ":com_google_guava_guava",
          ":io_opencensus_opencensus_api",
      ],
  )


  native.java_library(
      name = "io_grpc_grpc_auth",
      visibility = ["//visibility:public"],
      exports = ["@io_grpc_grpc_auth//jar"],
      runtime_deps = [
          ":com_google_auth_google_auth_library_credentials",
          ":com_google_code_findbugs_jsr305",
          ":com_google_errorprone_error_prone_annotations",
          ":com_google_guava_guava",
          ":com_google_instrumentation_instrumentation_api",
          ":io_grpc_grpc_context",
          ":io_grpc_grpc_core",
          ":io_opencensus_opencensus_api",
          ":io_opencensus_opencensus_contrib_grpc_metrics",
      ],
  )


  native.java_library(
      name = "com_google_api_gax",
      visibility = ["//visibility:public"],
      exports = ["@com_google_api_gax//jar"],
      runtime_deps = [
          ":com_google_api_api_common",
          ":com_google_auth_google_auth_library_credentials",
          ":com_google_auth_google_auth_library_oauth2_http",
          ":com_google_http_client_google_http_client",
          ":com_google_http_client_google_http_client_jackson2",
          ":org_threeten_threetenbp",
      ],
  )


  native.java_library(
      name = "io_opencensus_opencensus_exporter_trace_stackdriver",
      visibility = ["//visibility:public"],
      exports = ["@io_opencensus_opencensus_exporter_trace_stackdriver//jar"],
      runtime_deps = [
          ":com_google_api_api_common",
          ":com_google_api_grpc_proto_google_cloud_trace_v1",
          ":com_google_api_grpc_proto_google_cloud_trace_v2",
          ":com_google_api_grpc_proto_google_common_protos",
          ":com_google_auth_google_auth_library_credentials",
          ":com_google_cloud_google_cloud_core",
          ":com_google_cloud_google_cloud_core_grpc",
          ":com_google_cloud_google_cloud_trace",
          ":com_google_guava_guava",
          ":com_google_protobuf_protobuf_java",
          ":io_grpc_grpc_auth",
          ":io_grpc_grpc_netty_shaded",
          ":io_grpc_grpc_stub",
          ":io_opencensus_opencensus_api",
          ":io_opencensus_opencensus_contrib_monitored_resource_util",
      ],
  )


  native.java_library(
      name = "com_google_j2objc_j2objc_annotations",
      visibility = ["//visibility:public"],
      exports = ["@com_google_j2objc_j2objc_annotations//jar"],
  )


  native.java_library(
      name = "io_grpc_grpc_core",
      visibility = ["//visibility:public"],
      exports = ["@io_grpc_grpc_core//jar"],
      runtime_deps = [
          ":com_google_code_findbugs_jsr305",
          ":com_google_errorprone_error_prone_annotations",
          ":com_google_guava_guava",
          ":com_google_instrumentation_instrumentation_api",
          ":io_grpc_grpc_context",
          ":io_opencensus_opencensus_api",
          ":io_opencensus_opencensus_contrib_grpc_metrics",
      ],
  )


  native.java_library(
      name = "io_opencensus_opencensus_contrib_monitored_resource_util",
      visibility = ["//visibility:public"],
      exports = ["@io_opencensus_opencensus_contrib_monitored_resource_util//jar"],
      runtime_deps = [
          ":io_opencensus_opencensus_api",
      ],
  )


  native.java_library(
      name = "joda_time_joda_time",
      visibility = ["//visibility:public"],
      exports = ["@joda_time_joda_time//jar"],
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
      name = "org_apache_httpcomponents_httpcore",
      visibility = ["//visibility:public"],
      exports = ["@org_apache_httpcomponents_httpcore//jar"],
  )


  native.java_library(
      name = "com_lmax_disruptor",
      visibility = ["//visibility:public"],
      exports = ["@com_lmax_disruptor//jar"],
  )


  native.java_library(
      name = "com_google_protobuf_protobuf_java",
      visibility = ["//visibility:public"],
      exports = ["@com_google_protobuf_protobuf_java//jar"],
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
      name = "io_grpc_grpc_stub",
      visibility = ["//visibility:public"],
      exports = ["@io_grpc_grpc_stub//jar"],
      runtime_deps = [
          ":io_grpc_grpc_core",
      ],
  )


  native.java_library(
      name = "io_opencensus_opencensus_impl",
      visibility = ["//visibility:public"],
      exports = ["@io_opencensus_opencensus_impl//jar"],
      runtime_deps = [
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
          ":com_google_code_gson_gson",
          ":com_google_guava_guava",
          ":com_google_protobuf_protobuf_java",
          ":com_google_protobuf_protobuf_java_util",
          ":io_grpc_grpc_core",
          ":io_grpc_grpc_protobuf_lite",
      ],
  )


  native.java_library(
      name = "io_netty_netty_codec_socks",
      visibility = ["//visibility:public"],
      exports = ["@io_netty_netty_codec_socks//jar"],
      runtime_deps = [
          ":io_netty_netty_codec",
      ],
  )


  native.java_library(
      name = "io_netty_netty_codec",
      visibility = ["//visibility:public"],
      exports = ["@io_netty_netty_codec//jar"],
      runtime_deps = [
          ":io_netty_netty_buffer",
          ":io_netty_netty_common",
          ":io_netty_netty_resolver",
          ":io_netty_netty_transport",
      ],
  )


  native.java_library(
      name = "io_netty_netty_buffer",
      visibility = ["//visibility:public"],
      exports = ["@io_netty_netty_buffer//jar"],
      runtime_deps = [
          ":io_netty_netty_common",
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
          ":com_google_protobuf_protobuf_java",
          ":com_google_protobuf_protobuf_java_util",
          ":io_grpc_grpc_auth",
          ":io_grpc_grpc_context",
          ":io_grpc_grpc_core",
          ":io_grpc_grpc_netty_shaded",
          ":io_grpc_grpc_protobuf",
          ":io_grpc_grpc_stub",
          ":org_threeten_threetenbp",
      ],
  )


  native.java_library(
      name = "io_grpc_grpc_netty",
      visibility = ["//visibility:public"],
      exports = ["@io_grpc_grpc_netty//jar"],
      runtime_deps = [
          ":io_grpc_grpc_core",
          ":io_netty_netty_buffer",
          ":io_netty_netty_codec",
          ":io_netty_netty_codec_http",
          ":io_netty_netty_codec_http2",
          ":io_netty_netty_codec_socks",
          ":io_netty_netty_common",
          ":io_netty_netty_handler",
          ":io_netty_netty_handler_proxy",
          ":io_netty_netty_resolver",
          ":io_netty_netty_transport",
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
      name = "junit_junit",
      visibility = ["//visibility:public"],
      exports = ["@junit_junit//jar"],
      runtime_deps = [
          ":org_hamcrest_hamcrest_core",
      ],
  )


  native.java_library(
      name = "io_prometheus_simpleclient",
      visibility = ["//visibility:public"],
      exports = ["@io_prometheus_simpleclient//jar"],
  )


  native.java_library(
      name = "org_codehaus_mojo_animal_sniffer_annotations",
      visibility = ["//visibility:public"],
      exports = ["@org_codehaus_mojo_animal_sniffer_annotations//jar"],
  )


  native.java_library(
      name = "io_opencensus_opencensus_exporter_stats_stackdriver",
      visibility = ["//visibility:public"],
      exports = ["@io_opencensus_opencensus_exporter_stats_stackdriver//jar"],
      runtime_deps = [
          ":com_google_api_api_common",
          ":com_google_api_gax",
          ":com_google_api_gax_grpc",
          ":com_google_api_grpc_proto_google_cloud_monitoring_v3",
          ":com_google_api_grpc_proto_google_common_protos",
          ":com_google_api_grpc_proto_google_iam_v1",
          ":com_google_auth_google_auth_library_credentials",
          ":com_google_auth_google_auth_library_oauth2_http",
          ":com_google_cloud_google_cloud_core",
          ":com_google_cloud_google_cloud_core_grpc",
          ":com_google_cloud_google_cloud_monitoring",
          ":com_google_guava_guava",
          ":com_google_http_client_google_http_client",
          ":com_google_http_client_google_http_client_jackson2",
          ":com_google_protobuf_protobuf_java",
          ":com_google_protobuf_protobuf_java_util",
          ":commons_codec_commons_codec",
          ":commons_logging_commons_logging",
          ":io_grpc_grpc_auth",
          ":io_grpc_grpc_context",
          ":io_grpc_grpc_core",
          ":io_grpc_grpc_netty_shaded",
          ":io_grpc_grpc_protobuf",
          ":io_grpc_grpc_stub",
          ":io_opencensus_opencensus_api",
          ":io_opencensus_opencensus_contrib_monitored_resource_util",
          ":joda_time_joda_time",
          ":org_apache_httpcomponents_httpclient",
          ":org_apache_httpcomponents_httpcore",
          ":org_threeten_threetenbp",
      ],
  )


  native.java_library(
      name = "io_netty_netty_codec_http",
      visibility = ["//visibility:public"],
      exports = ["@io_netty_netty_codec_http//jar"],
      runtime_deps = [
          ":io_netty_netty_buffer",
          ":io_netty_netty_codec",
          ":io_netty_netty_common",
          ":io_netty_netty_resolver",
          ":io_netty_netty_transport",
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
      runtime_deps = [
          ":io_netty_netty_buffer",
          ":io_netty_netty_codec",
          ":io_netty_netty_codec_http",
          ":io_netty_netty_common",
          ":io_netty_netty_handler",
          ":io_netty_netty_resolver",
          ":io_netty_netty_transport",
      ],
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
          ":io_grpc_grpc_netty_shaded",
          ":io_grpc_grpc_protobuf",
          ":io_grpc_grpc_stub",
          ":org_threeten_threetenbp",
      ],
  )


  native.java_library(
      name = "com_google_api_grpc_proto_google_common_protos",
      visibility = ["//visibility:public"],
      exports = ["@com_google_api_grpc_proto_google_common_protos//jar"],
  )


  native.java_library(
      name = "io_opencensus_opencensus_contrib_zpages",
      visibility = ["//visibility:public"],
      exports = ["@io_opencensus_opencensus_contrib_zpages//jar"],
      runtime_deps = [
          ":com_google_guava_guava",
          ":io_opencensus_opencensus_api",
          ":io_opencensus_opencensus_contrib_grpc_metrics",
      ],
  )


  native.java_library(
      name = "com_google_guava_guava",
      visibility = ["//visibility:public"],
      exports = ["@com_google_guava_guava//jar"],
      runtime_deps = [
          ":com_google_code_findbugs_jsr305",
          ":com_google_errorprone_error_prone_annotations",
          ":com_google_j2objc_j2objc_annotations",
          ":org_codehaus_mojo_animal_sniffer_annotations",
      ],
  )


  native.java_library(
      name = "io_grpc_grpc_all",
      visibility = ["//visibility:public"],
      exports = ["@io_grpc_grpc_all//jar"],
      runtime_deps = [
          ":com_google_api_grpc_proto_google_common_protos",
          ":com_google_auth_google_auth_library_credentials",
          ":com_google_code_findbugs_jsr305",
          ":com_google_code_gson_gson",
          ":com_google_errorprone_error_prone_annotations",
          ":com_google_guava_guava",
          ":com_google_instrumentation_instrumentation_api",
          ":com_google_protobuf_nano_protobuf_javanano",
          ":com_google_protobuf_protobuf_java",
          ":com_google_protobuf_protobuf_java_util",
          ":com_squareup_okhttp_okhttp",
          ":com_squareup_okio_okio",
          ":io_grpc_grpc_auth",
          ":io_grpc_grpc_context",
          ":io_grpc_grpc_core",
          ":io_grpc_grpc_netty",
          ":io_grpc_grpc_okhttp",
          ":io_grpc_grpc_protobuf",
          ":io_grpc_grpc_protobuf_lite",
          ":io_grpc_grpc_protobuf_nano",
          ":io_grpc_grpc_stub",
          ":io_grpc_grpc_testing",
          ":io_netty_netty_buffer",
          ":io_netty_netty_codec",
          ":io_netty_netty_codec_http",
          ":io_netty_netty_codec_http2",
          ":io_netty_netty_codec_socks",
          ":io_netty_netty_common",
          ":io_netty_netty_handler",
          ":io_netty_netty_handler_proxy",
          ":io_netty_netty_resolver",
          ":io_netty_netty_transport",
          ":io_opencensus_opencensus_api",
          ":io_opencensus_opencensus_contrib_grpc_metrics",
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
          ":com_google_api_api_common",
          ":com_google_api_gax",
          ":com_google_api_grpc_proto_google_common_protos",
          ":com_google_api_grpc_proto_google_iam_v1",
          ":com_google_auth_google_auth_library_credentials",
          ":com_google_auth_google_auth_library_oauth2_http",
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
          ":com_google_api_gax",
          ":com_google_api_gax_grpc",
          ":com_google_api_grpc_proto_google_cloud_monitoring_v3",
          ":com_google_api_grpc_proto_google_common_protos",
          ":com_google_api_grpc_proto_google_iam_v1",
          ":com_google_auth_google_auth_library_credentials",
          ":com_google_auth_google_auth_library_oauth2_http",
          ":com_google_cloud_google_cloud_core",
          ":com_google_cloud_google_cloud_core_grpc",
          ":com_google_http_client_google_http_client",
          ":com_google_http_client_google_http_client_jackson2",
          ":com_google_protobuf_protobuf_java",
          ":com_google_protobuf_protobuf_java_util",
          ":commons_codec_commons_codec",
          ":commons_logging_commons_logging",
          ":io_grpc_grpc_auth",
          ":io_grpc_grpc_context",
          ":io_grpc_grpc_core",
          ":io_grpc_grpc_netty_shaded",
          ":io_grpc_grpc_protobuf",
          ":io_grpc_grpc_stub",
          ":joda_time_joda_time",
          ":org_apache_httpcomponents_httpclient",
          ":org_apache_httpcomponents_httpcore",
          ":org_threeten_threetenbp",
      ],
  )


