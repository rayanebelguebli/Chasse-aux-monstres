package fr.univlille.multiplayer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.SocketException;
import java.net.Socket;

/**
 * This class holds the server socket and is responsible
 * of the communications between the host and the player(s).
 */
public class Server extends MultiplayerBody {
	private static Server instance;
	private ServerSocket server;
	private Socket clientSocket;
	private String clientHostname;

	// This class cannot get instantiated outside of the class itself.
	private Server() { }

	/**
	 * This class is a singleton.
	 * In order to use the methods of this class,
	 * you need to get the unique instance.
	 * @return The unique instance of the `Server` class.
	 */
	public static Server getInstance() {
		if (instance == null) {
			instance = new Server();
		}
		return instance;
	}

	/**
	 * Creates a server socket and listens to client messages in non-blocking threads.
	 * It will accept any incoming request as long as `acceptingNewUsers` is `true`.
	 * By default, this function will set `acceptingNewUsers` to `true`.
	 * The clients will need the hostname (the name of the phsyical machine) in order to subscribe.
	 * @param port The port that the server will use for its communications.
	 */
	public void host(int port) throws IOException {
		try {
			server = new ServerSocket(port);
			new Thread(() -> {
				try {
					while (isAlive()) {
						// the server will accept requests
						// as long as there is no client.
						if (!hasClient()) {
							Socket clientSocket = server.accept();
							System.out.println("accepted new user : " + clientSocket);
							welcomeIncomingClient(clientSocket);
							new Thread(new ClientHandler(clientSocket)).start();
						}
					}
				} catch (IOException e) {
					System.err.println("Exception caught when trying to listen on port " + port + " or listening for a connection");
					System.err.println(e.getMessage());
				}
			}).start();
		} catch (SocketException e) {
			System.err.println("SOCKET EXCEPTION in host() of Server : this.server = " + this.server);
			System.err.println(e.getMessage());
			System.err.println(e.getCause());
		}
	}

	/**
	 * Checks if the server has a player.
	 * A server must be alive to have a player.
	 * @return `true` if the server has a player, `false` otherwise.
	 */
	public boolean hasClient() {
		// just making sure that if the client's socket is closed
		// then we delete its reference, as it is no longer needed
		if (clientSocket != null && clientSocket.isClosed()) {
			clientSocket = null;
			clientHostname = null;
		}
		return isAlive() && clientSocket != null;
	}

	/**
	 * Returns the hostname of the client that was previously saved by the server.
	 * It's useful when the host wants to go back to the LobbyController.
	 * @return The name of the client to be displayed in the lobby.
	 */
	public String getSavedClientHostname() {
		return clientHostname;
	}

	/**
	 * Sets the hostname of the client
	 * so as to make sure the host never forgets it
	 * when he decides to go back to the lobby.
	 * @param hostname The hostname of the client.
	 */
	public void setClientHostname(String hostname) {
		this.clientHostname = hostname;
	}

	/**
	 * Stops the server, closes the client socket, deletes the `onIncomingCommunicationCallback`
	 * and drops all the communications currently waiting to be read in the buffer.
	 * 
	 * The server has to be restarted (by calling `host()`) if it needs to be used again.
	 * 
	 * This method will inform all the listener of the server termination
	 * by broadcasting a communication of type `MultiplayerCommand.SERVER_TERMINATION`.
	 * @throws IOException
	 */
	@Override
	public void kill() throws IOException {
		if (!isAlive()) {
			return;
		}
		super.kill();
		if (hasClient()) {
			broadcast(
				new MultiplayerCommunication(
					MultiplayerCommand.SERVER_TERMINATION
				)
			);
			clientSocket.close();
			clientSocket = null;
			clientHostname = null;
		}
		server.close();
	}

	/**
	 * Checks if the server is running (the `ServerSocket` shouldn't be closed).
	 * @return `true` if the server is running, `false` otherwise.
	 */
	@Override
	public boolean isAlive() {
		// the server socket will never be null,
		// unless `host` was never called.
		return server != null && !server.isClosed(); // `server` can be null if the instance is created, but `host()` has never been called.
	}

	/**
	 * Sends a message to a client using its output stream.
	 * @param socket  The socket instance of the client.
	 * @param message The message to send to the client.
	 * @throws IOException If the output stream of the client socket led to an Exception.
	 */
	private void writeToClient(Socket clientSocket, MultiplayerCommunication message) throws IOException {
		MultiplayerUtils.getOutputFromSocket(clientSocket).println(message.toString());
	}

	/**
	 * Sends a welcome message to the client, confirming its successfull connection.
	 * The server sends its name along with the message.
	 * @param clientSocket The socket of the incoming client.
	 * @throws IOException
	 */
	private void welcomeIncomingClient(Socket client) throws IOException {
		writeToClient(client, new MultiplayerCommunication(MultiplayerCommand.HOST, MultiplayerUtils.getHostname()));
		this.clientSocket = client;
	}

	/**
	 * Sends a message to the user that's listening to this server.
	 * @param message The message to broadcast.
	 */
	@Override
	public void broadcast(MultiplayerCommunication message) throws IOException {
		writeToClient(clientSocket, message);
	}

	/**
	 * Handles the communications of a client to the server.
	 * This class should not be used in the main thread
	 * because its `run` method is blocking.
	 */
	private final class ClientHandler implements Runnable {
		private Socket clientSocket;

		public ClientHandler(Socket clientSocket) {
			this.clientSocket = clientSocket;
		}

		@Override
		public void run() {
			try (
				PrintWriter out = MultiplayerUtils.getOutputFromSocket(clientSocket);
				BufferedReader in = MultiplayerUtils.getInputFromSocket(clientSocket);
			) {
				String inputLine;
				while (isAlive() && (inputLine = in.readLine()) != null) {
					System.out.println("Server received: " + inputLine);
					handle(inputLine);
				}
			} catch (SocketException e) {
				System.err.println("SOCKET exception in Server ClientHandler (" + server + ")");
				System.out.println(e.getMessage());
			} catch (IOException e) {
				System.err.println("Error handling client input");
				e.printStackTrace();
			}
		}

		/**
		 * Handles an incoming transmission from a user to the server.
		 * The transmission is a string that this method is going to parse
		 * into an instance of `MultiplayerCommunication`.
		 * 
		 * The communication, if valid, is added to the `incomingBuffer`.
		 * If defined, the `onIncomingCommunicationCallback` is runned.
		 * 
		 * If the communication command is `MultiplayerCommand.DISCONNECTION`,
		 * then the clientSocket is removed by calling `removeClientSocket`.
		 * 
		 * An invalid communication is ignored.
		 * @param input The line read from the input stream of the client socket.
		 */
		private void handle(String input) {
			try {
				MultiplayerCommunication incoming = new MultiplayerCommunication(input);
				incomingBuffer.add(incoming);
				if (onIncomingCommunicationCallback != null) {
					onIncomingCommunicationCallback.run();
				}
				// The client socket was closed client-side,
				// therefore the socket must be removed
				if (incoming.isCommand(MultiplayerCommand.DISCONNECTION)) {
					clientSocket = null;
				}
			} catch (InvalidCommunicationException e) {
				// Invalid communications are ignored.
				System.err.println("Server received invalid communication : " + input);
			}
		}
	}
}