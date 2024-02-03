package fr.univlille;

import fr.univlille.iutinfo.cam.player.perception.ICoordinate;

/**
 * Defines coordinates and allows us to calculate distances between two positions.
 * As it implements "ICoordinate",
 * to get the X coordinate, use "getCol()",
 * and to get the Y coordinate, use "getRow()".
 */
public class Coordinate implements ICoordinate {
    private int x;
    private int y;

    public Coordinate(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Coordinate(double x, double y) {
        this.x = (int) x;
        this.y = (int) y;
    }

    public Coordinate(int[] coordinates) {
        this.x = coordinates[0];
        this.y = coordinates[1];
    }

    public void setCol(int x) {
        this.x = x;
    }

    public void setRow(int y) {
        this.y = y;
    }

    @Override
    public int getCol() {
        return this.x;
    }

    @Override
    public int getRow() {
        return this.y;
    }

    public static double distance(ICoordinate c1, ICoordinate c2) {
        return Math.sqrt(Math.pow((c1.getCol() - c2.getCol()), 2) + Math.pow((c1.getRow() - c2.getRow()), 2));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        Coordinate other = (Coordinate) obj;
        return other.getCol() == getCol() && other.getRow() == getRow();
    }

    @Override
    public String toString() {
        return "(" + getCol() + ", " + getRow() + ")";
    }

    @Override
    public ICoordinate clone() {
        return new Coordinate(x, y);
    }
}
