import application.NetworkApplication;

/*
 * TODO: maybe add pause and pause-toggle commands
 * TODO: finish discover and connect
 * TODO: fix names of peers
 * TODO: add Console Handler Settings
 * TODO: ci/cd? see chatgpt chat
 */
public class Main {
	public static void main(String[] args) {
		NetworkApplication networkApplication = new NetworkApplication();
		networkApplication.startApplication();
	}
}
