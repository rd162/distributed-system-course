package com.rd162.listener;

import com.rd162.eventbus.*;
import com.rd162.eventbus.kafka.*;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Logger;

public class KafkaListenerServer {
    private static final Logger logger = Logger.getLogger(KafkaListenerServer.class.getName());

    final static String TOPIC = "hello_topic";
    final static String CONSUMER_GROUP = "listener-service";
    final static String CONNECTION_STRING = "kafka.default.svc.cluster.local:9092";

    final static String HOSTNAME = System.getenv("HOSTNAME");

    public KafkaListenerServer() {
    }

    private void startProducer() throws IOException {
        logger.info("start produce messages");
        ProducerConfiguration configuration = new ProducerConfiguration() {
            @Override
            public String getConnection() {
                return CONNECTION_STRING;
            }

            @Override
            public Map<String, Object> getProperties() {
                return null;
            }

            @Override
            public String getTopic() {
                return TOPIC;
            }
        };

        EventSerializer<String> serializer = new EventSerializer<String>() {
            @Override
            public byte[] serialize(String dataObj) {
                return dataObj.getBytes();
            }
        };

        logger.info("create producer");
        
        try (KafkaEventProducer<String> producer = new KafkaEventProducer<String>(configuration, serializer)) {
            logger.info("start send messages");

            for (long i = 0;; i++) {
                producer.send(new Event<>("Hello, Kafka consumer! msg#" + i));                
            }
        }
    }

    private void startConsumer() throws IOException, InterruptedException {
        logger.info("start consumer HOSTNAME=" + HOSTNAME);
        ConsumerConfiguration configuration = new ConsumerConfiguration() {
            public Collection<String> getTopics() {
                return Collections.singleton(TOPIC);
            }

            public Map<String, Object> getProperties() {
                return null;
            }

            public String getConsumerGroup() {
                return CONSUMER_GROUP;
            }

            public String getConnection() {
                return CONNECTION_STRING;
            }
        };

        EventDeserializer<String> deserializer = new EventDeserializer<String>() {
            @Override
            public String deserialize(byte[] data) {
                return new String(data);
            }
        };

        logger.info("create consumer");
        KafkaEventConsumer<String> consumer = new KafkaEventConsumer<String>(configuration, deserializer);

        consumer.addEventListener(new EventListener<String>() {
            @Override
            public void acceptEvent(Event<String> event) {
                logger.info("Kafka event arrived: " + event.getData() + " HOSTNAME=" + HOSTNAME);
            }
        });

        logger.info("run consumer");

        consumer.run();
    }

    public void start() {
        try {
            Thread thread = new Thread(() -> {
                try {
                    startProducer();
                } catch (IOException e) {
                    logger.warning("Kafka producer failed to start: " + e);
                    e.printStackTrace();
                }
            });
            thread.setDaemon(true);
            thread.start();
            startConsumer();
        } catch (Exception e) {
            logger.warning("Kafka consumer failed to start: " + e);
            e.printStackTrace();
        }
    }
}