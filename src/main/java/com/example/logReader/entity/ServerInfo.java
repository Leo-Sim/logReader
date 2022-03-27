package com.example.logReader.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ServerInfo {

    private String ip;
    private int port;
    private SendProtocol protocol;

}
