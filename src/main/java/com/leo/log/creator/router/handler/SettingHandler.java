package com.leo.log.creator.router.handler;

import com.leo.log.creator.config.LogReaderConfig;
import com.leo.log.creator.reader.AbstractFileReader;
import com.leo.log.creator.reader.ReaderForText;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Getter
public class SettingHandler {

    private final LogReaderConfig logReaderConfig;

    public Mono<ServerResponse> getTextLogs(ServerRequest request) {

        String dirPath = logReaderConfig.getDirPath();
        AbstractFileReader reader = new ReaderForText(dirPath);

        Flux<String> flux = reader.getLogs();
        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                .body(flux, Map.class);
    }
}
