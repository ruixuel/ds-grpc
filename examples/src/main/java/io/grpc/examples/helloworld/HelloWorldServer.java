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
	/* The port on which the server should run */
	private final static int PORT = 50051;
	private final static String NAME = "Ruixue"; // Put your name here
	private static String messageSent = null;
	private static IPList ipList = IPList.getInstance();

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
		ipList.addUser("128.237.139.109", "User1");
		ipList.addUser("128.237.129.30", "User2");
		ipList.addUser("128.237.130.31", "User3");
		ipList.addUser("128.237.130.32", "User4");
		ipList.addUser("128.237.130.33", "User5");

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

				HelloWorldClient[] clients = new HelloWorldClient[ipList.getSize()];
				for (int i = 0; i < clients.length; i++) {
					User u = ipList.getUserByIndex(i);
					clients[i] = new HelloWorldClient(u.getIP(), u.getName(), PORT);
				}

				String user = req.getName();
				for (int j = 0; j < clients.length; j++) {
					try {
						clients[j].greet(user, clients[j].getName());
					} catch (Exception e) {
						System.out.println(clients[j].getName() + " is offline.");
						ipList.removeUser(clients[j].getName(), clients[j].getHost());
					} finally {
						System.out.println("You have " + ipList.getSize() + " IPs now.");
						try {
							clients[j].shutdown();
						} catch (Exception e) {
							System.out.println("Error: shutdown " + clients[j].getHost());
						}
					}
				}
				
				clients = new HelloWorldClient[ipList.getSize()];
				
				while(ipList.getSize() > 0 && ipList.getSize() < 5) {
					for (int i = 0; i < clients.length; i++) {
						User u = ipList.getUserByIndex(i);
						clients[i] = new HelloWorldClient(u.getIP(), u.getName(), PORT);
					}
					for (int j = 0; j < clients.length; j++) {
						try {
							clients[j].requestIPS("Request IP List", clients[j].getName());
						} catch (Exception e) {
							System.out.println(clients[j].getName() + " is offline.");
							ipList.removeUser(clients[j].getName(), clients[j].getHost());
						} finally {
							System.out.println("You have " + ipList.getSize() + " IPs now.");
							try {
								clients[j].shutdown();
							} catch (Exception e) {
								System.out.println("Error: shutdown " + clients[j].getHost());
							}
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

		@Override
		public void getIPs(HelloRequest req, StreamObserver<HelloReply> responseObserver) {
			StringBuffer sb = new StringBuffer();
//			Random rd = new Random();
//			int startIndex = rd.nextInt(5);
			//For Test
			List<User> list = new ArrayList<User>();
			list.add(new User("aaa", "192.168.130.1"));
			list.add(new User("bbb", "192.168.120.2"));
			list.add(new User("ccc", "192.168.130.3"));
			list.add(new User("ddd", "192.168.140.4"));
			list.add(new User("eee", "192.168.150.5"));
			
			int ip1 = 0;
			int ip2 = 0;
			
			if(list.size() != 1) {
				Random rd = new Random();
				ip1 = rd.nextInt(list.size());
				ip2 = rd.nextInt(list.size());
				while(ip1 == ip2) {
					ip2 = rd.nextInt(list.size());
				}
				User u1 = list.get(ip1);
				sb.append(u1.getIP() + "," + u1.getName() + ";");
				User u2 = list.get(ip2);
				sb.append(u2.getIP() + "," + u2.getName() + ";");
			}
			
//			if(ipList.getSize() != 1) {
//				Random rd = new Random();
//				ip1 = rd.nextInt(ipList.getSize());
//				ip2 = rd.nextInt(ipList.getSize());
//				while(ip1 == ip2) {
//					ip2 = rd.nextInt(ipList.getSize());
//				}
//				User u1 = ipList.getUserByIndex(ip1);
//				sb.append(u1.getIP() + "," + u1.getName() + ";");
//				User u2 = ipList.getUserByIndex(ip2);
//				sb.append(u2.getIP() + "," + u2.getName() + ";");
//			}
			
			
			HelloReply reply = HelloReply.newBuilder().setMessage(sb.toString()).build();
			responseObserver.onNext(reply);
			responseObserver.onCompleted();
		}

	}
}
