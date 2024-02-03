package fr.univlille.views;

import fr.univlille.iutinfo.cam.player.perception.ICoordinate;
import javafx.scene.canvas.GraphicsContext;

public class ViewUtils {

    /**
     * Cette méthode permet d'afficher une texture de 64x64 du Sprite Sheet dans le
     * Canvas du jeu.
     * 
     * @param gc                  GraphicsContext
     * @param spritesheetPosition La position X et Y dans la Sprite Sheet, pour
     *                            réference voir les images dans resources/images.
     * @param gamePosition        La position dans la fenêtre du jeu.
     */
    public static void drawSimpleTexture(GraphicsContext gc, ICoordinate spritesheetPosition,
            ICoordinate gamePosition) {
        gc.drawImage(
                GameView.spritesheet, spritesheetPosition.getCol(), spritesheetPosition.getRow(), 64, 64,
                gamePosition.getCol() * GameView.TILE_SIZE,
                gamePosition.getRow() * GameView.TILE_SIZE,
                GameView.TILE_SIZE, GameView.TILE_SIZE);
    }

    /**
     * @param gc           GraphicsContext
     * @param x            Position X dans la Sprite Sheet
     * @param y            Position Y dans la Sprite Sheet
     * @param gamePosition La position dans la fenêtre du jeu.
     */
    public static void drawSimpleTexture(GraphicsContext gc, int x, int y, ICoordinate gamePosition) {
        gc.drawImage(
                GameView.spritesheet, x, y, 64, 64,
                gamePosition.getCol() * GameView.TILE_SIZE,
                gamePosition.getRow() * GameView.TILE_SIZE,
                GameView.TILE_SIZE, GameView.TILE_SIZE);
    }

    /**
     * @param gc                  GraphicsContext
     * @param spritesheetPosition La position X et Y dans la Sprite Sheet, pour
     *                            réference voir les images dans resources/images.
     * @param x                   Position X dans le jeu
     * @param y                   Position Y dans le jeu
     */
    public static void drawSimpleTexture(GraphicsContext gc, ICoordinate spritesheetPosition, int x, int y) {
        gc.drawImage(
                GameView.spritesheet, spritesheetPosition.getCol(), spritesheetPosition.getRow(), 64, 64,
                x * GameView.TILE_SIZE,
                y * GameView.TILE_SIZE,
                GameView.TILE_SIZE, GameView.TILE_SIZE);
    }

    /**
     * @param gc GraphicsContext
     * @param sx Position X dans la Sprite Sheet
     * @param sy Position Y dans la Sprite Sheet
     * @param gx Position X dans le jeu
     * @param gy Position Y dans le jeu
     */
    public static void drawSimpleTexture(GraphicsContext gc, int sx, int sy, int gx, int gy) {
        gc.drawImage(
                GameView.spritesheet, sx, sy, 64, 64,
                gx * GameView.TILE_SIZE,
                gy * GameView.TILE_SIZE,
                GameView.TILE_SIZE, GameView.TILE_SIZE);
    }
}
