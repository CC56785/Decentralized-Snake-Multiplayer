package application.command;

import application.NetworkApplication;
import application.StandardStringPatterns;

import java.util.Arrays;
import java.util.List;

public abstract class CommandHandler {
	private final NetworkApplication networkApplication;
	private final List<Command> allCommands;

	protected CommandHandler(NetworkApplication networkApplication) {
		this.networkApplication = networkApplication;
		allCommands = getCommands();
	}

	/**
	 * Returns a List of all the Commands this CommandHandler should handle.
	 * @return all Commands this CommandHandler manages
	 */
	protected abstract List<Command> getCommands();

	/**
	 * Parses the inputted String and tries to execute the parsed command.
	 * <p>
	 * Might fail if the passed String did not decode a valid Command.
	 * @param commandString the command as a String
	 * @return null, when the command was executed successfully, an error message otherwise
	 */
	public String parseAndExecuteCommand(String commandString) {
		if (!Command.isCommand(commandString)) {
			return "The passed String did not appear to be a Command, as it did not start with the Command Prefix!";
		}

		String commandStringWithoutPrefix = commandString.substring(StandardStringPatterns.COMMAND_PREFIX.get().length());
		String[] parts = commandStringWithoutPrefix.split(StandardStringPatterns.ARGS_SEPARATION_DELIMITER.get());
		if (parts.length == 0) {
			throw new IllegalStateException("This should not be possible to reach!");
		}

		String commandIdentifierString = parts[0];
		Command command = getCommand(commandIdentifierString);
		if (command == null) {
			return "\"%s\" is not a valid Command! Use \"/help\" to get a list of all Commands!".formatted(commandIdentifierString);
		}

		String[] args = Arrays.copyOfRange(parts, 1, parts.length);
		if (args.length != command.getNumberOfArguments()) {
			String s = "Wrong number of Arguments! Expected %d Arguments but found %d Arguments!";
			return s.formatted(command.getNumberOfArguments(), args.length);
		}

		command.execute(networkApplication, args);
		return null;
	}

	private Command getCommand(String commandIdentifierAsString) {
		for (Command command : allCommands) {
			if (command.getIdentifier().equals(commandIdentifierAsString)) {
				return command;
			}
		}
		for (Command command : allCommands) {
			if (command.getName() != null && command.getName().equals(commandIdentifierAsString)) {
				return command;
			}
		}
		return null;
	}
}
