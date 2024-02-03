package fr.univlille;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Maze {
    private int tailleX;
    private int tailleY;
    private boolean[][] mazeTile;

    public Maze(int tailleX, int tailleY) {
        this.tailleX = tailleX;
        this.tailleY = tailleY;
        this.mazeTile = new boolean[tailleY][tailleX];
        initializeMaze();
    }

    private void initializeMaze() {
        for (boolean[] row : mazeTile) {
            Arrays.fill(row, true);
        }
    }

    public boolean[][] createMaze(double threshold, Random random) {
        recursiveBacktrack(0, 0, random);

        for (int y = 0; y < mazeTile.length; y++) {
            for (int x = 0; x < mazeTile[0].length; x++) {
                if (random.nextDouble() > threshold) {
                    mazeTile[y][x] = false;
                }
            }
        }
        return mazeTile;
    }

    private void recursiveBacktrack(int currentX, int currentY, Random random) {
        List<int[]> directions = Arrays.asList(new int[] { 0, -2 }, new int[] { 0, 2 }, new int[] { -2, 0 }, new int[] { 2, 0 });
        Collections.shuffle(directions, random);

        for (int[] direction : directions) {
            int newX = currentX + direction[0];
            int newY = currentY + direction[1];

            if (isValidCell(newX, newY) && mazeTile[newY][newX]) {
                mazeTile[currentY + direction[1] / 2][currentX + direction[0] / 2] = false; // Ouvre le mur
                mazeTile[newY][newX] = false; // Marque la nouvelle case comme visitée
                recursiveBacktrack(newX, newY, random); // Appel récursif pour la nouvelle case
            }
        }
    }

    public boolean isValidCell(int x, int y) {
        return x >= 0 && x < tailleX && y >= 0 && y < tailleY;
    }

    public boolean[][] getMaze() {
        return mazeTile;
    }

    public Coordinate getDimensions() {
        return new Coordinate(tailleX, tailleY);
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("mazeTile " + tailleX + ", " + tailleY + "\n");
        for (int y = 0; y < tailleY; y++) {
            for (int x = 0; x < tailleX; x++) {
                if (mazeTile[y][x]) {
                    str.append(' ');
                } else {
                    str.append('#');
                }
            }
            str.append('\n');
        }
        return str.toString();
    }
}
