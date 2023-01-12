package com.leo.log.creator.reader;

import com.leo.log.creator.entity.BufferInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Create random log.
 * Read json files that have multiple values for each log.
 *
 *    ex)
 *      {
 *          "speed": "250",
 *          "delimiter": "`",
 *          "columns": {
 *           // All field and possible values are in "column". field name is 'key'
 *              "date": ["2020-11-10 12:00:00.123", "2020-12-11 11:00:00.234" ...],
 *              "eventId" ["12345", "67897" ...]
 *              .....
 *          },
 *          ....
 *      }
 *
 *
 *
 * */
public class ReaderForCustom extends AbstractFileReader{

    private Logger logger = LoggerFactory.getLogger(getClass());

    private Sinks.Many<String> createLogSink;

    private final String KEY_DELIMITER = "delimiter";
    private final String KEY_COLUMS = "columns";
    private final String KEY_SPEED = "speed";

    private final String DEFAULT_COUNT_SPEED = "200";

    public ReaderForCustom(String dirPath) {
        super(dirPath);


    }

    @Override
    public void readFilesInDirectory(Consumer<List<String>> consumer) {

        super.getFileList().stream()
                .filter(f -> f.endsWith(".json"))
                .forEach(f -> {
                    // Declare sinks for each file. After finishing sending logs, it will send complete signal.
                    createLogSink = Sinks.many().unicast().onBackpressureBuffer();
                    Flux<List<String>> flux = createLogSink.asFlux().bufferTimeout(BufferInfo.SEND_BUFFER_SIZE, BufferInfo.SEND_BUFFER_DURATION_SECOND);
                    flux.doOnNext(s -> logger.debug("create random log : {}", s)).subscribe(consumer);

                    Path fPath = Paths.get(f);

                    StringBuilder sb = new StringBuilder();
                    Flux.using(() -> Files.lines(fPath), Flux::fromStream, Stream::close)
                            .doOnNext(s -> sb.append(s))
                            .doOnComplete(() -> {
                                logger.info("Read custom log info from {}", f);

                                // Convert string into map.
                                ObjectMapper mapper = new ObjectMapper();
                                Map<String, Object> map = null;
                                try {
                                    map = (LinkedHashMap) mapper.readValue(sb.toString(), Map.class);

                                    int speed = Integer.parseInt((String) map.getOrDefault(KEY_SPEED, DEFAULT_COUNT_SPEED));
                                    String delimiter = (String) map.get(KEY_DELIMITER);
                                    LinkedHashMap<String, List<String>> colMap = (LinkedHashMap) map.get(KEY_COLUMS);

                                    createAndPushRandomLogs(delimiter, colMap, speed);

                                } catch (JsonProcessingException e) {
                                    e.printStackTrace();
                                }
                            }).subscribe();
//
                });
    }

    @Override
    public Flux<String> getLogs() {
        return null;
    }

    /**
     * This method creates random log from given log information and push it into flux.
     * 'map' has key(field name) and list of its value.
     * 'speed' is an int value for number of created log per tick
     * @param delimiter
     * @param map
     * @param speed
     *
     * */
    private void createAndPushRandomLogs(String delimiter, LinkedHashMap<String, List<String>> map, int speed) {

        StringBuilder sb = new StringBuilder();

        int i =0;
        while(i < speed) {

            String log = createSingleLog(delimiter, map);
            sb.append(log).append("\n");

            i++;
        }
        createLogSink.emitNext(sb.toString(), Sinks.EmitFailureHandler.FAIL_FAST);
        createLogSink.emitComplete(Sinks.EmitFailureHandler.FAIL_FAST);

    }

    /**
     * create random log
     * @param delimiter
     * @param map
     * */
    private String createSingleLog(String delimiter, LinkedHashMap<String, List<String>> map) {
        return map.entrySet().stream()
                .map(entry -> {
                    String randomVal = createRandomValue(entry.getValue());
                    return randomVal;
                }).collect(Collectors.joining(delimiter));
    }

    /**
     * return random value in list
     * @param columns
     * */
    private String createRandomValue(List<String> columns) {
        return columns.get(new Random().nextInt(columns.size()));
    }
}
