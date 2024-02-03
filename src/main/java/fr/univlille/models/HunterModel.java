package fr.univlille.models;

import java.util.ArrayList;
import java.util.List;

import fr.univlille.CellEvent;
import fr.univlille.Coordinate;
import fr.univlille.iutinfo.cam.player.perception.ICellEvent;
import fr.univlille.iutinfo.cam.player.perception.ICoordinate;
import fr.univlille.iutinfo.cam.player.perception.ICellEvent.CellInfo;
import fr.univlille.utils.Subject;

public class HunterModel extends Subject {
    private ArrayList<ICellEvent> shootsHistory;
    private GameModel gameModel;
    private int maxShoots;
    private int shootsLeft;
    private int grenadesLeft;
    private boolean grenadeMode;

    public boolean isGrenadeMode() {
        return grenadeMode;
    }

    public void setGrenadeMode(boolean grenadeMode) {
        this.grenadeMode = grenadeMode;
    }

    public int getShootsLeft() {
        return shootsLeft;
    }

    public void setShootsLeft(int shootsLeft) {
        this.shootsLeft = shootsLeft;
    }

    public int getGrenadesLeft() {
        return grenadesLeft;
    }

    public void setGrenadesLeft(int grenadesLeft) {
        this.grenadesLeft = grenadesLeft;
    }

    public List<ICellEvent> getShootsHistory() {
        return shootsHistory;
    }

    public int getMaxShoots() {
        return maxShoots;
    }

    public HunterModel(GameModel gameModel) {
        this.gameModel = gameModel;
        this.maxShoots = gameModel.getParameters().getHunterShoots();
        this.shootsLeft = this.maxShoots;
        this.grenadesLeft = gameModel.getParameters().getHunterGrenades();
        shootsHistory = new ArrayList<>();
    }

    /**
     * Sets the amount of shots left as the maximum amount allowed by the game parameters.
     * Use this function at the beginning of each turn.
     */
    public void turnBegin() {
        this.shootsLeft = maxShoots;
    }

    /**
     * Makes sure that the given hunter's target is valid.
     * The hunter cannot shoot outside of the maze.
     * @param shoot The target's position.
     * @return `true` if the target's position is valid, `false` otherwise.
     */
    public boolean isHunterShootValid(ICoordinate shoot) {
        ICoordinate mazeDimensions = gameModel.getMazeDimensions();
        return shoot.getCol() >= 0 && shoot.getCol() < mazeDimensions.getCol()
            && shoot.getRow() >= 0 && shoot.getRow() < mazeDimensions.getRow();
    }

    /**
     * Checks if the hunter has at least one shot left.
     * @return `true` if the hunter can shoot.
     */
    public boolean canShoot() {
        return getShootsLeft() > 0;
    }

    /**
     * Checks if the hunter has at least one grenade left.
     * @return `true` if the hunter can ue a grenade.
     */
    public boolean canUseGrenade() {
        return getGrenadesLeft() > 0;
    }

    /**
     * Checks if the turn of the hunter is valid.
     * The hunter selects his powerups and selects a cell to target.
     * If the powerup cannot be used, or if the targeted cell isn't valid,
     * then this function will return `false`.
     * Use this function to make sure that a turn is never completed with invalid game decisions.
     * @param shot The coordinates of the cell to shoot.
     * @return `true` if the hunter can complete his turn, `false` if he did something wrong and need to retry.
     */
    public boolean isTurnValid(ICoordinate shot) {
        return (isGrenadeMode() ? canUseGrenade() : canShoot()) && isHunterShootValid(shot);
    }

    /**
     * Gets information about the cell that the hunter is targeting.
     * @param shootPosition The coordinates of the hunter's target.
     * @return The type of cell that the hunter has shot.
     */
    public void shoot(ICoordinate shootPosition) {
        CellInfo state = CellInfo.EMPTY;
        if (gameModel.isWallAt(shootPosition)) {
            state = CellInfo.WALL;
        }

        // remove other shoots history with the same position
        for (int i = shootsHistory.size() - 1; i >= 0; i--) {
            if (shootsHistory.get(i).getCoord().equals(shootPosition)) {
                shootsHistory.remove(i);
            }
        }
        
        // l'historique de déplacement du monstre
        for (int i = gameModel.getHistory().size() - 1; i >= 0; i--) {
            ICellEvent cellEvent = gameModel.getHistory().get(i);
            if (cellEvent.getCoord().equals(shootPosition)) {
                shootsHistory.add(cellEvent);
                notifyObservers(cellEvent);
                return;
            }
        }
        CellEvent cellEvent = new CellEvent(shootPosition, state, gameModel.getTurn());

        shootsHistory.add(cellEvent);
        notifyObservers(cellEvent);
    }

    /**
     * Checks if the hunter can shoot,and he if can
     * then it shoots at the given coordinates.
     * @param coordinates The coordinates of the target.
     * @return `true` if the hunter successfully shot the target, or `false` if the hunter isn't allowed to shoot.
     */
    public boolean playHunterMove(ICoordinate coordinates) {
        if (!canShoot()) {
            return false;
        }
        shoot(coordinates);
        setShootsLeft(getShootsLeft() - 1);
        return true;
    }

    /**
     * Checks if the hunter is allowed to use a grenade,
     * and if he is, then it throws a grenade,
     * and remove one from the hunter's inventory.
     * @param coordinates The coordinates where the grenade should explode.
     * @return `true` if the grenade was successfully triggered, or `false` if the hunter cannot throw a grenade.
     */
    public boolean playHunterGrenade(ICoordinate coordinates) {
        if (!canUseGrenade()) {
            return false;
        }
        grenade(coordinates);
        setGrenadesLeft(getGrenadesLeft() - 1);
        return true;
    }

    /**
     * Simulates the explosion of a grenade following this pattern:
     * ```
     *  +
     * +++
     *  +
     * ```
     * The `shoot` function is used for each mark of this pattern.
     * @param grenadePosition
     */
    private void grenade(ICoordinate grenadePosition) {
        shoot(grenadePosition);
        ICoordinate t = new Coordinate(grenadePosition.getCol() + 1, grenadePosition.getRow()); // top
        ICoordinate r = new Coordinate(grenadePosition.getCol(), grenadePosition.getRow() + 1); // right
        ICoordinate b = new Coordinate(grenadePosition.getCol() - 1, grenadePosition.getRow()); // bottom
        ICoordinate l = new Coordinate(grenadePosition.getCol(), grenadePosition.getRow() - 1); // left
        if (isHunterShootValid(t)) {
            shoot(t);
        }
        if (isHunterShootValid(b)) {
            shoot(b);
        }
        if (isHunterShootValid(r)) {
            shoot(r);
        }
        if (isHunterShootValid(l)) {
            shoot(l);
        }
    }
}
