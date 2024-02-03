package fr.univlille;

import fr.univlille.iutinfo.cam.player.perception.ICellEvent;
import fr.univlille.iutinfo.cam.player.perception.ICoordinate;

public class CellEvent implements ICellEvent {
    private ICoordinate coord;
    private CellInfo state;
    private int turn;

    public CellEvent(ICoordinate coord, CellInfo state, int turn) {
        this.coord = coord;
        this.state = state;
        this.turn = turn;
    }

    @Override
    public ICoordinate getCoord() {
        return coord;
    }

    @Override
    public CellInfo getState() {
        return state;
    }

    @Override
    public int getTurn() {
        return turn;
    }

    @Override
    public String toString() {
        return "<" + getCoord() + ", " + getState() + ", " + getTurn() + ">";
    }
}
