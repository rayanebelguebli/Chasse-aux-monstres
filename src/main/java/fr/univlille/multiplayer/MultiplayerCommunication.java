package fr.univlille.multiplayer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MultiplayerCommunication {
  private static final Pattern pattern = Pattern.compile("command=(\\d+);?(.*)");

  private MultiplayerCommand command;
  private String[] parameters;

  /**
   * Reads an incoming communication.
   * @param message The message that a socket transmitted.
   * @throws InvalidCommunicationException If the message format isn't valid.
   */
  public MultiplayerCommunication(String message) throws InvalidCommunicationException {
    try {
      final Matcher matcher = pattern.matcher(message);
      matcher.matches();
      command = MultiplayerCommand.values()[Integer.parseInt(matcher.group(1))];
      if (matcher.group(2).length() >= 1) {
        parameters = matcher.group(2).split(";");
        if (parameters.length == 0) {
          throw new InvalidCommunicationException(message, "Empty argument was given.");
        }
      }
    } catch (IllegalStateException e) {
      throw new InvalidCommunicationException(message, "The format isn't right.");
    } catch (IndexOutOfBoundsException e) {
      throw new InvalidCommunicationException(message, "The given command doesn't exist.");
    }
  }

  /**
   * Creates a message meant to be sent via a socket.
   * @param command The command describing the communication's nature.
   * @param parameters The parameters of this command, as a string, separated by a semi-colon.
   */
  public MultiplayerCommunication(MultiplayerCommand command, String parameters) {
    this.command = command;
    this.parameters = parameters.split(";");
  }

  /**
   * Creates a message meant to be sent via a socket, but without parameters.
   * @param command The command describing the communication's nature.
   */
  public MultiplayerCommunication(MultiplayerCommand command) {
    this.command = command;
  }

  /**
   * Gets the type of command of this multiplayer communication.
   * @return The command.
   */
  public MultiplayerCommand getCommand() { return command; }

  /**
   * Checks if the command of this communication is the same as the given one.
   * @param c The command to try.
   * @return `true` if the given command is the same as the command of this communication.
   */
  public boolean isCommand(MultiplayerCommand c) {
    return this.command == c;
  }

  /**
   * The parameters of this command.
   * @return The parameters of this command, or NULL if there is none.
   */
  public String[] getParameters() { return parameters; }

  /**
   * Gets the parameter at the given index.
   * @param index The index of the requested parameter.
   * @return The requested parameter as a string, or `null` if the index isn't valid.
   */
  public String getParameter(int index) {
    if (index < 0 || index >= parameters.length) {
      return null;
    }
    return parameters[index];
  }

  /**
   * Checks if this command has parameters.
   * @return `true` if this command has parameters, `false` otherwise.
   */
  public boolean hasParameters() { return parameters != null; }

  /**
   * Transforms this communication into a string
   * that another instance of `MultiplayerCommunication` can parse.
   * 
   * Create your own communication and send it via a socket by using this method.
   */
  @Override
  public String toString() {
    if (hasParameters()) {
      return "command=" + command.ordinal() + ";" + String.join(";", parameters);
    } else {
      return "command=" + command.ordinal();
    }
  }
}
