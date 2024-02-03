package fr.univlille;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;

import fr.univlille.multiplayer.InvalidCommunicationException;
import fr.univlille.multiplayer.MultiplayerCommand;
import fr.univlille.multiplayer.MultiplayerCommunication;

public class TestMultiplayerCommunication {
  @Test
  public void create_communication_without_parameters() {
    MultiplayerCommunication comm = new MultiplayerCommunication(MultiplayerCommand.HOST);
    assertTrue(comm.getCommand() == MultiplayerCommand.HOST);
    assertFalse(comm.hasParameters());
    assertNull(comm.getParameters());
  }

  @Test
  public void create_communication_with_parameter() {
    MultiplayerCommunication comm = new MultiplayerCommunication(MultiplayerCommand.HOST, "true;1");
    assertTrue(comm.hasParameters());
    assertEquals(2, comm.getParameters().length);
    assertEquals("true", comm.getParameter(0));
    assertEquals("1", comm.getParameter(1));
  }

  @Test
  public void read_communication_without_parameters() {
    assertDoesNotThrow(() -> new MultiplayerCommunication("command=0"));
    try {
      MultiplayerCommunication comm = new MultiplayerCommunication("command=0");
      assertTrue(comm.getCommand() == MultiplayerCommand.values()[0]);
      assertFalse(comm.hasParameters());
    } catch(InvalidCommunicationException ignore) {}
  }

  @Test
  public void read_communication_with_parameters() {
    assertDoesNotThrow(() -> new MultiplayerCommunication("command=0;JavaFXHater"));
    try {
      MultiplayerCommunication comm = new MultiplayerCommunication("command=0;JavaFXHater");
      assertTrue(comm.hasParameters());
      assertTrue(comm.getCommand() == MultiplayerCommand.values()[0]);
      assertEquals(1, comm.getParameters().length);
      assertEquals("JavaFXHater", comm.getParameter(0));
    } catch(InvalidCommunicationException ignore) {}
  }

  @Test
  public void invalid_communication() {
    assertThrows(InvalidCommunicationException.class, () -> new MultiplayerCommunication(""));
    assertThrows(InvalidCommunicationException.class, () -> new MultiplayerCommunication("command"));
    assertThrows(InvalidCommunicationException.class, () -> new MultiplayerCommunication("command="));
    assertThrows(InvalidCommunicationException.class, () -> new MultiplayerCommunication("command=-1"));
    assertThrows(InvalidCommunicationException.class, () -> new MultiplayerCommunication("command="));
    assertThrows(InvalidCommunicationException.class, () -> new MultiplayerCommunication("command=0;;"));
    assertThrows(InvalidCommunicationException.class, () -> new MultiplayerCommunication("command=0;;;;;;;"));
    assertThrows(InvalidCommunicationException.class, () -> new MultiplayerCommunication("command=" + MultiplayerCommand.values().length)); // this command doesn't exist
  }

  @Test
  public void communication_to_string() {
    try {
      assertEquals("command=1", new MultiplayerCommunication("command=1;").toString());
      assertEquals("command=1;1", new MultiplayerCommunication("command=1;1").toString());
      assertEquals("command=1;true", new MultiplayerCommunication("command=1;true").toString());
      assertEquals("command=2;name;yoyo;2", new MultiplayerCommunication("command=2;name;yoyo;2").toString());
    } catch (InvalidCommunicationException e) {
      assertEquals("", e.getMessage()); // so as to see the exception in the output
    }
  }
}