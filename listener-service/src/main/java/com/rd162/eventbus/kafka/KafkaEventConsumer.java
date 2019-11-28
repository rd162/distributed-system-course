package com.rd162.eventbus.kafka;

import com.rd162.eventbus.*;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public final class KafkaEventConsumer<T> extends AbstractRunnableEventConsumer implements EventConsumer<T> {
    private final ConsumerConfiguration configuration;
    private final EventDeserializer<T> deserializer;
    private final Map<String, Object> properties;
    private final Collection<EventListener<T>> listeners = new ArrayList<EventListener<T>>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private int partitions = 0;

    public KafkaEventConsumer(ConsumerConfiguration configuration, EventDeserializer<T> deserializer) {
        super(configuration);
        if (configuration == null) {
            throw new IllegalArgumentException("configuration cannot be null");
        }
        if (configuration.getConnection() == null) {
            throw new IllegalArgumentException("connection must be provided in configuration");
        }
        if (configuration.getConsumerGroup() == null) {
            throw new IllegalArgumentException("consumer group must be provided in configuration");
        }
        if (configuration.getTopics() == null || configuration.getTopics().isEmpty()) {
            throw new IllegalArgumentException("topics must be provided in configuration");
        }
        if (deserializer == null) {
            throw new IllegalArgumentException("deserializer cannot be null");
        }
        this.configuration = configuration;
        this.deserializer = deserializer;
        Map<String, Object> properties = this.configuration.getProperties();
        if (properties == null) {
            this.properties = new HashMap<String, Object>();
        } else {
            this.properties = new HashMap<String, Object>(properties);
        }
        this.properties.putIfAbsent("bootstrap.servers", this.configuration.getConnection());
        this.properties.putIfAbsent("group.id", this.configuration.getConsumerGroup());
        this.properties.putIfAbsent("enable.auto.commit", false);
        this.properties.putIfAbsent("session.timeout.ms", 30000); //default: 3000
        this.properties.putIfAbsent("request.timeout.ms", 605000); //default: 305000
        this.properties.putIfAbsent("max.poll.interval.ms", 600000); //default: 300000
        this.properties.putIfAbsent("max.poll.records", 10); //default: 500
        this.properties.putIfAbsent("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
    }

    @Override
    public ConsumerConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    public int getPartitionsCount() {
        lock.readLock().lock();
        try {
            return partitions;
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void addEventListener(EventListener<T> listener) {
        if (listener == null) {
            throw new IllegalArgumentException("listener cannot be null");
        }
        lock.writeLock().lock();
        try {
            listeners.add(listener);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void removeEventListener(EventListener<T> listener) {
        if (listener == null) {
            throw new IllegalArgumentException("listener cannot be null");
        }
        lock.writeLock().lock();
        try {
            if (!listeners.remove(listener)) {
                throw new IllegalArgumentException("listener does not exist");
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void setEventListeners(Collection<EventListener<T>> listeners) {
        if (listeners == null) {
            throw new IllegalArgumentException("listeners cannot be null");
        }
        lock.writeLock().lock();
        try {
            this.listeners.clear();
            this.listeners.addAll(listeners);
        } finally {
            lock.writeLock().unlock();
        }
    }

    private void setPartitionsCount(int partitions) {
        lock.writeLock().lock();
        try {
            this.partitions = partitions;
        } finally {
            lock.writeLock().unlock();
        }
    }

    private boolean noListeners() {
        lock.readLock().lock();
        try {
            return this.listeners.isEmpty();
        } finally {
            lock.readLock().unlock();
        }
    }

    private void commitOffset(Consumer<String, Event<T>> consumer, ConsumerRecord<String, Event<T>> record) {
        HashMap<TopicPartition, OffsetAndMetadata> offset = new HashMap<TopicPartition, OffsetAndMetadata>();
        offset.put(new TopicPartition(record.topic(), record.partition()), new OffsetAndMetadata(record.offset() + 1));
        consumer.commitSync(offset);
    }

    protected Consumer<String, Event<T>> createKafkaConsumer() {
        return new KafkaConsumer<String, Event<T>>(properties, new StringDeserializer(), new KafkaEventDeserializer<T>(deserializer));
    }

    @Override
    public void run() {
        if (isRunning()) {
            throw new IllegalStateException("Cannot run the consumer when it is already running");
        }
        setState(State.Running);
        try {
            while (isRunning()) {
                while (noListeners()) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        return;
                    }
                }
                try (Consumer<String, Event<T>> consumer = createKafkaConsumer()) {
                    consumer.subscribe(configuration.getTopics());
                    while (isRunning()) {
                        try {
                            if (noListeners()) {
                                break;
                            }
                            if (Thread.currentThread().isInterrupted()) {
                                return;
                            }
                            ConsumerRecords<String, Event<T>> records = consumer.poll(100);
                            if (getPartitionsCount() == 0) {
                                for (TopicPartition tp : consumer.assignment()) {
                                    if (consumer.committed(tp) == null) {
                                        consumer.commitSync();
                                        break;
                                    }
                                }
                            }
                            setPartitionsCount(consumer.assignment().size());
                            for (ConsumerRecord<String, Event<T>> record : records) {
                                Event<T> event = record.value();
                                event.setKey(record.key());
                                event.setTopic(record.topic());
                                event.setPartition(record.partition());
                                event.setOffset(record.offset());
                                event.setTimestamp(Instant.ofEpochMilli(record.timestamp()));
                                fireEvent(event);
                                commitOffset(consumer, record);
                            }
                        } catch (RuntimeException e) {
                        }
                    }
                }
            }
        } finally {
            setPartitionsCount(0);
            setState(State.Completed);
        }
    }

    private void fireEvent(Event<T> event) {
        Object[] listeners;
        lock.readLock().lock();
        try {
            listeners = this.listeners.toArray();
        } finally {
            lock.readLock().unlock();
        }
        for (Object listener : listeners) {
            ((EventListener<T>) listener).acceptEvent(event);
        }
    }
}
