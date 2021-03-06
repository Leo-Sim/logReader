package com.leo.log.creator.entity;

import lombok.Getter;

import java.time.Duration;

public class BufferInfo {

    public static final int READ_BUFFER_SIZE = 2024 * 10;
    public static final Duration READ_BUFFER_DURATION_SECOND = Duration.ofSeconds(1);

    public static final int SEND_BUFFER_SIZE = 2024 * 10;
    public static final Duration SEND_BUFFER_DURATION_SECOND = Duration.ofSeconds(1);
}
