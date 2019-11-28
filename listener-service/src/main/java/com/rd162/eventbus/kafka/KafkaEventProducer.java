package com.rd162.eventbus.kafka;

import com.rd162.eventbus.*;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;

import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public final class KafkaEventProducer<T> implements EventProducer<T> {

    private final ProducerConfiguration configuration;
    private final EventSerializer<T> serializer;
    private final Map<String, Object> properties;
    private Producer<String, Event<T>> producer;
    private final Lock lock = new ReentrantLock();

    public KafkaEventProducer(ProducerConfiguration configuration, EventSerializer<T> serializer) {
        if (configuration == null) {
            throw new IllegalArgumentException("configuration cannot be null");
        }
        if (configuration.getConnection() == null) {
            throw new IllegalArgumentException("connection must be provided in configuration");
        }
        if (configuration.getTopic() == null) {
            throw new IllegalArgumentException("topic must be provided in configuration");
        }
        if (serializer == null) {
            throw new IllegalArgumentException("serializer cannot be null");
        }
        this.configuration = configuration;
        this.serializer = serializer;
        Map<String, Object> properties = this.configuration.getProperties();
        if (properties == null) {
            this.properties = new HashMap<String, Object>();
        } else {
            this.properties = new HashMap<String, Object>(properties);
        }
        this.properties.putIfAbsent("bootstrap.servers", this.configuration.getConnection());
        this.properties.putIfAbsent("request.timeout.ms", 30000);
        this.properties.putIfAbsent("acks", "all"); // default: 1
        this.properties.putIfAbsent("retries", 10); // default: 0
        this.properties.putIfAbsent("retry.backoff.ms", 100); // default: 100
        this.properties.putIfAbsent("max.in.flight.requests.per.connection", 1); // default: 5
        // this.properties.putIfAbsent("enable.idempotence", true); //default: false TODO: use idempotent producer from Kafka 0.11
        this.properties.putIfAbsent("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
    }

    private Producer<String, Event<T>> createKafkaProducer() {
        return new KafkaProducer<String, Event<T>>(properties, new StringSerializer(),
                new KafkaEventSerializer<T>(serializer));
    }

    @Override
    public ProducerConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    public void send(String topic, Event<T> event) {
        try {
            sendAsync(topic, event).get();
        } catch (InterruptedException e) {
            return;
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException)
            {
                throw (RuntimeException) cause;
            }
        }
    }

    @Override
    public Future<Event<T>> sendAsync(String topic, Event<T> event) {
        if (producer == null) {
            lock.lock();
            try {
                if (producer == null) {
                    producer = createKafkaProducer();
                }
            } finally {
                lock.unlock();
            }
        }
        Long timestamp = null;
        if (event.getTimestamp() != null) {
            timestamp = event.getTimestamp().toEpochMilli();
        }
        return new EventFuture(producer.send(
                new ProducerRecord<String, Event<T>>(topic, event.getPartition(), timestamp, event.getKey(), event)),
                event);
    }

    @Override
    public void close() throws IOException {
        producer.close();
    }

    private class EventFuture implements Future<Event<T>> {
        private final Future<RecordMetadata> future;
        private final Event<T> event;

        public EventFuture(Future<RecordMetadata> future, Event<T> event) {
            this.future = future;
            this.event = event;
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return future.cancel(mayInterruptIfRunning);
        }

        @Override
        public boolean isCancelled() {
            return future.isCancelled();
        }

        @Override
        public boolean isDone() {
            return future.isDone();
        }

        @Override
        public Event<T> get() throws InterruptedException, ExecutionException {
            RecordMetadata metadata = future.get();
            event.setTopic(metadata.topic());
            event.setPartition(metadata.partition());
            event.setOffset(metadata.offset());
            event.setTimestamp(Instant.ofEpochMilli(metadata.timestamp()));
            return event;
        }

        @Override
        public Event<T> get(long timeout, TimeUnit unit)
                throws InterruptedException, ExecutionException, TimeoutException {
            RecordMetadata metadata = future.get(timeout, unit);
            event.setTopic(metadata.topic());
            event.setPartition(metadata.partition());
            event.setOffset(metadata.offset());
            event.setTimestamp(Instant.ofEpochMilli(metadata.timestamp()));
            return event;
        }
    }
}
