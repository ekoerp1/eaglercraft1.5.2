package net.lax1dude.eaglercraft;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import net.lax1dude.eaglercraft.sp.ipc.IPCPacket0CPlayerChannel;
import net.lax1dude.eaglercraft.sp.relay.pkt.*;

public class IntegratedServerLAN {
	
	public static final List<String> currentICEServers = new ArrayList<>();

	private static RelayServerSocket lanRelaySocket = null;
	
	private static String currentCode = null;

	public static String shareToLAN(Consumer<String> progressCallback, String worldName, boolean worldHidden) {
		currentCode = null;
		RelayServerSocket sock = IntegratedServer.relayManager.getWorkingRelay((str) -> progressCallback.accept("Connecting: " + str),
				IntegratedServer.preferredRelayVersion, worldName + (worldHidden ? ";1" : ";0"));
		if(sock == null) {
			lanRelaySocket = null;
			return null;
		}else {
			progressCallback.accept("Opening: " + sock.getURI());
			IPacket00Handshake hs = (IPacket00Handshake)sock.readPacket();
			lanRelaySocket = sock;
			String code = hs.connectionCode;
			System.out.println("Relay [" + sock.getURI() + "] connected as 'server', code: " + code);
			progressCallback.accept("Opened '" + code + "' on " + sock.getURI());
			long millis = EaglerAdapter.steadyTimeMillis();
			do {
				if(sock.isClosed()) {
					System.out.println("Relay [" + sock.getURI() + "] connection lost");
					lanRelaySocket = null;
					return null;
				}
				IPacket pkt = sock.readPacket();
				if(pkt != null) {
					if(pkt instanceof IPacket01ICEServers) {
						IPacket01ICEServers ipkt = (IPacket01ICEServers)pkt;
						System.out.println("Relay [" + sock.getURI() + "] provided ICE servers:");
						currentICEServers.clear();
						for(net.lax1dude.eaglercraft.sp.relay.pkt.ICEServerSet.RelayServer srv : ipkt.servers) {
							System.out.println("Relay [" + sock.getURI() + "]     " + srv.type.name()
									+ ": " + srv.address);
							currentICEServers.add(srv.getICEString());
						}
						EaglerAdapter.serverLANInitializeServer(currentICEServers.toArray(new String[currentICEServers.size()]));
						return currentCode = code;
					}else {
						System.err.println("Relay [" + sock.getURI() + "] unexpected packet: " + pkt.getClass().getSimpleName());
						closeLAN();
						return null;
					}
				}
				EaglerAdapter.sleep(50);
			}while(EaglerAdapter.steadyTimeMillis() - millis < 2500l);
			System.out.println("Relay [" + sock.getURI() + "] relay provide ICE servers timeout");
			closeLAN();
			return null;
		}
	}
	
	public static String getCurrentURI() {
		return lanRelaySocket == null ? "<disconnected>" : lanRelaySocket.getURI();
	}
	
	public static String getCurrentCode() {
		return currentCode == null ? "<undefined>" : currentCode;
	}

	public static void closeLAN() {
		closeLANNoKick();
		EaglerAdapter.serverLANCloseServer();
		cleanupLAN();
	}
	
	public static void closeLANNoKick() {
		if(lanRelaySocket != null) {
			lanRelaySocket.close();
			lanRelaySocket = null;
			currentCode = null;
		}
	}
	
	public static void cleanupLAN() {
		Iterator<LANClient> itr = clients.values().iterator();
		while(itr.hasNext()) {
			itr.next().disconnect();
		}
		clients.clear();
	}

	public static boolean isHostingLAN() {
		return lanRelaySocket != null || EaglerAdapter.countPeers() > 0;
	}

	public static boolean isLANOpen() {
		return lanRelaySocket != null;
	}
	
	private static final Map<String, LANClient> clients = new HashMap<>();
	
	public static void updateLANServer() {
		if(lanRelaySocket != null) {
			IPacket pkt;
			while((pkt = lanRelaySocket.readPacket()) != null) {
				if(pkt instanceof IPacket02NewClient) {
					IPacket02NewClient ipkt = (IPacket02NewClient) pkt;
					if(clients.containsKey(ipkt.clientId)) {
						System.err.println("Relay [" + lanRelaySocket.getURI() + "] relay provided duplicate client '" + ipkt.clientId + "'");
					}else {
						clients.put(ipkt.clientId, new LANClient(ipkt.clientId));
					}
				}else if(pkt instanceof IPacket03ICECandidate) {
					IPacket03ICECandidate ipkt = (IPacket03ICECandidate) pkt;
					LANClient c = clients.get(ipkt.peerId);
					if(c != null) {
						c.handleICECandidates(ipkt.candidate);
					}else {
						System.err.println("Relay [" + lanRelaySocket.getURI() + "] relay sent IPacket03ICECandidate for unknown client '" + ipkt.peerId + "'");
					}
				}else if(pkt instanceof IPacket04Description) {
					IPacket04Description ipkt = (IPacket04Description) pkt;
					LANClient c = clients.get(ipkt.peerId);
					if(c != null) {
						c.handleDescription(ipkt.description);
					}else {
						System.err.println("Relay [" + lanRelaySocket.getURI() + "] relay sent IPacket04Description for unknown client '" + ipkt.peerId + "'");
					}
				}else if(pkt instanceof IPacket05ClientSuccess) {
					IPacket05ClientSuccess ipkt = (IPacket05ClientSuccess) pkt;
					LANClient c = clients.get(ipkt.clientId);
					if(c != null) {
						c.handleSuccess();
					}else {
						System.err.println("Relay [" + lanRelaySocket.getURI() + "] relay sent IPacket05ClientSuccess for unknown client '" + ipkt.clientId + "'");
					}
				}else if(pkt instanceof IPacket06ClientFailure) {
					IPacket06ClientFailure ipkt = (IPacket06ClientFailure) pkt;
					LANClient c = clients.get(ipkt.clientId);
					if(c != null) {
						c.handleFailure();
					}else {
						System.err.println("Relay [" + lanRelaySocket.getURI() + "] relay sent IPacket06ClientFailure for unknown client '" + ipkt.clientId + "'");
					}
				}else if(pkt instanceof IPacketFFErrorCode) {
					IPacketFFErrorCode ipkt = (IPacketFFErrorCode) pkt;
					System.err.println("Relay [" + lanRelaySocket.getURI() + "] error code thrown: " +
							IPacketFFErrorCode.code2string(ipkt.code) + "(" + ipkt.code + "): " + ipkt.desc);
					Throwable t;
					while((t = lanRelaySocket.getException()) != null) {
						t.printStackTrace();
					}
				}else {
					System.err.println("Relay [" + lanRelaySocket.getURI() + "] unexpected packet: " + pkt.getClass().getSimpleName());
				}
			}
			if(lanRelaySocket.isClosed()) {
				lanRelaySocket = null;
			}
		}
		Iterator<LANClient> itr = clients.values().iterator();
		while(itr.hasNext()) {
			LANClient cl = itr.next();
			cl.update();
			if(cl.dead) {
				itr.remove();
			}
		}
	}
	
	private static final class LANClient {
		
		private static final int PRE = 0, RECEIVED_ICE_CANDIDATE = 1, SENT_ICE_CANDIDATE = 2, RECEIVED_DESCRIPTION = 3,
				SENT_DESCRIPTION = 4, RECEIVED_SUCCESS = 5, CONNECTED = 6, CLOSED = 7;

		protected final String clientId;
		protected final String channelId;
		
		protected int state = PRE;
		protected boolean dead = false;
		protected String localICECandidate = null;
		protected boolean localChannel = false;
		protected List<byte[]> packetPreBuffer = null;
		protected final long startTime;
		
		protected LANClient(String clientId) {
			this.clientId = clientId;
			this.channelId = "NET|" + clientId;
			this.startTime = EaglerAdapter.steadyTimeMillis();
			EaglerAdapter.serverLANCreatePeer(clientId);
		}
		
		protected void handleICECandidates(String candidates) {
			if(state == SENT_DESCRIPTION) {
				EaglerAdapter.serverLANPeerICECandidates(clientId, candidates);
				if(localICECandidate != null) {
					lanRelaySocket.writePacket(new IPacket03ICECandidate(clientId, localICECandidate));
					localICECandidate = null;
					state = SENT_ICE_CANDIDATE;
				}else {
					state = RECEIVED_ICE_CANDIDATE;
				}
			}else {
				System.err.println("Relay [" + lanRelaySocket.getURI() + "] unexpected IPacket03ICECandidate for '" + clientId + "'");
			}
		}
		
		protected void handleDescription(String description) {
			if(state == PRE) {
				EaglerAdapter.serverLANPeerDescription(clientId, description);
				state = RECEIVED_DESCRIPTION;
			}else {
				System.err.println("Relay [" + lanRelaySocket.getURI() + "] unexpected IPacket04Description for '" + clientId + "'");
			}
		}
		
		protected void handleSuccess() {
			if(state == SENT_ICE_CANDIDATE) {
				if(localChannel) {
					EaglerAdapter.enableChannel(channelId);
					IntegratedServer.sendIPCPacket(new IPCPacket0CPlayerChannel(clientId, true));
					localChannel = false;
					if(packetPreBuffer != null) {
						for(byte[] b : packetPreBuffer) {
							EaglerAdapter.sendToIntegratedServer(channelId, b);
						}
						packetPreBuffer = null;
					}
					state = CONNECTED;
				}else {
					state = RECEIVED_SUCCESS;
				}
			}else {
				System.err.println("Relay [" + lanRelaySocket.getURI() + "] unexpected IPacket05ClientSuccess for '" + clientId + "'");
			}
		}
		
		protected void handleFailure() {
			if(state == SENT_ICE_CANDIDATE) {
				System.err.println("Client '" + clientId + "' failed to connect");
				disconnect();
			}else {
				System.err.println("Relay [" + lanRelaySocket.getURI() + "] unexpected IPacket06ClientFailure for '" + clientId + "'");
			}
		}
		
		protected void update() {
			if(state != CLOSED) {
				if(state != CONNECTED && EaglerAdapter.steadyTimeMillis() - startTime > 13000l) {
					System.out.println("LAN client '" + clientId + "' handshake timed out");
					disconnect();
					return;
				}
				PKT pk;
				while(state == CONNECTED && (pk = EaglerAdapter.recieveFromIntegratedServer("NET|" + clientId)) != null) {
					EaglerAdapter.serverLANWritePacket(clientId, pk.data);
				}
				List<LANPeerEvent> l = EaglerAdapter.serverLANGetAllEvent(clientId);
				if(l == null) {
					return;
				}
				read_loop: for(int i = 0, s = l.size(); i < s; ++i) {
					LANPeerEvent evt = l.get(i);
					if(evt instanceof LANPeerEvent.LANPeerDisconnectEvent) {
						System.out.println("LAN client '" + clientId + "' disconnected");
						disconnect();
					}else {
						switch(state) {
							case SENT_DESCRIPTION:{
								if(evt instanceof LANPeerEvent.LANPeerICECandidateEvent) {
									localICECandidate = ((LANPeerEvent.LANPeerICECandidateEvent)evt).candidates;
									continue read_loop;
								}
								break;
							}
							case RECEIVED_DESCRIPTION: {
								if(evt instanceof LANPeerEvent.LANPeerDescriptionEvent) {
									lanRelaySocket.writePacket(new IPacket04Description(clientId, ((LANPeerEvent.LANPeerDescriptionEvent)evt).description));
									state = SENT_DESCRIPTION;
									continue read_loop;
								}
								break;
							}
							case RECEIVED_ICE_CANDIDATE: {
								if(evt instanceof LANPeerEvent.LANPeerICECandidateEvent) {
									lanRelaySocket.writePacket(new IPacket03ICECandidate(clientId, ((LANPeerEvent.LANPeerICECandidateEvent)evt).candidates));
									state = SENT_ICE_CANDIDATE;
									continue read_loop;
								}else if(evt instanceof LANPeerEvent.LANPeerDataChannelEvent) {
									localChannel = true;
									continue read_loop;
								}else if(evt instanceof LANPeerEvent.LANPeerPacketEvent) {
									if(packetPreBuffer == null) packetPreBuffer = new LinkedList<>();
									packetPreBuffer.add(((LANPeerEvent.LANPeerPacketEvent)evt).payload);
									continue read_loop;
								}
								break;
							}
							case SENT_ICE_CANDIDATE: {
								if(evt instanceof LANPeerEvent.LANPeerDataChannelEvent) {
									localChannel = true;
									continue read_loop;
								}else if(evt instanceof LANPeerEvent.LANPeerPacketEvent) {
									if(packetPreBuffer == null) packetPreBuffer = new LinkedList<>();
									packetPreBuffer.add(((LANPeerEvent.LANPeerPacketEvent)evt).payload);
									continue read_loop;
								}
								break;
							}
							case RECEIVED_SUCCESS: {
								if(evt instanceof LANPeerEvent.LANPeerDataChannelEvent) {
									EaglerAdapter.enableChannel(channelId);
									IntegratedServer.sendIPCPacket(new IPCPacket0CPlayerChannel(clientId, true));
									if(packetPreBuffer != null) {
										for(byte[] b : packetPreBuffer) {
											EaglerAdapter.sendToIntegratedServer(channelId, b);
										}
										packetPreBuffer = null;
									}
									state = CONNECTED;
									continue read_loop;
								}
								break;
							}
							case CONNECTED: {
								if(evt instanceof LANPeerEvent.LANPeerPacketEvent) {
									EaglerAdapter.sendToIntegratedServer(channelId, ((LANPeerEvent.LANPeerPacketEvent)evt).payload);
									continue read_loop;
								}
								break;
							}
							default: {
								break;
							}
						}
						if(state != CLOSED) {
							System.err.println("LAN client '" + clientId + "' had an accident: " + evt.getClass().getSimpleName() + " (state " + state + ")");
						}
						disconnect();
						return;
					}
				}
			}else {
				disconnect();
			}
		}
		
		protected void disconnect() {
			if(!dead) {
				if(state == CONNECTED) {
					IntegratedServer.sendIPCPacket(new IPCPacket0CPlayerChannel(clientId, false));
					EaglerAdapter.disableChannel(channelId);
				}
				state = CLOSED;
				EaglerAdapter.serverLANDisconnectPeer(clientId);
				dead = true;
			}
		}
		
	}
}
