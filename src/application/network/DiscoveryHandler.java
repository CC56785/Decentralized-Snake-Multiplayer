package application.network;

import application.NetworkApplication;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 * This class can send out UDP discovery broadcasts into the network to find other devices.
 * It also provides a method that needs to be called for it to listen for incoming discoveries.
 */
public class DiscoveryHandler {
	private static final String DISCOVERY_MESSAGE = "DISCOVERY_MESSAGE";
	private static final int INCOMING_MESSAGE_BUFFER_SIZE = 128;
	private static final int DISCOVERY_PORT = 7653;

	private final NetworkApplication networkApplication;

	private DatagramSocket socket;

	public DiscoveryHandler(NetworkApplication networkApplication) {
		this.networkApplication = networkApplication;
	}

	public void closeDiscoveryHandler() {
		socket.close();
	}

	public void startListeningForDiscoveries() {
		new Thread(this::listenForDiscoveriesNoExceptions).start();
	}

	/**
	 * This method does not return until {@link DiscoveryHandler#closeDiscoveryHandler()} is called!
	 */
	private void listenForDiscoveriesNoExceptions() {
		try {
			listenForDiscoveries();
		} catch (IOException e) {
			if (networkApplication.isRunning()) {
				throw new RuntimeException(e);
			}
		} finally {
			socket.close();
		}
	}

	/**
	 * This method does not return until {@link DiscoveryHandler#closeDiscoveryHandler()} is called,
	 * in which case an IO Exception will be thrown!
	 * @throws IOException when an underlying system threw an exception, or when
	 * {@link DiscoveryHandler#closeDiscoveryHandler()} has been called.
	 */
	private void listenForDiscoveries() throws IOException {
		socket = new DatagramSocket(DISCOVERY_PORT);
		while (networkApplication.isRunning()) {
			byte[] incomingMessage = new byte[INCOMING_MESSAGE_BUFFER_SIZE];
			DatagramPacket incomingPacket = new DatagramPacket(incomingMessage, incomingMessage.length);

			socket.receive(incomingPacket);
			String message = new String(incomingMessage);

			networkApplication.getConsoleHandler().printControlMessage(null, message);

			if (message.equals(DISCOVERY_MESSAGE)) {
				networkApplication.getNetworkHandler().connectTo(incomingPacket.getAddress());
			}
		}
	}
}
