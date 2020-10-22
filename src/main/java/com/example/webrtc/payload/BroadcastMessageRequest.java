package com.example.webrtc.payload;

import lombok.Data;

/**
 * 广播消息载荷
 *
 * @author Administrator
 */
@Data
public class BroadcastMessageRequest {
    /**
     * 消息内容
     */
    private String message;
}
