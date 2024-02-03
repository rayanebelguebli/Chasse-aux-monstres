package fr.univlille.multiplayer;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

/**
 * A multiplayer instance (the server, or a client)
 * capable of receiving and sending asynchronous messages.
 * 
 * The received messages need to be contained in a queue,
 * and the program will read them whenever it needs to.
 */
public abstract class MultiplayerBody {
	/**
	 * When a multiplayer body sends an asynchronous message to another,
	 * it stores it into a queue, waiting for the program to read them.
	 * Invalid communications are ignored.
	 */
	protected Queue<MultiplayerCommunication> incomingBuffer = new LinkedList<>();

	/**
	 * The callback to execute when the body is receiving a message (optional).
	 */
	protected Runnable onIncomingCommunicationCallback;

	/**
	 * There is only two possible states:
	 * - The host is a hunter, therefore the client is the monster.
	 * - The host is not a hunter, therefore the client is the hunter.
	 * This information has to be saved on both bodies (host & client).
	 */
	protected boolean ishunter = false;

	/**
	 * Is the multiplayer body a hunter? If true, it means that the other is the monster.
	 * @return `true` if the body is a hunter, `false` if it's the monster.
	 */
	public boolean isHunter() {
		return ishunter;
	}

	/**
	 * Sets whether or not this multiplayer body is the hunter or not.
	 * @param hunter `true` if this body should be the hunter, `false` if it should be the monster.
	 */
	public void setIsHunter(boolean hunter) {
		this.ishunter = hunter;
	}

	/**
	 * Checks if the buffer holding the pending requests isn't empty.
	 * @return `true` if there is no communication waiting to be read, `false` otherwise.
	 */
	public boolean hasPendingCommunication() {
		return !incomingBuffer.isEmpty();
	}

	/**
	 * Gets the oldest communication still pending.
	 * The communications come from the server,
	 * and the client holds them until the program reads them.
	 * @return An instance of `MultiplayerCommunication` or `null` if there is none.
	 */
	public MultiplayerCommunication pollCommunication() {
		return incomingBuffer.poll();
	}

	/**
	 * Deletes all pending communications.
	 */
	public void dropCommunications() {
		incomingBuffer.clear();
	}

	/**
	 * Sets the Runnable callback to execute when there is an incoming communication.
	 * It's very useful because if the server or the client are waiting for a communication,
	 * we don't want this action to block the main thread.
	 * 
	 * If there are pending requests when calling this method,
	 * then the given callback is ran as many times
	 * as there are pending requests.
	 * @param callback
	 */
	public void setIncomingCommunicationCallback(Runnable callback) {
		// WARNING: cannot use `while(hasPendingRequests()) { callback.run(); }`
		// because even tough the callback is ran, the actual code is ran using Platform.runLater()
		// and as a consequence, the very first callback is actually executed when the loop is over,
		// and so it would lead to infinite loop.
		// We must use a for loop, keeping in mind that the incomingBuffer will not get modified in this thread.
 		for (int i = 0; i < incomingBuffer.size(); i++) {
			callback.run();
		}
		onIncomingCommunicationCallback = callback;
	}

	/**
	 * Reset the callback that's executed when the body receives an incoming communication.
	 */
	public void stopIncomingCommunicationCallback() {
		onIncomingCommunicationCallback = null;
	}

	/**
	 * Checks if the multiplayer body is currently able to handle communications.
	 * @return `true` if a callback was defined.
	 */
	public boolean hasIncomingCommunicationCallback() {
		return onIncomingCommunicationCallback != null;
	}

	public abstract boolean isAlive();

	public abstract void broadcast(MultiplayerCommunication communication) throws IOException;
	
	public void kill() throws IOException {
		stopIncomingCommunicationCallback();
		dropCommunications();
	}
}
