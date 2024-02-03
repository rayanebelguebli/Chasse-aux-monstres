package fr.univlille.multiplayer;

public enum MultiplayerCommand {
  HOST, // the host created the game and is sending its name to its players ("command=0;string")
  JOIN, // a client joined a game and is sending its name to the server ("command=1;string")
  DISCONNECTION, // the client left the lobby ("command=2")
  SERVER_TERMINATION, // the host left the lobby
  SET_GAME_ROLES, // the players are in the lobby, the host switched the roles (hunter/monster) ("command=..;X"), X being 1 or 0, the value of LobbyModel.host_is_hunter
  CREATING_GAME, // the host started the game and is now transmitting the seed used to generate any random event
  MONSTER_PLAYED, // the monster has played and is transmitting information about his play to the client's instance (the position of the monster: "command=..;x-y;boolean" for position (x;y) and a boolean to say if a super jump was used)
  HUNTER_PLAYED, // the hunter has played and is transmitting information about his play to the host's instance (the position of the hunter's shot: "command=..;x-y" for position (x;y) and a boolean to say if a grenade was used)
  GAME_RESTARTED, // the client, or the server, decided to restart the game. The game parameters stay the same.
  GAME_ENDED, // one player won, and must inform the other, no additional information is needed along with this command
}
