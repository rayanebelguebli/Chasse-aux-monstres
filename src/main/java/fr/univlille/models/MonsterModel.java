package fr.univlille.models;

import fr.univlille.CellEvent;
import fr.univlille.Coordinate;
import fr.univlille.iutinfo.cam.player.perception.ICellEvent.CellInfo;
import fr.univlille.utils.Subject;
import fr.univlille.iutinfo.cam.player.perception.ICoordinate;

public class MonsterModel extends Subject {
    private Coordinate position;
    private GameModel model;
    private int superJumpLeft = 1;
    private boolean superJump = false;
    private boolean[][] fogOfWar;

    public MonsterModel(GameModel model, ICoordinate startPosition) {
        this.model = model;
        position = (Coordinate) startPosition;
    }

    public boolean[][] getFogOfWar() {
        return fogOfWar;
    }

    public void setFogOfWar(boolean[][] fog) {
        this.fogOfWar = fog;
    }

    public Coordinate getPosition() {
        return position;
    }

    public ICoordinate move(ICoordinate movement) {
        position.setCol(movement.getCol());
        position.setRow(movement.getRow());
        return position;
    }

    public void play(ICoordinate movePosition) {
        if (shouldUseSuperJump()) {
            superJumpLeft -= 1;
        }
        changePosition(movePosition);
    }

    private void changePosition(ICoordinate movePosition) {
        model.incrementTurn();
        move(movePosition);
        model.addToHistory(new CellEvent(((Coordinate)movePosition).clone(), CellInfo.MONSTER, model.getTurn()));
    }

    /**
     * Checks if the monster is using a super jump
     * and if it has enough super jumps left.
     * @return `true` if the monster will use the super jump on the next movement.
     */
    public boolean shouldUseSuperJump() {
        return isUsingSuperJump() && hasEnoughSuperJumpsLeft();
    }

    public void setSuperJump(boolean useSuperJump) {
        this.superJump = useSuperJump;
    }

    public void toggleSuperJump() {
        this.superJump = !this.superJump;
    }

    public boolean isUsingSuperJump() {
        return superJump;
    }

    public boolean hasEnoughSuperJumpsLeft() {
        return superJumpLeft > 0;
    }

    public int getSuperJumpsLeft() {
        return superJumpLeft;
    }

    /**
     * Checks if the monster is making a valid turn.
     * If he wants to use a super jump, then we check if it's possible,
     * otherwise we just check if the movement is valid.
     * Use this function to make sure that a turn is never completed with invalid game decisions.
     * @param target The cell the monster wants to go to.
     * @return `true` if the monster can complete his turn, `false` if he has to retry.
     */
    public boolean isTurnValid(ICoordinate target) {
        return isUsingSuperJump() ? superJumpLeft > 0 : isMonsterMovementValid(target);
    }

    /**
     * Makes sure that the given movement is valid for the monster.
     * The monster cannot move outside of the maze.
     * It cannot move into a wall.
     * It cannot jump cells.
     * 
     * @param movement The desired movement of the monster.
     * @return `true` if the movement is valid, `false` otherwise.
     */
    public boolean isMonsterMovementValid(ICoordinate movement) {
        double max = shouldUseSuperJump() ? 2.0 : 1.0;
        ICoordinate mazeDimensions = model.getMazeDimensions();

        // On vérifie déjà si le déplacement est dans la grille du jeu
        if (movement.getCol() < 0 || movement.getCol() > mazeDimensions.getCol() || movement.getRow() < 0
                || movement.getRow() > mazeDimensions.getRow()) {
            return false;
        }

        double distance = Coordinate.distance(model.getMonster().getPosition(), movement);
        if (distance > max || distance < max) {
            return false;
        }

        // Et si le déplacement n'est pas dans un obstacle
        if (model.isWallAt(movement.getCol(), movement.getRow())) {
            return false;
        }
        return true;
    }

}
