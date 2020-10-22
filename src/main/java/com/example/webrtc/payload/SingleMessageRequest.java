package com.example.webrtc.payload;

import lombok.Data;

/**
 * 私聊消息载荷
 *
 * @author Administrator
 */
@Data
public class SingleMessageRequest {
    /**
     * 消息发送方用户id
     */
    private String fromUid;

    /**
     * 消息接收方用户id
     */
    private String toUid;

    /**
     * 消息内容
     */
    private String message;
}
