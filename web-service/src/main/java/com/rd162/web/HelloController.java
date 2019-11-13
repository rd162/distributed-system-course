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

    private static final String RPC_HOST = "rpc-service.default.svc.cluster.local";
    private static final String RABBITMQ_HOST = "rabbitmq.default.svc.cluster.local";
    private static final String RABBITMQ_EXCHANGE_NAME = "PubSub";

    private final ConnectionFactory factory = new ConnectionFactory();

    public HelloController() {
        factory.setUsername("guest");
        factory.setPassword("guest");
        factory.setVirtualHost("/");
        factory.setHost(RABBITMQ_HOST);
        factory.setPort(5672);
    }

    @GetMapping("/hello")
    public HelloResponse hello() throws InterruptedException, IOException {
        try (HelloWorldClient client = new HelloWorldClient(RPC_HOST, 50051)) {
            HelloReply message = client.greet("Dima");
            return new HelloResponse(message.getMessage());
        }
    }

    @PostMapping("/publish")
    public void publish(@RequestBody PublishRequest request)
            throws IOException, TimeoutException, UnsupportedEncodingException {
        try (Connection connection = factory.newConnection(); Channel channel = connection.createChannel()) {

            channel.exchangeDeclare(RABBITMQ_EXCHANGE_NAME, "fanout");

            String message = request.getMessage();
            channel.basicPublish(RABBITMQ_EXCHANGE_NAME, "", null, message.getBytes("UTF-8"));
        }
    }
}