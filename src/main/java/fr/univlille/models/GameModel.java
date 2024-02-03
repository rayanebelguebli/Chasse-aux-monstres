package fr.univlille.models;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import fr.univlille.Coordinate;
import fr.univlille.GameMode;
import fr.univlille.GameParameters;
import fr.univlille.Maze;
import fr.univlille.iutinfo.cam.player.perception.ICellEvent;
import fr.univlille.iutinfo.cam.player.perception.ICoordinate;
import fr.univlille.multiplayer.Client;
import fr.univlille.multiplayer.MultiplayerBody;
import fr.univlille.multiplayer.MultiplayerCommand;
import fr.univlille.multiplayer.MultiplayerCommunication;
import fr.univlille.multiplayer.MultiplayerUtils;
import fr.univlille.multiplayer.Server;
import fr.univlille.utils.Subject;

public class GameModel extends Subject {
    /**
     * Shared random generator using the seed of the game parameters.
     * This instance must be used for any random generation to ensure
     * that the client and the host are playing on the same maze.
     */
    private Random random;

    /**
     * The current turn of the game.
     * Initialized at 1 when creating the maze.
     */
    private int turn;
    /**
     * 1 represents a wall,
     * 0 represents an empty cell.
     * There is no need for other types of cells as they're contained in other
     * variables or in "history".
     */
    private boolean[][] maze;

    /**
     * The coordinates of the exit.
     */
    private ICoordinate exit;

    private HunterModel hunter;
    private MonsterModel monster;

    private GameParameters parameters;

    public GameParameters getParameters() {
        return parameters;
    }

    public void setParameters(GameParameters parameters) {
        this.parameters = parameters;
        if (parameters.getSeed() == -1) {
            this.random = new Random();
        } else {
            this.random = new Random(parameters.getSeed());
        }
    }

    public boolean[][] getMaze() {
        return maze;
    }

    /**
     * A list containing all the moves of the hunter and the monster.
     * As it stores instances of `ICellEvent` it remembers at which turn one
     * particular move was done,
     * so it's thanks to this variable that we can know at which turn the monster
     * was on a particular cell.
     */
    private ArrayList<ICellEvent> history = new ArrayList<>();

    /**
     * A boolean that stores whether or not the game has finished.
     */
    private boolean gameEnded;

    public int getHeight() {
        return maze.length;
    }

    public int getWidth() {
        return maze[0].length;
    }

    public HunterModel getHunter() {
        return hunter;
    }

    public MonsterModel getMonster() {
        return monster;
    }

    public boolean isGameEnded() {
        return gameEnded;
    }

    public void setGameEnded(boolean gameEnded) {
        if (gameEnded && isMultiplayer()) {
            try {
                MultiplayerBody body = MultiplayerUtils.getMultiplayerInstance();
                body.broadcast(
                    new MultiplayerCommunication(
                        MultiplayerCommand.GAME_ENDED
                    )
                );
            } catch (IOException ignore) { }
        }
        this.gameEnded = gameEnded;
    }

    public int getTurn() {
        return turn;
    }

    public void incrementTurn() {
        this.turn += 1;
    }

    /**
     * Checks if a particular cell is a wall or empty.
     * 
     * @param x The X coordinate of the given cell.
     * @param y The Y coordinate of the given cell.
     * @return `true` if this cell is a wall, `false` if it's empty.
     */
    public boolean isWallAt(int x, int y) {
        if(x < 0 || x >= getWidth() || y < 0 || y >= getHeight()) { // si en dehors du labyrinthe
            return true;
        }
        return maze[y][x];
    }

    /**
     * Checks if a particular cell is a wall or empty.
     * 
     * @param coordinate The coordinates of the given cell.
     * @return `true` if this cell is a wall, `false` if it's empty.
     */
    public boolean isWallAt(ICoordinate coordinate) {
        return isWallAt(coordinate.getRow(), coordinate.getCol());
    }

    /**
     * Checks if the position of the monster matches the position of the exit.
     * 
     * @return `true` if the monster has reached the exit, `false` otherwise.
     */
    public boolean monsterWon() {
        return monster.getPosition().equals(exit);
    }

    /**
     * Gets the width and height of the maze as an instance of `Coordinate`.
     * 
     * @return An instance of `Coordinate` where `x` is the width of the maze and
     *         `y` the height.
     */
    public ICoordinate getMazeDimensions() {
        return new Coordinate(getWidth(), getHeight());
    }

    /**
     * Gets the position of the exit.
     * 
     * @return The exact coordinates of the exit.
     */
    public ICoordinate getExit() {
        return exit;
    }

    /**
     * Gets a random position within the maze.
     * For now, it gives a random position that is not a wall.
     * 
     * @return A random position in the maze.
     */
    public ICoordinate randomPosition() {
        ArrayList<Coordinate> availableCoordinates = new ArrayList<>();
        for (int y = 0; y < getHeight(); y++) {
            for (int x = 0; x < getWidth(); x++) {
                if (!isWallAt(x, y)) {
                    availableCoordinates.add(new Coordinate(x, y));
                }
            }
        }

        return availableCoordinates.get(random.nextInt(availableCoordinates.size()));
    }
    
    /**
     * Fait comme la méthode ci-dessous, mais permet en plus
     * de définir une cible et une distance minimale
     * le script s'assurera que la position n'est pas dans la zone de la cible.
     * 
     * @return A random position in the maze.
     */
    public ICoordinate randomPosition(ICoordinate target, int minDist) {
        ArrayList<Coordinate> availableCoordinates = new ArrayList<>();
        for (int y = 0; y < getHeight(); y++) {
            for (int x = 0; x < getWidth(); x++) {
                if (!isWallAt(x, y) && Coordinate.distance(new Coordinate(x, y), target) >= minDist) {
                    availableCoordinates.add(new Coordinate(x, y));
                }
            }
        }

        return availableCoordinates.get(random.nextInt(availableCoordinates.size()));
    }

    /**
     * Generates the maze.
     * It initializes the hunter and monster models.
     * It gives a random position to the monster.
     * The turns start at 1 and the history is cleared.
     * The exit is also randomized.
     */
    public void generateMaze(GameParameters parameters) {
        this.parameters = parameters;
        Maze laby = new Maze(parameters.getMazeWidth(), parameters.getMazeHeight());
        maze = laby.createMaze(parameters.getWallsPercentage(), this.random);

        this.hunter = new HunterModel(this);
        ICoordinate monsterPosition = randomPosition();
        this.monster = new MonsterModel(this, monsterPosition);

        this.turn = 1;
        this.history.clear();

        
        setRandomExitPosition(this.monster.getPosition());
    }
    
    private void setRandomExitPosition(ICoordinate monsterCoord) {
        int minDist = (getWidth() + getHeight()) / 3;
        exit = randomPosition(monsterCoord, minDist);
        
        int iteration = 0;
        while (iteration < 100 || exit == null) {
            exit = randomPosition(monsterCoord, minDist);
            iteration++;
        }
        if(iteration == 100) {
            exit = randomPosition();
        }
    }

    public void predefinedMaze(String path) {
        try{
            Scanner scanner = new Scanner(new File(path));
            int lignes = 0;
            int colonnes = 0;

            while(scanner.hasNextLine()) {
                lignes ++;
                String ligne = scanner.nextLine();
                String[] valeurs = ligne.split(",");
                colonnes = valeurs.length;
            }
            scanner = new Scanner(new File(path));
            maze = new boolean[lignes][colonnes];

            for(int i = 0; i< lignes; i++){
                String ligne = scanner.nextLine();
                String[] valeurs = ligne.split(","); 

                for(int j = 0; j<colonnes; j++){
                    if(Integer.parseInt(valeurs[j]) == 2){
                        this.monster = new MonsterModel(this, new Coordinate(i, j));
                        maze[i][j] = false;
                    }
                    else if(Integer.parseInt(valeurs[j]) == 3){
                        this.exit =  new Coordinate(i, j);
                        maze[i][j] = false;
                    }
                    else if(Integer.parseInt(valeurs[j]) == 0){
                        maze[i][j] = true;
                    }
                    else{
                        maze[i][j] = false;
                    }
                }
            }
            scanner.close();
        }catch(FileNotFoundException e){
            e.printStackTrace();
        }


        this.hunter = new HunterModel(this);
        

        this.turn = 1;
        this.history.clear();
    }

    public List<ICellEvent> getHistory() {
        return history;
    }

    public void addToHistory(ICellEvent cellEvent) {
        history.add(cellEvent);
    }

    /**
     * Checks whether or not the player is playing against a BOT.
     * @return `true` if the game mode is `GameMode.BOT`.
     */
    public boolean isPlayerAgainstAI() {
        return parameters.getGameMode() == GameMode.BOT;
    }

    /**
     * Is the game mode multiplayer?
     * @return `true` if the player is playing with someone else located on another computer.
     */
    public boolean isMultiplayer() {
        return parameters.getGameMode() == GameMode.ONLINE;
    }

    /**
     * Is the game being played on the same instance?
     * Two players can play on the same computer, on the same instance,
     * and a dark screen will appear at the end of each turn,
     * so that one doesn't see the other's move.
     * @return `true` if the game mode is `TWO_PLAYERS`.
     */
    public boolean isSplitScreen() {
        return parameters.getGameMode() == GameMode.TWO_PLAYERS;
    }

    /**
     * Considering it's a multiplayer game,
     * is the host of the game the hunter?
     * @return `true` if the host is the hunter.
     */
    private boolean isMultiplayerHostHunter() {
        return Server.getInstance().isAlive() && Server.getInstance().isHunter();
    }

    /**
     * Considering it's a multiplayer game,
     * is the client (so the player not hosting the game) the hunter?
     * @return `true` if the client is the hunter.
     */
    private boolean isMultiplayerClientHunter() {
        return Client.getInstance().isAlive() && Client.getInstance().isHunter();
    }

    /**
     * Considering it's a multiplayer game,
     * is the client, or the server, the hunter?
     * @return `true` if the current multiplayer body is the hunter.
     */
    public boolean isMultiplayerBodyPlayingHunter() {
        return isMultiplayerClientHunter() || isMultiplayerHostHunter();
    }
}