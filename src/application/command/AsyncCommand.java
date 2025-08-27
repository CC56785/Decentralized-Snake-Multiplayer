package application.command;

import application.NetworkApplication;

/**
 * This Interface extends the Command Interface by providing an
 * {@link AsyncCommand#executeAsync asynchronous execute} Method. This Method automatically
 * gets called in a new Thread, whenever the {@link Command#execute execute} Method of the Command gets called.
 */
public interface AsyncCommand extends Command {
	@Override
	default void execute(NetworkApplication app, String[] args) {
		new Thread(() -> executeAsync(app, args)).start();
	}

	/**
	 * Executes this Command on the given arguments.
	 * <p>
	 * This Method should be called asynchronously!
	 * @param app the application to execute the command on
	 * @param args the arguments for this command
	 */
	void executeAsync(NetworkApplication app, String[] args);
}
