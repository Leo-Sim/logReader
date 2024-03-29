package com.leo.log.creator.reader;

import com.leo.log.creator.entity.BufferInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class ReaderForText extends AbstractFileReader {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private Predicate<String> fileFilter = f ->f.endsWith(".txt");

    public ReaderForText(String dirPath) {
        super(dirPath);
    }

    @Override
    public Flux<String> getLogs() {

        return Flux.create(fSink -> {
            super.getFileList().stream()
                    .filter(fileFilter)
                    .forEach(f -> {
                        Path fPath = Paths.get(f);
                        Flux.using(() -> Files.lines(fPath), Flux::fromStream, Stream::close)
                                .subscribe(s -> {
                                    fSink.next(s);
                                });
                    });

            fSink.complete();

        });
    }

    @Override
    public void readFilesInDirectory(Consumer<List<String>> consumer) {

        super.getFileList().stream()
                .filter(fileFilter)
                .forEach(f -> {
                    Path fPath = Paths.get(f);

                    Flux<List<String>> sFlux = Flux.using(() -> Files.lines(fPath), Flux::fromStream, Stream::close)
                            .doOnNext(s -> logger.debug("Log in file {} : {}", f, s))
                            .bufferTimeout(BufferInfo.READ_BUFFER_SIZE, BufferInfo.READ_BUFFER_DURATION_SECOND);

                    sFlux.subscribe(consumer);
                });
    }
}
