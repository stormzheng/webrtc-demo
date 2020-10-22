package com.example.webrtc.payload;

import lombok.Data;

/**
 * 加群载荷
 *
 * @author Administrator
 */
@Data
public class CallRequest {

    private String to;
    private String from;
    private String sdpOffer;

}
