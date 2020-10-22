package com.example.webrtc.config;

import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.SpringAnnotationScanner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Administrator
 */
@Configuration
@EnableConfigurationProperties({WsConfig.class})
public class ServerConfig {

    @Bean
    public SocketIOServer server(WsConfig wsConfig) {
        com.corundumstudio.socketio.Configuration config = new com.corundumstudio.socketio.Configuration();
        config.setHostname(wsConfig.getHost());
        config.setPort(wsConfig.getPort());
        return new SocketIOServer(config);
    }

    /**
     * Spring 扫描自定义注解
     */
    @Bean
    public SpringAnnotationScanner springAnnotationScanner1(SocketIOServer server) {
        return new SpringAnnotationScanner(server);
    }

}
