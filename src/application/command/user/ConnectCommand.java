package application.command.user;

import application.NetworkApplication;
import application.command.AsyncCommand;

import java.net.UnknownHostException;

public class ConnectCommand implements AsyncCommand {
	@Override
	public String getIdentifier() {
		return "c";
	}

	@Override
	public String getName() {
		return "connect";
	}

	@Override
	public String[] getArgumentNames() {
		return new String[] { "ip" };
	}

	@Override
	public String getDescription() {
		return "tries to establish a connection to the specified ip";
	}

	@Override
	public int getNumberOfArguments() {
		return 1;
	}

	@Override
	public void executeAsync(NetworkApplication app, String[] args) {
		try {
			app.getNetworkHandler().connectTo(args[0]);
		} catch (UnknownHostException e) {
			app.getConsoleHandler().printSystemMessage("The specified ip was not in a valid format!");
		}
	}
}
