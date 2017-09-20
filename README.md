gRPC-Java - An RPC library and framework

clone the git branch: git@github.com:ruixuel/ds-grpc.git

1. Navigate to the Java examples:

	$ cd grpc-java/examples

2. From the examples directory:

	Compile the client and server

	$ ./gradlew installDist

3. Run the server:

	$ ./build/install/examples/bin/hello-world-server

4. In another terminal, run the client with your message as the argument:

	$ ./build/install/examples/bin/hello-world-client {Your message}

Congratulations! You’ve just run a client-server application with gRPC.