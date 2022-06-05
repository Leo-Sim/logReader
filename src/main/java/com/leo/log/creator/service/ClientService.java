package com.leo.log.creator.service;

import com.leo.log.creator.config.LogReaderConfig;
import com.leo.log.creator.entity.ServerInfo;
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
        List<ServerInfo> dests = logReaderConfig.getServers();
    }

//    private void createClientByProtocol(List<D>)



}
