package fr.univlille;

public class InvalidGameDataException extends Exception {
  InvalidGameDataException(String details, Object context) {
    super("Les données du jeu n'ont pas pu être parsées car invalides" + (details.isBlank() ? "" : (": " + details)) + (context == null ? "" : ("\nContexte : " + context)));
  }

  InvalidGameDataException(String details) {
    this(details, null);
  }

  InvalidGameDataException() {
    this("");
  }
}
