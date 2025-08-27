package application.command.user;

import application.NetworkApplication;
import application.command.Command;

import java.util.List;

public class HelpCommand implements Command {
	@Override
	public String getIdentifier() {
		return "h";
	}

	@Override
	public String getName() {
		return "help";
	}

	@Override
	public String[] getArgumentNames() {
		return new String[0];
	}

	@Override
	public String getDescription() {
		return "prints a list of all the commands";
	}

	@Override
	public int getNumberOfArguments() {
		return 0;
	}

	@Override
	public void execute(NetworkApplication app, String[] args) {
		StringBuilder sb = new StringBuilder("All Commands:");
		List<Command> commands = new UserCommandHandler(app).getCommands();
		for (Command command : commands) {
			sb.append(System.lineSeparator());
			sb.append(command.getIdentifierTag());
			for (String argument : command.getArgumentNames()) {
				sb.append(" <");
				sb.append(argument);
				sb.append(">");
			}
			sb.append(" (");
			sb.append(command.getName());
			sb.append(": ");
			sb.append(command.getDescription());
			sb.append(")");
		}
		app.getConsoleHandler().printSystemMessage(sb.toString());
	}
}
