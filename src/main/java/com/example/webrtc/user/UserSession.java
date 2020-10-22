/*
 * (C) Copyright 2014 Kurento (http://kurento.org/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.example.webrtc.user;

import com.corundumstudio.socketio.SocketIOClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * User session.
 *
 * @author Boni Garcia (bgarcia@gsyc.es)
 * @author Micael Gallego (micael.gallego@gmail.com)
 * @since 4.3.1
 */
public class UserSession {

  private static final Logger log = LoggerFactory.getLogger(UserSession.class);

  private final String name;
  private final SocketIOClient session;
  private String sdpOffer;
  private String callingTo;
  private String callingFrom;
  private final List<IceCandidate> candidateList = new ArrayList<IceCandidate>();

  public UserSession(String name, SocketIOClient session) {
    this.name = name;
    this.session = session;
  }

  public String getName() {
    return name;
  }

  public SocketIOClient getSession() {
    return session;
  }

  public String getSessionId() {
    return session.getSessionId().toString();
  }

  public String getSdpOffer() {
    return sdpOffer;
  }

  public void setSdpOffer(String sdpOffer) {
    this.sdpOffer = sdpOffer;
  }

  public String getCallingTo() {
    return callingTo;
  }

  public void setCallingTo(String callingTo) {
    this.callingTo = callingTo;
  }

  public String getCallingFrom() {
    return callingFrom;
  }

  public void setCallingFrom(String callingFrom) {
    this.callingFrom = callingFrom;
  }

  public List<IceCandidate> getCandidateList() {
    return candidateList;
  }

  public void addCandidate(IceCandidate candidate) {
      candidateList.add(candidate);
  }

  public void clear() {
    this.candidateList.clear();
  }
}
