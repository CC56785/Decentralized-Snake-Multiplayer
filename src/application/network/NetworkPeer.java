package application.network;

import application.NetworkApplication;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a remote user and handles the actual connection to him.
 * It also handles the sending and receiving of Messages.
 */
public class NetworkPeer {
	private static final String END_OF_MESSAGE_INDICATOR = "\n";

	private final NetworkApplication networkApplication;
	private final Socket socket;

	private boolean isAlive;

	/**
	 * Creates a new Peer by trying to establish a connection to the passed address.
	 * @param networkApplication the Network Application this peer belongs to
	 * @param address the address to connect to
	 * @param port the port to connect to
	 * @throws IOException when no connection could be established
	 */
	public NetworkPeer(NetworkApplication networkApplication, InetAddress address, int port) throws IOException {
		this.networkApplication = networkApplication;
		socket = new Socket(address, port);
		isAlive = true;
	}

	/**
	 * Creates a new Peer associated with the passed socket.
	 * @param networkApplication the Network Application this peer belongs to
	 * @param socket the socket to the other peer
	 */
	public NetworkPeer(NetworkApplication networkApplication, Socket socket) {
		this.networkApplication = networkApplication;
		this.socket = socket;
		isAlive = true;
	}

	/**
	 * Activates this Peer, so it can send and receive messages.
	 */
	public void initializePeer() {
		new Thread(new MessageReceiver()).start();
	}

	/**
	 * Closes the connection to this Peer and releases all its resources.
	 */
	public void disconnectPeer() {
		isAlive = false;
		try {
			socket.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void sendMessage(String message) {
		if (message.contains(END_OF_MESSAGE_INDICATOR)) {
			throw new IllegalArgumentException("Messages can not contain the END_OF_MESSAGE_INDICATOR!");
		}
		String fullMessage = message + END_OF_MESSAGE_INDICATOR;
		try {
			OutputStream out = socket.getOutputStream();
			out.write(fullMessage.getBytes());
			out.flush();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public String getName() {
		// TODO: fix this
		return "lol";
	}

	public String getIp() {
		return socket.getInetAddress().getHostAddress();
	}

	/**
	 * This inner class contains all the Methods for receiving messages and forwarding them to the NetworkHandler.
	 * <p>
	 * This class ensures, that only actual full messages, so byte arrays terminated by an END_OF_MESSAGE_INDICATOR,
	 * get forwarded. That means even multiple messages in a single packet, as well as one message spanning multiple packets,
	 * all get handled correctly.
	 */
	private class MessageReceiver implements Runnable {
		@Override
		public void run() {
			receiveMessages();
		}

		/**
		 * This method does not return until {@link NetworkPeer#disconnectPeer()} is called!
		 */
		private void receiveMessages() {
			try {
				receiveMessagesWithExceptions();
			} catch (IOException e) {
				if (isAlive) {
					throw new RuntimeException(e);
				}
			}
		}

		/**
		 * Receives Messages and forwards them to the NetworkHandler via
		 * {@link NetworkHandler#handleReceivedMessage}.
		 * <p>
		 * This method does not return until {@link NetworkPeer#disconnectPeer()} is called,
		 * in which case an IO Exception will be thrown!
		 * @throws IOException when an underlying system threw an exception, or when
		 * {@link NetworkPeer#disconnectPeer()} has been called.
		 */
		private void receiveMessagesWithExceptions() throws IOException {
			InputStream in = socket.getInputStream();
			// Since TCP is stream based and not message based we might only receive a partial message or multiple messages at once.
			List<byte[]> fullMessage = new ArrayList<>();
			while (isAlive) {
				boolean hasFullMessage = containsFullMessage(fullMessage);
				while (!hasFullMessage) {
					fullMessage.add(receiveDataWithBlock(in));
					hasFullMessage = containsFullMessage(fullMessage);
				}
				forwardFullMessageAndExtractRemainder(fullMessage);
			}
		}

		private byte[] receiveDataWithBlock(InputStream in) throws IOException {
			// Tries to only read one Byte first, so we block until data arrives. Once that happens we can inquire how much data has arrived.
			int first = in.read();

			if (first == -1) {
				networkApplication.getNetworkHandler().removeNetworkPeer(NetworkPeer.this);
				networkApplication.getConsoleHandler().printSystemMessage("Peer %s has disconnected.".formatted(getName()));
				throw new IOException("Peer disconnected.");
			}
			byte firstByte = (byte) first;

			int numberOfBytesToRead = in.available();
			byte[] data = new byte[numberOfBytesToRead + 1];
			data[0] = firstByte;
			in.readNBytes(data, 1, numberOfBytesToRead);
			return data;
		}

		private void forwardFullMessageAndExtractRemainder(List<byte[]> fullMessage) {
			if (new String(fullMessage.getLast()).endsWith(END_OF_MESSAGE_INDICATOR)) {
				String message = extractMessage(fullMessage);
				networkApplication.getNetworkHandler().handleReceivedMessage(NetworkPeer.this, message);
				fullMessage.clear();
			} else {
				String last = new String(fullMessage.getLast());
				String[] split = last.split(END_OF_MESSAGE_INDICATOR, 2);

				fullMessage.set(fullMessage.size() - 1, split[0].getBytes());
				String message = extractMessage(fullMessage);
				networkApplication.getNetworkHandler().handleReceivedMessage(NetworkPeer.this, message);

				fullMessage.clear();
				fullMessage.add(split[1].getBytes());
			}
		}

		private boolean containsFullMessage(List<byte[]> message) {
			// Only checks the last part, since the other parts have been checked previously.
			return !message.isEmpty() && new String(message.getLast()).contains(END_OF_MESSAGE_INDICATOR);
		}

		private String extractMessage(List<byte[]> message) {
			StringBuilder sb = new StringBuilder();
			for (byte[] array : message) {
				sb.append(new String(array));
			}
			return sb.toString();
		}
	}
}
