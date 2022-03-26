package com.example.logReader.service;

import com.example.logReader.config.LogReaderConfig;
import com.example.logReader.reader.AbstractFileReader;
import com.example.logReader.reader.ReaderForCustom;
import com.example.logReader.reader.ReaderForText;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
public class FileReadService {

    private Logger logger = LoggerFactory.getLogger(getClass());
    private final LogReaderConfig logReaderConfig;
    private final SendService sendService;

    @Scheduled(fixedDelay = 1000)
    public void sendLog() {
       String dirPath = logReaderConfig.getDirPath();
       String customPath = logReaderConfig.getCustomLogPath();

       // if dir path is exist, read all files in it and send it to servers
       if(StringUtils.hasText(dirPath)) {
           AbstractFileReader reader = new ReaderForText(dirPath);
           reader.readFilesInDirectory(getReadConsumer());
       }
        // if custom log path is exist, read all files in it, make logs and send it to servers
       if(StringUtils.hasText(customPath)) {
            AbstractFileReader reader = new ReaderForCustom(customPath);
            reader.readFilesInDirectory(getReadConsumer());
       }

    }

    /**
     * This consumer emits next event on logs in files.
     * */
    private Consumer<List<String>> getReadConsumer() {
        return list -> {
            list.stream().filter(s -> StringUtils.hasText(s)).forEach(s -> {
                Sinks.Many<String> sink = sendService.getLineSink();
                sink.emitNext(s, Sinks.EmitFailureHandler.FAIL_FAST);
            });
        };
    }

}
