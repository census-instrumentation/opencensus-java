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

import com.google.census.CensusClientInterceptor;
import com.google.census.examples.CommandProcessorGrpc;
import com.google.census.examples.CommandRequest;
import com.google.census.examples.CommandResponse;
import com.google.common.flags.Flag;
import com.google.common.flags.FlagSpec;
import com.google.common.flags.Flags;
import com.google.monitoring.runtime.HookManager;
import com.google.net.rpc3.server.RpcServer;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * A client for the Census Game-of-Life example.
 */
public class CensusClient {
  private static final Logger logger = Logger.getLogger(CensusClient.class.getName());

  private final ManagedChannel channel;
  private final CommandProcessorGrpc.CommandProcessorBlockingStub blockingStub;

  @FlagSpec(
      name = "port",
      help = "The Game-of-Life client stubby port.")
  private static final Flag<Integer> port = Flag.value(10001);

  @FlagSpec(
      name = "gol_server_grpc_port",
      help = "The Game-of-Life server gRPC port.")
  private static final Flag<Integer> golServerGrpcPort = Flag.value(20000);

  @FlagSpec(
      name = "gol_server_host",
      help = "The Game-of-Life server host.")
  private static final Flag<String> golServerHost = Flag.value("localhost");

  @FlagSpec(
      name = "gol_gens_per_rpc",
      help = "The number of generations to calculate per rpc.")
  private static final Flag<Integer> golGensPerRpc = Flag.value(1001);

  /** Construct client connecting to GameOfLife server at {@code host:port}. */
  public CensusClient(String host, int port) {
    channel = ManagedChannelBuilder.forAddress(host, port)
        .usePlaintext(true)
        .intercept(new CensusClientInterceptor())
        .build();
    blockingStub = CommandProcessorGrpc.newBlockingStub(channel);
  }

  public void shutdown() throws InterruptedException {
    channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
  }

  /**
   * Invokes the synchronous RPC call that remotely executes the census command.
   * Returns the result of executing the command.
   *
   * @param command the command string to send
   * @return the result of executing the command
   */
  public String executeCommand(String command) {
    try {
      CommandRequest request = CommandRequest.newBuilder().setReq(command).build();
      CommandResponse response = blockingStub.execute(request);
      return response.getRetval();
    } catch (RuntimeException e) {
      logger.log(Level.WARNING, "RPC failed", e);
      return "";
    }
  }

  /** Game of Life client. */
  public static void main(String[] args) throws Exception {
    // TODO(dpo): this call is necessary to run the completion hook to enable CensusNative. In
    // general, G3 services built with gRPC will need to do this otherwise many libries will not
    // work correctly. This issue needs to be resolved with the gRPC team.
    Flags.parse(args);

    HookManager.getManager().addHook(
        new CensusClientz(golServerHost.get(), golServerGrpcPort.get(), golGensPerRpc.get()));

    // TODO(dpo): remove stubby-based server for monitoring hooks once gRPC team has implemented
    // direct support.
    RpcServer.newBuilder(port.get()).createAndStart().blockUntilShutdown();
  }
}
