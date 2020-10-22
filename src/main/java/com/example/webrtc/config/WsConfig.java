package com.example.webrtc.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Administrator
 */
@ConfigurationProperties(prefix = "ws.server")
@Data
public class WsConfig {
    /**
     * 端口号
     */
    private Integer port;

    /**
     * host
     */
    private String host;
}

