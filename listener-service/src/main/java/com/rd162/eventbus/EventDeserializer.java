package com.rd162.eventbus;

public interface EventDeserializer<T> {
    T deserialize(byte[] data);
}
