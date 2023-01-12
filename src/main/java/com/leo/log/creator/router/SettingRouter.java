package com.leo.log.creator.router;

import com.leo.log.creator.router.handler.SettingHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class SettingRouter {

    @Bean
    public RouterFunction<ServerResponse> monitorRouterInfo(SettingHandler settingHandler) {
        return RouterFunctions.route()
                .GET("/log/setting", request -> settingHandler.getTextLogs(request))
//                .GET("/monitor/realtime/logs", request -> monitorHandler.getRealtimeLogs(request))
                .build();
    }
}