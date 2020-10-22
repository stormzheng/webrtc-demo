package com.example.webrtc.payload;

import lombok.Data;

/**
 * @author Administrator
 */
@Data
public class OnIceCandidateRequest {

    private String sdpMid;
    private int sdpMLineIndex;
    private String candidate;
}
