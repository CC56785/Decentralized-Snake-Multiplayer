package application.network;

import application.NetworkApplication;
import application.command.CommandHandler;
import application.command.control.ConnectToControlMessage;
import application.command.control.ControlMessage;
import application.command.control.ControlMessageHandler;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class NetworkHandler {
	private final NetworkApplication networkApplication;
	private final CommandHandler controlMessageHandler;
	private final DiscoveryHandler discoveryHandler;
	private final NewConnectionsHandler newConnectionsHandler;
	private final Set<NetworkPeer> peers;

	private InterfaceAddress localNetworkInterfaceAddress = null;

	public NetworkHandler(NetworkApplication networkApplication) {
		this.networkApplication = networkApplication;
		controlMessageHandler = new ControlMessageHandler(networkApplication);
		discoveryHandler = new DiscoveryHandler(networkApplication);
		newConnectionsHandler = new NewConnectionsHandler(networkApplication);
		peers = new HashSet<>();
	}

	public void startNetworkHandler() {
		discoveryHandler.startListeningForDiscoveries();
		newConnectionsHandler.startListeningForIncomingConnections();
	}

	public void closeNetworkHandler() {
		discoveryHandler.closeDiscoveryHandler();
		newConnectionsHandler.closeNewConnectionsHandler();
		for (NetworkPeer peer : peers) {
			peer.disconnectPeer();
		}
	}

	public String getLocalIP() {
		InterfaceAddress interfaceAddress = getLocalInterfaceAddress();
		return interfaceAddress.getAddress().getHostAddress();
	}

	private InterfaceAddress getLocalInterfaceAddress() {
		if (localNetworkInterfaceAddress == null) {
			initializeLocalInterfaceAddress();
		}
		return localNetworkInterfaceAddress;
	}

	/**
	 * Sets the local Network Interface Address to a valid Interface. Preferable tries to find a wi-fi Interface.
	 */
	private void initializeLocalInterfaceAddress() {
		Map<NetworkInterface, InterfaceAddress> validAddresses = new HashMap<>();
		try {
			List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
			for (NetworkInterface networkInterface : interfaces) {
				if (!networkInterface.isUp() || networkInterface.isLoopback() || networkInterface.isVirtual()) continue;

				List<InterfaceAddress> addresses = networkInterface.getInterfaceAddresses();
				for (InterfaceAddress interfaceAddress : addresses) {
					InetAddress address = interfaceAddress.getAddress();
					if (address instanceof Inet4Address && !address.isLoopbackAddress()) {
						validAddresses.put(networkInterface, interfaceAddress);
					}
				}
			}
		} catch (SocketException e) {
			throw new RuntimeException("Failed while trying to find a valid Network Interface:" + e);
		}
		if (validAddresses.isEmpty()) {
			throw new RuntimeException("Could not find a valid Network Interface!");
		}
		for (NetworkInterface networkInterface : validAddresses.keySet()) {
			String name = networkInterface.getName().toLowerCase();
			String desc = networkInterface.getDisplayName().toLowerCase();
			if (name.contains("wireless") || name.contains("wi-fi") || desc.contains("wireless") || desc.contains("wi-fi")) {
				localNetworkInterfaceAddress = validAddresses.get(networkInterface);
				return;
			}
		}
		localNetworkInterfaceAddress = validAddresses.values().toArray(new InterfaceAddress[0])[0];
	}

	/**
	 * Adds a new peer to our known peers and also tells every other peer, to also connect to this new peer.
	 * @param peer the new peer
	 */
	public void addNewNetworkPeer(NetworkPeer peer) {
		if (hasConnectionTo(peer.getIp())) {
			throw new IllegalStateException("Can not add an already added Peer!");
		}
		sendControlMessage(null, new ConnectToControlMessage(), peer.getIp());
		peers.add(peer);
		peer.initializePeer();
	}

	/**
	 * Removes a peer from the managed peers and frees all its resources.
	 * @param peer the peer to be removed
	 */
	public void removeNetworkPeer(NetworkPeer peer) {
		peers.remove(peer);
		peer.disconnectPeer();
	}

	public boolean hasConnectionTo(String address) {
		for (NetworkPeer peer : peers) {
			if (peer.getIp().equals(address)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Tries to establish a new connection to the passed address, if no connection to this address exists yet.
	 * <p>
	 * This method can take quite a while to return! Consider calling it asynchronously!
	 * @param address the address to connect to
	 */
	public void connectTo(String address) throws UnknownHostException {
		connectTo(InetAddress.getByName(address));
	}

	/**
	 * Tries to establish a new connection to the passed address, if no connection to this address exists yet.
	 * <p>
	 * This method can take quite a while to return! Consider calling it asynchronously!
	 * @param address the address to connect to
	 */
	public void connectTo(InetAddress address) {
		if (hasConnectionTo(address.getHostAddress())) {
			networkApplication.getConsoleHandler().printSystemMessage("You are already connected with this Peer!");
		} else {
			newConnectionsHandler.createConnectionTo(address);
		}
	}

	/**
	 * Sends a certain Message to a specific peer, or all connected peers.
	 * @param peer the peer to send to, or null when broadcasting
	 * @param message the message
	 */
	public void sendMessage(NetworkPeer peer, String message) {
		sendAnyMessage(peer, message, false);
	}

	/**
	 * Sends a certain Control Message to a specific peer, or all connected peers.
	 * @param peer the peer to send to, or null when broadcasting
	 * @param messageType an instance of the type of Control Message that should get send
	 * @param args the arguments of the Control Message
	 */
	public void sendControlMessage(NetworkPeer peer, ControlMessage messageType, String... args) {
		String message = messageType.getAsString(args);
		sendAnyMessage(peer, message, true);
	}

	/**
	 * Sends a certain Message to a specific peer, or all connected peers.
	 * <p>
	 * Also makes sure the Console Handler gets notified.
	 * @param peer the peer to send to, or null when broadcasting
	 * @param message the message
	 * @param isControl whether the Message is a Control Message
	 */
	private void sendAnyMessage(NetworkPeer peer, String message, boolean isControl) {
		if (peer != null) {
			peer.sendMessage(message);
		} else {
			for (NetworkPeer p : peers) {
				p.sendMessage(message);
			}
		}
		networkApplication.getConsoleHandler().printSentMessage(peer, message, isControl);
	}

	/**
	 * This Method handles incoming Messages. It should get called, whenever a new Message has been received.
	 * @param peer the peer the message came from
	 * @param message the message
	 */
	public void handleReceivedMessage(NetworkPeer peer, String message) {
		if (ControlMessage.isControlMessage(message)) {
			networkApplication.getConsoleHandler().printControlMessage(peer, message);
			String errorMessage = controlMessageHandler.parseAndExecuteCommand(message);
			if (errorMessage != null) {
				throw new RuntimeException("Invalid Control Message Received: " + errorMessage);
			}
		} else {
			networkApplication.getConsoleHandler().printMessage(peer, message);
		}
	}
}
