package com.hcmus.mentor.backend.config;

import com.corundumstudio.socketio.SocketConfig;
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

    @Bean(destroyMethod = "stop")
    public SocketIOServer socketIOServer() {
        var config = new com.corundumstudio.socketio.Configuration();
        config.setHostname(host);
        config.setPort(port);

        SocketConfig socketConfig = new SocketConfig();
        socketConfig.setReuseAddress(true);

        config.setSocketConfig(socketConfig);

        final SocketIOServer server = new SocketIOServer(config);

        Runtime.getRuntime().addShutdownHook(new SocketIOServerShutdownHook(server));

        return server;
    }
}