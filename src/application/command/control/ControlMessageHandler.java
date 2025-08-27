package application.command.control;

import application.NetworkApplication;
import application.StandardStringPatterns;
import application.command.Command;
import application.command.CommandHandler;

import java.util.ArrayList;
import java.util.List;

public class ControlMessageHandler extends CommandHandler {
	public ControlMessageHandler(NetworkApplication networkApplication) {
		super(networkApplication);
	}

	@Override
	protected List<Command> getCommands() {
		List<Command> commands = new ArrayList<>();
		commands.add(new ConnectToControlMessage());
		return commands;
	}

	@Override
	public String parseAndExecuteCommand(String commandString) {
		if (!ControlMessage.isControlMessage(commandString)) {
			return "The passed String did not appear to be a Control Message, as it did not start with the Control Message Prefix!";
		}
		String[] parts = commandString.split(StandardStringPatterns.ARGS_SEPARATION_DELIMITER.get(), 2);
		if (parts.length != 2 || !parts[0].equals(StandardStringPatterns.CONTROL_MESSAGE_PREFIX.get())) {
			throw new IllegalArgumentException("This should never happen! It seems like an invalid Control Message has been received!");
		}
		String actualCommandPart = parts[1];
		return super.parseAndExecuteCommand(actualCommandPart);
	}
}
