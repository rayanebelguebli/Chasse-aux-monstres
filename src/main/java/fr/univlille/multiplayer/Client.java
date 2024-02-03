package fr.univlille.multiplayer;

import java.io.IOException;
import java.io.BufferedReader;
import java.net.UnknownHostException;
import java.net.Socket;

/**
 * A player that's not hosting the game, but joining the game created by a host.
 * The host uses the `Server` class, but a client doesn't have a server socket initialized.
 */
public class Client extends MultiplayerBody {
	private static Client instance;
	private Socket socket;

	// This class cannot get instantiated outside of the class itself.
	private Client() {}

	/**
	 * This class is a singleton.
	 * In order to use the methods of this class,
	 * you need to get the unique instance.
	 * @return The unique instance of the `Client` class.
	 */
	public static Client getInstance() {
		if (instance == null) {
			instance = new Client();
		}
		return instance;
	}

	/**
	 * Creates a socket between the server at the given address on the given port.
	 * @param serverAddress The address of the server (the name of the physical machine that's hosting the server).
	 * @param port          The port used by the server to communicate.
	 * @throws UnknownHostException If the given server address isn't found.
	 * @throws IOException          If for some reason the connection cannot be establish.
	 */
	public void connect(String serverAddress, int port) throws UnknownHostException, IOException {
		socket = new Socket(serverAddress, port);
		announcePresence();

		System.out.println("client successfully connected, and sent welcome to the server");

		new Thread(() -> {
			try {
				String serverMessage = "";
				BufferedReader in = MultiplayerUtils.getInputFromSocket(socket);
				while (isAlive() && (serverMessage = in.readLine()) != null) {
					handleCommunicationOfServer(serverMessage);
				}
			} catch (IOException e) {
				// When `kill()` is executed,
				// an IOException ("socket closed") is thrown here.
				// We catch it and we don't want to do anything with it.
			}
		}).start();
	}

	/**
	 * Handles the incoming communication of the server.
	 * This methods takes the stringified command read from the input stream of the socket. 
	 * 
	 * The communication is parsed into an instance of `MultiplayerCommunication`
	 * and added into the incoming buffer.
	 * 
	 * If defined, `onIncomingCommunicationCallback` is called.
	 * 
	 * An invalid communication is ignored.
	 * @param serverMessage The raw command as a string read from the input stream of the socket.
	 */
	private void handleCommunicationOfServer(String serverMessage) {
		try {
			MultiplayerCommunication incoming = new MultiplayerCommunication(serverMessage);
			incomingBuffer.add(incoming);
			if (onIncomingCommunicationCallback != null) {
				onIncomingCommunicationCallback.run();
			}
		} catch (InvalidCommunicationException e) {
			// An invalid communication is ignored.
			System.err.println("Server received invalid communication: " + serverMessage);
		}
	}

	/**
	 * Terminates the client socket and informs the server about it
	 * by sending a communication whose command is `MultiplayerCommand.DISCONNECTION`.
	 * The communication also holds the local address of the socket so that the Server can recognize this client.
	 * 
	 * The `onIncomingCommunicationCallback` is deleted and the incoming buffer is cleared.
	 * @throws IOException
	 */
	@Override
	public void kill() throws IOException {
		if (!isAlive()) {
			return;
		}
		super.kill();
		// Broadcast the disconnection even if the server has already been terminated
		broadcast(
			new MultiplayerCommunication(
				MultiplayerCommand.DISCONNECTION
			)
		);
		socket.close();
	}

	/**
	 * Checks if the client was initialized and if it's successfully connected to the server.
	 * @return `true` if the client is connected, `false` otherwise.
	 */
	@Override
	public boolean isAlive() {
		return socket != null && !socket.isClosed();
	}

	/**
	 * Sends a message to the server.
	 * @param message The message to send to the server.
	 * @throws IOException
	 */
	@Override
	public void broadcast(MultiplayerCommunication message) throws IOException {
		MultiplayerUtils.getOutputFromSocket(socket).println(message.toString());
	}

	/**
	 * Sends a message to the server announcing the successfull connection of the client.
	 * The server needs the hostname of the client so as to display a name in the UI of the lobby.
	 * @throws IOException
	 */
	private void announcePresence() throws IOException {
		broadcast(
			new MultiplayerCommunication(
				MultiplayerCommand.JOIN,
				MultiplayerUtils.getHostname()
			)
		);
	}
}