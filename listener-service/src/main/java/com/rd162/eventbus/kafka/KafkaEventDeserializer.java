package com.rd162.eventbus.kafka;

import com.rd162.eventbus.Event;
import com.rd162.eventbus.EventDeserializer;

import org.apache.kafka.common.serialization.Deserializer;

import java.util.Map;

final class KafkaEventDeserializer<T> implements Deserializer<Event<T>> {
    private EventDeserializer<T> deserializer;

    KafkaEventDeserializer(EventDeserializer<T> deserializer) {
        this.deserializer = deserializer;
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
    }

    @Override
    public Event<T> deserialize(String topic, byte[] data) {
        Event<T> event = new Event<T>(data);
        event.setDeserializer(deserializer);
        return event;
    }

    @Override
    public void close() {

    }
}
