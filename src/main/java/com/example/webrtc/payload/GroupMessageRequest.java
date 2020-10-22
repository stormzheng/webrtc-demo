package com.example.webrtc.payload;

import lombok.Data;

/**
 * 群聊消息载荷
 *
 * @author Administrator
 */
@Data
public class GroupMessageRequest {
    /**
     * 消息发送方用户id
     */
    private String fromUid;

    /**
     * 群组id
     */
    private String groupId;

    /**
     * 消息内容
     */
    private String message;
}
