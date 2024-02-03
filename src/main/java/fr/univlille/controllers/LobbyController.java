package fr.univlille.controllers;

import java.io.IOException;

import fr.univlille.App;
import fr.univlille.GameMode;
import fr.univlille.GameParameters;
import fr.univlille.InvalidGameDataException;
import fr.univlille.models.LobbyModel;
import fr.univlille.multiplayer.Client;
import fr.univlille.multiplayer.MultiplayerCommand;
import fr.univlille.multiplayer.MultiplayerCommunication;
import fr.univlille.multiplayer.MultiplayerUtils;
import fr.univlille.multiplayer.Server;
import javafx.application.Platform;
import javafx.scene.layout.AnchorPane;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.fxml.FXML;

public class LobbyController extends AnchorPane {
    private LobbyModel model;

    @FXML
    private Label hostname_label;

    @FXML
    private Label client_name;

    @FXML
    private Label host_role;

    @FXML
    private Label client_role;

    @FXML
    private Button button_start_game;

    @FXML
    public void backButtonPressed() throws IOException {
        App app = App.getApp();
        app.changeScene("menu");
        try {
            MultiplayerUtils.getMultiplayerInstance().kill();
        } catch (IOException e) {
            System.err.println("Cannot kill the instance of multiplayer body : " + e.getMessage());
        }
    }

    @FXML
    public void startGame() throws IOException {
        // The host is the only able to start the game.
        // He cannot start it if there is no client.
        if (Server.getInstance().hasClient()) {
            Server server = Server.getInstance();
            App app = App.getApp();
            app.resetGameParameters();
            app.getGameParameters().setGameMode(GameMode.ONLINE); // will update the UI accordingly
            app.getGameParameters().setSeed(System.currentTimeMillis()); // will set a common seed shared with the client for maze generation
            app.changeScene("settings");
            server.setIsHunter(model.isHostHunter());
        } else {
            // If the client quit the app unexpectedly
            // then we make sure that his hostname is no longer displayed
            removeClientHostname();
        }
    }

    @FXML
    public void reverseRoles() {
        // Only the host can change the roles
        if (Server.getInstance().isAlive()) {
            model.invertRoles();
            applyModel();
            broadcastRoles();
        }
    }

    /**
     * Informs the clients about the selected roles.
     */
    private void broadcastRoles() {
        try {
            Server.getInstance().broadcast(
                new MultiplayerCommunication(
                    MultiplayerCommand.SET_GAME_ROLES,
                    model.isHostHunter() ? "1" : "0"
                )
            );
        } catch (IOException e) {
            System.err.println("Broadcast of inverted roles failed : " + e.getMessage());
        }
    }

    private void applyModel() {
        if (model.isHostHunter()) {
            host_role.setText("Chasseur");
            client_role.setText("Monstre");
        } else {
            host_role.setText("Monstre");
            client_role.setText("Chasseur");
        }
    }

    private void hideStartGameButton() {
        button_start_game.setDisable(true); 
        button_start_game.setVisible(false);
    }

    public void initialize() {
        // At this point of the program,
        // there is a multiplayer instance,
        // therefore `MultiplayerUtils.getMultiplayerInstance()` cannot be null.
        if (MultiplayerUtils.getMultiplayerInstance().hasIncomingCommunicationCallback()) {
            // the lobby has already been created before,
            // and the host has decided to come back to it.
            // It's possible when using the "cancel" button from the settings controller.
            // As a consequence, we need to make sure that the lobby model has the right value.
            // Note that in such scenario the client hasn't moved from the lobby,
            // so in this condition, the Client cannot be alive.
            this.model = new LobbyModel();
            this.model.setIsHostHunter(Server.getInstance().isHunter());
            hostname_label.setText(MultiplayerUtils.getHostname());
            client_name.setText(Server.getInstance().getSavedClientHostname());
            applyModel();
        } else {
            // The lobby has never been created before,
            // therefore we initialize everything.
            this.model = new LobbyModel();
            if (Client.getInstance().isAlive()) {
                hideStartGameButton(); // the client cannot launch the game
                handleClientSide();
            } else {
                handleServerSide();
                hostname_label.setText(MultiplayerUtils.getHostname());
            }
        }
    }

    private void handleClientSide() {
        Client client = Client.getInstance();

        // Will handle all messages from the server (in the lobby only)
        client.setIncomingCommunicationCallback(() ->
            Platform.runLater(() -> {
                MultiplayerCommunication message = Client.getInstance().pollCommunication();
                switch (message.getCommand()) {
                    case HOST:
                        hostname_label.setText(message.getParameter(0));
                        client_name.setText(MultiplayerUtils.getHostname());
                        break;
                    case SET_GAME_ROLES:
                        model.setIsHostHunter(Integer.parseInt(message.getParameter(0)) == 1);
                        applyModel();
                        break;
                    case CREATING_GAME:
                        try {
                            client.stopIncomingCommunicationCallback();
                            client.setIsHunter(!model.isHostHunter());

                            App.getApp().overwriteGameParameters(GameParameters.readParameters(message.getParameter(0)));
                            App.getApp().changeScene("game");
                        } catch (InvalidGameDataException e) {
                            System.err.println("The given game parameters are invalid: " + message);
                        } catch (IOException e) {
                            System.err.println("The client was unable to load the game page.");
                            e.printStackTrace();
                        }
                        break;
                    case SERVER_TERMINATION:
                        try {
                            Client.getInstance().kill();
                            App.getApp().changeScene("menu");
                        } catch (Exception e) {
                            System.out.println("Server termination led to error : " + e.getMessage());
                        }
                        break;
                    default:
                        System.out.println("incoming communication from server was ignored by client : " + message);
                        // ignored
                }
            })
        );
    }

    private void handleServerSide() {
        Server server = Server.getInstance();
        
        // The server listens to the client's arrival
        // in a non-blocking way for the main thread.
        // Indeed we don't know when he's going to arrive, or if he's going to arrive at all.
        server.setIncomingCommunicationCallback(() -> 
            // Because we cannot update the JavaFX UI outside of the main thread,
            // as it would cause synchronization issues,
            // we tell it to run it "later"
            // (so as soon as it can).
            Platform.runLater(() -> {
                MultiplayerCommunication announce = server.pollCommunication();
                System.out.println("Server handling communication : " + announce);
                switch (announce.getCommand()) {
                    case JOIN:
                        server.setClientHostname(announce.getParameter(0));
                        client_name.setText(server.getSavedClientHostname());
                        if (model.hasChanged()) {
                            broadcastRoles();
                        }
                        break;
                    case DISCONNECTION:
                        removeClientHostname();
                        break;
                    default:
                        System.out.println("incoming communication from server was ignored by server : " + announce);
                        // ignored
                }
            })
        );
    }

    private void removeClientHostname() {
        client_name.setText("???");
    }
}
