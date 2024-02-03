package fr.univlille;

import java.util.ArrayList;

import fr.univlille.iutinfo.cam.player.monster.IMonsterStrategy;
import fr.univlille.iutinfo.cam.player.perception.ICellEvent;
import fr.univlille.iutinfo.cam.player.perception.ICoordinate;
import fr.univlille.models.GameModel;

public class MonsterStrategy implements IMonsterStrategy {
 
    private boolean[][] maze;

    private int mazeWidth;
    private int mazeHeight;

    private int monsterX;
    private int monsterY;

    private int exitX;
    private int exitY;

    static final int INFINITY = Integer.MAX_VALUE;

    public boolean containsFalse(boolean[][] array){
        for (int i = 0; i<array.length; i++){
            for (int j = 0; j<array[0].length; j++){
                if (!array[i][j]) return true;
            }
        }
        return false;
    }

    @Override
    public ICoordinate play() {
        int[][] distances = new int[mazeHeight][mazeWidth];
        boolean[][] visited = new boolean[mazeHeight][mazeWidth];
        ICoordinate[][] visitedFrom = new ICoordinate[mazeHeight][mazeWidth];

        for (int i = 0; i<mazeHeight; i++){
            for (int j = 0; j<mazeWidth; j++){
                distances[i][j] = INFINITY;
            }
        }
        
        for (int i = 0; i<mazeHeight; i++){
            for (int j = 0; j<mazeWidth; j++){
                if (isWallAt(j,i)) visited[i][j] = true;
            }
        }

        distances[monsterY][monsterX] = 0;
        visitedFrom[monsterY][monsterX] = new Coordinate(monsterX,monsterY);

        while (containsFalse(visited)){ //on mettra les murs comme visited aussi, tant que tout n'a pas été visité on continue
            int minDistance = INFINITY;
            int currentX = 0;
            int currentY = 0;
            for (int i = 0; i<mazeHeight; i++){
                for (int j = 0; j<mazeWidth; j++){
                    if (distances[i][j] < minDistance && !visited[i][j]) { //la prochaine cellule qu'on utilisera sera celle avec la distance minimale mais qui n'a pas encore été visitée
                        minDistance = distances[i][j]; 
                        currentY = i;
                        currentX = j;
                    }
                }
            }

            visited[currentY][currentX] = true;

            //en haut
            if (isInBounds(currentX,currentY-1)){
                if (!isWallAt(currentX,currentY-1)){
                    if (distances[currentY-1][currentX] > minDistance+1){
                        distances[currentY-1][currentX] = minDistance+1;
                        visitedFrom[currentY-1][currentX] = new Coordinate(currentX,currentY);
                    }
                }
            }

            //à droite
            if (isInBounds(currentX+1,currentY)){
                if (!isWallAt(currentX+1,currentY)){
                    if (distances[currentY][currentX+1] > minDistance+1){
                        distances[currentY][currentX+1] = minDistance+1;
                        visitedFrom[currentY][currentX+1] = new Coordinate(currentX,currentY);
                    }
                }
            }

            //en bas
            if (isInBounds(currentX,currentY+1)){
                if (!isWallAt(currentX,currentY+1)){
                    if (distances[currentY+1][currentX] > minDistance+1){
                        distances[currentY+1][currentX] = minDistance+1;
                        visitedFrom[currentY+1][currentX] = new Coordinate(currentX,currentY);
                    }
                }
            }

            //à gauche
            if (isInBounds(currentX-1,currentY)){
                if (!isWallAt(currentX-1,currentY)){
                    if (distances[currentY][currentX-1] > minDistance+1){
                        distances[currentY][currentX-1] = minDistance+1;
                        visitedFrom[currentY][currentX-1] = new Coordinate(currentX,currentY);
                    }
                }    
            }
        }
        int x = exitX;
        int y = exitY;
        ArrayList<ICoordinate> listeChemin = new ArrayList<>();
        listeChemin.add(new Coordinate(exitX,exitY));
        while (x != monsterX || y != monsterY){
            int newX = visitedFrom[y][x].getCol();
            int newY = visitedFrom[y][x].getRow();
            if (isDiagonal(x, y, newX, newY)) {
                listeChemin.add(new Coordinate(x, newY)); // going vertically first
            }
            x = newX;
            y = newY;
            listeChemin.add(new Coordinate(x,y));
        }
        return listeChemin.get(listeChemin.size()-2);
    }

    public boolean isDiagonal(int x, int y, int newX, int newY) {
        return Math.abs(x - newX) == 1 && Math.abs(y - newY) == 1;
    }

    public boolean isInBounds(int x, int y){
        if (x<0 || y<0 || x>=mazeWidth || y>=mazeHeight) return false;
        return true;
    }

    public boolean isWallAt(int x, int y) {
        if(x < 0 || x >= mazeWidth || y < 0 || y >= mazeHeight) { // si en dehors du labyrinthe
            return true;
        }
        return maze[y][x];
    }


    public void setMonsterVariables(GameModel game) {
        this.exitX = game.getExit().getCol();
        this.exitY = game.getExit().getRow();
        this.monsterX = game.getMonster().getPosition().getCol();
        this.monsterY = game.getMonster().getPosition().getRow();
    }

    @Override
    public void update(ICellEvent arg0) {
        this.monsterX = arg0.getCoord().getCol();
        this.monsterY = arg0.getCoord().getRow();
    }

    @Override
    public void initialize(boolean[][] arg0) {
        this.maze = arg0;
        this.mazeWidth = maze[0].length;
        this.mazeHeight = maze.length;
    }

}