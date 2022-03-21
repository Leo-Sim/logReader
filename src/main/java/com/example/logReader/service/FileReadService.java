package com.example.logReader.service;

import com.example.logReader.config.LogReaderConfig;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FileReadService {

    private Logger logger = LoggerFactory.getLogger(getClass());
    private final LogReaderConfig logReaderConfig;

    @Scheduled(fixedDelay = 1000)
    public void sendLog() {
        Path path = Paths.get(logReaderConfig.getDirPath());

        try {
            Files.walk(path).filter(Files::isRegularFile).forEach(f-> {
                Path file = f.toAbsolutePath();
                String name = file.getFileName().toString();
                if(name.endsWith(".txt")) {
                    try {
                        List<String> lines = Files.readAllLines(file);


                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("an error occured while reading files in {}", logReaderConfig);
        }
    }


}
