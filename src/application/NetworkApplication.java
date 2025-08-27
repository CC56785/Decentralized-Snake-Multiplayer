package application;

import application.network.NetworkHandler;

public class NetworkApplication {
	private final ConsoleHandler consoleHandler;
	private final NetworkHandler networkHandler;

	private boolean isRunning = false;

	public NetworkApplication() {
		consoleHandler = new ConsoleHandler(this);
		networkHandler = new NetworkHandler(this);
	}

	public void startApplication() {
		isRunning = true;
		networkHandler.startNetworkHandler();
		consoleHandler.startConsole();
	}

	public void quitApplication() {
		isRunning = false;
		networkHandler.closeNetworkHandler();
	}

	public boolean isRunning() {
		return isRunning;
	}

	public ConsoleHandler getConsoleHandler() {
		return consoleHandler;
	}

	public NetworkHandler getNetworkHandler() {
		return networkHandler;
	}
}
