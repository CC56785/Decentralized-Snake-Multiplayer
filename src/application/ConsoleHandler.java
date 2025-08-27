package application;

import application.command.Command;
import application.command.CommandHandler;
import application.command.control.ControlMessage;
import application.command.user.UserCommandHandler;
import application.network.NetworkPeer;

import java.util.Scanner;

public class ConsoleHandler {
	private static final String CONTROL_MESSAGE_PREFIX = "[Control] ";
	private static final String SENT_MESSAGE_FORMAT = "[Out->%s] ";
	private static final String SENT_CONTROL_MESSAGE_FORMAT = "[Out->%s][Control] ";
	private static final String SYSTEM_MESSAGE_PREFIX = "[System] ";
	private static final String LOCAL_MESSAGE_FORMAT = "You: %s";
	private static final String FOREIGN_MESSAGE_FORMAT = "%s: %s";

	private final NetworkApplication networkApplication;
	private final CommandHandler commandHandler;

	private Thread consoleHandlerThread;

	public ConsoleHandler(NetworkApplication networkApplication) {
		this.networkApplication = networkApplication;
		commandHandler = new UserCommandHandler(networkApplication);
	}

	/**
	 * Activates the local Console.
	 * <p>
	 * Note: This method will not return while the application is running.
	 */
	public void startConsole() {
		consoleHandlerThread = Thread.currentThread();
		printSystemMessage("Application started. Use \"/help\" to get a list of all commands,");
		printSystemMessage("or just write a normal message to send a message to all connected Peers.");
		try (Scanner scanner = new Scanner(System.in)) {
			while (networkApplication.isRunning()) {
				clearCurrentLine();
				prepareCurrentLineForInput();
				String input = scanner.nextLine();
				input = input.trim();

				if (Command.isCommand(input)) {
					String errorMessage = commandHandler.parseAndExecuteCommand(input);
					if (errorMessage != null) {
						printSystemMessage(errorMessage);
					}
				} else if (ControlMessage.isControlMessage(input)) {
					String m = "Your message can not begin with %s! Your message has not been sent.";
					printSystemMessage(m.formatted(StandardStringPatterns.CONTROL_MESSAGE_PREFIX.get()));
				} else {
					printMessage(null, input);
					networkApplication.getNetworkHandler().sendMessage(null, input);
				}
			}
		}
	}

	/**
	 * Prints a message received from a certain peer.
	 * @param peer the peer who send the message, or null if the message originates from the local user
	 * @param message the message
	 */
	public void printMessage(NetworkPeer peer, String message) {
		if (peer == null) {
			print(LOCAL_MESSAGE_FORMAT.formatted(message));
		} else {
			print(FOREIGN_MESSAGE_FORMAT.formatted(peer.getName(), message));
		}
	}

	/**
	 * Prints an info message from the system. This message may span multiple lines.
	 * @param message the message
	 */
	public void printSystemMessage(String message) {
		String messageWithPrefix = message.replaceAll(System.lineSeparator(), System.lineSeparator() + SYSTEM_MESSAGE_PREFIX);
		print(SYSTEM_MESSAGE_PREFIX + messageWithPrefix);
	}

	/**
	 * This Method should be called whenever a Control Message is received.
	 * Depending on the current settings, this message may then be printed.
	 * @param peer the peer who send the message, or null if the origin is unknown
	 * @param message the message
	 */
	public void printControlMessage(NetworkPeer peer, String message) {
		String origin = peer == null ? "Unknown" : peer.getName();
		print(CONTROL_MESSAGE_PREFIX + FOREIGN_MESSAGE_FORMAT.formatted(origin, message));
	}

	/**
	 * This Method should be called whenever a Message is being sent.
	 * Depending on the current settings, this message may then be printed.
	 * @param peer the peer the message is being sent to, or null if it's a broadcast
	 * @param message the message being sent
	 * @param isControl whether this message is a Control Message or not
	 */
	public void printSentMessage(NetworkPeer peer, String message, boolean isControl) {
		String target = peer == null ? "all" : peer.getName();
		String prefixFormat = isControl ? SENT_CONTROL_MESSAGE_FORMAT : SENT_MESSAGE_FORMAT;
		String prefix = prefixFormat.formatted(target);
		print(prefix + message);
	}

	private synchronized void print(String message) {
		clearCurrentLine();
		System.out.println(message);
		if (Thread.currentThread() != consoleHandlerThread) {
			// Re-prepare the line for input again, when the print has been called asynchronously.
			prepareCurrentLineForInput();
		}
	}

	private void clearCurrentLine() {
		System.out.print("\r\u001B[2K");
	}

	private void prepareCurrentLineForInput() {
		System.out.print("> ");
	}
}
