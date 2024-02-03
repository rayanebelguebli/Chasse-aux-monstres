package fr.univlille.controllers;

import java.io.IOException;

import fr.univlille.App;
import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;

public class CreditsController extends AnchorPane {
    private App app;

    public App getApp() {
        return app;
    }

    public void setApp(App app) {
        this.app = app;
    }

    @FXML
    public void backButtonPressed() throws IOException {
        app = App.getApp();
        app.changeScene("menu");
    }
}
