package net.lax1dude.eaglercraft.adapter.teavm;

import org.teavm.jso.JSFunctor;
import org.teavm.jso.JSObject;
import org.teavm.jso.typedarrays.ArrayBuffer;

public interface EaglercraftLANServer extends JSObject {

	boolean LANServerSupported();
	
	void initializeServer();
	
	void setICEServers(String[] urls);
	
	void setICECandidateHandler(ICECandidateHandler cb);
	
	void setDescriptionHandler(DescriptionHandler cb);
	
	void setRemoteClientDataChannelHandler(ClientSignalHandler cb);
	
	void setRemoteClientDisconnectHandler(ClientSignalHandler cb);
	
	void setRemoteClientPacketHandler(PeerPacketHandler cb);
	
	void sendPacketToRemoteClient(String peerId, ArrayBuffer buffer);

	void signalRemoteConnect(String peerId);
	
	void signalRemoteDescription(String peerId, String descJSON);
	
	void signalRemoteICECandidate(String peerId, String candidate);
	
	void signalRemoteDisconnect(String peerId);
	
	int countPeers();

	@JSFunctor
	public static interface ICECandidateHandler extends JSObject {
		void call(String peerId, String candidate);
	}
	
	@JSFunctor
	public static interface DescriptionHandler extends JSObject {
		void call(String peerId, String candidate);
	}
	
	@JSFunctor
	public static interface ClientSignalHandler extends JSObject {
		void call(String peerId);
	}
	
	@JSFunctor
	public static interface PeerPacketHandler extends JSObject {
		void call(String peerId, ArrayBuffer buffer);
	}
	
}
