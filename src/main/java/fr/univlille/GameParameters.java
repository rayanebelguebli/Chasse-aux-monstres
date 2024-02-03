package fr.univlille;

import java.util.Arrays;

public class GameParameters {
    /**
     * The delimiter used in `toString()`.
     * It CANNOT be the same as the one used in `MultiplayerCommunication` nor in the CSV file,
     * because it would create conflicts during the parsing.
     */
    private static final char STRING_DELIMITER = '&';

    /**
     * The seed to give to the random generators. 
     * In the case of a mutiplayer game,
     * the host would create the game, 
     * and create a unique seed along with it,
     * and share it with the other player,
     * so as to have the same maze on both sides.
     */
    private long seed = -1;

    private int mazeWidth;
    private int mazeHeight;

    private int hunterShoots;
    private int hunterGrenades;

    private double wallsPercentage;

    private boolean fogOfWar;
    private int fogOfWarRadius = 3;

    private GameMode gameMode;

    private boolean predefined;

    public GameMode getGameMode() {
        return gameMode;
    }

    public void setGameMode(GameMode gameMode) {
        this.gameMode = gameMode;
    }

    // AI
    private boolean aiPlayerIsHunter;

    public boolean isAiPlayerIsHunter() {
        return aiPlayerIsHunter;
    }

    public void setAiPlayerIsHunter(boolean aiPlayerIsHunter) {
        this.aiPlayerIsHunter = aiPlayerIsHunter;
    }

    // MULTIPLAYER

    public int getMazeWidth() {
        return mazeWidth;
    }

    public long getSeed() {
        return seed;
    }

    public boolean hasDefinedSeed() {
        return seed != -1;
    }

    public void setSeed(long s) {
        this.seed = s;
    }

    public void setMazeWidth(int mazeWidth) {
        this.mazeWidth = mazeWidth;
    }

    public int getMazeHeight() {
        return mazeHeight;
    }

    public void setMazeHeight(int mazeHeight) {
        this.mazeHeight = mazeHeight;
    }

    public int getHunterShoots() {
        return hunterShoots;
    }

    public void setHunterShoots(int hunterShoots) {
        this.hunterShoots = hunterShoots;
    }

    public int getHunterGrenades() {
        return hunterGrenades;
    }

    public void setHunterGrenades(int hunterGrenades) {
        this.hunterGrenades = hunterGrenades;
    }

    public double getWallsPercentage() {
        return wallsPercentage;
    }

    public void setWallsPercentage(double wallsPercentage) {
        this.wallsPercentage = wallsPercentage;
    }

    public boolean isFogOfWar() {
        return fogOfWar;
    }

    public void setFogOfWar(boolean fogOfWar) {
        this.fogOfWar = fogOfWar;
    }

    public int getFogOfWarRadius() {
        return fogOfWarRadius;
    }

    public void setFogOfWarRadius(int fogOfWarRadius) {
        this.fogOfWarRadius = fogOfWarRadius;
    }

    public void setPredefinedMaze(boolean predefined){
        this.predefined = predefined;
    }

    public boolean getPredefinedMaze(){
        return this.predefined;
    }

    /**
     * Gets the value of a string following this format: "name=value".
     * This function would return "value".
     * 
     * It is useful when parsing the result of a stringified instance of `GameParameters`.
     * @param parameter The parameter following this format: "name=value".
     * @return The value of a stringified game parameter.
     */
    private static String getStringParameterValue(String parameter) {
        return parameter.substring(parameter.indexOf('=') + 1);
    }

    /**
     * Gets the name of a string following this format: "name=value";
     * This function would return "name".
     * 
     * Same purpose as `getStringParameterValue()`.
     * @param parameter The parameter following this format: "name=value".
     * @return The value of a stringified game parameter.
     */
    private static String getStringParameterName(String parameter) {
        return parameter.substring(0, parameter.indexOf('='));
    }

    /**
     * Reads an instance of `GameParameters` that got stringified using the custom `toString()` method.
     * This is useful because the parameters of a game can be saved in a CSV file,
     * or sent via a socket when the host is launching a game.
     * @param input A stringified instance of `GameParameters`. 
     * @return A new object of type `GameParameters`.
     * @throws InvalidGameDataException If the input string isn't valid.
     */
    public static GameParameters readParameters(String input) throws InvalidGameDataException {
        GameParameters params = new GameParameters();
        String[] attributes = input.split(String.valueOf(STRING_DELIMITER));
        if (attributes.length == 0) {
            throw new InvalidGameDataException("Aucun paramètre n'a pu être lu dans '" + input + "'.");
        }
        System.out.println(Arrays.toString(attributes));
        for (String parameter : attributes) {
            String name = getStringParameterName(parameter);
            String plainValue = getStringParameterValue(parameter);
            switch (name) {
                case "seed":
                    try {
                        long seed = Long.parseLong(plainValue);
                        if (seed != -1) {
                            params.setSeed(seed);
                        }
                    } catch (NumberFormatException e) {
                        throw new InvalidGameDataException("La seed n'a pas pu être parsée : '" + parameter + "'.", input);
                    }
                    break;
                case "dimensions":
                    String[] dimensions = plainValue.split("x");
                    if (dimensions.length != 2) {
                        throw new InvalidGameDataException("Les dimensions du labyrinthe ne sont pas valides. Les dimensions données sont: '" + parameter + "'.", input);
                    }
                    try {
                        params.setMazeWidth(Integer.parseInt(dimensions[0]));
                        params.setMazeHeight(Integer.parseInt(dimensions[1]));
                    } catch (NumberFormatException e) {
                        throw new InvalidGameDataException("Impossible de parser les dimensions données. L'un des nombres donnés n'est pas valide.", input);
                    }
                    break;
                case "hunterStuff":
                    String[] stuff = plainValue.split("-");
                    if (stuff.length != 2) {
                        throw new InvalidGameDataException("L'inventaire du chasseur n'est pas valide. Il manque des informations.", input);
                    }
                    try {
                        params.setHunterShoots(Integer.parseInt(stuff[0]));
                        params.setHunterGrenades(Integer.parseInt(stuff[1]));
                    } catch (NumberFormatException e) {
                        throw new InvalidGameDataException("Impossible de parser l'inventaire. L'une des valeurs n'est pas un nombre: '" + parameter + "'.", input);
                    }
                    break;
                case "wallsPercentage":
                    try {
                        params.setWallsPercentage(Double.parseDouble(plainValue));
                    } catch (NumberFormatException e) {
                        throw new InvalidGameDataException("Impossible de parser le pourcentage de murs : '" + parameter + "'.", input);
                    }
                    break;
                case "includeFog":
                    params.setFogOfWar(plainValue.equals("1"));
                    break;
                case "fogRadius":
                    try {
                        params.setFogOfWarRadius(Integer.parseInt(plainValue));
                    } catch (NumberFormatException e) {
                        throw new InvalidGameDataException("Impossible de parser le rayon du brouillard : '" + parameter + "'.", input);
                    }
                    break;
                case "gameMode":
                    try {
                        params.setGameMode(GameMode.values()[Integer.parseInt(plainValue)]);
                    } catch (NumberFormatException e) {
                        throw new InvalidGameDataException("Impossible de parser l'ordinal du mode de jeu : '" + parameter + "'.", input);
                    } catch (IndexOutOfBoundsException e) {
                        throw new InvalidGameDataException("Le mode de jeu renseigné n'existe pas : '" + parameter + "'.", input);
                    }
                    break;
                default:
                    throw new InvalidGameDataException("Un des paramètres donné n'existe pas. Le paramètre '" + parameter + "' est inconnu.");
            }
        }
        return params;
    }

    /**
     * Generates a string that can be used to transfer
     * the parameters to the client in case of a multiplayer game.
     * An example would be: "seed=-1;dimensions=10x10;..."
     */
    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("seed=" + seed + STRING_DELIMITER);
        str.append("dimensions=" + mazeWidth + "x" + mazeHeight + STRING_DELIMITER);
        str.append("hunterStuff=" + hunterShoots + "-" + hunterGrenades + STRING_DELIMITER);
        str.append("wallsPercentage=" + wallsPercentage + STRING_DELIMITER);
        str.append("includeFog=" + (fogOfWar ? 1 : 0) + STRING_DELIMITER);
        str.append("fogRadius=" + fogOfWarRadius + STRING_DELIMITER);
        str.append("gameMode=" + gameMode.ordinal());
        return str.toString();
    }
}
