package fr.univlille.multiplayer;

public class InvalidCommunicationException extends Exception {
  InvalidCommunicationException(String communication, String details) {
    super("The following communication isn't valid: '" + communication + "'. " + details);
  }

  InvalidCommunicationException(String communication) {
    this(communication, "");
  }
}
