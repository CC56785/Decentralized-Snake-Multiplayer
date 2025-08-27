package application.command.user;

import application.NetworkApplication;
import application.command.Command;
import application.command.CommandHandler;

import java.util.ArrayList;
import java.util.List;

public class UserCommandHandler extends CommandHandler {
	public UserCommandHandler(NetworkApplication networkApplication) {
		super(networkApplication);
	}

	@Override
	protected List<Command> getCommands() {
		List<Command> commands = new ArrayList<>();
		commands.add(new HelpCommand());
		commands.add(new QuitCommand());
		commands.add(new LocalIPCommand());
		commands.add(new ConnectCommand());
		return commands;
	}
}
