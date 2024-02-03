package fr.univlille.controllers;

import java.io.IOException;

import fr.univlille.App;
import fr.univlille.GameMode;
import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;

public class MenuController extends AnchorPane {
    private App app;

    public App getApp() {
        return app;
    }

    public void setApp(App app) {
        this.app = app;
    }

    @FXML
    public void playButtonPressed() throws IOException {
        app = App.getApp();
        app.showParameters(GameMode.BOT);
    }

    @FXML
    public void multiButtonPressed() throws IOException {
        app = App.getApp();
        app.changeScene("multiMenu");
    }
    
    @FXML
    public void creditsButtonPressed() throws IOException {
        app = App.getApp();
        app.changeScene("credits");
    }
    
    @FXML
    public void reglesButtonPressed() throws IOException {
        app = App.getApp();
        app.changeScene("regles");
    }
}
