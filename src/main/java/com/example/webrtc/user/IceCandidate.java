package com.example.webrtc.user;

import lombok.Data;

@Data
public class IceCandidate {

    /**
     * The candidate-attribute as defined in section 15.1 of ICE (rfc5245).
     **/
    private String candidate;
    /**
     * If present, this contains the identifier of the 'media stream identification'.
     **/
    private String sdpMid;
    /**
     * The index (starting at zero) of the m-line in the SDP this candidate is associated with.
     **/
    private int sdpMLineIndex;


}