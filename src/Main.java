import application.NetworkApplication;

/*
 * TODO: maybe add pause and pause-toggle commands
 * TODO: finish discover and connect
 * TODO: add control message for when you quit, so others know about it (use removePeer in NetworkHandler)? might not be necessary as handled in network peer now
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
