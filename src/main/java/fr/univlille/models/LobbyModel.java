package fr.univlille.models;

public class LobbyModel {
  private static final boolean DEFAULT_HOST_ROLE = true;
  private boolean hostIsHunter = DEFAULT_HOST_ROLE;

  public void invertRoles() {
    hostIsHunter = !hostIsHunter;
  }

  public void setIsHostHunter(boolean v) {
    hostIsHunter = v;
  }

  public boolean isHostHunter() {
    return hostIsHunter;
  }

  /**
   * Checks if the role of the hunter has changed from the default value.
   * @return `true` if the host changed the roles.
   */
  public boolean hasChanged() {
    return hostIsHunter != DEFAULT_HOST_ROLE;
  }
}
