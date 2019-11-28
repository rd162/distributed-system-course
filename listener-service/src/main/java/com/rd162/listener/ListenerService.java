package com.rd162.listener;

public class ListenerService {
    public static void main(String[] args) {
        // final RabbitMQListenerServer server = new RabbitMQListenerServer();
        // server.start();
        final KafkaListenerServer server = new KafkaListenerServer();
        server.start();
    }
}