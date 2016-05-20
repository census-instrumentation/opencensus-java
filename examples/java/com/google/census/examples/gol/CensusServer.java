/*
 * Copyright 2016, Google Inc. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *    * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following disclaimer
 * in the documentation and/or other materials provided with the
 * distribution.
 *
 *    * Neither the name of Google Inc. nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.google.census.examples.gol;

import com.google.census.CensusScope;
import com.google.census.CensusServerInterceptor;
import com.google.census.TagKey;
import com.google.census.TagMap;
import com.google.census.examples.CommandProcessorGrpc;
import com.google.census.examples.CommandRequest;
import com.google.census.examples.CommandResponse;
import com.google.common.flags.Flag;
import com.google.common.flags.FlagSpec;
import com.google.common.flags.Flags;
import com.google.net.rpc3.server.RpcServer;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.ServerInterceptors;
import io.grpc.stub.StreamObserver;

import java.util.logging.Logger;

/**
 * Census Game of Life Server.
 * <p>
 * Requests from the client are expected to be Strings of the form:
 * <p>
 *   {@code gol <integer number of generations> <string encoding of initial generation>}
 * <p>
 * The server parses the input then uses the GameOfLife class to return
 * a string encoding of the initial generation after the specified
 * number of generations.
 *
 * @author dpo@google.com (Dino Oliva)
 */
public class CensusServer {
  private static final Logger logger = Logger.getLogger(CensusServer.class.getName());
  private static final TagKey SERVER_KEY = new TagKey("census_gol_server");

  private final Server server;

  @FlagSpec(
      name = "port",
      help = "The Game-of-Life server stubby port.")
  private static final Flag<Integer> port = Flag.value(10000);

  /* The port on which the server should run */
  @FlagSpec(
      name = "gol_server_grpc_port",
      help = "The Game-of-Life server grpc port.")
  private static final Flag<Integer> golServerGrpcPort = Flag.value(20000);

  CensusServer() throws Exception {
    server = ServerBuilder.forPort(golServerGrpcPort.get())
        .addService(ServerInterceptors.intercept(CommandProcessorGrpc.bindService(
            new CommandProcessorImpl()), new CensusServerInterceptor()))
        .build()
        .start();
    logger.info("Server started, listening on " + golServerGrpcPort.get());
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        // Use stderr here since the logger may have been reset by its JVM shutdown hook.
        System.err.println("*** shutting down gRPC server since JVM is shutting down");
        CensusServer.this.stop();
        System.err.println("*** server shut down");
      }
    });
  }

  private void stop() {
    if (server != null) {
      server.shutdown();
    }
  }

  /**
   * Await termination on the main thread since the grpc library uses daemon threads.
   */
  private void blockUntilShutdown() throws InterruptedException {
    if (server != null) {
      server.awaitTermination();
    }
  }

  public static void main(String[] args) throws Exception {
    // TODO(dpo): this call is necessary to run the completion hook to enable CensusNative. In
    // general, G3 services built with gRPC will need to do this otherwise many libries will not
    // work correctly. This issue needs to be resolved with the gRPC team.
    Flags.parse(args);

    // TODO(dpo): remove stubby-based server for monitoring hooks once gRPC team has implemented
    // direct support.
    RpcServer.newBuilder(port.get()).createAndStart();
    logger.info("Server started, monitoring hooks on " + port.get());
    CensusServer server = new CensusServer();
    server.blockUntilShutdown();
  }

  private class CommandProcessorImpl implements CommandProcessorGrpc.CommandProcessor {
    @Override
    public void execute(CommandRequest request, StreamObserver<CommandResponse> responseObserver) {
      String retval = executeCommand(request.getReq());
      CommandResponse response = CommandResponse.newBuilder().setRetval(retval).build();
      responseObserver.onNext(response);
      responseObserver.onCompleted();
    }
  }

  private static String executeCommand(String command) {
    String[] decode = command.split(" ");
    if (decode.length == 3 && decode[0].equals("gol")) {
      int gens = toIntWithDefault(decode[1], 0);
      int dims = (int) Math.sqrt(decode[2].length());
      if (dims * dims == decode[2].length()) {
        // add server tag
        TagMap tags = TagMap.of(SERVER_KEY, genTagValue(dims, gens));
        try (CensusScope scope = new CensusScope(tags)) {
          return new GameOfLife(dims, decode[2]).calcNextGenerations(gens).encode();
        }
      }
    }
    return "Error: bad request";
  }

  private static String genTagValue(int dim, int gens) {
    return dim + "x" + dim + "-" + gens;
  }

  private static int toIntWithDefault(String s, int i) {
    try {
      return Integer.valueOf(s);
    } catch (NumberFormatException exn) {
      return i;
    }
  }

}
