window.initializeVoiceClient=()=>{class k{constructor(a,b,c,e){this.client=a;this.peerId=b;this.peerConnection=c;this.stream=null;this.peerConnection.addEventListener("icecandidate",f=>{f.candidate&&this.client.iceCandidateHandler(this.peerId,JSON.stringify({sdpMLineIndex:f.candidate.sdpMLineIndex,candidate:f.candidate.candidate}))});this.peerConnection.addEventListener("track",f=>{this.rawStream=f.streams[0];const g=new Audio;g.autoplay=!0;g.muted=!0;g.onended=function(){g.remove()};g.srcObject=
this.rawStream;this.client.peerTrackHandler(this.peerId,this.rawStream)});this.peerConnection.addStream(this.client.localMediaStream.stream);e&&this.peerConnection.createOffer(f=>{this.peerConnection.setLocalDescription(f,()=>{this.client.descriptionHandler(this.peerId,JSON.stringify(f))},g=>{console.error('Failed to set local description for "'+this.peerId+'"! '+g);this.client.signalDisconnect(this.peerId)})},f=>{console.error('Failed to set create offer for "'+this.peerId+'"! '+f);this.client.signalDisconnect(this.peerId)});
this.peerConnection.addEventListener("connectionstatechange",f=>{"disconnected"!==this.peerConnection.connectionState&&"failed"!==this.peerConnection.connectionState||this.client.signalDisconnect(this.peerId)})}disconnect(){this.peerConnection.close()}mute(a){this.rawStream.getAudioTracks()[0].enabled=!a}setRemoteDescription(a){try{const b=JSON.parse(a);this.peerConnection.setRemoteDescription(b,()=>{"offer"===b.type&&this.peerConnection.createAnswer(c=>{this.peerConnection.setLocalDescription(c,
()=>{this.client.descriptionHandler(this.peerId,JSON.stringify(c))},e=>{console.error('Failed to set local description for "'+this.peerId+'"! '+e);this.client.signalDisconnect(this.peerId)})},c=>{console.error('Failed to create answer for "'+this.peerId+'"! '+c);this.client.signalDisconnect(this.peerId)})},c=>{console.error('Failed to set remote description for "'+this.peerId+'"! '+c);this.client.signalDisconnect(this.peerId)})}catch(b){console.error('Failed to parse remote description for "'+this.peerId+
'"! '+b),this.client.signalDisconnect(this.peerId)}}addICECandidate(a){try{this.peerConnection.addIceCandidate(new RTCIceCandidate(JSON.parse(a)))}catch(b){console.error('Failed to parse ice candidate for "'+this.peerId+'"! '+b),this.client.signalDisconnect(this.peerId)}}}class d{constructor(){this.ICEServers=[];this.hasInit=!1;this.peerList=new Map;this.readyState=0;this.microphoneVolumeAudioContext=this.peerDisconnectHandler=this.peerTrackHandler=this.descriptionHandler=this.iceCandidateHandler=
null}voiceClientSupported(){return"undefined"!==typeof window.RTCPeerConnection&&"undefined"!==typeof navigator.mediaDevices&&"undefined"!==typeof navigator.mediaDevices.getUserMedia}setICEServers(a){for(var b=this.ICEServers.length=0;b<a.length;++b){var c=a[b].split(";");1===c.length?this.ICEServers.push({urls:c[0]}):3===c.length&&this.ICEServers.push({urls:c[0],username:c[1],credential:c[2]})}}setICECandidateHandler(a){this.iceCandidateHandler=a}setDescriptionHandler(a){this.descriptionHandler=
a}setPeerTrackHandler(a){this.peerTrackHandler=a}setPeerDisconnectHandler(a){this.peerDisconnectHandler=a}activateVoice(a){this.hasInit&&(this.localRawMediaStream.getAudioTracks()[0].enabled=a)}initializeDevices(){this.hasInit?this.readyState=1:navigator.mediaDevices.getUserMedia({audio:!0,video:!1}).then(a=>{this.microphoneVolumeAudioContext=new AudioContext;this.localRawMediaStream=a;this.localRawMediaStream.getAudioTracks()[0].enabled=!1;this.localMediaStream=this.microphoneVolumeAudioContext.createMediaStreamDestination();
this.localMediaStreamGain=this.microphoneVolumeAudioContext.createGain();this.microphoneVolumeAudioContext.createMediaStreamSource(a).connect(this.localMediaStreamGain);this.localMediaStreamGain.connect(this.localMediaStream);this.readyState=this.localMediaStreamGain.gain.value=1;this.hasInit=!0}).catch(a=>{this.readyState=-1})}setMicVolume(a){this.hasInit&&(.5<a&&(a=.5+2*(a-.5)),1.5<a&&(a=1.5),0>a&&(a=0),this.localMediaStreamGain.gain.value=2*a)}getReadyState(){return this.readyState}signalConnect(a,
b){try{const c=new RTCPeerConnection({iceServers:this.ICEServers,optional:[{DtlsSrtpKeyAgreement:!0}]}),e=new k(this,a,c,b);this.peerList.set(a,e)}catch(c){}}signalDescription(a,b){a=this.peerList.get(a);"undefined"!==typeof a&&null!==a&&a.setRemoteDescription(b)}signalDisconnect(a,b){var c=this.peerList.get(a);if("undefined"!==typeof c&&null!==c){this.peerList.delete(c);try{c.disconnect()}catch(e){}this.peerDisconnectHandler(a,b)}}mutePeer(a,b){a=this.peerList.get(a);"undefined"!==typeof a&&null!==
a&&a.mute(b)}signalICECandidate(a,b){a=this.peerList.get(a);"undefined"!==typeof a&&null!==a&&a.addICECandidate(b)}}window.constructVoiceClient=()=>new d};window.startVoiceClient=()=>{"function"!==typeof window.constructVoiceClient&&window.initializeVoiceClient();return window.constructVoiceClient()};
window.initializeLANClient=()=>{class k{constructor(){this.ICEServers=[];this.dataChannel=this.peerConnection=null;this.readyState=1;this.remotePacketHandler=this.remoteDisconnectHandler=this.remoteDataChannelHandler=this.descriptionHandler=this.iceCandidateHandler=null}LANClientSupported(){return"undefined"!==typeof window.RTCPeerConnection}initializeClient(){try{null!==this.dataChannel&&(this.dataChannel.close(),this.dataChannel=null),null!==this.peerConnection&&this.peerConnection.close(),this.peerConnection=
new RTCPeerConnection({iceServers:this.ICEServers,optional:[{DtlsSrtpKeyAgreement:!0}]}),this.readyState=1}catch(d){this.readyState=-2}}setICEServers(d){for(var a=this.ICEServers.length=0;a<d.length;++a){var b=d[a].split(";");1===b.length?this.ICEServers.push({urls:b[0]}):3===b.length&&this.ICEServers.push({urls:b[0],username:b[1],credential:b[2]})}}setICECandidateHandler(d){this.iceCandidateHandler=d}setDescriptionHandler(d){this.descriptionHandler=d}setRemoteDataChannelHandler(d){this.remoteDataChannelHandler=
d}setRemoteDisconnectHandler(d){this.remoteDisconnectHandler=d}setRemotePacketHandler(d){this.remotePacketHandler=d}getReadyState(){return this.readyState}sendPacketToServer(d){null!==this.dataChannel&&"open"===this.dataChannel.readyState?this.dataChannel.send(d):this.signalRemoteDisconnect(!1)}signalRemoteConnect(){const d=[];this.peerConnection.addEventListener("icecandidate",a=>{if(a.candidate){if(0===d.length){let b=[0,0],c;setTimeout(c=()=>{if(null!==this.peerConnection&&"disconnected"!==this.peerConnection.connectionState){const e=
++b[1];b[0]!==d.length&&3>e?(b[0]=d.length,setTimeout(c,2E3)):(this.iceCandidateHandler(JSON.stringify(d)),d.length=0)}},2E3)}d.push({sdpMLineIndex:a.candidate.sdpMLineIndex,candidate:a.candidate.candidate})}});this.dataChannel=this.peerConnection.createDataChannel("lan");this.dataChannel.binaryType="arraybuffer";this.dataChannel.addEventListener("open",async a=>{for(;0<d.length;)await new Promise(b=>setTimeout(b,10));this.remoteDataChannelHandler(this.dataChannel)});this.dataChannel.addEventListener("message",
a=>{this.remotePacketHandler(a.data)},!1);this.peerConnection.createOffer(a=>{this.peerConnection.setLocalDescription(a,()=>{this.descriptionHandler(JSON.stringify(a))},b=>{console.error("Failed to set local description! "+b);this.readyState=-1;this.signalRemoteDisconnect(!1)})},a=>{console.error("Failed to set create offer! "+a);this.readyState=-1;this.signalRemoteDisconnect(!1)});this.peerConnection.addEventListener("connectionstatechange",a=>{"disconnected"===this.peerConnection.connectionState?
this.signalRemoteDisconnect(!1):"connected"===this.peerConnection.connectionState?this.readyState=2:"failed"===this.peerConnection.connectionState&&(this.readyState=-1,this.signalRemoteDisconnect(!1))})}signalRemoteDescription(d){try{this.peerConnection.setRemoteDescription(JSON.parse(d))}catch(a){console.error(a),this.readyState=-1,this.signalRemoteDisconnect(!1)}}signalRemoteICECandidate(d){try{const a=JSON.parse(d);for(let b of a)this.peerConnection.addIceCandidate(b)}catch(a){console.error(a),
this.readyState=-1,this.signalRemoteDisconnect(!1)}}signalRemoteDisconnect(d){null!==this.dataChannel&&(this.dataChannel.close(),this.dataChannel=null);null!==this.peerConnection&&this.peerConnection.close();d||this.remoteDisconnectHandler();this.readyState=0}}window.constructLANClient=()=>new k};window.startLANClient=()=>{"function"!==typeof window.constructLANClient&&window.initializeLANClient();return window.constructLANClient()};
window.initializeLANServer=()=>{class k{constructor(a,b,c){this.client=a;this.peerId=b;this.peerConnection=c;this.dataChannel=null;const e=[];let f=!1;this.peerConnection.addEventListener("icecandidate",g=>{if(g.candidate){if(0===e.length){let h=[0,0],l;setTimeout(l=()=>{if(null!==this.peerConnection&&"disconnected"!==this.peerConnection.connectionState){const m=++h[1];h[0]!==e.length&&3>m?(h[0]=e.length,setTimeout(l,2E3)):(this.client.iceCandidateHandler(this.peerId,JSON.stringify(e)),e.length=0,
f=!0)}},2E3)}e.push({sdpMLineIndex:g.candidate.sdpMLineIndex,candidate:g.candidate.candidate})}});this.peerConnection.addEventListener("datachannel",async g=>{for(;!f;)await new Promise(h=>setTimeout(h,10));this.dataChannel=g.channel;this.client.remoteClientDataChannelHandler(this.peerId,this.dataChannel);this.dataChannel.addEventListener("message",h=>{this.client.remoteClientPacketHandler(this.peerId,h.data)},!1)},!1);this.peerConnection.addEventListener("connectionstatechange",g=>{"disconnected"!==
this.peerConnection.connectionState&&"failed"!==this.peerConnection.connectionState||this.client.signalRemoteDisconnect(this.peerId)})}disconnect(){null!==this.dataChannel&&(this.dataChannel.close(),this.dataChannel=null);this.peerConnection.close()}setRemoteDescription(a){try{const b=JSON.parse(a);this.peerConnection.setRemoteDescription(b,()=>{"offer"===b.type&&this.peerConnection.createAnswer(c=>{this.peerConnection.setLocalDescription(c,()=>{this.client.descriptionHandler(this.peerId,JSON.stringify(c))},
e=>{console.error('Failed to set local description for "'+this.peerId+'"! '+e);this.client.signalRemoteDisconnect(this.peerId)})},c=>{console.error('Failed to create answer for "'+this.peerId+'"! '+c);this.client.signalRemoteDisconnect(this.peerId)})},c=>{console.error('Failed to set remote description for "'+this.peerId+'"! '+c);this.client.signalRemoteDisconnect(this.peerId)})}catch(b){console.error('Failed to parse remote description for "'+this.peerId+'"! '+b),this.client.signalRemoteDisconnect(this.peerId)}}addICECandidate(a){try{const b=
JSON.parse(a);for(let c of b)this.peerConnection.addIceCandidate(new RTCIceCandidate(c))}catch(b){console.error('Failed to parse ice candidate for "'+this.peerId+'"! '+b),this.client.signalRemoteDisconnect(this.peerId)}}}class d{constructor(){this.ICEServers=[];this.hasInit=!1;this.peerList=new Map;this.remoteClientPacketHandler=this.remoteClientDisconnectHandler=this.remoteClientDataChannelHandler=this.descriptionHandler=this.iceCandidateHandler=null}LANServerSupported(){return"undefined"!==typeof window.RTCPeerConnection}initializeServer(){}setICEServers(a){for(var b=
this.ICEServers.length=0;b<a.length;++b){var c=a[b].split(";");1===c.length?this.ICEServers.push({urls:c[0]}):3===c.length&&this.ICEServers.push({urls:c[0],username:c[1],credential:c[2]})}}setICECandidateHandler(a){this.iceCandidateHandler=a}setDescriptionHandler(a){this.descriptionHandler=a}setRemoteClientDataChannelHandler(a){this.remoteClientDataChannelHandler=a}setRemoteClientDisconnectHandler(a){this.remoteClientDisconnectHandler=a}setRemoteClientPacketHandler(a){this.remoteClientPacketHandler=
a}sendPacketToRemoteClient(a,b){var c=this.peerList.get(a);"undefined"!==typeof c&&null!==c&&(null!=c.dataChannel&&"open"===c.dataChannel.readyState?c.dataChannel.send(b):this.signalRemoteDisconnect(a))}signalRemoteConnect(a){try{const b=new RTCPeerConnection({iceServers:this.ICEServers,optional:[{DtlsSrtpKeyAgreement:!0}]}),c=new k(this,a,b);this.peerList.set(a,c)}catch(b){}}signalRemoteDescription(a,b){a=this.peerList.get(a);"undefined"!==typeof a&&null!==a&&a.setRemoteDescription(b)}signalRemoteICECandidate(a,
b){a=this.peerList.get(a);"undefined"!==typeof a&&null!==a&&a.addICECandidate(b)}signalRemoteDisconnect(a){if(0===a.length){for(var b of this.peerList.values())if("undefined"!==typeof b&&null!==b){this.peerList.delete(a);try{b.disconnect()}catch(c){}this.remoteClientDisconnectHandler(a)}this.peerList.clear()}else if(b=this.peerList.get(a),"undefined"!==typeof b&&null!==b){this.peerList.delete(a);try{b.disconnect()}catch(c){}this.remoteClientDisconnectHandler(a)}}countPeers(){return this.peerList.size}}
window.constructLANServer=()=>new d};window.startLANServer=()=>{"function"!==typeof window.constructLANServer&&window.initializeLANServer();return window.constructLANServer()};