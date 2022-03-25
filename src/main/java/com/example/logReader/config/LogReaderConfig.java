package com.example.logReader.config;

import com.example.logReader.entity.Destination;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "config")
@Getter
@Setter
public class LogReaderConfig {

    private String dirPath;
    private String customLogPath;

    @NestedConfigurationProperty
    private List<Destination> servers;
}
