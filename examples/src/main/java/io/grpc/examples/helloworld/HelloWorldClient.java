
package io.grpc.examples.helloworld;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.*;

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

/**
 * A simple client that requests a greeting from the {@link HelloWorldServer}.
 */
public class HelloWorldClient {
	private static final Logger logger = Logger.getLogger(HelloWorldClient.class.getName());

	private final ManagedChannel channel;
	private final GreeterGrpc.GreeterBlockingStub blockingStub;
	private String host;

	/**
	 * Construct client connecting to HelloWorld server at {@code host:port}.
	 */
	public HelloWorldClient(String host, int port) {
		this(ManagedChannelBuilder.forAddress(host, port)
				// Channels are secure by default (via SSL/TLS). For the example
				// we disable TLS to avoid
				// needing certificates.
				.usePlaintext(true).build());
		this.host = host;
	}

	public String getHost() {
		return host;
	}
	
	/**
	 * Construct client for accessing RouteGuide server using the existing
	 * channel.
	 */
	HelloWorldClient(ManagedChannel channel) {
		this.channel = channel;
		blockingStub = GreeterGrpc.newBlockingStub(channel);
	}

	public void shutdown() throws InterruptedException {
		channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
	}

	public void greet(String message, String userName) throws StatusRuntimeException {
		logger.info("Will try to send message: " + '"' + message + '"' + " to " + userName);
		HelloRequest request = HelloRequest.newBuilder().setName(message).build();
		HelloReply response = null;
		response = blockingStub.sayHello(request);
		if (response != null) {
			logger.info("Greeting: " + response.getMessage());
		}
	}

	/**
	 * Greet server. If provided, the first element of {@code args} is the name
	 * to use in the greeting.
	 */
	public static void main(String[] args) {
		String IP = "localhost"; // Can be local IP
		Map<String, String> IPMap = new HashMap<String, String>();
		IPMap.put("localhost", "My server");
		IPMap.put("128.237.129.30", "User1");
		IPMap.put("128.237.130.169", "User2");

		HelloWorldClient[] clients = new HelloWorldClient[1];

		clients[0] = new HelloWorldClient(IP, 50051);
		// clients[1] = new HelloWorldClient(IP1, 50051);
		// clients[2] = new HelloWorldClient(IP2, 50051);

		/* Access a service running on the local machine on port 50051 */
		String user = "world";
		if (args.length > 0) {
			user = args[0]; /* Use the arg as the name to greet if provided */
		}
		for (int i = 0; i < 1; i++) {
			try {
				clients[i].greet(user, IPMap.get(clients[i].getHost()));
			} catch (Exception e) {
				System.out.println(IPMap.get(clients[i].getHost()) + " is offline.");
			} finally {
				try {
					clients[i].shutdown();
				} catch (Exception ex) {
					System.out.println("Error: shutdown " + clients[i].getHost());
				}
			}
		}
	}
}
