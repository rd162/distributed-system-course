package com.rd162.eventbus;

public interface EventListener<T> extends java.util.EventListener {
    void acceptEvent(Event<T> event);
}

