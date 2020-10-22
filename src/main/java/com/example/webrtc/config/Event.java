package com.example.webrtc.config;


/**
 * @author Administrator
 */
public interface Event {
    /**
     * 聊天事件
     */
    String CHAT = "chat" ;

    /**
     * 广播消息
     */
    String BROADCAST = "broadcast" ;

    /**
     * 群聊
     */
    String GROUP = "group" ;

    /**
     * 加入群聊
     */
    String JOIN = "join" ;

    /**
     * 创建群聊
     */
    String CREATE = "create" ;

    /**
     * 加入群聊响应事件
     */
    String JOINED = "joined" ;

    /**
     * 注册事件
     */
    String REGISTER = "register" ;

    /**
     * 注册响应事件
     */
    String REGISTER_RESPONSE = "registerResponse" ;

    /**
     * 呼叫事件
     */
    String CALL = "call" ;

    /**
     * 呼叫响应事件
     */
    String CALL_RESPONSE = "callResponse" ;

    /**
     * 来电事件
     */
    String INCOMING_CALL = "incomingCall" ;

    /**
     * 来电响应事件
     */
    String INCOMING_CALL_RESPONSE = "incomingCallResponse" ;

    /**
     * ice候选人收集事件
     */
    String ICE_CANDIDATE = "iceCandidate" ;

    /**
     * ice候选人加入事件
     */
    String ON_ICE_CANDIDATE = "onIceCandidate" ;

    /**
     * 关闭事件
     */
    String STOP = "stop" ;

    /**
     * 开始通话事件
     */
    String START_COMMUNICATION = "startCommunication" ;

    /**
     * 关闭通话事件
     */
    String STOP_COMMUNICATION = "stopCommunication" ;

}
