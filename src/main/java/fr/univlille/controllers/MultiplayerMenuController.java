package fr.univlille.controllers;

import java.io.IOException;

import fr.univlille.App;
import fr.univlille.GameMode;
import fr.univlille.multiplayer.MultiplayerUtils;
import fr.univlille.multiplayer.Server;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;

public class MultiplayerMenuController extends AnchorPane {
    private App app;

    public App getApp() {
        return app;
    }

    public void setApp(App app) {
        this.app = app;
    }

    @FXML
    private Button boutonHeberger;

    @FXML
    private Button boutonRejoindre;

    @FXML
    public void backButtonPressed() throws IOException {
        app = App.getApp();
        app.changeScene("menu");
    }

    @FXML
    public void localButtonPressed() throws IOException {
        app = App.getApp();
        app.showParameters(GameMode.TWO_PLAYERS);
    }

    @FXML
    public void hostButtonPressed() throws IOException {
        // We make sure that the lobby cannot be displayed
        // if the creation of the server fails.
        boolean success = false;
        try {
            if (!Server.getInstance().isAlive()) {
                Server.getInstance().host(App.getDefaultMultiplayerPort());
                System.out.println("Server is running at '" + MultiplayerUtils.getHostname() + "'");
            }
            success = true;
        } catch (IOException e) {
			System.err.println("Exception caught when trying to initialize the server on port " + App.getDefaultMultiplayerPort());
			System.err.println(e.getMessage());
		}
        if (success) {
            App.getApp().changeScene("lobby");
        }
    }

    @FXML
    public void joinButtonPressed() throws IOException {
        app = App.getApp();
        app.changeScene("recherche");
    }

    @FXML
    public void lanButtonPressed() {
        boutonHeberger.setVisible(!boutonHeberger.isVisible());
        boutonRejoindre.setVisible(!boutonRejoindre.isVisible());
    }
}
