package fr.univlille.controllers;

import java.io.IOException;

import fr.univlille.App;
import fr.univlille.GameMode;
import fr.univlille.GameParameters;
import fr.univlille.multiplayer.MultiplayerUtils;
import fr.univlille.multiplayer.Server;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;

public class SettingsController {

    @FXML
    public CheckBox fogOfWarCheckBox;
    @FXML
    public CheckBox predefCheckBox;

    @FXML
    public Spinner<Integer> fogOfWarSpinner;

    @FXML
    public Spinner<Integer> hunterShootsSpinner;

    @FXML
    public CheckBox playerRoleCheckBox;

    @FXML
    public Spinner<Integer> hunterGrenadesSpinner;

    @FXML
    public Spinner<Integer> mazeSizeXSpinner;

    @FXML
    public Spinner<Integer> mazeSizeYSpinner;

    @FXML
    public Spinner<Integer> wallPercentageSpinner;


    @FXML
    public Label gameModeLabel;

    private void bindFactory(Spinner<Integer> spinner, int min, int max, int defaultValue) {
        SpinnerValueFactory<Integer> factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(min, max);
        factory.setValue(defaultValue);
        spinner.setValueFactory(factory);
    }

    @FXML
    public void initialize() {
        bindFactory(hunterShootsSpinner, 1, 10, 1);
        bindFactory(hunterGrenadesSpinner, 0, 10, 1);
        bindFactory(mazeSizeXSpinner, 5, 19, 7);
        bindFactory(mazeSizeYSpinner, 5, 19, 7);
        bindFactory(wallPercentageSpinner, 50, 100, 100);
        bindFactory(fogOfWarSpinner, 1, 10, 1);
        GameMode gameMode = App.getApp().getGameParameters().getGameMode();
        playerRoleCheckBox.setVisible(gameMode == GameMode.BOT);
        switch (gameMode) {
            case BOT:
                gameModeLabel.setText("Jouer contre un robot");
                break;
            case ONLINE:
                gameModeLabel.setText("Jouer en ligne");
                break;
            case TWO_PLAYERS:
                gameModeLabel.setText("Jouer à deux joueurs sur une même machine");
                break;
        }
    }

    @FXML
    public void startGamePressed() throws IOException {
        // We make sure that the lobby is no longer
        // handling any incoming communication
        if (Server.getInstance().isAlive()) {
            Server.getInstance().stopIncomingCommunicationCallback();
        }
        
        GameParameters parameters = App.getApp().getGameParameters();
        parameters.setMazeWidth(mazeSizeXSpinner.getValue());
        parameters.setMazeHeight(mazeSizeYSpinner.getValue());
        parameters.setHunterShoots(hunterShootsSpinner.getValue());
        parameters.setHunterGrenades(hunterGrenadesSpinner.getValue());

        // fog of war
        parameters.setFogOfWar(fogOfWarCheckBox.isSelected());
        parameters.setFogOfWarRadius(fogOfWarSpinner.getValue());

        parameters.setWallsPercentage(wallPercentageSpinner.getValue() / 100.0);

        parameters.setAiPlayerIsHunter(playerRoleCheckBox.isSelected());

        parameters.setPredefinedMaze(predefCheckBox.isSelected());



        App.getApp().startGame(parameters);
    }

    @FXML
    public void cancelPressed() throws IOException {
        if (MultiplayerUtils.hasMultiplayerInstance()) {
            App.getApp().changeScene("lobby");
        } else {
            App.getApp().changeScene("menu");
        }
    }

    @FXML
    public void fogChecked() {
        fogOfWarSpinner.setVisible(!fogOfWarSpinner.isVisible());
    }
}
