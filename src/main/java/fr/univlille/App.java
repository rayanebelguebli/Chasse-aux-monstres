package fr.univlille;

import java.io.IOException;

import fr.univlille.multiplayer.MultiplayerCommand;
import fr.univlille.multiplayer.MultiplayerCommunication;
import fr.univlille.multiplayer.MultiplayerUtils;
import fr.univlille.multiplayer.Server;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {
    private static final int DEFAULT_MULTIPLAYER_PORT = 6666;
    private static Scene scene;
    private static App app;

    private GameParameters parameters;

    public static int getDefaultMultiplayerPort() {
        return DEFAULT_MULTIPLAYER_PORT;
    }

    public static App getApp() {
        if (app == null) {
            app = new App();
        }
        return app;
    }

    public GameParameters getGameParameters() {
        return parameters;
    }

    public void overwriteGameParameters(GameParameters params) {
        this.parameters = params;
    }

    public void resetGameParameters() {
        this.parameters = new GameParameters();
    }

    private static Parent loadFXML(String filename) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("controllers/" + filename + ".fxml"));

        if (fxmlLoader.getLocation() == null) {
            System.err.println("Le chemin du fichier FXML est invalide!");
            System.exit(1);
        }

        return fxmlLoader.load();
    }

    @Override
    public void start(Stage stage) throws IOException {
        Parent parent = loadFXML("menu");
        App.scene = new Scene(parent, 1000, 1000);
        stage.setScene(scene);
        stage.setTitle("Chasse au monstre");
        stage.show();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        // making sure the server and the client are properly closed when exiting the app
        MultiplayerUtils.getMultiplayerInstance().kill();
    }

    public void showParameters(GameMode gameMode) throws IOException {
        parameters = new GameParameters();
        parameters.setGameMode(gameMode);
        changeScene("settings");
    }

    public void changeScene(String name) throws IOException {
        scene.setRoot(loadFXML(name));
    }

    public void startGame(GameParameters parameters) throws IOException {
        if (Server.getInstance().hasClient()) {
            Server.getInstance().broadcast(
                new MultiplayerCommunication(
                    MultiplayerCommand.CREATING_GAME,
                    parameters.toString()
                )
            );
        }
        this.parameters = parameters;
        scene.setRoot(loadFXML("game"));
    }

    public static void main(String[] args) {
        launch(args);
    }
}