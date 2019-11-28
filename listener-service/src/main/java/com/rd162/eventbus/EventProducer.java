package com.rd162.eventbus;

import java.io.Closeable;
import java.util.concurrent.Future;

public interface EventProducer<T> extends Closeable {
    ProducerConfiguration getConfiguration();

    void send(Event<T> event);

    Future<Event<T>> sendAsync(Event<T> event);
}
