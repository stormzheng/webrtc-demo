package com.example.webrtc.handler;

import cn.hutool.core.lang.Dict;
import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.BroadcastOperations;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.OnConnect;
import com.corundumstudio.socketio.annotation.OnDisconnect;
import com.corundumstudio.socketio.annotation.OnEvent;
import com.example.webrtc.config.Event;
import com.example.webrtc.payload.CallRequest;
import com.example.webrtc.payload.IncomingCallResponseRequest;
import com.example.webrtc.payload.OnIceCandidateRequest;
import com.example.webrtc.payload.RegisterRequest;
import com.example.webrtc.user.IceCandidate;
import com.example.webrtc.user.UserRegistry;
import com.example.webrtc.user.UserSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.UUID;

/**
 * @author Administrator
 */
@Component
@Slf4j
public class MessageEventHandler2 {
    @Autowired
    private SocketIOServer server;

    @Autowired
    private UserRegistry registry;

    /**
     * connect事件，当客户端发起连接时调用
     *
     * @param client 客户端对象
     */
    @OnConnect
    public void onConnect(SocketIOClient client) {
        if (client != null) {
            UUID sessionId = client.getSessionId();
            log.info("连接成功,【sessionId】= {}", sessionId);
        } else {
            log.error("客户端为空");
        }
    }

    /**
     * disconnect事件，客户端断开连接时调用，刷新客户端信息
     *
     * @param client 客户端对象
     */
    @OnDisconnect
    public void onDisconnect(SocketIOClient client) {
        if (client != null) {
            UUID sessionId = client.getSessionId();
            log.info("客户端断开连接,【sessionId】= {}", sessionId);
            client.disconnect();
        } else {
            log.error("客户端为空");
        }
    }

    /**
     * 注册事件
     *
     * @param client  客户端
     * @param request 请求
     * @param data
     */
    @OnEvent(value = Event.REGISTER)
    public void onRegisterEvent(SocketIOClient client, AckRequest request, RegisterRequest data) {
        String name = data.getName();

        UserSession caller = registry.getBySession(client);
        if (caller != null) {
            if (caller.getName().equals(name)) {
                registry.removeBySession(client);
            } else {
                request.sendAckData(Dict.create().set("response", "rejected: user '" + caller.getSessionId() + "' already registered"));
                return;
            }
        }

        String responseMsg = "accepted";
        if (name.isEmpty()) {
            responseMsg = "rejected: empty user name";
        } else if (registry.exists(name)) {
            responseMsg = "rejected: user '" + name + "' already registered";
        } else {
            caller = new UserSession(name, client);
            registry.register(caller);
        }
        request.sendAckData(Dict.create().set("response", responseMsg));

        BroadcastOperations one2oneRoom = server.getRoomOperations("one2one");
        if (one2oneRoom.getClients() == null || one2oneRoom.getClients().isEmpty()) {
            client.sendEvent(Event.CREATE);
            client.joinRoom("one2one");
        } else if (one2oneRoom.getClients().size() == 1){
            one2oneRoom.sendEvent(Event.JOIN);
            client.joinRoom("one2one");
            client.sendEvent(Event.JOINED);
        } else {
            request.sendAckData(Dict.create().set("response", "Room one2one is full"));
        }
    }


    @OnEvent(value = Event.CALL)
    public void onCallEvent(SocketIOClient client, AckRequest request, CallRequest data) {
        UserSession caller = registry.getBySession(client);
        if (caller == null) {
            request.sendAckData(Dict.create().set("response", "Please register first"));
            return;
        }

        String to = data.getTo();
        String from = data.getFrom();
        String sdpOffer = data.getSdpOffer();
        if (registry.exists(to)) {
            caller.setSdpOffer(sdpOffer);
            caller.setCallingTo(to);

            UserSession callee = registry.getByName(to);
            callee.setCallingFrom(from);
            callee.getSession().sendEvent(Event.INCOMING_CALL, Dict.create().set("from", from).set("sdpOffer", sdpOffer));
        } else {
            request.sendAckData(Dict.create().set("response", "rejected: user '" + to + "' is not registered"));
        }
    }

    @OnEvent(value = Event.INCOMING_CALL_RESPONSE)
    public void onIncomingCallResponseEvent(SocketIOClient client, AckRequest request, IncomingCallResponseRequest data) {
        UserSession callee = registry.getBySession(client);
        if (callee == null) {
            request.sendAckData(Dict.create().set("response", "Please register first"));
            return;
        }

        String from = data.getFrom();
        String sdpAnswer = data.getSdpAnswer();
        String callResponse = data.getCallResponse();
        UserSession caller = registry.getByName(from);

        try {
            if ("accept".equals(callResponse)) {
                callee.getSession().sendEvent(Event.START_COMMUNICATION);
                caller.getSession().sendEvent(Event.CALL_RESPONSE, Dict.create()
                        .set("response", "accepted")
                        .set("sdpAnswer", sdpAnswer));

            } else {
                caller.getSession().sendEvent(Event.CALL_RESPONSE, Dict.create().set("response", "rejected"));
            }
        } catch (Throwable t) {
            log.error(t.getMessage(), t);
            caller.getSession().sendEvent(Event.CALL_RESPONSE, Dict.create().set("response", "rejected"));
            callee.getSession().sendEvent(Event.STOP_COMMUNICATION);
        }
    }

    @OnEvent(value = Event.ON_ICE_CANDIDATE)
    public void onIceCandidateEvent(SocketIOClient client, AckRequest request, OnIceCandidateRequest data) {
        UserSession caller = registry.getBySession(client);
        if (caller == null) {
            request.sendAckData(Dict.create().set("response", "Please register first"));
            return;
        }
        log.info("{}", data);
        IceCandidate iceCandidate = new IceCandidate();
        iceCandidate.setCandidate(data.getCandidate());
        iceCandidate.setSdpMid(data.getSdpMid());
        iceCandidate.setSdpMLineIndex(data.getSdpMLineIndex());
        caller.addCandidate(iceCandidate);

        BroadcastOperations one2oneRoom = server.getRoomOperations("one2one");
        Collection<SocketIOClient> clients = one2oneRoom.getClients();
        if (clients.contains(client)) {
            clients.forEach(c -> {
                if (c.getSessionId().equals(client.getSessionId())) {
                    return;
                }
                c.sendEvent(Event.ICE_CANDIDATE, Dict.create().set("candidate", iceCandidate));
            });
        }
    }

    @OnEvent(value = Event.STOP)
    public void onStopEvent(SocketIOClient client, AckRequest request) {
        UserSession caller = registry.getBySession(client);
        if (caller == null) {
            request.sendAckData(Dict.create().set("response", "Please register first"));
            return;
        }
        // Both users can stop the communication. A 'stopCommunication'
        // message will be sent to the other peer.
        UserSession stopperUser = registry.getBySession(client);
        if (stopperUser != null) {
            UserSession stoppedUser =
                    (stopperUser.getCallingFrom() != null) ? registry.getByName(stopperUser
                            .getCallingFrom()) : stopperUser.getCallingTo() != null ? registry
                            .getByName(stopperUser.getCallingTo()) : null;

            if (stoppedUser != null) {
                stoppedUser.getSession().sendEvent(Event.STOP_COMMUNICATION);
                stoppedUser.clear();
            }
            stopperUser.clear();
        }
    }
}
