package com.rd162.listener;

import com.rd162.eventbus.*;
import com.rd162.eventbus.kafka.*;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Logger;

public class KafkaListenerServer {
    private static final Logger logger = Logger.getLogger(KafkaListenerServer.class.getName());

    final String TOPIC = "hello_topic";
    final String CONSUMER_GROUP = "listener-service";
    final String CONNECTION_STRING = "";

    public KafkaListenerServer() {
    }

    public void start() {
        try {
            KafkaEventConsumer<String> consumer = new KafkaEventConsumer<String>(new ConsumerConfiguration() {

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
            }, new EventDeserializer<String>() {

                public String deserialize(byte[] data) {
                    return new String(data);
                }
            });
            consumer.addEventListener(new EventListener<String>() {

                public void acceptEvent(Event<String> event) {
                    logger.info("Kafka event arrived: " + event.getData());
                }
            });
            try (KafkaEventProducer<String> producer = new KafkaEventProducer<String>(new ProducerConfiguration() {

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

            }, new EventSerializer<String>() {

                @Override
                public byte[] serialize(String dataObj) {
                    return dataObj.getBytes();
                }
            })) {
                producer.send("hello_topic", new Event<String>("Hello, Kafka consumer!"));
                producer.send("hello_topic", new Event<String>("Hello, Kafka consumer $$$!"));
                producer.send("hello_topic", new Event<String>("Hello, Kafka consumer %%%!"));
            }
            consumer.run();
        } catch (Exception e) {
            logger.warning("Kafka listener failed to start: " + e);
        }
    }
}