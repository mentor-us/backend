package com.hcmus.mentor.backend.config;

import com.corundumstudio.socketio.SocketIOServer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SocketIOConfig {

    @Value("${server-socket.host}")
    private String host;

    @Value("${server-socket.port}")
    private Integer port;

    @Bean
    public SocketIOServer socketIOServer() {
        com.corundumstudio.socketio.Configuration configuration =
                new com.corundumstudio.socketio.Configuration();
        configuration.setHostname(host);
        configuration.setPort(port);
        return new SocketIOServer(configuration);
    }
}
