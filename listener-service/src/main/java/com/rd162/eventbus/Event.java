package com.rd162.eventbus;

import java.time.Instant;

public final class Event<T> {
    private String key;
    private byte[] data;
    private T dataObj;
    private EventSerializer<T> serializer;
    private EventDeserializer<T> deserializer;

    private String topic;
    private Integer partition;
    private long offset;

    private Instant timestamp;

    public Event(String key, byte[] data) {
        this.key = key;
        this.data = data;
        this.dataObj = null;
    }

    public Event(String key, T data) {
        this.key = key;
        this.data = null;
        this.dataObj = data;
    }

    public Event(byte[] data) {
        this(null, data);
    }

    public Event(T data) {
        this(null, data);
    }

    public String getKey()
    {
        return key;
    }

    public String getTopic() {
        return topic;
    }

    public Integer getPartition() {
        return partition;
    }

    public long getOffset() {
        return offset;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setKey(String key) {
        this.key = key;
    }
    
    public void setTopic(String topic) {
        this.topic = topic;
    }

    public void setPartition(int partition) {
        this.partition = partition;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public T getData() {
        return deserializeData();
    }

    public void setSerializer(EventSerializer<T> serializer)
    {
        this.serializer = serializer;
    }

    public void setDeserializer(EventDeserializer<T> deserializer)
    {
        this.deserializer = deserializer;
    }

    public byte[] serializeData() {
        if (data != null) {
            return data;
        }
        if (dataObj != null) {
            if (serializer == null) {
                throw new IllegalStateException("serializer cannot be null");
            }
            data = serializer.serialize(dataObj);
        } else {
            data = new byte[0];
        }
        return data;
    }

    private T deserializeData() {
        if (dataObj != null) {
            return (T) dataObj;
        }
        if (data == null) {
            return null;
        }
        if (deserializer == null) {
            throw new IllegalStateException("deserializer cannot be null");
        }
        dataObj = deserializer.deserialize(data);
        return dataObj;
    }
}