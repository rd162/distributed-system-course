package com.rd162.eventbus;

import java.io.Closeable;
import java.util.concurrent.Future;

public interface EventProducer<T> extends Closeable {
    ProducerConfiguration getConfiguration();

    void send(String topic, Event<T> event);

    Future sendAsync(String topic, Event<T> event);
}
