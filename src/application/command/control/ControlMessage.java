package application.command.control;

import application.StandardStringPatterns;
import application.command.Command;

/**
 * This Interface extends the Command Interface by some useful utilities for Control Messages.
 * <p>
 * Control Messages are Commands that get send over the network and trigger a certain action on the receiving device.
 * <p>
 * It also defines some of the methods, that the Command interface provides, that are not required for Control Messages.
 */
public interface ControlMessage extends Command {
	@Override
	default String getName() {
		return null;
	}

	@Override
	default String[] getArgumentNames() {
		return new String[0];
	}

	@Override
	default String getDescription() {
		return null;
	}

	/**
	 * Returns the Control Message with the passed arguments as a String, ready to be sent through the network.
	 * @return the full Control Message
	 */
	default String getAsString(String... args) {
		if (args.length != getNumberOfArguments()) {
			throw new IllegalArgumentException("The passed number of arguments did not match the expected number of arguments!");
		}

		StringBuilder builder = new StringBuilder();
		builder.append(StandardStringPatterns.CONTROL_MESSAGE_PREFIX.get());
		builder.append(StandardStringPatterns.ARGS_SEPARATION_DELIMITER.get());
		builder.append(getIdentifierTag());
		for (String s : args) {
			builder.append(StandardStringPatterns.ARGS_SEPARATION_DELIMITER.get());
			builder.append(s);
		}

		return builder.toString();
	}

	/**
	 * Returns true when the passed String might be a Control Message. A String might be a Control Message
	 * when it starts with the {@link StandardStringPatterns#CONTROL_MESSAGE_PREFIX CONTROL_MESSAGE_PREFIX}.
	 */
	static boolean isControlMessage(String string) {
		return string.startsWith(StandardStringPatterns.CONTROL_MESSAGE_PREFIX.get());
	}
}
