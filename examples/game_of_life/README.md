# OpenCensus Game of Life Codelab

This codelab demonstrates OpenCensus with a simple client/server Java example based on
[Conway's Game of Life](https://en.wikipedia.org/wiki/Conway%27s_Game_of_Life)
(abbreviated as Life for the remainder of this Codelab). You will learn how to define OpenCensus
tags, measures and views, how to add tags to the Life application, how to record stats against
current tag context, and how to view and understand the stats collected against those tags.

The Life application used in this Codelab consists of a Life client and a Life server.

The Life client consists of three two-dimensional matrices: 8x8, 16x16, and 32x32.
Each of the cells in the matrices can be alive or dead depending on some rules.
The client takes in the number of RPC calls from the user and sends that many requests to the server
to calculate the alive/dead cell configuration for the matrices. The server performs the
calculations and sends the response back to the client to display the
results.

## Prerequisites

1. Install [Bazel](https://docs.bazel.build/versions/master/install.html).
2. Install [Protobuf Compiler](https://github.com/google/protobuf#protocol-compiler-installation).
3. Get yourself familar with [OpenCensus basics](https://opencensus.io/).
4. Optionally, get yourself familar with [gRPC basics](https://grpc.io/docs/tutorials/basic/java.html).
5. Optionally, [set up a Google Cloud project and enable Stackdriver Monitoring](https://github.com/census-instrumentation/opencensus-java/tree/master/exporters/stats/stackdriver#prerequisites),
 if you would also like to view the stats on Stackdriver Monioring dashboard.

## Build and Run the Example

```bash
$ bazel build :all
$ bazel-bin/bazel-bin/game-of-life-server SERVER_PORT SERVER_ZPAGE_PORT CLOUD_PROJECT_ID PROMETHEUS_PORT
$ bazel-bin/bazel-bin/game-of-life-client SERVER_PORT CLIENT_ZPAGE_PORT CLOUD_PROJECT_ID
```
By default, SERVER_PORT is 3000, SERVER_ZPAGE_PORT is 9000, CLIENT_ZPAGE_PORT is 9001,
PROMETHEUS_PORT is 10000.

To view the game of life board and play the game, go to:  
localhost:CLIENT_ZPAGE_PORT/clientz

## Detailed Explanations on the Demo Code

TODO

## View Stats and Spans on ZPages

To view stats and/or spans on client side, go to:  
localhost:CLIENT_ZPAGE_PORT/rpcz  
localhost:CLIENT_ZPAGE_PORT/statsz  
localhost:CLIENT_ZPAGE_PORT/tracez

To view stats and/or spans on server side, go to:  
localhost:SERVER_ZPAGE_PORT/rpcz  
localhost:SERVER_ZPAGE_PORT/statsz  
localhost:SERVER_ZPAGE_PORT/tracez

## View Stats and Spans on Stackdriver Dashboard

If you specified a valid CLOUD_PROJECT_ID and have the appropriate Google credentials set, stats
from the Life client and server will be exported to Stackdriver Monitoring, and spans will be
exported to Stackdriver Trace.

Go to [Stackdriver Monitoring main page](https://app.google.stackdriver.com/), and follow the
[instructions](https://cloud.google.com/monitoring/charts/) to create a dashboard and charts. Then
you can view stats from Life application on those charts.

Go to [Stackdriver Trace console](https://console.cloud.google.com/traces/traces) to see traces.

## View Stats on Prometheus Metrics page

To view both server and client stats on Prometheus metrics page, go to:  
localhost:PROMETHEUS_PORT/metrics
