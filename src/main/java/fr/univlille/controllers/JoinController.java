package fr.univlille.controllers;

import java.io.IOException;
import java.net.UnknownHostException;

import fr.univlille.App;
import fr.univlille.multiplayer.Client;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;

public class JoinController extends AnchorPane {
    @FXML
    public TextArea textarea;

    @FXML
    public void backButtonPressed() throws IOException {
        App.getApp().changeScene("menu");
    }

    @FXML
    public void onConfirm() throws IOException {
        boolean success = false;
        try {
            String hostname = textarea.getText().trim();
            System.out.println("Trying to join " + hostname);
            Client.getInstance().connect(hostname, App.getDefaultMultiplayerPort());
            System.out.println("joined");
            success = true;
        } catch (UnknownHostException e) {
            System.err.println("Cannot join host " + e.getMessage());
        } catch (IOException e) {
            System.err.println("Oh no " + e.getMessage());
        }
        // The IOException of `changeScene` should be propagated
        if (success) {
            App.getApp().changeScene("lobby");
        }
    }
}
