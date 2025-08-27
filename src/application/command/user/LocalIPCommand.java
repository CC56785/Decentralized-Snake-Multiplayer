package application.command.user;

import application.NetworkApplication;
import application.command.Command;

public class LocalIPCommand implements Command {
	@Override
	public String getIdentifier() {
		return "ip";
	}

	@Override
	public String getName() {
		return "local-ip";
	}

	@Override
	public String[] getArgumentNames() {
		return new String[0];
	}

	@Override
	public String getDescription() {
		return "prints the ip address of your local machine";
	}

	@Override
	public int getNumberOfArguments() {
		return 0;
	}

	@Override
	public void execute(NetworkApplication app, String[] args) {
		String ip = app.getNetworkHandler().getLocalIP();
		app.getConsoleHandler().printSystemMessage("Your local ip is %s.".formatted(ip));
	}
}
