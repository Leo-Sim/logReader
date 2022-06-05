package com.leo.log.creator.service;

import com.leo.log.creator.config.LogReaderConfig;
import com.leo.log.creator.reader.AbstractFileReader;
import com.leo.log.creator.reader.ReaderForCustom;
import com.leo.log.creator.reader.ReaderForText;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Sinks;

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
