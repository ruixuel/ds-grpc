gRPC-Java - An RPC library and framework

clone the git branch: git@github.com:ruixuel/ds-grpc.git

$ # Navigate to the Java examples:
$ cd grpc-java/examples

From the examples directory:

Compile the client and server

$ ./gradlew installDist

Run the server:
$ ./build/install/examples/bin/hello-world-server

In another terminal, run the client with your message as the argument:
$ ./build/install/examples/bin/hello-world-client {Your message}

Congratulations! You’ve just run a client-server application with gRPC.