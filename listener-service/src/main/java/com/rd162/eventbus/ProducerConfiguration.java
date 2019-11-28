package com.rd162.eventbus;

import java.util.Map;

public interface ProducerConfiguration {
    String getConnection();

    Map<String, Object> getProperties();

    String getTopic();
}
