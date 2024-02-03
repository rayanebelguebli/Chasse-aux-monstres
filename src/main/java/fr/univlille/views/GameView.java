package fr.univlille.views;

import fr.univlille.Theme;
import fr.univlille.controllers.GameController;
import fr.univlille.Coordinate;
import fr.univlille.iutinfo.cam.player.perception.ICellEvent;
import fr.univlille.iutinfo.cam.player.perception.ICoordinate;
import fr.univlille.iutinfo.cam.player.perception.ICellEvent.CellInfo;
import fr.univlille.models.GameModel;
import fr.univlille.utils.Observer;
import fr.univlille.utils.Subject;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

public class GameView extends Canvas implements Observer {
    /**
     * A tile is one image from the tileset (spritesheet).
     * Each tile is 32 pixels wide.
     */
    public static final int TILE_SIZE = 32;

    /**
     * Each image in the game is contained in a spritesheet.
     * A spritesheet is a set of fixed-size images, and each image is a
     * "decoration".
     * Each decoration has a unique index, just like an array.
     */
    public static Image spritesheet = new Image(GameView.class.getResourceAsStream("/images/spritesheet.png"));

    /**
     * A reference to the instance of "Game" containing the hunter and monster
     * models, as well as the maze itself.
     */
    private final GameModel model;

    /**
     * The context that allows us to draw stuff into.
     */
    private final GraphicsContext gc;

    /**
     * In a multiplayer game,
     * one must wait for the other to complete its turn,
     * and in the meantime, he should not be able to interact with the canvas.
     * This property is used only in `GameMode.ONLINE` (multiplayer).
     */
    private boolean disabledView = false;

    /**
     * Is the turn of the hunter?
     */
    private boolean hunterTurn;

    /**
     * The current position of the cursor
     * represented as an instance of `Coordinate`.
     */
    private Coordinate cursorPosition;

    private Coordinate movePosition;

    private GameController mainPage;
    private MonsterView monsterView;
    private HunterView hunterView;

    /**
     * Is the turn of the hunter?
     * In the case of a multiplayer game, the logic is completely different.
     * That's why this method must be used over the `hunterTurn` property.
     * 
     * Multiplayer-only logic:
     * If the host, or the client, are playing as the hunter, then it will ALWAYS be his turn,
     * but he will not be able to play as long as the monster didn't complete his turn.
     * The hunter cannot play as long as he didn't receive a communication from the other.
     * @return `true` if it's the turn of the hunter, or if it's a multiplayer game and that the player is the hunter.
     */
    public boolean isHunterTurn() {
        if (model.isMultiplayer()) {
            return model.isMultiplayerBodyPlayingHunter();
        }
        return hunterTurn;
    }

    public void setHunterTurn(boolean hunterTurn) {
        this.hunterTurn = hunterTurn;
    }

    public void setMainPage(GameController mainPage) {
        this.mainPage = mainPage;
    }

    public MonsterView getMonsterView() {
        return monsterView;
    }

    /**
     * Disables any interaction with the game's canvas.
     */
    public void disableCanvas() {
        this.disabledView = true;
    }

    /**
     * Allows the player to interact with the game's canvas.
     */
    public void enableCanvas() {
        this.disabledView = false;
    }

    /**
     * Is the user allowed to interact with the game's canvas?
     * The user may not be allowed to do that in a multiplayer game,
     * when the other is playing his turn.
     * @return `true` if any interaction with the canvas is currently forbidden.
     */
    public boolean isCanvasDisabled() {
        return this.disabledView;
    }

    public GameView(GameModel model) {
        this.model = model;

        this.gc = getGraphicsContext2D();

        hunterView = new HunterView(gc, this, model);
        monsterView = new MonsterView(gc, this, model);

        ICoordinate mazeDimensions = model.getMazeDimensions();
        setWidth((double) TILE_SIZE * mazeDimensions.getCol());
        setHeight((double) TILE_SIZE * mazeDimensions.getRow());

        cursorPosition = new Coordinate(0, 0);
        movePosition = new Coordinate(-1, -1);
        setOnMouseMoved(e -> {
            if (model.isGameEnded() || (isHunterTurn() && model.getHunter().getShootsLeft() <= 0
                    && model.getHunter().getGrenadesLeft() <= 0)) {
                return;
            }
            Coordinate relativeMousePosition = new Coordinate(
                    (int) (e.getSceneX() - getLayoutX() - (TILE_SIZE * 0.5)),
                    (int) (e.getSceneY() - getLayoutY() - (TILE_SIZE * 0.5)));
            cursorPosition = new Coordinate(
                    (double) relativeMousePosition.getCol() / TILE_SIZE,
                    (double) relativeMousePosition.getRow() / TILE_SIZE);
            draw();

        });

        setOnMousePressed(e -> {
            if (model.isGameEnded() || isCanvasDisabled()) {
                return;
            }
            if (isHunterTurn()) {
                handleMousePressedHunter();
            } else {
                handleMousePressedMonster();
            }
            draw();
        });

        // on attache la vue au hunter
        model.getHunter().attach(this);
    }

    private void handleMousePressedHunter() {
        if (model.getHunter().isTurnValid(cursorPosition)) {
            // If two players are playing on the same computer,
            // then they will have to use the button dedicated to end the turn.
            if (model.isSplitScreen()) {
                play();
            } else {
                // Otherwise, end the turn immediately 
                mainPage.playTurn();
            }
        }
    }

    /**
     * Handles the logic of the monster's move when the player clicks on the cell.
     * If the movement isn't valid, then nothing happens.
     */
    private void handleMousePressedMonster() {
        if (model.getMonster().isTurnValid(cursorPosition)) {
            movePosition = cursorPosition;
            if (model.isSplitScreen()) {
                // The player has to confirm the end of turn
                // so as to give time for the other player to prepare his turn
                // on the same computer.
                play();
            } else {
                // ends the turn immediately
                mainPage.playTurn();
            }
        }
    }

    public ICoordinate getCursorPosition() {
        return cursorPosition;
    }

    public void setCursorPosition(ICoordinate cursorPosition) {
        this.cursorPosition = (Coordinate) cursorPosition;
    }

    public ICoordinate getMovePosition() {
        return movePosition;
    }

    public void setMovePosition(ICoordinate movePosition) {
        this.movePosition = (Coordinate) movePosition;
    }

    /**
     * Cette fonction affiche sur le Canvas les informations nécessaires. Elle est
     * appellée à chaque mouvement de souris ou à chaque action.
     */
    public void draw() {
        if (isHunterTurn()) {
            hunterView.draw();
        } else {
            monsterView.draw();
        }
    }

    public void play() {
        if (isHunterTurn()) {
            if (model.getHunter().isGrenadeMode()) {
                model.getHunter().playHunterGrenade(cursorPosition);
            } else {
                model.getHunter().playHunterMove(cursorPosition);
            }
        } else {
            model.getMonster().play(movePosition);
        }
        cursorPosition = new Coordinate(-1, -1);
        movePosition = new Coordinate(-1, -1);
    }

    /**
     * Sets the selected theme and re-draws the UI accordingly.
     * If the theme isn't valid, nothing happens.
     * 
     * @param theme The theme to be applied to the game.
     */
    public void setTheme(Theme theme) {
        switch (theme) {
            case DEFAULT:
                spritesheet = new Image(getClass().getResourceAsStream("/images/spritesheet.png"));
                break;
            case HALLOWEEN:
                spritesheet = new Image(getClass().getResourceAsStream("/images/spritesheet_halloween.png"));
                break;
            default:
                return;
        }
        draw();
    }

    @Override
    public void update(Subject subj) {
        ICellEvent cellEvent = (ICellEvent) subj;
        updateHunterErrorLabel(cellEvent);
    }

    @Override
    public void update(Subject subj, Object data) {
        ICellEvent cellEvent = (ICellEvent) data;
        updateHunterErrorLabel(cellEvent);
    }   

    /**
     * Updates the error label of the hunter after a shot.
     * In a multiplayer game, it will get called even though the user is the monster.
     * It's because the move of the distant player has to be replicated on the other's instance.
     * If it's not the hunter's turn, then this function won't do a thing.
     */
    private void updateHunterErrorLabel(ICellEvent cellEvent) {
        if (isHunterTurn()) {
            if (cellEvent.getState() == CellInfo.WALL) {
                mainPage.errorLabel.setText("Vous avez touché un arbre.");
            } else if (cellEvent.getState() == CellInfo.MONSTER) {
                monsterCell(cellEvent);
            } else {
                mainPage.errorLabel.setText("Vous n'avez rien touché...");
            }
            mainPage.updateEntitiesLabel();
            draw();
        }
    }

    private void monsterCell(ICellEvent cellEvent) {
        if (cellEvent.getTurn() == model.getTurn()) { // Si le monstre est sur cette case à ce tour-ci
            model.setGameEnded(true);
        } else {
            mainPage.errorLabel.setText("Le monstre est passé ici au tour n° " + cellEvent.getTurn() + ".");
        }
    }
}
