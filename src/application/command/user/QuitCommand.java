package application.command.user;

import application.NetworkApplication;
import application.command.Command;

public class QuitCommand implements Command {
	@Override
	public String getIdentifier() {
		return "q";
	}

	@Override
	public String getName() {
		return "quit";
	}

	@Override
	public String[] getArgumentNames() {
		return new String[0];
	}

	@Override
	public String getDescription() {
		return "closes the application";
	}

	@Override
	public int getNumberOfArguments() {
		return 0;
	}

	@Override
	public void execute(NetworkApplication app, String[] args) {
		app.getConsoleHandler().printSystemMessage("Closing Application.");
		app.quitApplication();
	}
}
