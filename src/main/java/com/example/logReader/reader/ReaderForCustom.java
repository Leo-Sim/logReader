package com.example.logReader.reader;

import com.example.logReader.entity.BufferInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
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
        createLogSink = Sinks.many().unicast().onBackpressureBuffer();

    }

    @Override
    public void readFilesInDirectory(Consumer<List<String>> consumer) {
        Flux<List<String>> flux = createLogSink.asFlux().bufferTimeout(BufferInfo.SEND_BUFFER_SIZE, BufferInfo.SEND_BUFFER_DURATION_SECOND);
        flux.subscribe(consumer);

        super.getFileList().stream()
                .filter(f -> f.endsWith(".json"))
                .forEach(f -> {
                    Path fPath = Paths.get(f);

                    StringBuilder sb = new StringBuilder();
                    Flux.using(() -> Files.lines(fPath), Flux::fromStream, Stream::close).subscribe(s -> sb.append(s));

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
                });
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

        IntStream.range(0, speed).forEach(i -> {
            //create a log which is delimited by delimiter
            String log = createLog(delimiter, map);

            if(i == (speed - 1)) {
                createLogSink.emitComplete(Sinks.EmitFailureHandler.FAIL_FAST);
            } else {
                createLogSink.emitNext(log, Sinks.EmitFailureHandler.FAIL_FAST);
            }

        });
    }

    private String createLog(String delimiter, LinkedHashMap<String, List<String>> map) {
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
