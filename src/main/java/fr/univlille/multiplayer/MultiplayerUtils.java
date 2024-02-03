package fr.univlille.multiplayer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class MultiplayerUtils {
  private MultiplayerUtils() {}

  /**
   * Gets the output stream of a socket as an instance of `PrintWriter`.
   * The print writer is automatically flushed.
   * @param socket The socket to get the output stream from.
   * @return The output stream of the given socket as an instance of `PrintWriter`.
   * @throws IOException
   */
  public static PrintWriter getOutputFromSocket(Socket socket) throws IOException {
    return new PrintWriter(socket.getOutputStream(), true); // "true" to automatically send the information once something is written
  }

  /**
   * Gets the input stream of a socket as an instance of `BufferedReader`.
   * @param socket The socket to get the input stream from.
   * @return The input stream of the given socket as an instance of `BufferedReader`
   * @throws IOException
   */
  public static BufferedReader getInputFromSocket(Socket socket) throws IOException {
    return new BufferedReader(new InputStreamReader(socket.getInputStream()));
  }

  /**
   * Gets the multiplayer instance that's currently alive.
   * If the player is the host, then it will return the instance of the `Server` class,
   * if not, then it means that the player is the client, so the instance of `Client` is returned.
   */
  public static MultiplayerBody getMultiplayerInstance() {
    return Server.getInstance().isAlive() ? Server.getInstance() : Client.getInstance();
  }

  /**
   * Checks if one multiplayer instance is alive.
   * @return `true` if there is a multiplayer instance (meaning that we are playing a game in multiplayer mode, or a lobby has just been created).
   */
  public static boolean hasMultiplayerInstance() {
    return Server.getInstance().isAlive() || Client.getInstance().isAlive();
  }

  /**
	 * Gets the name of the host that's running the server.
	 * It's the name of the physical machine running this code.
	 * @return The name of the physical machine, or "???" if it's unknown.
	 */
	public static String getHostname() {
		try {
			return InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			return "???";
		}
	}
}
