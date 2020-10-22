'use strict';

var pc;
var from;
/////////////////////////////////////////////

var isInitiator = false;
var isStarted = false;
var isChannelReady = false;

var socket = io(`http://localhost:8083`);
var localStream;
var localVideo;
var remoteVideo;

window.onload = function () {
    console = new Console();
    setRegisterState(NOT_REGISTERED);
    localVideo = document.getElementById('localVideo');
    remoteVideo = document.getElementById('remoteVideo');
    document.getElementById('name').focus();
}

window.onbeforeunload = function () {
    socket.disconnect();
}


var registerState = null;
const NOT_REGISTERED = 0;
const REGISTERING = 1;
const REGISTERED = 2;

function setRegisterState(nextState) {
    switch (nextState) {
        case NOT_REGISTERED:
            enableButton('#register', 'register()');
            setCallState(NO_CALL);
            break;
        case REGISTERING:
            disableButton('#register');
            break;
        case REGISTERED:
            disableButton('#register');
            setCallState(NO_CALL);
            break;
        default:
            return;
    }
    registerState = nextState;
}

var callState = null;
const NO_CALL = 0;
const PROCESSING_CALL = 1;
const IN_CALL = 2;

function setCallState(nextState) {
    switch (nextState) {
        case NO_CALL:
            enableButton('#call', 'call()');
            disableButton('#terminate');
            disableButton('#play');
            break;
        case PROCESSING_CALL:
            disableButton('#call');
            disableButton('#terminate');
            disableButton('#play');
            break;
        case IN_CALL:
            disableButton('#call');
            enableButton('#terminate', 'stop()');
            disableButton('#play');
            break;
        default:
            return;
    }
    callState = nextState;
}

function disableButton(id) {
    $(id).attr('disabled', true);
    $(id).removeAttr('onclick');
}

function enableButton(id, functionName) {
    $(id).attr('disabled', false);
    $(id).attr('onclick', functionName);
}

function register() {
    var name = document.getElementById('name').value;
    if (name === null || name === undefined || name === '') {
        window.alert('You must insert your user name');
        return;
    }
    setRegisterState(REGISTERING);
    var response = {
        name: name
    };
    console.log('sending register message:', JSON.stringify(response));
    socket.emit('register', response, function (message) {
        console.info('Received message: ' + JSON.stringify(message));
        if (message.response === 'accepted') {
            setRegisterState(REGISTERED);
        } else {
            setRegisterState(NOT_REGISTERED);
            var errorMessage = message.response ? message.response
                : 'Unknown reason for register rejection.';
            console.log(errorMessage);
            alert('Error registering user. See console for further information.');
        }
    });
    document.getElementById('peer').focus();
}

socket.on('create', function (message) {
    console.log('create room one2one');
    isInitiator = true;
});

socket.on('join', function (message) {
    console.log('Another peer join room one2one');
    isChannelReady = true;
});

socket.on('joined', function (message) {
    console.log('joined room one2one');
    isInitiator = true;
    isChannelReady = true;
});

function call() {
    let peerValue = document.getElementById('peer').value;
    if (peerValue === null || peerValue === undefined || peerValue === '') {
        window.alert('You must specify the peer name');
        return;
    }
    setCallState(PROCESSING_CALL);

    if (!isStarted && typeof localStream !== 'undefined' && isChannelReady) {
        console.log('>>>>>> creating peer connection');
        createPeerConnection();
        pc.addStream(localStream);
        isStarted = true;
        console.log('isInitiator', isInitiator);
        if (isInitiator) {
            pc.createOffer(createOfferAndSendMessage, handleCreateOfferError);
        }
    }
}

socket.on('incomingCall', function (message) {
    // If bussy just reject without disturbing user
    if (callState !== NO_CALL) {
        var response = {
            from: message.from,
            callResponse: 'reject',
            message: 'bussy'
        };
        socket.emit('incomingCallResponse', response);
        console.log('sending incomingCallResponse message:', JSON.stringify(response));
    }
    setCallState(PROCESSING_CALL);
    from = message.from;

    if (!isStarted && typeof localStream !== 'undefined' && isChannelReady) {
        console.log('>>>>>> creating peer connection');
        createPeerConnection();
        pc.addStream(localStream);
        isStarted = true;
    }

    var offer = new RTCSessionDescription({
        type: 'offer',
        sdp: message.sdpOffer
    });
    pc.setRemoteDescription(offer);
    pc.createAnswer().then(createAnswerAndSendMessage, handleCreateAnswerError);
});

socket.on('callResponse', function (message) {
    console.info('Received message: ' + JSON.stringify(message));
    if (message.response !== 'accepted') {
        console.info('Call not accepted by peer. Closing call');
        var errorMessage = message.message ? message.message
            : 'Unknown reason for  call rejection.';
        console.log(errorMessage);
        stop();
    } else {
        setCallState(IN_CALL);
        if (isStarted) {
            var answer = new RTCSessionDescription({
                type: 'answer',
                sdp: message.sdpAnswer
            });
            pc.setRemoteDescription(answer);
        }
    }
});

socket.on('startCommunication', function (message) {
    console.info('Communication start');
    setCallState(IN_CALL);
});

socket.on('stopCommunication', function (message) {
    console.info('Communication ended by remote peer');
    stop(true);
});

socket.on('iceCandidate', function (message) {
    console.info('Received message: ' + JSON.stringify(message));
    if (isStarted) {
        console.log(message.sdpMLineIndex);
        console.log(message.candidate);
        var candidate = new RTCIceCandidate({
            sdpMLineIndex: message.candidate.sdpMLineIndex,
            candidate: message.candidate.candidate
        });
        pc.addIceCandidate(candidate);
    }
});

function onError() {
    setCallState(NO_CALL);
}

function stop(message) {
    console.log('Hanging up !');
    setCallState(NO_CALL);
    remoteVideo.srcObject = null;
    if (pc != null) {
        pc.close();
        pc = null;
        if (!message) {
            socket.emit('stop', function (data) {
                console.info('Received message: ' + JSON.stringify(data));
            });
            console.log('sending stop message');
        }
    }
}

navigator.mediaDevices.getUserMedia({
    audio: false,
    video: true
}).then(openLocalStream)
    .catch(function (e) {
        alert('getUserMedia() error: ' + e.name);
    });

function openLocalStream(stream) {
    console.log('Open local video stream');
    localVideo.srcObject = stream;
    localStream = stream;
}

function createPeerConnection() {
    try {
        pc = new RTCPeerConnection(null);
        pc.onicecandidate = handleIceCandidate;
        pc.onaddstream = handleRemoteStreamAdded;
        pc.onremovestream = handleRemoteStreamRemoved;
        console.log('RTCPeerConnnection Created');
    } catch (e) {
        console.log('Failed to create PeerConnection, exception: ' + JSON.stringify(e.message));
        alert('Cannot create RTCPeerConnection object.');
    }
}

function createOfferAndSendMessage(sessionDescription) {
    console.log('CreateOfferAndSendMessage sending message', JSON.stringify(sessionDescription));
    pc.setLocalDescription(sessionDescription);
    var message = {
        from: document.getElementById('name').value,
        to: document.getElementById('peer').value,
        sdpOffer: sessionDescription.sdp
    };
    socket.emit('call', message, function (data) {
        if (data) {
            console.info('Received message: ' + JSON.stringify(data));
        }
    });
    console.log('sending call message:', JSON.stringify(message));
}

function createAnswerAndSendMessage(sessionDescription) {
    console.log('CreateAnswerAndSendMessage sending message', JSON.stringify(sessionDescription));
    pc.setLocalDescription(sessionDescription);
    var message = {
        from: from,
        callResponse: 'accept',
        sdpAnswer: sessionDescription.sdp
    };
    socket.emit('incomingCallResponse', message, function (data) {
        if (data) {
            console.info('Received message: ' + JSON.stringify(data));
        }
    });
    console.log('sending incomingCallResponse message:', JSON.stringify(message));
}

function handleCreateOfferError(event) {
    onError();
    console.log('CreateOffer() error: ', JSON.stringify(event));
}

function handleCreateAnswerError(error) {
    onError();
    console.log('CreateAnswer() error: ', JSON.stringify(error));
}

function handleIceCandidate(event) {
    console.log('Handle ICE candidate event: ', event);
    if (event.candidate) {
        console.log('sending onIceCandidate message:', JSON.stringify(event.candidate));
        var message = {
            sdpMid: event.candidate.sdpMid,
            sdpMLineIndex: event.candidate.sdpMLineIndex,
            candidate: event.candidate.candidate
        };
        socket.emit('onIceCandidate', message, function (data) {
            if (data) {
                console.info('Received message: ' + JSON.stringify(data));
            }
        });
    } else {
        console.log('End of candidates.');
    }
}

function handleRemoteStreamAdded(event) {
    console.log('Handle remote stream added.');
    remoteVideo.srcObject = event.stream;
}

function handleRemoteStreamRemoved(event) {
    console.log('Handle remote stream removed. Event: ', JSON.stringify(event));
    remoteVideo.srcObject = null;
}

function sendConnect() {
    socket.connect();
}

function sendDisconnect() {
    socket.disconnect();
}



