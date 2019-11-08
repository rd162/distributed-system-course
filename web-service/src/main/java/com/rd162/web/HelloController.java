package com.rd162.web;

import com.rd162.rpc.HelloReply;
import com.rd162.rpc.HelloWorldClient;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class HelloController {

    private static final String rpcHost = "rpc-service.default.svc.cluster.local";

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
}