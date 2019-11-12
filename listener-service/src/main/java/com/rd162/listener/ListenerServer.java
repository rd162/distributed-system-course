package com.rd162.listener;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;

public class ListenerServer {
    private static final String EXCHANGE_NAME = "PubSub";
    private static final Logger logger = Logger.getLogger(ListenerServer.class.getName());
    private static Connection connection;
    private static Channel channel;

    private void start() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();

        factory.setUsername("guest");
        factory.setPassword("guest");
        factory.setVirtualHost("/");
        factory.setHost("rabbitmq.default.svc.cluster.local");
        factory.setPort(5672);

        connection = factory.newConnection();
        channel = connection.createChannel();

        channel.exchangeDeclare(EXCHANGE_NAME, "fanout");

        String queueName = channel.queueDeclare().getQueue();
        channel.queueBind(queueName, EXCHANGE_NAME, "");

        boolean autoAck = false;
        channel.basicConsume(queueName, autoAck, new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body)
                    throws IOException {
                long deliveryTag = envelope.getDeliveryTag();
                
                String message = new String(body, "UTF-8");
                logger.info("Message received: " + message);

                channel.basicAck(deliveryTag, false);
            }
        });
    }

    public static void main(String[] args) {
        final ListenerServer server = new ListenerServer();
        try {
            server.start();
        } catch (Exception e) {
            logger.warning("Listener failed to start: " + e);
            return;
        }
    }
}