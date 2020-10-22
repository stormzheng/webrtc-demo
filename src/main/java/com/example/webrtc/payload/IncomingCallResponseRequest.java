package com.example.webrtc.payload;

import lombok.Data;

/**
 * @author Administrator
 */
@Data
public class IncomingCallResponseRequest {

    private String from;
    private String sdpAnswer;
    private String callResponse;
}
