package com.rd162.web;

import com.rd162.rpc.HelloReply;
import com.rd162.rpc.HelloWorldClient;
import com.rabbitmq.client.Connection;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Channel;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class HelloController {

    private static final String rpcHost = "rpc-service.default.svc.cluster.local";
    private static final String rabbitHost = "rabbitmq.default.svc.cluster.local";
    private static final String EXCHANGE_NAME = "PubSub";

    private final ConnectionFactory factory = new ConnectionFactory();

    public HelloController() {
        factory.setHost(rabbitHost);
    }

    @GetMapping("/hello")
    public HelloResponse hello() throws InterruptedException {
        HelloWorldClient client = new HelloWorldClient(rpcHost, 50051);
        try {
            HelloReply message = client.greet("Dima");
            return new HelloResponse(message.getMessage());
        } finally {
            client.shutdown();
        }
    }

    @PostMapping("/publish")
    public void publish(@RequestBody PublishRequest request)
            throws IOException, TimeoutException, UnsupportedEncodingException {
        try (Connection connection = factory.newConnection(); Channel channel = connection.createChannel()) {

            channel.exchangeDeclare(EXCHANGE_NAME, "fanout");

            String message = request.getMessage();
            channel.basicPublish(EXCHANGE_NAME, "", null, message.getBytes("UTF-8"));
        }
    }
}