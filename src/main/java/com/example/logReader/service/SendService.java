package com.example.logReader.service;

import com.example.logReader.entity.BufferInfo;
import lombok.Getter;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.List;

@Service
public class SendService {

    @Getter
    protected Sinks.Many<String> lineSink;
    protected Flux<List<String>> flux;

    @PostConstruct
    public void init() {
        lineSink = Sinks.many().unicast().onBackpressureBuffer();
        flux = lineSink.asFlux().bufferTimeout(BufferInfo.SEND_BUFFER_SIZE, BufferInfo.SEND_BUFFER_DURATION_SECOND);
        flux.subscribe(s -> {

        });
    }
}
