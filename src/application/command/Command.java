package application.command;

import application.NetworkApplication;
import application.StandardStringPatterns;

/**
 * This interface describes a general purpose command.
 * Commands get handled by a {@link CommandHandler}.
 */
public interface Command {
	/**
	 * Returns the Identifier of this Command.
	 * The identifier is a very short and unique String, encoding this Command.
	 * <p>
	 * Important: Identifiers can NOT contain {@link StandardStringPatterns#ARGS_SEPARATION_DELIMITER spaces}.
	 * @return the Identifier
	 */
	String getIdentifier();

	/**
	 * Returns the full name of this Command.
	 * This is usually a longer and more descriptive version of the Identifier.
	 * <p>
	 * Important: Names can NOT contain {@link StandardStringPatterns#ARGS_SEPARATION_DELIMITER spaces}.
	 * @return the name of this Command or null if not specified
	 */
	String getName();

	/**
	 * Returns an Array of the size of {@link Command#getNumberOfArguments()}
	 * with the names of the respective Arguments, unless the names aren't specified,
	 * in which case an Array of size zero is returned.
	 * @return the names, or an Array of size zero
	 */
	String[] getArgumentNames();

	/**
	 * Returns a detailed Description of what this Command does.
	 * @return the Description or null if not specified
	 */
	String getDescription();

	/**
	 * Returns the number of arguments this Command expects.
	 * @return the required number of arguments
	 */
	int getNumberOfArguments();

	/**
	 * Executes this Command on the given arguments.
	 * @param app the application to execute the command on
	 * @param args the arguments for this command
	 */
	void execute(NetworkApplication app, String[] args);

	/**
	 * Returns the identifier of this Command with the {@link StandardStringPatterns#COMMAND_PREFIX COMMAND_PREFIX}
	 * placed in front of it.
	 * @return the full identifier tag
	 */
	default String getIdentifierTag() {
		return StandardStringPatterns.COMMAND_PREFIX.get() + getIdentifier();
	}

	/**
	 * Returns true when the passed String might be a Command. A String might be a Command
	 * when it starts with the {@link StandardStringPatterns#COMMAND_PREFIX COMMAND_PREFIX}.
	 */
	static boolean isCommand(String commandString) {
		return commandString.startsWith(StandardStringPatterns.COMMAND_PREFIX.get());
	}
}
