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

public class RabbitMQListenerServer {
    private static final String RABBITMQ_HOST = "rabbitmq.default.svc.cluster.local";
    private static final String RABBITMQ_EXCHANGE_NAME = "PubSub";
    private static final Logger logger = Logger.getLogger(RabbitMQListenerServer.class.getName());
    private ConnectionFactory factory = new ConnectionFactory();
    private Connection connection;
    private Channel channel;

    public RabbitMQListenerServer() {
        factory.setUsername("guest");
        factory.setPassword("guest");
        factory.setVirtualHost("/");
        factory.setHost(RABBITMQ_HOST);
        factory.setPort(5672);
    }

    public void start() {

        try {
            connection = factory.newConnection();
            channel = connection.createChannel();

            channel.exchangeDeclare(RABBITMQ_EXCHANGE_NAME, "fanout");

            String queueName = channel.queueDeclare().getQueue();
            channel.queueBind(queueName, RABBITMQ_EXCHANGE_NAME, "");

            boolean autoAck = false;
            channel.basicConsume(queueName, autoAck, new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties,
                        byte[] body) throws IOException {
                    long deliveryTag = envelope.getDeliveryTag();

                    String message = new String(body, "UTF-8");
                    logger.info("RabbitMQ message received: " + message);

                    channel.basicAck(deliveryTag, false);
                }
            });
        } catch (Exception e) {
            logger.warning("RabbitMQ listener failed to start: " + e);
        }
    }
}