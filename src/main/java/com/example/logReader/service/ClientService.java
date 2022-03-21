package com.example.logReader.service;

import com.example.logReader.config.LogReaderConfig;
import com.example.logReader.entity.Destination;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final LogReaderConfig logReaderConfig;



    @PostConstruct
    public void init() {
        List<Destination> dests = logReaderConfig.getServers();
    }

//    private void createClientByProtocol(List<D>)



}
