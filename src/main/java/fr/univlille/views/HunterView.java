package fr.univlille.views;

import fr.univlille.Coordinate;
import fr.univlille.iutinfo.cam.player.perception.ICellEvent;
import fr.univlille.iutinfo.cam.player.perception.ICoordinate;
import fr.univlille.iutinfo.cam.player.perception.ICellEvent.CellInfo;
import fr.univlille.models.GameModel;
import fr.univlille.models.HunterModel;
import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

public class HunterView {

    private GraphicsContext gc;
    public GameModel gameModel;
    public GameView gameView;
    public HunterModel model;

    public HunterView(GraphicsContext gc, GameView gameView, GameModel gameModel) {
        this.gc = gc;
        this.gameView = gameView;
        this.gameModel = gameModel;
        this.model = gameModel.getHunter();
    }
    
    public void draw() {
        graphicStyle();
        drawBoard();
        
        if (!model.isHunterShootValid(gameView.getCursorPosition())) {
            ViewUtils.drawSimpleTexture(gc, new Coordinate(128, 256), gameView.getCursorPosition()); // Position souris
                                                                                                     // (si mouvement
                                                                                                     // impossible)
        } else {
            ViewUtils.drawSimpleTexture(gc, new Coordinate(0, 256), gameView.getCursorPosition()); // Position souris
                                                                                                   // (si mouvement
                                                                                                   // possible)
        }
        ViewUtils.drawSimpleTexture(gc, new Coordinate(64, 256), gameView.getMovePosition()); // Le mouvement
    }

    private void drawCheckboard() {
        ICoordinate dimensions = gameModel.getMazeDimensions();
        gc.drawImage(
            GameView.spritesheet, 192, 192, 64, 64, 0, 0,
            (double) dimensions.getCol() * GameView.TILE_SIZE,
            (double) dimensions.getRow() * GameView.TILE_SIZE
        );
        for (int x = 0; x < dimensions.getCol(); x++) {
            for (int y = 0; y < dimensions.getRow(); y++) {
                if (x % 2 == 0 && y % 2 == 0 || x % 2 == 1 && y % 2 == 1) {
                    ViewUtils.drawSimpleTexture(gc, 192, 128, x, y);
                }
            }
        }
    }

    private void drawBoard() {
        drawCheckboard();
        for (ICellEvent cellEvent : gameModel.getHunter().getShootsHistory()) {
            Coordinate coord = (Coordinate) cellEvent.getCoord();
            if (cellEvent.getState() == CellInfo.WALL) {
                ViewUtils.drawSimpleTexture(gc, 0, 64, coord.getCol(), coord.getRow());
            }
            if (cellEvent.getState() == CellInfo.EMPTY) {
                ViewUtils.drawSimpleTexture(gc, 192, 256, coord.getCol(), coord.getRow());
            }
            if (cellEvent.getState() == CellInfo.MONSTER) {
                if (cellEvent.getTurn() == gameModel.getTurn()) { // Si le monstre est actuellement sur la case (en gros
                                                                  // il est mort)
                    ViewUtils.drawSimpleTexture(gc, 64, 0, coord.getCol(), coord.getRow()); // Tombe
                } else {
                    if (cellEvent.getState() == CellInfo.MONSTER) {
                        gc.fillText(String.valueOf(cellEvent.getTurn()),
                                coord.getCol() * GameView.TILE_SIZE + GameView.TILE_SIZE / 2.0,
                                coord.getRow() * GameView.TILE_SIZE + GameView.TILE_SIZE / 2.0);
                    }
                }
            }
        }
    }

    private void graphicStyle() {
        gc.setFill(Color.BLACK);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.setFont(new Font("Comic Sans MS", 16));
    }

    public boolean playMove() {
        if (model.getShootsLeft() <= 0) {
            return false;
        } else {
            model.shoot(gameView.getCursorPosition());
        }
        return true;
    }

}
