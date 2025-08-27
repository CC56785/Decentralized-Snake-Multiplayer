package application.command.control;

import application.NetworkApplication;
import application.command.AsyncCommand;

import java.net.UnknownHostException;

/**
 * This Control Message makes the receiving Peer try to establish a connection to the passed IP.
 */
public class ConnectToControlMessage implements ControlMessage, AsyncCommand {
	@Override
	public String getIdentifier() {
		return "con";
	}

	@Override
	public int getNumberOfArguments() {
		return 1;
	}

	/**
	 * @param args args[0] contains the IP to connect to, as a String
	 */
	@Override
	public void executeAsync(NetworkApplication app, String[] args) {
		String address = args[0];
		if (address.equals(app.getNetworkHandler().getLocalIP()) || app.getNetworkHandler().hasConnectionTo(address)) {
			return;
		}
		try {
			app.getNetworkHandler().connectTo(address);
		} catch (UnknownHostException e) {
			throw new IllegalArgumentException("This should never happen! The Connect To Control Message contained an illegal address: " + e.getMessage());
		}
	}
}
