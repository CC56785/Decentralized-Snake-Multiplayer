package application.network;

import application.NetworkApplication;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class NewConnectionsHandler {
	private static final int PORT = 54321;

	private final NetworkApplication networkApplication;

	private ServerSocket localServerSocket;

	public NewConnectionsHandler(NetworkApplication networkApplication) {
		this.networkApplication = networkApplication;
	}

	/**
	 * This method can take quite a while to return! Consider calling it asynchronously!
	 */
	public void createConnectionTo(InetAddress address) {
		String message = "Trying to establish a connection with %s ...".formatted(address.getHostAddress());
		networkApplication.getConsoleHandler().printSystemMessage(message);

		NetworkPeer networkPeer;
		try {
			networkPeer = new NetworkPeer(networkApplication.getNetworkHandler(), address, PORT);
		} catch (IOException e) {
			String m = "Something went wrong when trying to connect to %s. No connection has been established.".formatted(address.getHostAddress());
			networkApplication.getConsoleHandler().printSystemMessage(m);
			return;
		}
		networkApplication.getConsoleHandler().printSystemMessage("Successfully connected to %s.".formatted(networkPeer.getIp()));
		networkApplication.getNetworkHandler().addNewNetworkPeer(networkPeer);
	}

	public void closeNewConnectionsHandler() {
		try {
			localServerSocket.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void startListeningForIncomingConnections() {
		new Thread(this::listenForIncomingConnections).start();
	}

	/**
	 * This method does not return until {@link NewConnectionsHandler#closeNewConnectionsHandler()} is called!
	 */
	private void listenForIncomingConnections() {
		try {
			listenForIncomingConnectionsWithExceptions();
		} catch (IOException e) {
			if (networkApplication.isRunning()) {
				throw new RuntimeException(e);
			}
		} finally {
			closeNewConnectionsHandler();
		}
	}

	/**
	 * This method does not return until {@link NewConnectionsHandler#closeNewConnectionsHandler()} is called,
	 * in which case an IO Exception will be thrown!
	 * @throws IOException when an underlying system threw an exception, or when
	 * {@link NewConnectionsHandler#closeNewConnectionsHandler()} has been called.
	 */
	private void listenForIncomingConnectionsWithExceptions() throws IOException {
		localServerSocket = new ServerSocket(PORT);
		while (networkApplication.isRunning()) {
			Socket newPeerSocket = localServerSocket.accept();
			NetworkPeer newPeer = new NetworkPeer(networkApplication.getNetworkHandler(), newPeerSocket);
			if (!networkApplication.getNetworkHandler().hasConnectionTo(newPeer.getIp())) {
				networkApplication.getConsoleHandler().printSystemMessage("New Peer at %s has joined the lobby.".formatted(newPeer.getIp()));
				networkApplication.getNetworkHandler().addNewNetworkPeer(newPeer);
			} else {
				newPeer.disconnectPeer();
			}
		}
	}
}
