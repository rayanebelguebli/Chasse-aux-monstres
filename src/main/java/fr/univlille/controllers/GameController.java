package fr.univlille.controllers;

import java.io.IOException;

import fr.univlille.App;
import fr.univlille.CellEvent;
import fr.univlille.Coordinate;
import fr.univlille.HunterStrategy;
import fr.univlille.MonsterStrategy;
import fr.univlille.iutinfo.cam.player.perception.ICoordinate;
import fr.univlille.iutinfo.cam.player.perception.ICellEvent.CellInfo;
import fr.univlille.models.GameModel;
import fr.univlille.multiplayer.Client;
import fr.univlille.multiplayer.MultiplayerBody;
import fr.univlille.multiplayer.MultiplayerCommand;
import fr.univlille.multiplayer.MultiplayerCommunication;
import fr.univlille.multiplayer.MultiplayerUtils;
import fr.univlille.multiplayer.Server;
import fr.univlille.views.GameView;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

public class GameController {

    @FXML
    public ToggleButton powerupButton;

    @FXML
    public Label turnLabel;

    @FXML
    public Button endTurnButton;

    @FXML
    public Label powerupEnabledLabel;

    @FXML
    public Label shootLeftLabel;

    @FXML
    public VBox mainVBox;

    @FXML
    public AnchorPane switchPane;

    @FXML
    public Label switchPaneCountdown;

    @FXML
    public Label errorLabel;

    @FXML
    public AnchorPane gameOverScreen;

    @FXML
    public Label winnerLabel;

    private GameView gameView;
    private GameModel game;

    private MonsterStrategy monsterStrategy;
    private HunterStrategy hunterStrategy;

    @FXML
    public void initialize() {
        initGame();
    }

    @FXML
    public void playButtonPressed() {
        playTurn();
    }

    @FXML
    public void restartGamePressed() throws IOException {
        if (game.isMultiplayer()) {
            MultiplayerBody body = MultiplayerUtils.getMultiplayerInstance();
            body.broadcast(
                new MultiplayerCommunication(
                    MultiplayerCommand.GAME_RESTARTED
                )
            );
        }
        initGame();
    }

    @FXML
    public void menuButtonPressed() throws IOException {
        if (game.isMultiplayer()) {
            try {
                MultiplayerUtils.getMultiplayerInstance().kill();
            } catch (IOException e) {
                System.err.println("Could not kill the instance of multiplayer body : " + e.getMessage());
            }
        }
        leaveGame();
    }

    private void leaveGame() throws IOException {
        App.getApp().changeScene("menu");
    }

    /**
     * Cette méthode permet d'initialiser la partie. Elle est appellée à chaque
     * rédemarrage du jeu.
     */
    public void initGame() {
        gameOverScreen.setVisible(false);

        game = new GameModel();
        game.setParameters(App.getApp().getGameParameters());
        if(game.getParameters().getPredefinedMaze() == true){
            game.predefinedMaze("src/main/resources/maze/maze.csv");
        }
        else{
        game.generateMaze(game.getParameters());
        }

        if (gameView != null) {
            mainVBox.getChildren().remove(gameView);
        }
        gameView = new GameView(game);

        mainVBox.getChildren().add(2, gameView);
        gameView.draw();
        gameView.setMainPage(this);
        updateEntitiesLabel();

        // On ajoute la première position du monstre dans l'historique
        Coordinate monsterPosition = game.getMonster().getPosition();

        // Cloner les coordonnées du monstre est nécessaire
        // car l'historique doit contenir une deep copy de la position.
        game.addToHistory(new CellEvent(monsterPosition.clone(), CellInfo.MONSTER, game.getTurn()));

        if (game.isPlayerAgainstAI()) {
            // On crée la MonsterStrategy ou la HunterStrategy en fonction du rôle que le joueur a pris.
            if (game.getParameters().isAiPlayerIsHunter()) {
                monsterStrategy = new MonsterStrategy();
                monsterStrategy.initialize(game.getMaze());
                monsterStrategy.setMonsterVariables(game);
                playTurn(); // Comme c'est toujours le monstre qui commence, on le laisse d'abord jouer.
            } else {
                hunterStrategy = new HunterStrategy();
                hunterStrategy.initialize(game.getParameters().getMazeWidth(), game.getParameters().getMazeHeight());
            }
        }
        
        if (game.isMultiplayer()) {
            if (game.isMultiplayerBodyPlayingHunter()) {
                // the monster always start,
                // so if the player is the hunter disable the buttons,
                // client-side as well as server-side
                disableInteractions();
            }
            initMultiplayerListeners();
        }

        // The end turn button is useful 
        // ONLY when two players play on the same computer.
        if (!game.isSplitScreen()) {
            // We hide and disable it
            disableEndTurnButton();
        }
    }

    /**
     * Initializes the listeners for both server-side and client-side communications.
     */
    private void initMultiplayerListeners() {
        MultiplayerBody body = MultiplayerUtils.getMultiplayerInstance();
        body.setIncomingCommunicationCallback(() ->
            Platform.runLater(() -> {
                handleMultiplayerExchange(body);
            })
        );
    }

    /**
     * When one player is telling the other about the completion of his turn,
     * he sends information along with the communication.
     * This first parameter of this communication is coordinates.
     * This method reads these coordinates and creates an instance of `ICoordinate`.
     * @param communication The communication that was sent by the other after the completion of his turn.
     * @return The coordinates of the other's move.
     */
    private ICoordinate readCoordinatesFromMultiplayerCommunication(MultiplayerCommunication communication) {
        int[] coordinates = new int[2];
        String[] strCoordinates = communication.getParameter(0).split("-");
        coordinates[0] = Integer.parseInt(strCoordinates[0]);
        coordinates[1] = Integer.parseInt(strCoordinates[1]);
        return new Coordinate(coordinates);
    }

    /**
     * Server-side or client-side, it doesn't matter, in both cases the logic is the same.
     * Indeed, the host and the client both receive a communication from the other at the end of each turn,
     * and depending on their role, the game will update accordingly.
     * @param body The client's instance, or the server's instance.
     */
    private void handleMultiplayerExchange(MultiplayerBody body) {
        MultiplayerCommunication message = body.pollCommunication();
        if (message.isCommand(MultiplayerCommand.HUNTER_PLAYED) || message.isCommand(MultiplayerCommand.MONSTER_PLAYED)) {
            // In both cases, the first parameter is the position of the other:
            // If the hunter is playing, then it receives the coordinates of the monster,
            // and vice-versa.
            ICoordinate coordinates = readCoordinatesFromMultiplayerCommunication(message);
            boolean usedPowerup = Boolean.parseBoolean(message.getParameter(1));
            if (body.isHunter()) {
                // the given coordinates are those of the monster
                game.getMonster().setSuperJump(usedPowerup);
                game.getMonster().play(coordinates);
            } else {
                // The player is the monster and
                // the given coordinates are those of the hunter's shot
                if (usedPowerup) {
                    game.getHunter().playHunterGrenade(coordinates);
                } else {
                    game.getHunter().playHunterMove(coordinates);
                }
            }
            updateEntitiesLabel();
            // if we are receiving word that the other has played his turn
            // then it means its our turn, therefore we enable the buttons.
            // When playing, the buttons get disabled,
            // so we know that the other's buttons are disabled.
            enableInteractions();
        } else if (message.isCommand(MultiplayerCommand.GAME_RESTARTED)) {
            // The other asked to restart the game,
            // the game parameters are the same,
            // so just init a new game:
            initGame();
        } else if (message.isCommand(MultiplayerCommand.GAME_ENDED)) {
            // The other player won the game,
            // so the role that won the game is the opposite of the current one.
            if (body.isHunter()) {
                winnerLabel.setText("Le monstre a gagné!");
            } else {
                winnerLabel.setText("Le chasseur a gagné!");
            }
            gameOverScreen.setVisible(true);
        } else {
            // Handling the "expected" disconnection of the other player from the game
            try {
                if (message.isCommand(MultiplayerCommand.SERVER_TERMINATION)) {
                    // Meaning that the body is the client,
                    // and the host has left the game.
                    leaveGame();
                    Client.getInstance().kill();
                } else if (message.isCommand(MultiplayerCommand.DISCONNECTION)) {
                    // Meaning the body is the host,
                    // and the client has left the game.
                    leaveGame();
                    Server.getInstance().kill();
                }
            } catch (IOException ignore) { }
        }
    }

    /**
     * Cette méthode permet de créer un Thread qui attends automatiquement le nombre
     * de millisecondes données en paramètre, puis éxecute le code de l'argument
     * continuation.
     * 
     * @param millis       Le nombre de millisecondes à attendre
     * @param continuation Le code à éxecuter à la fin du delay.
     */
    public static void delay(long millis, Runnable continuation) {
        Task<Void> sleeper = new Task<>() {
            @Override
            protected Void call() throws Exception {
                Thread.sleep(millis);
                return null;
            }
        };
        sleeper.setOnSucceeded(event -> continuation.run());
        new Thread(sleeper).start();
    }

    private boolean isBotTurn() {
        return game.isPlayerAgainstAI()
                && (gameView.isHunterTurn() && !game.getParameters().isAiPlayerIsHunter()
                        || !gameView.isHunterTurn() && game.getParameters().isAiPlayerIsHunter());
    }

    public void playTurn() {
        if (isBotTurn()) {
            if (game.getParameters().isAiPlayerIsHunter()) {
                monsterStrategy.update(new CellEvent(game.getMonster().getPosition(), CellInfo.MONSTER, game.getTurn()));
                gameView.setMovePosition(monsterStrategy.play()); // on fait jouer le monstre
            } else {
                while (game.getHunter().getShootsLeft() > 0) {
                    gameView.setCursorPosition(hunterStrategy.play()); // on fait jouer le chasseur
                    gameView.play();
                }
            }
        }

        // `GameMode.TWO_PLAYERS` is the only game mode that doesn't end the turn as soon as the player decided his move.
        // Therefore, in this game mode, `play` was already called, and the player used the "end turn" button to call this function.
        // Use `play()` only when it's NOT split screen mode.
        if (!game.isSplitScreen()) {
            // For some reason, "movePosition" is used only with the monster,
            // however "cursorPosition" must be used for the hunter's shot.
            // We need to save it first because it gets reset within `gameView.play()`
            ICoordinate targetPosition = ((Coordinate)(gameView.isHunterTurn() ? gameView.getCursorPosition() : gameView.getMovePosition())).clone();
            gameView.play();
            if (game.isMultiplayer()) {
                broadcastEndOfTurn(targetPosition);
            }
        }

        if (game.monsterWon()) {
            game.setGameEnded(true);
        }

        if (game.isGameEnded()) {
            if (game.monsterWon()) {
                winnerLabel.setText("Le monstre a gagné!");
            } else {
                winnerLabel.setText("Le chasseur a gagné!");
            }
            gameOverScreen.setVisible(true);
        }

        if (game.isSplitScreen()) {
            swapScreen();
        }

        // On échange les tours
        // et on update les labels
        // ainsi que le canvas
        swapTurn();
        updateEntitiesLabel();
        gameView.draw();

        if (isBotTurn()) {
            playTurn();
        }

        if (game.isMultiplayer()) {
            disableInteractions();
        }
    }

    /**
     * In the case of a multiplayer game, at the end of each turn, 
     * the player must inform the other about the completion of his turn,
     * and transmit the data necessary to the duplication of the move
     * on the other's instance.
     */
    private void broadcastEndOfTurn(ICoordinate targetPosition) {
        try {
            String targetCoordinates = targetPosition.getCol() + "-" + targetPosition.getRow();
            MultiplayerBody body = MultiplayerUtils.getMultiplayerInstance();
            if (Server.getInstance().isAlive() && !Server.getInstance().hasClient()) {
                // If the server is alive,
                // but there is no client,
                // then end the game immediately.
                // It happens if the client quits the java program unexpectedly.
                try {
                    leaveGame();
                    Server.getInstance().kill();
                    return;
                } catch (IOException ignore) { }
            }
            if (body.isHunter()) {
                System.out.println("The body is the hunter, sending HUNTER_PLAYED");
                body.broadcast(
                    new MultiplayerCommunication(
                        MultiplayerCommand.HUNTER_PLAYED,
                        targetCoordinates + ";" + game.getHunter().isGrenadeMode()
                    )
                );
            } else {
                System.out.println("The body is the hunter, sending MONSTER_PLAYED");
                body.broadcast(
                    new MultiplayerCommunication(
                        MultiplayerCommand.MONSTER_PLAYED,
                        targetCoordinates + ";" + game.getMonster().isUsingSuperJump()
                    )
                );
            }
        } catch (IOException e) {
            System.err.println("Caught an IOException when trying to send HUNTER_PLAYED or MONSTER_PLAYED:");
            e.printStackTrace();
        }
    }

    @FXML
    public void powerupButtonPressed() {
        if (gameView.isHunterTurn()) {
            if (game.getHunter().getGrenadesLeft() > 0) {
                boolean grenadeMode = game.getHunter().isGrenadeMode();
                powerupEnabledLabel.setVisible(grenadeMode); // TODO: check if this should be "!grenableMode" instead
                game.getHunter().setGrenadeMode(!grenadeMode);
            } else {
                errorLabel.setText("Vous n'avez plu de grenade...");
            }
        } else {
            // Toggling powerup, if possible
            if (game.getMonster().hasEnoughSuperJumpsLeft()) {
                powerupEnabledLabel.setVisible(!game.getMonster().isUsingSuperJump());
                game.getMonster().toggleSuperJump();
            } else {
                errorLabel.setText("Vous n'avez plu de SuperJump...");
            }
        }
    }

    /**
     * Two players are playing on the same game instance (the same computer).
     * A dark screen will appear for 3 seconds.
     */
    private void swapScreen() {
        switchPane.setVisible(true);
        switchPaneCountdown.setText("Dans 3...");
        delay(1000, () -> switchPaneCountdown.setText("Dans 2.."));
        delay(2000, () -> switchPaneCountdown.setText("Dans 1."));
        delay(3000, () -> switchPane.setVisible(false));
    }

    /**
     * Swaps the turn (if it's the turn of the hunter, then it's now the turn of the monster, or vice-versa).
     * The powerups are reset.
     * The fog is reset within the monster view.
     */
    private void swapTurn() {
        // `hunterTurn` property should not be changed in a multiplayer game
        if (!game.isMultiplayer()) {
            gameView.setHunterTurn(!gameView.isHunterTurn());
        }

        if (gameView.isHunterTurn()) {
            game.getHunter().turnBegin();
            game.getHunter().setGrenadeMode(false);
        } else {
            game.getMonster().setSuperJump(false);
            gameView.getMonsterView().turnStarted();
        }
    }

    @FXML
    public void saveButtonPressed() throws IOException {
        App.getApp().changeScene("save");
    }

    public void updateEntitiesLabel() {
        turnLabel.setText("Tour n°" + game.getTurn());
        // Do not invert the condition,
        // keep in mind that if it's a multiplayer game,
        // then, if the current multiplayer body is the hunter,
        // the below condition will always evaluate to true.
        if (gameView.isHunterTurn()) {
            showHunterLabels();
        } else {
            showMonsterLabels();
        }
    }

    /**
     * Handles the labels to display or hide when the hunter is playing.
     */
    private void showHunterLabels() {
        powerupButton.setDisable(!game.getHunter().canUseGrenade());
        boolean grenade = game.getHunter().isGrenadeMode();
        if (game.getHunter().getShootsLeft() == 1) {
            shootLeftLabel.setText("Il vous reste " + game.getHunter().getShootsLeft() + " tir!");
        } else {
            shootLeftLabel.setText("Il vous reste " + game.getHunter().getShootsLeft() + " tirs!");
        }
        
        powerupButton.setText("Grenade (" + game.getHunter().getGrenadesLeft() + ")");
        powerupEnabledLabel.setVisible(grenade);
        
        if (!grenade) {
            powerupButton.setSelected(false);
        }

        shootLeftLabel.setVisible(true);
    }

    /**
     * Handles the labels to display or hide when the monster is playing.
     */
    private void showMonsterLabels() {
        powerupButton.setDisable(!game.getMonster().hasEnoughSuperJumpsLeft());
        boolean superjump = game.getMonster().isUsingSuperJump();
        shootLeftLabel.setVisible(false);
        powerupButton.setText("SuperJump (" + game.getMonster().getSuperJumpsLeft() + ")");
        powerupEnabledLabel.setVisible(superjump);
        
        if(!superjump) {
            powerupButton.setSelected(false);
        }
    }

    /**
     * In multiplayer mode, the view is never changed,
     * and one player must WAIT for the other to complete his turn,
     * therefore the awaiting player should not be able to click on any button.
     */
    private void disableInteractions() {
        powerupButton.setDisable(true);
        gameView.disableCanvas();
    }

    /**
     * Same as `disableInteractions()` but this time the player is allowed to play.
     */
    private void enableInteractions() {
        powerupButton.setDisable(false);
        gameView.enableCanvas();
    }

    /**
     * In the case of a multiplayer game or against an AI,
     * the end turn button will always be disabled.
     */
    private void disableEndTurnButton() {
        endTurnButton.setDisable(true);
        endTurnButton.setVisible(false);
    }
}
