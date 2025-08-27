package application;

/**
 * This Enum contains certain String Patterns that get used in multiple places in the program.
 */
public enum StandardStringPatterns {
	/**
	 * The Prefix every {@link application.command.control.ControlMessage ControlMessage} starts with.
	 */
	CONTROL_MESSAGE_PREFIX("$c"),
	/**
	 * The prefix that indicates the start of a {@link application.command.Command Command} identifier or name.
	 * Should always be placed directly in front of the identifier/name.
	 */
	COMMAND_PREFIX("/"),
	/**
	 * The Delimiter that always gets used whenever commands, arguments,
	 * or even the {@link StandardStringPatterns#CONTROL_MESSAGE_PREFIX}
	 * need to be separated from each other when building or reading a String.
	 */
	ARGS_SEPARATION_DELIMITER(" ");

	private final String pattern;

	StandardStringPatterns(String pattern) {
		this.pattern = pattern;
	}

	/**
	 * Returns this Pattern.
	 * @return the pattern
	 */
	public String get() {
		return pattern;
	}
}
