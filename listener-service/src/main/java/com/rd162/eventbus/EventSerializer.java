package com.rd162.eventbus;

public interface EventSerializer<T> {
    byte[] serialize(T dataObj);
}
