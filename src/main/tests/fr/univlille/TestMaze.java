package fr.univlille;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Random;

import org.junit.Before;

public class TestMaze {

    Maze maze;

    @Before
    public void initialize() {
        maze = new Maze(9, 9);
    }


    @Test
    public void testIsValidCell() {
        assertTrue(maze.isValidCell(1, 4));
        assertTrue(maze.isValidCell(4, 2));
        assertFalse(maze.isValidCell(-4, -2));
        assertFalse(maze.isValidCell(18, 24));
    }

    @Test
    public void testCreateMaze() {
        maze.createMaze(1.0, new Random(0));
        assertTrue(maze.getMaze()[1][1]);
        assertTrue(maze.getMaze()[5][3]);
        assertTrue(maze.getMaze()[3][1]);
        assertFalse(maze.getMaze()[0][4]);
    }
}
