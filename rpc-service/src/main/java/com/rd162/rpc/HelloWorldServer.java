package com.rd162.rpc;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

import java.io.Closeable;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * Server that manages startup/shutdown of a Greeter server.
 */
public class HelloWorldServer implements Closeable {
    private static final Logger logger = Logger.getLogger(HelloWorldServer.class.getName());

    private Server server;

    private void start() throws IOException {
        int port = 50051; // The port on which the server should run
        server = ServerBuilder.forPort(port).addService(new GreeterImpl()).build().start();
        logger.info("gRPC Server started, listening on " + port);
        try {
            server.awaitTermination();
        } catch (InterruptedException e) {
        }
    }

    private void shutdown() {
        if (server != null) {
            server.shutdown();
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        try (HelloWorldServer server = new HelloWorldServer()) {
            server.start();
        }
    }

    static class GreeterImpl extends GreeterGrpc.GreeterImplBase {

        @Override
        public void sayHello(HelloRequest req, StreamObserver<HelloReply> responseObserver) {
            HelloReply reply = HelloReply.newBuilder().setMessage("Hello, " + req.getName() + "! :)").build();
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }
    }

    @Override
    public void close() throws IOException {
        shutdown();
    }
}