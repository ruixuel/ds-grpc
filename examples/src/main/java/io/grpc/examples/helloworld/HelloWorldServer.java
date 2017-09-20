/*
 * Copyright 2015, gRPC Authors All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.grpc.examples.helloworld;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import java.io.IOException;
import java.util.logging.Logger;
import java.util.*;

/**
 * Server that manages startup/shutdown of a {@code Greeter} server.
 */
public class HelloWorldServer {
	private static final Logger logger = Logger.getLogger(HelloWorldServer.class.getName());

	private Server server;
	private static Map<String, String> IPMap = new HashMap<String, String>();
	/* The port on which the server should run */
	private final static int PORT = 50051;
	private final static String NAME = "Ruixue"; // Put your name here
	private static String messageSent = null;

	private void start() throws IOException {
		server = ServerBuilder.forPort(PORT).addService(new GreeterImpl()).build().start();
		logger.info("Server started, listening on " + PORT);
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				// Use stderr here since the logger may have been reset by its
				// JVM shutdown hook.
				System.err.println("*** shutting down gRPC server since JVM is shutting down");
				HelloWorldServer.this.stop();
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
	 * Await termination on the main thread since the grpc library uses daemon
	 * threads.
	 */
	private void blockUntilShutdown() throws InterruptedException {
		if (server != null) {
			server.awaitTermination();
		}
	}

	/**
	 * Initialize IP List
	 */
	private void initalizeIPList() throws InterruptedException {
		IPMap.put("192.168.1.103", "Wenyue");
		// IPMap.put("128.237.129.30", "User2");
		// IPMap.put("128.237.130.31", "User3");
		// IPMap.put("128.237.130.32", "User4");
		// IPMap.put("128.237.130.33", "User5");

	}

	/**
	 * Main launches the server from the command line.
	 */
	public static void main(String[] args) throws IOException, InterruptedException {
		final HelloWorldServer server = new HelloWorldServer();
		server.start();
		server.initalizeIPList();
		server.blockUntilShutdown();
	}

	static class GreeterImpl extends GreeterGrpc.GreeterImplBase {

		@Override
		public void sayHello(HelloRequest req, StreamObserver<HelloReply> responseObserver) {
			if (!req.getName().equals(messageSent)) {
				messageSent = req.getName();
				System.out.println("Recieve message: " + req.getName());

				HelloWorldClient[] clients = new HelloWorldClient[IPMap.size()];
				int i = 0;
				for (Map.Entry<String, String> ip : IPMap.entrySet()) {
					clients[i++] = new HelloWorldClient(ip.getKey(), PORT);
				}

				String user = req.getName();
				for (int j = 0; j < clients.length; j++) {
					try {
						clients[j].greet(user, IPMap.get(clients[j].getHost()));
					} catch (Exception e) {
						System.out.println(IPMap.get(clients[j].getHost()) + " is offline.");
						IPMap.remove(clients[j].getHost());
					} finally {
						System.out.println("You have " + IPMap.size() + " IPs now.");
						try {
							clients[j].shutdown();
						} catch (Exception e) {
							System.out.println("Error: shutdown " + clients[j].getHost());
						}
					}
				}
			}

			// Put your reply here
			String replyMessage = "Reply from " + NAME + " Server: Message recieved!";
			HelloReply reply = HelloReply.newBuilder().setMessage(replyMessage).build();
			responseObserver.onNext(reply);
			responseObserver.onCompleted();
		}

	}
}
