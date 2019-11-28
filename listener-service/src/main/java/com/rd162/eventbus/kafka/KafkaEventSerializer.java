package com.rd162.eventbus.kafka;

import com.rd162.eventbus.Event;
import com.rd162.eventbus.EventSerializer;

import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

final class KafkaEventSerializer<T> implements Serializer<Event<T>> {
    private EventSerializer<T> serializer;

    public KafkaEventSerializer(EventSerializer<T> serializer) {
        this.serializer = serializer;
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {

    }

    @Override
    public byte[] serialize(String topic, Event<T> event) {
        event.setSerializer(serializer);
        return event.serializeData();
    }

    @Override
    public void close() {

    }
}
