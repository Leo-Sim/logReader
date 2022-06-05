package com.example.logReader.service;

import com.example.logReader.config.LogReaderConfig;
import com.example.logReader.entity.BufferInfo;
import com.example.logReader.entity.SendProtocol;
import com.example.logReader.entity.ServerInfo;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.netty.Connection;
import reactor.netty.tcp.TcpClient;
import reactor.netty.udp.UdpClient;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class SendService {

    private Logger logger = LoggerFactory.getLogger(getClass());
    private final LogReaderConfig logReaderConfig;

    @Getter
    protected Sinks.Many<String> lineSink;
    protected Flux<List<String>> flux;



    @PostConstruct
    public void init() {
      initFlux();
    }

    private void initFlux() {

        lineSink = Sinks.many().unicast().onBackpressureBuffer();
        flux = lineSink.asFlux().bufferTimeout(BufferInfo.SEND_BUFFER_SIZE, BufferInfo.SEND_BUFFER_DURATION_SECOND);

        flux.subscribe(list -> {
            sendLogToServer(list);
        });
    }

    /**
     * Send list of logs to target servers.
     * @param list
     * */
    private void sendLogToServer(List<String> list) {

        List<ServerInfo> servers = logReaderConfig.getServers();

        // Iterate servers and make connections.
        // One connection is set  when log buffer is flushed.
        servers.stream()
                .filter(server -> StringUtils.hasText(server.getIp()) && server.getPort() > 0 && StringUtils.hasText(server.getProtocol().name()))
                .forEach(server -> {

                    //get connection by protocol
                    Mono<Connection> connection = switch(server.getProtocol()) {
                        case TCP -> getTcpConnection(server);
                        case UDP -> getUdpConnection(server);
                        default -> Mono.empty();
                    };

                    connection.subscribe(conn -> {

                        String[] logArry = list.toArray(new String[list.size()]);
                        Flux.just(logArry)
                                .doOnComplete(() -> {
                                    // dispose connection when it finishes sending log.
                                    conn.dispose();
                                })
                                .subscribe(log -> {
                                    conn.outbound().sendString(Mono.just(log)).then().subscribe();
                                });

                    });
                });
    }

    private Mono<Connection> getTcpConnection(ServerInfo server) {
        Mono<Connection> connection = (Mono<Connection>) TcpClient.create()
                        .host(server.getIp())
                        .port(server.getPort())
                        .doOnConnected(conn -> {
                            logger.info("Connection made to {}:{}, via {}", server.getIp(), server.getPort(), server.getProtocol());
                        })
                        .doOnDisconnected(conn -> {
                            logger.info("Connection is disposed. {}:{}, via {}", server.getIp(), server.getPort(), server.getProtocol());
                        })
                        .connect();
        return connection;

    }

    private Mono<Connection> getUdpConnection(ServerInfo server) {
        Mono<Connection> connection = (Mono<Connection>) UdpClient.create()
                            .host(server.getIp())
                            .port(server.getPort())
                            .doOnConnect(conn -> {
                                logger.info("Connection made to {}:{}, via {}", server.getIp(), server.getPort(), server.getProtocol());
                            })
                            .doOnDisconnected(conn -> {
                                logger.info("Connection is disposed. {}:{}, via {}", server.getIp(), server.getPort(), server.getProtocol());
                            })
                            .connect();
        return connection;
    }

    
}
