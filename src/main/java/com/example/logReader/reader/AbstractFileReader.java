package com.example.logReader.reader;

import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Read files in directory path and set list of file names in it.
 * */
abstract public class AbstractFileReader {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Getter
    private String dirPath;
    @Getter
    private List<String> fileList;

    public AbstractFileReader(String dirPath) {
        this.dirPath = dirPath;
        fileList = getFilesInDirectory();
    }


    private List<String> getFilesInDirectory() {

        List list = new LinkedList();
        if(StringUtils.hasText(dirPath)) {
            Path path = Paths.get(dirPath);

            try {
                Files.walk(path)
                        .filter(Files::isRegularFile)
                        .forEach(f -> {
                            list.add(getFileName(f));
                        });

                return list;

            } catch (IOException e) {
                e.printStackTrace();
                logger.error("an error occured while reading files in {}", path);
            }
        }

        return new ArrayList<>();
    }

    /**
     * return file name
     *
     * @param path
     * */
    private String getFileName(Path path) {
        Path file = path.toAbsolutePath();
        return file.toString();
    }

    abstract public void readFilesInDirectory(Consumer<List<String>> consumer);
}
